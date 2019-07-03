package cn.joytur.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * copy复制
 * @time 2018年7月31日 下午11:19:44
 */
public class JoyBeanUtil extends cn.hutool.core.bean.BeanUtil {
	
	public static Model copyBean(Model source, Model target){
		return target;
		
	}

    public static <T> T copyBean(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }
        try {
            T o = (T)target.newInstance();
            copyProperties(source, o);
            return o;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static <T> List<T> copyList(List<? extends Object> sourceList, Class<T> clazz) {
        if (sourceList == null || sourceList.size() == 0) {
            return null;
        }
        List<T> beanList = new ArrayList<T>();
        for (Object source : sourceList) {
            beanList.add(copyBean(source, clazz));
        }
        return beanList;
    }
}
