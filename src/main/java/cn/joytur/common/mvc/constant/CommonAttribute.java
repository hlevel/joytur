package cn.joytur.common.mvc.constant;

import java.io.File;

/**
 * 公共参数
 * @author xuhang
 */
public final class CommonAttribute {
	
	/** UTF-8编码 */
	public static final String UTF_8 = "UTF-8";
	
	/** 系统配置名称 */
	public static final String SYSTEM_DEV_MODE = "system.devMode";
	public static final String SYSTEM_VERSION = "system.version";
	public static final String SYSTEM_THEME = "system.theme";
	public static final String SYSTEM_ADMIN_PATH = "system.admin.path";
	public static final String SYSTEM_PACKAGE = "system.package";
	public static final String SYSTEM_DDL_CREATE = "system.ddl.create";
	public static final String SYSTEM_JOY_MODE = "system.joy.mode";
	public static final String SYSTEM_DEBUG_OPENID = "system.debug.openid";
	
	/** 静态路径名称 */
	public static final String BASE_STATIC_PATH = "/static/";
	
	/** 后台服务错误页面 */
	public static final String ADMIN_ERROR_VIEW = "/templates/default/admin/error/error.html";
	/** 后台权限错误页面 */
	public static final String ADMIN_UNAUTHORIZED_VIEW = "/templates/default/admin/error/no_auth.html";
	/** 后台404错误页面 */
	public static final String ADMIN_NOTFOUNT_VIEW = "/templates/default/admin/error/404.html";
	
	/** 前台错误页面 */
	public static final String FRONT_ERROR_VIEW = "/templates/default/admin/error/error.html";
	
	/** 前台权限错误页面 */
	public static final String FRONT_RESOURCE_NOT_FOUND_VIEW = "/404.html";
	
	/** joytur.conf */
	public static final String CONFIG_PROPERTIES = "joytur.conf";
	
	/** 页面后缀 */
	public static final String VIEW_EXTENSION = ".html";
	
	/** 参数分隔符 */
	public static final String URL_PARA_SEPARATOR = "-";

	/** 上传文件目录文件夹名 */
	public static final String BASE_UPLOAD_PARENT_PATH = "static" + File.separator+ "upload" + File.separator;	
	/** 上传文件临时目录文件夹名 */
	public static final String BASE_UPLOAD_FOLDER_NAME = "tmp";
	/** 上传文件临时目录 */
	public static final String BASE_UPLOAD_TMP_PATH = BASE_UPLOAD_PARENT_PATH + BASE_UPLOAD_FOLDER_NAME;
	
	/** JSON时间格式 */
	public static final String JSON_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	/**cahce name*/
	public static final String CACHE_SESSION = "session";
	public static final String CACHE_SYSTEM = "system";
	public static final String CACHE_WECHAT = "wechat";
	public static final String CACHE_AREA = "area";
	public static final String CACHE_CONFIG = "config";
	
	/** 超级管理员登录名 */
	public static final String SUPER_ADMIN_NAME = "admin";
	/** 内部账户关联的用户id标识 */
	public static final String INSIDE_WECHAT_MEMBER_ID = "1";

	public static final String RECHARGE_DESCRIPTION = "微信充值";
	/** 充值订单过期时间120s */
	public static final long RECHARGE_EXPIRE_TIME = 120;
	
	/** 数据锁文件 */
	public static final String DB_DDL_LOCK = ".lock";
	
	/**
	 * 不可实例化
	 */
	private CommonAttribute() {
	}
    
}