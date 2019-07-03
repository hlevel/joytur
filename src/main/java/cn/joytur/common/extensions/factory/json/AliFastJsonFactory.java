package cn.joytur.common.extensions.factory.json;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.jfinal.json.FastJsonFactory;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Record;

public class AliFastJsonFactory implements IJsonFactory{

	private static final FastJsonFactory me = new FastJsonFactory();
	
	public static FastJsonFactory me() {
		return me;
	}
	
	public Json getJson() {
		return new AliFastJson();
	}
	
	/**
	 * 移除 FastJsonRecordSerializer
	 * 仅为了与 jfinal 3.3 版本之前版本的行为保持一致
	 */
	public void removeRecordSerializer() {
		SerializeConfig.getGlobalInstance().put(Record.class, null);
	}

}
