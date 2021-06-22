package utils;

import java.io.Serializable;
import java.util.List;

import javax.jms.Destination;

import common.Publication;


public class MessageObject implements Serializable{
	private static final long serialVersionUID = 123689L;
	//MESSAGE TYPE CODES FROM USER
	public static final int LOGIN_MESSAGE_TYPE = 1;
	public static final int REGISTER_MESSAGE_TYPE = 2;
	public static final int LIST_ALL_PUBLICATION_MESSAGE_TYPE = 3;
	public static final int SEARCH_PUBLICATION_BY_TITLE_MESSAGE_TYPE = 4;
	public static final int ADD_PUBLICATION_MESSAGE_TYPE = 5;
	public static final int UPDATE_PUBLICATION_MESSAGE_TYPE = 6;
	public static final int REMOVE_PUBLICATION_MESSAGE_TYPE = 7;
	
	private int messageType;
	private UserClient user;
	private Publication publication;
	private List<Publication> publicationsList;
	private String publicationTitle;
	private Destination user_Destination;
	
	public MessageObject(int messageType) {
		this.messageType = messageType;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public UserClient getUser() {
		return user;
	}

	public void setUser(UserClient user) {
		this.user = user;
	}

	public Destination getUser_Destination() {
		return user_Destination;
	}

	public void setUser_Destination(Destination user_Destination) {
		this.user_Destination = user_Destination;
	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	public String getPublicationTitle() {
		return publicationTitle;
	}

	public void setPublicationTitle(String publicationTitle) {
		this.publicationTitle = publicationTitle;
	}

	public List<Publication> getPublicationsList() {
		return publicationsList;
	}

	public void setPublicationsList(List<Publication> publicationsList) {
		this.publicationsList = publicationsList;
	}
		
}
