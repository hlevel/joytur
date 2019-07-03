package cn.joytur.modules.order.controller.admin;

import java.util.List;

import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Page;

import cn.hutool.core.codec.Base64;
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
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.common.utils.JoyUtil;
import cn.joytur.modules.order.entites.RechargeOrder;
import cn.joytur.modules.order.service.RechargeOrderService;
import cn.joytur.modules.system.entities.SysConfig;

/**
 * @author xuhang
 */
@RouteMapping(url = "${admin}/recharge/order")
public class AdminRechargeOrderController extends BaseAdminController {

	@Inject
	private RechargeOrderService rechargeOrderService;
	
	/**
	 * 列表
	 */
	@AuthRequire.Perms("recharge.order.view")
	public void index(RechargeOrder rechargeOrder) {
		rechargeOrder.put("openid", getPara("rechargeOrder.openid"));
		rechargeOrder.put("nickName", getPara("rechargeOrder.nickName"));
		
		Page<RechargeOrder> pageList = RechargeOrder.dao.paginate(getPage(), getSize(), rechargeOrder, new Sort("create_time", Enums.SortType.DESC));
		
		setAttr("page", pageList);
		setAttr("rechargeOrder", rechargeOrder);
		renderTpl("order/recharge/index.html");
	}
	
	/**
     * 修改发货状态
     * @param ids
     */
    @AuthRequire.Perms("recharge.order.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    /*@Before(Tx.class)*/
    public void status(){
    	String[] ids = getParaValues("ids");
    	
    	for(String id : ids) {
    		RechargeOrder rechargeOrder = RechargeOrder.dao.findById(id);
    		if(rechargeOrder == null) {
        		throw new BusinessException(RenderResultCode.BUSINESS_300);
        	}
    		
    		Long statusValue = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_FINISH, DictAttribute.RECHARGE_ORDER_STATUS, "4"));
    		
    		if(rechargeOrder.getStatus() == statusValue) {
    			continue;
        	}
    		
    		rechargeOrderService.payCompletedRechargeOrder(rechargeOrder.getRealTransAmt(), rechargeOrder.getStatus(), rechargeOrder.getWechatMemberId());
    	}
    	renderJson(RenderResult.success());
    }
    
    @Clear
    public void mob(){
    	String sign = getPara();
    	String t = getPara("t");
    	if(!JoyIdUtil.signatureVerification(t, sign)){
			renderTpl("error/404.html"); return;
		}
    	
    	//营业状态
    	String swTime = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CLOSING_TIME.name(), "09:00:00-12:00:00");
    	
    	List<RechargeOrder> rechargeOrderList = RechargeOrder.dao.findWaitHandleRechargeOrderList();
    	setAttr("swtime", JoyUtil.getBusinessTime(swTime));
    	setAttr("switch", JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CLOSING_SWITCH.name(), "0"));
		setAttr("orderList", rechargeOrderList);
		
		setCookie("_mob", Base64.encode((t+"-"+sign)), -1);
    	renderTpl("order/recharge/mob.html");
    }
    
    @Clear
    public void dobus(){
    	if(!validateCookieSign()){
    		renderJson(RenderResult.error()); return;
    	}
    	
    	String switchb = getPara();
    	SysConfig tmpSysConfig = SysConfig.dao.findByModel(new SysConfig().setName(Enums.SysConfigType.SYS_CLOSING_SWITCH.name()));
    	if(tmpSysConfig != null){
    		tmpSysConfig.setValue(switchb);
    		tmpSysConfig.setUpdateTime(new java.util.Date());
    		tmpSysConfig.update();
    		
    	}else{
    		tmpSysConfig = new SysConfig();
    		tmpSysConfig.setId(JoyIdUtil.simpleUUID());
    		tmpSysConfig.setName(Enums.SysConfigType.SYS_CLOSING_SWITCH.name());
    		tmpSysConfig.setValue(switchb);
    		tmpSysConfig.setCreateTime(new java.util.Date());
    		tmpSysConfig.setUpdateTime(new java.util.Date());
    		tmpSysConfig.save();
    	}
    	
    	JoyConfigUtil.clearCache();
    	
    	renderJson(RenderResult.success());
    }
	
    @Clear
    public void mobpay(){
    	if(!validateCookieSign()){
    		renderJson(RenderResult.error()); return;
    	}
    	
    	String id = getPara("id");
    	RechargeOrder tmpRechargeOrder = RechargeOrder.dao.findById(id);
    	if(tmpRechargeOrder == null) {
    		throw new BusinessException(RenderResultCode.BUSINESS_300);
    	}
		
		Long statusValue = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_FINISH, DictAttribute.RECHARGE_ORDER_STATUS, "4"));
		
		if(tmpRechargeOrder.getStatus() == statusValue) {
			throw new BusinessException(RenderResultCode.BUSINESS_302);
    	}
		
		rechargeOrderService.payCompletedRechargeOrder(tmpRechargeOrder.getRealTransAmt(), tmpRechargeOrder.getStatus(), tmpRechargeOrder.getWechatMemberId());
    	
    	renderJson(RenderResult.success(RenderResultCode.BUSINESS_315));
    }
    
    /**
     * 校验cookie 属性
     * @return
     */
    private boolean validateCookieSign(){
    	try {
			String _mob = getCookie("_mob");
			String[] params = Base64.decodeStr(_mob).split("-");
			
			if(JoyIdUtil.signatureVerification(params[0], params[1])){
				return true;
			}
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return false;
    }
    
}