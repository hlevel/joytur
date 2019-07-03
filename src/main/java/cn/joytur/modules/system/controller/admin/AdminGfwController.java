package cn.joytur.modules.system.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.system.entities.SysGfw;

/**
 * 防封
 * @author xuhang
 */
@RouteMapping(url = "${admin}/gfw")
public class AdminGfwController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("sys.gfw.view")
	public void index(SysGfw sysGfw) {
		Page<SysGfw> pageList = SysGfw.dao.paginate(getPage(), getSize(), sysGfw, new Sort("create_time", Enums.SortType.ASC));
		setAttr("page", pageList);
		setAttr("sysGfw", sysGfw);
		renderTpl("system/gfw/index.html");
	}
	
	/**
	 * toAdd页面
	 */
	@AuthRequire.Perms("sys.gfw.add")
	public void add(){
		renderTpl("system/gfw/add.html");
	}
	
	/**
	 * toEdit页面
	 */
	@AuthRequire.Perms("sys.gfw.edit")
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	public void edit(String id){
		SysGfw tmpSysGfw = SysGfw.dao.findById(id);
		
		setAttr("sysGfw", tmpSysGfw);
		renderTpl("system/gfw/add.html");
	}
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("sys.gfw.save")
	@Valids({
		@Valid(name = "sysGfw.appName", required = true),
		@Valid(name = "sysGfw.appType", required = true),
		@Valid(name = "sysGfw.masterUrl", required = true),
		@Valid(name = "sysGfw.slaveUrl", required = true)
	})
	@Before({POST.class, Tx.class})
	public void save(SysGfw sysGfw){
		//新增
		if(StrUtil.isBlank(sysGfw.getId())) {
			SysGfw quySysGfw = new SysGfw();
			quySysGfw.setAppName(sysGfw.getAppName());
			
			SysGfw tmpSysGfw = SysGfw.dao.findByModel(quySysGfw);
			if(tmpSysGfw != null) {
				throw new BusinessException(RenderResultCode.BUSINESS_237);
			}
			
			//查询一个没有使用并且有效推荐码
			String newAcceptCode = null;
			int newCountWhile = 0;
			do{
				newAcceptCode = JoyIdUtil.toSerialCode();
				newCountWhile ++;
			}while(SysGfw.dao.findCountByModel(new SysGfw().setAppCode(newAcceptCode)) > 0 && newCountWhile < 10);
			
			//新增
			sysGfw.setId(IdUtil.fastSimpleUUID());
			sysGfw.setAppCode(newAcceptCode);
			sysGfw.setStatus(1L);
			sysGfw.setCreateTime(new java.util.Date());
			sysGfw.setUpdateTime(new java.util.Date());
			sysGfw.save();
			
		} else {
			//修改
			sysGfw.setUpdateTime(new java.util.Date());
			sysGfw.update();
		}
		
		//判断是否第三方
    	Long otherType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GFW_APP_TYPE2, DictAttribute.GFW_APP_TYPE, "2"));
		if(sysGfw.getAppType() == otherType){
			//第三方域名则同步
			if(StrKit.isBlank(sysGfw.getVarUrl())){
				throw new BusinessException(RenderResultCode.BUSINESS_240);
			}
			String synUrl = sysGfw.getSlaveUrl() + sysGfw.getVarUrl() + "/gfw/synGfw/";
			
			StringBuffer base64Buffer = new StringBuffer();
			base64Buffer.append(sysGfw.getAppCode()).append(",");
			base64Buffer.append(sysGfw.getMasterUrl()).append(",");
			base64Buffer.append(sysGfw.getSlaveUrl());
			
			synUrl += Base64.encode(base64Buffer.toString());
			
			String res = HttpUtil.get(synUrl);
			RenderResult<?> result = JsonKit.parse(res, RenderResult.class);
			if(!result.isSuccess()){
				throw new BusinessException(RenderResultCode.BUSINESS_239);
			}
		}
		
		renderJson(RenderResult.success());
	}
    
    /**
     * 修改状态
     * @param ids
     */
    @AuthRequire.Perms("sys.gfw.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status(){
    	Long status = getParaToLong();
    	String[] ids = getParaValues("ids");
    	for(String id : ids) {
    		SysGfw sysGfw = SysGfw.dao.findById(id);
    		if(sysGfw == null) {
        		throw new BusinessException(RenderResultCode.BUSINESS_238);
        	}
    		
    		String statusValue = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.STATUS, "1");
    		sysGfw.setStatus(Long.valueOf(JoyDictUtil.getDictValue(statusValue, DictAttribute.STATUS, "1")));
    		sysGfw.setUpdateTime(new java.util.Date());
    		sysGfw.update();
    	}
    	renderJson(RenderResult.success());
    }
    
    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("sys.gfw.delete")
	@Valids({
		@Valid(name = "ids", required = true, min = 32)
	})
    public void delete(){
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		//删除
    		SysGfw.dao.deleteById(id);
    	}
    	
		renderJson(RenderResult.success());
	}
    
    /**
	 * 同步防火墙
	 */
    @Clear
	public void synGfw(){
		String cipherText = getPara();
		if(StrKit.isBlank(cipherText)){
			renderJson(RenderResult.error()); return;
		}
		String cipher = Base64.decodeStr(cipherText);
		String[] ct = cipher.split(",");
		String appCode = ct[0];
		String masterUrl = ct[1];
		String slaveUrl = ct[2];
		
		//查询是否有主域名
		Long type = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GFW_APP_TYPE1, DictAttribute.GFW_APP_TYPE, "1"));
		Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1"));
		SysGfw tmpSysGfw = SysGfw.dao.findByModel(new SysGfw().setAppType(type));
		
		if(tmpSysGfw != null && tmpSysGfw.getStatus() == status){
			renderJson(RenderResult.error(RenderResultCode.BUSINESS_239)); return;
		}
		
		if(tmpSysGfw == null){
			//新增
			tmpSysGfw = new SysGfw();
			tmpSysGfw.setId(IdUtil.fastSimpleUUID());
			tmpSysGfw.setAppCode(appCode);
			tmpSysGfw.setMasterUrl(masterUrl);
			tmpSysGfw.setSlaveUrl(slaveUrl);
			tmpSysGfw.setStatus(0L); //禁用状态
			tmpSysGfw.setCreateTime(new java.util.Date());
			tmpSysGfw.setUpdateTime(new java.util.Date());
			tmpSysGfw.save();
		}else{
			tmpSysGfw.setAppCode(appCode);
			tmpSysGfw.setMasterUrl(masterUrl);
			tmpSysGfw.setSlaveUrl(slaveUrl);
			tmpSysGfw.setStatus(0L); //禁用状态
			tmpSysGfw.setUpdateTime(new java.util.Date());
			tmpSysGfw.update();
		}
		
		renderJson(RenderResult.success());
	}
    
}