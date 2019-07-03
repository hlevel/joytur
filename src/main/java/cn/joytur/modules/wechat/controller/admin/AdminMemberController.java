package cn.joytur.modules.wechat.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

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
import cn.joytur.modules.wechat.entities.WechatMember;

/**
 * 微信会员管理 
 * @author xuhang
 */
@RouteMapping(url = "${admin}/member")
public class AdminMemberController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("wechat.member.view")
	public void index(WechatMember wechatMember) {
		Page<WechatMember> pageList = WechatMember.dao.paginate(getPage(), getSize(), wechatMember, new Sort("dict_code,sort", Enums.SortType.ASC));
		setAttr("page", pageList);
		setAttr("wechatMember", wechatMember);
		renderTpl("wechat/member/index.html");
	}
	
	
	/**
	 * toEdit页面
	 */
	@AuthRequire.Perms("wechat.member.edit")
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	public void edit(String id){
		WechatMember tmpWechatMember = WechatMember.dao.findById(id);
		
		setAttr("wechatMember", tmpWechatMember);
		renderTpl("wechat/member/add.html");
	}
	
    /**
	 * 保存
	 */
    @AuthRequire.Perms("wechat.member.save")
	@Valids({
		@Valid(name = "recommendRuleId", required = true)
	})
	@Before(POST.class)
	public void save(WechatMember wechatMember){
		//修改
    	WechatMember tmpWechatMember = WechatMember.dao.findById(wechatMember.getId());
    	if(tmpWechatMember == null){
    		throw new BusinessException(RenderResultCode.BUSINESS_260);
    	}
		
		renderJson(RenderResult.success());
	}
    
    /**
     * 修改状态
     * @param ids
     */
    @AuthRequire.Perms("wechat.member.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status(){
    	Long status = getParaToLong();
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		//修改状态
    		WechatMember tmpWechatMember = WechatMember.dao.findById(id);
    		if(tmpWechatMember == null){
    			throw new BusinessException(RenderResultCode.BUSINESS_260);
    		}
    		tmpWechatMember.setStatus(status);
    		tmpWechatMember.setUpdateTime(new java.util.Date());
    		tmpWechatMember.update();
    	}
    	
    	renderJson(RenderResult.success());
    }
    
}