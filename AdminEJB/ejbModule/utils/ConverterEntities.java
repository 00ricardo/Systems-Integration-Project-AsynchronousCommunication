package utils;

import java.util.ArrayList;

import common.Publication;
import common.Researcher;
import jpa.PublicationRequest;
import jpa.ResearcherRequest;

public class ConverterEntities {

	/**
	 *  Convert a Publication object into a PublicationRequest object
	 * @param pub
	 * @return
	 */
	public static PublicationRequest convertPublicationToPublicationRequest(Publication pub) {
		PublicationRequest publication =  new PublicationRequest(pub.getId_publication(), pub.getPublication_Date(), pub.getPublication_title(),
				pub.getCitations(), pub.getRecommendations(), pub.getReads());
		ArrayList<ResearcherRequest> researchres = new ArrayList<>();
		for (Researcher r : pub.getResearchers()) 
			researchres.add(ConverterEntities.convertResearcherToResearcherRequest(r));
		publication.setResearchers(researchres);
		return publication;
	}
	
	/**
	 * Convert Researcher object into a ResearcherRequest object
	 * @param res
	 * @return
	 */
	public static ResearcherRequest convertResearcherToResearcherRequest(Researcher res) {	
		return new ResearcherRequest(res.getFirstName(), res.getLastName(), res.getDescription());
	}
	
	/**
	 * Convert ResearcherRequest object into a Researcher object
	 * @param res
	 * @return
	 */
	public static Researcher convertResearcherRequestToResearcher(ResearcherRequest res) {	
		return new Researcher(res.getFirstName(), res.getLastName(), res.getDescription());
	}
	
	/**
	 * Convert a PublicationRequest object into a Publication object
	 * @param pub
	 * @return
	 */
	public static Publication convertPublicationRequestToPublication(PublicationRequest pub) {
		ArrayList<Researcher> researchres = new ArrayList<>();
		for (ResearcherRequest r : pub.getResearchers()) 
			researchres.add(ConverterEntities.convertResearcherRequestToResearcher(r));
		Publication publication =  new Publication(pub.getId_publication(), pub.getPublication_Date(), pub.getPublication_title(),
				pub.getCitations(), pub.getRecommendations(), pub.getReads(), researchres);
		return publication;
	}
}
