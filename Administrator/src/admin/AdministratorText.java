package admin;

import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import common.Publication;
import common.Researcher;
import ejb.AdministratorSessionBeanRemote;
import jpa.PublicationRequest;
import jpa.UserEntity;
import utils.MessageObject;

public class AdministratorText {
	public final static String MENU = "0 - Exit\n" // PRINCIPAL MENU
			+ "1 - List all users\n" 
			+ "2 - List all pending taks\n" 
			+ "3 - Aprove or Reject pending task\n"
			+ "4 - Deactivate a user\n" 
			+ "5 - Active a user\n" 
			+ "6 - List all publication titles\n"
			+ "7 - Show a publication detailed information\n";
	
	public final static String MENU_PENDING_TASKS = "0 - Back Previous Menu\n" // PENDIG TASKS MENU
			+ "1 - Registrations\n" 
			+ "2 - Adding Publications\n" 
			+ "3 - Updating Publication\n"
			+ "4 - Remove Publication\n";
	
	public final static int MIN_MENU_OPTION = 0; // minimum principal menu option
	public final static int MAX_MENU_OPTION = 7; // maximum principal menu option
	public final static int MIN_MENU_PENDING_TASKS_OPTION = 0; // minimum pending tasks menu option
	public final static int MAX_MENU_PENDING_TASKS_OPTION = 4; // maximum pending tasks menu option

	// Accessing EJB Remote
	Properties jndiProperties;
	Context context;
	AdministratorSessionBeanRemote adminBean;	

	private SendNotifications senderNotifications;
	


	public AdministratorText() throws NamingException {
		// Setting up the EJB connection
		jndiProperties = new Properties();
		jndiProperties.setProperty("java.naming.factory.initial",
				"org.jboss.naming.remote.client.InitialContextFactory");
		jndiProperties.setProperty("java.naming.provider.url", "http-remoting://localhost:8080");
		jndiProperties.setProperty("jboss.naming.client.ejb.context", "true");
		context = new InitialContext(jndiProperties);
		adminBean = (AdministratorSessionBeanRemote) context
				.lookup("EARProject3/AdminEJB/AdministratorSessionBean!ejb.AdministratorSessionBeanRemote");
		//Create Notification Sender
		this.senderNotifications = new SendNotifications();
	}

	public static void main(String[] args) {
		AdministratorText admin;
		try {
			admin = new AdministratorText();
		} catch (NamingException e) {
			return;
		}
		
		//Check if exists one administrator already executing
		if(!admin.startAdmin()) {
			System.out.println("There is already one administrator runnig! Bye!");
			return;		
		}
		
		boolean exitFlag = false;
		while (!exitFlag) {
			admin.printMenu();
			int option;
			option = readOption(MIN_MENU_OPTION, MAX_MENU_OPTION);
			switch (option) {
			case 0:
				exitFlag = true;
				break;
			case 1: // List all users
				admin.listUsersInformation(admin.getAllUsers());
				break;
			case 2: // List all pending tasks
				admin.listingPendigTasks();
				break;
			case 3: // Approve or Reject pending task
				admin.pendingTasks();
				break;
			case 4: // Deactivate a user
				admin.changeUserState(false);
				break;
			case 5: // Activate a user
				admin.changeUserState(true);
				break;
			case 6: // List all publication titles
				admin.printAllPublicationTitles(admin.getAllPublicationTitles());
				break;
			case 7:
				admin.publicationDeatiledInfo(admin.getAllPublicationTitles());
				break;
			}
		}
		
		admin.stopAdmin();
		System.out.println("Shutdown sistem....");
	}
	
