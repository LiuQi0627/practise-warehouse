> 《messi-skill-system》是我自己日常学习使用的一个标准库，很多内容还非常的不完善，未来我会继续保持内容添加。
> 希望通过这个作品让面试官对我有一个基本的了解，今后我还会继续在工作学习中保持对技术的热情，不断学习不断沉淀。
> 文档难免有对细节描述的不完整之处，更多细节可以直接看代码。
> 注：如果这里的图片挂了，可以阅读项目目录里的pdf文档

# 一、高并发环境下分布式微服务之间的数据一致性
这里的场景取自"提交订单"业务。"提交订单"模块的核心点是：需要调用多个微服务完成业务流程：主流程是订单微服务（order-system），扣减优惠券需要调用市场微服务(market-system)，扣减库存需要调用商品微服务(product-system)。在流程中，需要三个不同的微服务全部成功处理数据。

随着系统方案的升级优化，一共使用3种不同的技术方案实现此业务。分别是：使用分布式事务保证数据的强一致性、使用MQ事务消息对不同微服务做解耦和自研本地消息表实现数据最终一致性。
为了更直观的比较三种实现方案的优劣，三个不同实现版本的业务流程和方法入参完全一致，唯一区别是：

- v1版本：保存订单到订单数据库、扣减优惠券、扣减库存使用分布式事务强绑定；
- v2版本：扣减优惠券、扣减库存发送事务消息，由各自的微服务消费端消费到消息后做扣减；
- v3版本：保存订单到订单数据库、扣减优惠券、扣减库存生成3个最终一致性任务，保存在本地消息表。
## 1.1、SeataAT分布式刚性事务实现数据一致性保证
> 代码入口： com.messi.system.order.service.impl.submit.SubmitOrderServiceV1Impl

```java
@Override
@GlobalTransactional
public ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq) {

    //  1、入参检查
    super.checkReqParam(submitOrderReq);

    //  2、调用用户中心，验证用户
    super.checkUser(submitOrderReq);

    //  3、调用商品中心，验证商品
    super.checkProduct(submitOrderReq);

    //  4、调用营销中心，验证优惠券
    CheckCouponDTO checkCouponDTO = super.checkCoupon(submitOrderReq);

    //  5、调用营销中心，计算订单价格
    List<CheckOrderPriceDTO> calculatedOrderPrices = super.calculateOrderPrice(submitOrderReq);

    //  6、生成订单和条目id
    super.generateOrderAndOrderItemId(submitOrderReq);

    //  7、验证订单价格
    super.checkOrderPrice(submitOrderReq.getOrderItemPriceList(), calculatedOrderPrices);

    //  8、做订单价格优惠扣减
    Integer finalPrice = super.deductionOrderPrice(checkCouponDTO, calculatedOrderPrices);

    //  9、分布式事务扣优惠券MQ
    this.distributedTransactionDeductionCoupon(checkCouponDTO);

    //  10、分布式事务扣库存MQ
    this.distributedTransactionDeductionStock(submitOrderReq.getOrderProductList());

    //  11、构造订单
    Order order = super.buildOrder(submitOrderReq, finalPrice);

    //  12、保存订单
    super.saveOrder(order);

    //  13、发送延迟关单MQ
    super.sendDelayCancelOrder(order);

    //  14、返回响应信息
    log.info("v1创建订单完成,orderId:{}",order.getOrderInfoDO().getOrderId());
    return super.submitResponse(submitOrderReq);

}
```

分布式事务是刚性事务，需要加全局锁，在各个分支事务的执行前后要查询数据镜像，插入undo log日志。
如果所有的分支事务都执行成功，最后会提交全局事务，删除undo log。
如果有分支执行失败，每个分支都根据undo log做回滚。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674972251355-7b133aca-27eb-483b-99b5-da4eeea1fb63.png#averageHue=%23f5f5f5&clientId=u281de299-28d4-4&from=paste&height=489&id=u3e14d4e9&name=image.png&originHeight=855&originWidth=1556&originalType=binary&ratio=1&rotation=0&showTitle=false&size=234596&status=done&style=none&taskId=u9bc20b9b-b971-4481-84bf-c7a862ce088&title=&width=889.1428571428571)

## 1.2、RocketMQ柔性事务消息实现数据一致性保证
> 代码入口：com.messi.system.order.service.impl.submit.SubmitOrderServiceV2Impl

```java
@Override
public ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq) {

    //  1、入参检查
    super.checkReqParam(submitOrderReq);

    //  2、调用用户中心，验证用户
    super.checkUser(submitOrderReq);

    //  3、调用商品中心，验证商品
    super.checkProduct(submitOrderReq);

    //  4、调用营销中心，验证优惠券
    CheckCouponDTO checkCouponDTO = super.checkCoupon(submitOrderReq);

    //  5、调用营销中心，计算订单价格
    List<CheckOrderPriceDTO> calculatedOrderPrices = super.calculateOrderPrice(submitOrderReq);

    //  6、生成订单和条目id
    super.generateOrderAndOrderItemId(submitOrderReq);

    //  7、验证订单价格
    super.checkOrderPrice(submitOrderReq.getOrderItemPriceList(), calculatedOrderPrices);

    //  8、做订单价格优惠扣减
    Integer finalPrice = super.deductionOrderPrice(checkCouponDTO, calculatedOrderPrices);

    //  9、发送事务消息，做扣减操作
    this.sendTransactionMsgDeduction(checkCouponDTO, submitOrderReq.getOrderProductList());

    //  10、构造订单
    Order order = super.buildOrder(submitOrderReq, finalPrice);

    //  11、保存订单
    super.saveOrder(order);

    //  12、发送延迟关单MQ
    super.sendDelayCancelOrder(order);

    //  13、返回响应信息
    log.info("v2创建订单完成,orderId:{}",order.getOrderInfoDO().getOrderId());
    return super.submitResponse(submitOrderReq);

}
```

使用rocketmq柔性事务方案来替换刚性的分布式事务，避免在业务执行时产生大量的全局锁。
柔性事务指的是，consumer首先会向topic发送一个half消息，topic会返回half消息的响应，确保双方的消息链路是通的。然后执行当前的数据SQL，执行成功后，consumer再发送给topic完整的消息，此时topic接收到完整的消息后，代表着SQL也执行成功。
如果发送half消息时响应失败，代表双方的通信链路不同，此时consumer会稍后重新发送half消息。
如果再提交完整信息时失败，则SQL回滚。
如果因为网络故障，或者系统故障等原因导致half响应成功，但是完整消息提交失败，此时topic会反向对consumer做事务回查，检查事务状态。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674974793474-c48049c2-a2ee-4931-beec-7e3e646a2404.png#averageHue=%23f8f8f8&clientId=u281de299-28d4-4&from=paste&height=434&id=u48e470fb&name=image.png&originHeight=759&originWidth=1751&originalType=binary&ratio=1&rotation=0&showTitle=false&size=167574&status=done&style=none&taskId=uc0a7f708-b6fa-4d19-94ac-06c68df8348&title=&width=1000.5714285714286)
## 1.3、自研最终一致性框架+本地消息表+定时轮询实现数据一致性保证
> 代码入口：com.messi.system.order.service.impl.submit.SubmitOrderServiceV3Impl

```java
@Override
public ResResult<SubmitOrderDTO> submitOrder(SubmitOrderReq submitOrderReq) {

    //  1、入参检查
    super.checkReqParam(submitOrderReq);

    //  2、调用用户中心，验证用户
    super.checkUser(submitOrderReq);

    //  3、调用商品中心，验证商品
    super.checkProduct(submitOrderReq);

    //  4、调用营销中心，验证优惠券
    CheckCouponDTO checkCouponDTO = super.checkCoupon(submitOrderReq);

    //  5、调用营销中心，计算订单价格
    List<CheckOrderPriceDTO> calculatedOrderPrices = super.calculateOrderPrice(submitOrderReq);

    //  6、生成订单和条目id
    super.generateOrderAndOrderItemId(submitOrderReq);

    //  7、验证订单价格
    super.checkOrderPrice(submitOrderReq.getOrderItemPriceList(), calculatedOrderPrices);

    //  8、做订单价格优惠扣减
    Integer finalPrice = super.deductionOrderPrice(checkCouponDTO, calculatedOrderPrices);

    //  9、最终一致性框架扣优惠券MQ
    submitOrderV3.deductionCoupon(checkCouponDTO);

    //  10、最终一致性框架扣库存MQ
    submitOrderV3.frameworkDeductionStock(submitOrderReq.getOrderProductList());

    //  11、构造订单
    Order order = super.buildOrder(submitOrderReq, finalPrice);

    //  12、最终一致性框架保存订单
    submitOrderV3.saveOrder(order);

    //  13、发送延迟关单MQ
    super.sendDelayCancelOrder(order);

    //  14、返回响应信息
    log.info("v3创建订单完成,orderId:{}",order.getOrderInfoDO().getOrderId());
    return super.submitResponse(submitOrderReq);

}
```

这个方案使用自研最终一致性框架+本地消息表+定时任务组合实现。
最终一致性框架的执行流程说明：

1. 最终一致性框架：在业务方法上标注@ConsistencyFramework注解，在执行到此方法时，通过SpringAOP切面拦截执行方法，拆解执行方法的参数类型、全限定名、参数类型、方法入参、方法返回值等，将拆解的方法保存到本地的执行方法表。
2. 保存执行方法实例到本地消息表以后，强制让业务流程结束，当前业务并不执行业务方法。
3. 开启XXL-JOB定时轮询任务，查询本地方法表中的待执行业务方法，通过反射机制，重新拼装执行方法和方法入参，调用方法执行。
4. 如果方法执行成功，删除本地消息表，如果方法执行失败，下次轮询调用时还会重新启动方法。这样不断的执行，直到业务方法执行成功。

可以优化的点：

- 本地消息表支持分库分表
- 设置失败次数上限，超过上限可告警
- 配置执行失败的任务可以手动拉起

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674977363015-04085f7e-bcf0-41ea-b34e-635421947868.png#averageHue=%23fafafa&clientId=u281de299-28d4-4&from=paste&height=577&id=u58503346&name=image.png&originHeight=1010&originWidth=1855&originalType=binary&ratio=1&rotation=0&showTitle=false&size=163148&status=done&style=none&taskId=ubc98d90d-27d4-43d9-8e3c-34434fa0f81&title=&width=1060)
本地消息表：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674977505750-5f597c62-acc6-4081-850f-de647c16a515.png#averageHue=%23f8f6f5&clientId=u281de299-28d4-4&from=paste&height=395&id=u32e148ab&name=image.png&originHeight=692&originWidth=1634&originalType=binary&ratio=1&rotation=0&showTitle=false&size=216178&status=done&style=none&taskId=u88309caa-f776-4bd4-bb50-82b7ed59d5e&title=&width=933.7142857142857)
本地消息表中的测试数据：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674975269241-a0ee6d4d-3ce8-4d8c-a371-5e623cc82e31.png#averageHue=%23e7e3df&clientId=u281de299-28d4-4&from=paste&height=462&id=u4826d2b0&name=image.png&originHeight=809&originWidth=3376&originalType=binary&ratio=1&rotation=0&showTitle=false&size=405925&status=done&style=none&taskId=ufde13bf3-be62-4d0b-9cc9-398ae916724&title=&width=1929.142857142857)
执行方法举例：
```java
@Override
@ConsistencyFramework(taskName = "frameworkDeductionCoupon")
public void frameworkDeductionCoupon(CheckCouponDTO checkCouponDTO) {
    couponRemote.deductionCoupon(checkCouponDTO);
}
```
注解拦截入口：
```java
com.messi.system.consistency.aspect.AnnotationAspect
```
XXL-JOB定时轮询执行任务入口：
```java
com.messi.system.order.xxl.scheduler.FrameworkSchedulerTask#execConsistencyTask
```
## 1.4、三种方案的执行效率比对
本次单次请求的结果比对有2个前提：

