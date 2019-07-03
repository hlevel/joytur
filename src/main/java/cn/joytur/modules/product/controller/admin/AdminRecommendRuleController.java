package cn.joytur.modules.product.controller.admin;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;

import cn.hutool.core.util.StrUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.exception.BusinessException;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseAdminController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.modules.product.entities.RecommendRule;

/**
 * <p>
 *  代理规则
 * </p>
 * @since 2019/1/9
 */
@RouteMapping(url = "${admin}/recommend")
public class AdminRecommendRuleController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.recommend.view")
    public void index() {
        List<RecommendRule> list = RecommendRule.dao.findAll();

        setAttr("recommendRuleList", list);
        renderTpl("product/recommend/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.recommend.save")
    @Valids({
            @Valid(name = "recommendRule.recVal1", required = true),
            @Valid(name = "recommendRule.recVal2", required = true),
            @Valid(name = "recommendRule.recVal3", required = true),
            @Valid(name = "recommendRule.recAmount", required = true)
    })
    @Before(POST.class)
    public void save(RecommendRule recommendRule){
        //新增
        if(StrUtil.isBlank(recommendRule.getId())) {
            throw new BusinessException(RenderResultCode.BUSINESS_351);
        } else {
            recommendRule.update();
        }

        renderJson(RenderResult.success());
    }
}
