#namespace("rechargeOrder")
	#sql("findListPage")
		SELECT
			wm.openid,
			wm.nick_name AS nickName,
			ro.* 
		FROM
			joy_recharge_order ro
			LEFT JOIN joy_wechat_member wm ON ro.wechat_member_id = wm.id
		WHERE
			1=1
			#if(query.openid)
				AND wm.openid=#para(query.openid)
			#end
			#if(query.wechatMemberId)
				AND ro.wechat_member_id=#para(query.wechatMemberId)
			#end
			#if(query.nickName)
				AND wm.nick_name like concat('%', #para(query.nickName), '%')
			#end
			#if(query.orderNo)
				AND ro.order_no=#para(query.orderNo)
			#end
			#if(query.status)
				AND ro.status=#para(query.status)
			#end
			#if(query.transType)
				AND ro.trans_type=#para(query.transType)
			#end
		ORDER BY ro.create_time desc
	#end
	#sql("updateAllExpireOrder")
		UPDATE joy_recharge_order set `status`=#para(0) WHERE `status`=#para(1) AND expire_time>NOW()
	#end
	#sql("updateMemberAllExpireOrder")
		UPDATE joy_recharge_order set `status`=#para(0) WHERE `status`=#para(1) AND wechat_member_id=#para(2)
	#end
	#sql("findCountByToDay")
		SELECT sum(ro.real_trans_amt) FROM joy_recharge_order ro WHERE ro.status=? and ro.create_time>?
	#end
	#sql("findWaitHandleRechargeOrderList")
		SELECT ro.id,ro.real_trans_amt,wm.headimg_url AS headimgUrl,wm.nick_name AS nickName
		FROM 
			joy_recharge_order ro  INNER JOIN joy_wechat_member wm ON ro.wechat_member_id=wm.id
			INNER JOIN (
				SELECT ro.wechat_member_id,ro.real_trans_amt,max(ro.create_time) as last_time FROM joy_recharge_order ro 
				WHERE ro.`status` IN (#para(0),#para(1)) AND ro.create_time>#para(2) GROUP BY ro.wechat_member_id,ro.real_trans_amt
			) tab ON ro.create_time=tab.last_time
	#end
#end