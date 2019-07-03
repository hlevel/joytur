package cn.joytur.common.interceptor;


import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.ActionException;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.render.JsonRender;
import com.jfinal.render.RedirectRender;
import com.jfinal.render.TemplateRender;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.exception.ApiBusinessException;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.exception.NoAuthorizationException;
import cn.joytur.common.exception.NotLoggedInException;
import cn.joytur.common.exception.ValidErrorException;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.utils.DeviceUtil;
import cn.joytur.common.utils.JoyLogUtil;

/**
 * 全局异常
 * @author xuhang
 */
public class ExceptionInterceptor implements Interceptor{
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ExceptionInterceptor.class);
	
	private static String LOGGER_MESSAGE_REQ = "开始计时: {} - URI: {} - HTTP_METHOD: {} - IP: {} - CLASS_METHOD: {} - ARGS: {}";
	private static String LOGGER_MESSAGE_RES = "计时结束: {} - 耗时：{} - URI: {} - ARGS: {} - RESPONSE: {}";

	@Override
	public void intercept(Invocation inv){
		boolean ajax = DeviceUtil.isAjax(inv.getController().getRequest());
		
		Controller controller = inv.getController();
		HttpServletRequest request = controller.getRequest();
		com.jfinal.render.Render render = null;

		Method method = inv.getMethod(); //当前访问的Action
		AuthRequire.Perms perms = method.getAnnotation(AuthRequire.Perms.class);
		//1、开始时间  
		long beginTime = System.currentTimeMillis();
		String exceptionString = null;
		boolean wRecord = true; //是否写日志
		try{
			LOGGER.info(LOGGER_MESSAGE_REQ, new SimpleDateFormat("HH:mm:ss.SSS").format(beginTime), inv.getActionKey(), request.getMethod() 
					, request.getRemoteAddr(), controller.getClass().getName() + "." + inv.getMethodName() , JsonKit.toJson(controller.getParaMap()));
			
			inv.invoke();
			
		} catch (ValidErrorException e) {
			if(ajax){
				render = new JsonRender(RenderResult.error(e.getMessage()));
			}
			
			exceptionString = ExceptionUtil.getMessage(e);
		} catch (BusinessException e) {
			if(ajax){
				render = new JsonRender(RenderResult.error(e.getMessage()));
			} else {
				controller.setAttr("errorMessage", e.getMessage());
				render = new TemplateRender("/templates/" + PropKit.get(CommonAttribute.SYSTEM_THEME) + "/admin/error/error.html");
			}
			
			exceptionString = ExceptionUtil.getMessage(e);
		} catch (ApiBusinessException e) {
			render = new JsonRender(RenderResult.error(e.getMessage()));
			exceptionString = ExceptionUtil.getMessage(e);
		} catch (NotLoggedInException e) {
			if(ajax) {
				render = new JsonRender(RenderResult.error(RenderResultCode.NOTLOGGEDIN));
			} else {
				if(e.getRedirectUrl() == null){
					/*if(controller instanceof BaseAdminController){
						render = new RedirectRender(PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH) + "/login");
					}else{
						render = new RedirectRender(PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH) + "/wap");
					}*/
				}else{
					render = new RedirectRender(e.getRedirectUrl());
				}
				
			}
			exceptionString = ExceptionUtil.getMessage(e);
			wRecord = false;
		} catch (NoAuthorizationException e) {
			if(ajax) {
				render = new JsonRender(RenderResult.error(RenderResultCode.NOTAUTH));
			}
			
			exceptionString = ExceptionUtil.getMessage(e);
		} catch (ActionException e) {
			if(ajax) {
				render = new JsonRender(RenderResult.error(RenderResultCode.NOTAUTH));
			}
			
			exceptionString = ExceptionUtil.getMessage(e);
		} catch (Exception e) {
			e.printStackTrace();
			
			exceptionString = ExceptionUtil.getMessage(e);
		} finally {
			String responseText = JsonKit.toJson(render == null ? controller.getRender() : render);
			
			// 记录下请求内容
			long endTime = System.currentTimeMillis(); 	//2、结束时间  
			String consumeMs = formatDateTime(endTime - beginTime);
			
			//后台访问记录日志
			if(controller instanceof BaseAdminController){
				if(wRecord){
					//保存日志
					JoyLogUtil.saveOperLog(controller, perms != null ? perms.value() : null, exceptionString, responseText, consumeMs);
				}
			}
			
			//太长不做打印显示
			if(StrKit.notBlank(responseText) && responseText.length() > 512){
				responseText = StrUtil.sub(responseText, 0, 512) + "...";
			}
			
			LOGGER.info(LOGGER_MESSAGE_RES, new SimpleDateFormat("HH:mm:ss.SSS").format(endTime), consumeMs, inv.getActionKey(), JsonKit.toJson(controller.getParaMap()), responseText);
		}
		
		if(render != null){
			inv.getController().render(render);
		}
		
	}
	
	
	/**
	 * 转换为时间（天,时:分:秒.毫秒）
	 * @param timeMillis
	 * @return
	 */
    private String formatDateTime(long timeMillis){
    	if(timeMillis == 0){
    		return "";
    	}
		long day = timeMillis/(24*60*60*1000);
		long hour = (timeMillis/(60*60*1000)-day*24);
		long min = ((timeMillis/(60*1000))-day*24*60-hour*60);
		long s = (timeMillis/1000-day*24*60*60-hour*60*60-min*60);
		long sss = (timeMillis-day*24*60*60*1000-hour*60*60*1000-min*60*1000-s*1000);
		return (day>0?day+",":"")+hour+":"+min+":"+s+"."+sss;
    }
}
