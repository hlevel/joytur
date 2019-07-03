package cn.joytur.common.utils;

import javax.sql.DataSource;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * 在数据库表有任何变动时，运行一下 main 方法，极速响应变化进行代码重构
 */
public class _JfinalModelGeneratorUtil {
	
	public static DataSource getDataSource() {
		PropKit.use("joytur.conf");
		DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("jdbc.url"), PropKit.get("jdbc.username"), PropKit.get("jdbc.password"),PropKit.get("jdbc.driver"));
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}
	
	public static void main(String[] args) {
		// base DataModel 所使用的包名
		String baseDataModelPackageName = "cn.yayap.modules.wamall.entities.base";
		// base DataModel 文件保存路径
		String baseDataModelOutputDir = "d:/src/cn/yayap/modules/entities/base";
		
		// DataModel 所使用的包名 (MappingKit 默认使用的包名)
		String DataModelPackageName = "cn.yayap.modules.wamall.entities";
		// DataModel 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String DataModelOutputDir = baseDataModelOutputDir + "/..";
		
		// 创建生成器
		Generator generator = new Generator(getDataSource(), baseDataModelPackageName, baseDataModelOutputDir, DataModelPackageName, DataModelOutputDir);
		// 设置是否生成链式 setter 方法
		generator.setGenerateChainSetter(false);
		// 添加不需要生成的表名
//		generator.addExcludedTable("adv");
		// 设置是否在 DataModel 中生成 dao 对象
		generator.setGenerateDaoInModel(true);
		// 设置是否生成链式 setter 方法
		generator.setGenerateChainSetter(true);
		// 设置是否生成字典文件
		generator.setGenerateDataDictionary(false);
		// 设置需要被移除的表名前缀用于生成DataModelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的DataModel名为 "User"而非 OscUser
		generator.setRemovedTableNamePrefixes("joy_");
		// 生成
		generator.generate();
	}
}