	/**
	 * Pending Tasks Operations
	 */
	private void pendingTasks() {
		List<UserEntity> registrations = this.getPendingUsers();
		List<PublicationRequest> publications = this.getAllPublicationPendingRequest();
		if( (registrations == null && publications == null) || (publications.isEmpty() && registrations.isEmpty()) ) {
			System.out.println("     - There aren't pending tasks to check!\n");
			return;
		}
		printpendingTasksMenu();
		int option = readOption(MIN_MENU_PENDING_TASKS_OPTION, MAX_MENU_PENDING_TASKS_OPTION);
		switch(option) {
			case 0:  //Back previous menu
				break;
			case 1: // Approve or reject Registrations
				this.aproveOrRejectRegistration(registrations);
				break;
				
			case 2: // Approve or reject new Publication
				this.aproveOrRejectPublicationTask(adminBean.getPendingPublicationTasksFiltered(MessageObject.ADD_PUBLICATION_MESSAGE_TYPE), MessageObject.ADD_PUBLICATION_MESSAGE_TYPE);
				break;
				
			case 3: // Approve or reject update Publication
				this.aproveOrRejectPublicationTask(adminBean.getPendingPublicationTasksFiltered(MessageObject.UPDATE_PUBLICATION_MESSAGE_TYPE), MessageObject.UPDATE_PUBLICATION_MESSAGE_TYPE);
				break;
				
			case 4: // Approve or reject remove Publication  
				this.aproveOrRejectPublicationTask(adminBean.getPendingPublicationTasksFiltered(MessageObject.REMOVE_PUBLICATION_MESSAGE_TYPE), MessageObject.REMOVE_PUBLICATION_MESSAGE_TYPE);
				break;
		}
	}
	
	
	/**
	 * Approve or reject publication task
	 * @param pubList
	 * @param filter
	 */
	private void aproveOrRejectPublicationTask(List<PublicationRequest> pubList, int filter) {
		if(pubList == null || pubList.isEmpty()) {
			System.out.println("There aren't any pendig publication task\n");
			return;
		}
		int maxOpt = this.listAllPendingPublicationRequest(pubList);
		int selected = readOption(1, maxOpt);
		PublicationRequest pubSelected = pubList.get(selected-1);
		System.out.println("==============\n1 - Approve \n2 - Reject \n============== ");
		int optFinal = readOption(1, 2);
		if(optFinal == 1) {  // Approve Operation
			boolean operationResult = false;
			switch(filter) {
				case MessageObject.ADD_PUBLICATION_MESSAGE_TYPE:
					operationResult = adminBean.approveNewPublication(pubSelected, filter);
					if(operationResult)
						sendNotificationsToClients("New publication with title: " + pubSelected.getPublication_title() + " added to the system.");
					break;
					
				case MessageObject.UPDATE_PUBLICATION_MESSAGE_TYPE:
					operationResult = adminBean.updatePublicationData(pubSelected);
					if(operationResult)
						sendNotificationsToClients("Publication with title: " + pubSelected.getPublication_title() + ", has been updated");
					break;
					
				case MessageObject.REMOVE_PUBLICATION_MESSAGE_TYPE:
					operationResult = adminBean.removePublication(pubSelected);
					if(operationResult)
						sendNotificationsToClients("Publication with title: " + pubSelected.getPublication_title() + ", has been deleted from the system.");
					break;
			}
			if(!operationResult) 
				System.out.println("An error occours in the operation approvement. Please, repeat the operation.\n");
		} else {        // Reject Operation
			System.out.println("Publication operation rejected with successes!\n");
		}
		adminBean.removePublicationFromPendingList(pubSelected);
	}
		

	/**
	 * Deactivate a user
	 */
	private void changeUserState(boolean newState){
		List<UserEntity> users;
		if(newState)
			users = this.getAllDesctiveUsers();
		else
			users = this.getAllActiveUsers();	
		if( users == null || users.isEmpty() ) {
			if(newState)
				System.out.println("The system don't have disactive users registred.");
			else
				System.out.println("The system don't have active users registred.");
			return;
		}	
		int maxOpt = 0;
		for (UserEntity user : users) {
			maxOpt++;
			System.out.println("      - " + maxOpt + " - " + user.getUsername());
		}
		int selected = readOption(1, maxOpt);
		UserEntity userSelected = users.get(selected-1);
		if(adminBean.changeUserState(userSelected, newState))
			System.out.println("User: " + userSelected.getUsername() + " changed with successed!");
		else
			System.out.println("An error occours, please try again!");
	}
		
	
	/**
	 * Get all active users from the database
	 * @return
	 */
	private List<UserEntity> getAllActiveUsers() {
		return adminBean.getAllActiveUsers();
	}
	
	
	/**
	 * Get all desactive users from the database
	 * @return
	 */
	private List<UserEntity> getAllDesctiveUsers() {
		return adminBean.getAllDesactiveUsers();
	}
	
	
	/**
	 * Aprove or Reject a user registration
	 * @param registrations
	 */
	private void aproveOrRejectRegistration(List<UserEntity> registrations) {
		if(registrations == null || registrations.isEmpty()) {
			System.out.println("There aren't any pendig registration to check!\n");
			return;
		}
		int maxOpt = this.listAllPendingRegistrations(registrations);
		int selected = readOption(1, maxOpt);
		UserEntity userSelected = registrations.get(selected-1);
		System.out.println("==============\n1 - Approve \n2 - Reject \n============== ");
		int optFinal = readOption(1, 2);
		if(optFinal == 1) {  // Approve Registration
			if(adminBean.approveNewUser(userSelected))
				System.out.println("User " + userSelected.getUsername() + " has approved!\n");
			else
				System.out.println("An error occours in the user approvement. Please, repeat the operation.\n");
		} else {        // Reject Registration
			adminBean.removeUserFromPendingList(userSelected);
			System.out.println("User rejected with successes!\n");
		}
	}

	
	/**
	 * List all pending tasks
	 * 
	 * @return
	 */
	private void listingPendigTasks() {
		System.out.println("-> Pending Tasks: ");
		List<UserEntity> registrations = this.getPendingUsers();
		int a = this.listAllPendingRegistrations(registrations);
		int b = this.listAllPendingPublicationRequest(this.getAllPublicationPendingRequest());
		if(a == 0 && b == 0)
			System.out.println("     - There aren't pending tasks to check!\n");
	}

