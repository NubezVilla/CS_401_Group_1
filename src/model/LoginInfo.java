package model;

import java.io.Serializable;

public class LoginInfo implements Serializable {
	private String username;
	private String password;
	
	public LoginInfo(String un, String pw){
		username = un;
		password = pw;
	}
	
	@Override
	public int hashCode() {
		return (username.hashCode() * password.hashCode());
	}
	
	@Override
	public boolean equals(Object other) {
		return (this.hashCode() == other.hashCode());
	}

}
