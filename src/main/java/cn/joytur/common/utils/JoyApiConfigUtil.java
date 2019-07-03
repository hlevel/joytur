package cn.joytur.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.MenuApi;

import cn.joytur.modules.wechat.entities.WechatSubscribe;

/**
 * 扩展api微信公众号支持
 * @author xuhang
 */
public class JoyApiConfigUtil extends ApiConfigKit{

	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(JoyApiConfigUtil.class);
	
	private static final Map<String, String> CFG_TYPE_MAP = new ConcurrentHashMap<String, String>();
	private static final Map<String, String> CFG_ID_MAP = new ConcurrentHashMap<String, String>();
	
	/**
	 * 缓存当前配置
	 * @param apiConfig
	 * @param subscribeId
	 * @param appType
	 * @return
	 */
	public static ApiConfig putApiConfig(ApiConfig apiConfig, String subscribeId, String appType) {
		CFG_ID_MAP.put(apiConfig.getAppId(), subscribeId);
		CFG_TYPE_MAP.put(apiConfig.getAppId(), appType);
		
		return putApiConfig(apiConfig);
    }
	
	/**
	 * 获取公众号配置 如果没有则去库加载默认
	 * @return
	 */
	public static ApiConfig existsDefault(){
		WechatSubscribe tmpWechatSubscribe = WechatSubscribe.dao.findDefault();
		ApiConfig apiConfig = new ApiConfig(tmpWechatSubscribe.getToken(), tmpWechatSubscribe.getAppId(), tmpWechatSubscribe.getAppSecret());
		JoyApiConfigUtil.putApiConfig(apiConfig, tmpWechatSubscribe.getId(), String.valueOf(tmpWechatSubscribe.getAppType()));
		return getApiConfig(tmpWechatSubscribe.getAppId());
	}
	
	/**
	 * 获取配置类型
	 * @param appId
	 * @return
	 */
	public static String getAppType(String appId) {
		LOGGER.debug("appId: " + appId);
        String cfgType = CFG_TYPE_MAP.get(appId);
        if (cfgType == null)
            throw new IllegalStateException("未找到公众号对应类型,请检查配置是否正确.");
        return cfgType;
    }
	
	/**
	 * 获取当前线程类型配置
	 * @return
	 */
	public static String getAppType(){
		return getAppType(ApiConfigKit.getAppId());
	}
	
	/**
	 * 获取配置id
	 * @param appId
	 * @return
	 */
	public static String getSubscribeId(String appId) {
		LOGGER.debug("appId: " + appId);
        String cfgId = CFG_ID_MAP.get(appId);
        if (cfgId == null)
            throw new IllegalStateException("未找到公众号对应类型Id,请检查配置是否正确.");
        return cfgId;
    }
	
	/**
	 * 获取当前线程类型配置Id
	 * @return
	 */
	public static String getSubscribeId(){
		return getSubscribeId(ApiConfigKit.getAppId());
	}
	
	/**
	 * 清除配置
	 * @param appId
	 */
	public static void removeApiConfigByAppId(String appId) {
		ApiConfigKit.removeApiConfig(appId);
		CFG_ID_MAP.remove(appId);
		CFG_TYPE_MAP.remove(appId);
    }
	
	/**
	 * 同步菜单
	 * @param tmpWechatSubscribe
	 * @return
	 */
	public static String synMenu(String appId, String appSecret, String token){
		if(StrKit.isBlank(appId) || StrKit.isBlank(appSecret) || StrKit.isBlank(token)){
			return null;
		}
		//自动同步当前weixin菜单
		ApiConfig apiConfig = new ApiConfig(token, appId, appSecret);
		putApiConfig(apiConfig);
		ApiResult apiResult = MenuApi.getMenu();
		if (apiResult.isSucceed()){
			return apiResult.getJson();
		}
		return null;
	}
	
}
