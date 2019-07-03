package cn.joytur.modules.product.controller.admin;

import java.util.ArrayList;
import java.util.List;

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
import cn.joytur.modules.product.entities.Goods;
import cn.joytur.modules.product.entities.GoodsCategory;
import cn.joytur.modules.product.entities.GoodsRule;

/**
 * <p>
 *  产品管理
 * </p>
 * @since 2019/1/9
 */
@RouteMapping(url = "${admin}/goods")
public class AdminGoodsController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.goods.view")
    public void index(Goods goods) {
        Page<Goods> pageList = Goods.dao.paginate(getPage(), getSize(), goods, new Sort("update_time", Enums.SortType.DESC));
        
        setAttr("page", pageList);
        setAttr("goods", goods);
        renderTpl("product/goods/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.goods.add")
    public void add() {
    	setAttr("goodsRuleList", GoodsRule.dao.findList(new GoodsRule().setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1")))));
        setAttr("categoryList", GoodsCategory.dao.findList(new GoodsCategory().setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1")))));
        
        renderTpl("product/goods/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("product.goods.edit")
    @Valids({
            @Valid(name = "id", required = true, max = 32, min = 32)
    })
    public void edit(String id) {
        Goods dbData = Goods.dao.findById(id);

        setAttr("goodsRuleList", GoodsRule.dao.findList(new GoodsRule().setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1")))));
        setAttr("categoryList", GoodsCategory.dao.findList(new GoodsCategory().setStatus(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1")))));
        setAttr("goods", dbData);
        renderTpl("product/goods/add.html");
    }

    /**
     * 保存页面
     */
    @AuthRequire.Perms("product.goods.save")
    @Valids({
            @Valid(name = "goods.categoryId", desc = "商品分类", required = true, min = 1, max = 32),
            @Valid(name = "goods.goodsName", desc = "商品名称", required = true, min = 1, max = 32),
            @Valid(name = "goods.goodsImage", desc = "商品图片", required = true, min = 1, max = 64),
            @Valid(name = "goods.recommend", desc = "规则描述", required = true, min = 1, max = 32),
            @Valid(name = "goods.costPrice", desc = "成本价", required = true, min = 1, max = 20),
            @Valid(name = "goods.mktPrice", desc = "市场价", required = true, min = 1, max = 20),
            @Valid(name = "goods.scorePrice", desc = "消耗积分", required = true, min = 1, max = 20),
            @Valid(name = "goods.goodsRuleId", desc = "闯关规则", required = true, min = 1, max = 32)
    })
    @Before(POST.class)
    public void save(Goods goods) {
        //新增
        if (StrUtil.isBlank(goods.getId())) {
        	goods.setId(IdUtil.simpleUUID());
        	goods.setUpdateTime(new java.util.Date());
        	goods.setCreateTime(new java.util.Date());
            goods.save();
        } else {
            goods.setUpdateTime(DateTime.now());
            goods.update();
        }
        renderJson(RenderResult.success());
    }

    /**
     * 删除数据
     */
    @AuthRequire.Perms("product.goods.delete")
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
    @AuthRequire.Perms("product.goods.status")
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

    public void upload(){
        List<String> fileNames = new ArrayList<>();
        List<UploadFile> files = getFiles();
        files.forEach(uploadFile -> {
            String headimgUrl = JoyUploadFileUtil.uploadAdapter(uploadFile);
            fileNames.add(headimgUrl);
        });

        UploadBean<List<String>> bean = new UploadBean<>();
        bean.setErrno(0);
        bean.setData(fileNames);
        renderJson(bean);
    }

}
