package cn.joytur.modules.wechat.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.StrKit;

import cn.hutool.core.util.IdUtil;
import cn.joytur.common.mvc.async.AsyncManager;
import cn.joytur.common.mvc.async.AsyncTaskFactory;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.product.entities.RecommendRule;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatMemberProfit;
import cn.joytur.modules.wechat.entities.WechatMemberRecommend;

/**
 * 微信模版管理
 * @author xuhang
 * @time 2019年1月16日 下午9:39:56
 */
public class WechatMemberService {
	
	/**
	 * 创建用户基础权益
	 * @param wechatMember
	 * @param acceptCode
	 * @return
	 */
	public boolean saveWechatMemberProfit(WechatMember wechatMember, String acceptCode) {
		//填充基础信息
		wechatMember.setId(IdUtil.simpleUUID());
		wechatMember.setUpdateTime(new java.util.Date());
		wechatMember.setCreateTime(new java.util.Date());
		wechatMember.save();
		
		//创建账户
		Account tmpAccount = new Account();
		tmpAccount.setId(IdUtil.simpleUUID());
		tmpAccount.setWechatMemberId(wechatMember.getId());
		tmpAccount.setTotalAmt(0.0);
		tmpAccount.setFrzAmt(0.0);
		tmpAccount.setAvbAmt(0.0);
		tmpAccount.setStatus(1L);
		tmpAccount.setVersion(0L);
		tmpAccount.setAccType(Long.parseLong(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_TYPE_GAME, DictAttribute.ACCOUNT_ACC_TYPE, "3")));
		tmpAccount.setUpdateTime(new java.util.Date());
		tmpAccount.setCreateTime(new java.util.Date());
		tmpAccount.save();
		
		//添加默认权益档位
		/*
		RecommendRule underRecommendRule = RecommendRule.dao.findByModel(new RecommendRule().setRecName(DictAttribute.RECOMMEND_LEVEL_NAME[0]));
		if(underRecommendRule == null) {
			throw new BusinessException(RenderResultCode.BUSINESS_352);
		}
		WechatMemberProfit wechatMemberProfit = new WechatMemberProfit();
		wechatMemberProfit.setId(IdUtil.simpleUUID());
		wechatMemberProfit.setWechatMemberId(wechatMember.getId());
		wechatMemberProfit.setRecommendRuleId(underRecommendRule.getId());
		wechatMemberProfit.setRecVal1(underRecommendRule.getRecVal1());
		wechatMemberProfit.setRecVal2(underRecommendRule.getRecVal2());
		wechatMemberProfit.setRecVal3(underRecommendRule.getRecVal3());
		wechatMemberProfit.setStatus(1L);
		wechatMemberProfit.setCreateTime(new java.util.Date());
		wechatMemberProfit.setUpdateTime(new java.util.Date());
		wechatMemberProfit.save();
		*/

		//查询一个没有使用并且有效推荐码
		String newAcceptCode = null;
		int newCountWhile = 0;
		do{
			newAcceptCode = JoyIdUtil.toSerialCode();
			newCountWhile ++;
		}while(WechatMemberRecommend.dao.findCountByModel(new WechatMemberRecommend().setAcceptCode(newAcceptCode)) > 0 && newCountWhile < 100);
		
		//创建推荐推荐码
		WechatMemberRecommend tmpWechatMemberRecommend = new WechatMemberRecommend();
		tmpWechatMemberRecommend.setId(JoyIdUtil.simpleUUID());
		tmpWechatMemberRecommend.setWechatMemberId(wechatMember.getId());
		tmpWechatMemberRecommend.setAcceptCode(newAcceptCode);
		
		//查询推荐的code信息
		WechatMemberRecommend acceptWechatMemberRecommend = null;
		if(StrKit.notBlank(acceptCode)) {
			acceptWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setAcceptCode(acceptCode));
			
