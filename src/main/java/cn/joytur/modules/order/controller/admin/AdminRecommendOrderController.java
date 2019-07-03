package cn.joytur.modules.order.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.order.entites.RecommendOrder;
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatMemberRecommend;

/**
 * 充值订单管理 
 * @author xuhang
 */
@RouteMapping(url = "${admin}/recommend/order")
public class AdminRecommendOrderController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("recommend.order.view")
	public void index(RecommendOrder recommendOrder) {
		
		recommendOrder.put("openid", getPara("recommendOrder.openid"));
		recommendOrder.put("nickName", getPara("recommendOrder.nickName"));
		
		Page<RecommendOrder> pageList = RecommendOrder.dao.paginate(getPage(), getSize(), recommendOrder, new Sort("create_time", Enums.SortType.DESC));
		
		setAttr("page", pageList);
		setAttr("recommendOrder", recommendOrder);
		renderTpl("order/recommend/index.html");
	}
	
	/**
	 * to打款处理页面
	 */
	@AuthRequire.Perms("recommend.order.paycash")
	public void paycash(){
		String id = getPara();
		String view = getPara("view");
		RecommendOrder tmpRecommendOrder = RecommendOrder.dao.findById(id);
		if(tmpRecommendOrder == null){
			throw new BusinessException(RenderResultCode.BUSINESS_309);
		}
		Long applyStatus = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_ORDER_STATUS_APPLY, DictAttribute.RECOMMEND_ORDER_STATUS, "1"));
		if(tmpRecommendOrder.getStatus() != applyStatus && StrKit.isBlank(view)){
			throw new BusinessException(RenderResultCode.BUSINESS_311);
		}
			
		WechatMemberRecommend tmpWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(tmpRecommendOrder.getWechatMemberId()));
		if(tmpWechatMemberRecommend == null || StrKit.isBlank(tmpWechatMemberRecommend.getCashImage())){
			throw new BusinessException(RenderResultCode.BUSINESS_310);
		}
		
		WechatMember tmpWechatMember = WechatMember.dao.findById(tmpRecommendOrder.getWechatMemberId());
		
		setAttr("wechatMember", tmpWechatMember);
		setAttr("recommendOrder", tmpRecommendOrder);
		setAttr("payImg", JoyUploadFileUtil.getHttpPath(tmpWechatMemberRecommend.getCashImage()));
		
		setAttr("view", getPara("view"));
		renderTpl("order/recommend/paycash.html");
	}
	
	/**
	 * 打款处理
	 */
	@Before(Tx.class)
	@AuthRequire.Perms("recommend.order.paycashfinish")
	public void paycashfinish(){
		String id = getPara("id");
		RecommendOrder tmpRecommendOrder = RecommendOrder.dao.findById(id);
		if(tmpRecommendOrder == null){
			throw new BusinessException(RenderResultCode.BUSINESS_309);
		}
		
		Long applyStatus = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_ORDER_STATUS_APPLY, DictAttribute.RECOMMEND_ORDER_STATUS, "1"));
		if(tmpRecommendOrder.getStatus() != applyStatus){
			throw new BusinessException(RenderResultCode.BUSINESS_311);
		}
		
		WechatMemberRecommend tmpWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(tmpRecommendOrder.getWechatMemberId()));
		if(tmpWechatMemberRecommend == null || StrKit.isBlank(tmpWechatMemberRecommend.getCashImage())){
			throw new BusinessException(RenderResultCode.BUSINESS_310);
		}
		
		//提现完成
		Duang.duang(AccountService.class).withdrawalCash(tmpRecommendOrder.getWechatMemberId(), tmpRecommendOrder.getTransAmt());
		
		//修改提现订单状态
		RecommendOrder updateRecommendOrder = new RecommendOrder();
		updateRecommendOrder.setUpdateTime(new java.util.Date());
		updateRecommendOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_ORDER_STATUS_FINISH, DictAttribute.RECOMMEND_ORDER_STATUS, "3")));
		RecommendOrder.dao.updateModelById(updateRecommendOrder, tmpRecommendOrder.getId());
		
		renderJson(RenderResult.success());
	}
	
	
}