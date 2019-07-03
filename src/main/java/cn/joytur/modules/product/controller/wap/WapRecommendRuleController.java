package cn.joytur.modules.product.controller.wap;

import com.jfinal.kit.StrKit;

import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.modules.product.entities.RecommendRule;
import cn.joytur.modules.wechat.entities.WechatMemberProfit;

/**
 * 代理规则
 * @author xuhang
 *
 */
@RouteMapping(url = "/wap/recommend/rule")
public class WapRecommendRuleController extends BaseWapController {
	
	
	
	public void index(){
		WapMemberDTO memberDTO = getWapMemberDTO();
		
		RecommendRule mediumRecommendRule = RecommendRule.dao.findByModel(new RecommendRule().setRecType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_LEVEL1, DictAttribute.RECOMMEND_LEVEL, "1"))));
		RecommendRule seniorRecommendRule = RecommendRule.dao.findByModel(new RecommendRule().setRecType(Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.RECOMMEND_LEVEL2, DictAttribute.RECOMMEND_LEVEL, "2"))));
		
		WechatMemberProfit tmpWechatMemberProfit = WechatMemberProfit.dao.findByModel(new WechatMemberProfit().setWechatMemberId(memberDTO.getWechatMemberId()));
		
		String vipName = "";
		String vipId = "";
		if(tmpWechatMemberProfit != null) {
			if(StrKit.equals(tmpWechatMemberProfit.getRecommendRuleId(), mediumRecommendRule.getId())) {
				vipName = JoyDictUtil.getDictLabel(String.valueOf(mediumRecommendRule.getRecType()), DictAttribute.RECOMMEND_LEVEL, "1");
				vipId = mediumRecommendRule.getId();
			}else if(StrKit.equals(tmpWechatMemberProfit.getRecommendRuleId(), seniorRecommendRule.getId())) {
				vipName = JoyDictUtil.getDictLabel(String.valueOf(seniorRecommendRule.getRecType()), DictAttribute.RECOMMEND_LEVEL, "1");
				vipId = seniorRecommendRule.getId();
			}
		}
		
		setAttr("vipName", vipName).setAttr("vipId", vipId);
		setAttr("memberProfit", tmpWechatMemberProfit);
		setAttr("mediumRecommendRule", mediumRecommendRule).setAttr("mediumName", DictAttribute.RECOMMEND_LEVEL1);
		setAttr("seniorRecommendRule", seniorRecommendRule).setAttr("seniorName", DictAttribute.RECOMMEND_LEVEL2);
		renderWap("product/recommend.html");
	}
	
	
}
