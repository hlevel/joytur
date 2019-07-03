#namespace("account")
	#sql("findListPage")
		SELECT
			wm.openid,
			wm.nick_name AS nickName,
			a.*
		FROM
			joy_account a
			INNER JOIN joy_wechat_member wm ON a.wechat_member_id = wm.id
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
		ORDER BY a.create_time desc
	#end
#end