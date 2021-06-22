package jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserEntity implements Serializable{
	private static final long serialVersionUID = 1236L;
	@Id
	private String username;
	private String password;
	private boolean isActive;
	private boolean pendingRequest; 
	private boolean firstTimeLogin;
	
	public UserEntity() {
	}
	
	public UserEntity(String name, String password) {
		this.username = name;
		this.password = password;
		this.isActive = true;
		this.pendingRequest = true;
		this.firstTimeLogin = true;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isPendingRequest() {
		return pendingRequest;
	}

	public void setPendingRequest(boolean pendingRequest) {
		this.pendingRequest = pendingRequest;
	}

	public boolean isFirstTimeLogin() {
		return firstTimeLogin;
	}

	public void setFirstTimeLogin(boolean firstTimeLogin) {
		this.firstTimeLogin = firstTimeLogin;
	}
	
}
