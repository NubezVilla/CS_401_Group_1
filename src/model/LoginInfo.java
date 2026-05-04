package model;

import java.io.Serializable;
import java.util.Objects;

public class LoginInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    public LoginInfo(String un, String pw) {
        username = un;
        password = pw;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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
        return Objects.equals(username, other.username)
                && Objects.equals(password, other.password);
    }
}