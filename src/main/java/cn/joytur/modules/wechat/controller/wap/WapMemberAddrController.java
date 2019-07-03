package cn.joytur.modules.wechat.controller.wap;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;

import cn.hutool.core.util.IdUtil;
import cn.joytur.common.annotation.AuthRequire;
import cn.joytur.common.annotation.RouteMapping;
import cn.joytur.common.annotation.Valid;
import cn.joytur.common.annotation.Valids;
import cn.joytur.common.mvc.constant.CommonAttribute;
import cn.joytur.common.mvc.constant.DictAttribute;
import cn.joytur.common.mvc.constant.Enums.SortType;
import cn.joytur.common.mvc.constant.RenderResultCode;
import cn.joytur.common.mvc.controller.BaseWapController;
import cn.joytur.common.mvc.dto.RenderResult;
import cn.joytur.common.mvc.dto.Sort;
import cn.joytur.common.mvc.dto.UploadBean;
import cn.joytur.common.mvc.dto.WapMemberDTO;
import cn.joytur.common.utils.JoyDictUtil;
import cn.joytur.common.utils.JoyUploadFileUtil;
import cn.joytur.modules.wechat.entities.WechatMemberAddr;

/**
 * 地址管理
 * @author xuhang
 * @time 2019年6月27日 上午9:44:45
 */
@RouteMapping(url = "/wap/member/addr")
@AuthRequire.Logined
public class WapMemberAddrController extends BaseWapController {

