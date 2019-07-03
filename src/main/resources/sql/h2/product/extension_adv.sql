#namespace("extensionAdv")
	#sql("findExtensionAdvImage")
		SELECT adv_image FROM joy_extension_adv WHERE status=1 AND adv_type=?
	#end
#end