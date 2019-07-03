package cn.joytur.common.mvc.dto;

/**
 * 实体对象
 * @author xuhang
 *
 */
@SuppressWarnings("serial")
public class WapMemberDTO implements java.io.Serializable{

	private String wechatMemberId;	//用户id
	private String openid;		//用户openid
	private String nickname;	//用户昵称
	private String headimgurl;	//用户用户头像
	private String version;		//版本号用于自动清除cookie
	
	public String getWechatMemberId() {
		return wechatMemberId;
	}
	public void setWechatMemberId(String wechatMemberId) {
		this.wechatMemberId = wechatMemberId;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
