package jpa;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class ResearcherRequest {
	private static final long serialVersionUID = 111L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_researcher;
	private String firstName;
	private String lastName;
	private String description;
	@ManyToMany(mappedBy = "researchers")
	private Collection<PublicationRequest> publications;
	
	public ResearcherRequest() {
	}
					
	public ResearcherRequest(String firstName, String lastName, String description) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.description = description;
	}

	public Long getId_researcher() {
		return id_researcher;
	}

	public void setId_researcher(Long id_researcher) {
		this.id_researcher = id_researcher;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<PublicationRequest> getPublications() {
		return publications;
	}

	public void setPublications(Collection<PublicationRequest> publications) {
		this.publications = publications;
	}
	
}
