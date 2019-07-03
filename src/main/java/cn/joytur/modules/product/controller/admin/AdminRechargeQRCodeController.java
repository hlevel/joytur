package cn.joytur.modules.product.controller.admin;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.mvc.dto.UploadBean;
import cn.joytur.common.utils.JoyConfigUtil;
import cn.joytur.common.utils.JoyIdUtil;
import cn.joytur.common.utils.JoyQrCodeUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.product.entities.RechargeRule;
import cn.joytur.modules.product.entities.RechargeRuleQrCode;
import cn.joytur.modules.system.entities.SysConfig;
/**
 * <p>
 * 充值二维码管理
 * </p>
 *
 * @since 2019/1/9
 */
@RouteMapping(url = "${admin}/recharge/qrcode")
public class AdminRechargeQRCodeController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.recharge.qrcode.view")
    public void index(RechargeRuleQrCode qrCode) {
        Page<RechargeRuleQrCode> pageList = RechargeRuleQrCode.dao.paginate(getPage(), getSize(), qrCode, new Sort("update_time", Enums.SortType.ASC));
        setAttr("page", pageList);
        setAttr("qrCode", qrCode);
        renderTpl("product/rechargeQRCode/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.recharge.qrcode.add")
    public void add() {
        List<RechargeRule> rechargeRule = RechargeRule.dao.findAll(new Sort("trans_amt", Enums.SortType.ASC));

        List<Map<String, String>> rules = Lists.newArrayList();
        rechargeRule.forEach(rule -> {
            StringBuffer sb = new StringBuffer("充值:");
            sb.append(rule.getTransAmt()).append(" 元");
            sb.append(" / 到账:");
            sb.append(rule.getTransAfterAmt()).append("币");

            Map<String, String> ruleMap = Maps.newHashMap();
            ruleMap.put("key", rule.getId());
            ruleMap.put("value", sb.toString());
            rules.add(ruleMap);
        });

        setAttr("rules", rules);
        renderTpl("product/rechargeQRCode/add.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.recharge.qrcode.save")
    @Valids({
            @Valid(name = "qrCode.rechargeRuleId", required = true, max = 32, min = 32),
            @Valid(name = "qrCode.qrcodeImage", required = true),
            @Valid(name = "qrCode.transAmt", required = true)
    })
    @Before(POST.class)
    public void save(RechargeRuleQrCode qrCode) {
        RechargeRule rule = RechargeRule.dao.findById(qrCode.getRechargeRuleId());
        if (rule == null) {
            throw new BusinessException(RenderResultCode.BUSINESS_301);
        }
        qrCode.setId(IdUtil.fastSimpleUUID());
        try{
        	qrCode.setQrcodeUrl(JoyQrCodeUtil.decQrcodeURL(JoyUploadFileUtil.getHttpPath(qrCode.getQrcodeImage())));
        }catch(Exception e){
        	LOGGER.error(e.getMessage(), e);
        	throw new BusinessException(RenderResultCode.BUSINESS_312);
        }
        qrCode.setStatus(1L);
        qrCode.setCreateTime(DateTime.now());
        qrCode.setUpdateTime(DateTime.now());
        qrCode.save();
        renderJson(RenderResult.success());
        
    }

    /**
     * 删除数据
     */
    @AuthRequire.Perms("product.recharge.qrcode.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void delete() {
        String[] ids = getParaValues("ids");

        for (String id : ids) {
            RechargeRuleQrCode.dao.deleteById(id);
        }

        renderJson(RenderResult.success());
    }

    /**
     * 上传二维码
     */
    @AuthRequire.Perms("product.recharge.qrcode.save")
    public void upload(){
        UploadFile uploadFile = getFile();
        
        //再次转换图片只保留二维码内容
        String qrCodeContent = JoyQrCodeUtil.decQrcode(uploadFile.getFile().getAbsolutePath());
        File qrCodeFile = JoyQrCodeUtil.enQrCodeToImageFilePath(qrCodeContent, uploadFile.getUploadPath());
        UploadFile uploadFile2 = new UploadFile(uploadFile.getParameterName(), uploadFile.getUploadPath(), qrCodeFile.getName(), qrCodeFile.getName(), uploadFile.getContentType());
        
        String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile2);

        UploadBean<String[]> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(new String[] {JoyUploadFileUtil.getHttpPath(headimgUrl), headimgUrl});
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
        
        //再次转换
        String qrCodeContent = JoyQrCodeUtil.decQrcode(uploadFile.getFile().getAbsolutePath());
        File qrCodeFile = JoyQrCodeUtil.enQrCodeToImageFilePath(qrCodeContent, uploadFile.getUploadPath());
        UploadFile uploadFile2 = new UploadFile(uploadFile.getParameterName(), uploadFile.getUploadPath(), qrCodeFile.getName(), qrCodeFile.getName(), uploadFile.getContentType());
        
        String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile2);

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
        JoyConfigUtil.clearCache();
        
        UploadBean<String> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(JoyUploadFileUtil.getHttpPath(headimgUrl));
        
        renderJson(bean);
    }
    
}
