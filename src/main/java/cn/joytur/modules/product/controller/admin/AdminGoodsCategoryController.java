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
import cn.joytur.modules.product.entities.GoodsCategory;

/**
 * <p>
 *  产品分类管理
 * </p>
 * @since 2019/1/9
 */
@RouteMapping(url = "${admin}/goods/category")
public class AdminGoodsCategoryController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.goodscategory.view")
    public void index(GoodsCategory category) {
        Page<GoodsCategory> pageList = GoodsCategory.dao.paginate(getPage(), getSize(), category, new Sort("update_time", Enums.SortType.DESC));
        setAttr("page", pageList);
        setAttr("category", category);
        renderTpl("product/goodscategory/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.goodscategory.add")
    public void add() {
        renderTpl("product/goodscategory/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("product.goodscategory.edit")
    @Valids({
            @Valid(name = "id", required = true, max = 32, min = 32)
    })
    public void edit(String id) {
        GoodsCategory dbData = GoodsCategory.dao.findById(id);

        setAttr("category", dbData);
        renderTpl("product/goodscategory/add.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.goodscategory.save")
    @Valids({
            @Valid(name = "category.categoryName", required = true, min = 1, max = 20),
            @Valid(name = "category.description", min = 1, max = 20)
    })
    @Before(POST.class)
    public void save(GoodsCategory category) {
        GoodsCategory dbData = new GoodsCategory();
        dbData.setCategoryName(category.getCategoryName());
        dbData = GoodsCategory.dao.findByModel(dbData);
        //新增
        if (StrUtil.isBlank(category.getId())) {
            if (dbData != null) {
                throw new BusinessException(RenderResultCode.BUSINESS_360);
            }

            category.setId(IdUtil.fastSimpleUUID());
            category.setCreateTime(DateTime.now());
            category.setUpdateTime(DateTime.now());
            category.save();
        } else {
            if (dbData != null && !dbData.getId().equals(category.getId())) {
                throw new BusinessException(RenderResultCode.BUSINESS_360);
            }
            category.setUpdateTime(DateTime.now());
            category.update();
        }
        renderJson(RenderResult.success());
    }

    /**
     * 删除数据
     */
    @AuthRequire.Perms("product.goodscategory.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void delete() {
        String[] ids = getParaValues("ids");

        for (String id : ids) {
            GoodsCategory.dao.deleteById(id);
        }

        renderJson(RenderResult.success());
    }

    /**
     * 启/禁用分类
     */
    @AuthRequire.Perms("product.goodscategory.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status() {
        Long status = getParaToLong();
        String[] ids = getParaValues("ids");
        for(String id : ids) {
            GoodsCategory category = GoodsCategory.dao.findById(id);
            if(category == null) {
                throw new BusinessException(RenderResultCode.BUSINESS_361);
            }

            String statusValue = JoyDictUtil.getDictLabel(String.valueOf(status), DictAttribute.STATUS, "1");
            category.setStatus(Long.valueOf(JoyDictUtil.getDictValue(statusValue, DictAttribute.STATUS, "1")));
            category.setUpdateTime(new java.util.Date());
            category.update();
        }
        renderJson(RenderResult.success());
    }
}
