package common;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.*;


@Entity
public class Skill implements Serializable {
	private static final long serialVersionUID = 2L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_skill;
	private String skill;
	@ManyToMany
	private Collection<Researcher> researchers;
	
	public Skill() {
		
	}
	
	public Skill(String skill, Collection<Researcher> researchers) {
		this.skill = skill;
		this.researchers = researchers;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}


	public Collection<Researcher> getResearchers() {
		return researchers;
	}

	public void setResearchers(Collection<Researcher> researchers) {
		this.researchers = researchers;
	}
	
}
