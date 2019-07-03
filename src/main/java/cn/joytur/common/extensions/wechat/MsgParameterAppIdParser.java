package cn.joytur.common.extensions.wechat;

import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.weixin.sdk.jfinal.AppIdParser;

/**
 * 自定义获取appid
 * @author xuhang
 * @time 2019年1月17日 下午12:52:17
 */
public class MsgParameterAppIdParser implements AppIdParser {

	@Override
	public String getAppId(Invocation inv) {
		return getAppId(inv.getController());
	}

	@Override
	public String getAppId(Controller ctl) {
		return ctl.getPara();
	}
	
}
