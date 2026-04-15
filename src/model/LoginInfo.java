package model;

import java.io.Serializable;

public class LoginInfo implements Serializable {
	private String username;
	private String password;
	
	LoginInfo(String un, String pw){
		username = un;
		password = pw;
	}

}
