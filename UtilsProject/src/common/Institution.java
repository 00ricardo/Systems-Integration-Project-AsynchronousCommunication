package common;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.*;

@Entity
public class Institution implements Serializable{
	private static final long serialVersionUID = 4L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_institution;
	private String institution_name;
	private String location;
	private String department;
	@ManyToMany
	private Collection<Researcher> researchers;
	
	public Institution() {
		
	}

	public Institution(String institution_name, String location, String department, Collection<Researcher> researchers) {
		this.institution_name = institution_name;
		this.location = location;
		this.department = department;
		this.researchers = researchers;
	}
	
	public Institution(String institution_name, String location, Collection<Researcher> researchers) {
		this.institution_name = institution_name;
		this.location = location;
		this.researchers = researchers;
	}

	public Long getId_institution() {
		return id_institution;
	}

	public void setId_institution(Long id_institution) {
		this.id_institution = id_institution;
	}

	public String getInstitution_name() {
		return institution_name;
	}

	public void setInstitution_name(String institution_name) {
		this.institution_name = institution_name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Collection<Researcher> getResearchers() {
		return researchers;
	}

	public void setResearchers(Collection<Researcher> researchers) {
		this.researchers = researchers;
	}

	
}
