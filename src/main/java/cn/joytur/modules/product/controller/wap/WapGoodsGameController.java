package cn.joytur.modules.product.controller.wap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.URLUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.constant.Enums.OrderNoType;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.order.entites.GoodsOrder;
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.product.entities.Goods;
import cn.joytur.modules.product.entities.GoodsGame;
import cn.joytur.modules.product.entities.GoodsRule;
import cn.joytur.modules.system.entities.SysDictionary;

/**
 * <p>
 *  游戏管理
 * </p>
 * @since 2019/1/9
 */
@RouteMapping(url = "/wap/game")
@AuthRequire.Logined
public class WapGoodsGameController extends BaseWapController {

	@Inject
	private AccountService accountService;
	
    /**
     * 列表
     */
	@Before(Tx.class)
    public void index() {
		try {
			String goodsId = getPara("gid");
	    	
	    	WapMemberDTO memberDTO = getWapMemberDTO();
	    	//游戏初始化状态
	    	Long gameStatus1 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS1, DictAttribute.GOODS_GAME_STATUS, "1"));
	    	Long gameStatus2 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS2, DictAttribute.GOODS_GAME_STATUS, "2"));
	    	Long gameStatus3 = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS3, DictAttribute.GOODS_GAME_STATUS, "3"));
	    	
	    	Goods tmpGoods = Goods.dao.findById(goodsId);
	    	if(tmpGoods == null) {
	    		throw new BusinessException(RenderResultCode.BUSINESS_362);
	    	}

	    	//查询当前用户当前商品游戏闯关上次时间点
	    	int handleStatus = 0;
	    	GoodsGame quyGoodsGame = new GoodsGame().setWechatMemberId(memberDTO.getWechatMemberId()).setGoodsId(goodsId);
	    	GoodsGame existsGoodsGame = GoodsGame.dao.findByModel(quyGoodsGame);
	    	if(existsGoodsGame != null) {
	    		long secod = DateUtil.between(existsGoodsGame.getCreateTime(), new java.util.Date(), DateUnit.SECOND);
	    		//1.检测是否游戏开始未闯关刷新 游戏时间小于10s,并且等于初始化状态
	    		//去掉10s
	    		if(/*secod<10 && */existsGoodsGame.getStatus() == gameStatus1){
	    			handleStatus = 1;
	    		}
	    		/*
	    		else if(secod>10 && existsGoodsGame.getStatus() == gameStatus1){
	    			//未闯关但已经操作10s 则失败
	    			handleStatus = 2;
	    			existsGoodsGame.setStatus(gameStatus3);
	    			existsGoodsGame.setEndTime(new java.util.Date());
	    			existsGoodsGame.setUpdateTime(new java.util.Date());
	    			existsGoodsGame.update();
	    		}
	    		*/
	    		else if(existsGoodsGame.getStatus() == gameStatus2){
	    			//2.检测是否游戏开始已闯关刷新,并且处理为已完成状态,导航到首页
	    			handleStatus = 2;
	    			existsGoodsGame.setStatus(gameStatus3);
	    			existsGoodsGame.setEndTime(new java.util.Date());
	    			existsGoodsGame.setUpdateTime(new java.util.Date());
	    			existsGoodsGame.update();
	    		}
	    		/*
	    		else if(existsGoodsGame.getStatus() == gameStatus3 && secod < 40){
	    			//3.检查是否游戏结束刷新,并且是小于40s,导航到首页
	    			handleStatus = 3;
	    		}
	    		*/
	    		else if(existsGoodsGame.getStatus() == gameStatus3 && secod > 40){
	    			//4.检查是否游戏结束刷新,并且是大于40s,可以再次开始
	    			handleStatus = 4;
	    		}
	    	}
	    	
	    	if(handleStatus == 2 || handleStatus == 3) {
	    		redirect("/wap/index"); return;
	    	}
	    	
	    	if(handleStatus != 1){ //如果是状态1可以不用检测
	    		//查询账户余币是否充足
		    	Account tmpAccount = Account.dao.getGameAccount(getWapMemberDTO().getWechatMemberId());
		    	if(tmpAccount.getAvbAmt() < tmpGoods.getScorePrice()) {
		    		throw new BusinessException(RenderResultCode.BUSINESS_306);
		    	}
	    	}
	    	
	    	//加载游戏规则
	    	GoodsRule tmpGoodsRule = GoodsRule.dao.findById(tmpGoods.getGoodsRuleId());
	    	
	    	JSONObject ruleJSON = JSONArray.parseObject(tmpGoodsRule.getDiffValue());
	    	
	    	JSONArray numberJSON = new JSONArray();
	    	JSONArray speedJSON = new JSONArray();
	    	JSONArray usetimeJSON = new JSONArray();
	    	
	    	Integer maxSeconds = 0;
	    	
	    	for(SysDictionary dict : JoyDictUtil.getDictList(DictAttribute.GOODS_RULE_LEVEL_TYPE)) {
	    		JSONObject diffJSON = ruleJSON.getJSONObject(dict.getDictValue());

	    		numberJSON.add(diffJSON.getIntValue("quant"));
	        	speedJSON.add(diffJSON.getDouble("diff"));
	        	usetimeJSON.add(diffJSON.getIntValue("second"));
	        	
	        	maxSeconds += diffJSON.getIntValue("second");
	    	}
	    	
	    	JSONObject gameJSON = new JSONObject();
	    	gameJSON.put("number", numberJSON);
	    	gameJSON.put("speed", speedJSON);
	    	gameJSON.put("usetime", usetimeJSON);
	    	
	    	GoodsGame tmpGoodsGame = null;
	    	if(handleStatus == 1){
	    		//继续游戏
	    		tmpGoodsGame = existsGoodsGame;
	    	}else{
	    		//插入游戏记录
	    		tmpGoodsGame = new GoodsGame();
	    		tmpGoodsGame.setId(JoyIdUtil.simpleUUID());
	    		tmpGoodsGame.setGoodsId(tmpGoods.getId());
	    		tmpGoodsGame.setWechatMemberId(memberDTO.getWechatMemberId());
	    		tmpGoodsGame.setExpAmt(Double.valueOf(tmpGoods.getScorePrice()));
	    		tmpGoodsGame.setStartTime(new java.util.Date());
	    		tmpGoodsGame.setEndTime(null);
	    		tmpGoodsGame.setScreen("1");
	    		tmpGoodsGame.setStatus(gameStatus1);
	    		tmpGoodsGame.setGameParams(null);
	    		tmpGoodsGame.setGameResult(null);
	    		tmpGoodsGame.setUpdateTime(new java.util.Date());
	    		tmpGoodsGame.setCreateTime(new java.util.Date());
	    		tmpGoodsGame.setRemarks(null);
	    		tmpGoodsGame.save();
	    		
	    		//扣除游戏账户
	    		accountService.depleteGameCurrency(memberDTO.getWechatMemberId(), Double.valueOf(tmpGoods.getScorePrice()));
	    	}
	    	
	    	setAttr("game", gameJSON);
	    	setAttr("gameId", tmpGoodsGame.getId());
	    	setAttr("goodsId", goodsId);
	    	setAttr("timestamp", Long.toString(System.currentTimeMillis()/1000));
	    	
	    	setCookie("ajaxurl", "/wap/game/over/" + tmpGoodsGame.getId(), maxSeconds + 3); //多给3s
	    	renderWap("product/play.html");
		}catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	
    }
    
	/**
	 * 游戏结束
	 */
	@Before(Tx.class)
    public void over(){
		WapMemberDTO memberDTO = getWapMemberDTO();
		
    	String goodsGameId = getPara();
    	String ajaxurl = getCookie("ajaxurl");
    	String game_cookie = getCookie("game_cookie");
    	String orderResult = getPara("orderResult");
    	
    	if(StrKit.isBlank(ajaxurl) || StrKit.isBlank(game_cookie) || StrKit.isBlank(orderResult)){
    		renderJson(RenderResult.error(RenderResultCode.BUSINESS_374)); return;
    	}
    	
    	GoodsGame tmpGoodsGame = GoodsGame.dao.findById(goodsGameId);
    	if(tmpGoodsGame == null){
    		renderJson(RenderResult.error(RenderResultCode.BUSINESS_375)); return;
    	}
    	
    	if(StrKit.notBlank(tmpGoodsGame.getGameResult())){
    		renderJson(RenderResult.error(RenderResultCode.BUSINESS_376)); return;
    	}
    	
    	JSONObject resJSON = JSONObject.parseObject(Base64.decodeStr(orderResult));
    	int result = resJSON.getIntValue("result");
    	int level = resJSON.getIntValue("level");
    	
    	//JSONObject gameJSON = JSONObject.parseObject(URLUtil.decode(game_cookie));

    	tmpGoodsGame.setScreen(String.valueOf(level));
    	tmpGoodsGame.setGameParams(URLUtil.decode(game_cookie));
    	tmpGoodsGame.setGameResult(Base64.decodeStr(orderResult));
    	tmpGoodsGame.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS3, DictAttribute.GOODS_GAME_STATUS, "3")));
    	tmpGoodsGame.setEndTime(new java.util.Date());
    	tmpGoodsGame.setUpdateTime(new java.util.Date());
    	tmpGoodsGame.update();
    	
    	//闯关成功
    	if(result == 2){
    		//判断今天中奖个数
    		
    		//添加中奖信息
    		GoodsOrder goodsOrder = new GoodsOrder();
    		goodsOrder.setId(JoyIdUtil.simpleUUID());
    		goodsOrder.setOrderNo(JoyIdUtil.getOrderNo(OrderNoType.GOODS_ORDER));
    		goodsOrder.setWechatMemberId(memberDTO.getWechatMemberId());
    		goodsOrder.setGameId(goodsGameId);
    		goodsOrder.setGoodsId(tmpGoodsGame.getGoodsId());
    		goodsOrder.setOrderType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_ORDER_TYPE1, DictAttribute.GOODS_ORDER_TYPE, "1")));
    		goodsOrder.setStatus(1L);
    		goodsOrder.setAddrMirror(null);
    		goodsOrder.setLogisticsNumber(null);
    		goodsOrder.setCreateTime(new java.util.Date());
    		goodsOrder.setUpdateTime(new java.util.Date());
    		goodsOrder.save();
    	}
    	
    	renderJson(RenderResult.success(RenderResultCode.BUSINESS_377));
    }
	
	/**
	 * 通知关卡
	 */
	public void not(){
		String goodsGameId = getPara();
		String level = getPara("level");
		if(StrKit.isBlank(goodsGameId)){
    		renderJson(RenderResult.error(RenderResultCode.BUSINESS_374)); return;
    	}
		GoodsGame tmpGoodsGame = GoodsGame.dao.findById(goodsGameId);
    	if(tmpGoodsGame == null){
    		renderJson(RenderResult.error(RenderResultCode.BUSINESS_375)); return;
    	}
    	if(StrKit.notBlank(tmpGoodsGame.getGameResult())){
    		renderJson(RenderResult.error(RenderResultCode.BUSINESS_376)); return;
    	}
    	tmpGoodsGame.setScreen(level);
    	tmpGoodsGame.setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS2, DictAttribute.GOODS_GAME_STATUS, "2")));
    	tmpGoodsGame.setUpdateTime(new java.util.Date());
    	tmpGoodsGame.update();
    	
		renderJson(RenderResult.success());
	}
    
    /**
     * 体验模式
     */
    public void taste(){
    	JSONArray numberJSON = new JSONArray();
    	JSONArray speedJSON = new JSONArray();
    	JSONArray usetimeJSON = new JSONArray();
    	
    	numberJSON.add(10);
    	numberJSON.add(15);
    	numberJSON.add(20);
    	
    	speedJSON.add(0.02);
    	speedJSON.add(0.01);
    	speedJSON.add(0.9);
    	
    	usetimeJSON.add(45);
    	usetimeJSON.add(30);
    	usetimeJSON.add(20);
    	
    	JSONObject gameJSON = new JSONObject();
    	
    	gameJSON.put("number", numberJSON);
    	gameJSON.put("speed", speedJSON);
    	gameJSON.put("usetime", usetimeJSON);
    	
    	setAttr("game", gameJSON);
    	
    	renderWap("product/taste.html");
    }
    
    
    /**
     * 难度系数
     */
    public void degree(){
    	JSONObject resJSON = new JSONObject();
    	
    	JSONArray ROTAION_SPEED_ARRAY = new JSONArray();
    	ROTAION_SPEED_ARRAY.add(-0.06);
    	ROTAION_SPEED_ARRAY.add(-0.03);
    	ROTAION_SPEED_ARRAY.add(-0.02);
    	ROTAION_SPEED_ARRAY.add(0.02);
    	ROTAION_SPEED_ARRAY.add(0.04);
    	ROTAION_SPEED_ARRAY.add(0.06);
    	
    	JSONArray level1 = new JSONArray();
    	level1.add(0); //初始转盘几支口红
    	level1.add(2); //发射几支口红
    	level1.add(0.03);//旋转速度
    	level1.add(30);//限定时间
    	
    	JSONArray level2 = new JSONArray();
    	level2.add(0);
    	level2.add(3);
    	level2.add(0.02);
    	level2.add(15);
    	
    	JSONArray level3 = new JSONArray();
    	level3.add(0);
    	level3.add(4);
    	level3.add(0.01);
    	level3.add(12);
    	
    	JSONObject Level1_PARAMETERS = new JSONObject();
    	Level1_PARAMETERS.put("levelArray", level1);
    	Level1_PARAMETERS.put("ROTAION_SPEED_ARRAY", ROTAION_SPEED_ARRAY);
    	Level1_PARAMETERS.put("rotationAccelerationSpeed", 0.002);

    	JSONObject Level2_PARAMETERS = new JSONObject();
    	Level2_PARAMETERS.put("levelArray", level2);
    	Level2_PARAMETERS.put("ROTAION_SPEED_ARRAY", ROTAION_SPEED_ARRAY);
    	Level2_PARAMETERS.put("rotationAccelerationSpeed", 0.002);
    	
    	JSONObject Level3_PARAMETERS = new JSONObject();
    	Level3_PARAMETERS.put("levelArray", level3);
    	Level3_PARAMETERS.put("ROTAION_SPEED_ARRAY", ROTAION_SPEED_ARRAY);
    	Level3_PARAMETERS.put("rotationAccelerationSpeed", 0.002);
    	
    	//Level1_PARAMETERS.put("levelArray", levelArray);
    	//Level1_PARAMETERS.put("levelArray", levelArray);
    	
    	JSONObject fail_PARAMETERS = new JSONObject();
    	fail_PARAMETERS.put("startJudgeCheatDistanceNum", -1);
    	
    	resJSON.put("Level1_PARAMETERS", Level1_PARAMETERS);
    	resJSON.put("Level2_PARAMETERS", Level2_PARAMETERS);
    	resJSON.put("Level3_PARAMETERS", Level3_PARAMETERS);
    	
    	resJSON.put("fail_PARAMETERS", fail_PARAMETERS);
    	
    	renderJson(resJSON);
    }
    
}
