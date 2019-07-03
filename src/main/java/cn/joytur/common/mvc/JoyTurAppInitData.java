package cn.joytur.common.mvc;

import com.jfinal.aop.Duang;
import com.jfinal.kit.PropKit;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.modules.order.entites.Account;
import cn.joytur.modules.product.entities.Goods;
import cn.joytur.modules.product.entities.GoodsCategory;
import cn.joytur.modules.product.entities.GoodsRule;
import cn.joytur.modules.product.entities.RechargeRule;
import cn.joytur.modules.product.entities.RecommendRule;
import cn.joytur.modules.system.entities.SysConfig;
import cn.joytur.modules.system.entities.SysDictionary;
import cn.joytur.modules.system.entities.SysMenu;
import cn.joytur.modules.system.entities.SysUser;
import cn.joytur.modules.wechat.entities.WechatMember;
import cn.joytur.modules.wechat.entities.WechatSubscribe;
import cn.joytur.modules.wechat.entities.WechatTemplate;
import cn.joytur.modules.wechat.service.WechatMemberService;
import cn.joytur.modules.wechat.service.WechatTemplateService;

/**
 * 初始化基础数据
 * @author xuhang
 * @time 2019年1月10日 上午10:50:16
 */
public class JoyTurAppInitData {

	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(JoyTurAppInitData.class);
	
	/**
	 * 初始化基础数据
	 */
	public void initData(){
		LOGGER.info("初始化基础数据.");
		sysMenuData();
		sysDictData();
		accountData();
		sysUserData();
		productDefaultRecommendRule();
		sysConfigData();
		LOGGER.info("初始化基础数据完成.");
		
		if(PropKit.getBoolean(CommonAttribute.SYSTEM_DEV_MODE) == true){
			bussinseDebugData();
		}

	}
	
