包说明

order-system-api dubbo暴露的微服务调用接口 domain 服务实体

order-system-service api.iml dubbo暴露的微服务调用接口实现类 controller 接收http请求controller dao DAO层 manager 当前服务的事务逻辑实现 mapper
当前服务的mapper映射 remote 封装调用外部其他微服务的调用接口，各个微服务之间使用**remote组件互相调用，内部调用服务的api接口 service 当前服务的service实现类

================================ 提交订单： 涉及表：订单信息表 订单明细表 配送信息表 订单状态变更记录表




  ----------------------------

主线流程：1、正向下单 超时自动取消，事务mq 延迟mq 分布式事务、最终一致性框架 xxljob 分布式锁 2、逆向售后、取消订单，缺品退款 客服售后 普通mq 设计模式 3、c端分库分表、b端 es搜索查询 4、 im 日志系统
秒杀系统