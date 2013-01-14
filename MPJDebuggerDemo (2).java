import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mpi.*;

public class MPJDebuggerDemo {
	private static int COUNTER = 1000;

	
	public static void main(String args[]) throws FileNotFoundException,
			IOException {

		String[] status = MPI.Init(args);

		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		if (rank == 0) {
			// .......................................................................

			// INITIALIZING DATA STRUCTURES

			HashMap<Integer, Vector<Integer>> hash = new HashMap<Integer, Vector<Integer>>();

			if (args.length < 4)
				System.out
						.println("USAGE: java PageRankSeqGroup8 [inputfile] [outputfile] [number of iterations] [damping factor]");

			double dampingfactor = 0.85;//Double.parseDouble(args[3]);
			if (0 > dampingfactor && dampingfactor > 1) {
				System.out
						.print("damping factor should be a positive number less than or equal to 1");
				System.exit(0);
			}

			int numOfIterations = 1;//Integer.parseInt(args[2]);

			//Done getting inputs from the command line.
			//Read the file.
			String outputfile = args[1];
			FileReader dis = new FileReader("src\\pagerank.input");
			BufferedReader br = new BufferedReader(dis);

			String strng;
			Vector<String[]> bucket = new Vector();
			String strarray[];

			for (COUNTER = 0; (strng = br.readLine()) != null; COUNTER++) {
				strarray = strng.split(" ");
				bucket.add(strarray);
			}

			double pagerank[] = new double[COUNTER];
			int outbounds[] = new int[COUNTER];
			Vector<Integer> inbounds[] = new Vector[COUNTER];
			////Done reading the file
			
			//Populate the relevant data structures
			
			for (int i = 0; i < inbounds.length; i++)
				inbounds[i] = new Vector<Integer>();

			Iterator itr0;
			itr0 = bucket.iterator();
			int node = -1;
			while (itr0.hasNext()) {
				strarray = (String[]) itr0.next();
				node = Integer.parseInt(strarray[0]);
				for (int i = 1; i < strarray.length; i++)
					inbounds[Integer.parseInt(strarray[i])].add(new Integer(
							node));

				hash.put(new Integer(node), inbounds[node]);
				outbounds[Integer.parseInt(strarray[0])] = strarray.length - 1;
			}

			bucket = null; // Free the memory for the bucket.
			itr0 = null;
			
			
			double temp[] = new double[COUNTER];

			for (int i = 0; i < pagerank.length; i++)
				pagerank[i] = 1D / (double) COUNTER; // since there is an equal
														// probability
			// of visiting each page
			
			//Done populating datastructures.

			/*
			 * List<Integer> partitionInfo = new ArrayList<Integer>(); Iterator
			 * hashItr, vectorItr; hashItr = hash.entrySet().iterator();
			 * 
			 * Vector<Integer> inbounds2; Map.Entry<Integer, Vector<Integer>>
			 * pair1; int source1 = -1; int tempval1 = 0; int nodecounter =0 ;
			 * 
			 * 
			 * for (int processRank = 0; processRank < 4; processRank++) { while
			 * (hashItr.hasNext()) {
			 * 
			 * if((nodecounter++%250)==0) break;
			 * 
			 * pair1 = (Map.Entry<Integer, Vector<Integer>>) hashItr.next();
			 * inbounds2 = pair1.getValue(); vectorItr = inbounds2.iterator();
			 * while (vectorItr.hasNext()) {
			 * 
			 * Integer x = (Integer) vectorItr.next(); if
			 * (!partitionInfo.contains(x)) partitionInfo.add(x); } } }
			 */

			HashMap<Integer, Vector<Integer>> hash1[] = new HashMap[3];
			
			
			String counterStrng[] = { Integer.toString(COUNTER) };

			// send the size. THIS IS USELESS FOR NOW
			int partition = COUNTER / size;
			int lastpartition = COUNTER % size + partition;

			int rankCounter = 1;
			int nodeCounter = 0;
			itr0=hash.entrySet().iterator();
			Map.Entry<Integer, Vector<Integer>> pair;
			
			for (;rankCounter <size; rankCounter++){
				HashMap tempMap = new HashMap();
				for(nodeCounter=0;nodeCounter<partition&&itr0.hasNext();nodeCounter++){
					pair = (Map.Entry<Integer, Vector<Integer>>) itr0.next();
					tempMap.put(pair.getKey(), pair.getValue());
				}
				hash1[rankCounter-1]=tempMap;
			}
			
			for (int rank1 = 1; rank1 <= 3; rank1++) {
				MPI.COMM_WORLD.Send(counterStrng, 0, 1, MPI.OBJECT, rank1, 67);
				MPI.COMM_WORLD.Send(outbounds, 0, COUNTER, MPI.INT, rank1, 68);
				MPI.COMM_WORLD
						.Send(pagerank, 0, COUNTER, MPI.DOUBLE, rank1, 70);
				MPI.COMM_WORLD.Send(hash1, rank1-1, 1, MPI.OBJECT, rank1, 69);
			}
			// MPI.COMM_WORLD.Send(string, 0, string.length, MPI.OBJECT, 1, 78);
		}

		else{

			//RECEIVE relevant data
			
			int numOfIterations = 1;
			
			double dampingfactor=0.85;
			
			System.out.println("I am rank:" + rank);
			String strngCounter[] = new String[1];
			MPI.COMM_WORLD.Recv(strngCounter, 0, 1, MPI.OBJECT, 0, 67);

			final int COUNTER = Integer.parseInt(strngCounter[0]);
			System.out.println(COUNTER);

			int partition = COUNTER / size;

			int outbounds[] = new int[COUNTER];
			MPI.COMM_WORLD.Recv(outbounds, 0, outbounds.length, MPI.INT, 0, 68);
			for (int x : outbounds)
				System.out.print(x);

			double pagerank[] = new double[COUNTER];
			MPI.COMM_WORLD.Recv(pagerank, 0, COUNTER, MPI.DOUBLE, 0, 70);

			HashMap[] hash1 = new HashMap[1];
			MPI.COMM_WORLD.Recv(hash1, 0, 1, MPI.OBJECT, 0, 69);
			HashMap<Integer,Vector<Integer>>hash = hash1[0];
			
			double temp[] = new double[hash.keySet().size()];
//...........Done receiving data..................................................
			
			Iterator itr, itr1;
			int target = -1, source = -1;
			double tempval = 0, danglingPageRank = 0;
			Vector<Integer> inbounds1;
			Map.Entry<Integer, Vector<Integer>> pair;

				// System.out.println("\nIteration number:" + (i + 1));

				danglingPageRank = 0;

				itr = hash.entrySet().iterator();
				
				while (itr.hasNext()) {
					pair = (Map.Entry<Integer, Vector<Integer>>) itr.next();
					target = pair.getKey().intValue();

					System.out.print("target node:" + target
							+ " inbound array:[");

					if (outbounds[target] == 0)
						danglingPageRank += pagerank[target];

					tempval = 0;

					inbounds1 = pair.getValue();
					itr1 = inbounds1.iterator();
					while (itr1.hasNext()) {
						source = ((Integer) itr1.next()).intValue();

						System.out.print(" " + source + " ");

						tempval += dampingfactor
								* (pagerank[source] / outbounds[source]);
					}

					System.out.println(']');

					temp[target] = (((1.0 - dampingfactor) / (double) COUNTER) + tempval);
				}

				danglingPageRank = dampingfactor
						* (danglingPageRank / (double) COUNTER);
				for (int m = 0; m < temp.length; m++)
					temp[m] = temp[m] + danglingPageRank;

				System.out.println("Page ranks:");
				for (int m = 0; m < temp.length; m++)
					System.out.println("node:" + m + ", old:" + pagerank[m]
							+ ", new:" + temp[m]);

				System.arraycopy(temp, 0, pagerank, 0, COUNTER);
			

		}
		MPI.Finalize();
	}
}
