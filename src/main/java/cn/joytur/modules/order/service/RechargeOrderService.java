package cn.joytur.modules.order.service;

import java.util.Calendar;
import java.util.List;

import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.joytur.common.exception.ApiBusinessException;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.async.AsyncManager;
import cn.joytur.common.mvc.async.AsyncTaskFactory;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.SortType;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.entites.RechargeOrder;
import cn.joytur.modules.product.entities.RechargeRule;
import cn.joytur.modules.product.entities.RechargeRuleQrCode;
import cn.joytur.modules.product.entities.RecommendRule;
import cn.joytur.modules.system.entities.SysConfig;
import cn.joytur.modules.wechat.service.WechatMemberService;

/**
 * 充值订单service
 * @author xuhang
 *
 */
public class RechargeOrderService {
	
	@Inject
	private WechatMemberService wechatMemberService;
	@Inject
	private AccountService accountService;
	
	/**
	 * 创建充值订单
	 * @param rule
	 * @param wechatMemberId
	 * @return
	 */
	public RechargeOrder saveRechargeOrder(String ruleId, Long type, Double transAmt,  Double transAfterAmt, String wechatMemberId) {
		Long qrcodeType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_QRCODE_TYPE1, DictAttribute.RECHARGE_QRCODE_TYPE, "1"));
		Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, "1"));
		Long actionType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ACTION_TYPE1, DictAttribute.RECHARGE_ACTION_TYPE, "1"));
		
		//扫码url
		String qrcodeImage = null;
		//扫码金额
		Double realPayAmt = transAmt;
		
		Double minAmt = NumberUtil.sub(realPayAmt, Double.valueOf(0.09));
		Double maxAmt = NumberUtil.add(realPayAmt, Double.valueOf(0.09));

		//查询当前规则最小和最大金额区分
		List<RechargeRuleQrCode> tmpRechargeRuleList = RechargeRuleQrCode.dao.findList(new RechargeRuleQrCode().setRechargeRuleId(ruleId), new Sort("trans_amt", SortType.ASC));
		if(tmpRechargeRuleList != null && tmpRechargeRuleList.size() > 0){
			Double minTransAmt = tmpRechargeRuleList.get(0).getTransAmt();
			Double maxTransAmt = tmpRechargeRuleList.get(tmpRechargeRuleList.size() - 1).getTransAmt();
			
			if(minTransAmt < minAmt) {
				minAmt = minTransAmt;
			}
			
			if(maxTransAmt > maxAmt){
				maxAmt = maxTransAmt;
			}
		}
		
		//查询是否已经存在定额未锁定金额订单
		List<RechargeRuleQrCode> tmpRechargeRuleQrCodeList = RechargeRuleQrCode.dao.findUnlockQrCodeListByTransAmt(minAmt, maxAmt);
		if(tmpRechargeRuleQrCodeList != null && tmpRechargeRuleQrCodeList.size() > 0){
			//扫描自动支付金额
			realPayAmt = tmpRechargeRuleQrCodeList.get(0).getTransAmt();
			qrcodeImage = tmpRechargeRuleQrCodeList.get(0).getQrcodeImage();
		} else {
			//扫描手动输入金额
			actionType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ACTION_TYPE2, DictAttribute.RECHARGE_ACTION_TYPE, "2"));
			SysConfig tmpSysConfig = SysConfig.dao.findByModel(new SysConfig().setName(Enums.SysConfigType.WAP_WXPAY_QRCODE.name()));
			if(tmpSysConfig == null){
				throw new BusinessException(RenderResultCode.BUSINESS_313);
			}
			qrcodeImage = tmpSysConfig.getValue();
			
			//查询所有未支付锁定扫码订单
			RechargeOrder quyRechargeOrder = new RechargeOrder().setRechargeRuleId(ruleId).setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, "1")));
			List<RechargeOrder> tmpRechargeOrderList = RechargeOrder.dao.findList(quyRechargeOrder, new Sort("real_trans_amt", SortType.ASC));
			if(tmpRechargeOrderList != null && tmpRechargeOrderList.size() > 0){
				Double maxTransAmt = tmpRechargeOrderList.get(tmpRechargeOrderList.size() - 1).getRealTransAmt();
				Double minTransAmt = tmpRechargeOrderList.get(0).getRealTransAmt();
				
				if(maxTransAmt < maxAmt){
					realPayAmt = NumberUtil.add(maxTransAmt, Double.valueOf(0.01));
				} else {
					realPayAmt = NumberUtil.sub(minTransAmt, Double.valueOf(0.01));
				}
				
			}
			
		}
		
		RechargeOrder rechargeOrder = new RechargeOrder();
		rechargeOrder.setId(IdUtil.simpleUUID());
		rechargeOrder.setCreateTime(DateTime.now());
		rechargeOrder.setUpdateTime(DateTime.now());
		rechargeOrder.setOrderNo(JoyIdUtil.getOrderNo(Enums.OrderNoType.RECHARGE_ORDER));
		rechargeOrder.setStatus(status);
		rechargeOrder.setTransType(type);
		rechargeOrder.setTransAmt(transAmt);
		rechargeOrder.setTransAfterAmt(transAfterAmt);
		rechargeOrder.setRealTransAmt(realPayAmt);
		rechargeOrder.setCommissionStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_COMMISSION_STATUS1, DictAttribute.RECHARGE_COMMISSION_STATUS, "1")));
		rechargeOrder.setActionType(actionType);
		rechargeOrder.setRechargeRuleId(ruleId);
		rechargeOrder.setQrcodeType(qrcodeType);
		rechargeOrder.setQrcodeImage(qrcodeImage);
		
		rechargeOrder.setWechatMemberId(wechatMemberId);
		rechargeOrder.setDescription(CommonAttribute.RECHARGE_DESCRIPTION);

		// 设置订单过期时间2分钟
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 2);
		rechargeOrder.setExpireTime(calendar.getTime());
		rechargeOrder.save();
		return rechargeOrder;
	}
	
	/**
	 * 支付完成订单
	 * @param realTransAmt 实际支付金额
	 * @param status 当前单子状态
	 */
	public void payCompletedRechargeOrder(Double realTransAmt, Long status, String wechatMemberId){
		RechargeOrder quyRechargeOrder = new RechargeOrder();
		quyRechargeOrder.setRealTransAmt(realTransAmt);
		if(wechatMemberId == null) {
			quyRechargeOrder.setWechatMemberId(wechatMemberId);
		}
		quyRechargeOrder.setWechatMemberId(wechatMemberId);
		if(status == null){
			quyRechargeOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY, DictAttribute.RECHARGE_ORDER_STATUS, "")));
		}else{
			quyRechargeOrder.setStatus(status);
		}
		
		RechargeOrder payRechargeOrder = RechargeOrder.dao.findByModel(quyRechargeOrder);
		if(payRechargeOrder == null){
			throw new ApiBusinessException(RenderResultCode.BUSINESS_302);
		}
		
		//超时
		/*
		if(DateUtil.between(payRechargeOrder.getCreateTime(), new java.util.Date(), DateUnit.SECOND) > CommonAttribute.RECHARGE_EXPIRE_TIME){
			throw new ApiBusinessException(RenderResultCode.BUSINESS_314);
		}
		*/
		
		//游戏币
		Long EType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_GAME, DictAttribute.ACCOUNT_ACC_UNIT_TYPE, "1"));
		//代理
		Long DType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_REWARD, DictAttribute.ACCOUNT_ACC_UNIT_TYPE, "2"));
		
		Db.tx(() -> {
			if(EType == payRechargeOrder.getTransType()) {
				//根据充值金额来赠送金币配置
				RechargeRule tmpRechargeRule = RechargeRule.dao.findById(payRechargeOrder.getRechargeRuleId());
				if(tmpRechargeRule == null) {
					throw new ApiBusinessException(RenderResultCode.BUSINESS_305);
				}
				payRechargeOrder.setCommissionStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_COMMISSION_STATUS2, DictAttribute.RECHARGE_COMMISSION_STATUS, "2")));
				accountService.rechargeGameCurrency(payRechargeOrder.getWechatMemberId(), payRechargeOrder.getTransAmt(), payRechargeOrder.getTransAfterAmt());
				AsyncManager.me().execute(AsyncTaskFactory.asyncTaskAllocationCommission());
			} else if (DType == payRechargeOrder.getTransType()) {
				//根据升级金额查询对应配置
				RecommendRule tmpRecommendRule = RecommendRule.dao.findById(payRechargeOrder.getRechargeRuleId());
				if(tmpRecommendRule == null) {
					throw new ApiBusinessException(RenderResultCode.BUSINESS_303);
				}
				payRechargeOrder.setCommissionStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_COMMISSION_STATUS0, DictAttribute.RECHARGE_COMMISSION_STATUS, "0")));
				accountService.rechargeUpgradeMember(payRechargeOrder.getWechatMemberId(), payRechargeOrder.getTransAmt());
				wechatMemberService.upgradeWechatMemberProfit(payRechargeOrder.getWechatMemberId(), tmpRecommendRule);
			}
			payRechargeOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_FINISH, DictAttribute.RECHARGE_ORDER_STATUS, "3")));
			payRechargeOrder.setUpdateTime(new java.util.Date());
			payRechargeOrder.update();
			return true;
		});
		
	}
	
}