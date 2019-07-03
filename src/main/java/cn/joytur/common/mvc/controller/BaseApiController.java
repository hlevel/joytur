package cn.joytur.common.mvc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;

import cn.joytur.common.mvc.constant.CommonAttribute;


/**
 * api类型
 * @author xuhang
 *
 */
public class BaseApiController extends Controller {

	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseApiController.class);
	
	
	/**
	 * 获取全戳
	 * @return
	 */
	protected String getPrefix(){
		return "/templates/" + PropKit.get(CommonAttribute.SYSTEM_THEME);
	}
	
}
