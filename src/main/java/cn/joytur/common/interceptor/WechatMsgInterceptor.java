package cn.joytur.common.interceptor;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.render.TextRender;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.jfinal.AppIdParser;
import com.jfinal.weixin.sdk.jfinal.MsgController;
import com.jfinal.weixin.sdk.kit.SignatureCheckKit;

import cn.joytur.common.extensions.wechat.MsgParameterAppIdParser;
import cn.joytur.common.utils.JoyApiConfigUtil;
import cn.joytur.modules.wechat.entities.WechatSubscribe;

/**
 * Msg 拦截器
 * @author xuhang
 * @time 2019年1月17日 下午12:43:54
 */
public class WechatMsgInterceptor implements Interceptor {

	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(WechatMsgInterceptor.class);
	
	private static AppIdParser _parser = new MsgParameterAppIdParser();

    public static void setAppIdParser(AppIdParser parser) {
        _parser = parser;
    }

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        if (!(controller instanceof MsgController)) {
            throw new RuntimeException("控制器需要继承 MsgController");
        }
        
        try {
            String appId = _parser.getAppId(controller);
            // 将 appId 与当前线程绑定，以便在后续操作中方便获取ApiConfig对象： ApiConfigKit.getApiConfig();
            JoyApiConfigUtil.setThreadLocalAppId(appId);
            
            ApiConfig apiConfig = null;
            try{
            	//获取当前对象
            	apiConfig = JoyApiConfigUtil.getApiConfig();
            } catch (IllegalStateException e) {
            	LOGGER.info("重新加载库接入配置");
            	apiConfig = findDbAppConfig(appId);
            }
            
            LOGGER.debug("request appid={}", apiConfig.getAppId());

            // 如果是服务器配置请求，则配置服务器并返回
            if (isConfigServerRequest(controller)) {
                configServer(controller);
                return;
            }

            // 对开发测试更加友好
            if (JoyApiConfigUtil.isDevMode()) {
                inv.invoke();
            } else {
                // 签名检测
                if (checkSignature(controller)) {
                    inv.invoke();
                } else {
                    controller.renderText("签名验证失败，请确定是微信服务器在发送消息过来");
                }
            }

        } catch (Exception e){
        	LOGGER.error(e.getMessage(), e);
        	controller.render(new TextRender(""));
        } finally {
            JoyApiConfigUtil.removeThreadLocalAppId();
        }
    }

    /**
     * 检测签名
     */
    private boolean checkSignature(Controller controller) {
        String signature = controller.getPara("signature");
        String timestamp = controller.getPara("timestamp");
        String nonce = controller.getPara("nonce");
        if (StrKit.isBlank(signature) || StrKit.isBlank(timestamp) || StrKit.isBlank(nonce)) {
            controller.renderText("check signature failure");
            return false;
        }

        if (SignatureCheckKit.me.checkSignature(signature, timestamp, nonce)) {
            return true;
        } else {
        	LOGGER.error("check signature failure: " +
                    " signature = " + controller.getPara("signature") +
                    " timestamp = " + controller.getPara("timestamp") +
                    " nonce = " + controller.getPara("nonce"));

            return false;
        }
    }

    /**
     * 是否为开发者中心保存服务器配置的请求
     */
    private boolean isConfigServerRequest(Controller controller) {
        return StrKit.notBlank(controller.getPara("echostr"));
    }
    
    /**
     * 获取db配置公众号信息
     * @param appId
     * @return
     */
    private ApiConfig findDbAppConfig(String appId){
    	WechatSubscribe subscribe = WechatSubscribe.dao.findByModel(new WechatSubscribe().setAppId(appId));
    	if(subscribe == null){
    		throw new IllegalStateException("请先登陆后台管理界面->微信管理->公众号管理填写接入信息.");
    	}
    	ApiConfig tmpApiConfig = new ApiConfig();
    	tmpApiConfig.setAppId(appId);
    	tmpApiConfig.setAppSecret(subscribe.getAppSecret());
    	tmpApiConfig.setToken(subscribe.getToken());
    	if(StrKit.notBlank(subscribe.getEncodingAesKey())){
    		tmpApiConfig.setEncryptMessage(true);
    		tmpApiConfig.setEncodingAesKey(subscribe.getEncodingAesKey());
    	}
    	JoyApiConfigUtil.putApiConfig(tmpApiConfig, subscribe.getId(), String.valueOf(subscribe.getAppType()));
    	
		return tmpApiConfig;
    }

    /**
     * 配置开发者中心微信服务器所需的 url 与 token
     *
     * @param c 控制器
     */
    private void configServer(Controller c) {
        // 通过 echostr 判断请求是否为配置微信服务器回调所需的 url 与 token
        String echostr = c.getPara("echostr");
        String signature = c.getPara("signature");
        String timestamp = c.getPara("timestamp");
        String nonce = c.getPara("nonce");
        boolean isOk = SignatureCheckKit.me.checkSignature(signature, timestamp, nonce);
        if (isOk)
            c.renderText(echostr);
        else
        	LOGGER.error("验证失败：configServer");
    }
    
}
