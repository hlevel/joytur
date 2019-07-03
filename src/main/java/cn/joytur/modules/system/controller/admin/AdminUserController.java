package cn.joytur.modules.system.controller.admin;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.system.entities.SysRole;
import cn.joytur.modules.system.entities.SysUser;
import cn.joytur.modules.system.entities.SysUserRole;

/**
 *  用户管理
 *  @author xuhang
 */
@RouteMapping(url = "${admin}/user")
public class AdminUserController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("sys.user.view")
    public void index(SysUser sysUser) {
        Page<SysUser> pageList = SysUser.dao.paginate(getPage(), getSize(), sysUser);

        setAttr("page", pageList);
        setAttr("sysUser", sysUser);
        renderTpl("system/user/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("sys.user.add")
    public void add(){
    	renderTpl("system/user/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("sys.user.edit")
    @Valids({
    	@Valid(name = "id", required = true, max=32 , min=32)
    })
    public void edit(String id){
    	setAttr("sysUser", SysUser.dao.findById(id));
    	renderTpl("system/user/add.html");
    }

    /**
     * 保存
     */
    @AuthRequire.Perms("sys.user.save")
    @Valids({
    	@Valid(name = "sysUser.nickName", required = true),
    	@Valid(name = "sysUser.email", required = true)
    })
    @Before(POST.class)
    public void save(SysUser sysUser){
    	if (StrKit.equals(CommonAttribute.SUPER_ADMIN_NAME, sysUser.getUserName())) {
    		throw new BusinessException(RenderResultCode.BUSINESS_218);
    	}
    	
    	//新增
    	String rePassword = getPara("sysUser.confirm");
		if(StrUtil.isBlank(sysUser.getId())) {
			//新增用户名
			if(StrKit.isBlank(sysUser.getUserName())){
				throw new BusinessException(RenderResultCode.BUSINESS_217);
			}
			
			//新增密码不能为空
			if(StrKit.isBlank(sysUser.getPassword())){
				throw new BusinessException(RenderResultCode.BUSINESS_216);
			}
			
			SysUser tmpSysUser = SysUser.dao.findByModel(new SysUser().setUserName(sysUser.getUserName()));
			if(tmpSysUser != null) {
				throw new BusinessException(RenderResultCode.BUSINESS_214);
			}

			//两次密码比较
			if(!StrKit.equals(rePassword, sysUser.getPassword())){
				throw new BusinessException(RenderResultCode.BUSINESS_215);
			}
			
			//新增
			sysUser.setId(IdUtil.fastSimpleUUID());
			sysUser.setSalt(RandomUtil.randomString(8));
			sysUser.setEmailVerified(1);
			sysUser.setPassword(SecureUtil.hmacMd5(sysUser.getSalt()).digestHex(sysUser.getPassword()));
			sysUser.setStatus(1L);
			sysUser.setCreateTime(new java.util.Date());
			sysUser.setUpdateTime(new java.util.Date());
			sysUser.save();
		}else{
			SysUser existsSysUser = SysUser.dao.findById(sysUser.getId());
			
			//如果修改密码则比较,无法修改超级管理员密码
			if(StrKit.notBlank(sysUser.getPassword())){
				//两次密码比较
				if(!StrKit.equals(rePassword, sysUser.getPassword())){
					throw new BusinessException(RenderResultCode.BUSINESS_215);
				}
				//修改
				existsSysUser.setSalt(RandomUtil.randomString(8));
				existsSysUser.setPassword(SecureUtil.hmacMd5(existsSysUser.getSalt()).digestHex(existsSysUser.getPassword()));
			}
			existsSysUser.setNickName(sysUser.getNickName());
			existsSysUser.setEmail(sysUser.getEmail());
			existsSysUser.setPhone(sysUser.getPhone());
			existsSysUser.setSex(sysUser.getSex());
			
			existsSysUser.setUpdateTime(new java.util.Date()).update();
		}
		renderJson(RenderResult.success());
    }

    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("sys.user.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void delete(){
    	String[] ids = getParaValues("ids");
    	for(String id : ids) {
    		SysUser sysUser = SysUser.dao.findById(id);
    		if(sysUser == null) {
        		throw new BusinessException(RenderResultCode.BUSINESS_210);
        	}
    		
    		if (StrKit.equals(CommonAttribute.SUPER_ADMIN_NAME, sysUser.getUserName())) {
        		throw new BusinessException(RenderResultCode.BUSINESS_218);
        	}
    		SysUserRole.dao.deteleByModel(new SysUserRole().setUserId(id));
    		SysUser.dao.deleteById(id);
    		
    	}
    	renderJson(RenderResult.success());
    }
    
    /**
     * 修改状态
     * @param ids
     */
    @AuthRequire.Perms("sys.user.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status(){
    	Long status = getParaToLong();
    	String[] ids = getParaValues("ids");
    	for(String id : ids) {
    		SysUser sysUser = SysUser.dao.findById(id);
    		if(sysUser == null) {
        		throw new BusinessException(RenderResultCode.BUSINESS_210);
        	}
    		
    		if (StrKit.equals(CommonAttribute.SUPER_ADMIN_NAME, sysUser.getUserName())) {
        		throw new BusinessException(RenderResultCode.BUSINESS_218);
        	}
    		
    		String statusValue = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.STATUS, "1");
    		sysUser.setStatus(Long.valueOf(JoyDictUtil.getDictValue(statusValue, DictAttribute.STATUS, "1")));
    		sysUser.setUpdateTime(new java.util.Date());
    		sysUser.update();
    	}
    	renderJson(RenderResult.success());
    }
    
    /**
     * 跳转到授权页面
     */
    @AuthRequire.Perms("sys.user.role")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void toRole(String ids) {
    	List<SysRole> roleList = SysRole.dao.findAll(new Sort("create_time", Enums.SortType.DESC));
    	
    	//查询用户对应角色
    	List<SysUserRole> userRoleList = SysUserRole.dao.findList(new SysUserRole().setUserId(ids));
    	for(SysRole role : roleList) {
    		role.setDescription("");
    		for(SysUserRole userRole : userRoleList) {
    			if(StrKit.equals(role.getId(), userRole.getRoleId())) {
    				role.setDescription("auth:true");
    			}
    		}
    	}
    	
    	setAttr("id", ids);
    	setAttr("roleList", roleList);
    	renderTpl("system/user/role.html");
    }
    
    @AuthRequire.Perms("sys.user.role")
    @Valids({
            @Valid(name = "id", required = true, min = 32),
            @Valid(name = "roleId", required = true, min = 32)
    })
    @Before({Tx.class, POST.class})
    public void role(String id) {
    	SysUser sysUser = SysUser.dao.findById(id);
    	if(sysUser == null) {
    		throw new BusinessException(RenderResultCode.BUSINESS_210);
    	}
    	
    	if (StrKit.equals(CommonAttribute.SUPER_ADMIN_NAME, sysUser.getUserName())) {
    		throw new BusinessException(RenderResultCode.BUSINESS_218);
    	}
    	
    	String[] roleIds = getParaValues("roleId");
    	
    	//先删除分配
    	SysUserRole.dao.deteleByModel(new SysUserRole().setUserId(id));
    	for(String roleId : roleIds) {
    		new SysUserRole().setId(IdUtil.simpleUUID()).setRoleId(roleId).setUserId(id).setCreateTime(new java.util.Date()).save();
    	}
    	
    	renderJson(RenderResult.success());
    }
    
}
