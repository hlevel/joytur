package cn.joytur.common.mvc.controller;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

import cn.hutool.core.util.ArrayUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.dto.AdminDTO;
import cn.joytur.common.utils.JWTUtil;


/**
 * 基础类
 * @author xuhang
 * @time 2018年7月31日 下午10:14:09
 */
public class BaseAdminController extends Controller {

	//protected final static Log LOGGER = Log.getLog(BaseAdminController.class);
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseAdminController.class);
	
	protected final static Integer DEF_PAGE = 1; //默认页数
	protected final static Integer DEF_SIZE = 10; //默认页条数
	
	/**
	 * 获取当前页
	 * @return
	 */
	protected Integer getPage() {
		String search = getSearch("page", "size");
		setAttr("pageUrl", getRequest().getServletPath() + (StrKit.isBlank(search) ? "?" : "&"));
		return getParaToInt("page", DEF_PAGE);
	}

	/**
	 *  获取页面大小
	 * @return
	 */
	protected Integer getSize() {
		return getParaToInt("size", DEF_SIZE);
	}
	
	/**
	 * 获取用户
	 * @return
	 */
	protected AdminDTO getAdminDTO(){
		AdminDTO adminDTO = JWTUtil.getAdminDTO(this);
		return adminDTO;
	}
	
	/**
	 * 实现转换dto
	 * @param DataModelClass
	 * @return
	 
	protected <T> T getDataModelDTO(Class<T> DataModelClass){
		Field[] fields = ReflectUtil.getFields(DataModelClass);
		
		for(Field field : fields){
			System.out.println(field.getName());
			//ReflectUtil.invoke(obj, method, args);
		}
		
		return null;
	}*/
	
	/**
	 * 获取搜索条件
	 * @return
	 */
	protected String getSearch(){
		return getSearch("");
	}
	
	/**
	 * 获取搜索条件
	 * @return
	 */
	protected String getSearch(String... ignoreValue){
		StringBuilder builder = new StringBuilder();
		
		boolean isFirst = true;
		Enumeration<String> ePara = getParaNames();
		while(ePara.hasMoreElements()){
			String name = ePara.nextElement();
			
			if(ArrayUtil.contains(ignoreValue, name)){	//忽略掉搜索条件
				continue;
			}
			
			String value = getPara(name);
			if(StrKit.notBlank(getPara(name))){
				builder.append(isFirst ? "?" : "&").append(name).append("=").append(value);
				isFirst = false;
			}
		}
		return builder.toString();
	}
	
	/**
	 * 获取全戳
	 * @return
	 */
	private String getPrefix(){
		return "/templates/" + PropKit.get(CommonAttribute.SYSTEM_THEME);
	}
	
	/**
	 * 输出判断路径
	 * @param view
	 */
	protected void renderTpl(String view){
		render(getPrefix() + "/admin/" + view);
	}
	
	/**
	 * 输出判断路径
	 * @param view
	 */
	protected void redirectUrl(String url){
		redirect(PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH) + "/" + url);
	}
	
}
