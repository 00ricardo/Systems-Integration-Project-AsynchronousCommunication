package admin;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SendNotifications {

	private ConnectionFactory connectionFactory;
	private Destination destination;
	
	
	public SendNotifications() {
		try {
			this.connectionFactory = InitialContext.doLookup("jms/RemoteConnectionFactory");
			this.destination = InitialContext.doLookup("jms/topic/playTopic");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	/**
	 * Send a notification to all clients logged
	 * @param text
	 */
	public void send(String text) {
		try (JMSContext context = connectionFactory.createContext("john", "!1secret");) {
			JMSProducer messageProducer = context.createProducer();
			messageProducer.send(destination, text);
		} catch (Exception re) {
			re.printStackTrace();
		}
	}
}
