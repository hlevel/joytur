package cn.joytur.modules.wechat.controller.admin;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.MenuApi;

import cn.hutool.core.util.IdUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.utils.JoyApiConfigUtil;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.wechat.entities.WechatSubscribe;
import cn.joytur.modules.wechat.entities.WechatTemplate;
import cn.joytur.modules.wechat.service.WechatTemplateService;

/**
 * 公众号管理
 * @author xuhang
 * @time 2019年1月15日 下午4:23:36
 */
@RouteMapping(url = "${admin}/subscribe")
public class AdminSubscribeController extends BaseAdminController {

	@Inject
	private WechatTemplateService wechatTemplateService;    // 此处会注入依赖对象
	
	/**
	 * 列表
	 */
	@AuthRequire.Perms("wechat.subscribe.view")
	public void index() {
		WechatSubscribe wechatSubscribe = WechatSubscribe.dao.findDefault();
		
		//查询url
		String domain = JoyConfigUtil.getSdUrl();
		
		if(StrKit.notBlank(domain) && domain.startsWith("http://")){
			setAttr("domain", domain);
		}
		
		String cusType = JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_CUSTOM, DictAttribute.TEMPLATE_EVENT_TYPE, "2");
		
		if(wechatSubscribe != null) {
			List<WechatTemplate> wechatTemplateList = WechatTemplate.dao.findList(new WechatTemplate().setSubscribeId(wechatSubscribe.getId()).setEventType(Long.valueOf(cusType)));
			setAttr("wechatTemplateList", wechatTemplateList);
			
			if(StrKit.isBlank(wechatSubscribe.getMenuData()) && StrKit.notBlank(wechatSubscribe.getAppId(), wechatSubscribe.getAppSecret(), wechatSubscribe.getToken())) {
				String menuData = JoyApiConfigUtil.synMenu(wechatSubscribe.getAppId(), wechatSubscribe.getAppSecret(), wechatSubscribe.getToken());
				wechatSubscribe.setMenuData(menuData);
			}
		}
		
		setAttr("wechatSubscribe", wechatSubscribe);
		renderTpl("wechat/subscribe/index.html");
	}
	
	/**
	 * json list
	 * @param wechatTemplate
	 */
	@AuthRequire.Perms("wechat.subscribe.view")
	public void templateList(WechatTemplate wechatTemplate) {
		
		renderTpl("wechat/subscribe/template_list.html");
	}
	
    /**
     * 菜单数据
     */
    @AuthRequire.Perms("wechat.subscribe.menuSave")
    public void menuSave() {
    	String id = getPara("id");
    	String menuData = getPara("menuData");
    	
    	WechatSubscribe tmpWechatSubscribe = WechatSubscribe.dao.findById(id);
    	if(tmpWechatSubscribe == null){
    		throw new BusinessException(RenderResultCode.BUSINESS_249);
    	}
    	
    	WechatSubscribe updateWechatSubscribe = new WechatSubscribe();
    	updateWechatSubscribe.setMenuData(menuData);
    	updateWechatSubscribe.setUpdateTime(new java.util.Date());
    	
    	WechatSubscribe.dao.updateModelById(updateWechatSubscribe, id);
    	
    	//发布菜单
		ApiConfig apiConfig = new ApiConfig(tmpWechatSubscribe.getToken(), tmpWechatSubscribe.getAppId(), tmpWechatSubscribe.getAppSecret());
		JoyApiConfigUtil.putApiConfig(apiConfig, tmpWechatSubscribe.getId(), String.valueOf(tmpWechatSubscribe.getAppType()));
		
		JSONObject menuJSON = JSONObject.parseObject(menuData);
		ApiResult apiResult = MenuApi.createMenu(menuJSON.getString("menu"));
		if (apiResult.isSucceed()){
			LOGGER.info("[{}]发布并更新菜单成功:{}",updateWechatSubscribe.getId(), apiResult.getJson());
			renderJson(RenderResult.success());
		}else{
			LOGGER.info("[{}]发布并更新菜单失败:{}", updateWechatSubscribe.getId(), apiResult.getErrorMsg());
			renderJson(RenderResult.error());
		}
    }
	
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("wechat.subscribe.save")
	@Valids({
		@Valid(name = "wechatSubscribe.appName", desc="公众号名称", required = true),
		@Valid(name = "wechatSubscribe.appCode", required = true),
		@Valid(name = "wechatSubscribe.appId", required = true),
		@Valid(name = "wechatSubscribe.appSecret", required = true),
		@Valid(name = "wechatSubscribe.appType", required = true),
		@Valid(name = "wechatSubscribe.token", required = true)
	})
	@Before({POST.class, Tx.class})
	public void save(WechatSubscribe wechatSubscribe){
		//修改
    	WechatSubscribe tmpWechatSubscribe = WechatSubscribe.dao.findDefault();
    	if(tmpWechatSubscribe == null){
    		wechatSubscribe.setId(IdUtil.simpleUUID());
    		wechatSubscribe.setStatus(1L);
    		wechatSubscribe.setMenuData(JoyApiConfigUtil.synMenu(wechatSubscribe.getAppId(), wechatSubscribe.getAppSecret(), wechatSubscribe.getToken()));
    		wechatSubscribe.setCreateTime(new java.util.Date());
    		wechatSubscribe.setUpdateTime(new java.util.Date());
    		wechatSubscribe.save();
    		
    		wechatTemplateService.initWechatTemplate(wechatSubscribe.getId());
    	}else{
    		wechatSubscribe.setId(tmpWechatSubscribe.getId());
    		wechatSubscribe.setUpdateTime(new java.util.Date());
    		wechatSubscribe.update();
    		
    		//清除配置
    		JoyApiConfigUtil.removeApiConfigByAppId(wechatSubscribe.getAppId());
    	}
    	
    	ApiConfig apiConfig = new ApiConfig(wechatSubscribe.getToken(), wechatSubscribe.getAppId(), wechatSubscribe.getAppSecret());
    	JoyApiConfigUtil.putApiConfig(apiConfig, wechatSubscribe.getId(), String.valueOf(wechatSubscribe.getAppType()));
    	
		renderJson(RenderResult.success(RenderResultCode.BUSINESS_250));
	}
    
    /**
     * wap页面二维码
     */
    public void wapQrCode(){
    	String domain = JoyConfigUtil.getSdUrl(null, String.valueOf(getRequest().getLocalPort()));

    	if(StrKit.isBlank(domain)){
    		domain = "请进入系统管理-系统参数-网站域名-配置完成后刷新再试";
    	}else{
    		domain += "/wap";
    	}
    	
    	renderQrCode(domain, 200, 200);
    }
    
}