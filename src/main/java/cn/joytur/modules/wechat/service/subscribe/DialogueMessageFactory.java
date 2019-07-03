package cn.joytur.modules.wechat.service.subscribe;

import com.jfinal.aop.Duang;
import com.jfinal.kit.StrKit;

import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.wechat.service.subscribe.impl.EnterpriseServiceDialogueMessageImpl;
import cn.joytur.modules.wechat.service.subscribe.impl.EnterpriseSubscribeDialogueMessageImpl;
import cn.joytur.modules.wechat.service.subscribe.impl.SingleSubscribeDialogueMessageImpl;

/**
 * 对话消息处理	
 * @author xuhang
 *
 */
public class DialogueMessageFactory {

	/**
	 * 创建对应bean
	 * @param apptype
	 * @return
	 */
	public static IDialogueMessage getBean(String apptype){
		if(StrKit.equals(apptype, JoyDictUtil.getDictValue(DictAttribute.SUBSCRIBE_TYPE_PERSONAL, DictAttribute.SUBSCRIBE_TYPE, ""))){
			return Duang.duang(SingleSubscribeDialogueMessageImpl.class);
		}else if(StrKit.equals(apptype, JoyDictUtil.getDictValue(DictAttribute.SUBSCRIBE_TYPE_ENTERPRISE, DictAttribute.SUBSCRIBE_TYPE, ""))){
			return Duang.duang(EnterpriseSubscribeDialogueMessageImpl.class);
		}else if(StrKit.equals(apptype, JoyDictUtil.getDictValue(DictAttribute.SUBSCRIBE_TYPE_SERVICE, DictAttribute.SUBSCRIBE_TYPE, ""))){
			return Duang.duang(EnterpriseServiceDialogueMessageImpl.class);
		}
		return null;
	}
	
}
