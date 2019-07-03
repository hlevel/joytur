#namespace("rechargeRule")
	#sql("findByModel")
		SELECT
			rr.* 
		FROM
			joy_recharge_rule rr
		WHERE
			1=1
			#if(query.transAmt)
				AND rr.trans_amt>=#para(query.transAmt)
			#end
			#if(query.status)
				AND rr.status=#para(query.status)
			#end
			#if(minAmt)
				AND rr.trans_amt=(select min(crr.trans_amt) from joy_recharge_rule crr)
			#end
		ORDER BY rr.trans_amt asc
	#end
#end