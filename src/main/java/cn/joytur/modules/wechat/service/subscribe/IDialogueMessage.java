package cn.joytur.modules.wechat.service.subscribe;

import com.jfinal.weixin.sdk.msg.in.InMsg;
import com.jfinal.weixin.sdk.msg.in.InTextMsg;
import com.jfinal.weixin.sdk.msg.in.event.InFollowEvent;
import com.jfinal.weixin.sdk.msg.in.event.InMenuEvent;
import com.jfinal.weixin.sdk.msg.out.OutMsg;

import cn.joytur.modules.wechat.entities.WechatMember;

/**
 * 对话消息处理
 * @author xuhang
 *
 */
public interface IDialogueMessage {

	/**
	 * 检查用户入库
	 * @param subscribeId
	 * @param openid
	 * @return
	 */
	public WechatMember checkWechatMember(String subscribeId, String openid);
	
	/**
	 * 关注处理
	 * @param inFollowEvent
	 * @return
	 */
	public OutMsg onFollowEvent(InFollowEvent inFollowEvent);
	
	/**
	 * 文本输入消息
	 * @param inTextMsg
	 * @return
	 */
	public OutMsg onTextMsgEvent(InTextMsg inTextMsg);
	
	/**
	 * 菜单点击事件
	 * @param inMenuEvent
	 * @return
	 */
	public OutMsg onInMenuEvent(InMenuEvent inMenuEvent);
	
	/**
	 * 异常处理
	 * @param inMsg
	 * @return
	 */
	public OutMsg onExcpetionEvent(InMsg inMsg);
	
}
