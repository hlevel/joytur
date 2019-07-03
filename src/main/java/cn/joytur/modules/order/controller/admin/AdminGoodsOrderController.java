package cn.joytur.modules.order.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

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
import cn.joytur.modules.order.entites.GoodsOrder;
import cn.joytur.modules.wechat.entities.WechatMemberAddr;

/**
 * @author xuhang
 */
@RouteMapping(url = "${admin}/goods/order")
public class AdminGoodsOrderController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("goods.order.view")
	public void index(GoodsOrder goodsOrder) {
		goodsOrder.put("openid", getPara("goodsOrder.openid"));
		goodsOrder.put("nickName", getPara("goodsOrder.nickName"));
		
		Page<GoodsOrder> pageList = GoodsOrder.dao.paginate(getPage(), getSize(), goodsOrder, new Sort("create_time", Enums.SortType.DESC));
		
		setAttr("page", pageList);
		setAttr("goodsOrder", goodsOrder);
		renderTpl("order/goods/index.html");
	}
	
	/**
     * 修改发货状态
     * @param ids
     */
    @AuthRequire.Perms("goods.order.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status(){
    	Long status = getParaToLong();
    	String[] ids = getParaValues("ids");
    	for(String id : ids) {
    		GoodsOrder goodsOrder = GoodsOrder.dao.findById(id);
    		if(goodsOrder == null) {
        		throw new BusinessException(RenderResultCode.BUSINESS_366);
        	}
    		
    		String statusLabel = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.GOODS_ORDER_STATUS, "1");
    		Long statusValue = Long.valueOf(JoyDictUtil.getDictValue(statusLabel, DictAttribute.GOODS_ORDER_STATUS, "2"));
    		
    		if(goodsOrder.getStatus() == Long.valueOf(JoyDictUtil.getDictValue(statusLabel, DictAttribute.GOODS_ORDER_STATUS, "2"))) {
        		//throw new BusinessException(RenderResultCode.BUSINESS_367);
    			continue;
        	}
    		
    		//查询默认地址
    		WechatMemberAddr quyWechatMemberAddr = new WechatMemberAddr().setDeleted(1L);
    		quyWechatMemberAddr.setWechatMemberId(goodsOrder.getWechatMemberId());
    		quyWechatMemberAddr.setStatus(2L);
    		WechatMemberAddr tmpWechatMemberAddr = WechatMemberAddr.dao.findByModel(quyWechatMemberAddr);
    		
    		if(tmpWechatMemberAddr == null){
    			throw new BusinessException(RenderResultCode.BUSINESS_368);
    		}
    		
    		goodsOrder.setAddrMirror(tmpWechatMemberAddr.getRealName() + ", " + tmpWechatMemberAddr.getMobile() + ", " + tmpWechatMemberAddr.getAddrArea() + " " + tmpWechatMemberAddr.getAddrDetail());
    		goodsOrder.setStatus(statusValue);
    		goodsOrder.setUpdateTime(new java.util.Date());
    		goodsOrder.update();
    	}
    	renderJson(RenderResult.success());
    }
	
}