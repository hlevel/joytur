#namespace("goodsOrder")
	#sql("findListPage")
		SELECT
			go.id,
			wm.openid,
			wm.nick_name AS nickName,
			g.goods_image AS goodsImage,
			g.goods_name AS goodsName,
			g.recommend,
			go.order_no,
			go.addr_mirror,
			go.`status`,
			wma.real_name AS realName,
			wma.mobile AS mobile,
			wma.addr_area AS addArea,
			wma.addr_detail AS addrDetail,
			go.create_time
		FROM
			joy_wechat_member wm
			INNER JOIN joy_goods_order go ON wm.id = go.wechat_member_id
			INNER JOIN joy_goods g ON go.goods_id = g.id
			LEFT JOIN joy_wechat_member_addr wma ON wma.wechat_member_id=wm.id AND wma.status=2 AND wma.deleted=1
		WHERE
			1=1
			#if(query.wechatMemberId)
				AND go.wechat_member_id=#para(query.wechatMemberId)
			#end
			#if(query.openid)
				AND wm.openid=#para(query.openid)
			#end
			#if(query.nickName)
				AND wm.nick_name like concat('%', #para(query.nickName), '%')
			#end
			#if(query.status)
				AND go.status=#para(query.status)
			#end
		ORDER BY go.create_time desc
	#end
	
	#sql("findWapListPage")
		SELECT
			g.goods_image,
			g.goods_name,
			g.recommend,
			go.order_no,
			go.`status`
		FROM
			joy_wechat_member wm
			INNER JOIN joy_goods_order go ON wm.id = go.wechat_member_id
			INNER JOIN joy_goods g ON go.goods_id = g.id
		WHERE
			1=1
			#if(query.wechatMemberId)
				AND go.wechat_member_id=#para(query.wechatMemberId)
			#end
			#if(query.status)
				AND go.status=#para(query.status)
			#end
		ORDER BY go.create_time desc
	#end
	
	#sql("findNoticeList")
		SELECT
			go.id,
			wm.nick_name,
			g.goods_name,
			g.recommend,
			go.order_no,
			go.order_type,
			go.`status` 
		FROM
			joy_wechat_member wm
			INNER JOIN joy_goods_order go ON wm.id = go.wechat_member_id
			INNER JOIN joy_goods g ON go.goods_id = g.id 
		ORDER BY
			go.create_time DESC 
			LIMIT #para(0)
	#end
	
	#sql("findCountByWait")
		SELECT count(*) FROM joy_goods_order go WHERE go.status=?
	#end
#end