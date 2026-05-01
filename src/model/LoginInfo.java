package model;

import java.io.Serializable;
import java.util.Objects;

public class LoginInfo implements Serializable {
	private String username;
	private String password;
	
	public LoginInfo(String un, String pw){
		username = un;
		password = pw;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(username, password);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
	    if (!(o instanceof LoginInfo)) return false;
	    LoginInfo other = (LoginInfo) o;
	    return this.hashCode() == other.hashCode();
	}

}
