package jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AdminLog implements Serializable{

	private static final long serialVersionUID = 196852L;
	
	@Id
	private int id;
	private boolean adminOn;

	public AdminLog() {
		this.adminOn = true;
		this.id = 1;
	}

	public boolean isAdminOn() {
		return adminOn;
	}

	public void setAdminOn(boolean adminOn) {
		this.adminOn = adminOn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
		
}