	/**
	 * 初始化菜单
	 */
	public void sysMenuData(){
		if(SysMenu.dao.findCountByModel(new SysMenu())  == 0){
			//一级菜单
			SysMenu sysMenu0 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("主页").setUrl("/main").setPid("0").setPermission("").setPath("0").setIcon("layui-icon layui-icon-home").setSort(0L).setType(1L).setCreateTime(new java.util.Date());
			sysMenu0.save();
			
			//一级菜单
			SysMenu orderMenu0 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("订单管理").setUrl("#").setPid("0").setPermission(null).setPath("0").setIcon("fa fa-first-order").setType(1L).setSort(1l).setCreateTime(new java.util.Date());
			orderMenu0.save();
			
				//二级菜单
				SysMenu orderMenu3 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("发货订单").setUrl("/goods/order/index").setPid(orderMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
				orderMenu3.save();
					SysMenu orderMenu31 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/goods/order/add").setPid(orderMenu3.getId()).setPermission("goods.order.add").setPath("0," + orderMenu0.getId() + "," + orderMenu3.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu orderMenu32 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/goods/order/edit").setPid(orderMenu3.getId()).setPermission("goods.order.edit").setPath("0," + orderMenu0.getId() + "," + orderMenu3.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu orderMenu34 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/goods/order/save").setPid(orderMenu3.getId()).setPermission("goods.order.save").setPath("0," + orderMenu0.getId() + "," + orderMenu3.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					orderMenu31.save();orderMenu32.save();orderMenu34.save();
					
				SysMenu orderMenu1 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("充值订单").setUrl("/recharge/order/index").setPid(orderMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
				orderMenu1.save();
					SysMenu orderMenu11 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/recharge/order/add").setPid(orderMenu1.getId()).setPermission("recharge.order.add").setPath("0," + orderMenu0.getId() + "," + orderMenu1.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu orderMenu12 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/recharge/order/edit").setPid(orderMenu1.getId()).setPermission("recharge.order.edit").setPath("0," + orderMenu0.getId() + "," + orderMenu1.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu orderMenu13 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("处理为已收款").setUrl("/recharge/order/status").setPid(orderMenu1.getId()).setPermission("recharge.order.status").setPath("0," + orderMenu0.getId() + "," + orderMenu1.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu orderMenu14 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/recharge/order/save").setPid(orderMenu1.getId()).setPermission("recharge.order.save").setPath("0," + orderMenu0.getId() + "," + orderMenu1.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					orderMenu11.save();orderMenu12.save();orderMenu13.save();orderMenu14.save();
				
				SysMenu orderMenu2 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("提现订单").setUrl("/recommend/order/index").setPid(orderMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
				orderMenu2.save();
					SysMenu orderMenu21 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("打款").setUrl("/recommend/order/paycash").setPid(orderMenu2.getId()).setPermission("recommend.order.paycash").setPath("0," + orderMenu0.getId() + "," + orderMenu2.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu orderMenu22 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("打款完成").setUrl("/recommend/order/paycashfinish").setPid(orderMenu2.getId()).setPermission("recommend.order.paycashfinish").setPath("0," + orderMenu0.getId() + "," + orderMenu2.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu orderMenu23 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/recommend/order/add").setPid(orderMenu2.getId()).setPermission("recommend.order.add").setPath("0," + orderMenu0.getId() + "," + orderMenu2.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu orderMenu24 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/recommend/order/edit").setPid(orderMenu2.getId()).setPermission("recommend.order.edit").setPath("0," + orderMenu0.getId() + "," + orderMenu2.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu orderMenu25 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/recommend/order/save").setPid(orderMenu2.getId()).setPermission("recommend.order.save").setPath("0," + orderMenu0.getId() + "," + orderMenu2.getId()).setType(2L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
					orderMenu21.save();orderMenu22.save();orderMenu23.save();orderMenu24.save();orderMenu25.save();

				SysMenu orderMenu6 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("游戏记录").setUrl("/goods/game/index").setPid(orderMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
				orderMenu6.save();
					SysMenu orderMenu61 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("退款").setUrl("/goods/game/refund").setPid(orderMenu6.getId()).setPermission("product.goods.game.refund").setPath("0," + orderMenu0.getId() + "," + orderMenu2.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					orderMenu61.save();



				SysMenu orderMenu4 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("会员账单").setUrl("/account/funds/index").setPid(orderMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
				orderMenu4.save();
					SysMenu orderMenu41 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("对账").setUrl("/account/funds/bill").setPid(orderMenu4.getId()).setPermission("capital.fund.add").setPath("0," + orderMenu0.getId() + "," + orderMenu4.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					orderMenu41.save();

				SysMenu orderMenu5 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("会员账户").setUrl("/account/index").setPid(orderMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
				orderMenu5.save();
					SysMenu orderMenu51 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/account/add").setPid(orderMenu5.getId()).setPermission("order.account.add").setPath("0," + orderMenu0.getId() + "," + orderMenu5.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu orderMenu52 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/account/edit").setPid(orderMenu5.getId()).setPermission("order.account.edit").setPath("0," + orderMenu0.getId() + "," + orderMenu5.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu orderMenu56 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/account/save").setPid(orderMenu5.getId()).setPermission("order.account.save").setPath("0," + orderMenu0.getId() + "," + orderMenu5.getId()).setType(2L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
					orderMenu51.save();orderMenu52.save();orderMenu56.save();
					
			//一级菜单
			SysMenu productMenu0 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("产品运营").setUrl("#").setPid("0").setPermission(null).setPath("0").setIcon("fa fa-product-hunt").setType(1L).setSort(2l).setCreateTime(new java.util.Date());
			productMenu0.save();
				SysMenu productMenu1 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("商品列表").setUrl("/goods/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
				productMenu1.save();
					SysMenu productMenu11 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/goods/add").setPid(productMenu1.getId()).setPermission("product.goods.add").setPath("0," + productMenu0.getId() + "," + productMenu1.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu12 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/goods/edit").setPid(productMenu1.getId()).setPermission("product.goods.edit").setPath("0," + productMenu0.getId() + "," + productMenu1.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu13 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/goods/delete").setPid(productMenu1.getId()).setPermission("product.goods.delete").setPath("0," + productMenu0.getId() + "," + productMenu1.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu14 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/goods/save").setPid(productMenu1.getId()).setPermission("product.goods.save").setPath("0," + productMenu0.getId() + "," + productMenu1.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu11.save();productMenu12.save();productMenu13.save();productMenu14.save();

				SysMenu productMenu2 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("商品分类").setUrl("/goods/category/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
				productMenu2.save();
					SysMenu productMenu21 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/goods/category/add").setPid(productMenu2.getId()).setPermission("product.goodscategory.add").setPath("0," + productMenu0.getId() + "," + productMenu2.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu22 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/goods/category/edit").setPid(productMenu2.getId()).setPermission("product.goodscategory.edit").setPath("0," + productMenu0.getId() + "," + productMenu2.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu23 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/goods/category/delete").setPid(productMenu2.getId()).setPermission("product.goodscategory.delete").setPath("0," + productMenu0.getId() + "," + productMenu2.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu24 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/goods/category/save").setPid(productMenu2.getId()).setPermission("product.goodscategory.save").setPath("0," + productMenu0.getId() + "," + productMenu2.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu21.save();productMenu22.save();productMenu23.save();productMenu24.save();

				SysMenu productMenu3 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("闯关设置").setUrl("/goods/rule/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
				productMenu3.save();
					SysMenu productMenu31 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/goods/rule/add").setPid(productMenu3.getId()).setPermission("product.goods.rule.add").setPath("0," + productMenu0.getId() + "," + productMenu3.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu32 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/goods/rule/edit").setPid(productMenu3.getId()).setPermission("product.goods.rule.edit").setPath("0," + productMenu0.getId() + "," + productMenu3.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu33 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/goods/rule/delete").setPid(productMenu3.getId()).setPermission("product.goods.rule.delete").setPath("0," + productMenu0.getId() + "," + productMenu3.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu34 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/goods/rule/save").setPid(productMenu3.getId()).setPermission("product.goods.rule.save").setPath("0," + productMenu0.getId() + "," + productMenu3.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu31.save();productMenu32.save();productMenu33.save();productMenu34.save();

				SysMenu productMenu5 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("代理设置").setUrl("/recommend/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
				productMenu5.save();
					SysMenu productMenu51 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/recommend/add").setPid(productMenu5.getId()).setPermission("product.recommend.add").setPath("0," + productMenu0.getId() + "," + productMenu5.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu52 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/recommend/edit").setPid(productMenu5.getId()).setPermission("product.recommend.edit").setPath("0," + productMenu0.getId() + "," + productMenu5.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu53 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/recommend/delete").setPid(productMenu5.getId()).setPermission("product.recommend.delete").setPath("0," + productMenu0.getId() + "," + productMenu5.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu54 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/recommend/save").setPid(productMenu5.getId()).setPermission("product.recommend.save").setPath("0," + productMenu0.getId() + "," + productMenu5.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu51.save();productMenu52.save();productMenu53.save();productMenu54.save();

				SysMenu productMenu4 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("充值设置").setUrl("/recharge/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
				productMenu4.save();
					SysMenu productMenu41 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/recharge/add").setPid(productMenu4.getId()).setPermission("product.recharge.add").setPath("0," + productMenu0.getId() + "," + productMenu4.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu42 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/recharge/edit").setPid(productMenu4.getId()).setPermission("product.recharge.edit").setPath("0," + productMenu0.getId() + "," + productMenu4.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu43 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/recharge/delete").setPid(productMenu4.getId()).setPermission("product.recharge.delete").setPath("0," + productMenu0.getId() + "," + productMenu4.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu44 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/recharge/save").setPid(productMenu4.getId()).setPermission("product.recharge.save").setPath("0," + productMenu0.getId() + "," + productMenu4.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu41.save();productMenu42.save();productMenu43.save();productMenu44.save();

				SysMenu productMenu7 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("推广设置").setUrl("/extension/rule/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
				productMenu7.save();
					SysMenu productMenu71 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/extension/rule/add").setPid(productMenu7.getId()).setPermission("product.extension.rule.add").setPath("0," + productMenu0.getId() + "," + productMenu7.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu72 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/extension/rule/edit").setPid(productMenu7.getId()).setPermission("product.extension.rule.edit").setPath("0," + productMenu0.getId() + "," + productMenu7.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu74 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/extension/rule/save").setPid(productMenu7.getId()).setPermission("product.extension.rule.save").setPath("0," + productMenu0.getId() + "," + productMenu7.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu75 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/goods/rule/delete").setPid(productMenu7.getId()).setPermission("product.extension.rule.delete").setPath("0," + productMenu0.getId() + "," + productMenu7.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu productMenu76 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("修改状态").setUrl("/goods/rule/status").setPid(productMenu7.getId()).setPermission("product.extension.rule.status").setPath("0," + productMenu0.getId() + "," + productMenu7.getId()).setType(2L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
					productMenu71.save();productMenu72.save();productMenu74.save();productMenu75.save();productMenu76.save();
				
				SysMenu productMenu8 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("广告位设置").setUrl("/extension/adv/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(7l).setCreateTime(new java.util.Date());
				productMenu8.save();
					SysMenu productMenu81 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/extension/adv/add").setPid(productMenu7.getId()).setPermission("product.extension.adv.add").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu82 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/extension/adv/edit").setPid(productMenu7.getId()).setPermission("product.extension.adv.edit").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu84 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/extension/adv/save").setPid(productMenu7.getId()).setPermission("product.extension.adv.save").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu85 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/extension/adv/delete").setPid(productMenu7.getId()).setPermission("product.extension.adv.delete").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu productMenu86 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("修改状态").setUrl("/extension/adv/status").setPid(productMenu7.getId()).setPermission("product.extension.adv.status").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
					productMenu81.save();productMenu82.save();productMenu84.save();productMenu85.save();productMenu86.save();
				
				SysMenu productMenu9 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("收款二维码").setUrl("/recharge/qrcode/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(8l).setCreateTime(new java.util.Date());
					productMenu9.save();
					SysMenu productMenu91 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/recharge/qrcode/add").setPid(productMenu9.getId()).setPermission("product.recharge.qrcode.add").setPath("0," + productMenu0.getId() + "," + productMenu9.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu92 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/recharge/qrcode/edit").setPid(productMenu9.getId()).setPermission("product.recharge.qrcode.edit").setPath("0," + productMenu0.getId() + "," + productMenu9.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu93 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/recharge/qrcode/delete").setPid(productMenu9.getId()).setPermission("product.recharge.qrcode.delete").setPath("0," + productMenu0.getId() + "," + productMenu9.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu94 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/recharge/qrcode/save").setPid(productMenu9.getId()).setPermission("product.recharge.qrcode.save").setPath("0," + productMenu0.getId() + "," + productMenu9.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu91.save();productMenu92.save();productMenu93.save();productMenu94.save();
					
				SysMenu productMenu6 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("收款监控").setUrl("/recharge/monitor/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + productMenu0.getId()).setType(1L).setIcon(null).setSort(9l).setCreateTime(new java.util.Date());
				productMenu6.save();
					SysMenu productMenu61 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/recharge/monitor/add").setPid(productMenu6.getId()).setPermission("product.recharge.monitor.add").setPath("0," + productMenu0.getId() + "," + productMenu6.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu productMenu62 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/recharge/monitor/edit").setPid(productMenu6.getId()).setPermission("product.recharge.monitor.edit").setPath("0," + productMenu0.getId() + "," + productMenu6.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu productMenu63 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/recharge/monitor/delete").setPid(productMenu6.getId()).setPermission("product.recharge.monitor.delete").setPath("0," + productMenu0.getId() + "," + productMenu6.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu productMenu64 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/recharge/monitor/save").setPid(productMenu6.getId()).setPermission("product.recharge.monitor.save").setPath("0," + productMenu0.getId() + "," + productMenu6.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					productMenu61.save();productMenu62.save();productMenu63.save();productMenu64.save();
				
					
				/*
				SysMenu productMenu8 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("意见反馈").setUrl("/member/feedback/index").setPid(productMenu0.getId()).setPermission(null).setPath("0," + orderMenu0.getId()).setType(1L).setIcon(null).setSort(8l).setCreateTime(new java.util.Date());
				productMenu8.save();
				SysMenu productMenu81 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/member/feedback/add").setPid(productMenu8.getId()).setPermission("member.feedback.add").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
				SysMenu productMenu82 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/member/feedback/edit").setPid(productMenu8.getId()).setPermission("member.feedback.edit").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
				SysMenu productMenu84 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/member/feedback/save").setPid(productMenu8.getId()).setPermission("member.feedback.save").setPath("0," + productMenu0.getId() + "," + productMenu8.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
				productMenu81.save();productMenu82.save();productMenu84.save();
				
				 */
			
			//一级菜单fa fa-cubes
			SysMenu sysMenu100 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("微信管理").setUrl("#").setPid("0");sysMenu100.setPermission(null).setPath("0").setIcon("fa fa-weixin").setType(1L).setSort(4l).setCreateTime(new java.util.Date());
			sysMenu100.save();
				SysMenu sysMenu101 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("会员管理").setUrl("/member/index").setPid(sysMenu100.getId()).setPermission(null).setPath("0," + sysMenu100.getId()).setIcon(null).setType(1L).setSort(1l).setCreateTime(new java.util.Date());
				sysMenu101.save();
					SysMenu sysMenu102 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/member/add").setPid(sysMenu101.getId()).setPermission("wechat.member.add").setPath("0," + sysMenu100.getId() + "," + sysMenu101.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu103 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/member/edit").setPid(sysMenu101.getId()).setPermission("wechat.member.edit").setPath("0," + sysMenu100.getId() + "," + sysMenu101.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu105 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("修改状态").setUrl("/member/status").setPid(sysMenu101.getId()).setPermission("wechat.member.status").setPath("0," + sysMenu100.getId() + "," + sysMenu101.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu sysMenu106 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/member/save").setPid(sysMenu101.getId()).setPermission("wechat.member.save").setPath("0," + sysMenu100.getId() + "," + sysMenu101.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					sysMenu102.save();sysMenu103.save();sysMenu105.save();sysMenu106.save();
					
				SysMenu sysMenu121 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("公众号管理").setUrl("/subscribe/index").setPid(sysMenu100.getId()).setPermission(null).setPath("0," + sysMenu100.getId()).setType(1L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
				sysMenu121.save();
					SysMenu sysMenu123 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/subscribe/save").setPid(sysMenu121.getId()).setPermission("wechat.subscribe.save").setPath("0," + sysMenu100.getId() + "," + sysMenu121.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu125 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("修改状态").setUrl("/subscribe/status").setPid(sysMenu121.getId()).setPermission("wechat.subscribe.status").setPath("0," + sysMenu100.getId() + "," + sysMenu121.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu sysMenu128 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("维护菜单").setUrl("/subscribe/menuSave").setPid(sysMenu121.getId()).setPermission("wechat.subscribe.menuSave").setPath("0," + sysMenu100.getId() + "," + sysMenu121.getId()).setType(2L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
					sysMenu123.save();sysMenu125.save();sysMenu128.save();
					
				SysMenu sysMenu131 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("模版管理").setUrl("/template/index").setPid(sysMenu100.getId()).setPermission(null).setType(1L).setIcon(null).setPath("0," + sysMenu100.getId()).setSort(4l).setCreateTime(new java.util.Date());
				sysMenu131.save();
					SysMenu sysMenu132 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/template/add").setPid(sysMenu131.getId()).setPermission("wechat.template.add").setPath("0," + sysMenu100.getId() + "," + sysMenu131.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu133 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/template/edit").setPid(sysMenu131.getId()).setPermission("wechat.template.edit").setPath("0," + sysMenu100.getId() + "," + sysMenu131.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu135 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/template/delete").setPid(sysMenu131.getId()).setPermission("wechat.template.delete").setPath("0," + sysMenu100.getId() + "," + sysMenu131.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu sysMenu136 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("修改状态").setUrl("/template/status").setPid(sysMenu131.getId()).setPermission("wechat.template.status").setPath("0," + sysMenu100.getId() + "," + sysMenu131.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu sysMenu137 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/template/save").setPid(sysMenu131.getId()).setPermission("wechat.template.save").setPath("0," + sysMenu100.getId() + "," + sysMenu131.getId()).setType(2L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
					sysMenu132.save();sysMenu133.save();sysMenu135.save();sysMenu136.save();sysMenu137.save();
					
			//一级菜单
			SysMenu sysMenu1 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("系统管理").setUrl("#").setPid("0").setPermission(null).setPath("0").setIcon("fa fa-cog").setType(1L).setSort(5l).setCreateTime(new java.util.Date());
			sysMenu1.save();
				SysMenu sysMenu3 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("用户列表").setUrl("/user/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
				sysMenu3.save();
					SysMenu sysMenu13 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/user/add").setPid(sysMenu3.getId()).setPermission("sys.user.add").setPath("0," + sysMenu1.getId() + "," + sysMenu3.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu19 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/user/edit").setPid(sysMenu3.getId()).setPermission("sys.user.edit").setPath("0," + sysMenu1.getId() + "," + sysMenu3.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu30 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("分配").setUrl("/user/role").setPid(sysMenu3.getId()).setPermission("sys.user.role").setPath("0," + sysMenu1.getId() + "," + sysMenu3.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu sysMenu35 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/user/delete").setPid(sysMenu3.getId()).setPermission("sys.user.delete").setPath("0," + sysMenu1.getId() + "," + sysMenu3.getId()).setType(2L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
					SysMenu sysMenu36 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/user/save").setPid(sysMenu3.getId()).setPermission("sys.user.save").setPath("0," + sysMenu1.getId() + "," + sysMenu3.getId()).setType(2L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
					sysMenu13.save();sysMenu19.save();sysMenu30.save();sysMenu35.save();sysMenu36.save();
			
				SysMenu sysMenu5 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("角色列表").setUrl("/role/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
				sysMenu5.save();
					SysMenu sysMenu12 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/role/add").setPid(sysMenu5.getId()).setPermission("sys.role.add").setPath("0," + sysMenu1.getId() + "," + sysMenu5.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu18 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/role/edit").setPid(sysMenu5.getId()).setPermission("sys.role.edit").setPath("0," + sysMenu1.getId() + "," + sysMenu5.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu24 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("授权").setUrl("/role/auth").setPid(sysMenu5.getId()).setPermission("sys.role.auth").setPath("0," + sysMenu1.getId() + "," + sysMenu5.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu sysMenu33 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/role/delete").setPid(sysMenu5.getId()).setPermission("sys.role.delete").setPath("0," + sysMenu1.getId() + "," + sysMenu5.getId()).setType(2L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
					SysMenu sysMenu34 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/role/save").setPid(sysMenu5.getId()).setPermission("sys.role.save").setPath("0," + sysMenu1.getId() + "," + sysMenu5.getId()).setType(2L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
					sysMenu12.save();sysMenu18.save();sysMenu24.save();sysMenu33.save();sysMenu34.save();
					
				SysMenu sysMenu7 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("菜单列表").setUrl("/menu/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
				sysMenu7.save();
					SysMenu sysMenu11 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/menu/add").setPid(sysMenu7.getId()).setPermission("sys.menu.add").setPath("0," + sysMenu1.getId() + "," + sysMenu7.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu17 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/menu/edit").setPid(sysMenu7.getId()).setPermission("sys.menu.edit").setPath("0," + sysMenu1.getId() + "," + sysMenu7.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu28 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/menu/delete").setPid(sysMenu7.getId()).setPermission("sys.menu.delete").setPath("0," + sysMenu1.getId() + "," + sysMenu7.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu sysMenu29 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/menu/save").setPid(sysMenu7.getId()).setPermission("sys.menu.save").setPath("0," + sysMenu1.getId() + "," + sysMenu7.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					sysMenu11.save();sysMenu17.save();sysMenu28.save();sysMenu29.save();
				
				SysMenu sysMenu8 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("系统设置").setUrl("/config/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(5l).setCreateTime(new java.util.Date());
				sysMenu8.save();
					SysMenu sysMenu80 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/config/save").setPid(sysMenu8.getId()).setPermission("sys.config.save").setPath("0," + sysMenu1.getId() + "," + sysMenu8.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					sysMenu80.save();
					
				SysMenu sysMenu16 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("防封管理").setUrl("/gfw/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(6l).setCreateTime(new java.util.Date());
				sysMenu16.save();
					SysMenu sysMenu161 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/gfw/add").setPid(sysMenu16.getId()).setPermission("sys.gfw.add").setPath("0," + sysMenu1.getId() + "," + sysMenu16.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu162 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/gfw/edit").setPid(sysMenu16.getId()).setPermission("sys.gfw.edit").setPath("0," + sysMenu1.getId() + "," + sysMenu16.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu163 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/gfw/delete").setPid(sysMenu16.getId()).setPermission("sys.gfw.delete").setPath("0," + sysMenu1.getId() + "," + sysMenu16.getId()).setType(2L).setIcon(null).setSort(3l).setCreateTime(new java.util.Date());
					SysMenu sysMenu164 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/gfw/save").setPid(sysMenu16.getId()).setPermission("sys.gfw.save").setPath("0," + sysMenu1.getId() + "," + sysMenu16.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					sysMenu161.save();sysMenu162.save();sysMenu163.save();sysMenu164.save();
			
				SysMenu sysMenu9 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("字典列表").setUrl("/dict/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(7l).setCreateTime(new java.util.Date());
				sysMenu9.save();
					SysMenu sysMenu14 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("添加").setUrl("/dict/add").setPid(sysMenu9.getId()).setPermission("sys.dict.add").setPath("0," + sysMenu1.getId() + "," + sysMenu9.getId()).setType(2L).setIcon(null).setSort(1l).setCreateTime(new java.util.Date());
					SysMenu sysMenu20 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("编辑").setUrl("/dict/edit").setPid(sysMenu9.getId()).setPermission("sys.dict.edit").setPath("0," + sysMenu1.getId() + "," + sysMenu9.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					SysMenu sysMenu31 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/dict/delete").setPid(sysMenu9.getId()).setPermission("sys.dict.delete").setPath("0," + sysMenu1.getId() + "," + sysMenu9.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					SysMenu sysMenu32 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("保存").setUrl("/dict/save").setPid(sysMenu9.getId()).setPermission("sys.dict.save").setPath("0," + sysMenu1.getId() + "," + sysMenu9.getId()).setType(2L).setIcon(null).setSort(4l).setCreateTime(new java.util.Date());
					sysMenu14.save();sysMenu20.save();sysMenu31.save();sysMenu32.save();
				
				SysMenu sysMenu10 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("操作日志").setUrl("/operlog/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(8l).setCreateTime(new java.util.Date());
				sysMenu10.save();
					SysMenu sysMenu21 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("删除").setUrl("/operlog/delete").setPid(sysMenu10.getId()).setPermission("sys.log.delete").setPath("0," + sysMenu1.getId() + "," + sysMenu10.getId()).setType(2L).setIcon(null).setSort(2l).setCreateTime(new java.util.Date());
					sysMenu21.save();
					
				SysMenu sysMenu22 = new SysMenu().setId(IdUtil.simpleUUID()).setTitle("服务监控").setUrl("/server/index").setPid(sysMenu1.getId()).setPermission(null).setPath("0," + sysMenu1.getId()).setType(1L).setIcon(null).setSort(9l).setCreateTime(new java.util.Date());
				sysMenu22.save();
					
		}
	}
	
	/**
	 * 初始化字典
	 */
	public void sysDictData(){
		if(SysDictionary.dao.findCountByModel(new SysDictionary())  == 0){
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.MENU_TYPE).setDictName(DictAttribute.MENU_TYPE_MENU).setDictValue("1").setSort(0l).setDescription("菜单类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.MENU_TYPE).setDictName(DictAttribute.MENU_TYPE_AUTHORITY).setDictValue("2").setSort(1l).setDescription("菜单类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.STATUS).setDictName(DictAttribute.STATUS_PROHIBIT).setDictValue("0").setSort(0l).setDescription("状态描述").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.STATUS).setDictName(DictAttribute.STATUS_ENABLE).setDictValue("1").setSort(1l).setDescription("状态描述").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.SEX).setDictName(DictAttribute.SEX_FEMALE).setDictValue("0").setSort(0l).setDescription("性别描述").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.SEX).setDictName(DictAttribute.SEX_MALE).setDictValue("1").setSort(1l).setDescription("性别描述").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GFW_APP_TYPE).setDictName(DictAttribute.GFW_APP_TYPE1).setDictValue("1").setSort(0l).setDescription("防封应用类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GFW_APP_TYPE).setDictName(DictAttribute.GFW_APP_TYPE2).setDictValue("2").setSort(1l).setDescription("防封应用类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.SUBSCRIBE_TYPE).setDictName(DictAttribute.SUBSCRIBE_TYPE_ENTERPRISE).setDictValue("1").setSort(0l).setDescription("公众号类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.SUBSCRIBE_TYPE).setDictName(DictAttribute.SUBSCRIBE_TYPE_SERVICE).setDictValue("2").setSort(1l).setDescription("公众号类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.TEMPLATE_EVENT_TYPE).setDictName(DictAttribute.TEMPLATE_EVENT_TYPE_SYSTEM).setDictValue("1").setSort(0l).setDescription("系统类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.TEMPLATE_EVENT_TYPE).setDictName(DictAttribute.TEMPLATE_EVENT_TYPE_CUSTOM).setDictValue("2").setSort(1l).setDescription("自定义类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.TEMPLATE_RESPONSE_TYPE).setDictName(DictAttribute.TEMPLATE_RESPONSE_TYPE_TEXT).setDictValue("1").setSort(0l).setDescription("文本消息").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.TEMPLATE_RESPONSE_TYPE).setDictName(DictAttribute.TEMPLATE_RESPONSE_TYPE_IMAGE).setDictValue("2").setSort(1l).setDescription("图片消息").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.TEMPLATE_RESPONSE_TYPE).setDictName(DictAttribute.TEMPLATE_RESPONSE_TYPE_IMAGETEXT).setDictValue("3").setSort(2l).setDescription("图文消息").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.SUBSCRIBE_STATUS).setDictName(DictAttribute.SUBSCRIBE_STATUS_NO).setDictValue("0").setSort(0l).setDescription("关注状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.SUBSCRIBE_STATUS).setDictName(DictAttribute.SUBSCRIBE_STATUS_YES).setDictValue("1").setSort(1l).setDescription("关注状态").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECOMMEND_LEVEL).setDictName(DictAttribute.RECOMMEND_LEVEL1).setDictValue("1").setSort(1l).setDescription("会员代理等级").setCreateTime(DateTime.now()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECOMMEND_LEVEL).setDictName(DictAttribute.RECOMMEND_LEVEL2).setDictValue("2").setSort(2l).setDescription("会员代理等级").setCreateTime(DateTime.now()).save();
			
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_ACC_TYPE).setDictName(DictAttribute.ACCOUNT_ACC_TYPE_INSIDE_GAME).setDictValue("1").setSort(0l).setDescription("账户类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_ACC_TYPE).setDictName(DictAttribute.ACCOUNT_ACC_TYPE_INSIDE_SETT).setDictValue("2").setSort(0l).setDescription("账户类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_ACC_TYPE).setDictName(DictAttribute.ACCOUNT_ACC_TYPE_GAME).setDictValue("3").setSort(1l).setDescription("账户类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_ACC_TYPE).setDictName(DictAttribute.ACCOUNT_ACC_TYPE_SETT).setDictValue("4").setSort(2l).setDescription("账户类型").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_RECHARGE).setDictValue("1").setSort(0l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_AGENT).setDictValue("2").setSort(1l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_CONSUME).setDictValue("3").setSort(2l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND1).setDictValue("4").setSort(3l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND2).setDictValue("5").setSort(4l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_RECOMMEND3).setDictValue("6").setSort(5l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_WITHFRE).setDictValue("7").setSort(6l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_WITHUNFRE).setDictValue("8").setSort(7l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_MARKETING).setDictValue("9").setSort(8l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_CONVERT).setDictValue("10").setSort(9l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_ELE).setDictName(DictAttribute.ACCOUNT_FUNDS_ELE_REFUND).setDictValue("11").setSort(9l).setDescription("资金成分类型").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE).setDictName(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_ADD).setDictValue("1").setSort(0l).setDescription("资金进账类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE).setDictName(DictAttribute.ACCOUNT_FUNDS_TRANSTYPE_SUBTRACT).setDictValue("2").setSort(1l).setDescription("资金进账类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_ACC_UNIT_TYPE).setDictName(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_GAME).setDictValue("1").setSort(0l).setDescription("资金单位类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ACCOUNT_ACC_UNIT_TYPE).setDictName(DictAttribute.ACCOUNT_ACC_UNIT_TYPE_REWARD).setDictValue("2").setSort(1l).setDescription("资金单位类型").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_ORDER_STATUS).setDictName(DictAttribute.RECHARGE_ORDER_STATUS_COLSE).setDictValue("0").setSort(0l).setDescription("充值状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_ORDER_STATUS).setDictName(DictAttribute.RECHARGE_ORDER_STATUS_UNPAY).setDictValue("1").setSort(0l).setDescription("充值状态").setCreateTime(new java.util.Date()).save();
			//new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_ORDER_STATUS).setDictName(DictAttribute.RECHARGE_ORDER_STATUS_PAY).setDictValue("2").setSort(1l).setDescription("充值状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_ORDER_STATUS).setDictName(DictAttribute.RECHARGE_ORDER_STATUS_FINISH).setDictValue("3").setSort(2l).setDescription("充值状态").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_COMMISSION_STATUS).setDictName(DictAttribute.RECHARGE_COMMISSION_STATUS0).setDictValue("0").setSort(0l).setDescription("充值返佣状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_COMMISSION_STATUS).setDictName(DictAttribute.RECHARGE_COMMISSION_STATUS1).setDictValue("1").setSort(1l).setDescription("充值返佣状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_COMMISSION_STATUS).setDictName(DictAttribute.RECHARGE_COMMISSION_STATUS2).setDictValue("2").setSort(2l).setDescription("充值返佣状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_COMMISSION_STATUS).setDictName(DictAttribute.RECHARGE_COMMISSION_STATUS3).setDictValue("3").setSort(3l).setDescription("充值返佣状态").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_QRCODE_TYPE).setDictName(DictAttribute.RECHARGE_QRCODE_TYPE1).setDictValue("1").setSort(0l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_QRCODE_TYPE).setDictName(DictAttribute.RECHARGE_QRCODE_TYPE2).setDictValue("2").setSort(1l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_ACTION_TYPE).setDictName(DictAttribute.RECHARGE_ACTION_TYPE1).setDictValue("1").setSort(0l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECHARGE_ACTION_TYPE).setDictName(DictAttribute.RECHARGE_ACTION_TYPE2).setDictValue("2").setSort(1l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECOMMEND_ORDER_STATUS).setDictName(DictAttribute.RECOMMEND_ORDER_STATUS_COLSE).setDictValue("0").setSort(0l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECOMMEND_ORDER_STATUS).setDictName(DictAttribute.RECOMMEND_ORDER_STATUS_APPLY).setDictValue("1").setSort(1l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			//new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECOMMEND_ORDER_STATUS).setDictName(DictAttribute.RECOMMEND_ORDER_STATUS_HANDLE).setDictValue("2").setSort(2l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.RECOMMEND_ORDER_STATUS).setDictName(DictAttribute.RECOMMEND_ORDER_STATUS_FINISH).setDictValue("3").setSort(3l).setDescription("提现状态").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.EXTENSION_TYPE).setDictName(DictAttribute.EXTENSION_TYPE1).setDictValue("1").setSort(2l).setDescription("活动类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.EXTENSION_TYPE).setDictName(DictAttribute.EXTENSION_TYPE2).setDictValue("2").setSort(3l).setDescription("活动类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.EXTENSION_TYPE).setDictName(DictAttribute.EXTENSION_TYPE3).setDictValue("3").setSort(3l).setDescription("活动类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.EXTENSION_TYPE).setDictName(DictAttribute.EXTENSION_TYPE4).setDictValue("4").setSort(4l).setDescription("活动类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_ORDER_STATUS).setDictName(DictAttribute.GOODS_ORDER_STATUS1).setDictValue("1").setSort(0l).setDescription("发货状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_ORDER_STATUS).setDictName(DictAttribute.GOODS_ORDER_STATUS2).setDictValue("2").setSort(1l).setDescription("发货状态").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_ORDER_TYPE).setDictName(DictAttribute.GOODS_ORDER_TYPE1).setDictValue("1").setSort(0l).setDescription("订单状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_ORDER_TYPE).setDictName(DictAttribute.GOODS_ORDER_TYPE2).setDictValue("2").setSort(1l).setDescription("订单状态").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_JOY_TYPE).setDictName(DictAttribute.GOODS_JOY_TYPE1).setDictValue("1").setSort(0l).setDescription("商品特性").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_JOY_TYPE).setDictName(DictAttribute.GOODS_JOY_TYPE2).setDictValue("2").setSort(1l).setDescription("商品特性").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_RULE_LEVEL_TYPE).setDictName(DictAttribute.GOODS_RULE_LEVEL_TYPE1).setDictValue("1").setSort(0l).setDescription("关卡等级").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_RULE_LEVEL_TYPE).setDictName(DictAttribute.GOODS_RULE_LEVEL_TYPE2).setDictValue("2").setSort(1l).setDescription("关卡等级").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_RULE_LEVEL_TYPE).setDictName(DictAttribute.GOODS_RULE_LEVEL_TYPE3).setDictValue("3").setSort(2l).setDescription("关卡等级").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_RULE_DIFF_TYPE).setDictName(DictAttribute.GOODS_RULE_DIFF_TYPE1).setDictValue("0.3").setSort(0l).setDescription("难度类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_RULE_DIFF_TYPE).setDictName(DictAttribute.GOODS_RULE_DIFF_TYPE2).setDictValue("0.5").setSort(1l).setDescription("难度类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_RULE_DIFF_TYPE).setDictName(DictAttribute.GOODS_RULE_DIFF_TYPE3).setDictValue("0.8").setSort(2l).setDescription("难度类型").setCreateTime(new java.util.Date()).save();
			
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_GAME_STATUS).setDictName(DictAttribute.GOODS_GAME_STATUS1).setDictValue("1").setSort(0l).setDescription("游戏状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_GAME_STATUS).setDictName(DictAttribute.GOODS_GAME_STATUS2).setDictValue("2").setSort(1l).setDescription("游戏状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_GAME_STATUS).setDictName(DictAttribute.GOODS_GAME_STATUS3).setDictValue("3").setSort(2l).setDescription("游戏状态").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.GOODS_GAME_STATUS).setDictName(DictAttribute.GOODS_GAME_STATUS4).setDictValue("4").setSort(3l).setDescription("游戏状态").setCreateTime(new java.util.Date()).save();

			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ADV_BANNER_TYPE).setDictName(DictAttribute.ADV_BANNER_TYPE1).setDictValue("1").setSort(0l).setDescription("广告类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ADV_BANNER_TYPE).setDictName(DictAttribute.ADV_BANNER_TYPE2).setDictValue("2").setSort(1l).setDescription("广告类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ADV_BANNER_TYPE).setDictName(DictAttribute.ADV_BANNER_TYPE3).setDictValue("3").setSort(2l).setDescription("广告类型").setCreateTime(new java.util.Date()).save();
			new SysDictionary().setId(IdUtil.simpleUUID()).setDictCode(DictAttribute.ADV_BANNER_TYPE).setDictName(DictAttribute.ADV_BANNER_TYPE4).setDictValue("4").setSort(3l).setDescription("广告类型").setCreateTime(new java.util.Date()).save();

		}
	}
	
	/**
	 * 初始化用户
	 */
	public void sysUserData(){
		if(SysUser.dao.findCountByModel(new SysUser())  == 0){
			//添加超级管理员
			SysUser sysUser = new SysUser();
			sysUser.setId(IdUtil.simpleUUID()).setNickName("超级管理员").setUserName("admin");
			sysUser.setSalt(RandomUtil.randomString(8)).setPassword("123456");
			sysUser.setPassword(SecureUtil.hmacMd5(sysUser.getSalt()).digestHex(sysUser.getPassword()));
			sysUser.setSex(1L).setPhone("12345678900").setEmail("admin@admin.org").setEmailVerified(1);
			sysUser.setStatus(1L).setCreateTime(new java.util.Date()).setUpdateTime(new java.util.Date()).save();
		}
	}
	
	/**
	 * 初始化系统账户
	 */
	public void accountData(){
		if(Account.dao.findCountByModel(new Account())  == 0){
			//添加内部账户
			Account account1 = new Account();
			account1.setId(JoyIdUtil.simpleUUID());
			account1.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
			account1.setTotalAmt(0d);
			account1.setFrzAmt(0d);
			account1.setAvbAmt(0d);
			account1.setAccType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_TYPE_INSIDE_GAME, DictAttribute.ACCOUNT_ACC_TYPE, "1")));
			account1.setStatus(1L);
			account1.setVersion(1L);
			account1.setCreateTime(new java.util.Date());
			account1.setUpdateTime(new java.util.Date());
			
			//添加内部账户
			Account account2 = new Account();
			account2.setId(JoyIdUtil.simpleUUID());
			account2.setWechatMemberId(CommonAttribute.INSIDE_WECHAT_MEMBER_ID);
			account2.setTotalAmt(0d);
			account2.setFrzAmt(0d);
			account2.setAvbAmt(0d);
			account2.setAccType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.ACCOUNT_ACC_TYPE_INSIDE_SETT, DictAttribute.ACCOUNT_ACC_TYPE, "1")));
			account2.setStatus(1L);
			account2.setVersion(1L);
			account2.setCreateTime(new java.util.Date());
			account2.setUpdateTime(new java.util.Date());
			
			account1.save();
			account2.save();
		}
	}
	
	/**
	 * 初始化config数据
	 */
	public void sysConfigData(){
		if(SysConfig.dao.findCountByModel(new SysConfig())  == 0){
			//添加腾讯oss配置
			/**
			 * 成都（西南）	ap-chengdu	<BucketName-APPID>.cos.ap-chengdu.myqcloud.com
			 * 所属地域=成都(西南)(ap-chengdu)
			 * 存储桶=yayap-1251064230
			 * https://github.com/chocotan/osm
			new SysConfig().setId(IdUtil.simpleUUID()).setName(Enums.SysConfigType.SYS_CONFIG_TENCENT_ENDPOINT.name()).setValue("ap-chengdu").setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			new SysConfig().setId(IdUtil.simpleUUID()).setName(Enums.SysConfigType.SYS_CONFIG_TENCENT_SECRETID.name()).setValue("AKIDaPyQjdtidIJ8vPoRuEbyoYODAvsZnpdX").setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			new SysConfig().setId(IdUtil.simpleUUID()).setName(Enums.SysConfigType.SYS_CONFIG_TENCENT_SECRETKEY.name()).setValue("qNxmMguoSpsgYPd9E3b0fBQF15tVA2dz").setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			new SysConfig().setId(IdUtil.simpleUUID()).setName(Enums.SysConfigType.SYS_CONFIG_TENCENT_BUCKETNAME.name()).setValue("yayap-1251064230").setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			 */
			
			new SysConfig().setId(IdUtil.simpleUUID()).setName(Enums.SysConfigType.SYS_ALLOW_CASH.name()).setValue("5").setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			new SysConfig().setId(IdUtil.simpleUUID()).setName(Enums.SysConfigType.SYS_ALLOW_TRANSFER.name()).setValue("5").setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
		}
	}
	
	/**
	 * 初始化默认会员分成数据
	 */
	public void productDefaultRecommendRule() {
		if(RecommendRule.dao.findCountByModel(new RecommendRule())  == 0) {
			new RecommendRule().setId(JoyIdUtil.simpleUUID()).setRecType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_LEVEL1, DictAttribute.RECOMMEND_LEVEL, "1"))).setRecVal1(0.12).setRecVal2(0.15).setRecVal3(0.0).setRecAmount(66.0).setCreateTime(DateTime.now()).setUpdateTime(DateTime.now()).save();
			new RecommendRule().setId(JoyIdUtil.simpleUUID()).setRecType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_LEVEL2, DictAttribute.RECOMMEND_LEVEL, "2"))).setRecVal1(0.15).setRecVal2(0.18).setRecVal3(0.20).setRecAmount(88.0).setCreateTime(DateTime.now()).setUpdateTime(DateTime.now()).save();
		}
	}
	
	/**
	 * 初始化开发用的业务数据
	 */
	public void bussinseDebugData() {
		/**
		 * 初始化测试公众号
		 */
		if(WechatSubscribe.dao.findCountByModel(new WechatSubscribe()) == 0){
			WechatSubscribe tmpWechatSubscribe = new WechatSubscribe();
			tmpWechatSubscribe.setId(IdUtil.simpleUUID());
			tmpWechatSubscribe.setAppType(1L);
			tmpWechatSubscribe.setAppName("微信公众平台测试号");
			tmpWechatSubscribe.setAppCode("gh_3bfc52c4b890");
			tmpWechatSubscribe.setAppId("wxf492cc0bf7d1f5e3");
			tmpWechatSubscribe.setAppSecret("de59416f07088970e8c3dd7ff571c089");
			tmpWechatSubscribe.setToken("hlevel");
			tmpWechatSubscribe.setStatus(1L);
			tmpWechatSubscribe.setUpdateTime(new java.util.Date());
			tmpWechatSubscribe.setCreateTime(new java.util.Date());
			tmpWechatSubscribe.save();
			
			String subscribeId = tmpWechatSubscribe.getId();
			//查询当前对象是否已经存在
			int count = WechatTemplate.dao.findCountByModel(new WechatTemplate().setSubscribeId(subscribeId));
			if(count == 0){
				Duang.duang(WechatTemplateService.class).initWechatTemplate(subscribeId);
			}
		}
		
		/**
		 * 闯关规则
		 */
		GoodsRule goodsRule = null;
		if(GoodsRule.dao.findCountByModel(new GoodsRule())  == 0){
			goodsRule = new GoodsRule().setId(IdUtil.simpleUUID()).setRuleName("普通规则").setDiffValue("{\"1\":{\"diff\":\"0.02\",\"quant\":\"3\",\"second\":\"60\"},\"2\":{\"diff\":\"0.01\",\"quant\":\"4\",\"second\":\"30\"},\"3\":{\"diff\":\"0.9\",\"quant\":\"5\",\"second\":\"15\"}}").setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
			goodsRule.save();
		}
		
		/**
		 * 商品规则
		 */
		GoodsCategory goodsCategory1 = null;
		GoodsCategory goodsCategory2 = null;
		if(GoodsCategory.dao.findCountByModel(new GoodsCategory())  == 0){
			goodsCategory1 = new GoodsCategory().setId(IdUtil.simpleUUID()).setCategoryName("口红").setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
			goodsCategory1.save();
			
			goodsCategory2 = new GoodsCategory().setId(IdUtil.simpleUUID()).setCategoryName("补水").setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date());
			goodsCategory2.save();
		}
		
		/**
		 * 商品
		 */
		if(Goods.dao.findCountByModel(new Goods()) == 0) {
			new Goods().setId(IdUtil.simpleUUID()).setGoodsName("中棠口红").setCategoryId(goodsCategory1.getId()).setCostPrice(12d).setMktPrice(20d)
			.setScorePrice(6l).setGoodsImage("20190202183309095.jpg").setGoodsRuleId(goodsRule.getId()).setRecommend("Axsure 系列").setMonthSales(0L)
			.setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			
			new Goods().setId(IdUtil.simpleUUID()).setGoodsName("海棠面膜").setCategoryId(goodsCategory2.getId()).setCostPrice(10d).setMktPrice(15d)
			.setScorePrice(8l).setGoodsImage("20190217213951834.jpg").setGoodsRuleId(goodsRule.getId()).setRecommend("养肤 系列").setMonthSales(0L)
			.setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
		}
		
		/**
		 * 充值金额
		 */
		if(RechargeRule.dao.findCountByModel(new RechargeRule()) == 0) {
			new RechargeRule().setId(IdUtil.simpleUUID()).setTransAmt(5d).setTransAfterAmt(5d).setTransDayLimit(5l).setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			new RechargeRule().setId(IdUtil.simpleUUID()).setTransAmt(10d).setTransAfterAmt(11d).setTransDayLimit(4l).setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
			new RechargeRule().setId(IdUtil.simpleUUID()).setTransAmt(20d).setTransAfterAmt(23d).setTransDayLimit(3l).setStatus(1L).setUpdateTime(new java.util.Date()).setCreateTime(new java.util.Date()).save();
		}
		
		/**
		 * 用户
		 */
		if(WechatMember.dao.findCountByModel(new WechatMember()) == 0) {
			//创建保存用户
			WechatMember tmpWechatMember = new WechatMember();
			tmpWechatMember.setOpenid("ole4lt37B4UGR0cfHTVebiPnXbmI");
			tmpWechatMember.setSubscribe(1L);
			tmpWechatMember.setSubscribeId(WechatSubscribe.dao.findDefault().getId());
			tmpWechatMember.setNickName("舟亢");
			tmpWechatMember.setSex(1L);
			tmpWechatMember.setLanguage("zh_CN");
			tmpWechatMember.setCountry("中国");
			tmpWechatMember.setProvince("湖北");
			tmpWechatMember.setCity("武汉");
			tmpWechatMember.setHeadimgUrl("http://thirdwx.qlogo.cn/mmopen/du913FMg7ZOQahBe4FNbFicibGyoqSnfiat7Yn4h98hpWic0GKicNUvH0NwUn34nLTMETWEhibhRcxovcrOhOAyM9focM5SDEChJLB/132");
			
			Duang.duang(WechatMemberService.class).saveWechatMemberProfit(tmpWechatMember, null);
			
		}
	}
}
