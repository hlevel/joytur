package cn.joytur.modules.product.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
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
import cn.joytur.modules.product.entities.GoodsRule;
import cn.joytur.modules.system.entities.SysDictionary;

/**
 * <p>
 * 闯关规则
 * </p>
 *
 */
@RouteMapping(url = "${admin}/goods/rule")
public class AdminGoodsRuleController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.goodsRule.view")
    public void index(GoodsRule goodsRule) {
        Page<GoodsRule> pageList = GoodsRule.dao.paginate(getPage(), getSize(), goodsRule, new Sort("create_time", Enums.SortType.ASC));
        
        List<SysDictionary> dictList = JoyDictUtil.getDictList(DictAttribute.GOODS_RULE_LEVEL_TYPE);
        
        for(int i=0; pageList.getList() != null && i< pageList.getList().size(); i++) {
        	JSONObject diffLevelJSON = JSONObject.parseObject(pageList.getList().get(i).getDiffValue());
        	
        	StringBuffer diffBuff = new StringBuffer();
        	for(int j=0; j < dictList.size(); j ++) {
        		JSONObject diffJSON = diffLevelJSON.getJSONObject(dictList.get(j).getDictValue());
        		diffBuff.append("[");
        		diffBuff.append(JoyDictUtil.getDictLabel(dictList.get(j).getDictValue(), DictAttribute.GOODS_RULE_LEVEL_TYPE, ""));
        		diffBuff.append("-").append(diffJSON.getString("quant"));
        		diffBuff.append("-").append(JoyDictUtil.getDictLabel(diffJSON.getString("diff"), DictAttribute.GOODS_RULE_DIFF_TYPE, ""));
        		diffBuff.append("-").append(diffJSON.getString("second") + "s");
        		diffBuff.append("]&nbsp;");
        		
        		pageList.getList().get(i).setDiffValue(diffBuff.toString());
        	}
        	
        }
        
        setAttr("page", pageList);
        setAttr("goodsRule", goodsRule);
        renderTpl("product/goodsrule/index.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.goodsRule.add")
    public void add() {
        renderTpl("product/goodsrule/add.html");
    }

    /**
     * toEdit页面
     */
    @AuthRequire.Perms("product.goodsRule.edit")
    @Valids({
            @Valid(name = "id", required = true, max = 32, min = 32)
    })
    public void edit(String id) {
    	GoodsRule dbData = GoodsRule.dao.findById(id);
    	
    	Map<String, Map<String, String>> diffMap = new HashMap<String, Map<String, String>>();
    	
    	JSONObject diffLevelJSON = JSONObject.parseObject(dbData.getDiffValue());
    	
    	for(SysDictionary dict : JoyDictUtil.getDictList(DictAttribute.GOODS_RULE_LEVEL_TYPE)) {
    		JSONObject diffJSON = diffLevelJSON.getJSONObject(dict.getDictValue());
    		
    		Map<String, String> tmp = new HashMap<String, String>();
    		tmp.put("quant", diffJSON.getString("quant"));
    		tmp.put("diff", diffJSON.getString("diff"));
    		tmp.put("second", diffJSON.getString("second"));
    		
    		diffMap.put("level_" + dict.getDictValue(), tmp);
    	}

    	setAttr("diffMap", diffMap);
        setAttr("goodsRule", dbData);
        renderTpl("product/goodsrule/add.html");
    }

    /**
     * toAdd页面
     */
    @AuthRequire.Perms("product.goodsRule.save")
    @Valids({
            @Valid(name = "goodsRule.ruleName", required = true),
            @Valid(name = "goodsRule.status", required = true)
    })
    @Before(POST.class)
    public void save(GoodsRule goodsRule) {
    	
    	JSONObject diffLevelJSON = new JSONObject();
    	for (SysDictionary dict : JoyDictUtil.getDictList(DictAttribute.GOODS_RULE_LEVEL_TYPE)) {
    		String quant = getPara("quant_" + dict.getDictValue());
    		String diff = getPara("diff_" + dict.getDictValue());
    		String second = getPara("second_" + dict.getDictValue());
			if(StrKit.isBlank(quant)) {
				throw new BusinessException(RenderResultCode.BUSINESS_370);
			}
			if(StrKit.isBlank(diff)) {
				throw new BusinessException(RenderResultCode.BUSINESS_371);
			}
			
			JSONObject diffJSON = new JSONObject();
			diffJSON.put("quant", quant);
			diffJSON.put("diff", diff);
			diffJSON.put("second", second);
			
			diffLevelJSON.put(dict.getDictValue(), diffJSON);
		}
    	
    	goodsRule.setDiffValue(diffLevelJSON.toJSONString());
    	
        //新增
        if (StrUtil.isBlank(goodsRule.getId())) {
        	GoodsRule existsGoodsRule = GoodsRule.dao.findByModel(new GoodsRule().setRuleName(goodsRule.getRuleName()));
        	if(existsGoodsRule != null) {
        		throw new BusinessException(RenderResultCode.BUSINESS_373);
        	}
        	
        	goodsRule.setId(IdUtil.fastSimpleUUID());
        	goodsRule.setCreateTime(DateTime.now());
        	goodsRule.setUpdateTime(DateTime.now());
        	goodsRule.save();
        } else {
        	goodsRule.setUpdateTime(DateTime.now());
        	goodsRule.update();
        }

        renderJson(RenderResult.success());
    }

    /**
     * 修改状态
     */
    @AuthRequire.Perms("product.goodsRule.status")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void status() {
        Long status = getParaToLong();
        String[] ids = getParaValues("ids");
        for (String id : ids) {
        	GoodsRule dbData = GoodsRule.dao.findById(id);
            if (dbData == null) {
                throw new BusinessException(RenderResultCode.BUSINESS_372);
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
    @AuthRequire.Perms("product.goodsRule.delete")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    public void delete() {
        String[] ids = getParaValues("ids");

        for (String id : ids) {
        	GoodsRule.dao.deleteById(id);
        }

        renderJson(RenderResult.success());
    }

}
