package cn.joytur.common.mvc.constant;

/**
 * @author xuhang
 */
public class Enums {

	/**
	 * 权限类型 
	 */
	public enum AuthCode {
		REQ_ROLES, REQ_PERMS, REQ_LOGIN, REQ_GUEST
	}
	
	/**
	 * 系统参数http://www.ccvnn.com/forum-2-1.html
	 */
	public enum SysConfigType {
		WAP_DOMIAN_NAME, WAP_HOME_PHRASE, WAP_HOME_DESCRIPTION, WAP_SHARE_X, WAP_SHARE_Y, WAP_WXPAY_QRCODE,
		SYS_DOMAIN_NAME, SYS_CONFIG_LOGO, SYS_CONFIG_KEYWORD, SYS_CONFIG_DESCRIPTION,SYS_CONFIG_COPYRIGHT, SYS_CONFIG_ICP, 
		SYS_CONFIG_LOGINERR, SYS_CONFIG_ALLOWIP, SYS_ALLOW_CASH, SYS_ALLOW_TRANSFER, SYS_CLOSING_SWITCH, SYS_CLOSING_TIME, SYS_GIFT_PACKED,
		SYS_CONFIG_TENCENT_ENDPOINT, SYS_CONFIG_TENCENT_SECRETID, SYS_CONFIG_TENCENT_SECRETKEY, SYS_CONFIG_TENCENT_BUCKETNAME,
		MONITOR_APP_HEARTBEAT, MONITOR_APP_SIGN, MONITOR_APP_RECEIVABLES, MONITOR_WEB_RECEIVABLES, MONITOR_MAIL_RECEIVABLES, MONITOR_SEND_MAIL, MONITOR_SEND_MAIL_PWD, MONITOR_RECEIPT_MAIL
		
	}
	
	/**
	 * 排序
	 * @author xuhang
	 * @time 2019年1月7日 下午4:24:12
	 */
	public enum SortType{
		ASC, DESC
	}
	
	/**
	 * 事件名称
	 */
	public enum ActionType {
		/**
		 * 首次关注
		 */
		FIRST_FOLLOW("FIRST_FOLLOW", "首次关注"),
		/**
		 * 再次关注
		 */
		ONCE_FOLLW("ONCE_FOLLW", "再次关注"),
		/**
		 * 系统异常提示
		 */
		SYSTEM_EXCEPTION_TIP("SYSTEM_EXCEPTION_TIP", "系统异常提示"),
		
		/**
		 * 无法找到回复提示
		 */
		SYSTEM_NOTFOUNT_TIP("SYSTEM_NOTFOUNT_TIP", "无法找到回复提示")
		;

		private String code;
		private String text;

		ActionType(String code, String text) {
			this.code = code;
			this.text = text;
		}

		public String getCode() {
			return code;
		}

		public static String getText(String code) {
			for (ActionType status : ActionType.values()) {
				if (status.getCode().equals(code))
					return status.text;
			}
			return "";
		}

		// 校验CODE
		public static String valiCode(String code) {
			for (ActionType type : ActionType.values()) {
				if (type.getCode().equals(code))
					return type.getCode();
			}
			return "";
		}
		
	}

	public enum OrderNoType {
		RECHARGE_ORDER(1),
		RECOMMEND_ORDER(2),
		GOODS_ORDER(3);

		OrderNoType(int code) {
			this.code = code;
		}

		private int code;

		public int getCode() {
			return code;
		}
	}
	
	
	public enum WebWechatStatus {
		WAIT(70, "正在获取登录二维码"),
		LOADING_QRCODE(71, "正在获取登录二维码"),
		RECEIVED_QRCODE(72, "获取成功,请用手机微信扫码"),
		SCANNED_QRCODE(73, "扫码成功,请在手机微信中点击登录"),
		LOGIN_SUCCES(74, "登录成功"),
		LOGIN_FAIL(75, "登录失败"),
		LOGIN_DROPPED(76, "微信已离线,请重新登录"),
		;

		private Integer code;
		private String text;

		WebWechatStatus(Integer code, String text) {
			this.code = code;
			this.text = text;
		}

		public Integer getCode() {
			return code;
		}
		
		public String getText() {
			return text;
		}

		public static String getText(String code) {
			for (WebWechatStatus status : WebWechatStatus.values()) {
				if (status.getCode().equals(code))
					return status.text;
			}
			return "";
		}

		// 校验CODE
		public static Integer valiCode(String code) {
			for (WebWechatStatus type : WebWechatStatus.values()) {
				if (type.getCode().equals(code))
					return type.getCode();
			}
			return null;
		}
		
	}
	
}
