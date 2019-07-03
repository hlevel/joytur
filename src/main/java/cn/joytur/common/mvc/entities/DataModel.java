package cn.joytur.common.mvc.entities;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;

import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.dto.Sort;

/**
 * 基础base 作用于添加公共查询
 * @author xuhang
 *
 * @param <M>
 */
@SuppressWarnings({ "serial", "rawtypes" })
public abstract class DataModel<M extends DataModel> extends Model<M> {

	/**
	 * 查询所有
	 * @return
	 */
	public List<M> findAll(){
		return findAll(null);
	}
	
	/**
	 * 查询所有
	 * @return
	 */
	public List<M> findAll(Sort sort){
		String sql = createSelectSql("SELECT * FROM", null, sort);
		return find(sql);
	}
	
	/**
	 * 查询等于条件数据集合
	 * @param query
	 * @return
	 */
	public List<M> findList(M query){
		return findList(query, new Sort("create_time", Enums.SortType.DESC));
	}
	
	/**
	 * 查询等于条件数据集合
	 * @param query 查询
	 * @param sort 排序
	 * @return
	 */
	public List<M> findList(M query, Sort sort){
		if(query == null) {
			return null;
		}
		
		return find(createSqlPara(query, sort));
	}
	
	/**
	 * 查询第一个
	 * @param query
	 * @return
	 */
	public M findByModel(M query) {
		List<M> mlist = findList(query);
		
		if(mlist == null || mlist.isEmpty()) {
			return null;
		}
		
		return mlist.get(0);
	}
	
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param query
	 * @return
	 */
	public Page<M> paginate(int pageNumber, int pageSize, M query){
		return paginate(pageNumber, pageSize, query, new Sort("create_time", Enums.SortType.DESC));
	}
	
	/**
	 * 分页查询
	 * @param pageNumber
	 * @param pageSize
	 * @param query
	 * @param sort
	 * @return
	 */
	public Page<M> paginate(int pageNumber, int pageSize, M query, Sort sort){
		return paginate(pageNumber, pageSize, createSqlPara(query, sort));
	}

	/**
	 * count统计
	 * @param query
	 * @return
	 */
	public int findCountByModel(M query) {
		return Db.queryInt(createSelectSql("SELECT count(1) FROM", query), query._getAttrValues());
	}
	
	/**
	 * 对当前赋值model字段进行更新
	 * @param update 需要更新model
	 * @param id 主键id 更新条件
	 * @return
	 */
	public int updateModelById(M update, String id){
		String updateSql = createUpdateSql(update, null) + " WHERE id=?";
		
		List<Object> pList = new ArrayList<Object>(java.util.Arrays.asList(update._getAttrValues()));
		pList.add(id);
		
		return Db.update(updateSql, pList.toArray(new Object[]{}));
	}
	
	/**
	 * 对当前赋值model字段进行更新
	 * @param update 需要更新model
	 * @param where 更新条件
	 * @return
	 */
	public int updateModelByModel(M update, M where){
		String updateSql = createUpdateSql(update, where);
		List<Object> pList = new ArrayList<Object>(java.util.Arrays.asList(update._getAttrValues()));
		pList.addAll(java.util.Arrays.asList(where._getAttrValues()));
		
		return Db.update(updateSql, pList.toArray(new Object[]{}));
	}
	
	/**
	 * 根据对象删除
	 * @param query
	 * @return
	 */
	public int deteleByModel(M query){
		return Db.update(createSelectSql("delete FROM", query), query._getAttrValues());
	}
	
	
	/**
	 * 创建基础条件
	 * @param query
	 * @return
	 */
	private SqlPara createSqlPara(M query, Sort sort) {
		Kv cond = Kv.create();
		for(String name : query._getAttrNames()) {
			Object val = query.get(name);
			if(query.get(name) != null && StrKit.notBlank(val.toString())) {
				cond.set(name + "=", val);
			}
		}
		
		Kv fKv = Kv.by("tab", query._getTable().getName()).set("cond", cond);
		if(sort != null){
			fKv.set("sort", sort);
		}
		
		SqlPara sqlPara = Db.getSqlPara("common.findList", fKv);
		return sqlPara;
	}
	
	/**
	 * 创建sql动态单表
	 * @param query
	 * @return
	 */
	private String createSelectSql(String from, M query){
		return createSelectSql(from, query, null);
	}
	
	/**
	 * 创建sql动态单表
	 * @param query
	 * @return
	 */
	private String createSelectSql(String from, M query, Sort sort){
		StringBuilder builder = new StringBuilder(from + " "+ _getTable().getName());
		boolean isFirst = true;
		if(query != null){
			for(String name : query._getAttrNames()) {
				Object val = query.get(name);
				if(val != null && StrKit.notBlank(val.toString())) {
					builder.append(isFirst ? " WHERE " : " AND ").append(name).append("=?");
					isFirst = false;
				}
			}
		}
		
		if(sort != null){
			builder.append(" order by ").append(sort.getName()).append(" ").append(sort.getSortType().name());
		}
		
		return builder.toString();
	}
	
	/**
	 * 创建sql动态单表
	 * @param query
	 * @return
	 */
	private String createUpdateSql(M update, M where){
		StringBuilder builder = new StringBuilder("UPDATE "+ _getTable().getName());
		if(update != null){
			boolean isFirst = true;
			for(String name : update._getAttrNames()) {
				Object val = update.get(name);
				if(val != null && StrKit.notBlank(val.toString())) {
					builder.append(isFirst ? " SET " : ",").append(name).append("=?");
					isFirst = false;
				}
			}
		}
		
		if(where != null){
			boolean isFirst = true;
			for(String name : where._getAttrNames()) {
				Object val = where.get(name);
				if(val != null && StrKit.notBlank(val.toString())) {
					builder.append(isFirst ? " WHERE " : " AND ").append(name).append("=?");
					isFirst = false;
				}
			}
		}
		
		return builder.toString();
	}
	
}
