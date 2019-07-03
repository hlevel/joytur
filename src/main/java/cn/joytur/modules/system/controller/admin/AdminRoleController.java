package cn.joytur.modules.system.controller.admin;

import java.util.List;

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
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.modules.system.entities.SysMenu;
import cn.joytur.modules.system.entities.SysRole;
import cn.joytur.modules.system.entities.SysRoleMenu;
import cn.joytur.modules.system.entities.SysUser;
import cn.joytur.modules.system.entities.SysUserRole;

/**
 * <p>
 *  角色管理
 * </p>
 * @since 2019/1/8
 */
@RouteMapping(url = "${admin}/role")
public class AdminRoleController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("sys.role.view")
    public void index(SysRole sysRole) {
        Page<SysRole> pageList = SysRole.dao.paginate(getPage(), getSize(), sysRole);

        setAttr("page", pageList);
        setAttr("sysRole", sysRole);
        renderTpl("system/role/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("sys.role.add")
    public void add(){
    	renderTpl("system/role/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("sys.role.edit")
    @Valids({
    	@Valid(name = "id", required = true, max=32 , min=32)
    })
    public void edit(String id){
    	setAttr("sysRole", SysRole.dao.findById(id));
    	renderTpl("system/role/add.html");
    }

    /**
     * 保存
     */
    @AuthRequire.Perms("sys.role.save")
    @Valids({
    	@Valid(name = "sysRole.roleCode", required = true), 
    	@Valid(name = "sysRole.roleName", required = true)
    })
    @Before(POST.class)
    public void save(SysRole sysRole){
    	//新增
		if(StrUtil.isBlank(sysRole.getId())) {
			SysRole quySysRole = new SysRole();
			quySysRole.setRoleName(sysRole.getRoleName());
			quySysRole.setRoleCode(sysRole.getRoleCode());
			
			SysRole tmpSysRole = SysRole.dao.findByModel(quySysRole);
			if(tmpSysRole != null) {
				throw new BusinessException(RenderResultCode.BUSINESS_234);
			}
			
			//新增
			sysRole.setId(IdUtil.fastSimpleUUID());
			sysRole.setCreateTime(new java.util.Date());
			sysRole.setUpdateTime(new java.util.Date());
			sysRole.save();
		}else{
			//修改
			sysRole.setUpdateTime(new java.util.Date()).update();
		}
		renderJson(RenderResult.success());
    }

    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("sys.role.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void delete(){
    	String[] ids = getParaValues("ids");
    	for(String id : ids) {
    		//查询角色是否被用户引用
    		SysUserRole quySysUserRole = new SysUserRole();
    		quySysUserRole.setRoleId(id);
    		SysUserRole tmpSysUserRole = SysUserRole.dao.findByModel(quySysUserRole);
    		if(tmpSysUserRole != null){
    			throw new BusinessException(RenderResultCode.BUSINESS_235);
    		}
    		
    		SysRoleMenu.dao.deteleByModel(new SysRoleMenu().setRoleId(id));
    		SysRole.dao.deleteById(id);
    	}
    	renderJson(RenderResult.success());
    }
    
    /**
     * 跳转到授权页面
     */
    @AuthRequire.Perms("sys.role.auth")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void toAuth(String ids) {
    	setAttr("id", ids);
    	renderTpl("system/role/auth.html");
    }
    
    /**
     * 获取权限列表
     * @param id
     */
    @AuthRequire.Perms("sys.role.auth")
    @Valids({
        @Valid(name = "id", required = true, min = 32)
    })
    public void authList(String id){
    	//先获取已经对应角色的菜单
    	List<SysRoleMenu> roleMenuList = SysRoleMenu.dao.findList(new SysRoleMenu().setRoleId(id));
    	
    	//获取所有菜单
    	List<SysMenu> menulist = SysMenu.dao.findAll(new Sort("sort", Enums.SortType.ASC));
    	
    	//标记已经选项
    	for(SysMenu menu : menulist){
    		menu.setDescription("");
    		for(SysRoleMenu m : roleMenuList){
    			if(StrKit.equals(menu.getId(), m.getMenuId())) {
    				menu.setDescription("auth:true");
    				break;
    			}
    		}
    	}
    	
    	renderJson(RenderResult.success(menulist));
    }
    
    /**
     * 保存授权数据
     * @param id
     */
    @AuthRequire.Perms("sys.role.auth")
    @Valids({
        @Valid(name = "id", required = true, min = 32),
        @Valid(name = "authId", required = true, min = 32)
    })
    @Before({POST.class, Tx.class})
    public void auth(String id){
    	String[] authIds = getParaValues("authId");
    	//先删除
    	SysRoleMenu.dao.deteleByModel(new SysRoleMenu().setRoleId(id));
    	//再添加
    	for(String authId : authIds) {
    		new SysRoleMenu().setMenuId(authId).setRoleId(id).setCreateTime(new java.util.Date()).save();
    	}
    	
    	renderJson(RenderResult.success());
    }
    
    /**
     * 跳转到用户列表
     * @param id
     */
    @AuthRequire.Perms("sys.role.view")
    @Valids({
    	@Valid(name = "id", required = true, min = 32)
    })
    public void userList(String id){
    	List<SysUser> userList = SysUser.dao.findByRoleId(id);
    	setAttr("userList", userList);
    	renderTpl("system/role/user_list.html");
    }
    
}
