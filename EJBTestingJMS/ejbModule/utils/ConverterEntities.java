package utils;

import java.util.ArrayList;

import common.Publication;
import common.Researcher;
import jpa.PublicationRequest;
import jpa.ResearcherRequest;

public class ConverterEntities {

	/**
	 * 
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
	 * 
	 * @param res
	 * @return
	 */
	public static ResearcherRequest convertResearcherToResearcherRequest(Researcher res) {	
		return new ResearcherRequest(res.getFirstName(), res.getLastName(), res.getDescription());
	}
}
