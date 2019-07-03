package cn.joytur.modules.wechat.service.subscribe.impl;

import com.jfinal.aop.Duang;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.weixin.sdk.msg.out.OutMsg;
import com.jfinal.weixin.sdk.msg.out.OutTextMsg;

import cn.joytur.common.utils.JoyApiConfigUtil;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatTemplate;
import cn.joytur.modules.wechat.service.WechatMemberService;
import cn.joytur.modules.wechat.service.subscribe.AbstractTagTemplateImpl;

/**
 * 企业服务号
 * @author xuhang
 * @time 2019年1月17日 下午3:48:20
 */
public class EnterpriseServiceDialogueMessageImpl extends AbstractTagTemplateImpl{

	@Override
	public WechatMember checkWechatMember(String subscribeId, String openid) {
		ApiResult apiResult = UserApi.getUserInfo(openid);
		
		LOGGER.info("[{}]获取获取用户详情信息:{}", openid, apiResult.toString());
		String nickname = apiResult.getStr("nickname");
		Long sex = apiResult.getLong("sex");
		String language = apiResult.getStr("language");
		String country = apiResult.getStr("country");
		String province = apiResult.getStr("province");
		String city = apiResult.getStr("city");
		String headimgurl = apiResult.getStr("headimgurl");
		
		WechatMember tmpWechatMember = WechatMember.dao.findByModel(new WechatMember().setSubscribeId(subscribeId).setOpenid(openid));
		if(tmpWechatMember == null){
			//创建保存用户
			tmpWechatMember = new WechatMember();
			tmpWechatMember.setOpenid(openid);
			tmpWechatMember.setSubscribe(1L);
			tmpWechatMember.setSubscribeId(subscribeId);
			
			if(StrKit.notBlank(nickname))  tmpWechatMember.setNickName(nickname);
			if(sex != null) tmpWechatMember.setSex(sex);
			if(StrKit.notBlank(language)) tmpWechatMember.setLanguage(language);
			if(StrKit.notBlank(country)) tmpWechatMember.setCountry(country);
			if(StrKit.notBlank(province)) tmpWechatMember.setProvince(province);
			if(StrKit.notBlank(city)) tmpWechatMember.setCity(city);
			if(StrKit.notBlank(headimgurl)) tmpWechatMember.setHeadimgUrl(headimgurl);
			/*
			tmpWechatMember.setBirth(java.util.Date birth);
			tmpWechatMember.setUnionid(java.lang.String unionid);
			tmpWechatMember.setRemarks(java.lang.String remarks);
			*/
			
			Duang.duang(WechatMemberService.class).saveWechatMemberProfit(tmpWechatMember, null);
		} else {
			if(tmpWechatMember.getSubscribe() != 1L){
				
				if(StrKit.notBlank(nickname))  tmpWechatMember.setNickName(nickname);
				if(sex != null) tmpWechatMember.setSex(sex);
				if(StrKit.notBlank(language)) tmpWechatMember.setLanguage(language);
				if(StrKit.notBlank(country)) tmpWechatMember.setCountry(country);
				if(StrKit.notBlank(province)) tmpWechatMember.setProvince(province);
				if(StrKit.notBlank(city)) tmpWechatMember.setCity(city);
				if(StrKit.notBlank(headimgurl)) tmpWechatMember.setHeadimgUrl(headimgurl);
				
				tmpWechatMember.setSubscribe(1L);
				tmpWechatMember.setUpdateTime(new java.util.Date());
				tmpWechatMember.update();
			}
		}
		
		return tmpWechatMember;
	}

	@Override
	public OutMsg onInMenuEvent(InMenuEvent inMenuEvent) {
		
		if(WechatMember.dao.findCountByModel(new WechatMember().setSubscribeId(JoyApiConfigUtil.getSubscribeId()).setOpenid(inMenuEvent.getFromUserName())) == 0){
			checkWechatMember(JoyApiConfigUtil.getSubscribeId(), inMenuEvent.getFromUserName());
		}
		
		if(StrKit.equals(inMenuEvent.getEvent(), "VIEW")){
			OutTextMsg outMsg = new OutTextMsg(inMenuEvent);
    		outMsg.setContent("点击网页浏览");
    		return outMsg;
		}
		
		WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findById(inMenuEvent.getEventKey());
		return outMsgWechat(inMenuEvent, tmpWechatTemplate);
	}

}
