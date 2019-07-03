package cn.joytur.common.mvc.async;

import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Duang;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;

import cn.hutool.core.util.NumberUtil;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.order.entites.RechargeOrder;
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.product.entities.ExtensionRule;
import cn.joytur.modules.wechat.entities.WechatMemberProfit;
import cn.joytur.modules.wechat.entities.WechatMemberRecommend;

/**
 * 异步工厂（产生任务用）
 */
public class AsyncTaskFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskFactory.class);

	/**
	 * 佣金派发
	 * @param wechatMemberId
	 * @return
	 */
	public static TimerTask asyncTaskAllocationCommission() {
		return new TimerTask() {
			@Override
			public void run() {
				//查询可返佣的订单
				Long commissionStatus2 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_COMMISSION_STATUS2, DictAttribute.RECHARGE_COMMISSION_STATUS, "2"));
				List<RechargeOrder> rechargeOrderList = RechargeOrder.dao.findList(new RechargeOrder().setCommissionStatus(commissionStatus2));
				if(rechargeOrderList == null || rechargeOrderList.isEmpty()){
					LOGGER.info("未有返佣订单,停止任务");
					return;
				}
				
				Db.tx(() -> {
					for (RechargeOrder rechargeOrder : rechargeOrderList) {
						//查询当前下单用户对应推荐用户
						WechatMemberRecommend tmpWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(rechargeOrder.getWechatMemberId()));
						/*
						//查询推荐用户对应分成方案
						WechatMemberProfit tmpWechatMemberProfit = null;
						if(tmpWechatMemberRecommend != null && StrKit.notBlank(tmpWechatMemberRecommend.getReceiveWechatMemberId())) {
							tmpWechatMemberProfit = WechatMemberProfit.dao.findByModel(new WechatMemberProfit().setWechatMemberId(tmpWechatMemberRecommend.getReceiveWechatMemberId()));
						}
						
						// 获取当前订单用户的推荐用户
						//tmpWechatMemberProfit = WechatMemberProfit.dao.findReceiveWechatMemberProfit(rechargeOrder.getWechatMemberId());
						// 进行返佣
						if (tmpWechatMemberProfit == null) {
							Long commissionStatus0 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_COMMISSION_STATUS0,DictAttribute.RECHARGE_COMMISSION_STATUS, "0"));
							// 如果没有推荐用户则停止
							RechargeOrder updateRechargeOrder = new RechargeOrder();
							updateRechargeOrder.setCommissionStatus(commissionStatus0);
							updateRechargeOrder.setUpdateTime(new java.util.Date());
							RechargeOrder whereRechargeOrder = new RechargeOrder();
							whereRechargeOrder.setId(rechargeOrder.getId());
							RechargeOrder.dao.updateModelByModel(updateRechargeOrder, whereRechargeOrder);

							LOGGER.info("WechatMemberId={},当前用户未有推荐用户返佣结束", rechargeOrder.getWechatMemberId());
							//continue;
						}
						*/
						// 分级返佣
						treeCommission(rechargeOrder, tmpWechatMemberRecommend, 1);
						
						//更新返佣为已完成
						Long commissionStatus3 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_COMMISSION_STATUS3,DictAttribute.RECHARGE_COMMISSION_STATUS, "3"));
						RechargeOrder updateRechargeOrder = new RechargeOrder();
						updateRechargeOrder.setCommissionStatus(commissionStatus3);
						updateRechargeOrder.setUpdateTime(new java.util.Date());
						RechargeOrder whereRechargeOrder = new RechargeOrder();
						whereRechargeOrder.setId(rechargeOrder.getId());
						RechargeOrder.dao.updateModelByModel(updateRechargeOrder, whereRechargeOrder);
						
					}
					return true;
				});
				
				
			}
			
			/**
			 * 分层分级返佣
			 * @param wechatMemberProfit
			 * @param rechargeOrder
			 * @param wechatMemberId
			 * @param level
			 */
			private void treeCommission(RechargeOrder rechargeOrder, WechatMemberRecommend tmpWechatMemberRecommend, Integer level){
				
				if(level > 3){
					LOGGER.info("WechatMemberId={},返佣用户返佣已经达到顶级返佣结束", tmpWechatMemberRecommend.getWechatMemberId());
					return;
				}
				
				//查询上级用户
				WechatMemberRecommend topWechatMemberRecommend = null;
				//查询推荐用户对应分成方案
				WechatMemberProfit tmpWechatMemberProfit = null;
				if(tmpWechatMemberRecommend != null && StrKit.notBlank(tmpWechatMemberRecommend.getReceiveWechatMemberId())) {
					tmpWechatMemberProfit = WechatMemberProfit.dao.findByModel(new WechatMemberProfit().setWechatMemberId(tmpWechatMemberRecommend.getReceiveWechatMemberId()));
					//查询推荐人对应的推荐人
					topWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(tmpWechatMemberRecommend.getReceiveWechatMemberId()));
				}
				//返佣操作
				if(tmpWechatMemberProfit != null) {
					
					Double perScale = 0.0d;
					String eleText = null;
					switch (level) {
					case 1:
						perScale = tmpWechatMemberProfit.getRecVal1();
						eleText = DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND1;
						break;
					case 2:
						perScale = tmpWechatMemberProfit.getRecVal2();
						eleText = DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND2;
						break;
					case 3:
						perScale = tmpWechatMemberProfit.getRecVal3();
						eleText = DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND3;
						break;
					default:
						break;
					}
					
					//如果当前用户未有返佣计划则取消
					if(perScale != null && perScale > 0.0){
						Double transAmt = rechargeOrder.getTransAmt();
						
						//Double per = 100d;
						//返佣小数百分比
						//Double decScale = NumberUtil.div(perScale, per, 2);
						//计算佣金
						Double commissionCNY = NumberUtil.mul(transAmt, perScale);
						
						JSONObject tagJSON = new JSONObject();
						tagJSON.put("orderNo", rechargeOrder.getOrderNo());
						tagJSON.put("payAmt", rechargeOrder.getTransAmt());
						tagJSON.put("payScale", perScale);
						
						//进行返佣查询佣金比例查询层级
						Duang.duang(AccountService.class).commissionCash(tmpWechatMemberProfit.getWechatMemberId(), commissionCNY, tagJSON.toJSONString(), eleText);
					}
				}
				
				if(topWechatMemberRecommend != null) {
					treeCommission(rechargeOrder, topWechatMemberRecommend, level + 1);
				}
				
				//再次查询推荐人的推荐人
				/*
				WechatMemberProfit topWechatMemberProfit = WechatMemberProfit.dao.findReceiveWechatMemberProfit(wechatMemberProfit.getWechatMemberId());
				if(tmpWechatMemberProfit != null){
				}else{
					LOGGER.info("WechatMemberId={},返佣用户未有推荐用户返佣结束", wechatMemberProfit.getWechatMemberId());
				}
				*/
			}
			
			
		};
	}
	
	/**
	 * 处理新注册用户
	 * @param wechatMemberId
	 * @return
	 */
	public static TimerTask asyncNewMemberActiveCommission(String wechatMemberId) {
		return new TimerTask() {
			@Override
			public void run() {
				Long commissionStatus1 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.EXTENSION_TYPE1, DictAttribute.EXTENSION_TYPE, "1"));
				
				ExtensionRule quyExtensionRule = new ExtensionRule();
				quyExtensionRule.setExtensionType(commissionStatus1);
				quyExtensionRule.setStatus(1L);
				ExtensionRule tmpExtensionRule = ExtensionRule.dao.findByModel(quyExtensionRule);
				
				if(tmpExtensionRule == null || tmpExtensionRule.getRecAmount() <= 0) {
					LOGGER.info("WechatMemberId={},新注册用户未有配置活动", wechatMemberId);
					return;
				}
				
				Db.tx(() -> {
					Duang.duang(AccountService.class).marketingCommissionGameCurrency(wechatMemberId, tmpExtensionRule.getRecAmount());
					return true;
				});
			}
			
		};
	}
	
	/**
	 * 处理推荐用户
	 * @param wechatMemberId
	 * @return
	 */
	public static TimerTask asyncRecommendActiveCommission(String wechatMemberId) {
		return new TimerTask() {
			@Override
			public void run() {
				Long commissionStatus2 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.EXTENSION_TYPE2, DictAttribute.EXTENSION_TYPE, "2"));
				
				ExtensionRule quyExtensionRule = new ExtensionRule();
				quyExtensionRule.setExtensionType(commissionStatus2);
				quyExtensionRule.setStatus(1L);
				ExtensionRule tmpExtensionRule = ExtensionRule.dao.findByModel(quyExtensionRule);
				
				if(tmpExtensionRule == null || tmpExtensionRule.getRecAmount() <= 0) {
					LOGGER.info("WechatMemberId={},推荐用户未有配置活动", wechatMemberId);
					return;
				}
				
				
				Db.tx(() -> {
					Duang.duang(AccountService.class).marketingCommissionGameCurrency(wechatMemberId, tmpExtensionRule.getRecAmount());
					return true;
				});
				
			}
		};
	}

}
