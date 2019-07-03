package cn.joytur.common.utils;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NetUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.dto.AdminDTO;
import cn.joytur.modules.product.entities.ExtensionAdv;
import cn.joytur.modules.system.entities.SysConfig;
import cn.joytur.modules.system.entities.SysGfw;

/**
 * 系统配置工具类
 */
public class JoyConfigUtil {
	
	public static final String CACHE_CONFIG_KEY = "joy_config_key";
	
	public static String getConfigValue(String name) {
		return getConfigValue(name, null);
	}
	
	/**
	 *  获取缓存配置
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getConfigValue(String name, String defaultValue){
		@SuppressWarnings("unchecked")
		Map<String, String> configMap = (Map<String, String>) CacheKit.get(CommonAttribute.CACHE_SYSTEM, CACHE_CONFIG_KEY);
		
		if (configMap==null){
			configMap = new HashMap<String, String>();
			for (SysConfig config : SysConfig.dao.findAll()){
				configMap.put(config.getName(), config.getValue());
			}
			CacheKit.put(CommonAttribute.CACHE_SYSTEM, CACHE_CONFIG_KEY, configMap);
		}
		
		if(configMap.containsKey(name)) {
			return configMap.get(name);
		}
		
		if(StrKit.notBlank(defaultValue)) {
			return defaultValue;
		}
		
		return null;
	}
	
	/**
	 * 获取主域名
	 * @return
	 */
    public static String getMdUrl(){
    	String url = SysGfw.dao.findSelfMasterUrl();
    	if(StrKit.isBlank(url)){
    		return "/";
    	}
    	return url;
    }
    
    /**
     * 获取从域名
     * @return
     */
    public static String getSdUrl(){
    	return getSdUrl("/");
    }
    
    /**
     * 获取从域名
     * @return
     */
    public static String getSdUrl(String defaultUrl){
    	return getSdUrl(defaultUrl, null);
    }
    
    /**
     * 获取从域名
     * @return
     */
    public static String getSdUrl(String defaultUrl, String defaultPort){
    	String url = SysGfw.dao.findSelfSlaveUrl();
    	
    	if(StrKit.isBlank(url)){
			url = defaultUrl!=null ? defaultUrl : "http://" + NetUtil.getLocalhost().getHostAddress();
			if(StrKit.isBlank(defaultPort)){
				return url;
			}
			return url + ":" + defaultPort;
    	}

		return url;
    }

    /**
     * 鉴权是否拥有显示权限
     * @param dto
     * @return
     */
    public static boolean isAdPerm(AdminDTO dto, String perm){
    	if(StrKit.equals(dto.getUsername(), CommonAttribute.SUPER_ADMIN_NAME)){
    		return true;
    	}
    	return CollUtil.contains(dto.getPermissionList(), perm);
    }
    
    /**
     * 获取广告位图片
     * @param advType
     * @return
     */
    public static String getAdvImage(Integer advType){
    	return ExtensionAdv.dao.findExtensionAdvImage(advType);
    }
    
    
    /**
     * 清除缓存
     */
	public static void clearCache() {
		CacheKit.remove(CommonAttribute.CACHE_SYSTEM, CACHE_CONFIG_KEY);
	}
	
}