			if(acceptWechatMemberRecommend != null) {
				tmpWechatMemberRecommend.setReceiveWechatMemberId(acceptWechatMemberRecommend.getWechatMemberId());
				
				//启动返还活动
				AsyncManager.me().execute(AsyncTaskFactory.asyncRecommendActiveCommission(acceptWechatMemberRecommend.getWechatMemberId()));
			}
		}
		
		tmpWechatMemberRecommend.setStatus(1L); //有效
		tmpWechatMemberRecommend.setCreateTime(new java.util.Date());
		tmpWechatMemberRecommend.setUpdateTime(new java.util.Date());
		tmpWechatMemberRecommend.save();
		
		AsyncManager.me().execute(AsyncTaskFactory.asyncNewMemberActiveCommission(wechatMember.getId()));
		
		return true;
	}
	
	
	/**
	 * 升级用户权益
	 * @param wechatMemberId
	 * @param recommendRuleId
	 */
	public void upgradeWechatMemberProfit(String wechatMemberId, RecommendRule tmpRecommendRule) {
		//修改已经拥有权益
		WechatMemberProfit tmpWechatMemberProfit = WechatMemberProfit.dao.findByModel(new WechatMemberProfit().setWechatMemberId(wechatMemberId));
		
		if(tmpWechatMemberProfit == null){
			tmpWechatMemberProfit = new WechatMemberProfit();
			tmpWechatMemberProfit.setId(JoyIdUtil.simpleUUID());
			tmpWechatMemberProfit.setRecommendRuleId(tmpRecommendRule.getId());
			tmpWechatMemberProfit.setRecVal1(tmpRecommendRule.getRecVal1());
			tmpWechatMemberProfit.setRecVal2(tmpRecommendRule.getRecVal2());
			tmpWechatMemberProfit.setRecVal3(tmpRecommendRule.getRecVal3());
			tmpWechatMemberProfit.setWechatMemberId(wechatMemberId);
			tmpWechatMemberProfit.setLastMirror(null);
			tmpWechatMemberProfit.setCreateTime(new java.util.Date());
			tmpWechatMemberProfit.setUpdateTime(new java.util.Date());
			tmpWechatMemberProfit.save();
			
		}else{
			
			JSONObject lastMirrorJSON = new JSONObject();
			lastMirrorJSON.put("val1", tmpRecommendRule.getRecVal1());
			lastMirrorJSON.put("val2", tmpRecommendRule.getRecVal2());
			lastMirrorJSON.put("val3", tmpRecommendRule.getRecVal3());
			
			tmpWechatMemberProfit.setRecommendRuleId(tmpRecommendRule.getId());
			tmpWechatMemberProfit.setRecVal1(tmpRecommendRule.getRecVal1());
			tmpWechatMemberProfit.setRecVal2(tmpRecommendRule.getRecVal2());
			tmpWechatMemberProfit.setRecVal3(tmpRecommendRule.getRecVal3());
			tmpWechatMemberProfit.setLastMirror(lastMirrorJSON.toJSONString());
			tmpWechatMemberProfit.setUpdateTime(new java.util.Date());
			tmpWechatMemberProfit.update();
		}
		
		//创建收益账户
		Account tmpAccount = Account.dao.getSettAccount(wechatMemberId);
		if(tmpAccount == null){
			tmpAccount = new Account();
			tmpAccount.setId(IdUtil.simpleUUID());
			tmpAccount.setWechatMemberId(wechatMemberId);
			tmpAccount.setTotalAmt(0.0);
			tmpAccount.setFrzAmt(0.0);
			tmpAccount.setAvbAmt(0.0);
			tmpAccount.setStatus(1L);
			tmpAccount.setVersion(0L);
			tmpAccount.setAccType(Long.parseLong(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_TYPE_SETT, DictAttribute.ACCOUNT_ACC_TYPE, "4")));
			tmpAccount.setUpdateTime(new java.util.Date());
			tmpAccount.setCreateTime(new java.util.Date());
			tmpAccount.save();
		}
		
	}
	
}