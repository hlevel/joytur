package cn.joytur.modules.system.controller.wap;

import com.alibaba.fastjson.JSONObject;

import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;

/**
 * wap登录 暂未实现
 * @author xuhang
 */
@RouteMapping(url = "/wap/login")
public class WapLoginController extends BaseWapController {

	/**
	 * 登录页面
	 */
	public void index() {
		renderWap("login.html");
	}

	public void loading() {
		JSONObject reJSON = new JSONObject();
		reJSON.put("statu", 12);
		reJSON.put("loading_url", "/static/default/wap/img/fx.png");
		renderJson(RenderResult.success(reJSON));
	}

}