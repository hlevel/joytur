package cn.joytur.modules.order.controller.wap;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.aop.Inject;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.common.utils.JoyUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.order.entites.RechargeOrder;
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.order.service.RechargeOrderService;
import cn.joytur.modules.product.entities.ExtensionRule;
import cn.joytur.modules.product.entities.RechargeRule;
import cn.joytur.modules.product.entities.RechargeRuleQrCode;
import cn.joytur.modules.product.entities.RecommendRule;
import cn.joytur.modules.system.entities.SysConfig;
import cn.joytur.modules.wechat.entities.WechatMember;

/**
 * wap 充值订单
 * @author xuhang
 */
@RouteMapping(url = "/wap/recharge/order")
@AuthRequire.Logined
public class WapRechargeOrderController extends BaseWapController {

	@Inject
	private RechargeOrderService rechargeOrderService;
	
	/**
	 * 列表
	 */
	public void index() {
		RechargeRule param = new RechargeRule();
		param.setStatus(1L);
		List<RechargeRule> rechargeRules = RechargeRule.dao.findList(param, new Sort("trans_amt", Enums.SortType.ASC));
		setAttr("rechargeRules", rechargeRules);
		setAttr("gameAccount", Account.dao.getGameAccount(getWapMemberDTO().getWechatMemberId()));
		if(closeingBetweenBusinessTimeStatus()){
			renderWap("order/suspend_recharge.html"); return;
		}
		
		renderWap("order/recharge.html");
	}

