package cn.joytur.common.mvc.constant;

/**
 * 字典管理
 * @author xuhang
 */
public final class DictAttribute {
	
	public static final String MENU_TYPE = "menu_type"; //菜单code
	public static final String MENU_TYPE_MENU = "菜单"; 
	public static final String MENU_TYPE_AUTHORITY = "权限";
	
	public static final String STATUS = "status"; //状态code
	public static final String STATUS_PROHIBIT = "禁用"; //状态name
	public static final String STATUS_ENABLE = "启用"; //状态name
	
	public static final String SEX = "sex"; //性别code
	public static final String SEX_FEMALE = "女"; 
	public static final String SEX_MALE = "男";
	
	public static final String GFW_APP_TYPE = "app_type"; //应用类型
	public static final String GFW_APP_TYPE1 = "当前"; //自己
	public static final String GFW_APP_TYPE2 = "第三方"; //当前
	
	public static final String SUBSCRIBE_TYPE = "subscribe_type"; //公众号code
	public static final String SUBSCRIBE_TYPE_PERSONAL = "个人订阅号"; 
	public static final String SUBSCRIBE_TYPE_ENTERPRISE = "企业订阅号";
	public static final String SUBSCRIBE_TYPE_SERVICE = "企业服务号";
	
	public static final String TEMPLATE_EVENT_TYPE = "template_rule_type"; //公众号code
	public static final String TEMPLATE_EVENT_TYPE_SYSTEM = "系统"; 
	public static final String TEMPLATE_EVENT_TYPE_CUSTOM = "自定义";
	
	public static final String TEMPLATE_RESPONSE_TYPE = "template_response_type"; //公众号code
	public static final String TEMPLATE_RESPONSE_TYPE_TEXT = "文本消息"; 
	public static final String TEMPLATE_RESPONSE_TYPE_IMAGE = "图片消息";
	public static final String TEMPLATE_RESPONSE_TYPE_IMAGETEXT = "图文消息";
	
	public static final String SUBSCRIBE_STATUS = "subscribe_status"; //状态code
	public static final String SUBSCRIBE_STATUS_YES = "已关注"; //状态name
	public static final String SUBSCRIBE_STATUS_NO = "未关注"; //状态name

	public static final String RECOMMEND_LEVEL = "recommend_level";  // 会员等级
	public static final String RECOMMEND_LEVEL1 = "中级代理";
	public static final String RECOMMEND_LEVEL2 = "高级代理";
	
	public static final String ACCOUNT_ACC_TYPE = "account_acc_type"; //账户类型code
	public static final String ACCOUNT_ACC_TYPE_INSIDE_GAME = "内部游戏币账户"; //name
	public static final String ACCOUNT_ACC_TYPE_INSIDE_SETT = "内部结算账户"; //name
	public static final String ACCOUNT_ACC_TYPE_GAME = "游戏币账户"; //name
	public static final String ACCOUNT_ACC_TYPE_SETT = "结算账户"; //name
	
	public static final String ACCOUNT_FUNDS_ELE = "account_funds_ele"; //资金成分code
	public static final String ACCOUNT_FUNDS_ELE_RECHARGE = "充值游戏币"; //name
	public static final String ACCOUNT_FUNDS_ELE_AGENT = "升级代理"; //name
	public static final String ACCOUNT_FUNDS_ELE_CONSUME = "闯关消耗"; //name
	public static final String ACCOUNT_FUNDS_ELE_RECOMMEND1 = "一级推广奖励"; //name
	public static final String ACCOUNT_FUNDS_ELE_RECOMMEND2 = "二级推广奖励"; //name
	public static final String ACCOUNT_FUNDS_ELE_RECOMMEND3 = "三级推广奖励"; //name
	public static final String ACCOUNT_FUNDS_ELE_WITHFRE = "冻结"; //name
	public static final String ACCOUNT_FUNDS_ELE_WITHUNFRE = "提现"; //name
	public static final String ACCOUNT_FUNDS_ELE_MARKETING = "推广奖励"; //name
	public static final String ACCOUNT_FUNDS_ELE_CONVERT = "兑换游戏币"; //name
	public static final String ACCOUNT_FUNDS_ELE_REFUND = "游戏退币"; //name
	
	public static final String ACCOUNT_FUNDS_TRANSTYPE = "account_funds_transtype"; //资金流水类型code
	public static final String ACCOUNT_FUNDS_TRANSTYPE_ADD = "+"; //name
	public static final String ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT = "-"; //name
	
	public static final String ACCOUNT_ACC_UNIT_TYPE = "account_acc_unit_type"; //类型单位
	public static final String ACCOUNT_ACC_UNIT_TYPE_GAME = "游戏币"; //name
	public static final String ACCOUNT_ACC_UNIT_TYPE_REWARD = "代理"; //name
	
