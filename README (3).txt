Requirements:

FOLLWING FILES NEED TO BE PRESENT IN THE HOME DIRECTORY OF THE USER ACCOUNT.
1. Daemon.jar
2. Daemon_lib
3. MPJDebuggerDemo.class
4. SystemsInformation.java
5. pagerank.input.10000.80
6. baremetalscript.pbs
7. Folder that contains mpj jars and configuration files.

FOLLOWING CHANGES MAY BE REQUIIRED FOR EXECUTING THE MPJ PROGRAM.

FILE TO BE CHANGED: wrapper.conf
PATH: ...[mpj folder]/conf/

1.Uncomment following line and change the port number if busy.
wrapper.java.additional.2=-Xrunjdwp:transport=dt_socket,address=25887,server=y,suspend=n

2.Uncomment following lines and change memory size if required.
wrapper.java.initmemory=3
wrapper.java.maxmemory=512

3.Change the port number in the following line if required.
#port number for the daemon.
wrapper.app.parameter.2=25420

LIBRARIES REQUIRED FOR BUILDING AND EXECUTING THE MONITORING DAEMON.
1. libsigar-amd64-linux.so
2. sigar.jar
3. log4j.jar
4. activem1-all-5.5.1.jar
5. sl4j-nop-1.5.2.jar

LIBRARIES REQUIRED FOR BUILDING AND EXECUTING THE FRONT END.
1. activem1-all-5.5.1.jar
2. jcommon-1.0.17.jar
3. jfreechart-1.0.14.jar
4. sl4j-nop-1.5.2.jar

SUBMITTING JOBS:
1. Baremetal job submisstion command:
qsub baremetalscript.pbs

2. VM job submission command:
qsub cloudscript.pbs
