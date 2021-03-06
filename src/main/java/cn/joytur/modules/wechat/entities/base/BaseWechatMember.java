package cn.joytur.modules.wechat.entities.base;

import com.jfinal.plugin.activerecord.IBean;

import cn.joytur.common.mvc.entities.DataModel;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseWechatMember<M extends BaseWechatMember<M>> extends DataModel<M> implements IBean {

	public M setId(java.lang.String id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public M setNickName(java.lang.String nickName) {
		set("nick_name", nickName);
		return (M)this;
	}
	
	public java.lang.String getNickName() {
		return getStr("nick_name");
	}

	public M setBirth(java.util.Date birth) {
		set("birth", birth);
		return (M)this;
	}
	
	public java.util.Date getBirth() {
		return get("birth");
	}

	public M setOpenid(java.lang.String openid) {
		set("openid", openid);
		return (M)this;
	}
	
	public java.lang.String getOpenid() {
		return getStr("openid");
	}
	
	public M setSubscribeId(java.lang.String subscribeId) {
		set("subscribe_id", subscribeId);
		return (M)this;
	}
	
	public java.lang.String getSubscribeId() {
		return getStr("subscribe_id");
	}

	public M setSubscribe(java.lang.Long subscribe) {
		set("subscribe", subscribe);
		return (M)this;
	}
	
	public java.lang.Long getSubscribe() {
		return getLong("subscribe");
	}

	public M setHeadimgUrl(java.lang.String headimgUrl) {
		set("headimg_url", headimgUrl);
		return (M)this;
	}
	
	public java.lang.String getHeadimgUrl() {
		return getStr("headimg_url");
	}

	public M setSex(java.lang.Long sex) {
		set("sex", sex);
		return (M)this;
	}
	
	public java.lang.Long getSex() {
		return getLong("sex");
	}

	public M setCountry(java.lang.String country) {
		set("country", country);
		return (M)this;
	}
	
	public java.lang.String getCountry() {
		return getStr("country");
	}

	public M setProvince(java.lang.String province) {
		set("province", province);
		return (M)this;
	}
	
	public java.lang.String getProvince() {
		return getStr("province");
	}

	public M setCity(java.lang.String city) {
		set("city", city);
		return (M)this;
	}
	
	public java.lang.String getCity() {
		return getStr("city");
	}

	public M setUnionid(java.lang.String unionid) {
		set("unionid", unionid);
		return (M)this;
	}
	
	public java.lang.String getUnionid() {
		return getStr("unionid");
	}

	public M setLanguage(java.lang.String language) {
		set("language", language);
		return (M)this;
	}
	
	public java.lang.String getLanguage() {
		return getStr("language");
	}
	
	public M setToken(java.lang.String token) {
		set("token", token);
		return (M)this;
	}
	
	public java.lang.String getToken() {
		return getStr("token");
	}
	
	public M setStatus(java.lang.Long status) {
		set("status", status);
		return (M)this;
	}
	
	public java.lang.Long getStatus() {
		return getLong("status");
	}

	public M setUpdateTime(java.util.Date updateTime) {
		set("update_time", updateTime);
		return (M)this;
	}
	
	public java.util.Date getUpdateTime() {
		return get("update_time");
	}

	public M setCreateTime(java.util.Date createTime) {
		set("create_time", createTime);
		return (M)this;
	}
	
	public java.util.Date getCreateTime() {
		return get("create_time");
	}

	public M setRemarks(java.lang.String remarks) {
		set("remarks", remarks);
		return (M)this;
	}
	
	public java.lang.String getRemarks() {
		return getStr("remarks");
	}

}
