#namespace("user")
	#sql("findByRoleId")
		select su.* from joy_sys_user su inner join joy_sys_user_role sur on su.id=sur.user_id where sur.role_id=#para(0)
	#end
	#sql("findPermissionByUserId")
		SELECT DISTINCT sm.permission  
		FROM joy_sys_user su INNER JOIN joy_sys_user_role sur ON su.id = sur.user_id INNER JOIN joy_sys_role_menu srm ON srm.role_id = sur.role_id INNER JOIN joy_sys_menu sm ON sm.id=srm.menu_id AND sm.type=2
		WHERE su.id=#para(0)
	#end
#end