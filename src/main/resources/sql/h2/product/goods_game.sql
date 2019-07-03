#namespace("goodsGame")
	#sql("findListPage")
		SELECT 
			gg.id,m.openid,m.nick_name,g.goods_name,g.goods_image,gg.exp_amt,gg.screen,gg.status,gg.game_result,gg.update_time,gg.create_time
		FROM 
			joy_goods g inner join joy_goods_game gg on gg.goods_id=g.id inner join joy_wechat_member m on m.id=gg.wechat_member_id
		WHERE
			1=1
			#if(query.gameParams)
				AND gg.game_params like concat('%', #para(query.gameParams), '%')
			#end
			#if(query.status)
				AND gg.status=#para(query.status)
			#end
			#if(query.nickName)
				AND wm.nick_name like concat('%', #para(query.nickName), '%')
			#end
			#if(query.wechatMemberId)
				AND gg.wechat_member_id=#para(query.wechatMemberId)
			#end
		ORDER BY gg.create_time desc
	#end
	#sql("findListWapPage")
		SELECT 
			g.goods_image,g.goods_name,g.recommend,gg.* 
		FROM 
			joy_goods_game gg INNER JOIN joy_goods g on gg.goods_id=g.id
		WHERE
			1=1
			#if(query.gameParams)
				AND gg.game_params like concat('%', #para(query.gameParams), '%')
			#end
			#if(query.wechatMemberId)
				AND gg.wechat_member_id=#para(query.wechatMemberId)
			#end
		ORDER BY gg.create_time desc
	#end
#end