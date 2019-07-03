package cn.joytur.modules.wechat.service.subscribe;

import com.jfinal.weixin.sdk.msg.in.InMsg;
import com.jfinal.weixin.sdk.msg.in.InTextMsg;
import com.jfinal.weixin.sdk.msg.in.event.InFollowEvent;
import com.jfinal.weixin.sdk.msg.out.OutImageMsg;
import com.jfinal.weixin.sdk.msg.out.OutMsg;
import com.jfinal.weixin.sdk.msg.out.OutNewsMsg;
import com.jfinal.weixin.sdk.msg.out.OutTextMsg;

import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.ActionType;
import cn.joytur.common.utils.JoyApiConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatTemplate;

/**
 * 模版标签处理
 * @author xuhang
 * @time 2019年1月16日 下午10:21:01
 */
public abstract class AbstractTagTemplateImpl implements IDialogueMessage{

	protected static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(AbstractTagTemplateImpl.class);
	
	//当前用户
	//protected WechatMember wechatMember;
	
	@Override
	public OutMsg onFollowEvent(InFollowEvent inFollowEvent) {
		WechatMember quyWechatMember = new WechatMember().setSubscribeId(JoyApiConfigUtil.getSubscribeId()).setOpenid(inFollowEvent.getFromUserName());
		if (InFollowEvent.EVENT_INFOLLOW_SUBSCRIBE.equals(inFollowEvent.getEvent())){
			ActionType actionType = ActionType.FIRST_FOLLOW;
			
			int tmpWechatMemberCount = WechatMember.dao.findCountByModel(quyWechatMember);
			if(tmpWechatMemberCount > 0){
				actionType = ActionType.ONCE_FOLLW;
			}
			
			checkWechatMember(JoyApiConfigUtil.getSubscribeId(), inFollowEvent.getFromUserName());
			
			WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findByModel(new WechatTemplate().setEventCode(actionType.name()));
			
			return outMsgWechat(inFollowEvent, tmpWechatTemplate);
		}
		// 如果为取消关注事件，将无法接收到传回的信息
		if (InFollowEvent.EVENT_INFOLLOW_UNSUBSCRIBE.equals(inFollowEvent.getEvent())){
			WechatMember tmpWechatMember = WechatMember.dao.findByModel(quyWechatMember);
			if(tmpWechatMember != null){
				tmpWechatMember.setSubscribe(0L);
				tmpWechatMember.setUpdateTime(new java.util.Date());
				tmpWechatMember.update();
			}
			OutTextMsg outMsg = new OutTextMsg(inFollowEvent);
    		outMsg.setContent("再见,我的朋友!");
    		return outMsg;
		}
		
		return outMsgWechat(inFollowEvent, null);
		
	}
	
	@Override
	public OutMsg onTextMsgEvent(InTextMsg inTextMsg) {
		if(WechatMember.dao.findCountByModel(new WechatMember().setSubscribeId(JoyApiConfigUtil.getSubscribeId()).setOpenid(inTextMsg.getFromUserName())) == 0){
			checkWechatMember(JoyApiConfigUtil.getSubscribeId(), inTextMsg.getFromUserName());
		}
		
		String context = inTextMsg.getContent();
		WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findByKeywords(context);
		
		return outMsgWechat(inTextMsg, tmpWechatTemplate);
	}
	
	@Override
	public OutMsg onExcpetionEvent(InMsg inMsg){
		WechatTemplate tipWechatTemplate = WechatTemplate.dao.findByModel(new WechatTemplate().setEventCode(Enums.ActionType.SYSTEM_EXCEPTION_TIP.name()));
		if(tipWechatTemplate == null){
			OutTextMsg outMsg = new OutTextMsg(inMsg);
    		outMsg.setContent("抱歉,系统出现异常请您稍后再试!");
    		return outMsg;
		}
		return outMsgWechat(inMsg, tipWechatTemplate);
	}
	
	/**
	 * 返回处理
	 * @param wechatTemplate
	 * @return
	 */
	protected OutMsg outMsgWechat(InMsg inMsg, WechatTemplate wechatTemplate){
		
		if(wechatTemplate == null){
			//赋予默认值
			wechatTemplate = WechatTemplate.dao.findByModel(new WechatTemplate().setEventCode(Enums.ActionType.SYSTEM_NOTFOUNT_TIP.name()));
			if(wechatTemplate == null){
				OutTextMsg outMsg = new OutTextMsg(inMsg);
				outMsg.setContent("抱歉,无法识别您的指令,需要管理员进行后台配置.");
				return outMsg;
			}
		}
		
		Long text = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "1"));
		Long img = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_IMAGE, DictAttribute.TEMPLATE_RESPONSE_TYPE, "2"));
		Long textimg = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_IMAGETEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "3"));
		
		if(wechatTemplate.getResponseType() == text){
			OutTextMsg outMsg = new OutTextMsg(inMsg);
			outMsg.setContent(wechatTemplate.getResponseText());
			return outMsg;
		}else if(wechatTemplate.getResponseType() == img){
			OutImageMsg outMsg = new OutImageMsg(inMsg);
			outMsg.setMediaId(wechatTemplate.getResponsePicUrl());
			return outMsg;
		}else if(wechatTemplate.getResponseType() == textimg){
			/*
			OutNewsMsg outMsg = new OutNewsMsg(inMsg);
			outMsg.addNews("秀色可餐", "JFinal Weixin 极速开发就是这么爽，有木有 ^_^", "http://mmbiz.qpic.cn/mmbiz/zz3Q6WSrzq2GJLC60ECD7rE7n1cvKWRNFvOyib4KGdic3N5APUWf4ia3LLPxJrtyIYRx93aPNkDtib3ADvdaBXmZJg/0", "http://mp.weixin.qq.com/s?__biz=MjM5ODAwOTU3Mg==&mid=200987822&idx=1&sn=7eb2918275fb0fa7b520768854fb7b80#rd");
			render(outMsg);
			*/
			
			OutNewsMsg outMsg = new OutNewsMsg(inMsg);
			outMsg.addNews(wechatTemplate.getResponseTitle(), wechatTemplate.getResponseDescription(), wechatTemplate.getResponsePicUrl(), wechatTemplate.getResponseArticleUrl());
			return outMsg;
		}
		
		return null;
	}
	
}
