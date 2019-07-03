package cn.joytur.common.mvc.dto;

import java.util.List;

/**
 * 实体对象
 * @author xuhang
 *
 */
@SuppressWarnings("serial")
public class AdminDTO implements java.io.Serializable{

	private String userid;		//用户id
	private String username;	//用户名
	private String nickname;	//用户昵称
	private List<String> permissionList;	//权限集合
	private String version;		//版本号用于自动清除cookie
	
	public String getUserid() {
		return userid;
	}
	public String getUsername() {
		return username;
	}
	public String getNickname() {
		return nickname;
	}
	public List<String> getPermissionList() {
		return permissionList;
	}
	public String getVersion() {
		return version;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public void setPermissionList(List<String> permissionList) {
		this.permissionList = permissionList;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
