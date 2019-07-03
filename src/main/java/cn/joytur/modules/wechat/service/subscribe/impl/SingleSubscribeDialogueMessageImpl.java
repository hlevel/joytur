package cn.joytur.modules.wechat.service.subscribe.impl;

import com.jfinal.aop.Duang;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.weixin.sdk.msg.out.OutMsg;
import com.jfinal.weixin.sdk.msg.out.OutTextMsg;

import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.service.WechatMemberService;
import cn.joytur.modules.wechat.service.subscribe.AbstractTagTemplateImpl;

/**
 * 个人订阅号
 * @author xuhang
 * @time 2019年1月17日 下午3:48:52
 */
public class SingleSubscribeDialogueMessageImpl extends AbstractTagTemplateImpl {

	@Override
	public WechatMember checkWechatMember(String subscribeId, String openid) {
		WechatMember tmpWechatMember = WechatMember.dao.findByModel(new WechatMember().setSubscribeId(subscribeId).setOpenid(openid));
		if(tmpWechatMember == null){
			//创建保存用户
			tmpWechatMember = new WechatMember();
			tmpWechatMember.setOpenid(openid);
			tmpWechatMember.setSubscribe(1L);
			tmpWechatMember.setSubscribeId(subscribeId);
			
			Duang.duang(WechatMemberService.class).saveWechatMemberProfit(tmpWechatMember, null);
		} else {
			if(tmpWechatMember.getSubscribe() != 1L){
				
				tmpWechatMember.setSubscribe(1L);
				tmpWechatMember.setUpdateTime(new java.util.Date());
				tmpWechatMember.update();
			}
		}
		
		return tmpWechatMember;
	}

	@Override
	public OutMsg onInMenuEvent(InMenuEvent inMenuEvent) {
		OutTextMsg outMsg = new OutTextMsg(inMenuEvent);
		outMsg.setContent("个人公众号不支持菜单配置");
		return outMsg;
	}

}
