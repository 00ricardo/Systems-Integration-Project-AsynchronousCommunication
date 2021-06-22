package common;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;


@Entity
public class Researcher implements Serializable{
	private static final long serialVersionUID = 1L;
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id_researcher;
		private String firstName;
		private String lastName;
		private String description;
		@ManyToMany(mappedBy = "researchers")
		private Collection<Skill> skills;
		@ManyToMany(mappedBy = "researchers")
		private Collection<Publication> publications;
		@ManyToMany(mappedBy = "researchers")
		private Collection<Institution> institutions;
		
		
		public Researcher() {
		}
						
		public Researcher(String firstName, String lastName, String description) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.description = description;
		}

		public Long getId() {
			return id_researcher;
		}

		public void setId(Long id) {
			this.id_researcher = id;
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

		public Collection<Skill> getSkills() {
			return skills;
		}

		public void setSkills(Collection<Skill> skills) {
			this.skills = skills;
		}

		public Collection<Publication> getPublications() {
			return publications;
		}

		public void setPublications(Collection<Publication> publications) {
			this.publications = publications;
		}

		public Collection<Institution> getInstitutions() {
			return institutions;
		}

		public void setInstitutions(Collection<Institution> institutions) {
			this.institutions = institutions;
		}
				
}
