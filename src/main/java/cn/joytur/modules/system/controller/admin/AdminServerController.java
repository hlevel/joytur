package cn.joytur.modules.system.controller.admin;

import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.server.Server;

/**
 * 服务器监控
 * @author xuhang
 */
@RouteMapping(url = "${admin}/server")
@AuthRequire.Logined
public class AdminServerController extends BaseAdminController {

	/**
	 * 主页中心
	 */
	public void index() {
		Server server = new Server();
        try {
			server.copyTo();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
        
        setAttr("server", server);
		
		renderTpl("system/server/index.html");
	}
	
}