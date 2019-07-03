#namespace("sysGfw")
	#sql("findSelfAppCode")
		SELECT app_code FROM joy_sys_gfw WHERE status=1 AND app_type=1
	#end
	#sql("findSelfMasterUrl")
		SELECT master_url FROM joy_sys_gfw WHERE status=1 AND app_type=1
	#end
	#sql("findSelfSlaveUrl")
		SELECT slave_url FROM joy_sys_gfw WHERE status=1 AND app_type=1
	#end
	#sql("findOtherMasterUrl")
		SELECT master_url FROM joy_sys_gfw WHERE status=1 AND app_type=2 AND app_code=?
	#end
	#sql("findOtherSlaveUrl")
		SELECT slave_url FROM joy_sys_gfw WHERE status=1 AND app_type=2 AND app_code=?
	#end
#end