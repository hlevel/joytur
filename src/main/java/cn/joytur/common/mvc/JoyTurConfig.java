package cn.joytur.common.mvc;

import java.io.File;
import java.util.Set;

import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.TabMapping;
import cn.joytur.common.extensions.directive.FromDayDirective;
import cn.joytur.common.extensions.directive.ImageDirective;
import cn.joytur.common.extensions.factory.json.AliFastJsonFactory;
import cn.joytur.common.extensions.factory.logback.LogBackLogFactory;
import cn.joytur.common.extensions.plugin.DbActiveRecordPlugin;
import cn.joytur.common.handler.ResourcesHandler;
import cn.joytur.common.interceptor.AuthInterceptor;
import cn.joytur.common.interceptor.ExceptionInterceptor;
import cn.joytur.common.interceptor.ValidInterceptor;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.entities.DataModel;

public class JoyTurConfig extends JFinalConfig {

	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(JoyTurConfig.class);
	
	public void configConstant(Constants me) {
		PropKit.use(CommonAttribute.CONFIG_PROPERTIES);
		/** 开发者模式 */
		me.setDevMode(PropKit.getBoolean("system.devMode", false));
		/** 配置页面 */
		me.setViewExtension(CommonAttribute.VIEW_EXTENSION);
		me.setError404View(CommonAttribute.ADMIN_NOTFOUNT_VIEW);
		me.setError500View(CommonAttribute.ADMIN_ERROR_VIEW);
		/** 编码配置 */
		me.setEncoding(CommonAttribute.UTF_8);
		/** 上传文件临时目录 */
		me.setBaseUploadPath(CommonAttribute.BASE_UPLOAD_TMP_PATH);
		/** 设置参数分隔符 */
		me.setUrlParaSeparator(CommonAttribute.URL_PARA_SEPARATOR);
		/** 设置JSON */
		me.setJsonFactory(new AliFastJsonFactory());
		me.setJsonDatePattern(CommonAttribute.JSON_DATE_PATTERN);
		me.setLogFactory(new LogBackLogFactory());
		me.setInjectDependency(true);
	}

	@SuppressWarnings("unchecked")
	public void configRoute(Routes me) {
		String admin = PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH);
		String packageName = PropKit.get(CommonAttribute.SYSTEM_PACKAGE);
		
		Set<Class<?>> controllerClassList = ClassUtil.scanPackageByAnnotation(packageName, RouteMapping.class);
		
