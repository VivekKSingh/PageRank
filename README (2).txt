Following is the description of compiling and running the daemon and the client using the Eclipse IDE environment.

For the producer,
1. Go to, File >> New >> Java Project.
2. Type the name of the project in the Project Name, text field and click Next button.
3. Under the Libraries tab, click the Add External JARs button. Then, navigate to the folder in your file system that contain the sl4j, sigar, apache-activemq-all libraries. After adding these libraries, click Finish.
4. Paste the Producer.java file in the 'src' folder of the project in the project explorer of eclipse.
5. Also add the SystemsInformation java file in the same folder which is daemon.
6. Run the producer.

For the consumer,
1. Go to, File >> New >> Java Project.
2. Type the name of the project in the Project Name, text field and click Next button.
3. Under the Libraries tab, click the Add External JARs button. Then, navigate to the folder in your file system that contain the sl4j, jfree, apache-activemq-all libraries. After adding these libraries, click Finish.
4. Paste the FrontEnd.java file in the 'src' folder of the project in the project explorer of eclipse.
5. Run the front end.

Note: The UI will freeze as soon as it stops receiving messages from the producer via the broker. Hence, the broker needs to be alive for the UI to display any data.