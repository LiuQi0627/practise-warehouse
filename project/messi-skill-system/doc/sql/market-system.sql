CREATE
    DATABASE if NOT EXISTS `messi_market_system` DEFAULT CHARACTER SET utf8;
USE
    `messi_market_system`;

SET NAMES utf8;
SET
    FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `coupon_info`;
CREATE TABLE `coupon_info`
(
    `id`              bigint(20)  NOT NULL COMMENT '主键',
    `coupon_id`       varchar(50) NOT NULL COMMENT '优惠券id',
    `coupon_type`     int(11)     NOT NULL COMMENT '优惠券类型 0：满减优惠 1：折扣优惠',
    `coupon_price`    int(11)     NULL COMMENT '满减金额',
    `coupon_discount` int(11)     NULL COMMENT '折扣比率',
    `user_id`         varchar(50) NOT NULL COMMENT '分发的指定用户',
    `use_status`      tinyint(4)  NOT NULL COMMENT '使用情况 0：未使用 1：已使用',
    `usage_time`      datetime    NULL COMMENT '使用时间',
    `create_time`     datetime    NOT NULL COMMENT '创建时间',
    `modified_time`   datetime    NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8 COMMENT = '优惠券信息表';

ALTER TABLE `messi_market_system`.`coupon_info`
    MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键' FIRST;

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
