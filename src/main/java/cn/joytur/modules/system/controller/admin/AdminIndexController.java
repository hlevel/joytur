package cn.joytur.modules.system.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums.SortType;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JWTUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyMenuUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.order.entites.GoodsOrder;
import cn.joytur.modules.order.entites.RechargeOrder;
import cn.joytur.modules.order.entites.RecommendOrder;
import cn.joytur.modules.system.entities.SysMenu;
import cn.joytur.modules.system.entities.SysUser;
import cn.joytur.modules.wechat.entities.WechatMember;

/**
 * 主页 
 * @author xuhang
 */
@RouteMapping(url = "${admin}")
@AuthRequire.Logined
public class AdminIndexController extends BaseAdminController {

	/**
	 * 主页中心
	 */
	public void index() {
		List<SysMenu> sourceList = SysMenu.dao.findList(new SysMenu().setType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.MENU_TYPE_MENU, DictAttribute.MENU_TYPE, "1"))), new Sort("sort", SortType.ASC));

		//处理为树形结构
		List<SysMenu> menuList = new ArrayList<SysMenu>();
        if(sourceList != null && sourceList.size() > 0){
        	JoyMenuUtil.sortMenuList(menuList, sourceList, "0");
        }
        SysUser user = SysUser.dao.findById(getAdminDTO().getUserid());
        
        if(user == null){ //防止用户id改变查不到自动清除重新登录
        	logout(); return;
        }
		
        setAttr("user", user);
		setAttr("menuList", menuList);
		renderTpl("main.html");
	}
	
	/**
	 * 首页中心
	 */
	public void main() {
		setAttr("memberCount", WechatMember.dao.findCountByToDay());
		setAttr("rechargeCount", RechargeOrder.dao.findCountByToDay());
		setAttr("recommendCount", RecommendOrder.dao.findCountByWait());
		setAttr("goodsCount", GoodsOrder.dao.findCountByWait());
		renderTpl("system/main/index.html");
	}
	
	/**
	 * 用户信息
	 */
	public void formUserInfo() {
		setAttr("user", SysUser.dao.findById(getAdminDTO().getUserid()));
		renderTpl("system/main/user_info.html");
	}
	

	/**
	 * 修改保存信息
	 */
	@Valids({
		@Valid(name = "original", required = true, min=6, max=32),
		@Valid(name = "password", required = true, min=6, max=32),
		@Valid(name = "confirm", required = true, min=6, max=32)
	})
	@Before(POST.class)
	public void userInfo() {
		
	}
	
	/**
	 * 修改密码
	 */
	public void formEditPwd(){
		renderTpl("system/main/edit_pwd.html");
	}
	
	/**
	 * 保存修改密码
	 */
	@Valids({
		@Valid(name = "original", desc="原始密码", required = true, min=6, max=32),
		@Valid(name = "password", desc="新密码", required = true, min=6, max=32),
		@Valid(name = "confirm", desc="确认密码", required = true, min=6, max=32)
	})
	@Before(POST.class)
	public void editPwd(String original, String password, String confirm){
		SysUser meUser = SysUser.dao.findById(getAdminDTO().getUserid());
		
		//两次密码比较
		if(!StrKit.equals(password, confirm)){
			throw new BusinessException(RenderResultCode.BUSINESS_215);
		}
		
		//原始密码不能和新密码相同
		if(StrKit.equals(original, confirm)){
			throw new BusinessException(RenderResultCode.BUSINESS_222);
		}
		
		//比较原始密码
		String originalPassword = SecureUtil.hmacMd5(meUser.getSalt()).digestHex(original);
		if(!StrKit.equals(originalPassword, meUser.getPassword())){
			throw new BusinessException(RenderResultCode.BUSINESS_221);
		}
		
		//修改
		meUser.setSalt(RandomUtil.randomString(8));
		meUser.setPassword(SecureUtil.hmacMd5(meUser.getSalt()).digestHex(password));
		meUser.setUpdateTime(new java.util.Date());
		meUser.update();
		
		renderJson(RenderResult.success());
	}

	
	/**
	 * 更新用户图片
	 */
	public void userPicture(){
		String headimgUrl = JoyUploadFileUtil.uploadAdapter(getFile());
		if(headimgUrl != null) {
			SysUser meUser = SysUser.dao.findById(getAdminDTO().getUserid());
			meUser.setHeadimgUrl(headimgUrl).setUpdateTime(new java.util.Date()).update();
			renderJson(RenderResult.success(RenderResultCode.COMMON_151)); return;
		}
		
		renderJson(RenderResult.error(RenderResultCode.COMMON_152));
	}
	
	/**
	 * 退出登录
	 */
	public void logout(){
		removeCookie(JWTUtil.ADMIN);
		redirectUrl("login");
	}
	
}