package cn.joytur.modules.system.controller.wap;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.api.SnsAccessToken;
import com.jfinal.weixin.sdk.api.SnsAccessTokenApi;
import com.jfinal.weixin.sdk.api.SnsApi;
import com.jfinal.weixin.sdk.api.UserApi;

import cn.hutool.http.HttpUtil;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.Enums.SortType;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JWTUtil;
import cn.joytur.common.utils.JoyApiConfigUtil;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.entites.GoodsOrder;
import cn.joytur.modules.product.entities.Goods;
import cn.joytur.modules.product.entities.GoodsCategory;
import cn.joytur.modules.system.entities.SysGfw;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatSubscribe;
import cn.joytur.modules.wechat.service.WechatMemberService;

/**
 * 用户主页
 * @author xuhang
 */
@RouteMapping(url = "/wap")
public class WapIndexController extends BaseWapController {

	/**
	 * 主页中心
	 */
	public void index() {
		//未登录去授权登录
		WapMemberDTO dto = getWapMemberDTO();
		
		WechatMember tmpWechatMember = null;
		if(dto != null){
			tmpWechatMember = WechatMember.dao.findById(dto.getWechatMemberId());
		}
        
		if(tmpWechatMember == null){
			String openid = PropKit.get(CommonAttribute.SYSTEM_DEBUG_OPENID);
			if(StrKit.notBlank(openid) && PropKit.getBoolean(CommonAttribute.SYSTEM_DEV_MODE) == true) {
				//进行登录操作
				tmpWechatMember = WechatMember.dao.findByModel(new WechatMember().setOpenid(openid));
				createWapMemberDTO(tmpWechatMember);
			} else {
				String k = getPara("k"); //授权令登录
				if(StrKit.notBlank(k) && k.length() == 32){ //不为空则登录操作
					tmpWechatMember = WechatMember.dao.findByModel(new WechatMember().setToken(k).setStatus(1L));
					if(tmpWechatMember != null){
						//直接登录
						createWapMemberDTO(tmpWechatMember);
					}
				}else{
					String code = getPara("code");
					WechatSubscribe tmpWechatSubscribe = WechatSubscribe.dao.findDefault();
					String calbackUrl = JoyConfigUtil.getSdUrl() + "/wap/oauth";
					String url = SnsAccessTokenApi.getAuthorizeURL(tmpWechatSubscribe.getAppId(), calbackUrl, code, false);
					redirect(url); return;
				}
			}
			
		}
		
		//获取所有商品
		List<Goods> goodsList = Goods.dao.findList(new Goods().setStatus(1L), new Sort("score_price", SortType.ASC));
		//获取所有商品分类
		List<GoodsCategory> goodsCategoryList = GoodsCategory.dao.findList(new GoodsCategory().setStatus(1L));
		
		//获取6条闯关成功信息
		List<GoodsOrder> goodsOrderList = GoodsOrder.dao.findNoticeList(6);
		
		//组装分类
		Map<String, List<Goods>> goodsMap = new LinkedHashMap<String, List<Goods>>();
		for(GoodsCategory cat : goodsCategoryList) {
			goodsMap.put(cat.getId(), new ArrayList<Goods>());
		}
		
		for(Goods goods : goodsList) {
			if(!goodsMap.containsKey(goods.getCategoryId())) {
				goodsMap.put(goods.getCategoryId(), new ArrayList<Goods>());
			}
			goodsMap.get(goods.getCategoryId()).add(goods);
		}
		
		setAttr("goodsMap", goodsMap);
		setAttr("goodsList", goodsList);
		setAttr("goodsCategoryList", goodsCategoryList);
		setAttr("goodsOrderList", goodsOrderList);
		renderWap("index.html");
	}
	
	
	public void notice(){
		String content = "通知";
		renderJson(RenderResult.success(content));
	}
	
