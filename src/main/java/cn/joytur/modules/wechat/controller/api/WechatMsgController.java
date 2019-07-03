package cn.joytur.modules.wechat.controller.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.weixin.sdk.jfinal.MsgControllerAdapter;
import com.jfinal.weixin.sdk.msg.in.InTextMsg;
import com.jfinal.weixin.sdk.msg.in.event.InFollowEvent;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.weixin.sdk.msg.in.event.InQrCodeEvent;

import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.interceptor.WechatMsgInterceptor;
import cn.joytur.common.utils.JoyApiConfigUtil;
import cn.joytur.modules.wechat.service.subscribe.DialogueMessageFactory;
import cn.joytur.modules.wechat.service.subscribe.IDialogueMessage;

/**
 * wechat消息接入
 * @author xuhang
 * @time 2019年1月17日 下午12:46:25
 */
@Clear
@RouteMapping(url = "/wechat/msg")
public class WechatMsgController extends MsgControllerAdapter {

	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(WechatMsgInterceptor.class);
	
	@Before(WechatMsgInterceptor.class)
	public void index() {
    	try{
    		super.index();
    	}catch(Exception e){
    		LOGGER.error(e.getMessage(), e);
    		
    		IDialogueMessage dialogueMessage = DialogueMessageFactory.getBean(JoyApiConfigUtil.getAppType());
    		render(dialogueMessage.onExcpetionEvent(getInMsg()));
    	}
	}
	
	@Override
	protected void processInTextMsg(InTextMsg inTextMsg) {
		IDialogueMessage dialogueMessage = DialogueMessageFactory.getBean(JoyApiConfigUtil.getAppType());
		render(dialogueMessage.onTextMsgEvent(inTextMsg));
	}

	@Override
	protected void processInFollowEvent(InFollowEvent inFollowEvent) {
		IDialogueMessage dialogueMessage = DialogueMessageFactory.getBean(JoyApiConfigUtil.getAppType());
		render(dialogueMessage.onFollowEvent(inFollowEvent));
	}

	@Override
	protected void processInMenuEvent(InMenuEvent inMenuEvent) {
		IDialogueMessage dialogueMessage = DialogueMessageFactory.getBean(JoyApiConfigUtil.getAppType());
		render(dialogueMessage.onInMenuEvent(inMenuEvent));
	}
	
	@Override
	protected void processInQrCodeEvent(InQrCodeEvent inQrCodeEvent) {
		LOGGER.info("[openid:"+inQrCodeEvent.getFromUserName()+"]扫码推荐二维码");
		if (InQrCodeEvent.EVENT_INQRCODE_SUBSCRIBE.equals(inQrCodeEvent.getEvent())){
			LOGGER.info("未关注扫码：" + inQrCodeEvent.getFromUserName());
		}
		if (InQrCodeEvent.EVENT_INQRCODE_SCAN.equals(inQrCodeEvent.getEvent())){
			LOGGER.info("已关注扫码：" + inQrCodeEvent.getFromUserName());
		}
	}

}
