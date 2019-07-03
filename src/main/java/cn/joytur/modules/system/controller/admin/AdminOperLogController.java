package cn.joytur.modules.system.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.modules.system.entities.SysLoginLog;
import cn.joytur.modules.system.entities.SysOperLog;

/**
 * 日志管理
 * @author xuhang
 */
@RouteMapping(url = "${admin}/operlog")
public class AdminOperLogController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("sys.operlog.view")
	public void index(SysOperLog sysOperLog) {
		Page<SysOperLog> pageList = SysOperLog.dao.paginate(getPage(), getSize(), sysOperLog);
		setAttr("page", pageList);
		setAttr("sysOperLog", sysOperLog);
		renderTpl("system/operlog/index.html");
	}
	
    
    /**
     * 删除数据
     * @param ids
     */
    @AuthRequire.Perms("sys.operlog.delete")
    @Before(Tx.class)
    public void delete(){
    	
    	SysOperLog.dao.deleteAll();
    	SysLoginLog.dao.deleteAll();
    	
		renderJson(RenderResult.success());
	}
    
}