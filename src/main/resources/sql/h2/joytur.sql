/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50711
 Source Host           : localhost:3306
 Source Schema         : joytur_dev

 Target Server Type    : MySQL
 Target Server Version : 50711
 File Encoding         : 65001

 Date: 07/01/2019 10:35:46
*/
#sql("dbinit")
DROP TABLE IF EXISTS `joy_account`;
CREATE TABLE `joy_account`  (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NULL DEFAULT NULL COMMENT '微信id',
  `total_amt` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '可用总金额',
  `frz_amt` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
  `avb_amt` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '可用金额',
  `acc_type` tinyint(4) NULL DEFAULT NULL COMMENT '账户类型',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账户状态:0注销 1生效2冻结',
  `version` tinyint(11) NULL DEFAULT NULL,
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户账户';

DROP TABLE IF EXISTS `joy_account_funds`;
CREATE TABLE `joy_account_funds`  (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NOT NULL,
  `from_account_id` varchar(32) NULL DEFAULT NULL COMMENT '来源帐户',
  `to_account_id` varchar(32) NOT NULL COMMENT '到达账户',
  `acc_type` tinyint(4) NULL DEFAULT NULL COMMENT '账户类型',
  `trans_amt` decimal(6, 2) NOT NULL COMMENT '交易金额',
  `trans_type` tinyint(4) NOT NULL COMMENT '1:减 2:加 3:冻结 4:解冻',
  `ele_type` tinyint(4) NULL DEFAULT NULL COMMENT '成分 10:提现减扣  20:订单奖励 21:推荐订单奖励 30:提现冻结 40:提现解冻',
  `bef_amt` decimal(8, 2) NOT NULL COMMENT '交易前可用金额',
  `aft_amt` decimal(8, 2) NOT NULL COMMENT '交易后可用金额',
  `tag` varchar(64) NULL DEFAULT NULL COMMENT '交易流水标记值',
  `description` varchar(32) NULL DEFAULT NULL COMMENT '交易描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户资金变动记录';

DROP TABLE IF EXISTS `joy_goods`;
CREATE TABLE `joy_goods`  (
  `id` varchar(32) NOT NULL,
  `goods_name` varchar(32) NULL DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(64) NULL DEFAULT NULL COMMENT '商品图片',
  `recommend` varchar(512) NULL DEFAULT NULL COMMENT '商品介绍',
  `cost_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '成本价',
  `mkt_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '市场价格',
  `score_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '消耗积分',
  `category_id` varchar(32) NULL DEFAULT NULL COMMENT '分类',
  `goods_rule_id` varchar(32) NULL DEFAULT NULL COMMENT '规则id',
  `month_sales` tinyint(4) NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态:0禁用，1启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '商品查询表';

DROP TABLE IF EXISTS `joy_goods_category`;
CREATE TABLE `joy_goods_category` (
  `id` varchar(32) NOT NULL,
  `category_name` varchar(32) DEFAULT NULL COMMENT '类别名称',
  `description` varchar(512) DEFAULT NULL COMMENT '类别描述',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态:0禁用，1启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='商品类别表';

DROP TABLE IF EXISTS `joy_goods_rule`;
CREATE TABLE `joy_goods_rule`  (
  `id` varchar(32) NOT NULL,
  `rule_name` varchar(32) NOT NULL COMMENT '规则名称',
  `played_num` tinyint(4) NULL DEFAULT NULL COMMENT '众人玩过次数送',
  `fail_num` tinyint(4) NULL DEFAULT NULL COMMENT '单人玩过失败次数送',
  `diff_value` varchar(150) NULL DEFAULT NULL COMMENT '难度[{"level":"1","diff":2, "quant":34}]',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态:0无效 1有效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '商品规则表';

DROP TABLE IF EXISTS `joy_goods_game`;
CREATE TABLE `joy_goods_game`  (
  `id` varchar(32) NOT NULL,
  `goods_id` varchar(32) NULL DEFAULT NULL COMMENT '商品id',
  `wechat_member_id` varchar(32) NULL DEFAULT NULL COMMENT '微信用户id',
  `exp_amt` decimal(5, 2) NOT NULL COMMENT '消耗积分',
  `start_time` datetime NOT NULL COMMENT '开始关卡时间',
  `end_time` datetime COMMENT '结束关卡时间',
  `screen` varchar(64) NULL DEFAULT NULL COMMENT '当前关卡',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '状态0失败,1成功',
  `game_params` varchar(256) NULL DEFAULT NULL COMMENT '获取游戏结束参数',
  `game_result` varchar(32) NULL DEFAULT NULL COMMENT '获取游戏结束结果',
  `update_time` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  `remarks` varchar(128) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '商品闯关记录表';

DROP TABLE IF EXISTS `joy_goods_order`;
CREATE TABLE `joy_goods_order`  (
  `id` varchar(32) NOT NULL,
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `wechat_member_id` varchar(32) NOT NULL COMMENT '微信用户id',
  `game_id` varchar(32) NULL DEFAULT NULL COMMENT '游戏id',
  `goods_id` varchar(32) NOT NULL COMMENT '商品id',
  `order_type` tinyint(4) NOT NULL COMMENT '商品订单类型 1闯关送 2满额送',
  `status` tinyint(4) NOT NULL COMMENT '订单状态, 1未发货 2已发货',
  `addr_mirror` varchar(512) NULL DEFAULT NULL COMMENT '发货地址',
  `logistics_number` varchar(32) NULL DEFAULT NULL COMMENT '物流单号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '商品中奖订单';

DROP TABLE IF EXISTS `joy_recharge_order`;
CREATE TABLE `joy_recharge_order`  (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NULL DEFAULT NULL COMMENT '微信用户id',
  `order_no` varchar(16) NULL DEFAULT NULL COMMENT '订单号',
  `trans_type` tinyint(4) NOT NULL COMMENT '充值类型 1:游戏币2:代理',
  `trans_amt` decimal(5, 2) NOT NULL COMMENT '充值金额',
  `real_trans_amt` decimal(5, 2) NOT NULL COMMENT '实际需要支付金额',
  `trans_after_amt` decimal(5, 2) NULL DEFAULT NULL COMMENT '到账金币',
  `commission_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '返佣状态:0不可返 1待确认 2待返佣 3已返佣',
  `qrcode_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '类型:1wx 2alipy',
  `qrcode_image` varchar(128) NOT NULL COMMENT '二维码图片',
  `action_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1扫码后自动输入金额 2需要手动输入金额',
  `mail_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态:0未通知 1已通知',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态:0:已失效 1:待支付 2:已支付 3已完成',
  `description` varchar(32) NULL DEFAULT NULL COMMENT '交易描述',
  `recharge_rule_id` varchar(32) NULL DEFAULT NULL COMMENT '对应升级或者充值规则id',
  `expire_time` datetime NOT NULL COMMENT '失效时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户充值订单';

DROP TABLE IF EXISTS `joy_recharge_rule`;
CREATE TABLE `joy_recharge_rule`  (
  `id` varchar(32) NOT NULL,
  `trans_amt` decimal(8, 2) NOT NULL COMMENT '充值金额',
  `trans_after_amt` decimal(8, 2) NULL DEFAULT NULL COMMENT '到账金币',
  `trans_day_limit` tinyint(4) NOT NULL COMMENT '单日限制',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态:0无效 1有效',
  `description` varchar(32) NULL DEFAULT NULL COMMENT '交易描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户充值规则';

DROP TABLE IF EXISTS `joy_recharge_rule_qrcode`;
CREATE TABLE `joy_recharge_rule_qrcode`  (
  `id` varchar(32) NOT NULL,
  `recharge_rule_id` varchar(32) NOT NULL COMMENT '用户充值规则ID',
  `trans_amt` decimal(8, 2) NULL DEFAULT NULL COMMENT '充值金额',
  `qrcode_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '类型:1wx 2alipy',
  `qrcode_image` varchar(128) NOT NULL COMMENT '二维码图片',
  `qrcode_url` varchar(128) NOT NULL COMMENT '二维码url链接',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态:0无效 1有效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户充值规则二维码';

DROP TABLE IF EXISTS `joy_recommend_order`;
CREATE TABLE `joy_recommend_order`  (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NULL DEFAULT NULL,
  `order_no` varchar(32) NOT NULL COMMENT '帐户编号',
  `trans_amt` decimal(6, 2) NOT NULL COMMENT '交易金额',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '订单状态:0失效 1申请 2处理中 3完成',
  `description` varchar(32) NULL DEFAULT NULL COMMENT '交易描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户返佣提现订单';

DROP TABLE IF EXISTS `joy_recommend_rule`;
CREATE TABLE `joy_recommend_rule`  (
  `id` varchar(32) NOT NULL,
  `rec_type` tinyint(4) NOT NULL COMMENT '返佣类型',
  `rec_val1` decimal(6, 2) NULL DEFAULT NULL COMMENT '一级',
  `rec_val2` decimal(6, 2) NULL DEFAULT NULL COMMENT '二级',
  `rec_val3` decimal(6, 2) NULL DEFAULT NULL COMMENT '三级',
  `rec_amount` decimal(6,2) DEFAULT NULL COMMENT '返佣金额',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户返佣规则';

DROP TABLE IF EXISTS `joy_extension_rule`;
CREATE TABLE `joy_extension_rule` (
  `id` varchar(32) NOT NULL,
  `extension_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '推广类型',
  `rec_amount` decimal(6,2) DEFAULT NULL COMMENT '返佣游戏币',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '权益状态 0:无效 1有效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='系统活动返佣规则';

DROP TABLE IF EXISTS `joy_extension_adv`;
CREATE TABLE `joy_extension_adv` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `adv_type` tinyint(4) NOT NULL COMMENT '广告类型',
  `adv_image` varchar(64) NOT NULL COMMENT '图片地址',
  `adv_para` varchar(128) DEFAULT NULL COMMENT '参数',
  `status` tinyint(4) NOT NULL COMMENT '状态',
  `description` varchar(100) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='系统广告管理';

DROP TABLE IF EXISTS `joy_sys_dictionary`;
CREATE TABLE `joy_sys_dictionary`  (
  `id` varchar(32) NOT NULL,
  `dict_code` varchar(32) NOT NULL COMMENT '字典标识',
  `dict_name` varchar(50) NOT NULL COMMENT '字典名称',
  `dict_value` varchar(50) NOT NULL COMMENT '字典值',
  `description` varchar(200) NULL DEFAULT NULL COMMENT '描述',
  `sort` tinyint(4) NULL DEFAULT NULL COMMENT '排序',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) COMMENT = '字典';

DROP TABLE IF EXISTS `joy_sys_login_log`;
CREATE TABLE `joy_sys_login_log`  (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `browser_type` varchar(200) NULL DEFAULT NULL COMMENT '浏览器类型',
  `ip_address` varchar(200) NULL DEFAULT NULL COMMENT 'ip地址',
  `status` tinyint(4) NOT NULL COMMENT '状态，0失败，1成功',
  `create_time` datetime NOT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户登录日志';

DROP TABLE IF EXISTS `joy_sys_oper_log`;
CREATE TABLE `joy_sys_oper_log` (
  `id` varchar(32) NOT NULL COMMENT '编号',
  `type` tinyint(4) DEFAULT '1' COMMENT '日志类型',
  `title` varchar(255) DEFAULT '' COMMENT '日志标题',
  `remote_addr` varchar(255) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '用户代理',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求URI',
  `request_method` varchar(8) DEFAULT NULL COMMENT '操作方式',
  `params` varchar(1000) DEFAULT NULL,
  `exception` text COMMENT '异常信息',
  `response` text COMMENT '返回内容',
  `consume_ms` varchar(16) DEFAULT NULL COMMENT '消耗时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建者id',
  `create_user_name` varchar(32) DEFAULT NULL COMMENT '创建者名字',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户操作日志';

DROP TABLE IF EXISTS `joy_sys_menu`;
CREATE TABLE `joy_sys_menu`  (
  `id` varchar(32) NOT NULL,
  `pid` varchar(32) NULL DEFAULT NULL,
  `title` varchar(50) NOT NULL COMMENT '名称',
  `path` varchar(512) NOT NULL COMMENT '菜单路径',
  `url` varchar(50) NOT NULL COMMENT '授权标识',
  `permission` varchar(32) NULL DEFAULT NULL,
  `icon` varchar(32) NULL DEFAULT NULL COMMENT '图标',
  `type` tinyint(4) NOT NULL COMMENT '菜单类型 1:菜单 2:权限',
  `sort` tinyint(4) NOT NULL COMMENT '排序',
  `description` varchar(100) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) COMMENT = '权限表';

DROP TABLE IF EXISTS `joy_sys_role`;
CREATE TABLE `joy_sys_role`  (
  `id` varchar(32) NOT NULL COMMENT '角色id',
  `role_code` varchar(10) NOT NULL COMMENT '角色code',
  `role_name` varchar(20) NOT NULL COMMENT '角色名称',
  `description` varchar(100) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '角色表';

DROP TABLE IF EXISTS `joy_sys_role_menu`;
CREATE TABLE `joy_sys_role_menu`  (
  `role_id` varchar(32) NOT NULL COMMENT '角色id',
  `menu_id` varchar(32) NOT NULL COMMENT '权限id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间'
) COMMENT = '角色权限关联表';

DROP TABLE IF EXISTS `joy_sys_config`;
CREATE TABLE `joy_sys_config`  (
  `id` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `value` varchar(255) NOT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '系统设置';

DROP TABLE IF EXISTS `joy_sys_gfw`;
CREATE TABLE `joy_sys_gfw` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `app_name` varchar(10) NOT NULL COMMENT '应用名称',
  `app_code` varchar(16) NOT NULL COMMENT '应用code',
  `app_type` tinyint(4) NOT NULL COMMENT '应用类型1当前,2第三方',
  `master_url` varchar(128) DEFAULT NULL COMMENT '主要域名',
  `slave_url` varchar(128) DEFAULT NULL COMMENT '从域名',
  `var_url` varchar(16) DEFAULT NULL COMMENT '第三方应用中间段',
  `status` tinyint(4) NOT NULL COMMENT '状态',
  `description` varchar(100) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='防封管理';

DROP TABLE IF EXISTS `joy_sys_user`;
CREATE TABLE `joy_sys_user`  (
  `id` varchar(32) NOT NULL COMMENT '用户id',
  `user_name` varchar(20) NOT NULL COMMENT '账号',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `salt` varchar(8) NOT NULL COMMENT '加密盐',
  `nick_name` varchar(20) NOT NULL COMMENT '昵称',
  `headimg_url` varchar(200) NULL DEFAULT NULL COMMENT '头像',
  `sex` tinyint(4) NOT NULL COMMENT '性别',
  `phone` varchar(12) NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) NULL DEFAULT NULL COMMENT '邮箱',
  `email_verified` int(11) NULL DEFAULT NULL COMMENT '邮箱是否验证，0未验证，1已验证',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态，1正常，0冻结',
  `create_time` datetime NOT NULL COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_user_name`(`user_name`)
) COMMENT = '用户表';

DROP TABLE IF EXISTS `joy_sys_user_role`;
CREATE TABLE `joy_sys_user_role`  (
  `id` varchar(32) NOT NULL,
  `user_id` varchar(32) NOT NULL COMMENT '用户id',
  `role_id` varchar(32) NOT NULL COMMENT '角色id',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT = '用户角色关联表';

DROP TABLE IF EXISTS `joy_wechat_member`;
CREATE TABLE `joy_wechat_member`  (
  `id` varchar(32) NOT NULL,
  `openid` varchar(64) NOT NULL,
  `subscribe_id` varchar(32) NOT NULL,
  `subscribe` tinyint(4) NOT NULL,
  `nick_name` varchar(128) NULL DEFAULT NULL,
  `birth` datetime NULL DEFAULT NULL,
  `headimg_url` varchar(256) NULL DEFAULT NULL,
  `sex` tinyint(4) NULL DEFAULT NULL,
  `country` varchar(64) NULL DEFAULT NULL,
  `province` varchar(64) NULL DEFAULT NULL,
  `city` varchar(32) NULL DEFAULT NULL,
  `unionid` varchar(64) NULL DEFAULT NULL,
  `language` varchar(8) NULL DEFAULT NULL,
  `token` varchar(32) NULL DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态，1正常，0冻结',
  `update_time` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  `remarks` varchar(128) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid_idx` (`openid`)
) COMMENT = '微信用户';

DROP TABLE IF EXISTS `joy_wechat_member_addr`;
CREATE TABLE `joy_wechat_member_addr`  (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NOT NULL,
  `real_name` varchar(32) NOT NULL COMMENT '收件人',
  `addr_area` varchar(64) NOT NULL COMMENT '区域',
  `addr_detail` varchar(128) NULL DEFAULT NULL COMMENT '详细',
  `mobile` varchar(16) NULL DEFAULT NULL COMMENT '电话号码',
  `addr_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '地址状态1:实体 2:虚拟',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '地址状态1:正常 2:默认',
  `deleted` tinyint(4) NOT NULL DEFAULT 1 COMMENT '删除状态0删除 1正常',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户账户';

DROP TABLE IF EXISTS `joy_wechat_member_recommend`;
CREATE TABLE `joy_wechat_member_recommend`  (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NOT NULL COMMENT '微信用户id',
  `accept_code` varchar(32) NULL DEFAULT NULL COMMENT '邀请code',
  `receive_wechat_member_id` varchar(32) NULL DEFAULT NULL COMMENT '接受邀请微信用户id',
  `cash_image` varchar(128) NULL DEFAULT NULL COMMENT '当前用户收款码图片',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '用户账户';

DROP TABLE IF EXISTS `joy_wechat_subscribe`;
CREATE TABLE `joy_wechat_subscribe` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `app_name` varchar(16) DEFAULT NULL COMMENT '微信名称',
  `app_code` varchar(16) DEFAULT NULL COMMENT '微信号',
  `app_id` varchar(32) DEFAULT NULL COMMENT '账号id',
  `app_secret` varchar(32) DEFAULT NULL COMMENT '账号秘钥',
  `app_type` int(11) DEFAULT NULL COMMENT '微信类型',
  `encoding_aes_key` varchar(64) DEFAULT NULL COMMENT '对消加密秘钥',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态',
  `token` varchar(16) DEFAULT NULL COMMENT '用户验证token',
  `web_app_id` varchar(32) DEFAULT NULL COMMENT 'web账号id',
  `web_app_secret` varchar(32) DEFAULT NULL COMMENT 'web账号秘钥',
  `menu_data` text COMMENT '微信菜单JSON',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '微信号管理';

DROP TABLE IF EXISTS `joy_wechat_template`;
CREATE TABLE `joy_wechat_template` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `event_type` tinyint(4) NOT NULL COMMENT '类型 1系统, 2自定义',
  `event_code` varchar(32) DEFAULT NULL COMMENT '事件操作',
  `event_keywords` varchar(32) DEFAULT NULL COMMENT '事件关键字',
  `response_type` tinyint(4) NOT NULL COMMENT '回复类型 1:文本消息 2:图片消息 3:图文消息',
  `response_text` varchar(512) DEFAULT NULL COMMENT '主键',
  `response_title` varchar(64) DEFAULT NULL COMMENT '文本消息',
  `response_article_url` varchar(128) DEFAULT NULL COMMENT '图文标题url',
  `response_pic_url` varchar(128) DEFAULT NULL COMMENT '图文图片url',
  `response_description` varchar(128) DEFAULT NULL COMMENT '图片描述',
  `subscribe_id` varchar(32) DEFAULT NULL COMMENT '微信号',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT = '微信回复规则';

DROP TABLE IF EXISTS `joy_wechat_member_profit`;
CREATE TABLE `joy_wechat_member_profit` (
  `id` varchar(32) NOT NULL,
  `wechat_member_id` varchar(32) NOT NULL COMMENT '微信用户id',
  `recommend_rule_id` varchar(32) NOT NULL COMMENT '权益id',
  `rec_val1` decimal(4,2) NOT NULL COMMENT '一级比例',
  `rec_val2` decimal(4,2) NOT NULL COMMENT '二级比例',
  `rec_val3` decimal(4,2) NOT NULL COMMENT '三级比例',
  `last_mirror` varchar(64) DEFAULT NULL COMMENT '上次返佣配置备份',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '权益状态 0:无效 1有效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) COMMENT='用户权益';

#end
