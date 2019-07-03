package cn.joytur.modules.order.service;

import cn.hutool.core.util.NumberUtil;
import cn.joytur.common.exception.ApiBusinessException;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.order.entites.AccountFunds;

/**
 * 账户管理service
 * @author xuhang
 *
 */
public class AccountService {
	
	/**
	 * 充值游戏币
	 * @param wechatMemberId 用户id
	 * @param payCNY 支付金额
	 * @param addGameCoin 添加游戏币数
	 */
	public void rechargeGameCurrency(String wechatMemberId, Double payCNY, Double addGameCoin) {
		Account mgAccount = Account.dao.getGameAccount(wechatMemberId);
		//Account msAccount = Account.dao.getSettAccount(wechatMemberId);
		
		Account igAccount = Account.dao.getInsideGameAccount();
		Account isAccount = Account.dao.getInsideSettAccount();
		
		//增加游戏币-用户
		Double befMGAmt = new Double(mgAccount.getAvbAmt());
		Double aftMGAmt = NumberUtil.add(befMGAmt, addGameCoin);
		Long lastMGVersion = new Long(mgAccount.getVersion() + 1l);
		
		Account updateMgAccount = new Account();
		updateMgAccount.setAvbAmt(aftMGAmt);
		updateMgAccount.setTotalAmt(NumberUtil.add(mgAccount.getFrzAmt(), aftMGAmt));
		updateMgAccount.setUpdateTime(new java.util.Date());
		updateMgAccount.setVersion(lastMGVersion);
		
		//用户游戏币流水
		AccountFunds mgAccountFunds = new AccountFunds();
		mgAccountFunds.setId(JoyIdUtil.simpleUUID());
		mgAccountFunds.setWechatMemberId(wechatMemberId);
		mgAccountFunds.setFromAccountId(igAccount.getId());
		mgAccountFunds.setToAccountId(mgAccount.getId());
		mgAccountFunds.setAccType(mgAccount.getAccType());
		mgAccountFunds.setTransAmt(addGameCoin);
		mgAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		mgAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECHARGE, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		mgAccountFunds.setBefAmt(befMGAmt);
		mgAccountFunds.setAftAmt(aftMGAmt);
		mgAccountFunds.setDescription(null);
		mgAccountFunds.setCreateTime(new java.util.Date());
		mgAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户游戏账户
		Account whereMgAccount = new Account();
		whereMgAccount.setId(mgAccount.getId());
		whereMgAccount.setVersion(mgAccount.getVersion());
		int mgCount = Account.dao.updateModelByModel(updateMgAccount, whereMgAccount);
		if(mgCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		mgAccountFunds.save();
		
		
		//减少游戏币-内账
		Double befIGAmt = new Double(igAccount.getAvbAmt());
		Double aftIGAmt = NumberUtil.sub(befIGAmt, addGameCoin);
		Long lastIGVersion = new Long(igAccount.getVersion() + 1l);
		
		Account updateIgAccount = new Account();
		updateIgAccount.setAvbAmt(aftIGAmt);
		updateIgAccount.setTotalAmt(NumberUtil.add(igAccount.getFrzAmt(), aftIGAmt));
		updateIgAccount.setUpdateTime(new java.util.Date());
		updateIgAccount.setVersion(lastIGVersion);
		
		//内账游戏币流水
		AccountFunds igAccountFunds = new AccountFunds();
		igAccountFunds.setId(JoyIdUtil.simpleUUID());
		igAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		igAccountFunds.setFromAccountId(igAccount.getId());
		igAccountFunds.setToAccountId(mgAccount.getId());
		igAccountFunds.setAccType(igAccount.getAccType());
		igAccountFunds.setTransAmt(addGameCoin);
		igAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "2")));
		igAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECHARGE, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		igAccountFunds.setBefAmt(befIGAmt);
		igAccountFunds.setAftAmt(aftIGAmt);
		igAccountFunds.setDescription(null);
		igAccountFunds.setCreateTime(new java.util.Date());
		igAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部游戏账户
		Account whereIgAccount = new Account();
		whereIgAccount.setId(igAccount.getId());
		//whereIgAccount.setVersion(mgAccount.getVersion()); //暂时不做锁
		int igCount = Account.dao.updateModelByModel(updateIgAccount, whereIgAccount);
		if(igCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		igAccountFunds.save();
		
		//增加结算账户-内部
		Double befISAmt = new Double(isAccount.getAvbAmt());
		Double aftISAmt = NumberUtil.add(befISAmt, payCNY);
		Long lastISVersion = new Long(isAccount.getVersion() + 1l);
		
		Account updateIsAccount = new Account();
		updateIsAccount.setAvbAmt(aftISAmt);
		updateIsAccount.setTotalAmt(NumberUtil.add(isAccount.getFrzAmt(), aftISAmt));
		updateIsAccount.setUpdateTime(new java.util.Date());
		updateIsAccount.setVersion(lastISVersion);
		
		//内部结算账户流水
		AccountFunds isAccountFunds = new AccountFunds();
		isAccountFunds.setId(JoyIdUtil.simpleUUID());
		isAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		isAccountFunds.setFromAccountId(null);
		isAccountFunds.setToAccountId(isAccount.getId());
		isAccountFunds.setAccType(isAccount.getAccType());
		isAccountFunds.setTransAmt(payCNY);
		isAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		isAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECHARGE, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		isAccountFunds.setBefAmt(befISAmt);
		isAccountFunds.setAftAmt(aftISAmt);
		isAccountFunds.setDescription(null);
		isAccountFunds.setCreateTime(new java.util.Date());
		isAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部结算账户
		Account whereIsAccount = new Account();
		whereIsAccount.setId(isAccount.getId());
		//whereIsAccount.setVersion(isAccount.getVersion());
		int isCount = Account.dao.updateModelByModel(updateIsAccount, whereIsAccount);
		if(isCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		isAccountFunds.save();
	}
	
	/**
	 * 退游戏币
	 * @param wechatMemberId
	 * @param addGameCoin
	 */
	public void refundGameCurrency(String wechatMemberId, Double addGameCoin) {
		Account mgAccount = Account.dao.getGameAccount(wechatMemberId);
		//Account msAccount = Account.dao.getSettAccount(wechatMemberId);
		
		Account igAccount = Account.dao.getInsideGameAccount();
		
		//增加游戏币-用户
		Double befMGAmt = new Double(mgAccount.getAvbAmt());
		Double aftMGAmt = NumberUtil.add(befMGAmt, addGameCoin);
		Long lastMGVersion = new Long(mgAccount.getVersion() + 1l);
		
		Account updateMgAccount = new Account();
		updateMgAccount.setAvbAmt(aftMGAmt);
		updateMgAccount.setTotalAmt(NumberUtil.add(mgAccount.getFrzAmt(), aftMGAmt));
		updateMgAccount.setUpdateTime(new java.util.Date());
		updateMgAccount.setVersion(lastMGVersion);
		
		//用户游戏币流水
		AccountFunds mgAccountFunds = new AccountFunds();
		mgAccountFunds.setId(JoyIdUtil.simpleUUID());
		mgAccountFunds.setWechatMemberId(wechatMemberId);
		mgAccountFunds.setFromAccountId(igAccount.getId());
		mgAccountFunds.setToAccountId(mgAccount.getId());
		mgAccountFunds.setAccType(mgAccount.getAccType());
		mgAccountFunds.setTransAmt(addGameCoin);
		mgAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		mgAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_REFUND, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		mgAccountFunds.setBefAmt(befMGAmt);
		mgAccountFunds.setAftAmt(aftMGAmt);
		mgAccountFunds.setDescription(null);
		mgAccountFunds.setCreateTime(new java.util.Date());
		mgAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户游戏账户
		Account whereMgAccount = new Account();
		whereMgAccount.setId(mgAccount.getId());
		whereMgAccount.setVersion(mgAccount.getVersion());
		int mgCount = Account.dao.updateModelByModel(updateMgAccount, whereMgAccount);
		if(mgCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		mgAccountFunds.save();
		
		
		//减少游戏币-内账
		Double befIGAmt = new Double(igAccount.getAvbAmt());
		Double aftIGAmt = NumberUtil.sub(befIGAmt, addGameCoin);
		Long lastIGVersion = new Long(igAccount.getVersion() + 1l);
		
		Account updateIgAccount = new Account();
		updateIgAccount.setAvbAmt(aftIGAmt);
		updateIgAccount.setTotalAmt(NumberUtil.add(igAccount.getFrzAmt(), aftIGAmt));
		updateIgAccount.setUpdateTime(new java.util.Date());
		updateIgAccount.setVersion(lastIGVersion);
		
		//内账游戏币流水
		AccountFunds igAccountFunds = new AccountFunds();
		igAccountFunds.setId(JoyIdUtil.simpleUUID());
		igAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		igAccountFunds.setFromAccountId(igAccount.getId());
		igAccountFunds.setToAccountId(mgAccount.getId());
		igAccountFunds.setAccType(igAccount.getAccType());
		igAccountFunds.setTransAmt(addGameCoin);
		igAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "2")));
		igAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_REFUND, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		igAccountFunds.setBefAmt(befIGAmt);
		igAccountFunds.setAftAmt(aftIGAmt);
		igAccountFunds.setDescription(null);
		igAccountFunds.setCreateTime(new java.util.Date());
		igAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部游戏账户
		Account whereIgAccount = new Account();
		whereIgAccount.setId(igAccount.getId());
		//whereIgAccount.setVersion(mgAccount.getVersion()); //暂时不做锁
		int igCount = Account.dao.updateModelByModel(updateIgAccount, whereIgAccount);
		if(igCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		igAccountFunds.save();
		
	}
	
	/**
	 * 充值升级会员
	 * @param wechatMemberId
	 * @param payCNY
	 */
	public void rechargeUpgradeMember(String wechatMemberId, Double payCNY) {
		Account isAccount = Account.dao.getInsideSettAccount();
		
		//增加结算账户-内部
		Double befISAmt = new Double(isAccount.getAvbAmt());
		Double aftISAmt = NumberUtil.add(befISAmt, payCNY);
		Long lastISVersion = new Long(isAccount.getVersion() + 1l);
		
		Account updateIsAccount = new Account();
		updateIsAccount.setAvbAmt(aftISAmt);
		updateIsAccount.setTotalAmt(NumberUtil.add(isAccount.getFrzAmt(), aftISAmt));
		updateIsAccount.setUpdateTime(new java.util.Date());
		updateIsAccount.setVersion(lastISVersion);
		
		//内部结算账户流水
		AccountFunds isAccountFunds = new AccountFunds();
		isAccountFunds.setId(JoyIdUtil.simpleUUID());
		isAccountFunds.setWechatMemberId(wechatMemberId);
		isAccountFunds.setFromAccountId(null);
		isAccountFunds.setToAccountId(isAccount.getId());
		isAccountFunds.setAccType(isAccount.getAccType());
		isAccountFunds.setTransAmt(payCNY);
		isAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		isAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECHARGE, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		isAccountFunds.setBefAmt(befISAmt);
		isAccountFunds.setAftAmt(aftISAmt);
		isAccountFunds.setDescription(null);
		isAccountFunds.setCreateTime(new java.util.Date());
		isAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部结算账户
		Account whereIsAccount = new Account();
		whereIsAccount.setId(isAccount.getId());
		//whereIsAccount.setVersion(isAccount.getVersion());
		int isCount = Account.dao.updateModelByModel(updateIsAccount, whereIsAccount);
		if(isCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		isAccountFunds.save();
	}
	
	/**
	 * 消耗游戏币
	 * @param wechatMemberId
	 * @param subGameCurrency
	 */
	public void depleteGameCurrency(String wechatMemberId, Double subGameCoin) {
		Account mgAccount = Account.dao.getGameAccount(wechatMemberId);
		Account igAccount = Account.dao.getInsideGameAccount();
		
		//消耗游戏币-用户
		Double befMGAmt = new Double(mgAccount.getAvbAmt());
		Double aftMGAmt = NumberUtil.sub(befMGAmt, subGameCoin);
		Long lastMGVersion = new Long(mgAccount.getVersion());
		
		Account updateMgAccount = new Account();
		updateMgAccount.setAvbAmt(aftMGAmt);
		updateMgAccount.setTotalAmt(NumberUtil.add(mgAccount.getFrzAmt(), aftMGAmt));
		updateMgAccount.setUpdateTime(new java.util.Date());
		updateMgAccount.setVersion(lastMGVersion + 1l);
		
		//用户游戏币流水
		AccountFunds mgAccountFunds = new AccountFunds();
		mgAccountFunds.setId(JoyIdUtil.simpleUUID());
		mgAccountFunds.setWechatMemberId(wechatMemberId);
		mgAccountFunds.setFromAccountId(mgAccount.getId());
		mgAccountFunds.setToAccountId(igAccount.getId());
		mgAccountFunds.setAccType(mgAccount.getAccType());
		mgAccountFunds.setTransAmt(subGameCoin);
		mgAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		mgAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_CONSUME, DictAttribute.ACCOUNT_FUNDS_ELE, "2")));
		mgAccountFunds.setBefAmt(befMGAmt);
		mgAccountFunds.setAftAmt(aftMGAmt);
		mgAccountFunds.setDescription(null);
		mgAccountFunds.setCreateTime(new java.util.Date());
		mgAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户游戏账户
		Account whereMgAccount = new Account();
		whereMgAccount.setId(mgAccount.getId());
		whereMgAccount.setVersion(lastMGVersion);
		int mgCount = Account.dao.updateModelByModel(updateMgAccount, whereMgAccount);
		if(mgCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		mgAccountFunds.save();
		
		
		//增加游戏币-内账
		Double befIGAmt = new Double(igAccount.getAvbAmt());
		Double aftIGAmt = NumberUtil.add(befIGAmt, subGameCoin);
		Long lastIGVersion = new Long(igAccount.getVersion());
		
		Account updateIgAccount = new Account();
		updateIgAccount.setAvbAmt(aftIGAmt);
		updateIgAccount.setTotalAmt(NumberUtil.add(igAccount.getFrzAmt(), aftIGAmt));
		updateIgAccount.setUpdateTime(new java.util.Date());
		updateIgAccount.setVersion(lastIGVersion + 1l);
		
		//内账游戏币流水
		AccountFunds igAccountFunds = new AccountFunds();
		igAccountFunds.setId(JoyIdUtil.simpleUUID());
		igAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		igAccountFunds.setFromAccountId(mgAccount.getId());
		igAccountFunds.setToAccountId(igAccount.getId());
		igAccountFunds.setAccType(igAccount.getAccType());
		igAccountFunds.setTransAmt(subGameCoin);
		igAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "2")));
		igAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_CONSUME, DictAttribute.ACCOUNT_FUNDS_ELE, "2")));
		igAccountFunds.setBefAmt(befIGAmt);
		igAccountFunds.setAftAmt(aftIGAmt);
		igAccountFunds.setDescription(null);
		igAccountFunds.setCreateTime(new java.util.Date());
		igAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部游戏账户
		Account whereIgAccount = new Account();
		whereIgAccount.setId(igAccount.getId());
		//whereIgAccount.setVersion(lastIGVersion); //暂时不做锁
		int igCount = Account.dao.updateModelByModel(updateIgAccount, whereIgAccount);
		if(igCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		igAccountFunds.save();
		
	}
	
	/**
	 * 会员推广返点
	 * @param wechatMemberId
	 * @param commissionCNY
	 */
	public void commissionCash(String wechatMemberId, Double commissionCNY, String tag, String eleText) {
		Account msAccount = Account.dao.getSettAccount(wechatMemberId);
		Account isAccount = Account.dao.getInsideSettAccount();
		
		//减少结算账户-内部
		Double befISAmt = new Double(isAccount.getAvbAmt());
		Double aftISAmt = NumberUtil.sub(befISAmt, commissionCNY);
		Long lastISVersion = new Long(isAccount.getVersion() + 1l);
		
		Account updateIsAccount = new Account();
		updateIsAccount.setAvbAmt(aftISAmt);
		updateIsAccount.setTotalAmt(NumberUtil.add(isAccount.getFrzAmt(), aftISAmt));
		updateIsAccount.setUpdateTime(new java.util.Date());
		updateIsAccount.setVersion(lastISVersion);
		
		//内部结算账户流水
		AccountFunds isAccountFunds = new AccountFunds();
		isAccountFunds.setId(JoyIdUtil.simpleUUID());
		isAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		isAccountFunds.setFromAccountId(isAccount.getId());
		isAccountFunds.setToAccountId(msAccount.getId());
		isAccountFunds.setAccType(isAccount.getAccType());
		isAccountFunds.setTransAmt(commissionCNY);
		isAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		isAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(eleText, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		isAccountFunds.setBefAmt(befISAmt);
		isAccountFunds.setAftAmt(aftISAmt);
		isAccountFunds.setTag(tag);
		isAccountFunds.setDescription(null);
		isAccountFunds.setCreateTime(new java.util.Date());
		isAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部结算账户
		Account whereIsAccount = new Account();
		whereIsAccount.setId(isAccount.getId());
		//whereIsAccount.setVersion(isAccount.getVersion());
		int isCount = Account.dao.updateModelByModel(updateIsAccount, whereIsAccount);
		if(isCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		isAccountFunds.save();
		
		//增加结算账户-用户
		Double befMSAmt = new Double(msAccount.getAvbAmt());
		Double aftMSAmt = NumberUtil.add(befMSAmt, commissionCNY);
		Long lastMSVersion = new Long(msAccount.getVersion() + 1l);
		
		Account updateMsAccount = new Account();
		updateMsAccount.setAvbAmt(aftMSAmt);
		updateMsAccount.setTotalAmt(NumberUtil.add(msAccount.getFrzAmt(), aftMSAmt));
		updateMsAccount.setUpdateTime(new java.util.Date());
		updateMsAccount.setVersion(lastMSVersion);
		
		//用户结算账户流水
		AccountFunds msAccountFunds = new AccountFunds();
		msAccountFunds.setId(JoyIdUtil.simpleUUID());
		msAccountFunds.setWechatMemberId(wechatMemberId);
		msAccountFunds.setFromAccountId(isAccount.getId());
		msAccountFunds.setToAccountId(msAccount.getId());
		msAccountFunds.setAccType(msAccount.getAccType());
		msAccountFunds.setTransAmt(commissionCNY);
		msAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		msAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(eleText, DictAttribute.ACCOUNT_FUNDS_ELE, "5")));
		msAccountFunds.setBefAmt(befMSAmt);
		msAccountFunds.setAftAmt(aftMSAmt);
		msAccountFunds.setTag(tag);
		msAccountFunds.setDescription(null);
		msAccountFunds.setCreateTime(new java.util.Date());
		msAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户结算账户
		Account whereMsAccount = new Account();
		whereMsAccount.setId(msAccount.getId());
		whereMsAccount.setVersion(msAccount.getVersion());
		int msCount = Account.dao.updateModelByModel(updateMsAccount, whereMsAccount);
		if(msCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		msAccountFunds.save();
	}
	
	/**
	 * 提现冻结
	 * @param wechatMemberId
	 * @param freezeCNY
	 */
	public void freezeWithdrawalCash(String wechatMemberId, Double freezeCNY) {
		Account msAccount = Account.dao.getSettAccount(wechatMemberId);
		
		//减少结算账户-用户
		Double befMSAmt = new Double(msAccount.getAvbAmt());
		Double aftMSAmt = NumberUtil.sub(befMSAmt, freezeCNY);
		Long lastMSVersion = new Long(msAccount.getVersion() + 1l);
		
		Account updateMsAccount = new Account();
		updateMsAccount.setAvbAmt(aftMSAmt);
		updateMsAccount.setFrzAmt(freezeCNY);
		updateMsAccount.setTotalAmt(NumberUtil.add(msAccount.getFrzAmt(), aftMSAmt));
		updateMsAccount.setUpdateTime(new java.util.Date());
		updateMsAccount.setVersion(lastMSVersion);
		
		//结算结算账户流水
		AccountFunds msAccountFunds = new AccountFunds();
		msAccountFunds.setId(JoyIdUtil.simpleUUID());
		msAccountFunds.setWechatMemberId(wechatMemberId);
		msAccountFunds.setFromAccountId(msAccount.getId());
		msAccountFunds.setToAccountId(msAccount.getId());
		msAccountFunds.setAccType(msAccount.getAccType());
		msAccountFunds.setTransAmt(freezeCNY);
		msAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "2")));
		msAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_WITHFRE, DictAttribute.ACCOUNT_FUNDS_ELE, "5")));
		msAccountFunds.setBefAmt(befMSAmt);
		msAccountFunds.setAftAmt(aftMSAmt);
		msAccountFunds.setDescription(null);
		msAccountFunds.setCreateTime(new java.util.Date());
		msAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户结算账户
		Account whereMsAccount = new Account();
		whereMsAccount.setId(msAccount.getId());
		whereMsAccount.setVersion(msAccount.getVersion());
		int msCount = Account.dao.updateModelByModel(updateMsAccount, whereMsAccount);
		if(msCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		msAccountFunds.save();
		
	}
	
	/**
	 * 提现解冻减扣
	 * @param wechatMemberId
	 * @param withdrawalCNY
	 */
	public void withdrawalCash(String wechatMemberId, Double withdrawalCNY) {
		Account msAccount = Account.dao.getSettAccount(wechatMemberId);
		Account isAccount = Account.dao.getInsideSettAccount();
		
		//减扣结算账户-内部
		Double befMSAmt = new Double(msAccount.getFrzAmt());
		Double aftMSAmt = NumberUtil.sub(befMSAmt, withdrawalCNY);
		Long lastMSVersion = new Long(msAccount.getVersion() + 1l);
		
		Account updateMsAccount = new Account();
		updateMsAccount.setFrzAmt(aftMSAmt);
		updateMsAccount.setTotalAmt(NumberUtil.add(msAccount.getAvbAmt(), aftMSAmt));
		updateMsAccount.setUpdateTime(new java.util.Date());
		updateMsAccount.setVersion(lastMSVersion);
		
		//用户结算账户流水
		AccountFunds msAccountFunds = new AccountFunds();
		msAccountFunds.setId(JoyIdUtil.simpleUUID());
		msAccountFunds.setWechatMemberId(wechatMemberId);
		msAccountFunds.setFromAccountId(msAccount.getId());
		msAccountFunds.setToAccountId(isAccount.getId());
		msAccountFunds.setAccType(msAccount.getAccType());
		msAccountFunds.setTransAmt(withdrawalCNY);
		msAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		msAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_WITHUNFRE, DictAttribute.ACCOUNT_FUNDS_ELE, "5")));
		msAccountFunds.setBefAmt(msAccount.getTotalAmt());
		msAccountFunds.setAftAmt(updateMsAccount.getTotalAmt());
		msAccountFunds.setDescription(null);
		msAccountFunds.setCreateTime(new java.util.Date());
		msAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户结算账户
		Account whereMsAccount = new Account();
		whereMsAccount.setId(msAccount.getId());
		whereMsAccount.setVersion(msAccount.getVersion());
		int msCount = Account.dao.updateModelByModel(updateMsAccount, whereMsAccount);
		if(msCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		msAccountFunds.save();
		

		//增加结算账户-内部
		Double befISAmt = new Double(isAccount.getAvbAmt());
		Double aftISAmt = NumberUtil.add(befISAmt, withdrawalCNY);
		Long lastISVersion = new Long(isAccount.getVersion() + 1l);
		
		Account updateIsAccount = new Account();
		updateIsAccount.setFrzAmt(aftISAmt);
		updateIsAccount.setTotalAmt(NumberUtil.add(isAccount.getFrzAmt(), isAccount.getAvbAmt()));
		updateIsAccount.setUpdateTime(new java.util.Date());
		updateIsAccount.setVersion(lastISVersion);
		
		//内部结算账户流水新增
		AccountFunds isAccountFunds = new AccountFunds();
		isAccountFunds.setId(JoyIdUtil.simpleUUID());
		isAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		isAccountFunds.setFromAccountId(msAccount.getId());
		isAccountFunds.setToAccountId(isAccount.getId());
		isAccountFunds.setAccType(isAccount.getAccType());
		isAccountFunds.setTransAmt(withdrawalCNY);
		isAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		isAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_WITHUNFRE, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		isAccountFunds.setBefAmt(befISAmt);
		isAccountFunds.setAftAmt(aftISAmt);
		isAccountFunds.setDescription(null);
		isAccountFunds.setCreateTime(new java.util.Date());
		isAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部结算账户
		Account whereIsAccount = new Account();
		whereIsAccount.setId(isAccount.getId());
		//whereIsAccount.setVersion(isAccount.getVersion());
		int isCount = Account.dao.updateModelByModel(updateIsAccount, whereIsAccount);
		if(isCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		isAccountFunds.save();
	}
	
	/**
	 * 营销返佣
	 * @param wechatMemberId
	 * @param addGameCoin
	 */
	public void marketingCommissionGameCurrency(String wechatMemberId, Double addGameCoin) {
		Account mgAccount = Account.dao.getGameAccount(wechatMemberId);
		Account igAccount = Account.dao.getInsideGameAccount();
		
		//增加游戏币-用户
		Double befMGAmt = new Double(mgAccount.getAvbAmt());
		Double aftMGAmt = NumberUtil.add(befMGAmt, addGameCoin);
		Long lastMGVersion = new Long(mgAccount.getVersion() + 1l);
		
		Account updateMgAccount = new Account();
		updateMgAccount.setAvbAmt(aftMGAmt);
		updateMgAccount.setTotalAmt(NumberUtil.add(mgAccount.getFrzAmt(), aftMGAmt));
		updateMgAccount.setUpdateTime(new java.util.Date());
		updateMgAccount.setVersion(lastMGVersion);
		
		//用户游戏币流水
		AccountFunds mgAccountFunds = new AccountFunds();
		mgAccountFunds.setId(JoyIdUtil.simpleUUID());
		mgAccountFunds.setWechatMemberId(wechatMemberId);
		mgAccountFunds.setFromAccountId(igAccount.getId());
		mgAccountFunds.setToAccountId(mgAccount.getId());
		mgAccountFunds.setAccType(mgAccount.getAccType());
		mgAccountFunds.setTransAmt(addGameCoin);
		mgAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		mgAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_MARKETING, DictAttribute.ACCOUNT_FUNDS_ELE, "7")));
		mgAccountFunds.setBefAmt(befMGAmt);
		mgAccountFunds.setAftAmt(aftMGAmt);
		mgAccountFunds.setDescription(null);
		mgAccountFunds.setCreateTime(new java.util.Date());
		mgAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户游戏账户
		Account whereMgAccount = new Account();
		whereMgAccount.setId(mgAccount.getId());
		whereMgAccount.setVersion(mgAccount.getVersion());
		int mgCount = Account.dao.updateModelByModel(updateMgAccount, whereMgAccount);
		if(mgCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		mgAccountFunds.save();
		
		
		//减少游戏币-内账
		Double befIGAmt = new Double(igAccount.getAvbAmt());
		Double aftIGAmt = NumberUtil.sub(befIGAmt, addGameCoin);
		Long lastIGVersion = new Long(igAccount.getVersion() + 1l);
		
		Account updateIgAccount = new Account();
		updateIgAccount.setAvbAmt(aftIGAmt);
		updateIgAccount.setTotalAmt(NumberUtil.add(igAccount.getFrzAmt(), aftIGAmt));
		updateIgAccount.setUpdateTime(new java.util.Date());
		updateIgAccount.setVersion(lastIGVersion);
		
		//内账游戏币流水
		AccountFunds igAccountFunds = new AccountFunds();
		igAccountFunds.setId(JoyIdUtil.simpleUUID());
		igAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		igAccountFunds.setFromAccountId(igAccount.getId());
		igAccountFunds.setToAccountId(mgAccount.getId());
		igAccountFunds.setAccType(igAccount.getAccType());
		igAccountFunds.setTransAmt(addGameCoin);
		igAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "2")));
		igAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_MARKETING, DictAttribute.ACCOUNT_FUNDS_ELE, "7")));
		igAccountFunds.setBefAmt(befIGAmt);
		igAccountFunds.setAftAmt(aftIGAmt);
		igAccountFunds.setDescription(null);
		igAccountFunds.setCreateTime(new java.util.Date());
		igAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部游戏账户
		Account whereIgAccount = new Account();
		whereIgAccount.setId(igAccount.getId());
		//whereIgAccount.setVersion(mgAccount.getVersion()); //暂时不做锁
		int igCount = Account.dao.updateModelByModel(updateIgAccount, whereIgAccount);
		if(igCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		igAccountFunds.save();
	}
	
	/**
	 * 兑换游戏币
	 * @param wechatMemberId 用户id
	 * @param payCNY 支付金额
	 * @param addGameCoin 添加游戏币数
	 */
	public void transferGameCurrency(String wechatMemberId, Double payCNY, Double addGameCoin) {
		Account mgAccount = Account.dao.getGameAccount(wechatMemberId);
		Account msAccount = Account.dao.getSettAccount(wechatMemberId);
		
		Account igAccount = Account.dao.getInsideGameAccount();
		Account isAccount = Account.dao.getInsideSettAccount();
		
		//增加游戏币-用户
		Double befMGAmt = new Double(mgAccount.getAvbAmt());
		Double aftMGAmt = NumberUtil.add(befMGAmt, addGameCoin);
		Long lastMGVersion = new Long(mgAccount.getVersion() + 1l);
		
		Account updateMgAccount = new Account();
		updateMgAccount.setAvbAmt(aftMGAmt);
		updateMgAccount.setTotalAmt(NumberUtil.add(mgAccount.getFrzAmt(), aftMGAmt));
		updateMgAccount.setUpdateTime(new java.util.Date());
		updateMgAccount.setVersion(lastMGVersion);
		
		//用户游戏币流水
		AccountFunds mgAccountFunds = new AccountFunds();
		mgAccountFunds.setId(JoyIdUtil.simpleUUID());
		mgAccountFunds.setWechatMemberId(wechatMemberId);
		mgAccountFunds.setFromAccountId(igAccount.getId());
		mgAccountFunds.setToAccountId(mgAccount.getId());
		mgAccountFunds.setAccType(mgAccount.getAccType());
		mgAccountFunds.setTransAmt(addGameCoin);
		mgAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		mgAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_CONVERT, DictAttribute.ACCOUNT_FUNDS_ELE, "9")));
		mgAccountFunds.setBefAmt(befMGAmt);
		mgAccountFunds.setAftAmt(aftMGAmt);
		mgAccountFunds.setDescription(null);
		mgAccountFunds.setCreateTime(new java.util.Date());
		mgAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户游戏账户
		Account whereMgAccount = new Account();
		whereMgAccount.setId(mgAccount.getId());
		whereMgAccount.setVersion(mgAccount.getVersion());
		int mgCount = Account.dao.updateModelByModel(updateMgAccount, whereMgAccount);
		if(mgCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		mgAccountFunds.save();
		
		//减扣结算账户-用户
		Double befMSAmt = new Double(msAccount.getAvbAmt());
		Double aftMSAmt = NumberUtil.sub(befMSAmt, payCNY);
		Long lastMSVersion = new Long(msAccount.getVersion() + 1l);
		
		Account updateMsAccount = new Account();
		updateMsAccount.setAvbAmt(aftMSAmt);
		updateMsAccount.setTotalAmt(NumberUtil.add(msAccount.getAvbAmt(), aftMSAmt));
		updateMsAccount.setUpdateTime(new java.util.Date());
		updateMsAccount.setVersion(lastMSVersion);
		
		//用户结算账户流水
		AccountFunds msAccountFunds = new AccountFunds();
		msAccountFunds.setId(JoyIdUtil.simpleUUID());
		msAccountFunds.setWechatMemberId(wechatMemberId);
		msAccountFunds.setFromAccountId(msAccount.getId());
		msAccountFunds.setToAccountId(isAccount.getId());
		msAccountFunds.setAccType(msAccount.getAccType());
		msAccountFunds.setTransAmt(payCNY);
		msAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		msAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_CONVERT, DictAttribute.ACCOUNT_FUNDS_ELE, "5")));
		msAccountFunds.setBefAmt(msAccount.getTotalAmt());
		msAccountFunds.setAftAmt(updateMsAccount.getTotalAmt());
		msAccountFunds.setDescription(null);
		msAccountFunds.setCreateTime(new java.util.Date());
		msAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新用户结算账户
		Account whereMsAccount = new Account();
		whereMsAccount.setId(msAccount.getId());
		whereMsAccount.setVersion(msAccount.getVersion());
		int msCount = Account.dao.updateModelByModel(updateMsAccount, whereMsAccount);
		if(msCount == 0) {
			throw new BusinessException(RenderResultCode.BUSINESS_304);
		}
		msAccountFunds.save();
		
		
		//减少游戏币-内账
		Double befIGAmt = new Double(igAccount.getAvbAmt());
		Double aftIGAmt = NumberUtil.sub(befIGAmt, addGameCoin);
		Long lastIGVersion = new Long(igAccount.getVersion() + 1l);
		
		Account updateIgAccount = new Account();
		updateIgAccount.setAvbAmt(aftIGAmt);
		updateIgAccount.setTotalAmt(NumberUtil.add(igAccount.getFrzAmt(), aftIGAmt));
		updateIgAccount.setUpdateTime(new java.util.Date());
		updateIgAccount.setVersion(lastIGVersion);
		
		//内账游戏币流水
		AccountFunds igAccountFunds = new AccountFunds();
		igAccountFunds.setId(JoyIdUtil.simpleUUID());
		igAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		igAccountFunds.setFromAccountId(igAccount.getId());
		igAccountFunds.setToAccountId(mgAccount.getId());
		igAccountFunds.setAccType(igAccount.getAccType());
		igAccountFunds.setTransAmt(addGameCoin);
		igAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "2")));
		igAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_CONVERT, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		igAccountFunds.setBefAmt(befIGAmt);
		igAccountFunds.setAftAmt(aftIGAmt);
		igAccountFunds.setDescription(null);
		igAccountFunds.setCreateTime(new java.util.Date());
		igAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部游戏账户
		Account whereIgAccount = new Account();
		whereIgAccount.setId(igAccount.getId());
		//whereIgAccount.setVersion(mgAccount.getVersion()); //暂时不做锁
		int igCount = Account.dao.updateModelByModel(updateIgAccount, whereIgAccount);
		if(igCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		igAccountFunds.save();
		
		//增加结算账户-内部
		Double befISAmt = new Double(isAccount.getAvbAmt());
		Double aftISAmt = NumberUtil.add(befISAmt, payCNY);
		Long lastISVersion = new Long(isAccount.getVersion() + 1l);
		
		Account updateIsAccount = new Account();
		updateIsAccount.setAvbAmt(aftISAmt);
		updateIsAccount.setTotalAmt(NumberUtil.add(isAccount.getFrzAmt(), aftISAmt));
		updateIsAccount.setUpdateTime(new java.util.Date());
		updateIsAccount.setVersion(lastISVersion);
		
		//内部结算账户流水
		AccountFunds isAccountFunds = new AccountFunds();
		isAccountFunds.setId(JoyIdUtil.simpleUUID());
		isAccountFunds.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
		isAccountFunds.setFromAccountId(null);
		isAccountFunds.setToAccountId(isAccount.getId());
		isAccountFunds.setAccType(isAccount.getAccType());
		isAccountFunds.setTransAmt(payCNY);
		isAccountFunds.setTransType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD, DictAttribute.ACCOUNT_FUNDS_TRANSTYPE, "1")));
		isAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECHARGE, DictAttribute.ACCOUNT_FUNDS_ELE, "1")));
		isAccountFunds.setBefAmt(befISAmt);
		isAccountFunds.setAftAmt(aftISAmt);
		isAccountFunds.setDescription(null);
		isAccountFunds.setCreateTime(new java.util.Date());
		isAccountFunds.setUpdateTime(new java.util.Date());
		
		//更新内部结算账户
		Account whereIsAccount = new Account();
		whereIsAccount.setId(isAccount.getId());
		//whereIsAccount.setVersion(isAccount.getVersion());
		int isCount = Account.dao.updateModelByModel(updateIsAccount, whereIsAccount);
		if(isCount == 0) {
			throw new ApiBusinessException(RenderResultCode.BUSINESS_304);
		}
		isAccountFunds.save();
	}
	
}