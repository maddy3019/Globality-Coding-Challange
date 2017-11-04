
/**
 * This class implements Apriori algorithm to compute frequent itemsets
 * @author Maddy Ver 1.2 Nov 1, 2017 11:45:00 PM
 * 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class SupermarketOptimization_Trial {

	// the list of current itemsets
	private static List<int[]> itemsets;
	// number of different items in the dataset
	private static int numItems;
	// total number of transactions in transaFile
	private static int numTransactions;
	// Writer instance to write to output file
	private static PrintWriter writer;
	// A minimal ’support level’ parameter, sigma – a positive integer
	private static int sigma;
	// variable to hold file path
	private static String transactionFile;
	// output header
	private static String outputHeader;

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		writer = new PrintWriter("Frequent Itemset.txt", "utf-8");
		outputHeader = "Itemset size" + "\t" + "Frequency" + "\t" + "Itemset";
		writeLog("Processing transaction file...", false);
		processFile(args);
		writeLog("Building associations...", false);
		startBuildingAssociations();
	}

	/**
	 * start Apriori algorithm
	 */
	private static void startBuildingAssociations() throws Exception {
		// start timer
		long start = System.currentTimeMillis();

		// first we generate the candidates of size 1
		createItemsetsOfSize1();
		int itemsetNumber = 1; // the current itemset being looked at
		int nbFrequentSets = 0;

		do {
			calculateFrequentItemsets();

			if (itemsets.size() != 0) {
				nbFrequentSets += itemsets.size();
				// writeLog("Found " + itemsets.size() + " frequent itemsets of size " +
				// itemsetNumber);
				createNewItemsetsFromPreviousOnes();
			}

			itemsetNumber++;
		} while (itemsets.size() > 0);

		// display the execution time
		long end = System.currentTimeMillis();
		// writeLog("Execution time is: " + ((double) (end - start) / 1000) + "
		// seconds.");
		// writeLog("Done");
	}

	/**
	 * 
	 */
	private static void createItemsetsOfSize1() {
		itemsets = new ArrayList<int[]>();
		for (int i = 0; i < numItems; i++) {
			int[] cand = { i };
			itemsets.add(cand);
		}
	}

	/**
	 * for size n of the current itemsets, generate all possible itemsets of size
	 * n+1 from pairs of current itemsets replaces the itemsets of itemsets by the
	 * new ones
	 */
	private static void createNewItemsetsFromPreviousOnes() {
		// by construction, all existing itemsets have the same size
		int currentSizeOfItemsets = itemsets.get(0).length;
		if (currentSizeOfItemsets > 1) {
			writeLog("Creating itemsets of size " + (currentSizeOfItemsets + 1) + " based on " + itemsets.size()
					+ " itemsets of size " + currentSizeOfItemsets, true);
			writeLog(outputHeader, true);
		}

		HashMap<String, int[]> tempCandidates = new HashMap<String, int[]>(); // temporary candidates

		// compare each pair of itemsets of size n-1
		for (int i = 0; i < itemsets.size(); i++) {
			for (int j = i + 1; j < itemsets.size(); j++) {
				int[] X = itemsets.get(i);
				int[] Y = itemsets.get(j);

				assert (X.length == Y.length);

				// make a string of the first n-2 tokens of the strings
				int[] newCand = new int[currentSizeOfItemsets + 1];
				for (int s = 0; s < newCand.length - 1; s++) {
					newCand[s] = X[s];
				}

				int ndifferent = 0;
				// find the missing value
				for (int s1 = 0; s1 < Y.length; s1++) {
					boolean found = false;
					// is Y[s1] in X?
					for (int s2 = 0; s2 < X.length; s2++) {
						if (X[s2] == Y[s1]) {
							found = true;
							break;
						}
					}
					if (!found) { // Y[s1] is not in X
						ndifferent++;
						// put the missing value at the end of newCand
						newCand[newCand.length - 1] = Y[s1];
					}

				}

				// find at least 1 different, to avoid having the same set twice in the existing
				// candidates
				assert (ndifferent > 0);

				if (ndifferent == 1) {
					// using Arrays.toString to reuse equals and hashcode of String
					Arrays.sort(newCand);
					tempCandidates.put(Arrays.toString(newCand), newCand);
				}
			}
		}

		// set the new itemsets
		itemsets = new ArrayList<int[]>(tempCandidates.values());
		// writeLog("Created " + itemsets.size() + " unique itemsets of size " +
		// (currentSizeOfItemsets + 1));
	}

	/**
	 * passes through the data to measure the frequency of sets in {@link itemsets},
	 * then filters the ones that have frequency greater than or equal to sigma
	 * 
	 * @throws IOException
	 */
	private static void calculateFrequentItemsets() throws IOException {
		// writeLog("Passing through the data to compute the frequency of " +
		// itemsets.size() + " itemsets of size "
		// + itemsets.get(0).length);

		List<int[]> frequentCandidates = new ArrayList<int[]>(); // the frequent candidates for the current itemset

		boolean match; // whether the transaction has all the items in an itemset
		int count[] = new int[itemsets.size()]; // the number of successful matches, initialized by zeros

		// load the transaction file
		BufferedReader data_in = new BufferedReader(new InputStreamReader(new FileInputStream(transactionFile)));

		boolean[] trans = new boolean[numItems];

		// for each transaction
		for (int i = 0; i < numTransactions; i++) {

			String line = data_in.readLine();
			line2booleanArray(line, trans);

			// check each candidate
			for (int c = 0; c < itemsets.size(); c++) {
				match = true; // reset match to false
				// tokenize the candidate so that we know what items need to be present for a
				// match
				int[] cand = itemsets.get(c);
				// check each item in the itemset to see if it is present in the transaction
				for (int xx : cand) {
					if (trans[xx] == false) {
						match = false;
						break;
					}
				}
				if (match) { // if at this point it is a match, increase the count
					count[c]++;
				}
			}
		}

		data_in.close();

		for (int i = 0; i < itemsets.size(); i++) {
			// if frequency is greater than sigma, then add to the candidate to
			// the frequent candidates
			if (count[i] >= sigma) {
				foundFrequentItemSet(itemsets.get(i), count[i]);
				frequentCandidates.add(itemsets.get(i));
			}
			// else log("-- Remove candidate: "+ Arrays.toString(candidates.get(i)) + " is:
			// "+ ((count[i] / (double) numTransactions)));
		}

		// new candidates are only the frequent candidates
		itemsets = frequentCandidates;

	}

	/**
	 * executes if a frequent item set has been found
	 * 
	 * @param is
	 * @param i
	 */
	private static void foundFrequentItemSet(int[] itemset, int frequency) {
		if (itemset.length >= 3 && frequency >= 4) {
			String toPrint = itemset.length + "\t" + frequency + "\t" + Arrays.toString(itemset);
			writeLog(toPrint, true);
		}
	}

	/**
	 * put "true" in trans[i] if the integer i is in line
	 * 
	 * @param line
	 * @param trans
	 */
	private static void line2booleanArray(String line, boolean[] trans) {
		Arrays.fill(trans, false);
		StringTokenizer stFile = new StringTokenizer(line, " "); // read a line from the file to the tokenizer
		// put the contents of that line into the transaction array
		while (stFile.hasMoreTokens()) {

			int parsedVal = Integer.parseInt(stFile.nextToken());
			trans[parsedVal] = true; // if it is not a 0, assign the value to true
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private static void processFile(String[] args) throws NumberFormatException, IOException {

		// setting transaction file
		if (args.length != 0)
			transactionFile = args[0];
		else
			throw new FileNotFoundException();

		// setting initial value for sigma
		sigma = 2;
		if (args.length >= 2)
			sigma = Integer.parseInt(args[1]);

		// going thourgh the file to compute numItems and numTransactions
		numItems = 0;
		numTransactions = 0;
		BufferedReader data_in = new BufferedReader(new FileReader(transactionFile));
		while (data_in.ready()) {
			String line = data_in.readLine();
			if (line.matches("\\s*"))
				continue; // be friendly with empty lines
			numTransactions++;
			StringTokenizer t = new StringTokenizer(line, " ");
			while (t.hasMoreTokens()) {
				int x = Integer.parseInt(t.nextToken());
				// log(x);
				if (x + 1 > numItems)
					numItems = x + 1;
			}
		}
	}

	/**
	 * @param string
	 */
	private static void writeLog(String string, boolean writeInFile) {
		System.out.println(string);
		if (writeInFile)
			writer.append(string + "\n");
	}

}
