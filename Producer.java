import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TimerTask;

import javax.jms.*;
import java.util.Timer;

import org.apache.activemq.*;
import org.hyperic.sigar.Mem;



public class Producer {

	
	private static String url = "tcp://129.79.49.181:61616";

	// Name of the Topic we will be sending messages to
	private static String subject = "G08_xyz";

	public static void main(String[] args) throws JMSException, InterruptedException {
		
       // Initializing and accessing the daemon class for information. 
		SystemsInformation sys = new SystemsInformation();
	    
		final Mem mem = SystemsInformation.fetchMemInfo();
		Map map = mem.toMap();
		
		// Getting JMS connection from the server and starting it
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		Connection connection = connectionFactory.createConnection();
		connection.start();
        
		// Creating a session.
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        
		// Creating a topic on the broker.
		Destination topic = session.createTopic(subject);
		
		// Creating a handler for the Producer.
		final MessageProducer producer = session.createProducer(topic);
		
		
		// Initializing and starting a timer for sending information to the broker.
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask(){
					
					int msgCount = 0;
					@Override
					public void run() {
						
						Double used_sys_mem = (mem.getUsedPercent());
						Double used_cpu = (Double)(SystemsInformation.fetchCpuPERC()*100);
						// Converting the information into String.
						String str_usedMem = used_sys_mem.toString();
						String str_usedCpu = used_cpu.toString();
						
						long currTime = System.currentTimeMillis();
						// Creating the information to be sent
						String str = str_usedMem + "-"+str_usedCpu+ "-" + Integer.toString(msgCount++);
						
						// Creating a message into which the information will be sent.
						TextMessage message;
					
						try {
							message = session.createTextMessage(str);
						
							producer.send(message);
							System.out.println("Sent message#"+ msgCount+ "\n" + str);
						    } catch (JMSException e) {
							
							e.printStackTrace();
						    }
					}
					
				}, 0, 1000);
				
				// Handling the main program and the timer 
				System.out.println("Main has slept. ");
				Thread.sleep(1000000000);	
				System.out.println("main woke up :-)");
				timer.cancel();
				timer.purge();
				
		connection.close();
	}

}