	public void index() {
		
		//实际地址
		if(StrKit.equals(PropKit.get(CommonAttribute.SYSTEM_JOY_MODE), JoyDictUtil.getDictValue(DictAttribute.GOODS_JOY_TYPE1, DictAttribute.GOODS_JOY_TYPE, "1"))) {
			
			WapMemberDTO memberDTO = getWapMemberDTO();
			
			WechatMemberAddr query = new WechatMemberAddr();
			query.setWechatMemberId(memberDTO.getWechatMemberId());
			query.setDeleted(1L);
			List<WechatMemberAddr> wechatMemberAddrList = WechatMemberAddr.dao.findList(query, new Sort("status", SortType.DESC));
			
			setAttr("wechatMemberAddrList", wechatMemberAddrList);
			renderWap("wechat/address.html");
		}else {
			Long addrType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_JOY_TYPE2, DictAttribute.GOODS_JOY_TYPE, "2"));
			WechatMemberAddr mainWechatMemberAddr = WechatMemberAddr.dao.findByModel(new WechatMemberAddr().setWechatMemberId(getWapMemberDTO().getWechatMemberId()).setAddrType(addrType).setDeleted(1L));
			if(mainWechatMemberAddr != null) {
				setAttr("cashImg", mainWechatMemberAddr.getAddrArea());
			}
			setAttr("wechatMemberAddr",mainWechatMemberAddr);
			renderWap("wechat/photo_accout.html");
		}
		
	}
	
	/**
	 * 保存地址
	 */
	@Valids({
		@Valid(name = "realName", required = true, max=32),
		@Valid(name = "mobile", required = true, max=12),
		@Valid(name = "address", required = true, max=128)
	})
	public void save() {
		String id = getPara("id");
		
		Long addrType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_JOY_TYPE1, DictAttribute.GOODS_JOY_TYPE, "1"));
		
		WapMemberDTO memberDTO = getWapMemberDTO();
		int addrCount = WechatMemberAddr.dao.findCountByModel(new WechatMemberAddr().setWechatMemberId(memberDTO.getWechatMemberId()).setAddrType(addrType).setDeleted(1L));
		if(addrCount > 5) {
			renderJson(RenderResult.error(RenderResultCode.BUSINESS_265)); return;
		}
		String realName = getPara("realName");
		String mobile = getPara("mobile");
		String address = getPara("address");
		String[] addrDetail = address.split(",");
		
		if(StrKit.isBlank(id)) {
			WechatMemberAddr tmpWechatMemberAddr = new WechatMemberAddr();
			tmpWechatMemberAddr.setId(IdUtil.simpleUUID());
			tmpWechatMemberAddr.setWechatMemberId(memberDTO.getWechatMemberId());
			tmpWechatMemberAddr.setRealName(realName);
			tmpWechatMemberAddr.setMobile(mobile);
			tmpWechatMemberAddr.setAddrArea(addrDetail[0]);
			tmpWechatMemberAddr.setAddrDetail(addrDetail[1]);
			if(addrCount == 0) {
				tmpWechatMemberAddr.setStatus(2L);
			} else {
				tmpWechatMemberAddr.setStatus(1L);
			}
			tmpWechatMemberAddr.setDeleted(1L);
			tmpWechatMemberAddr.setUpdateTime(new java.util.Date());
			tmpWechatMemberAddr.setCreateTime(new java.util.Date());
			tmpWechatMemberAddr.save();
		} else {
			WechatMemberAddr tmpWechatMemberAddr = WechatMemberAddr.dao.findById(id);
			if(tmpWechatMemberAddr != null) {
				tmpWechatMemberAddr.setRealName(realName);
				tmpWechatMemberAddr.setMobile(mobile);
				tmpWechatMemberAddr.setAddrArea(addrDetail[0]);
				tmpWechatMemberAddr.setAddrDetail(addrDetail[1]);
				tmpWechatMemberAddr.setUpdateTime(new java.util.Date());
				tmpWechatMemberAddr.update();
			}
		}
		
		
		
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * 默认地址
	 */
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	@Before(Tx.class)
	public void defaultAddr(String id) {
		WapMemberDTO memberDTO = getWapMemberDTO();
		
		WechatMemberAddr updateWechatMemberAddr = new WechatMemberAddr();
		updateWechatMemberAddr.setStatus(1L);
		updateWechatMemberAddr.setAddrType(1L);
		updateWechatMemberAddr.setUpdateTime(new java.util.Date());
		WechatMemberAddr.dao.updateModelByModel(updateWechatMemberAddr, new WechatMemberAddr().setWechatMemberId(memberDTO.getWechatMemberId()));
		
		WechatMemberAddr updateDefWechatMemberAddr = new WechatMemberAddr();
		updateDefWechatMemberAddr.setStatus(2L);
		WechatMemberAddr.dao.updateModelById(updateDefWechatMemberAddr, id);
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * 删除地址
	 */
	@Valids({
		@Valid(name = "id", required = true, max=32 , min=32)
	})
	public void delete(String id) {
		WechatMemberAddr updateWechatMemberAddr = new WechatMemberAddr();
		updateWechatMemberAddr.setDeleted(0L);
		WechatMemberAddr.dao.updateModelById(updateWechatMemberAddr, id);
		
		WapMemberDTO memberDTO = getWapMemberDTO();
		int addrCount = WechatMemberAddr.dao.findCountByModel(new WechatMemberAddr().setWechatMemberId(memberDTO.getWechatMemberId()).setDeleted(1L));
		if(addrCount == 1) {
			WechatMemberAddr query = new WechatMemberAddr();
			query.setWechatMemberId(memberDTO.getWechatMemberId());
			query.setDeleted(1L);
			List<WechatMemberAddr> wechatMemberAddrList = WechatMemberAddr.dao.findList(query, new Sort("create_time,status", SortType.DESC));
			
			if(wechatMemberAddrList != null && wechatMemberAddrList.size() > 0) {
				WechatMemberAddr tmpWechatMemberAddr = wechatMemberAddrList.get(0);
				tmpWechatMemberAddr.setStatus(2L);
				tmpWechatMemberAddr.setUpdateTime(new java.util.Date());
				tmpWechatMemberAddr.update();
			}
		}
		
		renderJson(RenderResult.success());
	}
	
	/**
	 * 上传账号截图
	 */
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
        
        renderJson(RenderResult.success(fileNames.get(0)));
    }
	
	/**
	 * 确认提交
	 */
	public void confirm() {
		String realname = getPara("realname");
		String addname = getPara("addname");
		
		Long addrType = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.GOODS_JOY_TYPE2, DictAttribute.GOODS_JOY_TYPE, "2"));
		Long status = Long.valueOf(JoyDictUtil.getDictValue(DictAttribute.STATUS_ENABLE, DictAttribute.STATUS, "1"));
		 
		WechatMemberAddr mainWechatMemberAddr = WechatMemberAddr.dao.findByModel(new WechatMemberAddr().setWechatMemberId(getWapMemberDTO().getWechatMemberId()).setAddrType(addrType).setDeleted(1L));
		if(mainWechatMemberAddr != null){
			WechatMemberAddr updateWechatMemberAddr = new WechatMemberAddr();
			updateWechatMemberAddr.setRealName(realname);
			updateWechatMemberAddr.setAddrArea(addname);
			updateWechatMemberAddr.setUpdateTime(new java.util.Date());
			updateWechatMemberAddr.update();
			
			WechatMemberAddr.dao.updateModelById(updateWechatMemberAddr, mainWechatMemberAddr.getId());
		}else {
			mainWechatMemberAddr = new WechatMemberAddr();
			mainWechatMemberAddr.setId(IdUtil.fastSimpleUUID());
			mainWechatMemberAddr.setWechatMemberId(getWapMemberDTO().getWechatMemberId());
			mainWechatMemberAddr.setRealName(realname);
			mainWechatMemberAddr.setAddrArea(addname);
			mainWechatMemberAddr.setAddrType(addrType);
			mainWechatMemberAddr.setStatus(status);
			mainWechatMemberAddr.setCreateTime(new java.util.Date());
			mainWechatMemberAddr.setUpdateTime(new java.util.Date());
			mainWechatMemberAddr.save();
		}
		renderJson(RenderResult.success());
	}
    
}