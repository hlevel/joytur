package cn.joytur.modules.order.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.order.entites.AccountFunds;
import cn.joytur.modules.system.entities.SysDictionary;

/**
 * 账户
 * @author xuhang
 */
@RouteMapping(url = "${admin}/account/funds")
public class AdminAccountFundsController extends BaseAdminController {

	/**
	 * 列表
	 */
	@AuthRequire.Perms("account.funds.view")
	public void index(AccountFunds accountFunds) {
		
		accountFunds.put("openid", getPara("accountFunds.openid"));
		accountFunds.put("nickName", getPara("accountFunds.nickName"));
		accountFunds.put("accType", getParaToLong("accountFunds.accType"));
		
		//查询条件过滤
		List<SysDictionary> accTypeList = JoyDictUtil.getDictList(DictAttribute.ACCOUNT_ACC_TYPE);
		List<SysDictionary> quyAccTypeList = new ArrayList<>();
		String insideGame = JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_TYPE_INSIDE_GAME, DictAttribute.ACCOUNT_ACC_TYPE, "");
		String insideSett = JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_TYPE_INSIDE_SETT, DictAttribute.ACCOUNT_ACC_TYPE, "");
		for(SysDictionary accType : accTypeList){
			if(StrKit.equals(accType.getDictValue(), insideGame) || StrKit.equals(accType.getDictValue(), insideSett)){
				continue;
			}
			quyAccTypeList.add(accType);
		}
		
		//查询数据
		Page<AccountFunds> pageList = AccountFunds.dao.paginate(getPage(), getSize(), accountFunds, new Sort("create_time", Enums.SortType.DESC));
		
		setAttr("quyAccType", quyAccTypeList);
		setAttr("page", pageList);
		setAttr("accountFunds", accountFunds);
		renderTpl("order/funds/index.html");
	}
	
}