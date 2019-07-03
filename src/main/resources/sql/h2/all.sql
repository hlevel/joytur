#include("system/sys_menu.sql")
#include("system/sys_user.sql")
#include("system/sys_login_log.sql")
#include("system/sys_oper_log.sql")
#include("system/sys_gfw.sql")

#include("wechat/wechat_template.sql")
#include("wechat/wechat_member.sql")
#include("wechat/wechat_member_profit.sql")

#include("order/account.sql")
#include("order/account_funds.sql")
#include("order/goods_order.sql")
#include("order/recharge_order.sql")
#include("order/recommend_order.sql")
#include("order/recharge_rule.sql")
#include("order/recharge_rule_qrcode.sql")

#include("product/extension_adv.sql")
#include("product/goods.sql")
#include("product/goods_game.sql")

#namespace("common")
	#sql("findList")
		select * from #(tab) #for(x : cond) #(for.first ? "where": "and") #(x.key) #para(x.value) #end #if(sort) order by #(sort.name) #(sort.sortType) #end
	#end
#end