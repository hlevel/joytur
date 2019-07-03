package cn.joytur.modules.product.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

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
import cn.joytur.modules.order.service.AccountService;
import cn.joytur.modules.product.entities.GoodsGame;

/**
 * 商品游戏消费记录
 * @author xuhang
 *
 */
@RouteMapping(url = "${admin}/goods/game")
public class AdminGoodsGameController extends BaseAdminController {

    /**
     * 列表
     */
    @AuthRequire.Perms("product.goods.game.view")
    public void index(GoodsGame goodsGame) {
    	goodsGame.put("nickName", getPara("goodsGame.nickName"));
        Page<GoodsGame> pageList = GoodsGame.dao.paginate(getPage(), getSize(), goodsGame, new Sort("update_time", Enums.SortType.DESC));
        
        setAttr("page", pageList);
        setAttr("goodsGame", goodsGame);
        renderTpl("product/goodsgame/index.html");
    }

    /**
     * 游戏退款
     */
    @AuthRequire.Perms("product.goods.game.refund")
    @Valids({
            @Valid(name = "ids", required = true, min = 32)
    })
    @Before(Tx.class)
    public void refund() {
        String[] ids = getParaValues("ids");
        Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_GAME_STATUS4, DictAttribute.GOODS_GAME_STATUS, "4"));
        for(String id : ids) {
        	GoodsGame goodsGame = GoodsGame.dao.findById(id);
            if(goodsGame == null) {
                throw new BusinessException(RenderResultCode.BUSINESS_316);
            }
            
            if(goodsGame.getStatus() == status){
            	throw new BusinessException(RenderResultCode.BUSINESS_317);
            }
            
            AccountService accountService = Duang.duang(AccountService.class);
            accountService.refundGameCurrency(goodsGame.getWechatMemberId(), goodsGame.getExpAmt());
            
            goodsGame.setStatus(status);
            goodsGame.setUpdateTime(new java.util.Date());
            goodsGame.update();
        }
        renderJson(RenderResult.success());
    }


}
