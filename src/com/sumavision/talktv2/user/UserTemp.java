package com.sumavision.talktv2.user;

/**
 * 
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 账户管理用户信息实体类
 * @changeLog
 * 
 */
public class UserTemp {

	private static UserTemp instance;
	private int userId;
	private String userName;
	private String password;
	
	public UserTemp(String name, String password, String id){
		this.userName = name;
		this.password = password;
		this.userId = Integer.parseInt(id);
	}
	
	public UserTemp(){

	}
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public static UserTemp current(){
		if(instance == null){
			instance = new UserTemp();
		}
		return instance;
	}
}
