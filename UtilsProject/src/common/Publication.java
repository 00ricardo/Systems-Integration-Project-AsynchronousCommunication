package common;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.*;

@Entity
public class Publication implements Serializable{
	private static final long serialVersionUID = 3L;
	@Id
	private String id_publication;
	private String publication_Date;
	private String publication_title;
	private int citations;
	private int recommendations;
	private int reads;
	@ManyToMany
	private Collection<Researcher> researchers;
	
	public Publication() {
		
	}
	
	public Publication(String id_publication, String publication_Date, String publication_title, int citations,
			int recommendations, int reads, Collection<Researcher> researchers) {
		this.id_publication = id_publication;
		this.publication_Date = publication_Date;
		this.publication_title = publication_title;
		this.citations = citations;
		this.recommendations = recommendations;
		this.reads = reads;
		this.researchers = researchers;
	}

	public String getId_publication() {
		return id_publication;
	}
	
	public void setId_publication(String id_publication) {
		this.id_publication = id_publication;
	}
	
	public String getPublication_Date() {
		return publication_Date;
	}
	
	public void setPublication_Date(String publication_Date) {
		this.publication_Date = publication_Date;
	}
	
	public String getPublication_title() {
		return publication_title;
	}

	public void setPublication_title(String publication_title) {
		this.publication_title = publication_title;
	}

	public int getCitations() {
		return citations;
	}
	
	public void setCitations(int citations) {
		this.citations = citations;
	}
	
	public int getRecommendations() {
		return recommendations;
	}
	
	public void setRecommendations(int recommendations) {
		this.recommendations = recommendations;
	}
	
	public int getReads() {
		return reads;
	}
	
	public void setReads(int reads) {
		this.reads = reads;
	}
	public Collection<Researcher> getResearchers() {
		return researchers;
	}
	
	public void setResearchers(Collection<Researcher> researchers) {
		this.researchers = researchers;
	}
	
	
}
