package cn.joytur.common.exception;

import cn.joytur.common.mvc.constant.RenderResultCode;

/**
 * 权限异常
 * @author xuhang
 */
@SuppressWarnings("serial")
public class NoAuthorizationException extends RuntimeException{

	/**
     * 异常code {@link RenderResultCode}
     */
    private int code;

    public NoAuthorizationException() {
        super();
    }

    public NoAuthorizationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public NoAuthorizationException(RenderResultCode error) {
        super(error.getMessage());
        this.code = error.getCode();
    }
    
    public NoAuthorizationException(RenderResultCode error, Throwable cause) {
        super(error.getMessage(), cause);
        this.code = error.getCode();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
