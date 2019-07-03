package cn.joytur.common.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.ehcache.CacheKit;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.Enums.SortType;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.modules.system.entities.SysDictionary;

/**
 * 字典工具类
 * @author xuhang
 */
public class JoyDictUtil {
	
	public static final String CACHE_DICT_KEY = "joy_dict_key";
	
	public static String getDictId(String value, String code, String defaultValue){
		if (StrUtil.isNotBlank(code) && StrUtil.isNotBlank(value)){
			for (SysDictionary sysDictionary : getDictList(code)){
				if (code.equals(sysDictionary.getDictCode()) && value.equals(sysDictionary.getDictValue())){
					return String.valueOf(sysDictionary.getId());
				}
			}
		}
		return defaultValue;
	}
	
	public static String getDictLabel(String value, String code, String defaultValue){
		if (StrUtil.isNotBlank(code) && StrUtil.isNotBlank(value)){
			for (SysDictionary sysDictionary : getDictList(code)){
				if (code.equals(sysDictionary.getDictCode()) && value.equals(sysDictionary.getDictValue())){
					return sysDictionary.getDictName();
				}
			}
		}
		return defaultValue;
	}
	
	public static String getDictLabels(String values, String code, String defaultValue){
		if (StrUtil.isNotBlank(code) && StrUtil.isNotBlank(values)){
			List<String> valueList = new ArrayList<String>();
			for (String value : StrUtil.split(values, ",")){
				valueList.add(getDictLabel(value, code, defaultValue));
			}
			return ArrayUtil.join(valueList, ",");
		}
		return defaultValue;
	}

	public static String getDictValue(String name, String code, String defaultLabel){
		if (StrUtil.isNotBlank(code) && StrUtil.isNotBlank(name)){
			for (SysDictionary sysDictionary : getDictList(code)){
				if (code.equals(sysDictionary.getDictCode()) && name.equals(sysDictionary.getDictName())){
					return sysDictionary.getDictValue();
				}
			}
		}
		return defaultLabel;
	}
	
	public static List<SysDictionary> getDictList(String code){
		@SuppressWarnings("unchecked")
		Map<String, List<SysDictionary>> dictMap = (Map<String, List<SysDictionary>>) CacheKit.get(CommonAttribute.CACHE_SYSTEM, CACHE_DICT_KEY);
		
		if (dictMap==null){
			dictMap = new HashMap<String, List<SysDictionary>>();
			for (SysDictionary sysDict : SysDictionary.dao.findAll(new Sort("sort", SortType.ASC))){
				List<SysDictionary> dictList = dictMap.get(sysDict.getDictCode());
				if (dictList != null){
					dictList.add(sysDict);
				}else{
					List<SysDictionary> sysDictionaryList = new ArrayList<SysDictionary>();
					sysDictionaryList.add(sysDict);
					
					dictMap.put(sysDict.getDictCode(), sysDictionaryList);
				}
			}
			CacheKit.put(CommonAttribute.CACHE_SYSTEM, CACHE_DICT_KEY, dictMap);
		}
		
		List<SysDictionary> sysDictList = dictMap.get(code);
		if (sysDictList == null){
			sysDictList = new ArrayList<>();
		}
		sysDictList.sort(Comparator.comparing(SysDictionary::getSort));
		return sysDictList;
	}
	
	/**
	 * 返回字典列表（JSON）
	 * @param type
	 * @return
	 */
	public static String getDictListJson(String type){
		return JsonKit.toJson(getDictList(type));
	}
	
	/**
	 * 清除缓存
	 */
	public static void clearCache() {
		CacheKit.remove(CommonAttribute.CACHE_SYSTEM, CACHE_DICT_KEY);
	}
	
}
