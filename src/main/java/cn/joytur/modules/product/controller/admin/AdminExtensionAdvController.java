package cn.joytur.modules.product.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.mvc.dto.UploadBean;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.product.entities.ExtensionAdv;

/**
 * 广告位图片管理
 * @author xuhang
 *
 */
@RouteMapping(url = "${admin}/extension/adv")
public class AdminExtensionAdvController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.extension.adv.view")
    public void index(ExtensionAdv extensionAdv) {
        Page<ExtensionAdv> pageList = ExtensionAdv.dao.paginate(getPage(), getSize(), extensionAdv, new Sort("create_time", Enums.SortType.ASC));
        setAttr("page", pageList);
        setAttr("extensionAdv", extensionAdv);
        renderTpl("product/extensionAdv/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.extension.adv.add")
    public void add() {
        renderTpl("product/extensionAdv/add.html");
    }
    
    /**
     * toEdit页面
     */
    @AuthRequire.Perms("product.extension.adv.edit")
    @Valids({
            @Valid(name = "id", required = true, max = 32, min = 32)
    })
    public void edit(String id) {
    	ExtensionAdv dbData = ExtensionAdv.dao.findById(id);

        setAttr("extensionAdv", dbData);
        renderTpl("product/extensionAdv/add.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.extension.adv.save")
    @Valids({
            @Valid(name = "extensionAdv.advType", required = true),
            @Valid(name = "extensionAdv.advImage", required = true)
    })
    @Before(POST.class)
    public void save(ExtensionAdv extensionAdv) {
    	ExtensionAdv dbData = new ExtensionAdv();
    	dbData.setAdvType(extensionAdv.getAdvType());
    	dbData = ExtensionAdv.dao.findByModel(dbData);
    	
        //新增
        if (StrUtil.isBlank(extensionAdv.getId())) {
            if (dbData != null) {
                throw new BusinessException(RenderResultCode.BUSINESS_357);
            }

            extensionAdv.setId(IdUtil.fastSimpleUUID());
            extensionAdv.setStatus(1L);
            extensionAdv.setCreateTime(DateTime.now());
            extensionAdv.setUpdateTime(DateTime.now());
            extensionAdv.save();
        } else {
            if (dbData != null && !dbData.getId().equals(extensionAdv.getId())) {
                throw new BusinessException(RenderResultCode.BUSINESS_358);
            }
            extensionAdv.setUpdateTime(DateTime.now());
            extensionAdv.update();
            
        }
        renderJson(RenderResult.success());
        
    }

    /**
     * 删除数据
     */
    @AuthRequire.Perms("product.extension.adv.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void delete() {
        String[] ids = getParaValues("ids");

        for (String id : ids) {
        	ExtensionAdv.dao.deleteById(id);
        }

        renderJson(RenderResult.success());
    }

    /**
     * 上传二维码
     */
    @AuthRequire.Perms("product.extension.adv.save")
    public void upload(){
        UploadFile uploadFile = getFile();
        
        String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile);

        UploadBean<String> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(headimgUrl);
        renderJson(bean);
    }
    
    /**
     * 启/禁用分类
     */
    @AuthRequire.Perms("product.extension.adv.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status() {
        Long status = getParaToLong();
        String[] ids = getParaValues("ids");
        for(String id : ids) {
        	ExtensionAdv extensionRule = ExtensionAdv.dao.findById(id);
            if(extensionRule == null) {
                throw new BusinessException(RenderResultCode.BUSINESS_358);
            }

            String statusValue = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.STATUS, "1");
            extensionRule.setStatus(Long.valueOf(JoyDictUtil.getDictValue(statusValue, DictAttribute.STATUS, "1")));
            extensionRule.setUpdateTime(new java.util.Date());
            extensionRule.update();
        }
        renderJson(RenderResult.success());
    }
    
    
}
