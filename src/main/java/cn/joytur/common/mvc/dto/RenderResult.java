package cn.joytur.common.mvc.dto;

import cn.joytur.common.mvc.constant.RenderResultCode;

/**
 * 操作消息提醒
 * 
 * @author xuhang
 */
public class RenderResult<T> {

	private int code; // 响应请求的编码
	private String msg; // 响应请求的消息
	private boolean success;	//是否成功
	private T data; // 响应请求的对应

	public RenderResult() {
	}

	public RenderResult(RenderResultCode renderResultCode, boolean success) {
		this.code = renderResultCode.getCode();
		this.msg = renderResultCode.getMessage();
		this.success = success;
	}

	public RenderResult(RenderResultCode renderResultCode, boolean success, T data) {
		this.code = renderResultCode.getCode();
		this.msg = renderResultCode.getMessage();
		this.success = success;
		this.data = data;
	}

	public RenderResult(int code, String msg, boolean success, T data) {
		this.code = code;
		this.msg = msg;
		this.success = success;
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public T getData() {
		return data;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	/**
	 * 返回成功
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> success(){
		return newRenderResult(RenderResultCode.SUCCESS.getCode(), RenderResultCode.SUCCESS.getMessage(), true, null);
	}
	
	/**
	 * 返回成功
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> success(RenderResultCode resultCode){
		return newRenderResult(RenderResultCode.SUCCESS.getCode(), resultCode.getMessage(), true, null);
	}
	
	/**
	 * 返回成功
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> success(T value){
		return newRenderResult(RenderResultCode.SUCCESS.getCode(), RenderResultCode.SUCCESS.getMessage(), true, value);
	}
	
	/**
	 * 返回失败
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> error(){
		return newRenderResult(RenderResultCode.ERROR.getCode(), RenderResultCode.ERROR.getMessage(), false, null);
	}
	
	/**
	 * 返回失败
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> error(String msg){
		return newRenderResult(RenderResultCode.ERROR.getCode(), msg, false, null);
	}
	
	/**
	 * 返回失败
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> error(RenderResultCode resultCode){
		return newRenderResult(resultCode.getCode(), resultCode.getMessage(), false, null);
	}
	
	/**
	 * 返回自定义消息
	 * @param resultCode
	 * @return
	 */
	public static <T> RenderResult<T> definedResult(RenderResultCode resultCode){
		return newRenderResult(resultCode.getCode(), resultCode.getMessage(), true, null);
	}
	
	/**
	 * 返回自定义消息
	 * @param resultCode
	 * @param value
	 * @return
	 */
	public static <T> RenderResult<T> definedResult(RenderResultCode resultCode, T value){
		return newRenderResult(resultCode.getCode(), resultCode.getMessage(), true, value);
	}
	
	
	/**
	 * 返回定义的成功或失败信息
	 *
	 * @param code 自定义code
	 * @param message 自定义返回的消息
	 * @param value 自定义返回的json对象
	 * @return {@link Result}
	 */
	public static <T> RenderResult<T> newRenderResult(int code, String msg, boolean success, T value) {
		return new RenderResult<T>(code, msg, success, value);
	}

}