1. 全部都是项目启动后的首次请求；
2. 全部中间件没有做任何的参数优化。

使用分布式事务提交订单，执行时间1569ms。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673960999175-fb949e2d-28a1-48ad-a371-0b2c43071c0f.png#averageHue=%23fcfbfb&clientId=uf9a4d707-bc00-4&from=paste&height=776&id=u413a3c4f&name=image.png&originHeight=1358&originWidth=1250&originalType=binary&ratio=1&rotation=0&showTitle=false&size=181548&status=done&style=none&taskId=u6a3834b9-e980-4231-82ae-cbc42219f9e&title=&width=714.2857142857143)

使用事务消息提交订单，执行时间962ms。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673961102135-84c5c2cc-b67a-445e-a3f4-26bd65ca98cc.png#averageHue=%23fcfbfb&clientId=uf9a4d707-bc00-4&from=paste&height=767&id=u8524a1e4&name=image.png&originHeight=1343&originWidth=1245&originalType=binary&ratio=1&rotation=0&showTitle=false&size=180869&status=done&style=none&taskId=uc067d1db-40ed-437a-86de-4e2a8516975&title=&width=711.4285714285714)

使用本地消息表+最终一致性框架提交订单，执行时间80ms，和上面两个的差距是非常大的。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673957983579-ae7193b4-02b5-4e3c-809d-4973a2dedc23.png#averageHue=%23fcfbfb&clientId=ue0340be3-8b03-4&from=paste&height=773&id=ud2544e45&name=image.png&originHeight=1353&originWidth=1260&originalType=binary&ratio=1&rotation=0&showTitle=false&size=179623&status=done&style=none&taskId=u429122bb-74ec-47ba-8312-89076fb2b3e&title=&width=720)
结论：按请求处理的响应时间先后排序：本地消息表+最终一致性框架 > 事务消息 > 分布式事务。
# 二、流量洪峰下保证系统高可用
在大流量冲击下，系统首先要保证的是不被流量击垮，其次才是让每个请求都可以完成最终的执行。这里以大流量冲击的抢购场景为例。
大流量的特征是持续时间短，瞬时流量大，所以在这种场景下，核心任务是流量的层层削峰，请求降级，增加缓存，避免对系统服务和数据库造成冲击，导致系统崩溃。
基本流程类似于下图：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674958015234-f54d81ea-4734-4df1-92dc-495113f4f06f.png#averageHue=%23fafafa&clientId=u281de299-28d4-4&from=paste&height=399&id=u44530e69&name=image.png&originHeight=699&originWidth=806&originalType=binary&ratio=1&rotation=0&showTitle=false&size=46453&status=done&style=none&taskId=uf8de964e-cdce-4872-aadf-781c728a3f5&title=&width=460.57142857142856)
## 2.1、双总线架构异步执行流程概览
这是一个高性能框架，可以保证在瞬时流量冲击下维持系统的高可用。
它是参考Reactor模型，基于disruptor、线程池、semaphore信号量、自研双总线架构组成的。如果熟悉Reactor模型的话，就会直到这套高并发设计模型在nginx，redis上都得到了重用。它的核心思想是有1个管理总线负责轮询请求，后面有N个工作总线负责执行具体的工作。
管理总线会根据处理事件的不同类型，将请求转发给不同的事件处理器，减少请求阻塞的时间。
在这个场景中，使用disruptor作为Jvm内存队列保存数据事件、自定义BossEventBus负责转发事件，自定义WorkerEventBus负责做真正的业务执行。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674960196130-a06dc667-31a5-4a57-ab35-cb8f68972832.png#averageHue=%23f8f8f8&clientId=u281de299-28d4-4&from=paste&height=393&id=u9c8683bd&name=image.png&originHeight=688&originWidth=1669&originalType=binary&ratio=1&rotation=0&showTitle=false&size=111286&status=done&style=none&taskId=ud034532b-694e-438a-a2fa-769384937cd&title=&width=953.7142857142857)
## 2.2、组件说明

- BossEventBus：基于disruptor实现的自定义管理总线，负责转发请求，全局1个线程
```java
com.messi.snap.up.reactor.bus.BossEventBus
```

- WorkerEventBus：基于disruptor实现的自定义工作总线，做事件的具体执行。以事件维度来说，业务上有多少个事件，就需要创建多少个WorkerEventBus。以线程维度来说，每个WorkerEventBus可以配置多个执行线程。
```java
com.messi.snap.up.reactor.bus.WorkEventBus
```

- 监听事件：使用自定义注解Channel标注的监听事件，每个事件负责一段执行的业务。一套完整的业务流程，需要拆分成多个监听事件。
```java
com.messi.snap.up.reactor.listener.snapup.ProcessListener1
```

- 线程池：自定义线程池，内部封装semaphore信号量，用于执行监听事件。
```java
com.messi.snap.up.reactor.executor.WorkerThreadExecutor
```
## 2.3、源码核心流程简述
这里的实现很难用文字说清楚，直接看代码和流程图会理解的比较明白。
系统启动时，会初始化总线组件。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674959864361-632d5237-e922-4471-8610-c0bbc99632e4.png#averageHue=%23f8f8f8&clientId=u281de299-28d4-4&from=paste&height=365&id=u5fe57ae0&name=image.png&originHeight=638&originWidth=1386&originalType=binary&ratio=1&rotation=0&showTitle=false&size=115444&status=done&style=none&taskId=u8f85d906-961f-4e71-8df9-d48f18f6b87&title=&width=792)
加载总线自定义配置。
```java
com.messi.snap.up.config.EventBusAutoConfiguration
```
```java
messi:
  snap:
    up:
      # 匹配 worker bus，每个线程池执行一个worker bus的事件
      threads:
        - threadPool: Process1
          threadCount: 1

        - threadPool: Process2
          threadCount: 1

        - threadPool: Process3
          threadCount: 1

        - threadPool: Process4
          threadCount: 1

        - threadPool: Process5
          threadCount: 1
      jedis:
        redisAddress:
          - 192.168.2.110:6379
          - 192.168.2.111:6379
          - 192.168.2.112:6379
          - 192.168.2.113:6379
      event:
        bus:
          # boss bus 全局只有一个
          boss:
            ringBufferSize: 4096
            eventHandlerNum: 1

          # 有几个流程，就配置几个worker bus
          workers:
            - channel: Process1
              ringBufferSize: 4096
              eventHandlerNum: 1

            - channel: Process2
              ringBufferSize: 4096
              eventHandlerNum: 1

            - channel: Process3
              ringBufferSize: 4096
              eventHandlerNum: 1

            - channel: Process4
              ringBufferSize: 4096
              eventHandlerNum: 1

            - channel: Process5
              ringBufferSize: 4096
              eventHandlerNum: 1
```

## 2.4、双总线架构的使用场景
在这里，双总线架构使用在了"高流量抢购"的场景，核心流程如下：

1. 配合运维，部署CDN、做抢购页面的活动预热、做Nginx限流（当前在代码中暂时省略）
2. 瞬时流量来了之后，sentinel中间件做限流（当前在代码中暂时省略）
3. 使用sentinel中间件做服务降级，因不可控原因导致系统报错后，返回给前台友好页面（当前在代码中暂时省略）
4. 过滤掉一部分流量后，进入系统的流量流入BossEventBus。
5. BossEventBus通过BossEventHandler，将不同类型事件转发给WorkerEventBus。
6. 通过WorkEventHandler，遍历所有的自定义监听做回调，通知Listener监听事件。
7. Listener监听到事件后，调用自定义WorkerThreadExecutor线程池执行真实的业务。
8. 每次执行完业务后，调用BossEventBus，发布下一步要做的事件，等待BossEventBus将事件转发给下一个WorkerEventBus。

