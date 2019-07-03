package cn.joytur.modules.wechat.controller.wap;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import cn.hutool.core.date.DateUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.OrderNoType;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.common.utils.JoyQrCodeUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.order.entites.AccountFunds;
import cn.joytur.modules.order.entites.GoodsOrder;
import cn.joytur.modules.order.entites.RechargeOrder;
import cn.joytur.modules.order.entites.RecommendOrder;
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.product.entities.ExtensionAdv;
import cn.joytur.modules.product.entities.ExtensionRule;
import cn.joytur.modules.product.entities.Goods;
import cn.joytur.modules.product.entities.GoodsGame;
import cn.joytur.modules.system.entities.SysGfw;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatMemberProfit;
import cn.joytur.modules.wechat.entities.WechatMemberRecommend;

/**
 * 会员中心
 * @author xuhang
 */
@RouteMapping(url = "/wap/member")
@AuthRequire.Logined
public class WapMemberController extends BaseWapController {

	@Inject
	private AccountService accountService;
	
	public void index() {
		WapMemberDTO memberDTO = getWapMemberDTO();
		
		Account msAccount = Account.dao.getSettAccount(memberDTO.getWechatMemberId());
		
		setAttr("goodsOrderCount", GoodsOrder.dao.findCountByModel(new GoodsOrder().setWechatMemberId(memberDTO.getWechatMemberId())));
		setAttr("gameAccount", Account.dao.getGameAccount(memberDTO.getWechatMemberId()));
		setAttr("settAccount", msAccount);

		//判断是否填写收货地址
		setAttr("receivingAddr", GoodsOrder.dao.findCountByModel(new GoodsOrder().setWechatMemberId(memberDTO.getWechatMemberId())));
		
		//判断是否开启充满送
		Long commissionStatus4 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.EXTENSION_TYPE4, DictAttribute.EXTENSION_TYPE, "4"));
		int existsCount = ExtensionRule.dao.findCountByModel(new ExtensionRule().setExtensionType(commissionStatus4).setStatus(1L));
		if(existsCount > 0){
			setAttr("giftGoodsList", Goods.dao.findGiftGoodsList(memberDTO.getWechatMemberId()));
		}
		
		Double rewardGame = 0.0;
		if(msAccount != null) {
			//查询是否有推广活动
			ExtensionRule quyExtensionRule = new ExtensionRule();
			quyExtensionRule.setExtensionType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.EXTENSION_TYPE3, DictAttribute.EXTENSION_TYPE, "3")));
			quyExtensionRule.setStatus(1L);
			ExtensionRule tmpExtensionRule = ExtensionRule.dao.findByModel(quyExtensionRule);
			if(tmpExtensionRule != null){
				rewardGame = tmpExtensionRule.getRecAmount();
			}
		}
		setAttr("rewardGame", rewardGame);
		renderWap("wechat/my.html");
	}
	
	/**
	 * 用户详情
	 */
	public void detail(){
		//做退款操作
		Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS1, DictAttribute.GOODS_GAME_STATUS, "1"));
		List<GoodsGame> goodsGameList = GoodsGame.dao.findList(new GoodsGame().setWechatMemberId(getWapMemberDTO().getWechatMemberId()).setStatus(status));
		for(int i = 0; goodsGameList != null && i < goodsGameList.size(); i ++ ){
			GoodsGame goodsGame = goodsGameList.get(i);
			if(goodsGame == null) {
                continue;
            }
            if(goodsGame.getStatus() != status){
            	continue;
            }
            accountService.refundGameCurrency(goodsGame.getWechatMemberId(), goodsGame.getExpAmt());
            
            goodsGame.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS4, DictAttribute.GOODS_GAME_STATUS, "4")));
            goodsGame.setUpdateTime(new java.util.Date());
            goodsGame.update();
		}
		
		Account tmpAccount = Account.dao.getGameAccount(getWapMemberDTO().getWechatMemberId());
		//查询积分账号
		JSONObject reJSON = new JSONObject();
		reJSON.put("balance", tmpAccount.getAvbAmt());
		renderJson(reJSON);
	}
	
	/**
	 * 分销明细
	 */
	public void distribute(){
		String act = getPara("act");
		if(StrKit.isBlank(act)) {
			//查询当前用户会员等级
			WechatMemberProfit wechatMemberProfit = WechatMemberProfit.dao.findByModel(new WechatMemberProfit().setWechatMemberId(getWapMemberDTO().getWechatMemberId()));
			
			String className = "topItem2";
			
			setAttr("profit", wechatMemberProfit);
			if(wechatMemberProfit.getRecVal1() != null && wechatMemberProfit.getRecVal1()>0
				&& wechatMemberProfit.getRecVal2() != null && wechatMemberProfit.getRecVal2()>0
				&& wechatMemberProfit.getRecVal3() != null && wechatMemberProfit.getRecVal3()>0){
				className = "";
			}
			setAttr("className", className);
			
			renderWap("wechat/distribute.html");
		} else {
			int level = getParaToInt("level");
			AccountFunds quyAccountFunds = new AccountFunds();
			
			switch (level) {
			case 1:
				quyAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND1, DictAttribute.ACCOUNT_FUNDS_ELE, "4")));
				break;
			case 2:
				quyAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND2, DictAttribute.ACCOUNT_FUNDS_ELE, "5")));
				break;
			case 3:
				quyAccountFunds.setEleType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND3, DictAttribute.ACCOUNT_FUNDS_ELE, "6")));
				break;
			default:
				break;
			}
			
			Page<AccountFunds> page = AccountFunds.dao.paginate(getParaToInt("page", DEF_PAGE), 10, quyAccountFunds);
			
			JSONArray rows = new JSONArray();
			for (int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				AccountFunds tmpAccountFunds = page.getList().get(i);

				JSONObject row = new JSONObject();
				row.put("createtime", DateUtil.formatDateTime(tmpAccountFunds.getCreateTime()));
				
				if(StrKit.isBlank(tmpAccountFunds.getTag())){
					row.put("price", "0");
					row.put("rate", "0");
					row.put("profit", tmpAccountFunds.getTransAmt());
					row.put("trade_no", "无");
					
				}else{
					JSONObject tagJSON = JSONObject.parseObject(tmpAccountFunds.getTag());
					
					row.put("price", tagJSON.getString("payAmt"));
					row.put("rate", tagJSON.getString("payScale"));
					row.put("profit", tmpAccountFunds.getTransAmt());
					row.put("trade_no", tagJSON.getString("orderNo"));
					
					//查询充值用户
					WechatMember tmpWechatMember = WechatMember.dao.findByRechargeOrderNo(tagJSON.getString("orderNo"));
					if(tmpWechatMember != null) {
						row.put("avatar", tmpWechatMember.getHeadimgUrl());
						row.put("nickname", tmpWechatMember.getNickName());
					}
				}
				
				
				rows.add(row);
			}
			
			renderJson(rows);
		}
	}
	
	/**
	 * 我的团队
	 */
	public void group(){
		String act = getPara("act");
		if(StrKit.isBlank(act)) {
			//查询当前用户会员等级
			WechatMemberProfit wechatMemberProfit = WechatMemberProfit.dao.findByModel(new WechatMemberProfit().setWechatMemberId(getWapMemberDTO().getWechatMemberId()));
			
			String className = "topItem2";
			
			setAttr("profit", wechatMemberProfit);
			if(wechatMemberProfit.getRecVal1() != null && wechatMemberProfit.getRecVal1()>0
				&& wechatMemberProfit.getRecVal2() != null && wechatMemberProfit.getRecVal2()>0
				&& wechatMemberProfit.getRecVal3() != null && wechatMemberProfit.getRecVal3()>0){
				className = "";
			}
			setAttr("className", className);
			renderWap("wechat/group.html");
		} else {
			int level = getParaToInt("level");
			
			Page<WechatMember> page = WechatMember.dao.findWapGroupPaginate(getParaToInt("page", DEF_PAGE), 10, level, getWapMemberDTO().getWechatMemberId());
			
			JSONArray rows = new JSONArray();
			for (int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				WechatMember tmpWechatMember = page.getList().get(i);
				
				JSONObject row = new JSONObject();
				row.put("avatar", tmpWechatMember.getHeadimgUrl());
				row.put("nickname", tmpWechatMember.getNickName());
				row.put("ctime", DateUtil.formatDateTime(tmpWechatMember.getCreateTime()));
				rows.add(row);
			}
			
			renderJson(rows);
		}
	}
	
	/**
	 * 提现记录
	 */
	public void cash() {
		String act = getPara("act");
		if(StrKit.isBlank(act)) {
			renderWap("wechat/cash.html");
		} else {
			RecommendOrder queryRecommendOrder = new RecommendOrder().setWechatMemberId(getWapMemberDTO().getWechatMemberId());
			//queryRecommendOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_ORDER_STATUS_FINISH, DictAttribute.RECOMMEND_ORDER_STATUS, "4")));
			Page<RecommendOrder> page = RecommendOrder.dao.paginate(getParaToInt("page", DEF_PAGE), 10, queryRecommendOrder, null);
			
			
			JSONArray rows = new JSONArray();
			for (int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				RecommendOrder tmpRecommendOrder = page.getList().get(i);
				
				JSONObject row = new JSONObject();
				//row.put("avatar", "/static/default/wap/h5css/images/goods.jpg");
				//row.put("nickname", tmpRecommendOrder.get("nickName"));
				row.put("nickname", JoyDictUtil.getDictLabel(tmpRecommendOrder.getStatus().toString(), DictAttribute.RECOMMEND_ORDER_STATUS, ""));
				row.put("createtime", DateUtil.formatDateTime(tmpRecommendOrder.getCreateTime()));
				row.put("money", tmpRecommendOrder.getTransAmt());
				
				rows.add(row);
			}
			
			renderJson(rows);
		}
		
		
	}
	
	/**
	 * 邀请好友
	 */
	public void share(){
		WapMemberDTO memberDTO = getWapMemberDTO();
		WechatMemberRecommend tmpWechatMemberRecommend = WechatMemberRecommend.dao.findByModel(new WechatMemberRecommend().setWechatMemberId(memberDTO.getWechatMemberId()));
		String defQrCode = (PathKit.getWebRootPath() + "/static/default/wap/h5css/images/share_qcode.png");
		String shareQrCode = ExtensionAdv.dao.findExtensionAdvImage(Integer.valueOf(JoyDictUtil.getDictValue(DictAttribute.ADV_BANNER_TYPE4, DictAttribute.ADV_BANNER_TYPE, "4")));
		if(StrKit.isBlank(shareQrCode)){
			shareQrCode = defQrCode;
		}
		String x = JoyConfigUtil.getConfigValue(Enums.SysConfigType.WAP_SHARE_X.name(), String.valueOf(128));
		String y = JoyConfigUtil.getConfigValue(Enums.SysConfigType.WAP_SHARE_Y.name(), String.valueOf(370));
		
		String appCode = SysGfw.dao.findSelfAppCode();
		if(StrKit.isBlank(appCode)){
			appCode = "";
		}
		
		String shareUrl = JoyConfigUtil.getMdUrl() + "/wap?code=" + tmpWechatMemberRecommend.getAcceptCode() + appCode;
			
		String imgBase64 = JoyQrCodeUtil.enQrCodeBackgroundPicToBase64(shareUrl, shareQrCode, Integer.valueOf(x), Integer.valueOf(y));
		setAttr("shareQrImage", imgBase64);
		
		renderWap("wechat/share.html");
	}
	
	
	/**
	 * 充值记录
	 */
	public void recharge(){
		String act = getPara("act");
		if(StrKit.isBlank(act)) {
			renderWap("wechat/recharge.html");
		} else {
			/*
			Account tmpAccount = Account.dao.getGameAccount(getWapMemberDTO().getWechatMemberId());
			
			AccountFunds quyAccountFunds = new AccountFunds();
			quyAccountFunds.setWechatMemberId(getWapMemberDTO().getWechatMemberId());
			quyAccountFunds.setToAccountId(tmpAccount.getId());
			quyAccountFunds.setAccType(tmpAccount.getAccType());
			Page<AccountFunds> page = AccountFunds.dao.paginate(getParaToInt("page", DEF_PAGE), 10, quyAccountFunds);
			
			JSONArray rows = new JSONArray();
			for (int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				AccountFunds tmpAccountFunds = page.getList().get(i);
				
				JSONObject row = new JSONObject();
				//row.put("avatar", "/static/default/wap/h5css/images/goods.jpg");
				row.put("desc", JoyDictUtil.getDictLabel(String.valueOf(tmpAccountFunds.getEleType()), DictAttribute.ACCOUNT_FUNDS_ELE, "未知"));
				row.put("money", tmpAccountFunds.getTransAmt());
				row.put("createtime", DateUtil.formatDateTime(tmpAccountFunds.getCreateTime()));
				
				rows.add(row);
			}
			*/
			RechargeOrder queryRechargeOrder = new RechargeOrder().setWechatMemberId(getWapMemberDTO().getWechatMemberId());
			queryRechargeOrder.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECHARGE_ORDER_STATUS_FINISH, DictAttribute.RECHARGE_ORDER_STATUS, "3")));
			Page<RechargeOrder> page = RechargeOrder.dao.paginate(getParaToInt("page", DEF_PAGE), 10, queryRechargeOrder);
			
			Long proxy = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_REWARD, DictAttribute.ACCOUNT_ACC_UNIT_TYPE, "2"));
			
			JSONArray rows = new JSONArray();
			for (int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				RechargeOrder tmpRechargeOrder = page.getList().get(i);
				
				JSONObject row = new JSONObject();
				//row.put("avatar", "/static/default/wap/h5css/images/goods.jpg");
				if (proxy == tmpRechargeOrder.getTransType()) {
					row.put("desc", "升级代理");
				} else {
					row.put("desc", "充值游戏币");
				}
				row.put("money", tmpRechargeOrder.getTransAfterAmt());
				row.put("createtime", DateUtil.formatDateTime(new java.util.Date()));
				
				rows.add(row);
			}
			renderJson(rows);
		}
	}
	
	/**
	 * 游戏记录
	 */
	public void game(){
		String act = getPara("act");
		if(StrKit.isBlank(act)) {
			renderWap("wechat/game.html");
		} else {
			GoodsGame queryGoodsGame = new GoodsGame();
			queryGoodsGame.setWechatMemberId(getWapMemberDTO().getWechatMemberId());
			Page<GoodsGame> page = GoodsGame.dao.paginateWap(getParaToInt("page", DEF_PAGE), 10, queryGoodsGame, null);
			
			JSONArray rows = new JSONArray();
			for (int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				GoodsGame tmpGoodsGame = page.getList().get(i);
				
				JSONObject row = new JSONObject();
				row.put("thumb", JoyUploadFileUtil.getHttpPath(tmpGoodsGame.getStr("goods_image")));
				row.put("title", tmpGoodsGame.getStr("goods_name"));
				row.put("model", tmpGoodsGame.getStr("recommend"));
				row.put("status", tmpGoodsGame.getStatus());
				row.put("money", tmpGoodsGame.getExpAmt());
				row.put("createtime", DateUtil.formatDateTime(tmpGoodsGame.getCreateTime()));
				
				rows.add(row);
			}
			
			renderJson(rows);
		}
	}
	
	/**
	 * 我的奖品
	 */
	public void prize(){
		String act = getPara("act");
		if(StrKit.isBlank(act)) {
			renderWap("wechat/prize.html");
		} else {
			JSONArray rows = new JSONArray();
			Page<GoodsOrder> page = GoodsOrder.dao.wapPaginate(getParaToInt("page", DEF_PAGE), 10, new GoodsOrder().setWechatMemberId(getWapMemberDTO().getWechatMemberId()), null);
			
			for(int i = 0; page.getList() != null && i < page.getList().size(); i++) {
				GoodsOrder tmpGoodsOrder = page.getList().get(i);
				JSONObject row = new JSONObject();
				row.put("trade_no", tmpGoodsOrder.getOrderNo());
				row.put("thumb", JoyUploadFileUtil.getHttpPath(tmpGoodsOrder.getStr("goods_image")));
				row.put("title", tmpGoodsOrder.getStr("goods_name"));
				row.put("model", tmpGoodsOrder.getStr("recommend"));
				row.put("status", tmpGoodsOrder.getStatus());
				row.put("type", 1);
				row.put("order_type", tmpGoodsOrder.getOrderType());
				
				rows.add(row);
			}
			
			renderJson(rows);
		}
	}
	
	/**
	 * 赠送奖品
	 */
	public void gift(){
		//获取所有商品
		List<Goods> goodsList = Goods.dao.findGiftGoodsList(getWapMemberDTO().getWechatMemberId());
		
		setAttr("goodsList", goodsList);
		renderWap("wechat/gift.html");
	}
	
	/**
	 * 选择赠品
	 */
	public void giftsuc(){
		String gid = getPara("gid"); //商品id
		
		if(StrKit.isBlank(gid)){
			renderJson(RenderResult.error(RenderResultCode.PARAM)); return;
		}
		
		//查询商品是否存在
		Goods tmpGoods = Goods.dao.findById(gid);
		if(tmpGoods == null){
			renderJson(RenderResult.error(RenderResultCode.BUSINESS_362)); return;
		}
		
		//查询是否符合商品赠送条件
		WapMemberDTO memberDTO = getWapMemberDTO();
		List<Goods> goodsList = Goods.dao.findGiftGoodsList(memberDTO.getWechatMemberId());
		if(!goodsList.stream().anyMatch(g -> StrKit.equals(g.getId(), gid))){
			renderJson(RenderResult.error(RenderResultCode.BUSINESS_363)); return;
		}
		
		//添加赠品信息
		GoodsOrder goodsOrder = new GoodsOrder();
		goodsOrder.setId(JoyIdUtil.simpleUUID());
		goodsOrder.setOrderNo(JoyIdUtil.getOrderNo(OrderNoType.GOODS_ORDER));
		goodsOrder.setWechatMemberId(memberDTO.getWechatMemberId());
		goodsOrder.setGameId(null);
		goodsOrder.setGoodsId(gid);
		goodsOrder.setOrderType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_ORDER_TYPE2, DictAttribute.GOODS_ORDER_TYPE, "2")));
		goodsOrder.setStatus(1L);
		goodsOrder.setAddrMirror(null);
		goodsOrder.setLogisticsNumber(null);
		goodsOrder.setCreateTime(new java.util.Date());
		goodsOrder.setUpdateTime(new java.util.Date());
		goodsOrder.save();
		
		renderJson(RenderResult.success(RenderResultCode.BUSINESS_364));
	}
	
	public void chat(){
		renderWap("wechat/chat.html");
	}
    
}