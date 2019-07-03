package cn.joytur.common.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.region.Region;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.Enums;

/**
 * 腾讯oss
 * @author xuhang
 * @time 2019年1月14日 下午3:59:48
 */
public class JoyUploadFileUtil {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(JoyUploadFileUtil.class);
	
	private static COSClient cosClient;
	private static String bucket;
	
	/**
	 * 初始化
	 */
	private static COSClient buildCOSClient() {
		if(cosClient != null){
			cosClient.shutdown();
		}
		// https://cloud.tencent.com/document/product/436/10199
		// https://blog.csdn.net/xlgen157387/article/details/51490995
		// 1 初始化用户身份信息（secretId, secretKey）。
		String secretId = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_SECRETID.name());
		String secretKey = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_SECRETKEY.name());
		String endpoint = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_ENDPOINT.name());
		bucket = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_BUCKETNAME.name());
		
		COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
		// 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
		// clientConfig中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者接口文档 FAQ 中说明。
		ClientConfig clientConfig = new ClientConfig(new Region(endpoint));
		// 3 生成 cos 客户端。
		return new COSClient(cred, clientConfig);
	}
	
	/**
	 * 适配上传无oss则转存本地
	 * @param upFile
	 * @return
	 */
	public static String uploadAdapter(UploadFile upFile) {
		String resFileName = null;
		//如果配置云则上传云
		if(JoyUploadFileUtil.isConfig()){
			resFileName = uploadTencentOSS(upFile);
		}else {
			resFileName = uploadLocal(upFile);
		}
		return resFileName;
	}
	
	/**
	 * 上传到本地
	 * @param upFile
	 * @return
	 */
	public static String uploadLocal(UploadFile upFile) {
		try {
			String suffix = StrUtil.subAfter(upFile.getFileName(), ".", true);
			if(StrUtil.isNotBlank(suffix)) {
				suffix = "." + suffix;
			}
			String newFileName = DateUtil.format(new java.util.Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + suffix;
			
			//新建文件夹
			String newFileFolder = DateUtil.format(new java.util.Date(), "yyyy" + File.separator + "MM" + File.separator + "dd");
			String newFileFolderPath = StrUtil.replace(upFile.getUploadPath(), CommonAttribute.BASE_UPLOAD_FOLDER_NAME, "");
			String newUploadFolderPath = newFileFolderPath + newFileFolder;
			File newUpFolderPath = new File(newUploadFolderPath);
			//如果文件夹不存在则创建
			if(!newUpFolderPath.exists()) {
				newUpFolderPath.mkdirs();
			}
			
			String newFilePath = newUpFolderPath + File.separator + newFileName;
			upFile.getFile().renameTo(new File(newFilePath));
			
			String resFilePath = File.separator + CommonAttribute.BASE_UPLOAD_PARENT_PATH + newFileFolder + File.separator + newFileName;
			return resFilePath;
		}catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 上传到oss
	 * @param key
	 * @param file
	 * @return
	 */
	public static String uploadTencentOSS(UploadFile upFile) {
		try {
			String suffix = StrUtil.subAfter(upFile.getFileName(), ".", true);
			if(StrUtil.isNotBlank(suffix)) {
				suffix = "." + suffix;
			}
			String newFileName = DateUtil.format(new java.util.Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + suffix;
			String resFileName = uploadTencentOSS(bucket, newFileName, upFile.getFile());
			return resFileName;
		}catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
    }
	
	public static boolean existTencentOSS(String key) {
    	return existTencentOSS(bucket, key);
    }
	
	public static void deleteTencentOSS(String key) {
		deleteTencentOSS(bucket, key);
    }
	
	public static void downloadToFile(String key, File file) {
		downloadToTencentOSSFile(bucket, key, file);
	}
	
    public static void downloadToTencentOSSFile(String bucket, String key, File file) {
    	if(cosClient == null){
    		cosClient = buildCOSClient();
    	}
    	cosClient.getObject(new GetObjectRequest(bucket, key), file);
    }
    
    public static String uploadTencentOSS(String bucket, String key, File file) {
    	if(cosClient == null){
    		cosClient = buildCOSClient();
    	}
    	cosClient.putObject(bucket, key, file);
    	return key;
    }

    public static boolean existTencentOSS(String bucket, String key) {
    	if(cosClient == null){
    		cosClient = buildCOSClient();
    	}
    	return cosClient.doesObjectExist(bucket, key);
    }
    
    public static void deleteTencentOSS(String bucket, String key) {
    	if(cosClient == null){
    		cosClient = buildCOSClient();
    	}
    	cosClient.deleteObject(bucket, key);
    }
    
    /**
     * 是否配置
     * @return
     */
    public static boolean isConfig(){
    	String secretId = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_SECRETID.name());
		String secretKey = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_SECRETKEY.name());
		String endpoint = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_ENDPOINT.name());
		bucket = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_BUCKETNAME.name());
		
		return StrKit.notBlank(secretId, secretKey, endpoint, bucket);
    }
    
    /**
     * 获取http全路径
     * @param fileName
     * @return
     */
    public static String getHttpPath(String fileName){
    	
    	if(StrKit.isBlank(fileName)){
    		return "";
    	}
    	String tmpFileName = fileName.replace("\\", "/");
    	String tmpPrxUpload = CommonAttribute.BASE_UPLOAD_PARENT_PATH.replace("\\", "/");

		//判断是否是本地图片
		if(tmpFileName.contains(tmpPrxUpload)) {
			return JoyConfigUtil.getSdUrl("") + tmpFileName;
		}
		
		//云展示地址
		String BUCKETNAME = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_BUCKETNAME.name(), "");
		String ENDPOINT = JoyConfigUtil.getConfigValue(Enums.SysConfigType.SYS_CONFIG_TENCENT_ENDPOINT.name(), "");
		
    	return "http://" + BUCKETNAME + ".cos." + ENDPOINT + ".myqcloud.com/" + tmpFileName;
    }
	
    /**
     * 获取本地硬盘文件路径
     * @param fileName
     * @return
     */
    public static String getDiskPath(String fileName){
    	
    	return null;
    }
    
	/**
	 * 清除缓存
	 */
	public static void clearCache() {
		cosClient.shutdown();
		cosClient = null;
	}
	
}
