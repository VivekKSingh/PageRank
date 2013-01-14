

import java.util.Map;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class SystemsInformation implements java.io.Serializable{

     public static final String CMD_TYPE_MESSAGETYPE_OBJECT = null;
	private static Sigar sigar = new Sigar();

     public static void main(String[] args) {
          fetchCpuInfo();
          fetchMemInfo();
          fetchCpuPERC();
     }
    
	public static Double fetchCpuPERC() {
	
		CpuPerc perc = null;
   	     try{
   		 perc = sigar.getCpuPerc();
   		 
   	     //System.out.println("The total cpu usage"+ (perc.getCombined()*100) +"%");
   	     
   	        }catch(SigarException se){
   		      se.printStackTrace();
   	     }
   	 
   	 	//System.out.println(perc.getCombined());
		return perc.getCombined();
	     }


	
	
     /* Method to get Informations about the CPU(s): */
     public static CpuInfo[] fetchCpuInfo() {
          
    	 	System.out.println("*** Informations about the CPUs: ***");
          

          CpuInfo[] cpuinfo = null;
          try {
               cpuinfo = sigar.getCpuInfoList();
          } catch (SigarException se) {
               se.printStackTrace();
          }

          System.out.println("---------------------");
          System.out.println("Sigar found " + cpuinfo.length + " CPU(s)!");
          System.out.println("---------------------");

          for (int i = 0; i<cpuinfo.length; i++) {
               Map map = cpuinfo[i].toMap();
               System.out.println("CPU " + i + ": " + map);
          }

          System.out.println("\n************************************\n");
          
          return cpuinfo;
		
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