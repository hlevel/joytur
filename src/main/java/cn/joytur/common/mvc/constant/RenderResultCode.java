package cn.joytur.common.mvc.constant;

import com.jfinal.kit.JsonKit;

/**
 * 错误枚举
 * @author xuhang
 * @time 2018年8月2日 下午4:56:46
 */
public enum RenderResultCode {

	NOTLOGGEDIN(401, "当前用户未登录"),
	NOTAUTH(403, "无权限访问"),
	/**
	 * code:0, message:请求服务器成功
	 */
	SUCCESS(200, "操作成功"),
	ERROR(500, "操作失败"),
	/**
	 * code:-1, message:服务器未知异常
	 */
	UNKNOWN(-1, "服务器未知异常"),
	/**
	 * code:-1, message:参数错误
	 */
	/**
	 * 100 - 150 系统异常
	 */
	PARAM(100, "参数错误"),
	
	/**
	 * 100 - 150 系统异常
	 */
	COMMON_150(150, "上传成功"),
	COMMON_151(151, "修改头像失败"),
	COMMON_152(152, "修改头像失败"),
	
	
	COMMON_161(161, "微信回调地址失败."),
	COMMON_162(162, "签名验证失败"),
	
	/**
     * 200 - 299 业务异常
     */
    BUSINESS_201(201, "登录成功"),
    BUSINESS_210(210, "当前用户未找到"),
    BUSINESS_211(211, "当前用户密码错误"),
    BUSINESS_212(212, "当前用户已被禁用"),
    BUSINESS_213(213, "当前用户未找到"),
    BUSINESS_214(214, "当前用户已经存在"),
    BUSINESS_215(215, "两次密码不一致"),
    BUSINESS_216(216, "密码不能为空"),
    BUSINESS_217(217, "用户不能为空"),
    BUSINESS_218(218, "当前无权限操作"),
    BUSINESS_219(219, "登录失败次数过请稍后再试"),
    BUSINESS_220(220, "登录失败次数过多您的帐号已禁用,请联系管理员"),
    BUSINESS_221(221, "当前用户原始密码错误"),
    BUSINESS_222(222, "当前原始密码不能和新修改密码一致"),
    
    BUSINESS_230(230, "当前菜单已经存在"),
    BUSINESS_231(231, "当前菜单已被引用,请先删子级菜单"),
    BUSINESS_232(232, "当前字典已经存在"),
    BUSINESS_233(233, "当前参数已经存在"),
    BUSINESS_234(234, "当前角色已经存在"),
    BUSINESS_235(235, "当前角色已被用户引用,请先取消授权"),
    BUSINESS_236(236, "当前用户已经存在"),
    BUSINESS_237(237, "当前应用名称已经存在"),
    BUSINESS_238(238, "当前应用未找到"),
    BUSINESS_239(239, "同步失败!应用配置已经存在,请联系删除或者禁用"),
    BUSINESS_240(240, "请填写第三方应用的中段url"),
    
    BUSINESS_249(249, "当前公众号未找到"),
    BUSINESS_250(250, "更新公众号配置成功"),
    BUSINESS_251(251, "当前公众号模版已经存在"),
    BUSINESS_252(252, "当前公众号模版未找到"),

    BUSINESS_253(253, "当前系统模版无法删除"),
    BUSINESS_254(254, "请先配置公众号信息"),
    
    BUSINESS_260(260, "当前会员未找到"),
    BUSINESS_265(265, "当前地址添加过多"),
    

    /* 充值 */
    BUSINESS_300(300, "订单号编号不存在"),
    BUSINESS_301(301, "充值规则不存在"),
    BUSINESS_302(302, "充值订单状态不正确"),
    BUSINESS_303(303, "充值订单升级代理未找到对应配置,请联系客服"),
    BUSINESS_305(305, "充值订单未找到对应配置,请联系客服"),
    BUSINESS_304(304, "当前充值业务失败,请联系客服"),
    BUSINESS_306(306, "抱歉您的账户可用游戏币不足"),
    BUSINESS_307(307, "抱歉提现规则未找到,操作失败"),
    BUSINESS_308(308, "提现余额不足,操作失败"),
    BUSINESS_309(309, "提现订单不存在"),
    BUSINESS_310(310, "当前提现订单用户未提交收款码"),
    BUSINESS_311(311, "当前提现订单状态已经处理,请勿重复处理"),
    BUSINESS_312(312, "收款二维码有误,请检查再上传"),
    BUSINESS_313(313, "未配置收款二维码,请联系客服"),
    BUSINESS_314(314, "支付订单超时"),
    BUSINESS_315(315, "充值订单处理成功,用户已到账"),
    BUSINESS_316(316, "闯关关卡记录未找到"),
    BUSINESS_317(316, "闯关关卡记录状态不对或者已经推过款"),
    

    BUSINESS_350(350, "代理规则名称已存在"),
    BUSINESS_351(351, "当前代理规则未找到"),
    BUSINESS_352(352, "基础代理规则未找到,请检查后台配置"),
    
    BUSINESS_355(355, "活动规则已存在"),
    BUSINESS_356(356, "活动规则未找到"),
    BUSINESS_357(357, "广告位类型已存在"),
    BUSINESS_358(358, "广告位未找到"),

    BUSINESS_360(360, "商品分类名称已存在"),
    BUSINESS_361(361, "当前商品分类名称未找到"),
    BUSINESS_362(362, "操作失败,当前商品未找到"),
    BUSINESS_363(363, "操作失败,当前商品不符合赠送条件"),
    BUSINESS_364(364, "奖品赠送成功,请在个人中心-我的奖品查看"),
    
    BUSINESS_366(366, "当前发货订单未找到"),
    BUSINESS_367(367, "当前发货订单状态已发货"),
    BUSINESS_368(368, "用户未设置默认地址无法已发货"),
    
    BUSINESS_370(370, "关卡击中数量不能为空"),
    BUSINESS_371(371, "关卡难度不能为空"),
    BUSINESS_372(372, "当前关卡规则未找到"),
    BUSINESS_373(373, "当前关卡规则名称已经存在"),
    BUSINESS_374(374, "当前游戏结束!闯关失败(原因:闯关数据超时处理)"),
    BUSINESS_375(375, "当前游戏结束!闯关失败(原因:未找到闯关数据)"),
    BUSINESS_376(376, "当前游戏结束!闯关失败(原因:闯关数据重复处理)"),
    BUSINESS_377(377, "当前游戏结束!恭喜您闯关成功"),
    

    BUSINESS_99999(99999, "-------------end封山-----------------");
	
	RenderResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return JsonKit.toJson(this);
    }
    
}
