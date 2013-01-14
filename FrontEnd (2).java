package my.test.file;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;

public class FrontEnd {

    private JFreeChart createNewChart(DynamicTimeSeriesCollection dataset, String title) {
	// Create dataset object for the chart
	float series[] = new float[COUNT];
	dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
	dataset.addSeries(series, 0, "Percentage usage");

	// Feed the data set to the JFreechart
	final JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "hh:mm:ss", "Percentage", dataset, true, true, false);

	final XYPlot plot = chart.getXYPlot();
	plot.setDomainCrosshairPaint(Color.GREEN);
	plot.setBackgroundPaint(Color.BLACK);
	ValueAxis domain = plot.getDomainAxis();
	domain.setAutoRange(true);
	ValueAxis range = plot.getRangeAxis();
	//range.setAutoRangeMinimumSize(0.00001, true);
	range.setAutoRangeMinimumSize(0.001);
	return chart;
    }

    public FrontEnd(String title) {

	// TODO Auto-generated constructor stub
	final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(1, COUNT, new Second());
	JFreeChart chart = createNewChart(dataset, "Overall average CPU usage on running nodes");

	final DynamicTimeSeriesCollection dataset1 = new DynamicTimeSeriesCollection(1, COUNT, new Second());
	JFreeChart chart1 = createNewChart(dataset1, "Overall average memory usage on running nodes");

	final DynamicTimeSeriesCollection dataset2 = new DynamicTimeSeriesCollection(1, COUNT, new Second());
	JFreeChart chart2 = createNewChart(dataset2, "Average CPU usage of MPI PageRank program on running nodes");

	final DynamicTimeSeriesCollection dataset3 = new DynamicTimeSeriesCollection(1, COUNT, new Second());
	JFreeChart chart3 = createNewChart(dataset3, "Average memory usage of MPI PageRank program on running nodes");
	// Create UI containers

	frame = new JFrame();
	frame.setTitle(title);
	frame.setBounds(100, 100, 450, 300);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

	ChartPanel cp = new ChartPanel(chart);

	frame.getContentPane().add(cp);

	ChartPanel cp1 = new ChartPanel(chart1);
	frame.getContentPane().add(cp1);

	ChartPanel cp2 = new ChartPanel(chart2);
	frame.getContentPane().add(cp2);

	ChartPanel cp3 = new ChartPanel(chart3);
	frame.getContentPane().add(cp3);

	final ExecutorService es = Executors.newCachedThreadPool(new ThreadFactory() {

	    @Override
	    public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	    }

	});

	final Runnable doJob = new Runnable() {
	    Message message;
	    String strng;
	    String dataReceived[];
	    final int totalNumOfProd = 2;
	    long prevTimeStamp = 0;
	    long currTimeStamp = 0;

	    float totProcUsed[] = { 0 };
	    float totMemUsed[] = { 0 };
	    float pidProcUsed[] = { 0 };
	    float pidMemUsed[] = { 0 };

	    public void run() {

		
		//System.out.println(totalNumOfProd);
		// TODO Auto-generated method stub
		
		for (int i = 0; i < totalNumOfProd; i++) {
		    try {
			message = consumer.receive();
			strng = ((TextMessage) message).getText();
			//System.out.println(i);
			System.out.println(strng);
			
		    } catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		    dataReceived = strng.split("-");
		   

		    totProcUsed[0] += Float.parseFloat(dataReceived[1]);
		    totMemUsed[0] += Float.parseFloat(dataReceived[0]);

		    try {
			pidMemUsed[0] += (float) Double.parseDouble(dataReceived[3]);
			pidProcUsed[0] += (float) Double.parseDouble(dataReceived[4]);
		    } catch (NumberFormatException e) {
			pidMemUsed[0] = 0;
			pidProcUsed[0] = 0;
		    }
		}
		System.out.println();
		

		totProcUsed[0] /= totalNumOfProd;
		totMemUsed[0] /= totalNumOfProd;
		pidMemUsed[0] /= totalNumOfProd;
		pidProcUsed[0] /= totalNumOfProd;

		dataset.advanceTime();
		dataset.appendData(totProcUsed);

		dataset1.advanceTime();
		dataset1.appendData(totMemUsed);

		dataset2.advanceTime();
		dataset2.appendData(pidProcUsed);

		dataset3.advanceTime();
		dataset3.appendData(pidMemUsed);

		totProcUsed[0] = 0;
		totMemUsed[0] = 0;
		pidProcUsed[0] = 0;
		pidMemUsed[0] = 0;
		// System.out.println(System.currentTimeMillis()-start);
	    }
	};

	timer = new Timer(1000, new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		es.execute(doJob);
	    }

	});
    }

    /**
     * @param args
     */
    private JFrame frame;
    static Timer timer;
    static MessageConsumer consumer = null;

    private static String url = "tcp://129.79.49.248:61616";

    final static int COUNT = 120;
    // final static String TITLE = "System Memory Usage";
    // final static String TITLE1 = "CPU Usage";
    final static String TOPIC = "G08_xyz1";
    final static private long serialVersionUID = -2197190906051451320L;

    public static void main(String[] args) throws JMSException {
	// TODO Auto-generated method stub

	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
	final Connection connection = connectionFactory.createConnection();
	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	Destination destination = session.createTopic(TOPIC);
	consumer = session.createConsumer(destination);
	connection.start();

	Runnable runthis = new Runnable() {

	    @Override
	    public void run() {
		// TODO Auto-generated method stub
		FrontEnd demo = new FrontEnd("Performance Analysis");
		demo.frame.setVisible(true);
		timer.start();
	    }

	};

	EventQueue.invokeLater(runthis);
    }

}