	/**
	 * wechat授权
	 */
	public void oauth() {
		Long subscribe = 0L;
		// 用户同意授权，获取code
		String code = getPara("code");
		String state = getPara("state");
		
		String acceptCode = state;
		String appCode = null;
		if(StrKit.notBlank(state) && state.length() == 8){ //第三方跳转
			acceptCode= state.substring(0,4);
			appCode = state.substring(4);
		}
		
		if (code != null) {
			WechatSubscribe tmpWechatSubscribe = WechatSubscribe.dao.findDefault();
			ApiConfig apiConfig = new ApiConfig(tmpWechatSubscribe.getToken(), tmpWechatSubscribe.getAppId(), tmpWechatSubscribe.getAppSecret());
			JoyApiConfigUtil.putApiConfig(apiConfig, tmpWechatSubscribe.getId(), String.valueOf(tmpWechatSubscribe.getAppType()));
			
			String appId = tmpWechatSubscribe.getAppId();
			String secret = tmpWechatSubscribe.getAppSecret();
			// 通过code换取网页授权access_token
			SnsAccessToken snsAccessToken = SnsAccessTokenApi.getSnsAccessToken(appId, secret, code);
			// String json=snsAccessToken.getJson();
			String token = snsAccessToken.getAccessToken();
			String openId = snsAccessToken.getOpenid();
			// 拉取用户信息(需scope为 snsapi_userinfo)
			ApiResult apiResult = SnsApi.getUserInfo(token, openId);

			// log.warn("getUserInfo:"+apiResult.getJson());
			if (apiResult.isSucceed()) {
				JSONObject jsonObject = JSON.parseObject(apiResult.getJson());
				String nickname = jsonObject.getString("nickname");
				// 用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
				Long sex = jsonObject.getLongValue("sex");
				String city = jsonObject.getString("city");// 城市
				String province = jsonObject.getString("province");// 省份
				String country = jsonObject.getString("country");// 国家
				String headimgurl = jsonObject.getString("headimgurl");
				String unionid = jsonObject.getString("unionid");
				String language = jsonObject.getString("language");
				// 获取用户信息判断是否关注
				ApiResult userInfo = UserApi.getUserInfo(openId);
				// log.warn(JsonKit.toJson("is subsribe>>"+userInfo));
				if (userInfo.isSucceed()) {
					String userStr = userInfo.toString();
					subscribe = JSON.parseObject(userStr).getLongValue("subscribe");
				}

				WechatMember tmpWechatMember = WechatMember.dao.findByModel(new WechatMember().setSubscribeId(tmpWechatSubscribe.getId()).setOpenid(openId));
				if (tmpWechatMember == null) {
					// 创建保存用户
					tmpWechatMember = new WechatMember();
					tmpWechatMember.setOpenid(openId);
					tmpWechatMember.setSubscribe(subscribe);
					tmpWechatMember.setSubscribeId(tmpWechatSubscribe.getId());
					tmpWechatMember.setToken(JoyIdUtil.simpleUUID());
					
					if (StrKit.notBlank(nickname)) tmpWechatMember.setNickName(nickname);
					if (sex != null) tmpWechatMember.setSex(sex);
					if (StrKit.notBlank(language)) tmpWechatMember.setLanguage(language);
					if (StrKit.notBlank(country)) tmpWechatMember.setCountry(country);
					if (StrKit.notBlank(province)) tmpWechatMember.setProvince(province);
					if (StrKit.notBlank(city)) tmpWechatMember.setCity(city);
					if (StrKit.notBlank(headimgurl)) tmpWechatMember.setHeadimgUrl(headimgurl);
					if (StrKit.notBlank(unionid)) tmpWechatMember.setUnionid(unionid);

					Duang.duang(WechatMemberService.class).saveWechatMemberProfit(tmpWechatMember, acceptCode);
				} else {
					
					if(StrKit.notBlank(nickname))  tmpWechatMember.setNickName(nickname);
					if(sex != null) tmpWechatMember.setSex(sex);
					if(StrKit.notBlank(language)) tmpWechatMember.setLanguage(language);
					if(StrKit.notBlank(country)) tmpWechatMember.setCountry(country);
					if(StrKit.notBlank(province)) tmpWechatMember.setProvince(province);
					if(StrKit.notBlank(city)) tmpWechatMember.setCity(city);
					if(StrKit.notBlank(headimgurl)) tmpWechatMember.setHeadimgUrl(headimgurl);
					if (StrKit.notBlank(unionid)) tmpWechatMember.setUnionid(unionid);
					
					tmpWechatMember.setToken(JoyIdUtil.simpleUUID());
					tmpWechatMember.setSubscribe(subscribe);
					tmpWechatMember.setUpdateTime(new java.util.Date());
					tmpWechatMember.update();
				}
			
				String redirectUrl = JoyConfigUtil.getMdUrl() + "/wap/index";
				
				if(appCode != null){ //第三方跳转
					String tmpRURL = SysGfw.dao.findOtherSlaveUrl(appCode);
					if(StrKit.notBlank(tmpRURL)){ //不为空
						//注册第三方应用
						try{
							Map<String, Object> paramMap = Maps.newHashMap();
							paramMap.put("m", tmpWechatMember.toJson());
							paramMap.put("code", acceptCode);
							String res = HttpUtil.post(tmpRURL + "/wap/mauth", paramMap);
							
							RenderResult<?> result = JsonKit.parse(res, RenderResult.class);
							if(result.getCode() == 200 && result.isSuccess()){
								redirectUrl = tmpRURL + "/wap/index?k=" + tmpWechatMember.getToken();
							}
						}catch(Exception e){
							LOGGER.error(e.getMessage(), e);
							redirectUrl = tmpRURL + "/wap/noservice";
						}
					}
				}else{
					//进行登录操作
					createWapMemberDTO(tmpWechatMember);
				}
				
				redirect(redirectUrl);
			}
			
		} else {
			renderJson(RenderResult.success(RenderResultCode.COMMON_161));
		}
	}
	
	
	private void createWapMemberDTO(WechatMember tmpWechatMember){
		//进行登录操作
		int maxAgeInSeconds = 60 * 60 * 24 * 7;	//默认7天
		
		//获取用户权限
		WapMemberDTO wapMemberDTO = new WapMemberDTO();
		wapMemberDTO.setWechatMemberId(tmpWechatMember.getId());
		wapMemberDTO.setOpenid(tmpWechatMember.getOpenid());
		wapMemberDTO.setNickname(tmpWechatMember.getNickName());
		wapMemberDTO.setHeadimgurl(tmpWechatMember.getHeadimgUrl());
		wapMemberDTO.setVersion(PropKit.get(CommonAttribute.SYSTEM_VERSION));
		
		setCookie(JWTUtil.MEMBER, JWTUtil.createJWT(JWTUtil.MEMBER, JsonKit.toJson(wapMemberDTO)), maxAgeInSeconds);
		
	}
	
	/**
	 * 调用认证
	 */
	@Before(POST.class)
	public void mauth(){
		String m = getPara("m");
		String acceptCode = getPara("code");
		WechatMember tmpWechatMember = JsonKit.parse(m, WechatMember.class);
		
		if(WechatMember.dao.findCountByModel(new WechatMember().setOpenid(tmpWechatMember.getOpenid())) > 0){
			renderJson(RenderResult.success()); return;
		}
		
		Duang.duang(WechatMemberService.class).saveWechatMemberProfit(tmpWechatMember, acceptCode);
		
		renderJson(RenderResult.success()); return;
	}
	
	/**
	 * 无服务
	 */
	public void noservice(){
		render("/templates/" + PropKit.get(CommonAttribute.SYSTEM_THEME) + "/admin/error/no_service.html");
	}
	
	
}