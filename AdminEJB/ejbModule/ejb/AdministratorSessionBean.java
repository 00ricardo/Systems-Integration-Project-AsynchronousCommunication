package ejb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import common.Publication;
import common.Researcher;
import jpa.AdminLog;
import jpa.PublicationRequest;
import jpa.ResearcherRequest;
import jpa.UserEntity;
import utils.ConverterEntities;

/**
 * Session Bean implementation class AdministratorSessionBean
 */
@Stateless
@LocalBean
public class AdministratorSessionBean implements AdministratorSessionBeanRemote, AdministratorSessionBeanLocal {

	
	@PersistenceContext(name = "PersistenceProject3SBAdmin")
	private EntityManager em;
		
    /**
     * Default constructor. 
     */
    public AdministratorSessionBean() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Return all publications saved in the database
     */
    @Override
    public List<Publication> getAllPublications(){
    	String jpql = "SELECT distinct p FROM Publication p JOIN FETCH p.researchers r";
		TypedQuery<Publication> typedQuery = em.createQuery(jpql, Publication.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<Publication> publicationsList = typedQuery.getResultList();	
		return publicationsList;
    }

    /**
     * Return all users saved in the database
     */
	@Override
	public List<UserEntity> getAllUsers() {
    	String jpql = "SELECT distinct u FROM UserEntity u ";
		TypedQuery<UserEntity> typedQuery = em.createQuery(jpql, UserEntity.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<UserEntity> usersList = typedQuery.getResultList();
		return usersList;
	}
	
    /**
     * Return all active users saved in the database
     */
	@Override
	public List<UserEntity> getAllActiveUsers() {
    	String jpql = "SELECT distinct u FROM UserEntity u WHERE u.isActive=true";
		TypedQuery<UserEntity> typedQuery = em.createQuery(jpql, UserEntity.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<UserEntity> usersList = typedQuery.getResultList();
		return usersList;
	}
	
    /**
     * Return all desactive users saved in the database
     */
	@Override
	public List<UserEntity> getAllDesactiveUsers() {
    	String jpql = "SELECT distinct u FROM UserEntity u WHERE u.isActive=false";
		TypedQuery<UserEntity> typedQuery = em.createQuery(jpql, UserEntity.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<UserEntity> usersList = typedQuery.getResultList();
		return usersList;
	}

	
    /**
     * Return all users saved in the database that are pending acceptance
     */
	@Override
	public List<UserEntity> getUsersPendingRequest() {
    	String jpql = "SELECT distinct u FROM UserEntity u WHERE u.pendingRequest=:req ";
		TypedQuery<UserEntity> typedQuery = em.createQuery(jpql, UserEntity.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		typedQuery.setParameter("req", true);
		List<UserEntity> usersList = typedQuery.getResultList();
		return usersList;
	}
	
	/**
	 * Return all Publication Pending Requests (Add/Update/Remove)
	 */
	@Override
	public List<PublicationRequest> getAllPendingPublicationTasks(){
    	String jpql = "SELECT distinct p FROM PublicationRequest p JOIN FETCH p.researchers r";
		TypedQuery<PublicationRequest> typedQuery = em.createQuery(jpql, PublicationRequest.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<PublicationRequest> publicationsList = typedQuery.getResultList();
		return publicationsList;
	}
	
	/**
	 * Return Publication Pending Requests filtered
	 */
	@Override
	public List<PublicationRequest> getPendingPublicationTasksFiltered(int filter){
    	String jpql = "SELECT distinct p FROM PublicationRequest p JOIN FETCH p.researchers r WHERE p.actionToDo=:action";
		TypedQuery<PublicationRequest> typedQuery = em.createQuery(jpql, PublicationRequest.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		typedQuery.setParameter("action", filter);
		List<PublicationRequest> publicationsList = typedQuery.getResultList();
		return publicationsList;
	}
	
	/**
	 * Approve a new User
	 */
	@Override
	public boolean approveNewUser(UserEntity newUser) {
		String jpql = "UPDATE UserEntity SET pendingrequest=false WHERE username=:name";
		Query query = em.createQuery(jpql);
		int updateCount = query.setParameter("name", newUser.getUsername()).executeUpdate();
		return updateCount != 0;
	}
	
	/**
	 * Remove user from pending list
	 */
	@Override
	public boolean removeUserFromPendingList(UserEntity newUser) {
		UserEntity user = em.find(UserEntity.class, newUser.getUsername());
		em.remove(user);
		return true;
	}
	
	/**
	 * Activate/Deactivate a user
	 */
	@Override
	public boolean changeUserState(UserEntity user, boolean activate) {
		String jpql;
		if(activate)
			jpql = "UPDATE UserEntity SET isactive=true WHERE username=:name";
		else
			jpql = "UPDATE UserEntity SET isactive=false WHERE username=:name";
		Query query = em.createQuery(jpql);
		int updateCount = query.setParameter("name", user.getUsername()).executeUpdate();
		return updateCount != 0;
	}

	/*
	 * Add new Publication to the database
	 */
	@Override
	public boolean approveNewPublication(PublicationRequest pub, int intentition) {
		Publication newPub = ConverterEntities.convertPublicationRequestToPublication(pub);
		for (Researcher r : newPub.getResearchers()) 	
			em.persist(r);
		em.persist(newPub);
		return true;
	}

	/**
	 * Remove publication from the pending list
	 */
	@Override
	public boolean removePublicationFromPendingList(PublicationRequest pub) {
		PublicationRequest publication = em.find(PublicationRequest.class, pub.getId_publication());
		em.remove(publication);
		for (ResearcherRequest r : pub.getResearchers()) {
			ResearcherRequest rRemove = em.find(ResearcherRequest.class, r.getId_researcher());
			em.remove(rRemove);
		}
		return true;
	}
	
	/**
	 * Remove publication from the database
	 */
	@Override
	public boolean removePublication(PublicationRequest pub) {
		Publication publication = em.find(Publication.class, pub.getId_publication());
		em.remove(publication);
		return true;
	}
	
	/**
	 * Update a publication information
	 */
	@Override
	public boolean updatePublicationData(PublicationRequest pub) {
		String jpql = "UPDATE Publication SET citations=:cit, recommendations=:rec, reads=:red WHERE id_publication=:idpub";
		Query query = em.createQuery(jpql);
		int updateCount = query.setParameter("idpub", pub.getId_publication()).setParameter("cit", pub.getCitations()).setParameter("rec", pub.getRecommendations()).setParameter("red", pub.getReads()).executeUpdate();
		return updateCount != 0;
	}
	
	/**
	 * Save a log in the database that indicates that are one administrator running
	 */
	@Override
	public boolean startAdmin() {
		AdminLog admin = em.find(AdminLog.class, 1);
		if(admin != null)
			return false;
		AdminLog newAdmin = new AdminLog();
		em.persist(newAdmin);
		return true;
	}
	
	/**
	 * Remove the log int the database that indicates that are on administrator running
	 */
	@Override
	public boolean stopAdmin() {
		AdminLog admin = em.find(AdminLog.class, 1);
		if(admin != null) {
			em.remove(admin);
			return true;
		}
		return false;
	}

}
