import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.TimerTask;

import javax.jms.*;
import javax.jms.IllegalStateException;
import javax.management.monitor.Monitor;

import java.util.Timer;

import org.apache.activemq.*;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcUtil;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;

public class ProducerTest {

	static public void daemonize() {
		System.out.close();
		System.err.close();
	}

	private static String url = "tcp://129.79.49.248:61616";

	// Name of the Topic we will be sending messages to
	private static String subject = "G08_xyz1";

	public static void main(String[] args) throws JMSException,
			InterruptedException, UnknownHostException, SigarException {

		// Initializing and accessing the daemon class for information.
		

		final Mem mem = SystemsInformation.fetchMemInfo();
		
		// Getting JMS connection from the server and starting it
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		Connection connection = connectionFactory.createConnection();
		connection.start();

		// Creating a session.
		final Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);

		// Creating a topic on the broker.
		Destination topic = session.createTopic(subject);

		// Creating a handler for the Producer.
		final MessageProducer producer = session.createProducer(topic);

		// Initializing and starting a timer for sending information to the
		// broker.

		final Sigar sigar = new Sigar();
		ProcessFinder pi = new ProcessFinder(new Sigar());

		TimerTask task = new TimerTask() {

			int msgCount = 0;
			long currTime;
			String str, str_usedMem, str_usedCpu, str_procCpu;
			TextMessage message;
			Double used_sys_mem;
			Double used_cpu;
			String ip;
			String str_usedCpuProc;
			String str_usedProcMem;

			// ProcCpu procCPU;

			@Override
			public void run() {

				// Creating the information to be sent

				// Creating a message into which the information will be sent.
				try {
					//daemonize();
					used_sys_mem = (mem.getUsedPercent());
					used_cpu = (Double) (SystemsInformation.fetchCpuPERC() * 100);

					// Converting the information into String.

					str_usedMem = used_sys_mem.toString();
					str_usedCpu = used_cpu.toString();
					str_usedCpuProc = SystemsInformation.fetchProcessCpuInfo().toString();
					str_usedProcMem = SystemsInformation.fetchProcessMemInfo()
							.toString();

					ip = InetAddress.getLocalHost().getHostAddress();
					message = session.createTextMessage(str_usedMem + "-"
							+ str_usedCpu + "-" + ip + "-" + str_usedProcMem
							+ "-" + str_usedCpuProc + "-"
							+ ((System.currentTimeMillis() / 1000) * 1000));
					producer.send(message);
					
				}

				catch (JMSException e1) {
					e1.printStackTrace();
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				}
				catch (SigarException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		};

		// Creating a Timer object.
		Timer timer = new Timer();
		
		Date dte = new Date();
		long[] processes;
		int i;
		dte.setTime(((dte.getTime() / 1000) * 1000) + 1000);
		// Starting the timer.
		timer.scheduleAtFixedRate(task, dte, 1000);
		
		
		// Handling the main program and the timer
		System.out.println("********  MPI PAGERANK HAS STARTED ***********");
		Thread.sleep(300000);
		
		
	    // Cancelling the timer
		timer.cancel();
	    timer.purge();
		
	  // Disconnecting from broker 
	 connection.close();
	}

}
