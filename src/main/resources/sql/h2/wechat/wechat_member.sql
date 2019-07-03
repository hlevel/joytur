#namespace("wechatMember")
	#sql("findListPage")
		SELECT
			wm.id,wm.nick_name,wm.sex,wm.headimg_url,wm.subscribe,wm.city,wm.status,wm.create_time,a.avb_amt,a.frz_amt,rr.rec_type
		FROM
			joy_wechat_member wm
			LEFT JOIN joy_account a ON wm.id = a.wechat_member_id AND a.acc_type=3
			LEFT JOIN joy_wechat_member_profit wmp ON wm.id = wmp.wechat_member_id
			LEFT JOIN joy_recommend_rule rr ON rr.id = wmp.recommend_rule_id
		WHERE
			1=1
			#if(query.nickName)
				AND wm.nick_name like concat('%', #para(query.nickName), '%')
			#end
		ORDER BY wm.create_time desc
	#end
	
	#sql("findWapGroupListPage")
		SELECT 
			wm.id,wm.nick_name,wm.sex,wm.headimg_url,wm.create_time
		FROM 
			joy_wechat_member_recommend r0 INNER JOIN joy_wechat_member wm ON r0.wechat_member_id=wm.id
		WHERE
			1=1
			#if(level==1)
				AND wm.id in (SELECT wechat_member_id from joy_wechat_member_recommend r1 where r1.receive_wechat_member_id=#para(wechatMemberId))
			#end
			#if(level==2)
				AND wm.id in (SELECT r1.wechat_member_id from joy_wechat_member_recommend r1 where r1.receive_wechat_member_id in (SELECT wechat_member_id from joy_wechat_member_recommend r2 where r2.receive_wechat_member_id=#para(wechatMemberId)))
			#end
			#if(level==3)
				AND wm.id in (SELECT r1.wechat_member_id from joy_wechat_member_recommend r1 where r1.receive_wechat_member_id in (SELECT wechat_member_id from joy_wechat_member_recommend r2 where r2.receive_wechat_member_id in  (SELECT r3.wechat_member_id FROM joy_wechat_member_recommend r3 WHERE r3.receive_wechat_member_id=#para(wechatMemberId))))
			#end
		ORDER BY wm.create_time desc
	#end
	
	#sql("findCountByToDay")
		SELECT count(*) FROM joy_wechat_member wm WHERE wm.create_time>?
	#end
	
	#sql("findByRechargeOrderNo")
		SELECT * FROM joy_wechat_member wm INNER JOIN joy_recharge_order ro ON wm.id=ro.wechat_member_id WHERE ro.order_no=#para(0)
	#end
#end