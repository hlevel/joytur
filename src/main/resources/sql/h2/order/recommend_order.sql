#namespace("recommendOrder")
	#sql("findListPage")
		SELECT
			wm.openid,
			wm.nick_name AS nickName,
			wm.headimg_url AS headimgUrl,
			ro.* 
		FROM
			joy_recommend_order ro
			LEFT JOIN joy_wechat_member wm ON ro.wechat_member_id = wm.id
		WHERE
			1=1
			#if(query.openid)
				AND wm.openid=#para(query.openid)
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
		ORDER BY ro.create_time desc
	#end
	#sql("findCountByWait")
		SELECT count(*) FROM joy_recommend_order ro WHERE ro.status=?
	#end
#end