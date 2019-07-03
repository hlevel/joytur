#namespace("loginLog")
	#sql("findCountByStatusAndUserIdAndMinute")
		select COUNT(1) from joy_sys_login_log sll where sll.status=? AND sll.user_id=? AND sll.create_time>=?
	#end
	#sql("deleteAll")
		DELETE FROM joy_sys_login_log
	#end
#end