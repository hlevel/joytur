#namespace("menu")
	#sql("findByPidAndIdNot")
		select * from joy_sys_menu where pid=#para(0) and id<>#para(1)
	#end
	#sql("findMaxSort")
		select max(sa.sort) from joy_sys_menu sa where sa.pid = ?
	#end
	#sql("updateSort")
		update joy_sys_menu set sort=#para(0) where id=#para(1)
	#end
#end