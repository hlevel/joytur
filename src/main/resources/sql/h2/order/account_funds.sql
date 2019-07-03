#namespace("accountFunds")
	#sql("findListPage")
		SELECT
			wm.openid,
			wm.nick_name AS nickName,
			wm.headimg_url AS headimgUrl,
			a.acc_type AS accType,
			af.* 
		FROM
			joy_wechat_member wm
			INNER JOIN joy_account_funds af ON af.wechat_member_id = wm.id
			INNER JOIN joy_account a ON a.wechat_member_id=wm.id AND a.acc_type=af.acc_type
		WHERE
			1=1
			#if(query.openid)
				AND wm.openid=#para(query.openid)
			#end
			#if(query.nickName)
				AND wm.nick_name like concat('%', #para(query.nickName), '%')
			#end
			#if(query.accType)
				AND a.acc_type=#para(query.accType)
			#end
			#if(query.eleType)
				AND af.ele_type=#para(query.eleType)
			#end
		ORDER BY af.create_time desc
	#end
#end