package cn.joytur.common.exception;

/**
 * 未登录
 * @author xuhang
 * @time 2018年10月16日 上午10:18:44
 */
@SuppressWarnings("serial")
public class NotLoggedInException extends RuntimeException{

    private String redirectUrl;	//重定向url

    public NotLoggedInException() {
        super();
    }

    public NotLoggedInException(String redirectUrl) {
    	this.redirectUrl = redirectUrl;
    }

	public String getRedirectUrl() {
		return redirectUrl;
	}
}
