package cn.joytur.modules.system.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;

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
import cn.joytur.common.utils.JoyMenuUtil;
import cn.joytur.modules.system.entities.SysMenu;
import cn.joytur.modules.system.entities.SysRoleMenu;

/**
 * 菜单管理 
 * @author xuhang
 */
@RouteMapping(url = "${admin}/menu")
public class AdminMenuController extends BaseAdminController {

	/**
	 * 列表
	 */
	public void index(SysMenu sysMenu) {
		setAttr("search", getSearch());
		renderTpl("system/menu/index.html");
	}
	
	/**
	 * 数据list
	 */
	public void list(SysMenu sysMenu){
		List<SysMenu> sysAuthorityList = SysMenu.dao.findList(sysMenu, new Sort("sort", Enums.SortType.ASC));
		renderJson(RenderResult.success(sysAuthorityList));
	}
	
	/**
	 * toAdd页面
	 */
	@Valids({
		@Valid(name = "pid", required = false, max=32 , min=32)
	})
	public void add(String pid){
		
		if(StrUtil.isNotBlank(pid)) {
			SysMenu tmpSysMenu = SysMenu.dao.findById(pid);
			setAttr("pSysMenu", tmpSysMenu);
		}
		
		renderTpl("system/menu/add.html");
	}
	
	/**
	 * toEdit页面
	 */
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	public void edit(String id){
		SysMenu tmpSysMenu = SysMenu.dao.findById(id);
		
		if(tmpSysMenu != null){
			SysMenu parentSysMenu = SysMenu.dao.findById(tmpSysMenu.getPid());
			setAttr("pSysMenu", parentSysMenu);
		}
		
		setAttr("sysMenu", tmpSysMenu);
		renderTpl("system/menu/add.html");
	}
	
	/**
     * 获取排序菜单列表
     */
    @Valids({
		@Valid(name = "pid", required = false, min=32, max=32),
		@Valid(name = "notId", required = false, min=32, max=32)
	})
    public void sortList(String pid, String notId){
        // 本级排序菜单列表
        notId = notId != null ? notId : "0";
        
        List<SysMenu> levelMenu = SysMenu.dao.findByPidAndIdNot(pid, notId);
        
        Map<String, String> sortMap = new TreeMap<>();
        for (int i = 1; i <= levelMenu.size(); i++) {
            sortMap.put(""+i, levelMenu.get(i - 1).getTitle());
        }
        
        renderJson(sortMap);
    }
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("sys.menu.save")
	@Valids({
		@Valid(name = "sysMenu.title", required = true)
	})
	@Before(POST.class)
	public void save(SysMenu sysMenu){
		//新增
		if(StrUtil.isBlank(sysMenu.getId())) {
			SysMenu quySysMenu = new SysMenu();
			quySysMenu.setTitle(sysMenu.getTitle());
			quySysMenu.setUrl(sysMenu.getUrl());
			quySysMenu.setPermission(sysMenu.getPermission());
			
			SysMenu tmpSysAuthority = SysMenu.dao.findByModel(quySysMenu);
			if(tmpSysAuthority != null) {
				throw new BusinessException(RenderResultCode.BUSINESS_230);
			}
			
			if(sysMenu.getSort() == null) {
				Long maxSort = SysMenu.dao.findMaxSort(sysMenu.getPid());
				sysMenu.setSort(maxSort == null ? 0 : maxSort+1);
			}
			
			//更新path路径
			if(!StrKit.equals(sysMenu.getPid(), "0")){
				SysMenu parentMenu = SysMenu.dao.findByModel(new SysMenu().setId(sysMenu.getPid()));
				sysMenu.setPath(parentMenu.getPath() + "," + parentMenu.getId());
			}
			
			//新增
			sysMenu.setId(IdUtil.fastSimpleUUID());
			sysMenu.setCreateTime(new java.util.Date());
			sysMenu.save();
			
		} else {
			//更新path路径
			if(!StrKit.equals(sysMenu.getPid(), "0")){
				SysMenu parentMenu = SysMenu.dao.findByModel(new SysMenu().setId(sysMenu.getPid()));
				sysMenu.setPath(parentMenu.getPath() + "," + parentMenu.getId());
			}
			
			//修改
			sysMenu.update();
		}
		
		//重新更新排序
		List<SysMenu> existSysMenuList = SysMenu.dao.findByPidAndIdNot(sysMenu.getPid(), sysMenu.getId());
		existSysMenuList.add(sysMenu.getSort().intValue(), sysMenu);
		for(int i = 0; i < existSysMenuList.size(); i++){
			SysMenu.dao.updateSort(existSysMenuList.get(i).getId(), new Long(i));
		}
		
		//清空缓存
		JoyMenuUtil.clearCache();
		
		renderJson(RenderResult.success());
	}
    
    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("sys.menu.delete")
	@Valids({
		@Valid(name = "ids", required = true, min = 32)
	})
    @Before(Tx.class)
    public void delete(){
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		//查询是否存在被引用
    		SysMenu quySysMenu = new SysMenu();
    		quySysMenu.setPid(id);
    		SysMenu tmpSysMenu = SysMenu.dao.findByModel(quySysMenu);
    		if(tmpSysMenu != null) {
    			throw new BusinessException(RenderResultCode.BUSINESS_231);
    		}
    		
    		//删除关系
    		SysRoleMenu.dao.deteleByModel(new SysRoleMenu().setMenuId(id));
    		//删除菜单
    		SysMenu.dao.deleteById(id);
    	}
    	
		renderJson(RenderResult.success());
	}
    
}