	/**
	 * 创建充值订单
	 */
	public void payment() {
		//判断是否开启智能营业状态
		if(closeingBetweenBusinessTimeStatus()){
			renderWap("order/suspend_recharge.html"); return;
		}
		
		String ruleId = getPara("ruleId");

		if (StrUtil.isBlank(ruleId) || ruleId.length() != 32) {
			throw new BusinessException(RenderResultCode.PARAM);
		}
		//查询是否配置无状态收款
		int wxPayStatelessCount = SysConfig.dao.findCountByModel(new SysConfig().setName(Enums.SysConfigType.WAP_WXPAY_QRCODE.name()));
		//查询是否配置有状态收款
		int wxPayStatefulCount = RechargeRuleQrCode.dao.findCountByModel(new RechargeRuleQrCode().setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1"))));
		if(wxPayStatelessCount == 0 && wxPayStatefulCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_313);
		}
		
		//先查询充值订单
		RechargeRule rule = RechargeRule.dao.findById(ruleId);

		Double transAmt = null;
		Double transAfterAmt = null;
		Long type = null;
		//再查询升级订单
		if(rule == null){
			RecommendRule tmpRecommendRule = RecommendRule.dao.findById(ruleId);
			if(tmpRecommendRule == null){
				throw new BusinessException(RenderResultCode.BUSINESS_301);
			}
			transAmt = tmpRecommendRule.getRecAmount();
			transAfterAmt = tmpRecommendRule.getRecAmount();
			type = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_REWARD, DictAttribute.ACCOUNT_ACC_UNIT_TYPE, "2"));
		}else{
			//判断是否达到单日充值上限
			RechargeOrder quyRechargeOrder = new RechargeOrder();
			quyRechargeOrder.setWechatMemberId(getWapMemberDTO().getWechatMemberId());
			quyRechargeOrder.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_GAME, DictAttribute.ACCOUNT_ACC_UNIT_TYPE, "1")));
			quyRechargeOrder.setRechargeRuleId(ruleId);
			quyRechargeOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_FINISH, DictAttribute.RECHARGE_ORDER_STATUS, "1")));
			int count = RechargeOrder.dao.findCountByModel(quyRechargeOrder);
			if(count >= rule.getTransDayLimit().intValue()){
				setAttr("limitAmt", rule.getTransAmt()).setAttr("limitCount", count);
				renderWap("order/limit_recharge.html"); return;
			}
			
			transAmt = rule.getTransAmt();
			transAfterAmt = rule.getTransAfterAmt();
			type = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_GAME, DictAttribute.ACCOUNT_ACC_UNIT_TYPE, "1"));
		}
		
		//更新所有用户过时定
		RechargeOrder.dao.updateAllExpireOrder();
		//更新当前用户所有订单都为过期
		RechargeOrder.dao.updateMemberAllExpireOrder(getWapMemberDTO().getWechatMemberId());
		
		//创建新订单
		RechargeOrder rechargeOrder = rechargeOrderService.saveRechargeOrder(ruleId, type, transAmt, transAfterAmt, getWapMemberDTO().getWechatMemberId());
		
		/*
		Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, "1"));

		RechargeOrder rechargeOrder = new RechargeOrder();
		rechargeOrder.setWechatMemberId(getWapMemberDTO().getWechatMemberId());
		rechargeOrder.setStatus(status);
		rechargeOrder = RechargeOrder.dao.findByModel(rechargeOrder);

		long time = CommonAttribute.RECHARGE_EXPIRE_TIME;
		if (rechargeOrder == null) {
			rechargeOrder = createRechargeOrder(rule, getWapMemberDTO().getWechatMemberId());
		} else {
			long now = System.currentTimeMillis();
			long expire = rechargeOrder.getExpireTime().getTime();
			time = (expire - now) / 1000;
			// 订单已过期
			if (time < 0) {
				rechargeOrder = createRechargeOrder(rule, getWapMemberDTO().getWechatMemberId());
				time = CommonAttribute.RECHARGE_EXPIRE_TIME;
			}
		}
		*/
		setAttr("time", CommonAttribute.RECHARGE_EXPIRE_TIME);
		setAttr("rechargeOrder", rechargeOrder);
		renderWap("order/order.html");
	}
	
	/**
	 * 佣金转游戏币
	 */
	@Before(Tx.class)
	public void transfer() {
		//查询当前用户资金账户余额
		Account msAccount = Account.dao.getSettAccount(getWapMemberDTO().getWechatMemberId());
		
		//查询最低金额
		RechargeRule queryRechargeRule = new RechargeRule();
		queryRechargeRule.setStatus(1L);
		RechargeRule tmpRechargeRule =  RechargeRule.dao.findByModel(queryRechargeRule, 1);
		
		if(tmpRechargeRule == null){
			renderJson(RenderResult.error(RenderResultCode.BUSINESS_307));return;
		}
		
		//如果转游戏少于最小金额则失败
		Double minAmt = Double.valueOf(JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_ALLOW_TRANSFER.name(), "2"));
		if(msAccount.getAvbAmt() < minAmt){
			renderJson(RenderResult.error("转账失败!最小转账额度为:"+tmpRechargeRule.getTransAmt()));return;
		}
		
		//额外赠送
		Double addedGame = 0.0d;
		//正常扣除金额
		Double transferAmt = msAccount.getAvbAmt();
		//正常添加币
		Double transferGame = 0.0d;
		
		//查询是否有推广活动
		ExtensionRule quyExtensionRule = new ExtensionRule();
		quyExtensionRule.setExtensionType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.EXTENSION_TYPE3, DictAttribute.EXTENSION_TYPE, "3")));
		quyExtensionRule.setStatus(1L);
		ExtensionRule tmpExtensionRule = ExtensionRule.dao.findByModel(quyExtensionRule);
		if(tmpExtensionRule != null){
			addedGame = tmpExtensionRule.getRecAmount();
		}
		
		//查询是否有区间额度
		RechargeRule queryRechargeRule2 = new RechargeRule();
		queryRechargeRule2.setTransAmt(msAccount.getAvbAmt());
		queryRechargeRule2.setStatus(1L);
		RechargeRule tmpRechargeRule2 =  RechargeRule.dao.findByModel(queryRechargeRule2);
		if(tmpRechargeRule2 != null){
			transferGame = tmpRechargeRule2.getTransAfterAmt();
		}else{
			int gameAmt = Integer.valueOf(transferAmt.toString()) + 1;
			transferGame = (double) gameAmt;
		}
		transferGame = NumberUtil.add(transferGame, addedGame);
		
		AccountService accountService = Duang.duang(AccountService.class);
		accountService.transferGameCurrency(getWapMemberDTO().getWechatMemberId(), transferAmt, transferGame);
		
		LOGGER.info("WechatMemberId={}, 转出佣金={}, 增加游戏币={}", getWapMemberDTO().getWechatMemberId(), transferAmt, transferGame);
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * 检查支付状态
	 */
	public void paystate() {
		String id = getPara("id");
		RechargeOrder tmpRechargeOrder = RechargeOrder.dao.findById(id);
		
		if(tmpRechargeOrder == null){
			renderJson(RenderResult.definedResult(RenderResultCode.BUSINESS_300)); return;
		}

		//超时
		if(DateUtil.between(tmpRechargeOrder.getCreateTime(), new java.util.Date(), DateUnit.SECOND) > CommonAttribute.RECHARGE_EXPIRE_TIME){
			renderJson(RenderResult.definedResult(RenderResultCode.BUSINESS_314)); return;
		}
		
		//支付成功
		Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_FINISH, DictAttribute.RECHARGE_ORDER_STATUS, "1"));
		if(tmpRechargeOrder.getStatus() == status){
			renderJson(RenderResult.success()); return;
		}
		
		//邮件未通知
		Long mailStatus = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_PROHIBIT, DictAttribute.STATUS, "0"));
		long ms = DateUtil.between(new java.util.Date(), tmpRechargeOrder.getCreateTime(), DateUnit.SECOND);
		if(tmpRechargeOrder.getMailStatus() == mailStatus){
			String receiptMail = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_RECEIPT_MAIL.name());
			if(StrKit.notBlank(receiptMail)){ //收件箱不能为空
				//查询时间
				if(ms > 5){
					WechatMember payWechatMember = WechatMember.dao.findById(tmpRechargeOrder.getWechatMemberId());
					try{
						long t = System.currentTimeMillis();
						Double price = tmpRechargeOrder.getRealTransAmt();
						String sign = JoyIdUtil.signatureEncrypt(String.valueOf(price+t));
						
						String url = JoyConfigUtil.getSdUrl()+"/api/monitor/mailPush?price=" + price + "&t=" + t + "&sign=" + sign;
						String content = "<a href=\""+url+"\">点击确认收款</a>";
						
						JoyUtil.sendEmail(receiptMail, "用户(呢称:"+payWechatMember.getNickName()+")支付微信:" + price + "元请确认!", content);
						
						//发送成功后更新邮件
						RechargeOrder updateRechargeOrder = new RechargeOrder();
						updateRechargeOrder.setMailStatus(1L);
						RechargeOrder.dao.updateModelById(updateRechargeOrder, tmpRechargeOrder.getId());
						
					}catch (Exception e){
						e.printStackTrace();
						LOGGER.error(e.getMessage(), e);
					}
				}
				
			}
		}
		
		renderJson(RenderResult.error()); return;
	}
	
	/**
	 * 判断是否正常营业
	 * @return
	 */
	private boolean closeingBetweenBusinessTimeStatus(){
		//判断是否开启智能营业状态
		String swstate = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CLOSING_SWITCH.name(), "0");
		if(StrKit.equals(swstate, JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "0"))){
			String swTime = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CLOSING_TIME.name(), "09:00:00-12:00:00");
			//判断当前时间是否在时间段内
			if(!JoyUtil.getNowBetweenBusinessTime(swTime)){
				setAttr("swtime", JoyUtil.getBusinessTime(swTime));
				return true;
			}
		}
		return false;
	}
	
}