	/**
	 * 
	 * @return
	 */
	private int listAllPendingPublicationRequest(List<PublicationRequest> pendingPublicationTasks) {
		int i = 0;
		if (pendingPublicationTasks == null)
			return 0;
		if (!pendingPublicationTasks.isEmpty())
			System.out.println("Pending Publications: ");
		for (PublicationRequest publicationRequest : pendingPublicationTasks) {
			i++;
			System.out.println("      - " + i + " - Title: " + publicationRequest.getPublication_title());
		}
		return i;
	}

	/**
	 * List all Pending Registrations
	 * 
	 * @return
	 */
	private int listAllPendingRegistrations(List<UserEntity> pedingRegistrations) {
		int i = 0;
		if(pedingRegistrations == null)
			return 0;
		if (!pedingRegistrations.isEmpty())
			System.out.println("USERS Pending:");
		for (UserEntity user : pedingRegistrations) {
			i++;
			System.out.println("      - " + i + " - " + user.getUsername());
		}
		return i;
	}

	/**
	 * Get all Publication Pending Requests (Add/Update/Remove)
	 * 
	 * @return
	 */
	private List<PublicationRequest> getAllPublicationPendingRequest() {
		return adminBean.getAllPendingPublicationTasks();
	}

	/**
	 * List all publications titles
	 */
	private List<Publication> getAllPublicationTitles() {
		return adminBean.getAllPublications();
	}

	/**
	 * Print all publication titles in the console (stdout)
	 * 
	 * @param publicationsList
	 */
	private void printAllPublicationTitles(List<Publication> publicationsList) {
		if (publicationsList.isEmpty() || publicationsList == null) {
			System.out.println("\nThere aren't publication saved in the database.\n");
			return;
		}
		System.out.println("\n  ->Publication Titles: ");
		for (Publication publication : publicationsList)
			System.out.println("     - " + publication.getPublication_title());
		System.out.println("\n");
	}

	/**
	 * List Detailed information regarding a publication
	 */
	private void publicationDeatiledInfo(List<Publication> pub) {
		if ( pub == null || pub.isEmpty() ) {
			System.out.println("\nThere aren't publications saved in the database.\n");
			return;
		}
		System.out.println("\n\nPublications : ");
		for (Publication p : pub)
			System.out.println((pub.indexOf(p) + 1) + "   - " + p.getPublication_title());
		System.out.println("\n Select a publication to see detailed information.");
		int option;
		option = readOption(1, pub.size() + 1);
		option--;
		System.out.println("\nThe " + pub.get(option).getPublication_title() + " publication was published in "
				+ pub.get(option).getPublication_Date() + ".");
		System.out.println("The Researchers (Authors) of " + pub.get(option).getPublication_title() + " are: ");
		for (Researcher r : pub.get(option).getResearchers()) {
			System.out.println("- " + r.getFirstName() + " " + r.getLastName());
		}
		System.out.println("At the moment the publication has: \n Citations: " + pub.get(option).getCitations()
				+ "\n Recommendations: " + pub.get(option).getRecommendations() + "\n Reads: "
				+ pub.get(option).getReads() + "\n");
	}

	/**
	 * Get all users saved in the database
	 * 
	 * @return
	 */
	private List<UserEntity> getAllUsers() {
		return adminBean.getAllUsers();
	}

	/**
	 * Get users saved in the databse that are waiting acceptance/rejection
	 * 
	 * @return
	 */
	private List<UserEntity> getPendingUsers() {
		return adminBean.getUsersPendingRequest();
	}

	/**
	 * List users information
	 */
	private void listUsersInformation(List<UserEntity> usersList) {
		if (usersList.isEmpty() || usersList == null) {
			System.out.println("\nThere aren't register users saved in the database.\n");
			return;
		}
		System.out.println("\n  ->Users: ");
		for (UserEntity user : usersList) {
			System.out.print("     - " + user.getUsername() + " | ");
			if (user.isActive())
				System.out.print("Active");
			else
				System.out.print("Disactive");
			if (user.isPendingRequest())
				System.out.print(" | Pending Registration");
			System.out.println("");
		}
		System.out.println("");
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
	 * Print the pending tasks menu to the stdout
	 */
	public void printpendingTasksMenu() {
		System.out.println(MENU_PENDING_TASKS);
	}

	/**
	 * print the menu to the stdout
	 */
	public void printMenu() {
		System.out.println(MENU);
	}

	/**
	 * Delegate method - send notifications to all clients
	 * @param text
	 */
	public void sendNotificationsToClients(String text) {
		senderNotifications.send(text);
	}
	

	/**
	 * Check if can start the admin
	 * @return
	 */
	public boolean startAdmin() {
		return adminBean.startAdmin();
	}
	
	/**
	 * Stop the admin
	 * @return
	 */
	public boolean stopAdmin() {
		return adminBean.stopAdmin();
	}
	
}
