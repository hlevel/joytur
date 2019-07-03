#namespace("rechargeRuleQrCode")
	#sql("findListPage")
		SELECT
			rr.trans_amt AS ruleTransAmt,
			rrq.* 
		FROM
			joy_recharge_rule_qrcode rrq INNER JOIN joy_recharge_rule rr ON rr.id = rrq.recharge_rule_id
		WHERE
			1=1
			#if(query.transAmt)
				AND rrq.trans_amt>#para(query.transAmt)
			#end
			#if(query.status)
				AND rrq.status=#para(query.status)
			#end
		ORDER BY rrq.create_time desc
	#end
	#sql("findUnlockQrCodeListByRechargeRuleId")
		SELECT
			rrq.* 
		FROM
			joy_recharge_rule_qrcode rrq 
		WHERE
			rrq.trans_amt BETWEEN #para(minAmt) AND #para(maxAmt) 
			AND rrq.trans_amt NOT IN ( SELECT ro.real_trans_amt FROM joy_recharge_order ro WHERE ro.status = #para(status) AND ro.real_trans_amt BETWEEN #para(minAmt) AND #para(maxAmt));	
	#end
#end