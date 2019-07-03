package cn.joytur.common.exception;

import cn.joytur.common.mvc.constant.RenderResultCode;

/**
 * 业务接口异常订单 
 * @author xuhang
 */
@SuppressWarnings("serial")
public class ApiBusinessException extends RuntimeException{

	/**
     * 异常code {@link RenderResultCode}
     */
    private int code;

    public ApiBusinessException() {
        super();
    }

    public ApiBusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ApiBusinessException(RenderResultCode error) {
        super(error.getMessage());
        this.code = error.getCode();
    }
    
    public ApiBusinessException(RenderResultCode error, Throwable cause) {
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