启动messi-snapup-system服务后，首先在postman上执行
系统启动完毕以后，发送“17、抢购商品分配库存”分配库存。
这里部署4台单机redis，没有使用sentinel哨兵，也没有使用redis cluster。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674963618821-dc03645e-3458-4c69-a330-fcb4ad7ac62c.png#averageHue=%23faf9f8&clientId=u281de299-28d4-4&from=paste&height=553&id=u6e5c62e1&name=image.png&originHeight=968&originWidth=1208&originalType=binary&ratio=1&rotation=0&showTitle=false&size=121807&status=done&style=none&taskId=u87d4d8cd-01ea-4864-8817-a55a134ca8b&title=&width=690.2857142857143)
然后执行“18、发起抢购”请求。
![QQ截图20230128224209.jpg](https://cdn.nlark.com/yuque/0/2023/jpeg/1477039/1674961344933-a6b8387d-8807-408b-95dd-ab3afcfca6fc.jpeg#averageHue=%2395bbe2&clientId=u281de299-28d4-4&from=paste&height=162&id=CwH1w&name=QQ%E6%88%AA%E5%9B%BE20230128224209.jpg&originHeight=284&originWidth=1796&originalType=binary&ratio=1&rotation=0&showTitle=false&size=130597&status=done&style=none&taskId=u260c8925-33e1-4b02-8fd8-2e322a1eae6&title=&width=1026.2857142857142)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674963180752-b83af2d9-619a-4c50-8742-ddc4231500b8.png#averageHue=%23fbfbfb&clientId=u281de299-28d4-4&from=paste&height=548&id=u1a17d1e9&name=image.png&originHeight=959&originWidth=1031&originalType=binary&ratio=1&rotation=0&showTitle=false&size=68510&status=done&style=none&taskId=uda6b809b-255b-4b65-a5de-d367031be92&title=&width=589.1428571428571)
## 2.5、真实的千万流量抢购场景
双总线架构是实现真实抢购业务的核心实现，想要支持千万流量的高可用，不仅要有合理的硬件机器规划、运维配合，还要有一套完整的抢购业务流程，涉及到的点非常多。下面是真实完整的抢购业务图。
![秒杀抢购项目流程和项目亮点 (1).png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674963824039-eaca73b0-f401-4f89-85f1-41105e73aa65.png#averageHue=%23fbfbfb&clientId=u281de299-28d4-4&from=paste&height=1251&id=u42ff32e0&name=%E7%A7%92%E6%9D%80%E6%8A%A2%E8%B4%AD%E9%A1%B9%E7%9B%AE%E6%B5%81%E7%A8%8B%E5%92%8C%E9%A1%B9%E7%9B%AE%E4%BA%AE%E7%82%B9%20%281%29.png&originHeight=2190&originWidth=4109&originalType=binary&ratio=1&rotation=0&showTitle=false&size=1169832&status=done&style=none&taskId=ua22a0e32-bdbe-4c38-a944-34d4f6f8da6&title=&width=2348)
# 三、海量数据下的数据异构存储方案
数据异构存储的演化是随着业务量级的增大而变化的。
在项目初期，一个单体应用对应一个单库单表就可以，在系统拆分成微服务以后，可以视实际的业务情况，决定要不要做数据库的垂直拆分，也就是每个微服务独立连接单独指定的数据库。
在微服务项目运行一段时间以后，随着数据量的继续增大，此时垂直分库也逐渐无法满足日常的运行要求了。比如单库单表的数据量在千万级以上的时候，做查询操作时的时间损耗一定是增加的，此时就可以考虑做分库分表。分库分表本质上也是一种以空间换时间的策略，将单库单表拆分成多库多表，通过指定的业务ID做路由，让每次请求都通过业务ID去指定的库表中查询数据。
## 3.1、10万级数据的单表查询优化
以"查询订单分页"方法为例，代码入口：
```java
com.messi.system.order.controller.OrderController#queryOrderPageByJoinTable
```
该查询方法的SQL是三张表联表查询，通过create_time做倒序排序。
```java
SELECT o.order_id AS orderId,
o.order_type AS orderType,
o.order_status AS orderStatus,
o.order_cancel_time AS orderCancelTime,
o.seller_id AS sellerId,
o.user_id AS userId,
o.total_amount AS totalAmount,
o.actual_amount AS actualAmount,
o.order_pay_type AS orderPayType,
o.pay_time AS payTime,
o.coupon_id AS couponId,
o.channel AS channel,
o.appraise_status AS appraiseStatus,
item.order_item_id AS orderItemId,
item.product_id AS productId,
item.sku_id AS skuId,
item.sale_num AS saleNum,
item.sale_price AS salePrice,
price_details.order_item_price AS orderItemPrice,
o.create_time AS createTime,
o.modified_time AS modifiedTime
FROM order_info AS o
LEFT JOIN order_item_info AS item on o.order_id = item.order_id
LEFT join order_price_details AS price_details on o.order_id = price_details.order_id
<where>
    <if test='query.orderStatuses != null and query.orderStatuses.size !=0'>
        <foreach collection='query.orderStatuses' item='orderStatus' open=' AND o.order_status in (' close=')'
                 separator=','>
            #{orderStatus}
        </foreach>
    </if>
    <if test='query.sellerIds != null and query.sellerIds.size !=0'>
        <foreach collection='query.sellerIds' item='sellerId' open=' AND o.seller_id in (' close=')'
                 separator=','>
            #{sellerId}
        </foreach>
    </if>
    <if test='query.userIds != null and query.userIds.size !=0'>
        <foreach collection='query.userIds' item='userId' open=' AND o.user_id in (' close=')' separator=','>
            #{userId}
        </foreach>
    </if>
    <if test='query.orderPayTypes != null and query.orderPayTypes.size !=0'>
        <foreach collection='query.orderPayTypes' item='orderPayType' open=' AND o.order_pay_type in ('
                 close=')' separator=','>
            #{orderPayType}
        </foreach>
    </if>
    <if test='query.appraiseStatuses != null and query.appraiseStatuses.size !=0'>
        <foreach collection='query.appraiseStatuses' item='appraiseStatus' open=' AND o.appraise_status in ('
                 close=')' separator=','>
            #{appraiseStatus}
        </foreach>
    </if>
</where>
ORDER BY o.create_time DESC
```

在每张数据表都没有索引的前提下，对10万数据量级发起联表查询，无法查询成功。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674127463749-a2082d38-728b-45f8-a140-f1709d64ed50.png#averageHue=%23f9f8f8&clientId=uad62edf4-7082-4&from=paste&height=415&id=diO1Z&name=image.png&originHeight=726&originWidth=1137&originalType=binary&ratio=1&rotation=0&showTitle=false&size=98247&status=done&style=none&taskId=u0e40fad1-658f-4726-96c4-e72c901c39d&title=&width=649.7142857142857)
使用explan查看SQL的执行计划，可见三张联表的查询没有走索引。
```sql
EXPLAIN SELECT
	o.order_id AS orderId,
	o.order_type AS orderType,
	o.order_status AS orderStatus,
	o.order_cancel_time AS orderCancelTime,
	o.seller_id AS sellerId,
	o.user_id AS userId,
	o.total_amount AS totalAmount,
	o.actual_amount AS actualAmount,
	o.order_pay_type AS orderPayType,
	o.pay_time AS payTime,
	o.coupon_id AS couponId,
	o.channel AS channel,
	o.appraise_status AS appraiseStatus,
	item.order_item_id AS orderItemId,
	item.product_id AS productId,
	item.sku_id AS skuId,
	item.sale_num AS saleNum,
	item.sale_price AS salePrice,
	price_details.order_item_price AS orderItemPrice,
	o.create_time AS createTime,
	o.modified_time AS modifiedTime 
FROM
	order_info AS o
	LEFT JOIN order_item_info AS item ON o.order_id = item.order_id
	LEFT JOIN order_price_details AS price_details ON o.order_id = price_details.order_id 
WHERE
	o.order_status IN ( 20, 30, 40 ) 
	AND o.seller_id IN ( 0, 1 ) 
	AND o.user_id IN ( 0, 1 ) 
	AND o.order_pay_type IN ( 0, 1, 2 ) 
	AND o.appraise_status IN ( 0, 1 ) 
ORDER BY
	o.create_time DESC 
	LIMIT 20
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674134612192-561c16cc-86b8-4176-b6d5-95ddcc33ce28.png#averageHue=%23f3f2f0&clientId=uad62edf4-7082-4&from=paste&height=122&id=tMdd3&name=image.png&originHeight=213&originWidth=1692&originalType=binary&ratio=1&rotation=0&showTitle=false&size=60243&status=done&style=none&taskId=u67e592d7-d934-46de-95f2-84677a91448&title=&width=966.8571428571429)
因为三张联表是通过order_id关联的，并且每次的order_id都是不同的，所以先给三张表增加order_id的唯一索引。
```sql
ALTER TABLE `messi_order_system`.`order_info` 
ADD UNIQUE INDEX `order_id_index`(`order_id`) USING BTREE COMMENT 'order_id_index';

ALTER TABLE `messi_order_system`.`order_item_info` 
ADD UNIQUE INDEX `order_id_index`(`order_id`) USING BTREE COMMENT 'order_id_index';

ALTER TABLE `messi_order_system`.`order_price_details` 
ADD UNIQUE INDEX `order_id_index`(`order_id`) USING BTREE COMMENT 'order_id_index';
```

再次使用查看SQL执行计划，主表order_info表目前还是ALL全表扫描，两张关联表的type是eq_ref，是唯一索引扫描。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674138413216-e48b51df-d30c-4e70-ab40-d39682266550.png#averageHue=%23f4f3f2&clientId=uad62edf4-7082-4&from=paste&height=132&id=Z87Zr&name=image.png&originHeight=231&originWidth=1492&originalType=binary&ratio=1&rotation=0&showTitle=false&size=49549&status=done&style=none&taskId=uf905a598-c6ee-4f67-826b-753ab5158b5&title=&width=852.5714285714286)
此时的查询时间是847ms。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674138455691-1897306d-78c5-472d-ba90-dca6fb4db46a.png#averageHue=%23fbfafa&clientId=uad62edf4-7082-4&from=paste&height=859&id=QJe0p&name=image.png&originHeight=1503&originWidth=1130&originalType=binary&ratio=1&rotation=0&showTitle=false&size=212624&status=done&style=none&taskId=ua412780c-7264-4266-a24d-4f54421253f&title=&width=645.7142857142857)
继续调整索引，一般单张数据表的索引3~5个，我们继续给order_info表的其他关联查询字段增加普通索引。
```sql
ALTER TABLE `messi_order_system`.`order_info` 
ADD INDEX `order_status_index` ( `order_status` ) USING BTREE COMMENT 'order_status_index';

ALTER TABLE `messi_order_system`.`order_info` 
ADD INDEX `order_seller_id_index` ( `seller_id` ) USING BTREE COMMENT 'order_seller_id_index';

ALTER TABLE `messi_order_system`.`order_info` 
ADD INDEX `order_user_id_index` ( `user_id` ) USING BTREE COMMENT 'order_user_id_index';

ALTER TABLE `messi_order_system`.`order_info` 
ADD INDEX `order_pay_type_index` ( `order_pay_type` ) USING BTREE COMMENT 'order_pay_type_index';

ALTER TABLE `messi_order_system`.`order_info` 
ADD INDEX `appraise_status_index` ( `appraise_status` ) USING BTREE COMMENT 'appraise_status_index';
```
再次查看SQL执行计划，发现order_info表还是全表扫描，虽然在possible_keys列里展示出了刚刚添加的索引，但是不一定会被查询实际使用到。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674139074027-af3554fd-ac6f-410a-aa76-ee1779330b76.png#averageHue=%23f6f5f4&clientId=uad62edf4-7082-4&from=paste&height=135&id=NmN51&name=image.png&originHeight=237&originWidth=2485&originalType=binary&ratio=1&rotation=0&showTitle=false&size=58087&status=done&style=none&taskId=u8489b327-eeae-4d39-8a85-b209e262d10&title=&width=1420)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674138966317-ad371475-8810-49cb-9b7f-f3b32f63305d.png#averageHue=%23faf9f9&clientId=uad62edf4-7082-4&from=paste&height=466&id=GJyxL&name=image.png&originHeight=816&originWidth=1405&originalType=binary&ratio=1&rotation=0&showTitle=false&size=119834&status=done&style=none&taskId=u0e2f11cc-a2f5-4596-8959-2883c97a874&title=&width=802.8571428571429)
到这里还没完，目前调整SQL，将LEFT JOIN 修改成JOIN后，执行EXPLAIN计划。
```sql
EXPLAIN SELECT
	o.order_id AS orderId,
	o.order_type AS orderType,
	o.order_status AS orderStatus,
	o.order_cancel_time AS orderCancelTime,
	o.seller_id AS sellerId,
	o.user_id AS userId,
	o.total_amount AS totalAmount,
	o.actual_amount AS actualAmount,
	o.order_pay_type AS orderPayType,
	o.pay_time AS payTime,
	o.coupon_id AS couponId,
	o.channel AS channel,
	o.appraise_status AS appraiseStatus,
	item.order_item_id AS orderItemId,
	item.product_id AS productId,
	item.sku_id AS skuId,
	item.sale_num AS saleNum,
	item.sale_price AS salePrice,
	price_details.order_item_price AS orderItemPrice,
	o.create_time AS createTime,
	o.modified_time AS modifiedTime 
FROM
	order_price_details AS price_details
	JOIN order_item_info AS item ON price_details.order_id = item.order_id
	JOIN order_info AS o ON price_details.order_id = o.order_id 
WHERE
	o.order_status IN ( 20, 30, 40 ) 
	AND o.seller_id IN ( 0, 1 ) 
	AND o.user_id IN ( 0, 1 ) 
	AND o.order_pay_type IN ( 0, 1, 2 ) 
	AND o.appraise_status IN ( 0, 1 ) 
ORDER BY
	o.create_time DESC 
	LIMIT 20
```
这时的order_item_info表和order_info表使用了索引，但是order_info主表还是全表扫描。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674143609650-463aa615-60a0-499d-b35a-f966556d0bd0.png#averageHue=%23f6f4f2&clientId=uad62edf4-7082-4&from=paste&height=150&id=yLs7A&name=image.png&originHeight=262&originWidth=2339&originalType=binary&ratio=1&rotation=0&showTitle=false&size=65240&status=done&style=none&taskId=u95a48ea4-bab6-46e2-aff7-10b6342e89f&title=&width=1336.5714285714287)
这里的order_info主表全表扫描的原因是，IN条件过多会让索引失效，MySQL自动切换到全表扫描。
如果要强制使用索引，就在FROM 表明后面添加 FORCE  (索引名称）
```sql
EXPLAIN SELECT
	o.order_id AS orderId,
	o.order_type AS orderType,
	o.order_status AS orderStatus,
	o.order_cancel_time AS orderCancelTime,
	o.seller_id AS sellerId,
	o.user_id AS userId,
	o.total_amount AS totalAmount,
	o.actual_amount AS actualAmount,
	o.order_pay_type AS orderPayType,
	o.pay_time AS payTime,
	o.coupon_id AS couponId,
	o.channel AS channel,
	o.appraise_status AS appraiseStatus,
	item.order_item_id AS orderItemId,
	item.product_id AS productId,
	item.sku_id AS skuId,
	item.sale_num AS saleNum,
	item.sale_price AS salePrice,
	price_details.order_item_price AS orderItemPrice,
	o.create_time AS createTime,
	o.modified_time AS modifiedTime 
FROM
	order_info AS o FORCE index(order_status_index,order_seller_id_index,order_user_id_index,order_pay_type_index,appraise_status_index)
	JOIN order_item_info AS item ON o.order_id = item.order_id
	JOIN order_price_details AS price_details ON o.order_id = price_details.order_id 
WHERE
	o.order_status IN ( 20,30,40 ) 
	
	AND o.seller_id IN ( 0, 1 ) 
	AND o.user_id IN ( 0, 1 ) 
	AND o.order_pay_type IN ( 0, 1, 2 ) 
	AND o.appraise_status IN ( 0, 1 ) 
ORDER BY
	o.create_time DESC 
	LIMIT 20;
```
强制使用索引后，再次查看执行计划，order_info表的type变成range。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674143866718-de082e82-cc41-4565-9e75-204b6b2e3e87.png#averageHue=%23f5f3f2&clientId=uad62edf4-7082-4&from=paste&height=134&id=YJdvF&name=image.png&originHeight=234&originWidth=2368&originalType=binary&ratio=1&rotation=0&showTitle=false&size=59810&status=done&style=none&taskId=ud4e44de1-15ec-4d69-b193-ec6d7c164ab&title=&width=1353.142857142857)
typoe字段的执行效率从慢到快排序：
ALL < INDEX < RANGE < REF < EQ_REF< CONST < SYSTEM < NULL
更新强制使用索引后，再次发起查询，发现查询的时间反而变久了。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674144337387-15e9a669-2507-4767-b7b9-9f7a583aad56.png#averageHue=%23faf9f9&clientId=uad62edf4-7082-4&from=paste&height=452&id=XyXWV&name=image.png&originHeight=791&originWidth=1376&originalType=binary&ratio=1&rotation=0&showTitle=false&size=115941&status=done&style=none&taskId=uc740029e-5436-421d-a5e5-fe567e9a4dd&title=&width=786.2857142857143)
这是因为在10万级以上的数据联表查询的话，如果强制使用索引，虽然让主表也做索引扫描，但是在大数据量下的回表的效率并不高，所以非常可能最终的执行效率不如全表扫描。
这里的优化思路是：
1、尽量不要做JOIN关联查询；
2、做每个单表的数据查询，在代码里做数据的组合拼接；
3、分库分表，不要让单库单表的数据量级太大。
## 3.2、有关分库分表的方案说明
### 3.2.1、为什么要做分库分表？
如果单张表的数据达到千万级别，在查询时即使使用B+索引做查询，效率也不会很高，所以要控制单表的数据量，一般控制在百万级别是比较合理的。
如果数据库里有很多张表，并且每张表的数据量都很大的话，服务器的存储空间资源是有限的，服务器不会一直扩容。
如果用8C16G的机器部署数据库，一般TPS最好控制在2k/s左右，如果达到每秒6k以上的TPS，数据库的磁盘、IO、网络、内存负载都会很高，数据库容易崩溃。
### 3.2.2、分库分表的原则
秉持着先垂直，再水平的原则做分库分表。
垂直：就是把系统拆分成不同的子微服务系统，每个微服务系统连接的都是自己独立部署的数据库，这样做就让各个微服务维护自己的数据库，不会让一个数据库里存放所有服务的数据。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674096511165-5e9f0c79-d5ae-406d-aaab-0642ebaed097.png#averageHue=%23fbfbfb&clientId=uad62edf4-7082-4&from=paste&height=412&id=ycjpj&name=image.png&originHeight=721&originWidth=1152&originalType=binary&ratio=1&rotation=0&showTitle=false&size=78995&status=done&style=none&taskId=u41c19bfb-1687-4ee2-b6f2-11c603109b3&title=&width=658.2857142857143)
水平：做完垂直区分，再做水平区分。水平拆分是横向的拆分，将一张数据表水平拆分成多张表。比如一张user表，原来存放1000万的数据，如果拆分成4库4表的话，每个数据库就存放1000/4=250万的数据。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674096648362-bcf0f7bf-a404-4c90-944c-4675ef11ce21.png#averageHue=%23fcfcfc&clientId=uad62edf4-7082-4&from=paste&height=498&id=I9jyA&name=image.png&originHeight=872&originWidth=787&originalType=binary&ratio=1&rotation=0&showTitle=false&size=81688&status=done&style=none&taskId=u8f395575-2556-4fe1-ba92-f2245280887&title=&width=449.7142857142857)

### 3.2.3、跨多个库表时的分布式事务
比如在一个事务里针对订单表和订单条目表，需要同时做一个插入或者是更新的操作，订单表在A库01表，订单条目表在B库02表，此时就设计到一个事务要更新多个库的操作。
关于这个场景，目前项目上使用的是自研的最终一致性框架+本地消息表来解决这个痛点。
### 3.2.4、分库分表操作流程

1. 刚开始的时候，业务系统对接单库单表，这里的单库单表可以是垂直拆分之后的，即单个微服务对应单个业务数据库中的单表。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674726090956-a6e671e5-5301-4c4b-a52f-cd47ff82c95c.png#averageHue=%23fbfbfb&clientId=uf60595d8-e715-4&from=paste&height=95&id=udf5f3008&name=image.png&originHeight=166&originWidth=596&originalType=binary&ratio=1&rotation=0&showTitle=false&size=7776&status=done&style=none&taskId=u4ddaf9f6-ee10-4cbd-bce6-b1f920d5c59&title=&width=340.57142857142856)

2. 申请机器，部署4台数据库。

以名为：messi_order_system 的数据库为例，共拆分出4个分库：依次是messi_order_system_0，messi_order_system_1，messi_order_system_2，messi_order_system_3。
messi_order_system 数据库里面有一张 order_info 表，每个分库里拆分出8张表，分别是：order_info_0，order_info_1，order_info_2，order_info_3，order_info_4，order_info_5，order_info_6，order_info_7。
所以整个拆分下来，针对 order_info 单张表，共拆分出32张表。（4个分库*每个库8张表）
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674726840327-26800c05-edf2-4688-8145-c01fe989d667.png#averageHue=%23fbfbfb&clientId=uf60595d8-e715-4&from=paste&height=239&id=ua9a92e4e&name=image.png&originHeight=418&originWidth=790&originalType=binary&ratio=1&rotation=0&showTitle=false&size=25183&status=done&style=none&taskId=ub42dd760-1426-41af-8ced-d343adb1152&title=&width=451.42857142857144)

3. 建库建表可以是手动创建，也可以通过ShardingJdbc的代码创建，为了简便我在这里是手动建库建表。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674787493701-99c3a290-d1a9-4702-b1a8-6001bf46e3b4.png#averageHue=%23f3f2f0&clientId=u808b54f6-3351-4&from=paste&height=514&id=u3cffdbc9&name=image.png&originHeight=899&originWidth=1578&originalType=binary&ratio=1&rotation=0&showTitle=false&size=358441&status=done&style=none&taskId=ua70d9bae-d183-4b31-8649-e245322b924&title=&width=901.7142857142857)

4. 建立库表以后，数据从单库单表迁移到分库分表，需要做3个核心操作：全量迁移历史数据、增量迁移项目运行过程中新产生的数据、单库单表和分库分表之间的数据匹配检查。
5. 首先要做全量迁移历史数据，以"2023-1-21 00:00:00"为准，日期之前的数据默认是历史数据，日期之后的数据项目还在运行时产生的增量数据。全量迁移历史数据的任务会查询出指定时间之前的数据，配合shardingJdbc将数据路由到指定的分库分表。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674788076970-cccf4ec1-837e-46f1-87a6-c1e762268772.png#averageHue=%23fcfcfc&clientId=u808b54f6-3351-4&from=paste&height=367&id=uba416d95&name=image.png&originHeight=642&originWidth=1415&originalType=binary&ratio=1&rotation=0&showTitle=false&size=53702&status=done&style=none&taskId=ue6b58ab0-2a4a-447a-ae2d-fe9cff12f74&title=&width=808.5714285714286)

6. 因为项目是在不停机运行，在全量迁移过程中还会产生新的业务数据，所以在全量历史数据迁移完成后，再启动增量历史数据迁移。
7. 增量历史数据会监听单库单表的binlog，执行insert、update、delete等事件，配合shardingJdbc将数据路由到指定的分库分表。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674788245940-f41acfa4-9419-4bb4-ab06-8f90fbd55129.png#averageHue=%23fbfbfb&clientId=u808b54f6-3351-4&from=paste&height=466&id=uf183c6db&name=image.png&originHeight=815&originWidth=1424&originalType=binary&ratio=1&rotation=0&showTitle=false&size=80232&status=done&style=none&taskId=u657a1cf4-c11d-43f3-ab5e-c16c46c8da2&title=&width=813.7142857142857)

8. 将增量历史数据任务和连接单库单表的项目并行运行一段时间，确认数据已经全部迁移完毕后，重新部署连接分库分表环境的项目，并再次并行观察一段时间。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674788302142-24f88595-e2fc-4766-a937-ea1ba68ef05c.png#averageHue=%23fbfbfb&clientId=u808b54f6-3351-4&from=paste&height=474&id=ua0f0ff03&name=image.png&originHeight=830&originWidth=1421&originalType=binary&ratio=1&rotation=0&showTitle=false&size=86476&status=done&style=none&taskId=u566f4eb5-2d95-4269-ac6d-52d68cdc6fb&title=&width=812)

9. 最后将连接单库单表环境的项目下线，执行单库单表和分库分表之间的数据匹配检查任务，确保2个环境的数据一致，此时便完成了分库分表的建立和数据迁移。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674788448843-c121acd0-52a7-4c7e-bc2e-46cea1ca7c4b.png#averageHue=%23fbfbfb&clientId=u808b54f6-3351-4&from=paste&height=570&id=u83cee086&name=image.png&originHeight=998&originWidth=1494&originalType=binary&ratio=1&rotation=0&showTitle=false&size=113178&status=done&style=none&taskId=ubfb0cde8-aa84-41af-9f71-eea2968c12f&title=&width=853.7142857142857)
### 3.2.5、冷热数据分离异构存储方案
这个方案由分库分表、es索引大宽表和hbase组成。
在分布式微服务项目中，操作数据的渠道是多种多样的，但基本上可以划分成2大类：服务端和客户端，简称B端和C端。服务端可以理解成管理端，是给管理人员、运营人员，日常维护人员使用的，对数据的使用要求是精准、高效。客户端是对外提供给大众使用的，任何人都可以使用，简称C端。对数据的使用要求是响应速度快、浏览体验好。
现在，所有的项目数据都保存在分库分表里，那么无论是B端还是C端，无论是新增、更新、查询、删除，本质上还是在频繁的使用数据库。
为了更进一步减轻数据库的使用压力，可以根据数据的使用场景，做冷热数据分离。热数据，指的就是无论B端还是C端，都会经常使用的业务数据，而且一般是以查询为主。冷数据，指的就是可能会一次生成，需要永久保存的数据，但是不会经常拿来使用。比如产生的和业务数据有关联的日志数据、快照数据、备份数据等。
针对这种场景，项目上是这样做的：

1. 首先，所有的数据都会保存在分库分表数据库

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674789209291-6c7ef2c5-981e-4b4e-b20e-f4659522ab84.png#averageHue=%23fcfcfc&clientId=u808b54f6-3351-4&from=paste&height=256&id=u6a2c8ed3&name=image.png&originHeight=448&originWidth=1081&originalType=binary&ratio=1&rotation=0&showTitle=false&size=26964&status=done&style=none&taskId=u49499a58-5300-4514-829a-b74ad35f6fe&title=&width=617.7142857142857)

2. 对接Elasticsearch集群，自定义es大宽表索引，保存绝大部分需要日常查询使用字段值。通过监听binlog，重新生成数据，将数据保存到自定义es索引。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674789484742-a967b4ef-f752-45d6-b342-5374db41e3cc.png#averageHue=%23f3f0ef&clientId=u808b54f6-3351-4&from=paste&height=449&id=uc2bd9964&name=image.png&originHeight=785&originWidth=1566&originalType=binary&ratio=1&rotation=0&showTitle=false&size=296720&status=done&style=none&taskId=u8a64dc92-8e2c-4b6a-bd0d-8109a629bbf&title=&width=894.8571428571429)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674789532868-d76613e6-9593-4686-be90-aa0d8e2f096b.png#averageHue=%23ebeaea&clientId=u808b54f6-3351-4&from=paste&height=375&id=ue26092d0&name=image.png&originHeight=657&originWidth=2517&originalType=binary&ratio=1&rotation=0&showTitle=false&size=295163&status=done&style=none&taskId=uc6f45f20-d0c7-4686-a526-556aa33f637&title=&width=1438.2857142857142)

3. 将es索引用于支撑C端的数据查询。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674789857257-853972e2-5beb-40e1-bc67-5ed29fc07922.png#averageHue=%23fdfdfd&clientId=u808b54f6-3351-4&from=paste&height=653&id=uaab7781a&name=image.png&originHeight=1142&originWidth=1193&originalType=binary&ratio=1&rotation=0&showTitle=false&size=59547&status=done&style=none&taskId=u2f13a5b9-5e0b-40dc-81ce-4f49f79dbca&title=&width=681.7142857142857)

4. B端查询，使用的还是数据库。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674789982821-753f475c-1227-4424-a03c-a008b86fd688.png#averageHue=%23fdfdfd&clientId=u808b54f6-3351-4&from=paste&height=786&id=ud9c4ff04&name=image.png&originHeight=1375&originWidth=1097&originalType=binary&ratio=1&rotation=0&showTitle=false&size=67997&status=done&style=none&taskId=ube4c2241-7a24-4e79-b56d-9caa950aa7c&title=&width=626.8571428571429)

5. 对接hbase集群，保存快照信息等冷数据。

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674790067922-470f1556-de26-4d7b-8469-922f455b4b7b.png#averageHue=%23fcfcfc&clientId=u808b54f6-3351-4&from=paste&height=783&id=u7ae7937d&name=image.png&originHeight=1371&originWidth=1126&originalType=binary&ratio=1&rotation=0&showTitle=false&size=82174&status=done&style=none&taskId=ud05b9045-f506-4389-a4ee-92df01933a7&title=&width=643.4285714285714)
## 3.3、执行数据迁移的方案说明
### 3.3.1、全量历史数据迁移任务
```java
com.messi.system.data.migration.controller.FullDataMigrationController#fullDataMigration
```
全量历史数据迁移任务开启一个线程，不断地从单库单表中查询出指定时间之前的历史数据，将这些数据批量插入分库分表。
```java
com.messi.system.data.migration.service.FullDataMigrationTaskService#migrationFullDataTask
```
打开postman，执行全量迁移数据请求。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674816130788-059a7adc-dd27-4652-91de-c47ded6306b3.png#averageHue=%23f1f0ef&clientId=u808b54f6-3351-4&from=paste&height=492&id=uf1c79dd0&name=image.png&originHeight=861&originWidth=587&originalType=binary&ratio=1&rotation=0&showTitle=false&size=112066&status=done&style=none&taskId=u3ce56af8-1604-4785-9821-b11ed7b48d7&title=&width=335.42857142857144)
### 3.3.2、增量数据迁移任务
先做全量数据迁移，再做增量数据迁移，这两项是串行执行。
在做增量数据迁移之前，要搭建完成分库分表的建立和canal的搭建部署。
> 有关canal的搭建部署见《搭建配置canal》章节。

```java
com.messi.system.data.migration.controller.IncrementalDataMigrationController#incrementDataMigration
```
启动增量迁移数据任务请求后，首先通过自定义的yml配置创建CanalServer对象。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674813671054-a612f60d-1923-4327-997b-6f18a659a915.png#averageHue=%23f8f7f2&clientId=u808b54f6-3351-4&from=paste&height=209&id=u518c1455&name=image.png&originHeight=365&originWidth=655&originalType=binary&ratio=1&rotation=0&showTitle=false&size=35935&status=done&style=none&taskId=uef852090-79ff-493f-8032-163508aaab3&title=&width=374.2857142857143)
然后再开启一个线程，不停地拉取binlog，从binlog中解析出RowData真实地行数据，对行数据做反序列化，从而获取到真实的数据集。
```java
com.messi.system.data.migration.task.IncrementalConsumeBinlogTask
```
监听到的数据库binlog：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674816039305-7fd27f6f-ef67-4d10-ab8c-46a7dadf9429.png#averageHue=%23f9f7f6&clientId=u808b54f6-3351-4&from=paste&height=499&id=u4270e0e2&name=image.png&originHeight=874&originWidth=2405&originalType=binary&ratio=1&rotation=0&showTitle=false&size=202255&status=done&style=none&taskId=u21e74a66-d981-42f3-bc94-cf489c3de93&title=&width=1374.2857142857142)
根据不同的数据表名、不同的事件类型，数据集会解析成不同的实体类型，通过shardingJdbc数据源做向指定路由的分库分表做数据处理。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674816523622-fe8f3bef-9ca6-46f3-a702-02c4123189f3.png#averageHue=%23fcfafa&clientId=u808b54f6-3351-4&from=paste&height=701&id=ubfdfb0ee&name=image.png&originHeight=1227&originWidth=1703&originalType=binary&ratio=1&rotation=0&showTitle=false&size=171349&status=done&style=none&taskId=u9e4759da-fc48-4ed0-a30f-f5dc73c69ef&title=&width=973.1428571428571)
启动 com.messi.system.data.migration.DataMigrationApp 后，使用postman发起监听binlog请求。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674816677401-dfbf08c4-d7bf-4f5a-9585-5933f33c5900.png#averageHue=%23f1efef&clientId=u808b54f6-3351-4&from=paste&height=488&id=u07907039&name=image.png&originHeight=854&originWidth=592&originalType=binary&ratio=1&rotation=0&showTitle=false&size=113938&status=done&style=none&taskId=u65e488b5-e998-47a4-baec-3f128597abe&title=&width=338.2857142857143)
### 3.3.3、全量数据和增量数据的匹配比对
用来比对全量数据和增量数据的差异，如果有差别，就手动将差别数据用代码的方式更新到指定的分库分表中。
主要有2项校验：

1. 数据量是否相等；
2. 数据库maxId是否相同（此项仅限于自增数据库ID）。
## 3.4、执行10万级数据的分库分表迁移
### 3.4.1、总体流程
因为在微服务系统的初期规划时就已使用了垂直分库，即每个微服务单独连接独立的数据库。所以这里做的是水平分库。
水平分库指的是由单库单表转为多库多表，这里将单库拆分为4个分库，每张单表拆分成8个数据分表。即每个单库单表最终会拆分成4库32张表。
假设单库单表保存1千万的数据，分库分表拆分后，支持保存3亿2千万的数据。
### 3.4.2、准备工作

- 重新模拟生成了一份10万条的订单数据
- 准备数据迁移框架代码
- 配置sharding-jdbc做分库分表
- 部署canal监听binlog，用来做增量数据迁移
### 3.4.3、引入sharding-jdbc依赖
因为要给订单系统数据库做分库分表，所以在messi-order-system-service的pom.xml中增加sharding-jdbc的依赖
```xml
<dependency>
  <groupId>org.apache.shardingsphere</groupId>
  <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
  <version>4.1.1</version>
  <scope>compile</scope>
</dependency>
```
### 3.4.4、改造配置文件
将四个库分别命名为：messi_order_system_1，messi_order_system_2，messi_order_system_3，messi_order_system_4。
在application-local.yml文件中，使用新的分库分表配置替换旧的单库配置。
```yaml
 # 分库分表
  shardingsphere:
    datasource:
      names: ds0,ds1,ds2,ds3
      ds0:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.2.110:3306/messi_order_system_0?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456

      ds1:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.2.111:3306/messi_order_system_1?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456

      ds2:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.2.112:3306/messi_order_system_2?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456

      ds3:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://192.168.2.113:3306/messi_order_system_3?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: 123456
```
在单库中，对于不需要做分库分表的数据表，需要指定数据表保存在一个默认的库里。这里暂时不对最终一致性框架使用的本地消息表做分库分表。
```yaml
    # 不需要分库分表的表，路由到默认的数据源
    sharding:
      default-data-source-name: ds0
```
本次分库分表以userId为维度做拆分，在生成分布式ID时，订单号的生成规则是末尾由三位数的userId组成，所以这里使用order_id作为分库键的字段，本质上还是以userId作为维度来路由数据的。具体的规则在OrderDatabasePrecisAlgorithm类中。
设置精准的分库策略。
```yaml
 # 默认的分库策略
      default-database-strategy:
        # 使用标准的分库策略
        standard:
          # 指定作为路由分片键的字段
          sharding-column: order_id
          # 指定分片逻辑
          precise-algorithm-class-name: com.messi.system.order.sharding.DatabasePreciseShardingAlgorithm
```
设置精准的分表的策略。
```yaml
 # SQL中带order_id字段的标准分片策略
      tables:
        # 指定分表的数据表，每个库8张表，4个库共32张表
        order_price_details:
          actual-data-nodes: ds$->{0..3}.order_price_details_$->{0..7}
          table-strategy:
            standard:
              # 指定作为路由分片键的字段
              sharding-column: order_id
              # 自定义策略
              precise-algorithm-class-name: com.messi.system.order.sharding.TablePreciseShardingAlgorithm
```

设置复杂的分库和分表策略。
```yaml
 # SQL中没有order_id字段的复合分片策略
        # 对于order_info表，在常规查询时使用order_id做路由键
        # 如果是查询分页，因为在业务中，order_id的最后三位是userId，所以这里用user_id做路由也可以
        order_info:
          actual-data-nodes: ds$->{0..3}.order_info_$->{0..7}
          database-strategy:
            complex:
              sharding-columns: order_id,user_id
              algorithm-class-name: com.messi.system.order.sharding.DatabaseComplexKeysShardingAlgorithm
          table-strategy:
            complex:
              sharding-columns: order_id,user_id
              algorithm-class-name: com.messi.system.order.sharding.TableComplexKeysShardingAlgorithm
```
### 3.4.3、自定义精准分片算法和复杂分片算法
在分布式业务ID设计之初，就要考虑清楚未来可能出现的分库分表的情况，避免给以后造成麻烦。在这里，因为本身分库分表的维度就是用户ID，分布式订单号的生成规则是末尾的三位数由用户ID组成，所以这里的分库分表，order_id或user_id都可以做路由键。
```xml
//	分库策略
com.messi.system.order.sharding.DatabasePreciseShardingAlgorithm
com.messi.system.order.sharding.DatabaseComplexKeysShardingAlgorithm

//	分表策略
com.messi.system.order.sharding.TablePreciseShardingAlgorithm
com.messi.system.order.sharding.TableComplexKeysShardingAlgorithm
```
分库和分表都分别有精准分片算法和复杂分片算法

- 精准分片算法，用来通过用户id做路由，查询到用户id所在的数据库。因为订单id的后三位就是用户id的后三位，所以这里的分片键，传递orderId或userId，在业务逻辑上来讲都可以。
- 复杂分片算法，用来匹配无法使用order_id的情况，比如订单详情是可以使用order_id的，但是分页查询有可能无法使用order_id。这是就通过多个值去路由数据，这里的优先级是先使用order_id，如果order_id不存在，继续使用user_id做路由。
- 匹配到数据库的算法：路由键 % 数据库数量，路由键对数据库数量取模。
```java
/**
* 计算匹配数据源的后缀
* 路由键 对 数据库 做取模
*
* @param valueSuffix 分片键后三位
* @return 数据源后缀
*/
public static String getDatabaseSuffix(int valueSuffix) {
    return valueSuffix % DATABASE_SIZE + "";
}
```

- 匹配到表的算法：路由键 / 数据库数量 % 表数量，路由键先除以数据库数量，然后对表取模。
```java
/**
 * 计算匹配表的后缀
 * 路由键 / 数据库 对表的数量做取模
 *
 * @param valueSuffix 分片键后三位
 * @return 数据源后缀
 */
public static String getTableSuffix(int valueSuffix) {
    return valueSuffix / DATABASE_SIZE % TABLE_SIZE + "";
}
```

给需要联表查询的数据表做绑定，避免出现笛卡尔积查询。
```yaml
  # 绑定表
  # 给需要联表查询的数据表做绑定，避免出现笛卡尔积关联
  binding-tables:
    - order_info,order_item_info,order_price_details
```
因为项目里使用的是druid-spring-boot-starter，所以在全部配置好以后，在项目启动时排除Druid数据源。
```yaml
@SpringBootApplication(exclude = {DruidDataSourceAutoConfigure.class})
```
这时启动项目后，数据源就会被sharding-jdbc代理。
### 3.4.4、搭建配置canal
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674818217209-a2ba303d-82ff-4581-98b9-86bebb62a3ae.png#averageHue=%23fefcfc&clientId=u808b54f6-3351-4&from=paste&height=347&id=ua70a1f61&name=image.png&originHeight=608&originWidth=2374&originalType=binary&ratio=1&rotation=0&showTitle=false&size=102273&status=done&style=none&taskId=u677f9f1b-9e17-4590-8cf7-28aa4c50134&title=&width=1356.5714285714287)
这里以做数据迁移的messi-data-migration-framework框架配置为例：
data-migration的配置模板
```properties
#################################################
## mysql serverId , v1.0.26+ will autoGen
# 不要和数据库中的server_id重复
canal.instance.mysql.slaveId=2

# enable gtid use true/false
canal.instance.gtidon=false

# position info
canal.instance.master.address=192.168.2.100:3306
canal.instance.master.journal.name=
canal.instance.master.position=
canal.instance.master.timestamp=1674316801000
canal.instance.master.gtid=

# rds oss binlog
canal.instance.rds.accesskey=
canal.instance.rds.secretkey=
canal.instance.rds.instanceId=

# table meta tsdb info
canal.instance.tsdb.enable=true
#canal.instance.tsdb.url=jdbc:mysql://127.0.0.1:3306/canal_tsdb
#canal.instance.tsdb.dbUsername=canal
#canal.instance.tsdb.dbPassword=canal

#canal.instance.standby.address =
#canal.instance.standby.journal.name =
#canal.instance.standby.position =
#canal.instance.standby.timestamp =
#canal.instance.standby.gtid=

# username/password
canal.instance.dbUsername=root
canal.instance.dbPassword=123456
canal.instance.connectionCharset = UTF-8
# enable druid Decrypt database password
canal.instance.enableDruid=false
#canal.instance.pwdPublicKey=MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALK4BUxdDltRRE5/zXpVEVPUgunvscYFtEip3pmLlhrWpacX7y7GCMo2/JM6LeHmiiNdH1FWgGCpUfircSwlWKUCAwEAAQ==

# table regex
canal.instance.filter.regex=.*\\..*

# table black regex
canal.instance.filter.black.regex=
# table field filter(format: schema1.tableName1:field1/field2,schema2.tableName2:field1/field2)
#canal.instance.filter.field=test1.t_product:id/subject/keywords,test2.t_company:id/name/contact/ch
# table field black filter(format: schema1.tableName1:field1/field2,schema2.tableName2:field1/field2)
#canal.instance.filter.black.field=test1.t_product:subject/product_image,test2.t_company:id/name/contact/ch

# mq config
canal.mq.topic=example
# dynamic topic route by schema or table regex
#canal.mq.dynamicTopic=mytest1.user,mytest2\\..*,.*\\..*
canal.mq.partition=0
# hash partition config
#canal.mq.partitionsNum=3
#canal.mq.partitionHash=test.table:id^name,.*\\..*
#################################################

```

### 3.4.5、手动建立分库分表SQL
没有通过sharding-jdbc的代码建立，这里直接手写的SQL。
数据库依次从messi_order_system_0 ~ messi_order_system_3。
数据表分别依次从0~7。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674818326156-75e93497-73ae-4e96-9c01-43a2b30a251d.png#averageHue=%23f3f1f0&clientId=u808b54f6-3351-4&from=paste&height=545&id=u518918fe&name=image.png&originHeight=953&originWidth=1529&originalType=binary&ratio=1&rotation=0&showTitle=false&size=377026&status=done&style=none&taskId=u2e3e1d06-8151-4568-81e6-c5a4fb66186&title=&width=873.7142857142857)
### 3.4.6、执行全量迁移程序
做10万单表数据的全量迁移，从单库单表迁移到4库，每库8表，共32张表。
在单机程序执行，迁移10万数据消耗229秒。
![QQ截图20230123213852.jpg](https://cdn.nlark.com/yuque/0/2023/jpeg/1477039/1674481352341-4478fe3e-39a1-4504-b823-030860d69513.jpeg#averageHue=%23e8e5de&clientId=uec8cae84-3aa5-4&from=paste&height=341&id=UFV1y&name=QQ%E6%88%AA%E5%9B%BE20230123213852.jpg&originHeight=596&originWidth=2313&originalType=binary&ratio=1&rotation=0&showTitle=false&size=175672&status=done&style=none&taskId=u17e3a321-6013-4c50-8a00-2acc3ff40ee&title=&width=1321.7142857142858)
### 3.4.7、单库单表，查询单条详情数据
根据订单号查询详情，匹配到单库单表查询详情数据，耗时16ms。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674556139513-e4980c55-d43d-4379-a406-73e2708433a2.png#averageHue=%23f7f5f4&clientId=udbec9f32-d042-4&from=paste&height=443&id=SEzz4&name=image.png&originHeight=776&originWidth=3524&originalType=binary&ratio=1&rotation=0&showTitle=false&size=305173&status=done&style=none&taskId=u547ad5fa-e2dd-4710-b30e-e7843970e37&title=&width=2013.7142857142858)
### 3.4.8、分库分表后，查询单条详情数据
根据订单号查询详情，匹配到指定的分库分表后查询到详情数据，耗时52ms。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674555195284-9bb04b9b-f17f-49e3-a6ee-97d1749a521f.png#averageHue=%23f7f5f4&clientId=udbec9f32-d042-4&from=paste&height=631&id=yYESa&name=image.png&originHeight=1104&originWidth=3497&originalType=binary&ratio=1&rotation=0&showTitle=false&size=462627&status=done&style=none&taskId=ub95a6314-8401-4bf2-a8a7-58d70e3cd3d&title=&width=1998.2857142857142)
再次查询同一个订单，因为已经将数据上传到ES，所以这次查询是从ES获取数据，耗时18ms。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674555265717-b25b400b-ab13-4ecb-8e9d-931029a2dbd9.png#averageHue=%23faf8f7&clientId=udbec9f32-d042-4&from=paste&height=528&id=GruIY&name=image.png&originHeight=924&originWidth=3518&originalType=binary&ratio=1&rotation=0&showTitle=false&size=296803&status=done&style=none&taskId=u0faa7018-1099-424b-8aac-25ce98ed03c&title=&width=2010.2857142857142)
### 3.4.9、单库单表和分库分表的查询详情总结
通过对比，目前单库单表的详情查询比分库分表的详情查询耗时短是正常的，因为单库单表直接去数据库查询数据，分库分表多了一个根据业务id路由到指定分库和指定分表的环节，会稍慢一点。
这里也有数据量级的影响因素在里面，因为目前只有10万级的数据，如果是单库单表是千万级乃至亿级的数据量，还是分库分表的效率高。
同时，面向客户端的数据会交由查询Elasticsearch来保证效率，面向服务端的数据查询相对来说，对查询耗时就不会那么敏感。
## 3.5、自定义es索引和上传数据
### 3.5.1、项目启动时创建索引
在项目中自定义ElasticsearchIndexRunner组件，实现ApplicationRunner。
```java
com.messi.system.order.elasticsearch.client.ElasticsearchClientService#createIndexIfNotExists
```
自定义es启动器会先判断是否存在索引，如果不存在，则创建索引。
### 3.5.2、自定义线程池上传数据到es
现阶段，数据上传到es的功能做在查询订单详情中。此功能在有了分库分表环境后，可以进一步做优化扩展：监听binlog，重新生成数据后上传到es索引。
这里使用自定义线程池做上传。
在上一步中，系统启动时建立好索引，再通过自定义线程池将需要的大宽表上传到es。
```java
com.messi.system.order.service.impl.OrderQueryServiceImpl#mqPushDataToElasticsearch
```
# ![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674819804980-71100f6e-356b-4288-a112-af0572453484.png#averageHue=%23ebebeb&clientId=u808b54f6-3351-4&from=paste&height=325&id=u619b3197&name=image.png&originHeight=569&originWidth=2491&originalType=binary&ratio=1&rotation=0&showTitle=false&size=253532&status=done&style=none&taskId=u429e05da-4cc6-415c-bc4a-63d448f82db&title=&width=1423.4285714285713)
# 三、项目中间件清单
这里列举的中间件参数是在生产环境中使用的，如果是个人搭建的虚拟机或者docker镜像，也可以这么配置这些参数，但因为机器本身的硬件限制，在运行过程中是达不到参数标准的。
其中所有的中间件分配在一台centos7虚拟机上，虚拟机配置32G内存（16G内存也足够用），8核处理器。
在分库分表环境下，另外创建4台1核2G的MySQL数据库。

| **中间件名称** | **用途** |
| --- | --- |
| MySQL | 数据库 |
| Redis | 做缓存服务、分布式锁、临时键值对key |
| Nacos | 注册中心、配置中心 |
| Seata | 刚性分布式事务 |
| Rocketmq | 消息队列 |
| xxl-job | 定时任务 |
| Elasticsearch | 搜索、数据异构存储 |
| Nginx | 反向代理服务器 |
| Skywalking | 分布式微服务链路跟踪 |
| Sentinel | 做接口降级、限流 |
| Canal | 监听MySQL bin-log |
| ShardingSphere | 分库分表数据源 |

# 四、项目服务启动顺序和设计模式
## 4.1、订单系统的服务启动顺序
按先后顺序排序：用户系统 UserApp > 商品系统 ProductApp > 营销系统 MarketApp > 订单系统 OrderApp > 数据迁移服务 DataMigration
## 4.2、代码中运用的设计模式
### 4.2.1、模板模式
在项目中分别使用了普通消息、事务消息和延迟消息。
使用模板模式维护rocketmq管理类。
```java
com.messi.system.rocketmq.producer.MqProducer
```
### 4.2.2、组合模式
实现三个版本的提交订单，通过抽象父类和实现版本接口，可以自由的组合业务实现流程。
```java
com.messi.system.order.service.impl.submit.AbstractSubmitOrder
com.messi.system.order.service.submit.SubmitOrder
```
### 4.2.3、构造器模式
构造复杂的订单数据对象
```java
com.messi.system.order.builder.OrderBuilder
```
### 4.2.4、工厂模式
订单的日志状态会不断的更新，通过工厂模式传入订单ID和前置状态，就可以产生不同状态的订单日志。
```java
com.messi.system.order.factory.OrderStatusRecordFactory
```
### 4.2.5、单例模式
使用的地方非常多，例如自定义Spring线程池。
```java
com.messi.system.order.thread.CustomThreadPoolConfig
```
### 4.2.6、享元模式
在JVM内存中，多个线程之间使用同一份元数据。
```java
com.messi.snap.up.reactor.executor.WorkerThreadExecutor
```
# 五、中间件部署优化和压测配置
## 5.1、Centos7
### 5.1.1、设置系统最大连接文件数
打开文件 vim /etc/security/limits.conf
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674114876324-e13bec54-cd51-481d-a99d-a3f8c11fe1eb.png#averageHue=%231d2022&clientId=uad62edf4-7082-4&from=paste&height=33&id=uSplC&name=image.png&originHeight=57&originWidth=597&originalType=binary&ratio=1&rotation=0&showTitle=false&size=9151&status=done&style=none&taskId=u8cdce865-0efa-454f-832d-23e17b43477&title=&width=341.14285714285717)
增加配置：
```java
* soft nofile 65535
* hard nofile 65535

* soft nproc 65535
* hard nproc 65535
```

- soft代表警告，可以超过这个值，但是超过后会有警告
- hard代表严格规定，不允许超过这个值

- "*"：表示所有用户
- nofile：每个进程可以打开的文件数量的限制
- nproc：操作系统对每个用户创建的进程数量的限制

- soft nofile：每个进程打开的文件数的告警值
- hard nofile：操作系统强制每个用户创建进程数量的限制值
### 5.1.2、修改后不重启机器让配置参数生效的办法
先执行 exit 退出当前用户，然后重新登录即可。
查询配置生效命令： ulimit -a
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674115398401-63494c8d-157a-4929-8a61-ba880c07b920.png#averageHue=%231b1e20&clientId=uad62edf4-7082-4&from=paste&height=395&id=kw4Rh&name=image.png&originHeight=692&originWidth=612&originalType=binary&ratio=1&rotation=0&showTitle=false&size=128124&status=done&style=none&taskId=u9cd89124-6846-45c1-bc80-165ff6c1a1b&title=&width=349.7142857142857)
## 5.2、MySQL
### 5.2.1、数据库表名对大小写敏感
让数据表以小写形式存储在磁盘上，在对比表名时不区分大小写。
打开 my.cnf 文件，vim /etc/my.cnf
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674115675267-6b3e9329-6112-4d25-8b0b-c01d864b838e.png#averageHue=%231a1c1e&clientId=uad62edf4-7082-4&from=paste&height=39&id=n2ekk&name=image.png&originHeight=68&originWidth=452&originalType=binary&ratio=1&rotation=0&showTitle=false&size=8085&status=done&style=none&taskId=u6f1e0fc0-cdb4-4ae1-bad1-6beef520579&title=&width=258.2857142857143)
增加配置
```java
lower_case_table_names=1
```
### 5.2.2、增加数据库最大连接数
还是在my.cnf文件中，增加配置。
```java
max_connections=1000
```
保存配置后，重启MySQL数据库。
重启命令
```java
systemctl start mysqld.service
```
### 5.2.3、开启binlog日志
如果log_bin是off，需要开启on。
```yaml
show VARIABLES like '%log_bin%'
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674353825484-1a1614bd-3bce-495b-8a15-293cff733211.png#averageHue=%23dbb98c&clientId=ue37c9d76-7629-4&from=paste&height=206&id=ubdaf982d&name=image.png&originHeight=360&originWidth=488&originalType=binary&ratio=1&rotation=0&showTitle=false&size=43025&status=done&style=none&taskId=u65be3515-e082-4d58-b892-ead5a460038&title=&width=278.85714285714283)
打开MySQL的配置文件。
```yaml
vim /etc/my.cnf
```
在 [mysqld] 的标签下增加配置。
```yaml
log-bin=mysql-bin # 开启 binlog
binlog-format=ROW # 选择 ROW 模式
server_id=1 # 配置 MySQL分片需要定义，但是不要和canal的slaveId重复
```
重启MySQL。
```yaml
 systemctl restart mysqld
```
重启后bin-log配置就生效了。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674354176021-1b5eb83f-7e67-4e0a-80bc-526f53b20a8d.png#averageHue=%23d9b687&clientId=ue37c9d76-7629-4&from=paste&height=221&id=u533bef24&name=image.png&originHeight=386&originWidth=655&originalType=binary&ratio=1&rotation=0&showTitle=false&size=49584&status=done&style=none&taskId=u3d0e748f-3787-4752-8669-fdcd614f727&title=&width=374.2857142857143)
这时可以创建一个具有MySQL slave权限的canal账号，也在后面的监听bin-log中直接使用root账号。
补充：在Canal的运维界面中，填入MySQL的root账号就可以。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674356578618-6f24db34-a2cc-4017-ab4c-ac25105e8679.png#averageHue=%23f9f8f5&clientId=ue37c9d76-7629-4&from=paste&height=141&id=uec17cf2f&name=image.png&originHeight=247&originWidth=595&originalType=binary&ratio=1&rotation=0&showTitle=false&size=55410&status=done&style=none&taskId=u08ed1c9e-5497-4761-8b70-7a5e3e1941b&title=&width=340)
### 5.2.4、设置分库账号和关闭centos7防火墙
设置可以外部访问MySQL
```properties
mysql -uroot -p

set global validate_password_policy=0;

set global validate_password_length=1;

grant all privileges on *.* to 'root'@'%' identified by '123456' with grant option;

FLUSH PRIVILEGES;
```

设置防火墙
```properties
systemctl status firewalld.service

systemctl stop firewalld.service

systemctl disable firewalld.service
```
## 5.3、Skywalking生成追踪ID和日志上报
### 5.3.1、在日志中生成追踪链路id
IDEA上增加pom.xml
```xml
<dependency>
  <groupId>org.apache.skywalking</groupId>
  <artifactId>apm-toolkit-logback-1.x</artifactId>
  <version>8.5.0</version>
</dependency>

<dependency>
  <groupId>org.apache.skywalking</groupId>
  <artifactId>apm-toolkit-trace</artifactId>
  <version>8.5.0</version>
</dependency>

<dependency>
  <groupId>org.apache.skywalking</groupId>
  <artifactId>apm-toolkit-opentracing</artifactId>
  <version>8.5.0</version>
  <scope>provided</scope>
</dependency>
```
Logback上增加TraceId采集器
```xml
<encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
  <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
    <Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%tid] %-5level [%.15thread] %logger{36} [%M:%line]:%X{sysUser} - %.-16384msg%n</Pattern>
  </layout>
  <!-- 设置字符集 -->
  <charset>UTF-8</charset>
</encoder>
```
### 5.3.2、自定义微服务链路追踪
实现在Skywalking的可视化追踪界面中可以查看服务的执行链路层级。
IDEA创建agent目录，把agent目录里全部内容拷贝到IDEA的agent目录
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674117935839-9f79e456-8189-4699-8a0c-588140571aa6.png#averageHue=%2315191c&clientId=uad62edf4-7082-4&from=paste&height=130&id=ua8ff0990&name=image.png&originHeight=227&originWidth=1511&originalType=binary&ratio=1&rotation=0&showTitle=false&size=44576&status=done&style=none&taskId=ua4c8090a-a5ab-4e80-937e-dff36102f12&title=&width=863.4285714285714)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674117989844-f37f69b4-28ac-411c-a650-6cb45affb32f.png#averageHue=%23e4e2de&clientId=uad62edf4-7082-4&from=paste&height=192&id=u12b0651e&name=image.png&originHeight=336&originWidth=416&originalType=binary&ratio=1&rotation=0&showTitle=false&size=29132&status=done&style=none&taskId=u06abebec-1d87-4283-92dd-096128dc51d&title=&width=237.71428571428572)
### 5.3.3、IDEA配置启动参数
在IDEA启动微服务时，在vm options中增加配置 ：
```xml
-javaagent:E:/Better/practise/project/messi-skill-system/agent/skywalking-agent.jar
```
在environment variables中增加配置： 
```xml
SW_AGENT_COLLECTOR_BACKEND_SERVICES=192.168.2.100:11800;SW_AGENT_NAME=messi-order-system
```
如图所示：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674118165251-32699abe-2fcf-4095-ab3f-8a917cb0ac5e.png#averageHue=%23fdfafa&clientId=uad62edf4-7082-4&from=paste&height=513&id=u97501d24&name=image.png&originHeight=898&originWidth=2445&originalType=binary&ratio=1&rotation=0&showTitle=false&size=256220&status=done&style=none&taskId=uc5a470e5-94a9-4c6b-a156-c08c237477d&title=&width=1397.142857142857)
代码中配置，在要做微服务链路跟踪的请求方法上增加注解
```java
@Trace
    @Tags({@Tag(key = "SubmitOrderReq", value = "arg[0]"),
           @Tag(key = "ResResult<SubmitOrderDTO>", value = "returnObject")})
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674118270635-d40a1250-e066-4231-b1bf-f575d2081996.png#averageHue=%23fdfcfc&clientId=uad62edf4-7082-4&from=paste&height=360&id=u6299e8ef&name=image.png&originHeight=630&originWidth=1965&originalType=binary&ratio=1&rotation=0&showTitle=false&size=116861&status=done&style=none&taskId=u3cfa77a0-df66-49a4-baa8-52c13371f2d&title=&width=1122.857142857143)
全部配置完成后，重新启动服务，执行指定的请求方法，然后就可以在可视化界面中看到指定的服务追踪链路。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674118334282-4bac916a-511b-412e-962c-108627fd3f16.png#averageHue=%23fafafa&clientId=uad62edf4-7082-4&from=paste&height=611&id=u3f6e1b7d&name=image.png&originHeight=1069&originWidth=3645&originalType=binary&ratio=1&rotation=0&showTitle=false&size=304218&status=done&style=none&taskId=ud0fd1127-a419-4375-bae4-d19375e2a89&title=&width=2082.8571428571427)

自定义日志上传监控，在IDEA的agent目录，在/agent/config/agent.config中增加以下配置项，可以只修改SW_GRPC_LOG_SERVER_HOST，其他保持不变。
```
plugin.toolkit.log.grpc.reporter.server_host=${SW_GRPC_LOG_SERVER_HOST:192.168.2.100}
plugin.toolkit.log.grpc.reporter.server_port=${SW_GRPC_LOG_SERVER_PORT:11800}
plugin.toolkit.log.grpc.reporter.max_message_size=${SW_GRPC_LOG_MAX_MESSAGE_SIZE:10485760}
plugin.toolkit.log.grpc.reporter.upstream_timeout=${SW_GRPC_LOG_GRPC_UPSTREAM_TIMEOUT:30}
```

在项目的logback-spring.xml日志配置中增加skywalking的日志采集上报，具体见项目中完整的日志配置文件。
```
<!-- 将日志上传到skywalking -->
<appender name="GRPC-LOG" class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.log.GRPCLogClientAppender">
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.mdc.TraceIdMDCPatternLogbackLayout">
            <Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%tid] %-5level [%.15thread] %logger{36} [%M:%line]:%X{sysUser} - %.-16384msg%n</Pattern>
        </layout>
    </encoder>
</appender>

<appender-ref ref="GRPC-LOG"/>
```

全部配置完成后，重启订单系统，再次执行业务操作时，产生的日志就会上报到skywalking的日志可视化界面。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674118653463-dcc65ea3-20a8-4b25-9ca0-139c9aeaca3e.png#averageHue=%23c3aa87&clientId=uad62edf4-7082-4&from=paste&height=945&id=u42fc48b1&name=image.png&originHeight=1654&originWidth=2097&originalType=binary&ratio=1&rotation=0&showTitle=false&size=586956&status=done&style=none&taskId=ub7cd9501-d61a-42fb-a216-7607542212c&title=&width=1198.2857142857142)
## 5.4、Jmeter压测工具的使用
### 5.4.1、调整显式乱码
首先下载Jmeter，下载地址：[https://jmeter.apache.org/download_jmeter.cgi](https://jmeter.apache.org/download_jmeter.cgi)，这里我下载的是Jmeter5.5。
解压后，在apache-jmeter-5.5/bin目录下，找到jmeter.properties，修改配置项：
```
#sampleresult.default.encoding=ISO-8859-1
#修改jmeter乱码
sampleresult.default.encoding=UTF-8
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673930495942-cef78865-49c4-49e1-8f50-04db3e85485a.png#averageHue=%232e3026&clientId=u0a6d460c-49ee-4&from=paste&height=79&id=uaa92dba5&name=image.png&originHeight=138&originWidth=921&originalType=binary&ratio=1&rotation=0&showTitle=false&size=33903&status=done&style=none&taskId=u14106b60-7b85-4904-aeda-183912406cc&title=&width=526.2857142857143)
### 5.4.2、配置压测
双击“ApacheJMeter.jar”启动Jmeter。
#### 1、创建测试线程组
鼠标右键单击“测试计划”。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673930717289-a16269bd-ab29-4ca5-a999-7b55bb4348b7.png#averageHue=%23e8e8e7&clientId=u0a6d460c-49ee-4&from=paste&height=332&id=ub3641086&name=image.png&originHeight=581&originWidth=1002&originalType=binary&ratio=1&rotation=0&showTitle=false&size=160538&status=done&style=none&taskId=ud5206ba9-bd77-4f40-be1d-137355ed119&title=&width=572.5714285714286)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673930740814-604b73bd-9d49-41fc-afd2-4b76fd738386.png#averageHue=%23ededec&clientId=u0a6d460c-49ee-4&from=paste&height=443&id=u4222755a&name=image.png&originHeight=776&originWidth=888&originalType=binary&ratio=1&rotation=0&showTitle=false&size=122481&status=done&style=none&taskId=u82c219fe-6fb3-446e-b055-4bee2a35409&title=&width=507.42857142857144)
#### 2、创建测试请求
鼠标右键单击刚创建好的“测试线程组”。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673930822027-c4c82f4c-ec98-4946-a7a2-b12ecde301ae.png#averageHue=%23e5e4e2&clientId=u0a6d460c-49ee-4&from=paste&height=166&id=uc12afe21&name=image.png&originHeight=291&originWidth=1132&originalType=binary&ratio=1&rotation=0&showTitle=false&size=136488&status=done&style=none&taskId=ue8443300-ada3-4bf2-817d-5a6e5550cd7&title=&width=646.8571428571429)
依次输入

- 名称：本次测试请求的名称
- 协议：http
- 服务器名称或IP：要请求访问的IP地址
- 端口号：请求的端口号
- HTTP请求：GET或者POST
- 路径：具体请求的controller地址
- 消息体数据：请求的参数

![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1674117458359-cf91f8f6-5fc7-4d75-9a69-7936911e61f8.png#averageHue=%23f2f1ed&clientId=uad62edf4-7082-4&from=paste&height=398&id=u13c88ee4&name=image.png&originHeight=697&originWidth=1226&originalType=binary&ratio=1&rotation=0&showTitle=false&size=122690&status=done&style=none&taskId=u792207db-5e41-44b0-9895-343205e8db1&title=&width=700.5714285714286)
添加 HTTP信息头管理器
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673939476311-63b9cb96-1900-4412-8271-3c6feb8ca6bc.png#averageHue=%23e8e7e6&clientId=u0a6d460c-49ee-4&from=paste&height=318&id=u946abbae&name=image.png&originHeight=557&originWidth=1021&originalType=binary&ratio=1&rotation=0&showTitle=false&size=198742&status=done&style=none&taskId=u483492e0-5b0e-4551-8bf7-dc81e77fcb0&title=&width=583.4285714285714)

名称：Content-Type，值：application/json
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673939538931-3ee40c92-2a47-419a-942f-388daf1eb1fe.png#averageHue=%23ecebeb&clientId=u0a6d460c-49ee-4&from=paste&height=169&id=u40d0fbff&name=image.png&originHeight=296&originWidth=908&originalType=binary&ratio=1&rotation=0&showTitle=false&size=43188&status=done&style=none&taskId=u6c03f233-76a9-44b5-bd61-98fa365b914&title=&width=518.8571428571429)
#### 3、查看测试结果
鼠标右键单击创建好的“测试请求”，选择添加-监听器-查看结果树。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673931085879-9014be68-5ad3-49f3-9568-e93396d180d8.png#averageHue=%23eeedeb&clientId=u0a6d460c-49ee-4&from=paste&height=557&id=uf73a3155&name=image.png&originHeight=975&originWidth=948&originalType=binary&ratio=1&rotation=0&showTitle=false&size=275561&status=done&style=none&taskId=ua9cf1ba9-da2d-4d3a-b0a5-606b314ac7e&title=&width=541.7142857142857)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673931136301-6b3f80e7-837d-4baa-a3de-7bbe42b272b0.png#averageHue=%23f4f4f4&clientId=u0a6d460c-49ee-4&from=paste&height=388&id=u7b4642e4&name=image.png&originHeight=679&originWidth=909&originalType=binary&ratio=1&rotation=0&showTitle=false&size=79640&status=done&style=none&taskId=u2fcfcb41-9c91-4c27-8147-4c10c283a52&title=&width=519.4285714285714)
#### 4、保存测试计划
全部设置完成后，选择文件-保存测试计划为，将测试计划保存至Jmeter的bin目录下，稍后生成中文测试报告时需要用到。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673931196743-57d0e5ff-06c4-4c4f-96b3-77e716da4db5.png#averageHue=%23e7e5e4&clientId=u0a6d460c-49ee-4&from=paste&height=341&id=u38c1b742&name=image.png&originHeight=597&originWidth=437&originalType=binary&ratio=1&rotation=0&showTitle=false&size=89649&status=done&style=none&taskId=u0107ef6b-8342-4d75-aab0-c6b4fc26990&title=&width=249.71428571428572)
### 5.4.3、Jmeter生成中文测试报告
#### 1、创建中文测试报告模板
下载“jmeter5.x-cn-report-template-master”，将里面的全部内容拷贝到Jmeter的/bin/report-template目录下。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673931903562-fa003fb7-7ff6-4c15-a451-98160f5adfbe.png#averageHue=%23faf8f6&clientId=u0a6d460c-49ee-4&from=paste&height=175&id=uf8beeeb1&name=image.png&originHeight=306&originWidth=345&originalType=binary&ratio=1&rotation=0&showTitle=false&size=16630&status=done&style=none&taskId=ue81f08b4-a028-4de6-b6ce-642b154d3e8&title=&width=197.14285714285714)
分别修改index.html.fmkr和/content/pages目录下的全部.fmkr文件，将文件编码设置成GBK。
.fmkr格式的文件可以使用sublime Text打开。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673932061904-3df4d2c7-1af3-4c3e-9aa8-74da779d9db3.png#averageHue=%23494946&clientId=u0a6d460c-49ee-4&from=paste&height=306&id=u28cfd543&name=image.png&originHeight=535&originWidth=1850&originalType=binary&ratio=1&rotation=0&showTitle=false&size=286501&status=done&style=none&taskId=u8ab73a9a-8986-4740-9e46-4a5d780083e&title=&width=1057.142857142857)
#### 2、执行测试计划
调整完.fmkr文件的编码格式以后，可以执行测试计划。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673932156426-8ee784d4-e22c-404d-abf8-48eb11445360.png#averageHue=%23f1f0ef&clientId=u0a6d460c-49ee-4&from=paste&height=226&id=u686908f3&name=image.png&originHeight=395&originWidth=794&originalType=binary&ratio=1&rotation=0&showTitle=false&size=102350&status=done&style=none&taskId=u14585f4a-aa0a-4855-ade0-f00ac480b18&title=&width=453.7142857142857)
#### 3、生成中文测试报告
测试计划执行完毕以后，进入Jmeter的bin目录，在bin目录下打开控制台。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673932232633-91e4b897-c1ee-4225-b638-c6e2760b3f44.png#averageHue=%23363636&clientId=u0a6d460c-49ee-4&from=paste&height=169&id=u925dbedb&name=image.png&originHeight=296&originWidth=742&originalType=binary&ratio=1&rotation=0&showTitle=false&size=17493&status=done&style=none&taskId=u6fd649e5-a0d2-4831-b93c-62c2443b184&title=&width=424)
输入以下指令，生成测试报告：
```
 .\jmeter -n -t order-system-测试线程组.jmx -l result.jtl -e -o ./测试报告
```
order-system-测试线程组.jmx：是你之前保存在bin目录的测试计划。
result.jtl：是指定生成的结果集文件。
./测试报告：指定生成报告的目录
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673932510964-ea3726ae-0810-47c8-b410-75fc58ebaa63.png#averageHue=%231c1c1c&clientId=u0a6d460c-49ee-4&from=paste&height=221&id=u6d634bb7&name=image.png&originHeight=387&originWidth=2064&originalType=binary&ratio=1&rotation=0&showTitle=false&size=167440&status=done&style=none&taskId=u682c851f-fc93-42e4-b843-3c7ecde37fe&title=&width=1179.4285714285713)
成功生成测试报告以后，在Jmeter的bin目录下会出现一个“测试报告”目录。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673932550205-d42e8c1a-c353-479e-b878-65c66d78ff31.png#averageHue=%23fbfaf9&clientId=u0a6d460c-49ee-4&from=paste&height=196&id=ub534d53a&name=image.png&originHeight=343&originWidth=893&originalType=binary&ratio=1&rotation=0&showTitle=false&size=45527&status=done&style=none&taskId=ufc8de0f0-578e-4545-bd1d-70a45feac16&title=&width=510.2857142857143)
进入“测试报告”目录，打开“index.html”，可以查看测试报告。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/1477039/1673932605710-2daf6692-e2da-4981-9d9c-c36e299ef846.png#averageHue=%23fafafa&clientId=u0a6d460c-49ee-4&from=paste&height=1018&id=uac26499d&name=image.png&originHeight=1781&originWidth=3787&originalType=binary&ratio=1&rotation=0&showTitle=false&size=292100&status=done&style=none&taskId=u9f6a1488-cb7a-48a8-b744-a135e73ca76&title=&width=2164)
#### 4、重新生成测试报告
如果需要重新生成指定测试计划的测试报告，只需要删除“result.jtl”文件，重新执行生成报告的指令即可。

# 
