package cn.joytur.common.exception;

import cn.joytur.common.mvc.constant.RenderResultCode;

/**
 * 业务异常订单 
 * @author xuhang
 */
@SuppressWarnings("serial")
public class BusinessException extends RuntimeException{

	/**
     * 异常code {@link RenderResultCode}
     */
    private int code;

    public BusinessException() {
        super();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(RenderResultCode error) {
        super(error.getMessage());
        this.code = error.getCode();
    }
    
    public BusinessException(RenderResultCode error, Throwable cause) {
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
