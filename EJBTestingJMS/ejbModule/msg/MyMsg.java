package msg;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import common.Publication;
import jpa.PublicationRequest;
import jpa.ResearcherRequest;
import jpa.UserEntity;
import utils.ConverterEntities;
import utils.MessageObject;
import utils.UserClient;

@MessageDriven(name = "MyMsg", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/playQueue") })
public class MyMsg implements MessageListener {

	public static final String STRING_YES = "yes";
	public static final String STRING_NO = "no";
	public static final String STRING_FIRST_TIME_LOGIN = "yes_first_time";
	
	@Resource(name = "jms/RemoteConnectionFactory")
	private ConnectionFactory connectionFactory;

	@PersistenceContext(name = "PersistenceProject3MDB")
	private EntityManager em;
	

	public MyMsg() {
	}

	/**
	 * Method responsible for receive requests from different users
	 */
	@Override
	public void onMessage(Message msg) {
		if (msg instanceof ObjectMessage) {
			try {
				ObjectMessage objMsg = (ObjectMessage) msg;
				MessageObject objReceived = (MessageObject) objMsg.getObject();
				int msgType = objReceived.getMessageType();

				switch (msgType) {
				case MessageObject.LOGIN_MESSAGE_TYPE: // LOGIN
					int loginResult = this.loginUser(objReceived.getUser());
					System.out.println("Login request received from user: " + objReceived.getUser().getUsername());
					if (loginResult == 1)
						sendMessageToClient(STRING_YES, msg.getJMSReplyTo());
					else if (loginResult == -1)
						sendMessageToClient(STRING_NO, msg.getJMSReplyTo());
					else if(loginResult == 0)
						sendMessageToClient(STRING_FIRST_TIME_LOGIN, msg.getJMSReplyTo());
					break;
					
				case MessageObject.REGISTER_MESSAGE_TYPE: // REGISTRATION
					boolean registerResult = this.persistNewUserRequest(objReceived);
					System.out.println("Registration request received from user: " + objReceived.getUser().getUsername());
					if(registerResult)
						sendMessageToClient(STRING_YES, msg.getJMSReplyTo());
					else
						sendMessageToClient(STRING_NO, msg.getJMSReplyTo());
					break;
					
				case MessageObject.SEARCH_PUBLICATION_BY_TITLE_MESSAGE_TYPE:  // GET PUBLICATION BY TITLE
					System.out.println("Search publication request received for publication:" + objReceived.getPublicationTitle());
					sendPublicationTitlesToOneClient(getPublicationsByTitle(objReceived.getPublicationTitle()), msg.getJMSReplyTo());
					break;
					
				case MessageObject.ADD_PUBLICATION_MESSAGE_TYPE: // ADD NEW PUBLICATION
					boolean addPubResult = this.persistNewAddPublicationRequest(objReceived);
					System.out.println("New publication request received for publication:" + objReceived.getPublication().getPublication_title());
					if(addPubResult)
						sendMessageToClient(STRING_YES, msg.getJMSReplyTo());
					else
						sendMessageToClient(STRING_NO, msg.getJMSReplyTo());
					break;
					
				case MessageObject.UPDATE_PUBLICATION_MESSAGE_TYPE: // UPDATE PUBLICATION DATA
					boolean updatePubResult = this.persistRemoveOrEditPublicationRequest(objReceived);
					System.out.println("Update publication request received for publication:" + objReceived.getPublication().getPublication_title());
					if(updatePubResult)
						sendMessageToClient(STRING_YES, msg.getJMSReplyTo());
					else
						sendMessageToClient(STRING_NO, msg.getJMSReplyTo());
					break;
					
				case MessageObject.REMOVE_PUBLICATION_MESSAGE_TYPE: // REMOVE PUBLICATION 
					System.out.println("Delete publication request received for publication:" + objReceived.getPublication().getPublication_title());
					boolean removePubResult = this.persistRemoveOrEditPublicationRequest(objReceived);
					if(removePubResult)
						sendMessageToClient(STRING_YES, msg.getJMSReplyTo());
					else
						sendMessageToClient(STRING_NO, msg.getJMSReplyTo());
					break;
					
				case MessageObject.LIST_ALL_PUBLICATION_MESSAGE_TYPE:   // GET ALL PUBLICATIONS 
					System.out.println("Get all publications informations request received for publication");
					sendPublicationTitlesToOneClient(getAllPublicationTitles(), msg.getJMSReplyTo());
					break;
				} // End Switch

			} catch (JMSException e) {
				e.printStackTrace();
			}
		} // end if instanceof ObjectMessage

	}

	
	/**
	 * Login a user
	 * 
	 * @param userReceived
	 * @return 1 if the user credential are correct | -1 if the user
	 *         credential are incorrect | 0 if the user credential are correct and is the first time login
	 */
	public int loginUser(UserClient userReceived) {
		String jpql = "SELECT u FROM UserEntity u where u.username=:name AND u.password=:pass";
		TypedQuery<UserEntity> typedQuery = em.createQuery(jpql, UserEntity.class);
		typedQuery.setParameter("name", userReceived.getUsername());
		typedQuery.setParameter("pass", userReceived.getPassword());
		List<UserEntity> mylist = typedQuery.getResultList();
		if (mylist.isEmpty())
			return -1;
		if (mylist.get(0).isActive() && !mylist.get(0).isPendingRequest() && mylist.get(0).isFirstTimeLogin()) {
			String jpql2 = "UPDATE UserEntity SET firstTimeLogin=:flag  WHERE username=:user";
			Query query = em.createQuery(jpql2);
			query.setParameter("flag", false).setParameter("user", userReceived.getUsername()).executeUpdate();
			return 0;
		}
		if (mylist.get(0).isActive() && !mylist.get(0).isPendingRequest() && !mylist.get(0).isFirstTimeLogin())
			return 1;
		return -1;
	}

	
	/**
	 * Save a new user in the database
	 * 
	 * @param action
	 * @return
	 */
	private boolean persistNewUserRequest(MessageObject action) {
		String jpql = "SELECT u FROM UserEntity u where u.username=:name AND u.password=:pass";
		TypedQuery<UserEntity> typedQuery = em.createQuery(jpql, UserEntity.class);
		typedQuery.setParameter("name", action.getUser().getUsername());
		typedQuery.setParameter("pass", action.getUser().getPassword());
		List<UserEntity> mylist = typedQuery.getResultList();
		if (!mylist.isEmpty())
			return false;
		em.persist(new UserEntity(action.getUser().getUsername(), action.getUser().getPassword()));
		return true;
	}
	

	/**
	 * Send a message to a client
	 * 
	 * @param messageToSend
	 * @param clientDestination
	 */
	private void sendMessageToClient(String messageToSend, Destination clientDestination) {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			JMSProducer producer = context.createProducer();
			TextMessage reply = context.createTextMessage();
			reply.setText(messageToSend);
			producer.send(clientDestination, reply);
		} catch (JMSRuntimeException | JMSException e) {
			// e.printStackTrace();
		}
	}
	
	
	/**
	 * Send a list of publications titles to a client
	 * @param publications
	 * @param clientDestination
	 */
	private void sendPublicationTitlesToOneClient(List<Publication> publications, Destination clientDestination) {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			JMSProducer producer = context.createProducer();
			ObjectMessage reply = context.createObjectMessage();
			MessageObject mObj = new MessageObject(MessageObject.LIST_ALL_PUBLICATION_MESSAGE_TYPE);
			mObj.setPublicationsList(publications);
			reply.setObject(mObj);
			producer.send(clientDestination, reply);
		} catch (JMSRuntimeException | JMSException e) {
			// e.printStackTrace();
		}
	}

	
	/**
	 * Get all Publications saved in the database
	 * @return List of Publications or null if the list is Empty
	 */
	private List<Publication> getAllPublicationTitles() {
    	String jpql = "SELECT distinct p FROM Publication p JOIN FETCH p.researchers r";
		TypedQuery<Publication> typedQuery = em.createQuery(jpql, Publication.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<Publication> publicationsList = typedQuery.getResultList();
		if(publicationsList.isEmpty() || publicationsList == null)
			return null;
		return publicationsList;
	}
	
	
	/**
	 * Get Publication given a title
	 * @param title
	 * @return
	 */
    public List<Publication> getPublicationsByTitle(String title){
    	String jpql = "SELECT distinct p FROM Publication p JOIN FETCH p.researchers r WHERE p.publication_title='" + title +"'";
		TypedQuery<Publication> typedQuery = em.createQuery(jpql, Publication.class).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
		List<Publication> publicationsList = typedQuery.getResultList();
		if(publicationsList.isEmpty() || publicationsList == null)
			return null;
		return publicationsList;
    }
    
		
    /**
     * Add to the database the add publication request
     * @param action
     * @return
     */
	private boolean persistNewAddPublicationRequest(MessageObject action) {
		String jpql = "SELECT p FROM Publication p where p.id_publication=:idpub";
		TypedQuery<Publication> typedQuery = em.createQuery(jpql, Publication.class);
		typedQuery.setParameter("idpub", action.getPublication().getId_publication());
		List<Publication> mylist = typedQuery.getResultList();
		if (!mylist.isEmpty())
			return false;
		PublicationRequest pubRequest = ConverterEntities.convertPublicationToPublicationRequest(action.getPublication());
		pubRequest.setActionToDo(action.getMessageType());
		for (ResearcherRequest researcherRequest : pubRequest.getResearchers())
			em.persist(researcherRequest);
		em.persist(pubRequest);
		return true;
	}

	/**
	 * Add to the database the remove or edit publication request
	 * @param action
	 * @return
	 */
	private boolean persistRemoveOrEditPublicationRequest(MessageObject action) {
		// Exists any publication with that id
		String jpql = "SELECT p FROM Publication p where p.id_publication=:idpub";
		TypedQuery<Publication> typedQuery = em.createQuery(jpql, Publication.class);
		typedQuery.setParameter("idpub", action.getPublication().getId_publication());
		List<Publication> mylist = typedQuery.getResultList();
		if (mylist.isEmpty())
			return false;
		// If the same action exists in the database
		jpql = "SELECT p FROM PublicationRequest p where p.id_publication=:idpub AND p.actionToDo=:action";
		TypedQuery<PublicationRequest> typedQuery2 = em.createQuery(jpql, PublicationRequest.class);
		typedQuery2.setParameter("idpub", action.getPublication().getId_publication());
		typedQuery2.setParameter("action", action.getMessageType());
		List<PublicationRequest> mylist2 = typedQuery2.getResultList();
		if (!mylist2.isEmpty())
			return false;
		// If the publication exists in the database and there aren't any similar action
		PublicationRequest pubRequest = ConverterEntities.convertPublicationToPublicationRequest(action.getPublication());
		pubRequest.setActionToDo(action.getMessageType());
		for (ResearcherRequest researcherRequest : pubRequest.getResearchers())
			em.persist(researcherRequest);
		em.persist(pubRequest);
		return true;
	}
}