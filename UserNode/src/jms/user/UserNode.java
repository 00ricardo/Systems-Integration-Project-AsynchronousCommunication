package jms.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import utils.MessageObject;
import common.Publication;
import common.Researcher;
import utils.UserClient;


public class UserNode implements MessageListener, Runnable{
	
	public final static String MENU_BEFORE_LOGIN = "0 - Exit\n"
			+ "1 - Register\n"
			+ "2 - Login\n";
	public final static String MENU_AFTER_LOGIN = "0 - Exit\n"
			+ "1 - List all publication titles\n"
			+ "2 - Search publication by title\n"
			+ "3 - Add a new publication\n"
			+ "4 - Update a publication\n"
			+ "5 - Remove a publication\n";
	public final static int MIN_MENU_OPTION_BEFORE_LOGIN = 0;
	public final static int MAX_MENU_OPTION_BEFORE_LOGIN = 2;
	public final static int MIN_MENU_OPTION_AFTER_LOGIN = 0;
	public final static int MAX_MENU_OPTION_AFTER_LOGIN = 5;
	
	private ConnectionFactory connectionFactory;
	//JMS Queue to receive requests
	private Destination destination;
	//JMS Topic to send notification to clients
	private Destination destinationTopic;
	
	
	private boolean isLoged = false;
	private UserClient user;
	private boolean shutdownFlag = false;
	private ArrayList<Researcher> researchers = new ArrayList<>();
	
	public UserNode() throws NamingException {
		this.connectionFactory = InitialContext.doLookup("jms/RemoteConnectionFactory");
		this.destination = InitialContext.doLookup("jms/queue/playQueue");
		this.destinationTopic = InitialContext.doLookup("jms/topic/playTopic");
		this.user = new UserClient();
	}
	
	public static void main(String[] args) throws NamingException {
		UserNode sender = new UserNode();
				
		while(!sender.shutdownFlag) {
			sender.printMenu();
			int option;
			if(!sender.isLoged)
				option = readOption(MIN_MENU_OPTION_BEFORE_LOGIN, MAX_MENU_OPTION_BEFORE_LOGIN);
			else
				option = readOption(MIN_MENU_OPTION_AFTER_LOGIN, MAX_MENU_OPTION_AFTER_LOGIN);
			switch(option) {
				case 0:
					sender.shutdownFlag = true;
					break;
					
				case 1:
					if(!sender.isLoged) { // Register
						System.out.print("New Username: ");
						sender.user.setUsername(readString());
						System.out.print("New Password: ");
						sender.user.setPassword(readString());
						boolean registerResult = sender.registerRequest();
						if(registerResult)
							System.out.println("You registration has been acepted, and waits confirmation from administrator!");
						else
							System.out.println("The username already exists. Please, try another one!");
					} else {              // List all publication title
						sender.printPublicationData(sender.getAllPublicationsRequest());						
					}
					break;
					
				case 2:
					if(!sender.isLoged) {  // Login
						
						System.out.print("Username: ");
						sender.user.setUsername(readString());
						System.out.print("Password: ");
						sender.user.setPassword(readString());
						int loginResultInt = sender.loginRequest();
						if(loginResultInt == 1) {
							System.out.println("You are logged in the application!");
							sender.isLoged = true;
							//Thread responsible by receive notifications from the Administration Server
							new Thread(sender).start();
						} else if(loginResultInt == -1) {
							System.out.println("You are not logged in the application! Please, try again!");
							sender.isLoged = false;
						}else if(loginResultInt == 0) {
							System.out.println("The administrator have accepted your registration! You are logged in the application!");
							sender.isLoged = true;
							//Thread responsible by receive notifications from the Administration Server
							new Thread(sender).start();
						}
						
					} else {              // Search Publication by Title
						System.out.println("Publication title: ");
						sender.publicationByTitleRequest(readString());
					}
					break;
					
				case 3:        // Add a new Publication
					sender.getResearcherData();
					sender.loopAddPublication();
					sender.addNewPublication(sender.getPublicationsData());
					break;
					
				case 4:       // Update a publication
					List<Publication> pubsUpdate = sender.getAllPublicationsRequest();
					int maxUpdateIndex = sender.printPublicationData(pubsUpdate);
					int pubSelected = readOption(1, maxUpdateIndex);
					sender.updateOrDeletePublicationRequest(sender.updatePublication(pubsUpdate.get(pubSelected-1)), MessageObject.UPDATE_PUBLICATION_MESSAGE_TYPE);
					break;
					
				case 5:       // Remove a publication
					List<Publication> pubsRemove = sender.getAllPublicationsRequest();
					int maxRemoveIndex = sender.printPublicationData(pubsRemove);
					int pubRemove = readOption(1, maxRemoveIndex);
					sender.updateOrDeletePublicationRequest(pubsRemove.get(pubRemove-1), MessageObject.REMOVE_PUBLICATION_MESSAGE_TYPE);
					break;
			}
		}
	}
	
