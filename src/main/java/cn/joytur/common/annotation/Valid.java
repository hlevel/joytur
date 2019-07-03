package cn.joytur.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {

	String name() default "";		//字段名称
	String desc() default ""; 	//字段描述
	int min() default 0;		//最小长度
    int max() default 0;		//最大长度
	boolean required() default false; //是否必须
}
