package cn.joytur.common.extensions.factory.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jfinal.json.FastJsonRecordSerializer;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Record;

public class AliFastJson extends Json{

	static {
		// 支持序列化 ActiveRecord 的 Record 类型
		SerializeConfig.getGlobalInstance().put(Record.class, new FastJsonRecordSerializer());
	}
	
	public static AliFastJson getJson() {
		return new AliFastJson();
	}
	
	public String toJson(Object object) {
		// 优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
		String dp = datePattern != null ? datePattern : getDefaultDatePattern();
		if (dp == null) {
			return JSON.toJSONString(object);
		} else {
			//return JSON.toJSONStringWithDateFormat(object, dp, SerializerFeature.WriteMapNullValue);	// 如果为null 默认过滤
			return JSON.toJSONStringWithDateFormat(object, dp, SerializerFeature.WriteNullStringAsEmpty);	// 如果为null 默认 ""
		}
	}
	
	public <T> T parse(String jsonString, Class<T> type) {
		return JSON.parseObject(jsonString, type);
	}

}
