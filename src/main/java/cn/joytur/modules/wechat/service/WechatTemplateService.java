package cn.joytur.modules.wechat.service;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.hutool.core.util.IdUtil;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.wechat.entities.WechatTemplate;

/**
 * 微信模版管理
 * @author xuhang
 * @time 2019年1月16日 下午9:39:56
 */
public class WechatTemplateService {

	/**
	 * 初始化基础模版
	 * @param subscribeId
	 */
	@Before(Tx.class)
	public void initWechatTemplate(String subscribeId){
		if(StrKit.notBlank(subscribeId)){
			//查询当前对象是否已经存在
			int count = WechatTemplate.dao.findCountByModel(new WechatTemplate().setSubscribeId(subscribeId));
			if(count == 0){
				//首次关注
				WechatTemplate FIRST_FOLLOW = new WechatTemplate();
				FIRST_FOLLOW.setId(IdUtil.simpleUUID());
		    	FIRST_FOLLOW.setEventType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM, DictAttribute.TEMPLATE_EVENT_TYPE, "")));
		    	FIRST_FOLLOW.setEventCode(Enums.ActionType.FIRST_FOLLOW.getCode());
		    	FIRST_FOLLOW.setResponseType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "")));
		    	FIRST_FOLLOW.setResponseText("谢谢您的关注!");
		    	FIRST_FOLLOW.setSubscribeId(subscribeId);
		    	FIRST_FOLLOW.setStatus(1l);
		    	FIRST_FOLLOW.setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
		    	FIRST_FOLLOW.save();
		    	
				//再次关注
		    	WechatTemplate ONCE_FOLLW = new WechatTemplate();
		    	ONCE_FOLLW.setId(IdUtil.simpleUUID());
		    	ONCE_FOLLW.setEventType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM, DictAttribute.TEMPLATE_EVENT_TYPE, "")));
		    	ONCE_FOLLW.setEventCode(Enums.ActionType.ONCE_FOLLW.getCode());
		    	ONCE_FOLLW.setResponseType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "")));
		    	ONCE_FOLLW.setResponseText("欢迎您再次回来,持续为您服务!");
		    	ONCE_FOLLW.setSubscribeId(subscribeId);
		    	ONCE_FOLLW.setStatus(1l);
		    	ONCE_FOLLW.setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
		    	ONCE_FOLLW.save();
		    	
		    	//系统异常提示
		    	WechatTemplate SYSTEM_EXCEPTION_TIP = new WechatTemplate();
		    	SYSTEM_EXCEPTION_TIP.setId(IdUtil.simpleUUID());
		    	SYSTEM_EXCEPTION_TIP.setEventType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM, DictAttribute.TEMPLATE_EVENT_TYPE, "")));
		    	SYSTEM_EXCEPTION_TIP.setEventCode(Enums.ActionType.SYSTEM_EXCEPTION_TIP.getCode());
		    	SYSTEM_EXCEPTION_TIP.setResponseType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "")));
		    	SYSTEM_EXCEPTION_TIP.setResponseText("抱歉,系统出现异常./:shake \n 请您稍后再试.");
		    	SYSTEM_EXCEPTION_TIP.setSubscribeId(subscribeId);
		    	SYSTEM_EXCEPTION_TIP.setStatus(1l);
		    	SYSTEM_EXCEPTION_TIP.setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
		    	SYSTEM_EXCEPTION_TIP.save();
		    	
		    	//系统异常提示
		    	WechatTemplate SYSTEM_NOTFOUNT_TIP = new WechatTemplate();
		    	SYSTEM_NOTFOUNT_TIP.setId(IdUtil.simpleUUID());
		    	SYSTEM_NOTFOUNT_TIP.setEventType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM, DictAttribute.TEMPLATE_EVENT_TYPE, "")));
		    	SYSTEM_NOTFOUNT_TIP.setEventCode(Enums.ActionType.SYSTEM_NOTFOUNT_TIP.getCode());
		    	SYSTEM_NOTFOUNT_TIP.setResponseType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "")));
		    	SYSTEM_NOTFOUNT_TIP.setResponseText("小主人这个指令我还未学习!如果您需要知道我会的功能\n请输入help或者“帮助”");
		    	SYSTEM_NOTFOUNT_TIP.setSubscribeId(subscribeId);
		    	SYSTEM_NOTFOUNT_TIP.setStatus(1l);
		    	SYSTEM_NOTFOUNT_TIP.setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
		    	SYSTEM_NOTFOUNT_TIP.save();
		    	
		    	//帮助
		    	WechatTemplate WEIXIN_HELP = new WechatTemplate();
		    	WEIXIN_HELP.setId(IdUtil.simpleUUID());
		    	WEIXIN_HELP.setEventType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_CUSTOM, DictAttribute.TEMPLATE_EVENT_TYPE, "")));
		    	WEIXIN_HELP.setEventCode(null);
		    	WEIXIN_HELP.setEventKeywords("help,帮助");
		    	WEIXIN_HELP.setResponseType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "")));
		    	WEIXIN_HELP.setResponseText("Q.如果您需要帮助\nA.请拨打电话123456");
		    	WEIXIN_HELP.setSubscribeId(subscribeId);
		    	WEIXIN_HELP.setStatus(1l);
		    	WEIXIN_HELP.setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
		    	WEIXIN_HELP.save();
		    	
			}
		}
	}
	
}