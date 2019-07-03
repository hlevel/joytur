package cn.joytur.common.interceptor;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.ValidErrorException;
import cn.joytur.common.mvc.constant.RenderResultCode;


/**
 * 参数拦截处理
 * @author xuhang
 */
public class ValidInterceptor implements Interceptor {
	
	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(ValidInterceptor.class);
	
	/*
	private final static Class<?>[] CLAZZ_TYPE = {int.class, double.class, long.class, short.class, byte.class, boolean.class, char.class, float.class
			, Integer.class , Double.class, Float.class , Long.class , Short.class , Byte.class , Boolean.class , Character.class , String.class, BigDecimal.class};
	*/
	
	public void intercept(Invocation inv) {
		
		Controller controller = inv.getController();
		Method method = inv.getMethod(); //当前访问的Action
		
		Valids valids = method.getAnnotation(Valids.class);
		if(valids != null){
			for(Valid valid : valids.value()){
				validField(valid, valid.name(), valid.desc(), controller.getPara(valid.name()));
			}
		}
		
        inv.invoke();
    }
	
	private void validField(Valid valid, String fieldName, String fieldDesc, Object fieldValue){
		
		String tipName = (StrKit.isBlank(fieldDesc) ? fieldName : fieldDesc);
		
		// 获取对象的成员的注解信息
		if(valid.required() == true && fieldValue == null){
			String message = ("{0}不能为空");
			throw new ValidErrorException(RenderResultCode.PARAM.getCode(), MessageFormat.format(message, tipName));
		}
		
		if(fieldValue == null){
			return;
		}
		
		String value = fieldValue.toString();
		
		if(valid.required() == true && StrKit.isBlank(value)){
			String message = ("{0}不能为空");
			throw new ValidErrorException(RenderResultCode.PARAM.getCode(), MessageFormat.format(message, tipName));
		}
		
		if(StrKit.notBlank(value) && valid.max() > 0 && value.length() > valid.max()){
			String message = ("{0}长度不能超过{1}位");
			throw new ValidErrorException(RenderResultCode.PARAM.getCode(), MessageFormat.format(message, tipName, valid.max()));
		}
		
		if (valid.required() == true && valid.min() > 0 && value.length() < valid.min()) {
			String message = ("{0}长度不能小于{1}位");
			throw new ValidErrorException(RenderResultCode.PARAM.getCode(), MessageFormat.format(message, tipName, valid.min()));
		}
		
	}
	
	/*
	private boolean checked(Class<?> type){
		for (int i = 0; i < CLAZZ_TYPE.length; i++) {
			if(type == CLAZZ_TYPE[i]){
				return true;
			}
		}
		return false;
	}
	*/
}
