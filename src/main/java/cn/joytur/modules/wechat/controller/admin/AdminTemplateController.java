package cn.joytur.modules.wechat.controller.admin;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.ActionType;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.wechat.entities.WechatSubscribe;
import cn.joytur.modules.wechat.entities.WechatTemplate;

/**
 * 微信模版管理
 * @author xuhang
 * @time 2019年6月27日 上午9:44:18
 */
@RouteMapping(url = "${admin}/template")
public class AdminTemplateController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("wechat.template.view")
	public void index(WechatTemplate wechatTemplate) {
		Page<WechatTemplate> pageList = WechatTemplate.dao.paginate(getPage(), getSize(), wechatTemplate, new Sort("event_type,create_time", Enums.SortType.DESC));
		
		Map<String, String> enumMap = new HashMap<String, String>();
		for(ActionType actionType : Enums.ActionType.values()){
			enumMap.put(actionType.getCode(), ActionType.getText(actionType.getCode()));
		}
		
		setAttr("page", pageList).setAttr("enumMap", enumMap);
		setAttr("wechatTemplate", wechatTemplate);
		renderTpl("wechat/template/index.html");
	}
	
	/**
	 * toAdd页面
	 */
	@AuthRequire.Perms("wechat.template.add")
	public void add(){
		renderTpl("wechat/template/add.html");
	}
	
	/**
	 * toEdit页面
	 */
	@AuthRequire.Perms("wechat.template.edit")
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	public void edit(String id){
		WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findById(id);
		
		setAttr("wechatTemplate", tmpWechatTemplate);
		renderTpl("wechat/template/add.html");
	}
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("wechat.template.save")
	@Valids({
		@Valid(name = "wechatTemplate.eventKeywords", required = true),
		@Valid(name = "wechatTemplate.responseType", required = true)
	})
	@Before(POST.class)
	public void save(WechatTemplate wechatTemplate){
    	WechatSubscribe tmpWechatSubscribe = WechatSubscribe.dao.findDefault();
    	
    	if(tmpWechatSubscribe == null){
    		throw new BusinessException(RenderResultCode.BUSINESS_254);
    	}
    		
		//新增
		if(StrUtil.isBlank(wechatTemplate.getId())) {
			WechatTemplate quyWechatTemplate = new WechatTemplate();
			quyWechatTemplate.setEventKeywords(wechatTemplate.getEventKeywords());
			
			WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findByModel(quyWechatTemplate);
			if(tmpWechatTemplate != null) {
				throw new BusinessException(RenderResultCode.BUSINESS_251);
			}
			
			//新增
			wechatTemplate.setId(IdUtil.fastSimpleUUID());
			wechatTemplate.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_CUSTOM, DictAttribute.TEMPLATE_EVENT_TYPE, "2"))); //用户自定义
			wechatTemplate.setSubscribeId(tmpWechatSubscribe.getId());
			wechatTemplate.setEventType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM, DictAttribute.TEMPLATE_EVENT_TYPE, "1")));
			wechatTemplate.setCreateTime(new java.util.Date());
			wechatTemplate.save();
			
		} else {
			//修改
			WechatTemplate existsWechatTemplate = WechatTemplate.dao.findById(wechatTemplate.getId());
			existsWechatTemplate.setEventKeywords(wechatTemplate.getEventKeywords());
			
			Long text = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "1"));
			Long img = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_IMAGE, DictAttribute.TEMPLATE_RESPONSE_TYPE, "2"));
			Long textimg = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_RESPONSE_TYPE_IMAGETEXT, DictAttribute.TEMPLATE_RESPONSE_TYPE, "3"));
			
			if(wechatTemplate.getResponseType() == text){
				existsWechatTemplate.setResponseText(wechatTemplate.getResponseText());
			}else if(wechatTemplate.getResponseType() == img){
				existsWechatTemplate.setResponsePicUrl(wechatTemplate.getResponsePicUrl());
			}else if(wechatTemplate.getResponseType() == textimg){
				existsWechatTemplate.setResponseArticleUrl(wechatTemplate.getResponseArticleUrl());
				existsWechatTemplate.setResponseDescription(wechatTemplate.getResponseDescription());
				existsWechatTemplate.setResponseTitle(wechatTemplate.getResponseTitle());
				existsWechatTemplate.setResponsePicUrl(wechatTemplate.getResponsePicUrl());
			}
			
			existsWechatTemplate.setUpdateTime(new java.util.Date());
			existsWechatTemplate.update();
		}
		
		renderJson(RenderResult.success());
	}
    
    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("wechat.template.delete")
	@Valids({
		@Valid(name = "ids", required = true, min = 32)
	})
    public void delete(){
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findById(id);
    		
    		if(StrKit.equals(JoyDictUtil.getDictValue(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM, DictAttribute.TEMPLATE_EVENT_TYPE,""), String.valueOf(tmpWechatTemplate.getEventType()))){
    			throw new BusinessException(RenderResultCode.BUSINESS_253);
    		}
    		//删除模版
    		WechatTemplate.dao.deleteById(id);
    	}
    	
		renderJson(RenderResult.success());
	}
    
    /**
     * 修改状态
     * @param ids
     */
    @AuthRequire.Perms("wechat.template.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status(){
    	Long status = getParaToLong();
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		//修改状态
    		WechatTemplate tmpWechatTemplate = WechatTemplate.dao.findById(id);
    		if(tmpWechatTemplate == null){
    			throw new BusinessException(RenderResultCode.BUSINESS_252);
    		}
    		tmpWechatTemplate.setStatus(status);
    		tmpWechatTemplate.setUpdateTime(new java.util.Date());
    		tmpWechatTemplate.update();
    	}
    	
    	renderJson(RenderResult.success());
    }
    
}