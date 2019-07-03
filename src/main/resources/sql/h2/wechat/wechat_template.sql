#namespace("wechatTemplate")
	#sql("findByKeywords")
		select wt.* from joy_wechat_template wt where wt.event_keywords like CONCAT('%,', #para(0), ',%')
	#end
#end