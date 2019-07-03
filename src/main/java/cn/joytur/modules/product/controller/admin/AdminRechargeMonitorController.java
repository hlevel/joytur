package cn.joytur.modules.product.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NetUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.extensions.wechat.weblistener.WechatManager;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.WebWechatStatus;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.UploadBean;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.common.utils.JoyQrCodeUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.product.service.WebWechatMonitorService;
import cn.joytur.modules.system.entities.SysConfig;

/**
 * 收款监控
 * @author xuhang
 */
@RouteMapping(url = "${admin}/recharge/monitor")
public class AdminRechargeMonitorController extends BaseAdminController {

	private static final String MONITOR_TAB = "_monitorTab"; //cookie
	
	@Inject
	private WebWechatMonitorService webWechatMonitorService;
	
    /**
	 * 列表
	 */
	@AuthRequire.Perms("product.recharge.monitor.view")
	public void index() {
		List<SysConfig> sysConfigList = SysConfig.dao.findAll();
		
		boolean signAdd = false;
		for(SysConfig tmpSysConfig : sysConfigList) {
			//判断是否存在签名
			if(tmpSysConfig.getName().equals(Enums.SysConfigType.MONITOR_APP_SIGN.name())){
				signAdd = true;
			}
		}
		
		if(!signAdd){
			SysConfig newSysConfig = new SysConfig();
			newSysConfig.setId(IdUtil.simpleUUID());
			newSysConfig.setName(Enums.SysConfigType.MONITOR_APP_SIGN.name());
			newSysConfig.setValue(IdUtil.fastSimpleUUID());
			newSysConfig.setCreateTime(new java.util.Date());
			newSysConfig.setUpdateTime(new java.util.Date());
			newSysConfig.save();
			
			sysConfigList = SysConfig.dao.findAll();
		}
		
		Map<String, String> configMap = sysConfigList.stream().collect(Collectors.toMap(SysConfig::getName, SysConfig::getValue));
		
		int port = getRequest().getLocalPort();
		String localPort = port == 80 ? "" : ("" + port);
		//app配置
		//String sdUrl = JoyConfigUtil.getSdUrl(NetUtil.getLocalhost().getHostAddress()) + localPort;
		String sdUrl = JoyConfigUtil.getSdUrl(NetUtil.getLocalhost().getHostAddress(), localPort);
		if(!sdUrl.endsWith("/")){
			sdUrl += "/";
		}
		
		//去掉http:// 和 https://
		sdUrl = sdUrl.replace("http://", "");
		sdUrl = sdUrl.replace("https://", "");
		
		Integer appSettingStatus = 0;
		Integer webSettingStatus = 0;
    	Integer mailSettingStatus = 0;
		
		String appStatus = "监控端未绑定，请您扫码绑定";
		String appMonitorUrl = sdUrl + configMap.get(Enums.SysConfigType.MONITOR_APP_SIGN.name());
		String appLastHeartbeat = configMap.get(Enums.SysConfigType.MONITOR_APP_HEARTBEAT.name()) == null ? "无" : configMap.get(Enums.SysConfigType.MONITOR_APP_HEARTBEAT.name());
		String appMonitorQrCode = JoyQrCodeUtil.enQrCodeToBase64(appMonitorUrl);
		
		if(appLastHeartbeat == null || StrKit.equals(appLastHeartbeat, "无")){
			appLastHeartbeat = "无";
		}else{
			long minute = DateUtil.between(DateUtil.parseDateTime(appLastHeartbeat), new java.util.Date(), DateUnit.MINUTE);
			
			if(minute < 1){
				appStatus = "监控中";
				appSettingStatus = 1;
			}
			
		}
		
		//wechat监控
		String webMonitorQrCode = "test";
		
		String sendMail = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_SEND_MAIL.name());
    	String sendMailPwd = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_SEND_MAIL_PWD.name());
    	String receiptMail = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_RECEIPT_MAIL.name());
    	
		if(StrKit.notBlank(sendMail, sendMailPwd, receiptMail)){
			mailSettingStatus = 1;
		}
		
		//邮件控制
		String monitorMailReceivables = configMap.get(Enums.SysConfigType.MONITOR_MAIL_RECEIVABLES.name());
		//tabid
		String monitorTab = (getCookie(MONITOR_TAB) == null ? "11" : getCookie(MONITOR_TAB));
		
		
		setAttr("appStatus", appStatus).setAttr("appLastHeartbeat", appLastHeartbeat).setAttr("appMonitorUrl", appMonitorUrl).setAttr("appMonitorQrCode", appMonitorQrCode);
		setAttr("appSettingStatus", appSettingStatus).setAttr("webSettingStatus", webSettingStatus).setAttr("mailSettingStatus", mailSettingStatus).setAttr("tabid", monitorTab);
		setAttr("webMonitorQrCode", webMonitorQrCode);
		setAttr("mailMonitorReceivables", monitorMailReceivables);
		setAttr("config", configMap);
		renderTpl("product/rechargeMonitor/index.html");
	}

	 /**
	 * 保存
	 */
	@AuthRequire.Perms("product.recharge.qrcode.save")
	@Before({POST.class, Tx.class})
	public void save(){
    	
    	for (Enums.SysConfigType sysConfigType : Enums.SysConfigType.values()) {
    		String paraValue = getPara(sysConfigType.name());
    		if(StrKit.notBlank(paraValue)){
    			SysConfig sysConfig = SysConfig.dao.findByName(sysConfigType.name());
    			if(sysConfig == null) {
    				sysConfig = new SysConfig();
    				sysConfig.setId(IdUtil.simpleUUID()).setName(sysConfigType.name()).setValue(paraValue).setCreateTime(new java.util.Date()).setUpdateTime(new java.util.Date()).save();
    			} else if(!StrKit.equals(paraValue, sysConfig.getValue())){
    				sysConfig.setValue(paraValue).setUpdateTime(new java.util.Date()).update();
    			}
    		}
    	}
    	
    	//清除配置
    	JoyConfigUtil.clearCache(); //清除缓存
    	
		renderJson(RenderResult.success());
	}
	    
    /**
     * 上传二维码
     */
    @AuthRequire.Perms("product.recharge.qrcode.save")
    public void upload(){
        UploadFile uploadFile = getFile();
        String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile);

        UploadBean<String> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(headimgUrl);
        renderJson(bean);
    }
    
    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.recharge.qrcodeNoAmt.add")
    public void addNoAmt() {
    	String noAmtImg = JoyConfigUtil.getConfigValue(Enums.SysConfigType.WAP_WXPAY_QRCODE.name());
    	setAttr("noAmtImg", noAmtImg);
        renderTpl("product/rechargeQRCode/addNoAmt.html");
    }

    /**
     * 上传二维码
     */
    @AuthRequire.Perms("product.recharge.qrcodeNoAmt.add")
    public void uploadNoAmt(){
        UploadFile uploadFile = getFile();
        String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile);

        SysConfig tmpSysConfig = SysConfig.dao.findByModel(new SysConfig().setName(Enums.SysConfigType.WAP_WXPAY_QRCODE.name()));
        if(tmpSysConfig == null){
        	tmpSysConfig = new SysConfig();
        	tmpSysConfig.setId(JoyIdUtil.simpleUUID());
        	tmpSysConfig.setCreateTime(new java.util.Date());
        	tmpSysConfig.setName(Enums.SysConfigType.WAP_WXPAY_QRCODE.name());
        	tmpSysConfig.setValue(headimgUrl);
        	tmpSysConfig.setUpdateTime(new java.util.Date());
        	tmpSysConfig.save();
        }else{
        	tmpSysConfig.setValue(headimgUrl);
        	tmpSysConfig.setUpdateTime(new java.util.Date());
        	tmpSysConfig.update();
        }
        
        //清除缓存
        JoyDictUtil.clearCache();
        
        UploadBean<String> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(headimgUrl);
        
        renderJson(bean);
    }
    
    /**
     * web微信处理
     */
    public void webwechat(){
    	RenderResult<?> result = null;
    	switch (webWechatMonitorService.getWebWechatStatus()) {
		case WAIT:
			webWechatMonitorService.login();
			result = RenderResult.newRenderResult(WebWechatStatus.WAIT.getCode(), WebWechatStatus.WAIT.getText(), true, null);
			break;
		case LOADING_QRCODE:
			result = RenderResult.newRenderResult(WebWechatStatus.LOADING_QRCODE.getCode(), WebWechatStatus.LOADING_QRCODE.getText(), true, WechatManager.loginCodeImage);
			break;
		case RECEIVED_QRCODE:
			result = RenderResult.newRenderResult(WebWechatStatus.RECEIVED_QRCODE.getCode(), WebWechatStatus.RECEIVED_QRCODE.getText(), true, WechatManager.loginCodeImage);
			break;
		case SCANNED_QRCODE:
			result = RenderResult.newRenderResult(WebWechatStatus.SCANNED_QRCODE.getCode(), WebWechatStatus.SCANNED_QRCODE.getText(), true, WechatManager.loginCodeImage);
			break;
		case LOGIN_SUCCES:
			result = RenderResult.newRenderResult(WebWechatStatus.LOGIN_SUCCES.getCode(), WebWechatStatus.LOGIN_SUCCES.getText(), true, WechatManager.loginCodeImage);
			break;
		case LOGIN_FAIL:
			result = RenderResult.newRenderResult(WebWechatStatus.LOGIN_FAIL.getCode(), WebWechatStatus.LOGIN_FAIL.getText(), true, WechatManager.loginCodeImage);
			break;
		case LOGIN_DROPPED:
			result = RenderResult.newRenderResult(WebWechatStatus.LOGIN_DROPPED.getCode(), WebWechatStatus.LOGIN_DROPPED.getText(), true, WechatManager.loginCodeImage);
			break;
		default:
			break;
		}
    	renderJson(result);
    }
    
    
    
}
