package cn.joytur.modules.order.controller.api;

import com.jfinal.aop.Inject;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseApiController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.service.RechargeOrderService;
import cn.joytur.modules.system.entities.SysConfig;

/**
 * 订单接口监控
 * @author xuhang
 */
@RouteMapping(url = "/api/monitor")
public class ApiMonitorController extends BaseApiController{
	
	@Inject
	private RechargeOrderService rechargeOrderService;

	//支付回调通知
	public void index() {
		renderJson(RenderResult.success());
	}
	
	/**
	 * 新增用户测试
	
	public void testAddMember(){
		String acceptCode = getPara();
		String uuid = JoyIdUtil.simpleUUID();
		//创建保存用户
		WechatMember tmpWechatMember = new WechatMember();
		tmpWechatMember.setOpenid(uuid);
		tmpWechatMember.setSubscribe(1L);
		tmpWechatMember.setSubscribeId(WechatSubscribe.dao.findDefault().getId());
		tmpWechatMember.setNickName(uuid);
		tmpWechatMember.setSex(1L);
		tmpWechatMember.setLanguage("zh_CN");
		tmpWechatMember.setCountry("中国");
		tmpWechatMember.setProvince("湖北");
		tmpWechatMember.setCity("测试");
		tmpWechatMember.setHeadimgUrl("http://thirdwx.qlogo.cn/mmopen/du913FMg7ZOQahBe4FNbFicibGyoqSnfiat7Yn4h98hpWic0GKicNUvH0NwUn34nLTMETWEhibhRcxovcrOhOAyM9focM5SDEChJLB/132");
		
		Duang.duang(WechatMemberService.class).saveWechatMemberProfit(tmpWechatMember, acceptCode);
		
		renderJson(RenderResult.success());
	}
	 */
	
	/**
	 * app心跳
	 */
	public void appHeart(){
		String t = getPara("t");
		String sign = getPara("sign");
		
		if(!JoyIdUtil.signatureVerification(t, sign)){
			renderJson(RenderResult.error(RenderResultCode.COMMON_162)); return;
		}
		
		DateTime dateTime = DateUtil.date(Long.valueOf(t));
		String nowDate = DateUtil.formatDateTime(dateTime.toJdkDate());
		
		SysConfig quySysConfig = new SysConfig();
		quySysConfig.setName(Enums.SysConfigType.MONITOR_APP_HEARTBEAT.name());
		
		SysConfig tmpSysConfig = SysConfig.dao.findByModel(quySysConfig);
		if(tmpSysConfig == null){
			tmpSysConfig = new SysConfig();
			tmpSysConfig.setId(IdUtil.simpleUUID());
			tmpSysConfig.setName(Enums.SysConfigType.MONITOR_APP_HEARTBEAT.name());
			tmpSysConfig.setValue(nowDate);
			tmpSysConfig.setCreateTime(new java.util.Date());
			tmpSysConfig.setUpdateTime(new java.util.Date());
			tmpSysConfig.save();
		}else{
			tmpSysConfig.setValue(nowDate);
			tmpSysConfig.setUpdateTime(new java.util.Date());
			tmpSysConfig.update();
		}
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * app推送
	 */
	public void appPush(){
		Integer type = getParaToInt("type");
		String price = getPara("price");
		String t = getPara("t");
		String sign = getPara("sign");
		
		if(!JoyIdUtil.signatureVerification((type+price+t), sign)){
			renderJson(RenderResult.error(RenderResultCode.COMMON_162)); return;
		}
		Long rechargeType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, ""));
		
		rechargeOrderService.payCompletedRechargeOrder(Double.valueOf(price), rechargeType, null);
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * mail推送
	 */
	public void mailPush(){
		String price = getPara("price");
		String t = getPara("t");
		String sign = getPara("sign");
		
		if(!JoyIdUtil.signatureVerification((price+t), sign)){
			renderJson(RenderResult.error(RenderResultCode.COMMON_162)); return;
		}
		Long rechargeType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, ""));
		rechargeOrderService.payCompletedRechargeOrder(Double.valueOf(price), rechargeType, null);
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * mail推送
	 */
	public void mailPush1(){
		String icon = "fa-close";
		icon = "fa-check";
		
		String t = String.valueOf(System.currentTimeMillis());
		String sign = JoyIdUtil.signatureEncrypt(t);
		
		setAttr("icon", icon).set("message", "确认到账成功!").set("t", t).set("sign", sign);
		render(getPrefix() + "/admin/error/receipt_su.html");
	}
	
}