		if (controllerClassList != null) {
			for (Class<?> clazz : controllerClassList) {
				RouteMapping urlMapping = clazz.getAnnotation(RouteMapping.class);
				if (null != urlMapping && StrKit.notBlank(urlMapping.url())) {
					me.add(urlMapping.url().replace("${admin}", admin), (Class<? extends Controller>) clazz);
				}
			}
		}
	}

	public void configEngine(Engine me) {
		me.addSharedObject("ctx", "");
		me.addSharedObject("ctxAd", PropKit.get(CommonAttribute.SYSTEM_ADMIN_PATH));
		me.addSharedObject("ctxAdSt", CommonAttribute.BASE_STATIC_PATH + PropKit.get(CommonAttribute.SYSTEM_THEME) + "/admin");
		me.addSharedObject("ctxWapSt", CommonAttribute.BASE_STATIC_PATH + PropKit.get(CommonAttribute.SYSTEM_THEME) + "/wap");
		
		me.addSharedObject("joyPage", new cn.joytur.common.utils.JoyPageUtil());
		me.addSharedObject("joyDict", new cn.joytur.common.utils.JoyDictUtil());
		me.addSharedObject("joyConfig", new cn.joytur.common.utils.JoyConfigUtil());
		
		me.addDirective("fromday", FromDayDirective.class);
		me.addDirective("image", ImageDirective.class);
		
		me.addSharedFunction("/templates/"+PropKit.get(CommonAttribute.SYSTEM_THEME)+"/admin/common/_layout.html");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void configPlugin(Plugins me) {
		String jdbcType = PropKit.get("jdbc.type");
		String jdbcDriver = PropKit.get("jdbc.driver");
		String jdbcUrl = PropKit.get("jdbc.url");
		String jdbcUsername = PropKit.get("jdbc.username");
		String jdbcPassword = Base64.decodeStr(PropKit.get("jdbc.password"));
		//判断绝对路径更改
		if(StrUtil.containsIgnoreCase(jdbcUrl, "classpath")){
			String cpth = PathKit.getRootClassPath() + File.separator + "db";
			jdbcUrl = StrUtil.replaceIgnoreCase(jdbcUrl, "classpath", cpth);
			if(PropKit.getBoolean("system.devMode", false)){
				System.out.println(cpth);
			}
		}
		
		/** 数据库配置 */
        DruidPlugin druidPlugin = new DruidPlugin(jdbcUrl, jdbcUsername, jdbcPassword, jdbcDriver);
        druidPlugin.addFilter(new StatFilter());
        druidPlugin.addFilter(new Slf4jLogFilter());
        WallFilter wall = new WallFilter();
        wall.setDbType(jdbcType);//mysql,h2等
        wall.setLogViolation(true);
        
        druidPlugin.addFilter(wall);
        
        me.add(druidPlugin);
        DbActiveRecordPlugin activeRecordPlugin = new DbActiveRecordPlugin(druidPlugin);
		/** 打印sql */
		activeRecordPlugin.addSqlTemplate("sql/"+jdbcType+"/all.sql");
		activeRecordPlugin.addSqlTemplate("sql/"+jdbcType+"/joytur.sql");
		activeRecordPlugin.setShowSql(false);
		Engine engine = activeRecordPlugin.getEngine();
		// 上面的代码获取到了用于 sql 管理功能的 Engine 对象，接着就可以开始配置了
		engine.setToClassPathSourceFactory();
		engine.addSharedMethod(new StrKit());
		
		me.add(activeRecordPlugin);
		/** 表对应的实体配置 */
		String packageName = PropKit.get(CommonAttribute.SYSTEM_PACKAGE);
		Set<Class<?>> modelClassList = ClassUtil.scanPackage(packageName);
		
		if (modelClassList != null) {
			for (Class<?> clazz : modelClassList) {
				TabMapping tabMapping = clazz.getAnnotation(TabMapping.class);
				if (null != tabMapping && StrKit.notBlank(tabMapping.tabName(), tabMapping.id())) {
					activeRecordPlugin.addMapping(tabMapping.tabName(), tabMapping.id(), (Class<? extends DataModel<? extends DataModel>>) clazz);
				}
			}
		}
		
		/** 定时任务 */
		//me.add(new Cron4jPlugin(CommonAttribute.JOB_PROPERTIES));
		/** 缓存 */
		me.add(new EhCachePlugin());
		/** 异步消息 
		// 初始化插件
		EventPlugin plugin = new EventPlugin();
		// 设置为异步，默认同步，或者使用`threadPool(ExecutorService executorService)`自定义线程池。
		plugin.async();
		// 设置扫描jar包，默认不扫描
		plugin.scanJar();
		// 设置监听器默认包，多个包名使用;分割，默认全扫描
		plugin.scanPackage("io.ymlrshop.actions.listener");
		// bean工厂，默认为DefaultBeanFactory，可实现IBeanFactory自定义扩展
		// 对于将@EventListener写在不含无参构造器的类需要使用`ObjenesisBeanFactory`
		plugin.beanFactory(new DuangBeanFactory());
		me.add(plugin);  
		*/
	}

	public void configInterceptor(Interceptors me) {
		/** session */
		//me.add(new SessionInViewInterceptor());
		
		me.add(new ExceptionInterceptor());
		me.add(new ValidInterceptor());
		me.add(new AuthInterceptor());
	}

	public void configHandler(Handlers me) {
		me.add(new ResourcesHandler());
		me.add(new UrlSkipHandler("/h2/console/*" , false));
	}
	
	@Override
	public void afterJFinalStart() {
		new JoyTurAppInitData().initData();
		//如果有公众号则默认加载
	}
	
}
