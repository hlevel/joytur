package cn.joytur.modules.system.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import cn.hutool.core.util.IdUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.UploadBean;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.system.entities.SysConfig;

/**
 * 系统配置
 * @author xuhang
 */
@RouteMapping(url = "${admin}/config")
public class AdminConfigController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("sys.config.view")
	public void index() {
		List<SysConfig> sysConfigList = SysConfig.dao.findAll();
		Map<String, String> configMap = sysConfigList.stream().collect(Collectors.toMap(SysConfig::getName, SysConfig::getValue));
		
		setAttr("config", configMap);
		renderTpl("system/config/index.html");
	}
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("sys.config.save")
	@Before({POST.class, Tx.class})
	public void save(){
    	
    	for (Enums.SysConfigType sysConfigType : Enums.SysConfigType.values()) {
    		String paraValue = getPara(sysConfigType.name());
    		if(StrKit.notBlank(paraValue)){
    			SysConfig sysConfig = SysConfig.dao.findByName(sysConfigType.name());
    			if(sysConfig == null) {
    				sysConfig = new SysConfig();
    				sysConfig.setId(IdUtil.simpleUUID()).setName(sysConfigType.name()).setValue(paraValue).setCreateTime(new java.util.Date()).setUpdateTime(new java.util.Date()).save();
    			} else if(!StrKit.equals(paraValue, sysConfig.getValue())){
    				sysConfig.setValue(paraValue).setUpdateTime(new java.util.Date()).update();
    			}
    		}
    	}
    	
    	//如果存在更改则oss则清除缓存
    	if(getParaMap().containsKey(Enums.SysConfigType.SYS_CONFIG_TENCENT_BUCKETNAME.name())){
    		JoyUploadFileUtil.clearCache();
    	}
    	
    	//判断是否保存歇业界面
    	if(getParaMap().containsKey(Enums.SysConfigType.SYS_CLOSING_TIME.name())){
    		String paraValue = getPara(Enums.SysConfigType.SYS_CLOSING_SWITCH.name());
    		if(StrKit.isBlank(paraValue)){
    			SysConfig.dao.deteleByModel(new SysConfig().setName(Enums.SysConfigType.SYS_CLOSING_SWITCH.name()));
    		}else{
    			//处理保存on字符串
    			SysConfig updateSysConfig = new SysConfig();
    			updateSysConfig.setValue(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1"));
    			SysConfig.dao.updateModelByModel(updateSysConfig, new SysConfig().setName(Enums.SysConfigType.SYS_CLOSING_SWITCH.name()));
    		}
    	}
    	
    	//充值满额送开关处理 ,现在已经废弃
    	/*
    	String paraValue = getPara(Enums.SysConfigType.SYS_GIFT_PACKED.name());
    	if(StrKit.isBlank(paraValue)){
    		SysConfig.dao.deteleByModel(new SysConfig().setName(Enums.SysConfigType.SYS_GIFT_PACKED.name()));
    	}else{
    		//处理保存on字符串
			SysConfig updateSysConfig = new SysConfig();
			updateSysConfig.setValue(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1"));
			SysConfig.dao.updateModelByModel(updateSysConfig, new SysConfig().setName(Enums.SysConfigType.SYS_GIFT_PACKED.name()));
    	}
		*/
    	//清除配置
    	JoyConfigUtil.clearCache(); //清除缓存
    	
		renderJson(RenderResult.success());
	}
    
    /**
     * 上传
     */
    public void upload(){
        List<String> fileNames = new ArrayList<>();
        List<UploadFile> files = getFiles();
        files.forEach(uploadFile -> {
            String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile);
            fileNames.add(headimgUrl);
        });

        UploadBean<List<String>> bean = new UploadBean<List<String>>();
        bean.setErrno(0);
        bean.setData(fileNames);
        renderJson(bean);
    }
    
}