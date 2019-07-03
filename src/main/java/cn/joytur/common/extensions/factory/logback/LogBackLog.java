package cn.joytur.common.extensions.factory.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.log.Log;

public class LogBackLog extends Log {

	private Logger LOGGER;
	 
    LogBackLog(Class<?> clazz) {
    	LOGGER = LoggerFactory.getLogger(clazz);
    }
 
    LogBackLog(String name) {
    	LOGGER = LoggerFactory.getLogger(name);
    }

	@Override
	public void debug(String message) {
		LOGGER.debug(message);
	}

	@Override
	public void debug(String message, Throwable t) {
		LOGGER.debug(message, t);
	}

	@Override
	public void info(String message) {
		LOGGER.info(message);
	}

	@Override
	public void info(String message, Throwable t) {
		LOGGER.info(message, t);
	}

	@Override
	public void warn(String message) {
		LOGGER.warn(message);
	}

	@Override
	public void warn(String message, Throwable t) {
		LOGGER.warn(message, t);
	}

	@Override
	public void error(String message) {
		LOGGER.error(message);
	}

	@Override
	public void error(String message, Throwable t) {
		LOGGER.error(message, t);
	}

	@Override
	public void fatal(String message) {
		LOGGER.error(message);
	}

	@Override
	public void fatal(String message, Throwable t) {
		LOGGER.error(message, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return LOGGER.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return LOGGER.isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return LOGGER.isWarnEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return LOGGER.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		return LOGGER.isErrorEnabled();
	}

}