	public static final String RECHARGE_ORDER_STATUS = "recharge_order_status"; //充值状态code
	public static final String RECHARGE_ORDER_STATUS_COLSE = "失效"; //name
	public static final String RECHARGE_ORDER_STATUS_UNPAY = "待支付"; //name
	//public static final String RECHARGE_ORDER_STATUS_PAY = "已支付"; //name
	public static final String RECHARGE_ORDER_STATUS_FINISH = "已完成"; //name
	
	public static final String RECHARGE_COMMISSION_STATUS = "commission_status"; //充值返佣状态
	public static final String RECHARGE_COMMISSION_STATUS0 = "不返佣"; //充值返佣状态
	public static final String RECHARGE_COMMISSION_STATUS1 = "待确认"; //充值返佣状态
	public static final String RECHARGE_COMMISSION_STATUS2 = "待返佣"; //充值返佣状态
	public static final String RECHARGE_COMMISSION_STATUS3 = "已返佣"; //充值返佣状态
	
	public static final String RECHARGE_QRCODE_TYPE = "qrcode_type"; //二维码类型 1微信 2支付宝
	public static final String RECHARGE_QRCODE_TYPE1 = "微信"; //name
	public static final String RECHARGE_QRCODE_TYPE2 = "支付宝"; //name
	
	public static final String RECHARGE_ACTION_TYPE = "action_type"; //动作类型 1扫码后自动输入金额 2需要手动输入金额
	public static final String RECHARGE_ACTION_TYPE1 = "扫码后自动输入金额"; //name
	public static final String RECHARGE_ACTION_TYPE2 = "需要手动输入金额"; //name
	
	public static final String RECOMMEND_ORDER_STATUS = "recommend_order_status"; //提现状态code
	public static final String RECOMMEND_ORDER_STATUS_COLSE = "失效"; //name
	public static final String RECOMMEND_ORDER_STATUS_APPLY = "申请中"; //name
	//public static final String RECOMMEND_ORDER_STATUS_HANDLE = "处理中"; //name
	public static final String RECOMMEND_ORDER_STATUS_FINISH = "已完成"; //name
	
	public static final String EXTENSION_TYPE = "extension_type"; //活动类型
	public static final String EXTENSION_TYPE1 = "用户首次关注"; //name
	public static final String EXTENSION_TYPE2 = "推荐用户奖励"; //name
	public static final String EXTENSION_TYPE3 = "兑换游戏币奖励"; //name
	public static final String EXTENSION_TYPE4 = "满额未中也送"; //name
	
	
	public static final String GOODS_ORDER_STATUS = "goods_order_status"; //充值状态code
	public static final String GOODS_ORDER_STATUS1 = "未发货"; //name
	public static final String GOODS_ORDER_STATUS2 = "已发货"; //name
	
	public static final String GOODS_JOY_TYPE = "goods_joy_type"; //商品特性code
	public static final String GOODS_JOY_TYPE1 = "实物"; //name
	public static final String GOODS_JOY_TYPE2 = "虚拟"; //name
	
	public static final String GOODS_ORDER_TYPE = "goods_order_type"; //商品订单类型
	public static final String GOODS_ORDER_TYPE1 = "闯关送"; //name
	public static final String GOODS_ORDER_TYPE2 = "满额送"; //name
	
	public static final String GOODS_RULE_LEVEL_TYPE = "goods_rule_level_type"; //充值状态code
	public static final String GOODS_RULE_LEVEL_TYPE1 = "1关"; //name
	public static final String GOODS_RULE_LEVEL_TYPE2 = "2关"; //name
	public static final String GOODS_RULE_LEVEL_TYPE3 = "3关"; //name
	
	public static final String GOODS_RULE_DIFF_TYPE = "goods_rule_diff_type"; //充值状态code
	public static final String GOODS_RULE_DIFF_TYPE1 = "普通"; //name
	public static final String GOODS_RULE_DIFF_TYPE2 = "困难"; //name
	public static final String GOODS_RULE_DIFF_TYPE3 = "噩梦"; //name
	
	public static final String GOODS_GAME_STATUS = "goods_game_status";  //游戏记录状态
	public static final String GOODS_GAME_STATUS1 = "初始化";  //游戏中可重复开始
	public static final String GOODS_GAME_STATUS2 = "闯关中";  //游戏中不可重复
	public static final String GOODS_GAME_STATUS3 = "已结束";  //游戏已结束
	public static final String GOODS_GAME_STATUS4 = "已退币";  //游戏已退币
	
	
	public static final String ADV_BANNER_TYPE = "adv_banner_type"; //广告位管理
	public static final String ADV_BANNER_TYPE1 = "客服二维码";
	public static final String ADV_BANNER_TYPE2 = "首页展示banner";
	public static final String ADV_BANNER_TYPE3 = "升级代理banner";
	public static final String ADV_BANNER_TYPE4 = "推广分享banner";
	
	/**
	 * 不可实例化
	 */
	private DictAttribute() {
	}
    
}