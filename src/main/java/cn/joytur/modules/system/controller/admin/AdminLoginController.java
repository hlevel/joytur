package cn.joytur.modules.system.controller.admin;

import java.util.Date;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.AdminDTO;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.utils.JWTUtil;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.modules.system.entities.SysLoginLog;
import cn.joytur.modules.system.entities.SysUser;

/**
 * 登录
 * @author xuhang
 */
@RouteMapping(url = "${admin}/login")
public class AdminLoginController extends BaseAdminController {

	/**
	 * 登录页面
	 */
	public void index() {
		//检测是否已经登录
		AdminDTO adminDTO = JWTUtil.getAdminDTO(this);
		if(adminDTO != null){
			//检测版本号是否过期 自动清除
			if(adminDTO.getVersion() != null && StrKit.equals(adminDTO.getVersion(), PropKit.get(CommonAttribute.SYSTEM_VERSION))){
				redirectUrl(""); return;
			}
			
			//清除cookie需要重新登录
			removeCookie(JWTUtil.ADMIN);
		}
		renderTpl("login.html");
	}

	/**
	 * 验证登录
	 */
	@Valids({
		@Valid(name = "username", required = true),
		@Valid(name = "password", required = true, max = 32, min = 6)
	})
	public void authen(String username, String password, String rememberMe){
		SysUser tmpSysUser = SysUser.dao.findByModel(new SysUser().setUserName(username));
		if(tmpSysUser == null){
			throw new BusinessException(RenderResultCode.BUSINESS_210);
		}
		
		//创建日志
		SysLoginLog sysLoginLog = new SysLoginLog().setId(IdUtil.simpleUUID()).setUserId(tmpSysUser.getId()).setBrowserType(getHeader("user-agent")).setCreateTime(new java.util.Date());
		//默认失败重试5次
		int loginErrCount = Integer.valueOf(JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_LOGINERR.name(), "5")); 
		
		//查询10分钟内登录失败次数
		DateTime mDateTime = DateUtil.offsetMinute(new Date(), -10);
		
		//获取格式化时间
		String dateString = DateUtil.format(mDateTime.toJdkDate(), DatePattern.NORM_DATETIME_PATTERN);
		int errCount = SysLoginLog.dao.findCountByStatusAndUserIdAndMinute(0L, tmpSysUser.getId(), dateString);
		
		if(errCount > loginErrCount) {
			if(tmpSysUser.getStatus() != 1L) {
				throw new BusinessException(RenderResultCode.BUSINESS_220);
			}
			
			//如果超级管理员则提示稍后再试
			if(StrKit.equals(tmpSysUser.getUserName(), CommonAttribute.SUPER_ADMIN_NAME)) {
				throw new BusinessException(RenderResultCode.BUSINESS_219);
			} else {
				tmpSysUser.setStatus(0L).setUpdateTime(new java.util.Date()).update();//其他用户禁用
				
				throw new BusinessException(RenderResultCode.BUSINESS_220);
			}
		}
		
		//加密后密码比较
		String enPassword = SecureUtil.hmacMd5(tmpSysUser.getSalt()).digestHex(password);
		if(!StrUtil.equals(tmpSysUser.getPassword(), enPassword)){
			sysLoginLog.setStatus(0L).save();	//密码失败
			int lastCount = loginErrCount-errCount-1;
			throw new BusinessException(RenderResultCode.BUSINESS_211.getCode(), "当前用户密码错误,您还可以尝试"+(lastCount) + "次");
		}

		//用户禁用
		if(tmpSysUser.getStatus() != 1L){
			throw new BusinessException(RenderResultCode.BUSINESS_212);
		}
		
		//登录成功
		//获取用户权限
		AdminDTO adminDTO = new AdminDTO();
		adminDTO.setUserid(tmpSysUser.getId());
		adminDTO.setUsername(tmpSysUser.getUserName());
		adminDTO.setNickname(tmpSysUser.getNickName());
		adminDTO.setPermissionList(SysUser.dao.findPermissionByUserId(tmpSysUser.getId()));
		adminDTO.setVersion(PropKit.get(CommonAttribute.SYSTEM_VERSION));
		
		int maxAgeInSeconds = -1;	//关闭浏览器cookie失效
		if(StrUtil.equals("on", rememberMe) ){
			maxAgeInSeconds = JWTUtil.MAX_AGE_IN_SECONDS;	//默认7天
		}
		
		setCookie(JWTUtil.ADMIN, JWTUtil.createJWT(JWTUtil.ADMIN, JsonKit.toJson(adminDTO)), maxAgeInSeconds);
		
		//记录日志
		sysLoginLog.setStatus(1L).save();
		
		renderJson(RenderResult.success(RenderResultCode.BUSINESS_201));
	}
	
}