package ejb;

import java.util.List;

import javax.ejb.Local;

import common.Publication;
import jpa.PublicationRequest;
import jpa.UserEntity;

@Local
public interface AdministratorSessionBeanLocal {
	List<Publication> getAllPublications();
	List<UserEntity> getAllUsers();
	List<UserEntity> getUsersPendingRequest();
	List<PublicationRequest> getAllPendingPublicationTasks();
	List<PublicationRequest> getPendingPublicationTasksFiltered(int filter);
	boolean approveNewUser(UserEntity newUser);
	boolean removeUserFromPendingList(UserEntity newUser);
	boolean changeUserState(UserEntity user, boolean activate);
	List<UserEntity> getAllActiveUsers();
	List<UserEntity> getAllDesactiveUsers();
	boolean approveNewPublication(PublicationRequest pub, int intentition);
	boolean removePublicationFromPendingList(PublicationRequest pub);
	boolean removePublication(PublicationRequest pub);
	boolean updatePublicationData(PublicationRequest pub); 
	boolean startAdmin();
	boolean stopAdmin();
}