	/**
	 * Print the titles of a publications list
	 */
	private int printPublicationData(List<Publication> publications) {
		if(publications == null || publications.isEmpty()) {
			System.out.println("There aren't publications available!");
			return -1;
		}
		int i=0;
		for (Publication p : publications) {
			i++;
			System.out.println("- " + i + " - Title: " + p.getPublication_title());
		}
		return i;
	}

	/**
	 * Get all publications data from the system service
	 * @return
	 */
	private List<Publication> getAllPublicationsRequest() {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			//Create the Producer
			JMSProducer messageProducer = context.createProducer();
			ObjectMessage msg = context.createObjectMessage();
			//Create the remporaryQueue
			Destination tmp = context.createTemporaryQueue();
			msg.setJMSReplyTo(tmp);
			//Message creation
			MessageObject mObj = new MessageObject(MessageObject.LIST_ALL_PUBLICATION_MESSAGE_TYPE);
			mObj.setUser(this.user);
			msg.setObject(mObj);
			//Sender message
			messageProducer.send(destination, msg);
			//Create the consumer to receive the response
			JMSConsumer cons = context.createConsumer(tmp);
			Message response = cons.receive();
			ObjectMessage objMsg = (ObjectMessage) response;
			MessageObject objReceived = (MessageObject) objMsg.getObject();
			return objReceived.getPublicationsList();
		} catch (JMSException e) {
			//e.printStackTrace();
			return null;
		}
	}

	/**
	 * Send a update publication request to the system
	 * @param publicationUpdated
	 * @return
	 */
	public void updateOrDeletePublicationRequest(Publication publication, int intention) {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			//Create the Producer
			JMSProducer messageProducer = context.createProducer();
			ObjectMessage msg = context.createObjectMessage();
			//Create the temporaryQueue
			Destination tmp = context.createTemporaryQueue();
			msg.setJMSReplyTo(tmp);
			//Message criation
			MessageObject mObj = new MessageObject(intention);
			mObj.setPublication(publication);
			msg.setObject(mObj);
			//Sender message
			messageProducer.send(destination, msg);
			//Create the consumer to receive the response
			JMSConsumer cons = context.createConsumer(tmp);
			String str = cons.receiveBody(String.class);
			if(str.equals("yes"))
				System.out.println("Request sended sucessfully... Wainting approvement!");
			else
				System.out.println("An Error occours. Please, try again!");
		} catch (Exception re) {
			//re.printStackTrace();
		}
	}

	
	/**
	 * Get the information about a publication given a tile
	 * @param title
	 * @return
	 */
	public int publicationByTitleRequest(String title) {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			//Create the Producer
			JMSProducer messageProducer = context.createProducer();
			ObjectMessage msg = context.createObjectMessage();
			//Create the temporaryQueue
			Destination tmp = context.createTemporaryQueue();
			msg.setJMSReplyTo(tmp);
			//Message criation
			MessageObject mObj = new MessageObject(MessageObject.SEARCH_PUBLICATION_BY_TITLE_MESSAGE_TYPE);
			mObj.setPublicationTitle(title);
			msg.setObject(mObj);
			//Sender message
			messageProducer.send(destination, msg);
			//Create the consumer to receive the response
			JMSConsumer cons = context.createConsumer(tmp);
			Message response = cons.receive();
			ObjectMessage objMsg = (ObjectMessage) response;
			MessageObject objReceived = (MessageObject) objMsg.getObject();
			this.printPublicationData(objReceived.getPublicationsList());
		} catch (Exception re) {
			//re.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Send a message to the system in order to add a new Publication
	 * @param publication
	 * @return
	 */
	public void addNewPublication(Publication publication) {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			//Create the Producer
			JMSProducer messageProducer = context.createProducer();
			ObjectMessage msg = context.createObjectMessage();
			//Create the temporaryQueue
			Destination tmp = context.createTemporaryQueue();
			msg.setJMSReplyTo(tmp);
			//Message criation
			MessageObject mObj = new MessageObject(MessageObject.ADD_PUBLICATION_MESSAGE_TYPE);
			mObj.setPublication(publication);
			msg.setObject(mObj);
			//Sender message
			messageProducer.send(destination, msg);
			//Create the consumer to receive the response
			JMSConsumer cons = context.createConsumer(tmp);
			String str = cons.receiveBody(String.class);
			if(str.equals("yes"))
				System.out.println("Request add new Publication sended sucessfully... Wainting approvement!");
			else
				System.out.println("Error adding new Publication. The Publication with DOI: " + publication.getId_publication() + " already exists!");

		} catch (Exception re) {
			//re.printStackTrace();
		}
		researchers.clear();
	}
	
	/**
	 * Get the information about a researcher from stdin 
	 * @return
	 */
	public  ArrayList<Researcher> getResearcherData() {
		String first_name;
		String last_name;
		String description;
		System.out.print("Researcher first name: ");
		first_name= readString();
		System.out.print("Researcher last name: ");
		last_name= readString();
		System.out.print("Researcher description: ");
		description= readString();	
		researchers.add(new Researcher(first_name,last_name,description));
		return researchers;
	}
	
	/**
	 * Read the publication informationm from stdin
	 * @return
	 */
	public  Publication getPublicationsData() {
		String doi;
		String publication_title;
		String publication_date;
		int citations;
		int recommendations;
		int reads;
		System.out.print("Publication DOI: ");
		doi= readString();
		System.out.print("Publication title: ");
		publication_title= readString();
		System.out.print("Publication date: ");
		publication_date= readString();
		System.out.print("Number of citations: ");
		citations= readInt();
		System.out.print("Number of recommendations: ");
		recommendations= readInt();
		System.out.print("Number of reads: ");
		reads= readInt();

		return (new Publication(doi,publication_date,publication_title,citations,recommendations,reads,researchers));
	}
	
	/**
	 * Read the new data to update the publication information
	 * @param oldPublication
	 * @return
	 */
	public Publication updatePublication(Publication oldPublication) {
		int citations;
		int recommendations;
		int reads;
		System.out.print("Number of citations: ");
		citations= readInt();
		System.out.print("Number of recommendations: ");
		recommendations= readInt();
		System.out.print("Number of reads: ");
		reads= readInt();
		return (new Publication(oldPublication.getId_publication(),oldPublication.getPublication_Date(),oldPublication.getPublication_title(),citations,recommendations,reads,oldPublication.getResearchers()));
	}
	
	/**
	 * Method reponsable for collect the new publication information
	 */
	public void loopAddPublication() {
		boolean loop = true;
		int op;
		while(loop) {
			System.out.println("1 - Add more Researchers. ");
			System.out.println("2 - Continue. ");
			for(Researcher r : researchers) {
				System.out.println("Researchers (Authors) associated: " + r.getFirstName() + r.getLastName());
			}
			op= readInt();
			switch(op) {
			case 1:
				getResearcherData();
				break;
			case 2:
				loop = false;			
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Send a login request to the system server
	 * @return 1 if login occurs with successes | -1 if the login don't occurs | 0 if its first time login
	 */
	public int loginRequest() {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			//Create the Producer
			JMSProducer messageProducer = context.createProducer();
			ObjectMessage msg = context.createObjectMessage();
			//Create the remporaryQueue
			Destination tmp = context.createTemporaryQueue();
			msg.setJMSReplyTo(tmp);
			//Message criation
			MessageObject mObj = new MessageObject(MessageObject.LOGIN_MESSAGE_TYPE);
			mObj.setUser(this.user);
			msg.setObject(mObj);
			//Sender message
			messageProducer.send(destination, msg);
			//Create the consumer to receive the response
			JMSConsumer cons = context.createConsumer(tmp);
			String str = cons.receiveBody(String.class);
			if(str.equals("yes"))
				return 1;
			else if(str.equals("yes_first_time"))
				return 0;
		} catch (Exception re) {
			re.printStackTrace();
		}
		return -1;
	}

	
	/**
	 * Send a register request to system server.
	 * @return 
	 */
	public boolean registerRequest() {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			//Create the Producer
			JMSProducer messageProducer = context.createProducer();
			ObjectMessage msg = context.createObjectMessage();
			//Create the temporaryQueue
			Destination tmp = context.createTemporaryQueue();
			msg.setJMSReplyTo(tmp);
			//Message creation
			MessageObject mObj = new MessageObject(MessageObject.REGISTER_MESSAGE_TYPE);
			mObj.setUser(this.user);
			msg.setObject(mObj);
			//Sender message
			messageProducer.send(destination, msg);
			//Create the consumer to receive the response
			JMSConsumer cons = context.createConsumer(tmp);
			System.out.println("Please, wait for the confirmation...");
			String str = cons.receiveBody(String.class);
			if(str.equals("yes"))
				return true;
		} catch (Exception re) {
			re.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Print the menu in the stdout
	 */
	public void printMenu() {
		if(!this.isLoged)
			System.out.println(MENU_BEFORE_LOGIN);
		else
			System.out.println(MENU_AFTER_LOGIN);
	}
	
	/**
	 * Read an int (option menu) from stdin
	 * 
	 * @param minimumOption  allowed
	 * @param maximumOptiona allowed
	 * @return an validate int
	 */
	private static int readOption(int minimumOption, int maximumOption) {
		int opt = 0;
		System.out.print("Insert your option: ");
		Scanner scan = new Scanner(System.in);
		boolean aux = true;
		while (aux) {
			if (scan.hasNextInt()) {
				opt = scan.nextInt();
				if (opt >= minimumOption && opt <= maximumOption)
					aux = false;
				else
					System.out.print("Invalid option. Try again: ");
			} else {
				System.out.print("Invalid option. Try again: ");
				scan.next();
			}
		}
		return opt;
	}

	/**
	 * Read a String from stdin
	 * 
	 * @return a string
	 */
	private static String readString() {
		Scanner sc = new Scanner(System.in);
		String str = sc.nextLine();
		return str;
	}
	
	/**
	 * 
	 * @return
	 */
	private static int readInt() {
		Scanner sc = new Scanner(System.in);
		int integer = sc.nextInt();
		return integer;
	}

	/**
	 * Method responsible for receive the notification messages
	 */
	@Override
	public void onMessage(Message msg) {
		TextMessage textMsg = (TextMessage) msg;
		try {
			System.out.println("[NOTIFICATION]:" + textMsg.getText());
		} catch (JMSException e) {
			System.err.println("[Catch Exeption]: onMessage Method -> JMSException.");
		}
	}

	/**
	 * Set the class sender an MessageListener
	 * @throws InterruptedException
	 */
	public void launch_and_wait() throws InterruptedException {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			context.setClientID(user.getUsername());
			JMSConsumer consumer = context.createDurableConsumer((Topic) destinationTopic, "mySubscription");
			consumer.setMessageListener(this);
			while(!this.shutdownFlag) {
					Thread.sleep(2000);
			}
		} catch (JMSRuntimeException  e) {
			System.err.println("[Catch Exeption]: JMSRuntimeException.");
		}
	}

	/**
	 * Thread responsible for call the method responsible for establish a connection to a topic
	 */
	@Override
	public void run() {
		try {
			launch_and_wait();
		} catch (InterruptedException e) {
			System.err.println("[Catch Exeption]: run method.");
		}
	}


}
