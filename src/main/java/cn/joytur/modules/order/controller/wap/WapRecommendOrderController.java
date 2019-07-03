package cn.joytur.modules.order.controller.wap;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.OrderNoType;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.UploadBean;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.order.entites.RecommendOrder;
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.wechat.entities.WechatMemberRecommend;

/**
 * wap 升级订单 
 * @author xuhang
 */
@RouteMapping(url = "/wap/recommend/order")
@AuthRequire.Logined
public class WapRecommendOrderController extends BaseWapController {

	/**
	 * 页面
	 */
	public void index() {
		String cashImg = "";
		
		WechatMemberRecommend tmpWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(getWapMemberDTO().getWechatMemberId()));
		if(tmpWechatMemberRecommend != null && StrKit.notBlank(tmpWechatMemberRecommend.getCashImage())){
			cashImg = JoyUploadFileUtil.getHttpPath(tmpWechatMemberRecommend.getCashImage());
		}
		setAttr("cashImg", cashImg);
		renderWap("order/photo.html");
	}
	
	/**
	 * 确认提现
	 */
	@Before(Tx.class)
	public void withdrawal() {
		Account msAccount = Account.dao.getSettAccount(getWapMemberDTO().getWechatMemberId());
		Double allowCash = Double.valueOf(JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_ALLOW_CASH.name(), "5"));
		
		if(msAccount.getAvbAmt() < allowCash){
			throw new BusinessException(RenderResultCode.BUSINESS_308);
		}
		
		//创建提现订单
		RecommendOrder tmpRecommendOrder = new RecommendOrder();
		tmpRecommendOrder.setId(JoyIdUtil.simpleUUID());
		tmpRecommendOrder.setWechatMemberId(getWapMemberDTO().getWechatMemberId());
		tmpRecommendOrder.setOrderNo(JoyIdUtil.getOrderNo(OrderNoType.RECOMMEND_ORDER));
		tmpRecommendOrder.setTransAmt(msAccount.getAvbAmt());
		tmpRecommendOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_ORDER_STATUS_APPLY, DictAttribute.RECOMMEND_ORDER_STATUS, "1")));
		tmpRecommendOrder.setDescription(null);
		tmpRecommendOrder.setCreateTime(new java.util.Date());
		tmpRecommendOrder.setUpdateTime(new java.util.Date());
		tmpRecommendOrder.save();
		
		//冻结金额
		Duang.duang(AccountService.class).freezeWithdrawalCash(getWapMemberDTO().getWechatMemberId(), msAccount.getAvbAmt());
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * 上传收款码
	 */
	public void upload(){
        List<String> fileNames = new ArrayList<>();
        List<UploadFile> files = getFiles();
        files.forEach(uploadFile -> {
            String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile);
            fileNames.add(headimgUrl);
        });

        UploadBean<List<String>> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(fileNames);
        
        WechatMemberRecommend tmpWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(getWapMemberDTO().getWechatMemberId()));
		if(tmpWechatMemberRecommend != null){
			WechatMemberRecommend updateWechatMemberRecommend = new WechatMemberRecommend();
			updateWechatMemberRecommend.setCashImage(fileNames.get(0));
			updateWechatMemberRecommend.setUpdateTime(new java.util.Date());
			tmpWechatMemberRecommend.update();
			
			WechatMemberRecommend.dao.updateModelById(updateWechatMemberRecommend, tmpWechatMemberRecommend.getId());
		}
        
        renderJson(bean);
    }
	
}