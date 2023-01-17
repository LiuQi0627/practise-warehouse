CREATE
    DATABASE if NOT EXISTS `messi_product_system` DEFAULT CHARACTER SET utf8;
USE
    `messi_product_system`;

SET NAMES utf8;
SET
    FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sku_info`;
CREATE TABLE `sku_info`
(
    `id`            bigint(20)   NOT NULL COMMENT '主键',
    `product_id`    varchar(20)  NOT NULL COMMENT '商品id',
    `sku_id`        varchar(20)  NOT NULL COMMENT 'skuid',
    `sku_name`      varchar(255) NOT NULL COMMENT '商品名称',
    `sku_price`     int(11)      NOT NULL COMMENT 'sku单价 单位：分',
    `create_time`   datetime     NOT NULL COMMENT '创建时间',
    `modified_time` datetime     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '商品表';

DROP TABLE IF EXISTS `sku_stock`;
CREATE TABLE `sku_stock`
(
    `id`            bigint(20)  NOT NULL COMMENT '主键id',
    `sku_id`        varchar(20) NOT NULL COMMENT 'skuid',
    `total_stock`   int(11)     NOT NULL COMMENT '总库存',
    `saled_stock`   int(11)     NOT NULL COMMENT '已售库存',
    `create_time`   datetime    NOT NULL COMMENT '创建时间',
    `modified_time` datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = 'sku库存表';


ALTER TABLE `messi_product_system`.`sku_info`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键' FIRST;

ALTER TABLE `messi_product_system`.`sku_stock`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id' FIRST;

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

