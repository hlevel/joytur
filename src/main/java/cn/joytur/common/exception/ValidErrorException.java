package cn.joytur.common.exception;

import cn.joytur.common.mvc.constant.RenderResultCode;

/**
 * 验证异常
 * @author xuhang
 */
@SuppressWarnings("serial")
public class ValidErrorException extends RuntimeException{

	/**
     * 异常code {@link RenderResultCode}
     */
    private int code;

    public ValidErrorException() {
        super();
    }

    public ValidErrorException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ValidErrorException(RenderResultCode error) {
        super(error.getMessage());
        this.code = error.getCode();
    }
    
    public ValidErrorException(RenderResultCode error, Throwable cause) {
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
