package cn.joytur.modules.system.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.system.entities.SysDictionary;

@RouteMapping(url = "${admin}/dict")
public class AdminDictController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("sys.dict.view")
	public void index(SysDictionary sysDictionary) {
		Page<SysDictionary> pageList = SysDictionary.dao.paginate(getPage(), getSize(), sysDictionary, new Sort("dict_code,sort", Enums.SortType.ASC));
		setAttr("page", pageList);
		setAttr("sysDictionary", sysDictionary);
		renderTpl("system/dict/index.html");
	}
	
	/**
	 * toAdd页面
	 */
	@AuthRequire.Perms("sys.dict.add")
	@Valids({
		@Valid(name = "dictCode", required = false, max=32)
	})
	public void add(String dictCode){
		
		if(StrUtil.isNotBlank(dictCode)){
			SysDictionary tipSysDictionary = new SysDictionary();
			tipSysDictionary.setDictCode(dictCode);
			setAttr("sysDictionary", tipSysDictionary);
		}
		
		renderTpl("system/dict/add.html");
	}
	
	/**
	 * toEdit页面
	 */
	@AuthRequire.Perms("sys.dict.edit")
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	public void edit(String id){
		SysDictionary tmpSysDictionary = SysDictionary.dao.findById(id);
		
		setAttr("sysDictionary", tmpSysDictionary);
		renderTpl("system/dict/add.html");
	}
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("sys.dict.save")
	@Valids({
		@Valid(name = "sysDictionary.dictCode", required = true),
		@Valid(name = "sysDictionary.dictName", required = true),
		@Valid(name = "sysDictionary.dictValue", required = true)
	})
	@Before(POST.class)
	public void save(SysDictionary sysDictionary){
		//新增
		if(StrUtil.isBlank(sysDictionary.getId())) {
			SysDictionary quySysDictionary = new SysDictionary();
			quySysDictionary.setDictCode(sysDictionary.getDictCode());
			quySysDictionary.setDictName(sysDictionary.getDictName());
			quySysDictionary.setDictValue(sysDictionary.getDictValue());
			
			SysDictionary tmpSysDictionary = SysDictionary.dao.findByModel(quySysDictionary);
			if(tmpSysDictionary != null) {
				throw new BusinessException(RenderResultCode.BUSINESS_232);
			}
			
			//新增
			sysDictionary.setId(IdUtil.fastSimpleUUID());
			sysDictionary.setCreateTime(new java.util.Date());
			sysDictionary.save();
			
		} else {
			//修改
			sysDictionary.update();
		}
		
		//清除缓存
		JoyDictUtil.clearCache();
		
		renderJson(RenderResult.success());
	}
    
    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("sys.dict.delete")
	@Valids({
		@Valid(name = "ids", required = true, min = 32)
	})
    public void delete(){
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		//删除字典
    		SysDictionary.dao.deleteById(id);
    	}
    	
		renderJson(RenderResult.success());
	}
    
}