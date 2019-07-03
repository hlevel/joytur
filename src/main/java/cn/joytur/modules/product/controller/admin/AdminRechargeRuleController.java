package cn.joytur.modules.product.controller.admin;

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
import cn.joytur.modules.product.entities.RechargeRule;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * <p>
 * 充值规则
 * </p>
 *
 * @since 2019/1/9
 */
@RouteMapping(url = "${admin}/recharge")
public class AdminRechargeRuleController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.recharge.view")
    public void index(RechargeRule rechargeRule) {
        Page<RechargeRule> pageList = RechargeRule.dao.paginate(getPage(), getSize(), rechargeRule, new Sort("update_time", Enums.SortType.ASC));
        setAttr("page", pageList);
        setAttr("rechargeRule", rechargeRule);
        renderTpl("product/recharge/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.recharge.add")
    public void add() {
        renderTpl("product/recharge/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("product.recharge.edit")
    @Valids({
            @Valid(name = "id", required = true, max = 32, min = 32)
    })
    public void edit(String id) {
        RechargeRule dbData = RechargeRule.dao.findById(id);

        setAttr("rechargeRule", dbData);
        renderTpl("product/recharge/add.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.recharge.save")
    @Valids({
            @Valid(name = "rechargeRule.transAmt", required = true),
            @Valid(name = "rechargeRule.transAfterAmt", required = true),
            @Valid(name = "rechargeRule.transDayLimit", required = true),
            @Valid(name = "rechargeRule.description", min = 1, max = 20)
    })
    @Before(POST.class)
    public void save(RechargeRule rechargeRule) {
        //新增
        if (StrUtil.isBlank(rechargeRule.getId())) {
            rechargeRule.setId(IdUtil.fastSimpleUUID());
            rechargeRule.setCreateTime(DateTime.now());
            rechargeRule.setUpdateTime(DateTime.now());
            rechargeRule.save();
        } else {
            rechargeRule.setUpdateTime(DateTime.now());
            rechargeRule.update();
        }

        renderJson(RenderResult.success());
    }

    /**
     * 修改状态
     */
    @AuthRequire.Perms("product.recharge.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status() {
        Long status = getParaToLong();
        String[] ids = getParaValues("ids");
        for (String id : ids) {
            RechargeRule dbData = RechargeRule.dao.findById(id);
            if (dbData == null) {
                throw new BusinessException(RenderResultCode.BUSINESS_351);
            }

            String statusValue = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.STATUS, "1");
            dbData.setStatus(Long.valueOf(JoyDictUtil.getDictValue(statusValue, DictAttribute.STATUS, "1")));
            dbData.setUpdateTime(DateTime.now());
            dbData.update();
        }
        renderJson(RenderResult.success());
    }

    /**
     * 删除数据
     */
    @AuthRequire.Perms("product.recharge.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void delete() {
        String[] ids = getParaValues("ids");

        for (String id : ids) {
            RechargeRule.dao.deleteById(id);
        }

        renderJson(RenderResult.success());
    }

}
