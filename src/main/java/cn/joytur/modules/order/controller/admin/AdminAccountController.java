package cn.joytur.modules.order.controller.admin;

import com.jfinal.plugin.activerecord.Page;

import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.modules.order.entites.Account;

/**
 * @author xuhang
 */
@RouteMapping(url = "${admin}/account")
public class AdminAccountController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("order.account.view")
	public void index(Account account) {
		account.put("openid", getPara("accountFunds.openid"));
		account.put("nickName", getPara("accountFunds.nickName"));
		account.put("accType", getParaToLong("accountFunds.accType"));
		
		Page<Account> pageList = Account.dao.paginate(getPage(), getSize(), account, new Sort("create_time", Enums.SortType.DESC));
		
		setAttr("page", pageList);
		setAttr("account", account);
		renderTpl("order/account/index.html");
	}
    
}