package cn.joytur.modules.product.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

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
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.product.entities.ExtensionRule;

/**
 * 活动规则
 * @author xuhang
 */
@RouteMapping(url = "${admin}/extension/rule")
public class AdminExtensionRuleController extends BaseAdminController {

	/**
     * 列表
     */
    @AuthRequire.Perms("product.extension.rule.view")
    public void index(ExtensionRule extensionRule) {
        Page<ExtensionRule> pageList = ExtensionRule.dao.paginate(getPage(), getSize(), extensionRule, new Sort("update_time", Enums.SortType.DESC));
        setAttr("page", pageList);
        setAttr("extensionRule", extensionRule);
        renderTpl("product/extensionRule/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.extension.rule.add")
    public void add() {
        renderTpl("product/extensionRule/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("product.extension.rule.edit")
    @Valids({
            @Valid(name = "id", required = true, max = 32, min = 32)
    })
    public void edit(String id) {
    	ExtensionRule dbData = ExtensionRule.dao.findById(id);

        setAttr("extensionRule", dbData);
        renderTpl("product/extensionRule/add.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.extension.rule.save")
    @Valids({
            @Valid(name = "extensionRule.extensionType", required = true, desc="活动类型", min = 1, max = 20),
            @Valid(name = "extensionRule.recAmount", desc="奖励游戏币", required = true, min = 1, max = 20)
    })
    @Before(POST.class)
    public void save(ExtensionRule extensionRule) {
    	ExtensionRule dbData = new ExtensionRule();
    	dbData.setExtensionType(extensionRule.getExtensionType());
    	dbData = ExtensionRule.dao.findByModel(dbData);
    	
        //新增
        if (StrUtil.isBlank(extensionRule.getId())) {
            if (dbData != null) {
                throw new BusinessException(RenderResultCode.BUSINESS_355);
            }

            extensionRule.setId(IdUtil.fastSimpleUUID());
            extensionRule.setCreateTime(DateTime.now());
            extensionRule.setUpdateTime(DateTime.now());
            extensionRule.save();
        } else {
            if (dbData != null && !dbData.getId().equals(extensionRule.getId())) {
                throw new BusinessException(RenderResultCode.BUSINESS_356);
            }
            extensionRule.setUpdateTime(DateTime.now());
            extensionRule.update();
            
        }
        renderJson(RenderResult.success());
    }

    /**
     * 删除数据
     */
    @AuthRequire.Perms("product.extension.rule.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void delete() {
        String[] ids = getParaValues("ids");

        for (String id : ids) {
        	ExtensionRule.dao.deleteById(id);
        }

        renderJson(RenderResult.success());
    }

    /**
     * 启/禁用分类
     */
    @AuthRequire.Perms("product.extension.rule.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status() {
        Long status = getParaToLong();
        String[] ids = getParaValues("ids");
        for(String id : ids) {
        	ExtensionRule extensionRule = ExtensionRule.dao.findById(id);
            if(extensionRule == null) {
                throw new BusinessException(RenderResultCode.BUSINESS_356);
            }

            String statusValue = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.STATUS, "1");
            extensionRule.setStatus(Long.valueOf(JoyDictUtil.getDictValue(statusValue, DictAttribute.STATUS, "1")));
            extensionRule.setUpdateTime(new java.util.Date());
            extensionRule.update();
        }
        renderJson(RenderResult.success());
    }
}
