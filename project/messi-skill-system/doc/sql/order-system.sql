CREATE
    DATABASE if NOT EXISTS `messi_order_system` DEFAULT CHARACTER SET utf8;
USE
    `messi_order_system`;

SET NAMES utf8;
SET
    FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`
(
    `id`                bigint(20)  NOT NULL COMMENT '主键id',
    `channel`           tinyint(4)  NOT NULL COMMENT '渠道标识 0:C端渠道，999:其他',
    `order_id`          varchar(50) NOT NULL COMMENT '订单号',
    `order_type`        tinyint(4)  NOT NULL COMMENT '订单类型 0：标准订单 999：其他',
    `order_status`      tinyint(4)  NOT NULL COMMENT '00：未支付,10：已支付，20：已入库，30：已出库，40：配送中：50：已签收，60：已取消，999：订单失效\r\n注：省略已入库->已出库中间的履约流程',
    `order_cancel_time` datetime    NULL COMMENT '取消订单的时间',
    `seller_id`         varchar(20) NOT NULL COMMENT '卖家id',
    `user_id`           varchar(20) NOT NULL COMMENT '买家id',
    `total_amount`      int(11)     NULL COMMENT '订单总金额，单位：分',
    `actual_amount`     int(11)     NULL COMMENT '实付金额，单位：分',
    `order_pay_type`    tinyint(4)  NULL COMMENT '订单支付类型 0：微信 1：支付宝  2：银联',
    `pay_time`          datetime    NULL COMMENT '支付时间',
    `coupon_id`         varchar(50) NULL COMMENT '优惠券id',
    `appraise_status`   tinyint(4)  NOT NULL COMMENT '订单评价状态 0：未评价 1：已评价',
    `create_time`       datetime    NOT NULL COMMENT '创建时间',
    `modified_time`     datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '订单信息表';

DROP TABLE IF EXISTS `order_item_info`;
CREATE TABLE `order_item_info`
(
    `id`            bigint(20)  NOT NULL COMMENT '主键id',
    `order_id`      varchar(20) NOT NULL COMMENT '所属订单id',
    `order_item_id` varchar(50) NOT NULL COMMENT '订单条目id',
    `product_id`    varchar(20) NOT NULL COMMENT '商品id',
    `sku_id`        varchar(20) NOT NULL COMMENT 'skuid',
    `sale_num`      int(11)     NOT NULL COMMENT '销售数量',
    `sale_price`    int(11)     NOT NULL COMMENT '销售单价，单位：分',
    `seller_id`     varchar(20) NOT NULL COMMENT '卖家id',
    `user_id`       varchar(20) NOT NULL COMMENT '买家id',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `modified_time` datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '订单明细表';

DROP TABLE IF EXISTS `order_status_record`;
CREATE TABLE `order_status_record`
(
    `id`            bigint(20)  NOT NULL COMMENT '主键ID',
    `order_id`      varchar(50) NOT NULL COMMENT '订单号',
    `prev_status`   tinyint(4)  NOT NULL COMMENT '订单的前一个状态',
    `cur_status`    tinyint(4)  NOT NULL COMMENT '订单的当前状态',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `modified_time` datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '订单状态变更记录表';

DROP TABLE IF EXISTS `order_price`;
CREATE TABLE `order_price`
(
    `id`                bigint(20)  NOT NULL COMMENT '主键',
    `order_id`          varchar(50) NOT NULL COMMENT '订单id',
    `order_total_price` int(11)     NOT NULL COMMENT '单笔订单总价格',
    `create_time`       datetime    NOT NULL COMMENT '创建时间',
    `modified_time`     datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '订单价格表';

DROP TABLE IF EXISTS `order_price_details`;
CREATE TABLE `order_price_details`
(
    `id`               bigint(20)  NOT NULL COMMENT '主键',
    `order_id`         varchar(50) NOT NULL COMMENT '订单id',
    `order_item_id`    varchar(50) NOT NULL COMMENT '订单条目id',
    `sku_id`           varchar(50) NOT NULL COMMENT 'sku id',
    `sale_num`         int(11)     NOT NULL COMMENT '单笔条目sku销售数量',
    `sale_price`       int(11)     NOT NULL COMMENT 'sku销售原价',
    `order_item_price` int(11)     NOT NULL COMMENT '订单条目实际收费价格',
    `create_time`      datetime    NOT NULL COMMENT '创建时间',
    `modified_time`    datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '订单价格明细表';

ALTER TABLE `messi_order_system`.`order_info`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id' FIRST;

ALTER TABLE `messi_order_system`.`order_item_info`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id' FIRST;

ALTER TABLE `messi_order_system`.`order_price`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键' FIRST;

ALTER TABLE `messi_order_system`.`order_price_details`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键' FIRST;

ALTER TABLE `messi_order_system`.`order_status_record`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID' FIRST;

CREATE TABLE `undo_log`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `branch_id`     bigint(20)   NOT NULL,
    `xid`           varchar(100) NOT NULL,
    `context`       varchar(128) NOT NULL,
    `rollback_info` longblob     NOT NULL,
    `log_status`    int(11)      NOT NULL,
    `log_created`   datetime     NOT NULL,
    `log_modified`  datetime     NOT NULL,
    `ext`           varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `consistency_info`;
CREATE TABLE `consistency_info`
(
    `id`                  bigint(20)   NOT NULL COMMENT '主键',
    `task`                varchar(255) NOT NULL COMMENT '本次执行最终一致性任务的方法',
    `task_method_name`    varchar(255) NOT NULL COMMENT '执行的任务方法名称',
    `full_sign_name`      varchar(255) NOT NULL COMMENT '完整的方法签名',
    `param_types`         varchar(255) NOT NULL COMMENT '执行的任务方法的参数类型',
    `params`              blob         NOT NULL COMMENT '执行参数,JSON格式保存',
    `exec_type`           int(11)      NOT NULL COMMENT '执行任务类型，0 立即执行',
    `exec_interval`       int(11)      NOT NULL COMMENT '任务执行间隔',
    `delay_ms`            int(11)      NOT NULL COMMENT '任务延迟执行时间',
    `exec_total`          int(11)      NOT NULL COMMENT '任务执行次数',
    `exec_time`           datetime     NOT NULL COMMENT '任务首次执行时间',
    `status`              int(11)      NOT NULL COMMENT '任务状态',
    `err_msg`             varchar(255) NULL COMMENT '任务执行失败后的异常信息',
    `downgrade_class`     varchar(255) NULL COMMENT '任务执行失败后的降级执行类',
    `downgrade_error_msg` varchar(255) NULL COMMENT '降级执行类执行失败的异常信息',
    `create_time`         datetime     NOT NULL COMMENT '创建时间',
    `modified_time`       datetime     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '最终一致性任务实例信息表';

ALTER TABLE `messi_order_system`.`consistency_info`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键' FIRST;

