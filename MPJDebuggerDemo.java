import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import mpi.*;

public class MPJDebuggerDemo {

	public static void main(String myargs[]) throws FileNotFoundException,
			IOException {

		String[] args = MPI.Init(myargs);

		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		// These are the datastructures that would be used by each process to
		// calculate the pagerank.
		
		double dampingfactor = 0.85;
		double threshold = 0.001;
		int numOfIterations = -1;
		String inFilename= null;
		String outFilename = "ParallelPageRank.output";
		boolean printHelp = false, printOutTime = true;
		
		for(int i=0;i<args.length;i++){
			if(args[i].equals("-i")){
				inFilename = args[i+1];
			}
			if(args[i].equals("-n")){
				numOfIterations = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals("-t")){
				threshold = Double.parseDouble(args[i+1]);
			}
			if(args[i].equals("-o")){
				printOutTime = true;
			}
			if(args[i].equals("-d")){
				
			}
			if(args[i].equals("-h")){
				printHelp = true;
			}
		}
		
		if(inFilename == null){
			if(rank == 0)
			System.out.println("Input file not specified!");
			printHelp = true;
		}
		if(numOfIterations == -1){
			if(rank == 0)
			System.out.println("Number of Iterations not specified!");
			printHelp = true;
		}
		if(printHelp){
			if(rank == 0){
			System.out.println("Usage: -i inputfilename -n num_iterations\n");
			System.out.println("-i filename    : adjacency matrix input file");
			System.out.println("-n num_iterations: number of iterations");
			System.out.println("-t threshold   : threshold value (default 0.0010)");
			System.out.println("-o             : output timing results (default yes)");
			System.out.println("-d             : enable debug mode");
			System.out.println("-h             : print help information");
			}
			return;
		}
		
		double compTime = -1, ioTime = -1;
		double startCompTime = -1;
		
		TreeMap<Integer, Vector<Integer>> hash = new TreeMap<Integer, Vector<Integer>>();
		int outbounds[] = new int[0];
		double pagerank[] = new double[1];
		int partition = 0,COUNTER=0;
		double delta = 1000;
		boolean readFileOnce = false;
		
		
		/*
		 * Process with rank 0 should read the file, send data and get back
		 * results from each process.
		 */
		int iteration = 0;
	for(iteration = 0;iteration<numOfIterations&&delta>threshold;iteration++){
		if (rank == 0) {
			// .......................................................................

			// INITIALIZING DATA STRUCTURES
			if (!readFileOnce) {
				hash = new TreeMap<Integer, Vector<Integer>>();

				/*
				 * if (args.length < 4) System.out .println(
				 * "USAGE: java PageRankSeqGroup8 [inputfile] [outputfile] [number of iterations] [damping factor]"
				 * );
				 */
				dampingfactor = 0.85;// Double.parseDouble(args[3]);
				if (0 > dampingfactor && dampingfactor > 1) {
					System.out
							.print("damping factor should be a positive number less than or equal to 1");
					System.exit(0);
				}

				//int numOfIterations = Integer.parseInt(args[2]);

				FileReader dis = new FileReader(inFilename);
				BufferedReader br = new BufferedReader(dis);

				String strng;
				Vector<String[]> bucket = new Vector<String[]>();
				String strarray[];

				double startIOTime = System.currentTimeMillis();
				
				for (COUNTER = 0; (strng = br.readLine()) != null; COUNTER++) {
					strarray = strng.split(" ");
					bucket.add(strarray);
				}

				ioTime = System.currentTimeMillis() - startIOTime;
				
				pagerank = new double[COUNTER];
				outbounds = new int[COUNTER];
				Vector<Integer> inbounds[] = new Vector[COUNTER];
				// //Done reading the file

				br.close();
				dis.close();
				
				// Populate the relevant data structures

				for (int i = 0; i < inbounds.length; i++)
					inbounds[i] = new Vector<Integer>();

				Iterator itr0;
				itr0 = bucket.iterator();
				int node = -1;
				while (itr0.hasNext()) {
					strarray = (String[]) itr0.next();
					node = Integer.parseInt(strarray[0]);
					for (int i = 1; i < strarray.length; i++)
						inbounds[Integer.parseInt(strarray[i])]
								.add(new Integer(node));

					hash.put(new Integer(node), inbounds[node]);
					outbounds[Integer.parseInt(strarray[0])] = strarray.length - 1;
				}

				bucket = null; // Free the memory for the bucket.
				itr0 = null;

				// temp = new double[COUNTER];

				for (int i = 0; i < pagerank.length; i++)
					pagerank[i] = 1D / (double) COUNTER;

				// .........Done populating datastructures....

				// Prepare for sending the
				// data....................................
				TreeMap<Integer, Vector<Integer>> hash1[] = new TreeMap[size - 1];

				String counterStrng[] = { Integer.toString(COUNTER) };

				// send the size. THIS IS USELESS FOR NOW
				partition = COUNTER / size;
				int rankCounter = 1;
				int nodeCounter = 0;
				itr0 = hash.entrySet().iterator();
				Map.Entry<Integer, Vector<Integer>> pair;

				TreeMap<Integer, Vector<Integer>> tempMap;
				for (; rankCounter < size; rankCounter++) {
					tempMap = new TreeMap<Integer, Vector<Integer>>();
					for (nodeCounter = 0; nodeCounter < partition
							&& itr0.hasNext(); nodeCounter++) {
						pair = (Map.Entry<Integer, Vector<Integer>>) itr0.next();
						tempMap.put(pair.getKey(), pair.getValue());
					}
					hash1[rankCounter - 1] = tempMap;
				}

				tempMap = new TreeMap<Integer, Vector<Integer>>();
				while (itr0.hasNext()) {
					pair = (Map.Entry<Integer, Vector<Integer>>) itr0.next();
					tempMap.put(pair.getKey(), pair.getValue());
				}
				hash = tempMap;

				// Modify the hash for use with process with rank 0.

				// ..........................Send the
				// data..........................
				MPI.COMM_WORLD.Bcast(counterStrng, 0, 1, MPI.OBJECT, 0);
				MPI.COMM_WORLD.Bcast(outbounds, 0, outbounds.length, MPI.INT, 0);
				
				for (int rank1 = 1; rank1 < size; rank1++) {
					MPI.COMM_WORLD.Send(hash1, rank1 - 1, 1, MPI.OBJECT, rank1,69);
				}
				
			}// done if reading file once

				MPI.COMM_WORLD.Bcast(pagerank, 0, pagerank.length, MPI.DOUBLE, 0);
			if(!readFileOnce){
				startCompTime = System.currentTimeMillis(); 
				readFileOnce = true;
			}
			// MPI.COMM_WORLD.Send(string, 0, string.length, MPI.OBJECT, 1, 78);
		}

		// The rest of the processes should receive the data.
		else {

			// RECEIVE relevant data
			

			dampingfactor = 0.85;
			int tempcounter = 0;
			if (!readFileOnce) {
				String strngCounter[] = new String[1];
				MPI.COMM_WORLD.Bcast(strngCounter, 0, 1, MPI.OBJECT, 0);
				COUNTER = Integer.parseInt(strngCounter[0]);
				//System.out.println(COUNTER);

				outbounds = new int[COUNTER];
				MPI.COMM_WORLD.Bcast(outbounds, 0, outbounds.length, MPI.INT,0);

				TreeMap[] hash1 = new TreeMap[1];
				MPI.COMM_WORLD.Recv(hash1, 0, 1, MPI.OBJECT, 0, 69);
				hash = hash1[0];
				
				readFileOnce = true;
			}
			pagerank = new double[COUNTER];
			MPI.COMM_WORLD.Bcast(pagerank, 0, pagerank.length, MPI.DOUBLE, 0);

			// ...........Done receiving data......................
		}

		// ................Calculate Pagerank................

		/*
		 * This would be common for all the processes. Including process with
		 * rank 0
		 */

		double[] temp = new double[hash.keySet().size()];
		Iterator itr, itr1;
		int target = -1, source = -1;
		double tempval = 0, danglingPageRank = 0;
		Vector<Integer> inbounds1;
		Map.Entry<Integer, Vector<Integer>> pair;

		// System.out.println("\nIteration number:" + (i + 1));

		danglingPageRank = 0;

		itr = hash.entrySet().iterator();
		int tempcounter = 0;

		while (itr.hasNext()) {
			pair = (Map.Entry<Integer, Vector<Integer>>) itr.next();
			target = pair.getKey().intValue();

			//System.out.print("target node:" + target + " inbound array:[");

			if (outbounds[target] == 0)
				danglingPageRank += pagerank[target];

			tempval = 0;

			inbounds1 = pair.getValue();
			itr1 = inbounds1.iterator();
			while (itr1.hasNext()) {
				source = ((Integer) itr1.next()).intValue();

				//System.out.print(" " + source + " ");

				tempval += dampingfactor* (pagerank[source] / outbounds[source]);
			}

			//System.out.println(']');

			temp[tempcounter++] = (((1.0 - dampingfactor) / (double) COUNTER) + tempval);
		}

		danglingPageRank = dampingfactor* (danglingPageRank / (double) COUNTER);

		if (rank == 0) {

			// double tempPageRank[] = new double[COUNTER];
			double danglingRecvBuf[] = new double[1];
			double newPageRank [] = new double[COUNTER];
			int temprank = 1;
			for (temprank = 1; temprank < size; temprank++) {
				MPI.COMM_WORLD.Recv(newPageRank, (temprank - 1) * partition,partition, MPI.DOUBLE, temprank, 89);
				MPI.COMM_WORLD.Recv(danglingRecvBuf, 0, 1, MPI.DOUBLE,temprank, 99);
				danglingPageRank += danglingRecvBuf[0];
			}
			
			//System.out.println(danglingPageRank);
			for(int i = (temprank - 1) * partition, y=0;i<newPageRank.length;i++,y++){
				newPageRank[i]=temp[y];
			}
			
			delta=0;
			 double diff = 0;
			for (int m = 0; m < newPageRank.length; m++){
				newPageRank[m] += danglingPageRank;
				diff = pagerank[m]-newPageRank[m];
				delta+= diff*diff;
			}
			System.arraycopy(newPageRank, 0, pagerank, 0, COUNTER);
			double deltaarray[]={delta};
			MPI.COMM_WORLD.Bcast(deltaarray, 0, 1, MPI.DOUBLE, 0);
			
			System.out.println("Current Iteration = "+ iteration+"  delta = "+delta);
			
		}
		// Send the temp pageranks back to the process with rank 0
		else {
			double danglingSendBuf[] = new double[1];
			danglingSendBuf[0] = danglingPageRank;
			double deltaarray[]=new double[1];
			
			MPI.COMM_WORLD.Send(temp, 0, temp.length, MPI.DOUBLE, 0, 89);
			MPI.COMM_WORLD.Send(danglingSendBuf, 0, 1, MPI.DOUBLE, 0, 99);
			MPI.COMM_WORLD.Bcast(deltaarray, 0, 1, MPI.DOUBLE, 0);
			delta = deltaarray[0];
		}

		// System.arraycopy(temp, 0, pagerank, 0, COUNTER);
	}

	if(rank == 0){
		
		compTime = System.currentTimeMillis()-startCompTime;
		
		BufferedWriter osw = new BufferedWriter(new FileWriter(outFilename));
		ArrayList<Double> arraylist = new ArrayList();
		ArrayList<Double> sortedlist;

		for (double i : pagerank)
			arraylist.add(new Double(i));

		sortedlist = (ArrayList<Double>) arraylist.clone();
		Collections.sort(sortedlist, Collections.reverseOrder());

		Iterator itr = sortedlist.iterator();
		int index = -1;
		int tempCntr = 0;
		Double x;
		
		System.out.println("The root process is writing rank values to file 'ParallelPageRank.output'.");
		
		double startIOTime = System.currentTimeMillis();
		while (itr.hasNext() && tempCntr++ < 10) {
			x = (Double) itr.next();
			index = arraylist.indexOf(x);
			arraylist.set(index, null);
			osw.write("Node:" + index + "  Pagerank:" + x.toString() + "\r\n");
		}
		osw.close();
		
		ioTime += (System.currentTimeMillis()-startIOTime);
		
		System.out.println("  **** MPI PageRank ****");
		System.out.println("Number of processes  = "+size);
		System.out.println("Input file           = "+inFilename);
		System.out.println("Total number of URLs = "+COUNTER);
		System.out.print("Number of iterations performed = "+iteration);
		if(iteration<numOfIterations)
			System.out.println(". The threshold was met.");
		else
			System.out.println("");
		System.out.println("Threshold            = "+threshold);
		if(printOutTime){
			System.out.println("I/O time             = "+ioTime+" ms");
			System.out.println("Computation time     = "+compTime+" ms");
		}
	}
	
		MPI.Finalize();
	}
}
