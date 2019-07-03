#namespace("goods")
	#sql("findListPage")
		SELECT
			g.*,
			gc.category_name AS categoryName,
			gr.rule_name AS ruleName 
		FROM
			joy_goods g
			LEFT JOIN joy_goods_category gc ON g.category_id = gc.id
			LEFT JOIN joy_goods_rule gr ON g.goods_rule_id = gr.id
		WHERE
			1=1
			#if(query.categoryId)
				AND g.category_id=#para(query.categoryId)
			#end
			#if(query.goodsRuleId)
				AND g.goods_rule_id=#para(query.goodsRuleId)
			#end
			#if(query.goodsName)
				AND g.goods_name like concat('%', #para(query.goodsName), '%')
			#end
			#if(query.status)
				AND g.`status`=#para(query.status)
			#end
		ORDER BY g.create_time desc
	#end
	#sql("findGiftGoodsList")
		SELECT g.id,g.goods_name,g.goods_image,g.mkt_price,g.recommend FROM joy_goods g WHERE `status`=1 AND g.mkt_price <= (
		(SELECT SUM(gg.exp_amt) FROM joy_goods_game gg WHERE gg.status<>4 AND gg.wechat_member_id=#para(wechatMemberId)) 
		-
		(SELECT ISNULL(SUM(jg.mkt_price),0) FROM joy_goods jg INNER JOIN joy_goods_order go ON jg.id=go.goods_id where go.wechat_member_id=#para(wechatMemberId))
		)
	#end
#end