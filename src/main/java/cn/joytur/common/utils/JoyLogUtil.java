package cn.joytur.common.utils;

import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.joytur.common.mvc.dto.AdminDTO;
import cn.joytur.modules.system.entities.SysOperLog;
/**
 * 日志工具类
 * @author xuhang
 */
public class JoyLogUtil {
	
	public static final Long TYPE_ACCESS = 1L;	//访问日志
	public static final Long TYPE_EXCEPTION = 2L;	//异常日志
	
	/**
	 * 保存日志
	 */
	public static void saveOperLog(Controller controller, String responseString, String consumeMs){
		saveOperLog(controller, null, null, responseString);
	}
	
	/**
	 * 保存日志
	 */
	public static void saveOperLog(Controller controller, String[] perms, String responseString, String consumeMs){
		saveOperLog(controller, perms, null, responseString, consumeMs);
	}
	
	/**
	 * 保存日志
	 */
	public static void saveOperLog(Controller controller, String[] perms, String exceptionString, String responseString, String consumeMs){
		AdminDTO dto = JWTUtil.getAdminDTO(controller);
		
		if (dto != null && dto.getUserid() != null){
			SysOperLog operLog = new SysOperLog();
			operLog.setId(IdUtil.simpleUUID());
			operLog.setType(exceptionString == null ? TYPE_ACCESS : TYPE_EXCEPTION);
			operLog.setRemoteAddr(ServletUtil.getClientIP(controller.getRequest()));
			operLog.setUserAgent(controller.getRequest().getHeader("user-agent"));
			operLog.setRequestUri(controller.getRequest().getRequestURI());
			operLog.setParams(JsonKit.toJson(controller.getParaMap()));
			operLog.setRequestMethod(controller.getRequest().getMethod());
			operLog.setResponse(responseString);
			operLog.setConsumeMs(consumeMs);
			operLog.setCreateUserId(dto.getUserid());
			operLog.setCreateUserName(dto.getNickname());
			operLog.setCreateTime(new java.util.Date());
			// 异步保存日志
			new SaveLogThread(operLog, perms).start();
		}
	}
	
	/**
	 * 保存日志线程
	 */
	public static class SaveLogThread extends Thread{
		
		private SysOperLog operLog;
		private String[] perms;
		
		public SaveLogThread(SysOperLog operLog, String[] perms){
			super(SaveLogThread.class.getSimpleName());
			this.operLog = operLog;
			this.perms = perms;
		}
		
		@Override
		public void run() {
			// 获取日志标题
			if (StrKit.isBlank(operLog.getTitle())){
				operLog.setTitle(JoyMenuUtil.getMenuNamePath(operLog.getRequestUri(), perms));
			}
			// 如果无标题并无异常日志，则不保存信息
			if (StrKit.isBlank(operLog.getTitle()) && StrKit.isBlank(operLog.getException())){
				return;
			}
			//保存异步线程
			operLog.save();
		}
	}

	
}
