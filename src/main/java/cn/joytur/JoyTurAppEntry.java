package cn.joytur;

import com.jfinal.server.undertow.UndertowServer;

import cn.joytur.common.mvc.JoyTurConfig;
import cn.joytur.common.mvc.constant.CommonAttribute;

/**
 * 启动入口类
 * @author xuhang
 * @time 2019年6月27日 下午5:58:07
 * @email hlevel@qq.com
 */
public class JoyTurAppEntry {

	public static void main(String[] args) {
		
		UndertowServer.create(JoyTurConfig.class, CommonAttribute.CONFIG_PROPERTIES).configWeb(builder -> {

			// 配置 Servlet
			builder.addServlet("H2Console", "org.h2.server.web.WebServlet");
			builder.addServletMapping("H2Console", "/h2/console/*");
			
		}).start();
	}
	
}
