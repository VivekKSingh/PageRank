

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Map;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;

public class SystemsInformation implements java.io.Serializable{

    public static final String CMD_TYPE_MESSAGETYPE_OBJECT = null;
	private static final Double NaN = null;
	private static Sigar sigar = new Sigar();

     public static void main(String[] args) throws SigarException, IOException {
          
          fetchMemInfo();
          fetchCpuPERC();
          fetchProcessCpuInfo();
          fetchProcessMemInfo();
          
     }
    
     // Function to capture the overall cpu usage	
     public static Double fetchCpuPERC() {
	
		CpuPerc perc = null;
   	     try{
   		 perc = sigar.getCpuPerc();
   		 }catch(SigarException se){
   		      se.printStackTrace();
   	     }
   	 
   	 
		return perc.getCombined();
    }

     // Function to capture the Process Cpu usage
     public static Double fetchProcessCpuInfo() throws SigarException{
	   
	   ProcessFinder p = new ProcessFinder(new Sigar());
	   Double sum =  (double) 0;
	   Double average = (double) 0;
	   Double cpuPercentage = null;
	   
	   long procMem;
	   // Query for fetching the PageRank process pid.
	   long[] pid = p.find("State.Name.eq=java,Args.*.ct=MPJDebuggerDemo");
	   
	   for(int i = 0; i<pid.length;i++){
		   ProcCpu procCPU = sigar.getProcCpu(pid[i]);
		   // Getting the process cpu usage.
		   cpuPercentage = (double)(procCPU.getPercent()*100.0);
		   sum += cpuPercentage;
	   }
	  
	   // Averaging the cpu usage.
	   average = (sum/pid.length);
	   
	   // Putting a cap to the CPU usage.
	   if(average > 100.00){
		   average = 100.00;
	   }
	
	   return average;
	   	
	   }
	
     // Function to capture the Process memory usage.
     public static Double fetchProcessMemInfo() throws SigarException{
   
	   ProcessFinder p = new ProcessFinder(new Sigar());
	  
	   Float sum = (float) 0;
	   Float average = (float) 0;
	   Double memPercentage = (double)0;
	   
	   long procMem;
	   long[] pid = p.find("State.Name.eq=java,Args.*.ct=MPJDebuggerDemo");
	   
	   for(int i = 0; i<pid.length;i++){
		   ProcCpu procCPU = sigar.getProcCpu(pid[i]);
		   procMem = sigar.getProcMem(pid[i]).getResident();
		   
		   sum += procMem;
		   }
	   
	   memPercentage = (double) ((sum/sigar.getMem().getTotal())*100);
	  
	   return memPercentage;
	  
	}
   
   
     /* Method to get Informations about the Memory: */
     public static Mem fetchMemInfo() {
          Mem mem = null;
          try {
               mem = sigar.getMem();
          } catch (SigarException se) {
               se.printStackTrace();
          }

          Map map = mem.toMap();
       
          return mem;
     }
    
}