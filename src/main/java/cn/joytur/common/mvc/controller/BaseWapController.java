package cn.joytur.common.mvc.controller;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.JsTicket;
import com.jfinal.weixin.sdk.api.JsTicketApi;
import com.jfinal.weixin.sdk.api.JsTicketApi.JsApiType;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JWTUtil;
import cn.joytur.common.utils.JoyApiConfigUtil;


/**
 * 基础类
 * @author xuhang
 * @time 2018年7月31日 下午10:14:09
 */
public class BaseWapController extends Controller {

	//protected final static Log LOGGER = Log.getLog(BaseAdminController.class);
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseWapController.class);
	
	protected final static Integer DEF_PAGE = 1; //默认页数
	protected final static Integer DEF_SIZE = 10; //默认页条数
	
	/**
	 * 获取当前页
	 * @return
	 */
	protected Integer getPage() {
		String search = getSearch("page", "size");
		setAttr("pageUrl", getRequest().getServletPath() + (StrKit.isBlank(search) ? "?" : "&"));
		return getParaToInt("page", DEF_PAGE);
	}

	/**
	 *  获取页面大小
	 * @return
	 */
	protected Integer getSize() {
		return getParaToInt("size", DEF_SIZE);
	}
	
	/**
	 * 获取用户
	 * @return
	 */
	protected WapMemberDTO getWapMemberDTO(){
		WapMemberDTO memberDTO = JWTUtil.getWapMemberDTO(this);
		return memberDTO;
	}
	
	/**
	 * 实现转换dto
	 * @param DataModelClass
	 * @return
	 
	protected <T> T getDataModelDTO(Class<T> DataModelClass){
		Field[] fields = ReflectUtil.getFields(DataModelClass);
		
		for(Field field : fields){
			System.out.println(field.getName());
			//ReflectUtil.invoke(obj, method, args);
		}
		
		return null;
	}*/
	
	/**
	 * 获取搜索条件
	 * @return
	 */
	protected String getSearch(){
		return getSearch("");
	}
	
	/**
	 * 获取搜索条件
	 * @return
	 */
	protected String getSearch(String... ignoreValue){
		StringBuilder builder = new StringBuilder();
		
		boolean isFirst = true;
		Enumeration<String> ePara = getParaNames();
		while(ePara.hasMoreElements()){
			String name = ePara.nextElement();
			
			if(ArrayUtil.contains(ignoreValue, name)){	//忽略掉搜索条件
				continue;
			}
			
			String value = getPara(name);
			if(StrKit.notBlank(getPara(name))){
				builder.append(isFirst ? "?" : "&").append(name).append("=").append(value);
				isFirst = false;
			}
		}
		return builder.toString();
	}
	
	/**
	 * 获取全戳
	 * @return
	 */
	private String getPrefix(){
		return "/templates/" + PropKit.get(CommonAttribute.SYSTEM_THEME);
	}
	
	/**
	 * 输出wap端html
	 * @param wapPath
	 */
	protected void renderWap(String wapPath){
		//setJsSdk();
		render(getPrefix() + "/wap/" + wapPath);
	}
	
	/**
	 * jssdk设置
	 */
	private void setJsSdk(){
		String appid = JoyApiConfigUtil.existsDefault().getAppId();
		
		JsTicket jsApiTicket = JsTicketApi.getTicket(JsApiType.jsapi);
		String jsapi_ticket = jsApiTicket.getTicket();
		String nonce_str = IdUtil.simpleUUID();
		// 注意 URL 一定要动态获取，不能 hardcode.
		String url = "http://" + getRequest().getServerName() // 服务器地址
				// + ":"
				// + getRequest().getServerPort() //端口号
				+ getRequest().getContextPath() // 项目名称
				+ getRequest().getServletPath();// 请求页面或其他地址
		
		String qs = getRequest().getQueryString(); // 参数
		if (qs != null) {
			url = url + "?" + (getRequest().getQueryString());
		}
		
		String timestamp = Long.toString(System.currentTimeMillis() / 1000);
		// 这里参数的顺序要按照 key 值 ASCII 码升序排序
		//注意这里参数名必须全部小写，且必须有序
		String  str = "jsapi_ticket=" + jsapi_ticket +
        "&noncestr=" + nonce_str +
        "&timestamp=" + timestamp +
        "&url=" + url;

		String signature = HashKit.sha1(str);
		
		LOGGER.info("url=" + url);
		LOGGER.info("appId=" + ApiConfigKit.getApiConfig().getAppId() + ",nonceStr=" + nonce_str + ",timestamp=" + timestamp);
		LOGGER.info("url=" + url + ",signature=" + signature);
		LOGGER.info("nonceStr=" + nonce_str + ",timestamp=" + timestamp);
		LOGGER.info("jsapi_ticket=" + jsapi_ticket);
		LOGGER.info("nonce_str=" + nonce_str);
		
		//处理jssdk
		JSONArray jsApiListJSON = new JSONArray();
		jsApiListJSON.add("onMenuShareAppMessage");
		jsApiListJSON.add("onMenuShareTimeline");
		jsApiListJSON.add("openAddress");
		jsApiListJSON.add("chooseWXPay");
		jsApiListJSON.add("chooseImage");
		jsApiListJSON.add("uploadImage");
		
		
		JSONObject jssdkJSON = new JSONObject();
		jssdkJSON.put("debug", String.valueOf(PropKit.getBoolean("system.devMode", false)));
		jssdkJSON.put("appId", appid);
		jssdkJSON.put("timestamp", timestamp);
		jssdkJSON.put("nonceStr", nonce_str);
		jssdkJSON.put("signature", signature);
		jssdkJSON.put("jsApiList", jsApiListJSON);
		jssdkJSON.put("jsapi_ticket", jsapi_ticket);
		jssdkJSON.put("url", url);
		
		setAttr("jssdkConfig", jssdkJSON.toJSONString());
	}
	
}
