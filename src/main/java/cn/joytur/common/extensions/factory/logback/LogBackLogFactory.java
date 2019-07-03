package cn.joytur.common.extensions.factory.logback;

import com.jfinal.log.ILogFactory;
import com.jfinal.log.Log;

/**
 * @author xuhang
 */
public class LogBackLogFactory implements ILogFactory {

	@Override
	public Log getLog(Class<?> clazz) {
		return new LogBackLog(clazz);
	}

	@Override
	public Log getLog(String name) {
		return new LogBackLog(name);
	}

}
