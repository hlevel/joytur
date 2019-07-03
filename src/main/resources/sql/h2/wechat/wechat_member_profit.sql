#namespace("wechatMemberProfit")
	#sql("findReceiveWechatMemberProfit")
		SELECT
			wmp.* 
		FROM
			joy_wechat_member_profit wmp INNER JOIN joy_wechat_member_recommend wmr ON wmp.wechat_member_id = wmr.receive_wechat_member_id
		WHERE wmr.wechat_member_id=#para(0)
	#end
	
#end