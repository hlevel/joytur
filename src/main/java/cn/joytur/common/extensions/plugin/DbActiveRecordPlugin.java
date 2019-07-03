package cn.joytur.common.extensions.plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;

import cn.hutool.core.util.StrUtil;
import cn.joytur.common.mvc.JoyTurConfig;
import cn.joytur.common.mvc.constant.CommonAttribute;

/**
 * 数据库初始化权限
 * @author xuhang
 * @time 2019年6月27日 上午9:34:20
 */
public class DbActiveRecordPlugin extends ActiveRecordPlugin{

	public static org.slf4j.Logger LOGGER  = org.slf4j.LoggerFactory.getLogger(JoyTurConfig.class);
	
	public DbActiveRecordPlugin(DruidPlugin druidPlugin) {
		super(druidPlugin);
	}

	@Override
	public boolean start() {
		//锁文件
		//String pathLock = PathKit.getRootClassPath() + File.separator + CommonAttribute.DB_DDL_LOCK;
		//if(!FileUtil.exist(pathLock)){
		if(PropKit.getBoolean(CommonAttribute.SYSTEM_DDL_CREATE)) {
			Connection conn = null;
			try {
				this.getSqlKit().parseSqlTemplate();
				
				String sql = this.getSqlKit().getSql("dbinit");
				
				//sql做转换
				sql = (sql.replaceAll("\n", ""));
				String[] sqlList = StrUtil.split(sql, ";");
				
				conn = this.dataSourceProvider.getDataSource().getConnection();
				Statement stm = conn.createStatement();
				for(String s : sqlList){
					stm.addBatch(s);
				}
				stm.executeBatch();
				
				//执行完成后创建锁文件
				//FileUtil.writeUtf8String("", pathLock);
			} catch (SQLException e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		return super.start();
	}

}
