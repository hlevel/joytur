package cn.joytur.common.interceptor;

import java.lang.reflect.Method;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.exception.NoAuthorizationException;
import cn.joytur.common.exception.NotLoggedInException;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.controller.BaseApiController;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.AdminDTO;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JWTUtil;

/**
 * 鉴权拦截器
 * @author xuhang
 * (http://www.jfinal.com/share/315)
 */
public class AuthInterceptor implements Interceptor {

	public void intercept(Invocation inv) {
		Controller controller = inv.getController();
		
        boolean authAccept = false; //鉴权结果，默认为失败
        Method method = inv.getMethod(); //当前访问的Action
        
        /**
         * 解析鉴权模式：
         * 1）先判断Action是否要求鉴权；
         * 2）如果没有，则继续判断Controller是否要求鉴权；
         * 3）如果仍然没有，则默认允许匿名访问
         */
        AuthMode mode = getAuthMode(method, controller.getClass()); //鉴权模式对象封装
        /*
        String authorization = controller.getPara(JWTUtil.AUTHORIZATION);	//认证数据则必须要微信浏览器
        
        if(StrKit.notBlank(authorization) && !DeviceUtil.isWechat(controller.getRequest())){
        	controller.redirect("/wap/security/wxbrowser"); return;
        }
        */
        if(controller instanceof BaseAdminController){
        	//无需权限
        	if (mode.authCode == Enums.AuthCode.REQ_GUEST){
        		authAccept = true;
        	} else {
        		AdminDTO dto = JWTUtil.getAdminDTO(controller);
        		
        		if(dto == null || StrKit.isBlank(dto.getUserid())){
        			throw new NotLoggedInException(PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH) + "/login"); //未登录
        		}
        		
        		inv.getController().setAttr("yaAdmin", dto);	//存放attr
        		
        		//super pass
        		if(StrUtil.equals(CommonAttribute.SUPER_ADMIN_NAME, dto.getUsername())){
        			authAccept = true;
        		}else{
        			
        			if(mode.authCode == Enums.AuthCode.REQ_LOGIN){
        				authAccept = true;
        			}else if(mode.authCode == Enums.AuthCode.REQ_ROLES){	//鉴权角色名
        				if(ArrayUtil.contains(mode.authIds, "admin")){
        					authAccept = true;
        				}
        			}else if(mode.authCode == Enums.AuthCode.REQ_PERMS){	//访问权限
        				for(String auth : mode.authIds){
        					if(CollUtil.contains(dto.getPermissionList(), auth)){
        						authAccept = true;
        					}
        				}
        			}
        			
        		}
        		
        	}
        } else if (controller instanceof BaseWapController){	//wap请求
        	if (mode.authCode == Enums.AuthCode.REQ_GUEST){
        		authAccept = true;
        	} else {
        		WapMemberDTO dto = JWTUtil.getWapMemberDTO(controller);
        		
        		if(dto == null || StrKit.isBlank(dto.getOpenid())){
        			throw new NotLoggedInException("/wap"); //未登录
        		}
        		
        		//检测版本号是否过期 自动清除
    			if(dto.getVersion() != null && !StrKit.equals(dto.getVersion(), PropKit.get(CommonAttribute.SYSTEM_VERSION))){
    				//清除cookie需要重新登录
    				controller.removeCookie(JWTUtil.MEMBER);
    				
    				throw new NotLoggedInException("/wap"); //未登录
    			}
        		
        		inv.getController().setAttr("yaMember", dto);	//存放attr
        		
        		authAccept = true;
        	}
        }
        else if (controller instanceof BaseApiController){	//api直接通过
        	authAccept = true;
        }
        /*
        else if (controller instanceof MsgControllerAdapter || controller instanceof ApiController){	//微信直接通过
        	authAccept = true;
        }
        */
        
        if(!authAccept){ //权限认证失败
        	throw new NoAuthorizationException(); 
        }
        
    	inv.invoke();
    }
    
	/**
     * 优先进行Action层面的鉴权
     * @param method
     * @param ctrl
     */
    private AuthMode getAuthMode(Method method, Class<?> ctrl){
    	AuthMode mode = new AuthMode();
        if(method.isAnnotationPresent(AuthRequire.Roles.class)){
            mode.authCode = Enums.AuthCode.REQ_ROLES;
            mode.authIds = method.getAnnotation(AuthRequire.Roles.class).value();
        }else if(method.isAnnotationPresent(AuthRequire.Perms.class)){
            mode.authCode = Enums.AuthCode.REQ_PERMS;
            mode.authIds = method.getAnnotation(AuthRequire.Perms.class).value();
        }else if(method.isAnnotationPresent(AuthRequire.Logined.class)){
            mode.authCode = Enums.AuthCode.REQ_LOGIN;
        }else if(method.isAnnotationPresent(AuthRequire.Guest.class)){
            mode.authCode = Enums.AuthCode.REQ_GUEST;
        }else{
        	
            //进行Controller层面的鉴权，只有当Action未设置时有效
            if(ctrl.isAnnotationPresent(AuthRequire.Roles.class)){
                mode.authCode = Enums.AuthCode.REQ_ROLES;
                mode.authIds = ctrl.getAnnotation(AuthRequire.Roles.class).value();
            }else if(ctrl.isAnnotationPresent(AuthRequire.Perms.class)){
                mode.authCode = Enums.AuthCode.REQ_PERMS;
                mode.authIds = ctrl.getAnnotation(AuthRequire.Perms.class).value();
            }else if(ctrl.isAnnotationPresent(AuthRequire.Logined.class)){
                mode.authCode = Enums.AuthCode.REQ_LOGIN;
            }else{
                mode.authCode = Enums.AuthCode.REQ_GUEST;		//默认可以访问
            }
            
        }
        
        return mode;
    }
    
    /**
     * 鉴权模式封装
     */
    class AuthMode{
        private Enums.AuthCode authCode = null; //各种鉴权模式枚举
        private String[] authIds = null; //多条鉴权标识（多角色、多权限）
    }
    
}
