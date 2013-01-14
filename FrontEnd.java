import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class FrontEnd extends ApplicationFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -2197190906051451320L;
    static Timer timer;
    final static String TOPIC = "G08_xyz";
    static MessageConsumer consumer = null;
    private static String url = "tcp://129.79.49.181:61616";

    public FrontEnd(String title) {
	super(title);
	// TODO Auto-generated constructor stub
	
	// Create dataset object for the chart
	float series[] = new float[COUNT];
	final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(1, COUNT, new Second());
	dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
	dataset.addSeries(series, 0, "Percentage usage");

	// Feed the data set to the jfreechart
	final JFreeChart chart = ChartFactory.createTimeSeriesChart(TITLE, "hh:mm:ss", "Percentage", dataset, true, true, false);
	final XYPlot plot = chart.getXYPlot();
	ValueAxis domain = plot.getDomainAxis();
	domain.setAutoRange(true);
	ValueAxis range = plot.getRangeAxis();
	range.setRange(0, 100);

	// Create dataset object for the chart
	float series1[] = new float[COUNT];
	final DynamicTimeSeriesCollection dataset1 = new DynamicTimeSeriesCollection(1, COUNT, new Second());
	dataset1.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
	dataset1.addSeries(series1, 0, "Percentage usage");

	// Feed the data set to the jfreechart
	final JFreeChart chart1 = ChartFactory.createTimeSeriesChart(TITLE1, "hh:mm:ss", "Percentage", dataset1, true, true, false);
	final XYPlot plot1 = chart1.getXYPlot();
	ValueAxis domain1 = plot1.getDomainAxis();
	domain1.setAutoRange(true);
	ValueAxis range1 = plot1.getRangeAxis();
	range1.setRange(0, 100);

	// Create UI containers

	ChartPanel cp1 = new ChartPanel(chart);
	JScrollPane s1 = new JScrollPane(cp1);

	ChartPanel cp2 = new ChartPanel(chart1);
	JScrollPane s2 = new JScrollPane(cp2);
	this.add(s1, BorderLayout.CENTER);
	this.add(s2, BorderLayout.NORTH);

	//Create Timer Object and register an action listener with the timer.
	timer = new Timer(1000, new ActionListener() {

	    // float data[] = { 20 };
	    float data[] = { 10 };
	    int msgCount = 0;

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		/*
		 * data[0] = 30; dataset.advanceTime();
		 * dataset.appendData(data);
		 */
		Message message;
		try {
		    message = consumer.receive();
		    
		    if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			String strng = textMessage.getText();
			String usedCPUPercentage[] = strng.split("-");
			
			//Put the message identification code somewhere around here
			/*if (Integer.parseInt(usedCPUPercentage[usedCPUPercentage.length - 1]) == msgCount) {*/
			    data[0] = Float.parseFloat(usedCPUPercentage[1]);
			    dataset1.advanceTime();
			    dataset1.appendData(data);
			    
			    data[0] = Float.parseFloat(usedCPUPercentage[0]);
			    dataset.advanceTime();
			    dataset.appendData(data);
			    
			   // msgCount++;
			/*}*/

			System.out.println("Received message '" + strng + "'");
		    }
		} catch (JMSException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	});
    }

    /**
     * @param args
     */
    final static int COUNT = 120;
    final static String TITLE = "System Memory Usage";
    final static String TITLE1 = "CPU Usage";
    
    public static void main(String[] args) throws JMSException {
	// TODO Auto-generated method stub

	// Eshtablish connection with the broker.

	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
	Connection connection = connectionFactory.createConnection();
	connection.start();

	//Create a sesssion with the broker.
	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	//Create a consumer that listens for messages of the topic 'TOPIC'
	Destination destination = session.createTopic(TOPIC);
	consumer = session.createConsumer(destination);

	// ...............................................................
	Runnable runthis = new Runnable() {

	    @Override
	    public void run() {
		// TODO Auto-generated method stub
	    	//Initialize the Object that initializes the UI and the Timer object.
		FrontEnd demo = new FrontEnd("Performance Analysis");
		
		//pack and display the UI components of the ApplicationFrame
		demo.pack();
		RefineryUtilities.positionFrameRandomly(demo);
		demo.setVisible(true);
		
		//Start the timer
		timer.start();
	    }

	};
	
	//Add this object to the evet queue to be scheduled for running.
	EventQueue.invokeLater(runthis);
    }

}