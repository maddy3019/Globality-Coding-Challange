
/**
 * This class implements an alogrithm for generating all frequent item sets of size 3 or more:
 * groups of 3 or more items that appear together in the transactions log at least as often as
 * the support level parameter value.
 * EX:
 * Given a value of sigma = 2, all sets of 3 items that appear 2 or more times together in the
 * transaction log should be returned.
 * @author Madhura Dole Ver 1.0 Oct 31, 2017 6:31:34 PM
 * 
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class SupermarketOptmization {

	/** map to keep frequency count of each itemset */
	public static HashMap<String, Integer> countKeeper;
	/** map each unique SKU to an index */
	public static HashMap<String, Integer> skuToIndexConverter;
	/** list holding list of indexes pointing to an SKU */
	public static ArrayList<ArrayList<Integer>> allTransactions;

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String fileName = args[0];
		int sigma = Integer.parseInt(args[1]);

		int count = processTransactionFile(fileName);

		String[] skuRetriever = computeFrequentItemsets(count);

		// generate output
		PrintWriter writer = new PrintWriter("Frequent Itemset.csv", "utf-8");
		// writing header
		writer.append("Itemset size" + "," + "Frequency" + "," + "Itemset\n");
		for (String m : countKeeper.keySet()) {
			if (countKeeper.get(m) >= sigma) {
				addToResult(m, skuRetriever, countKeeper.get(m), writer);
			}
		}
		writer.close();
	}

	/**
	 * @param count
	 * @return
	 */
	private static String[] computeFrequentItemsets(int count) {
		int noOfSKU = count;
		String[] skuRetriever = new String[noOfSKU];

		for (String sk : skuToIndexConverter.keySet()) {
			skuRetriever[skuToIndexConverter.get(sk)] = sk;
		}

		// hash map to keep count of each item-set of 3 or more items
		countKeeper = new HashMap<String, Integer>();

		for (ArrayList<Integer> transaction : allTransactions) {
			// optimization: generating associations only for transactions with 3 or more
			// SKUs
			if (transaction.size() >= 3) {
				insertAssociationsInHm(transaction, noOfSKU);
			}
		}
		return skuRetriever;
	}

	/**
	 * @param fileName
	 * @return
	 */
	private static int processTransactionFile(String fileName) {
		skuToIndexConverter = new HashMap<String, Integer>();

		allTransactions = new ArrayList<ArrayList<Integer>>();

		BufferedReader br = null;
		FileReader fr = null;
		int count = 0;

		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				// each SKU in each transaction lines
				String[] skuInTransaction = sCurrentLine.split(" ");

				// list to hold unique indexes for each SKU (starting from 0)
				ArrayList<Integer> currentTransactionIndexes = new ArrayList<Integer>();
				for (String sku : skuInTransaction) {
					if (!skuToIndexConverter.containsKey(sku)) {
						skuToIndexConverter.put(sku, count);
						currentTransactionIndexes.add(count);
						count++;
					} else {
						// get the index of sku already added in skuToIndexConverter
						// add this index to currentTransactionIndexes
						currentTransactionIndexes.add(skuToIndexConverter.get(sku));
					}
				}
				allTransactions.add(currentTransactionIndexes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * @param m
	 * @param skuRetriever
	 * @param integer
	 */
	private static void addToResult(String m, String[] skuRetriever, Integer frequency, PrintWriter writer) {
		// generate output file
		String[] indexes = m.split(",");
		String skuItemSet = "[";
		for (String index : indexes) {
			skuItemSet += skuRetriever[Integer.parseInt(index)] + ",";
		}
		skuItemSet = skuItemSet.substring(0, skuItemSet.length() - 1) + "]";
		writer.append(indexes.length + "," + frequency + "," + "\"" + skuItemSet + "\"" + "\n");
		System.out.println(indexes.length + "\t" + frequency + "\t" + skuItemSet + "\r");
	}

	/**
	 * @param arrayList
	 * @param countKeeper
	 * @param noOfSKU
	 */
	private static void insertAssociationsInHm(ArrayList<Integer> transaction, int noOfSKU) {
		// we generate all possible combinations of groups 3 or more items and add it to
		// countKeeper
		// if the itemset is already in countKepper, we simply increase it's count
		if (transaction.size() >= 3) {
			// compute for 2^n possible itemsets in each transaction of size n
			int max = 1 << transaction.size();
			for (int k = 0; k < max; k++) {
				String subset = convertIntToSet(k, transaction);
				String[] splitSubset = subset.split(",");
				if (splitSubset.length >= 3) {
					// allAssociation.add(subset);
					if (!countKeeper.containsKey(subset)) {
						countKeeper.put(subset, 1);
					} else {
						countKeeper.put(subset, countKeeper.get(subset) + 1);
					}
				}
			}
		}
	}

	/**
	 * @param k
	 * @param transaction
	 * @return
	 */
	private static String convertIntToSet(int x, ArrayList<Integer> transaction) {
		// char[] charArrayForSku = new char[transaction.size()];
		String str = "";
		int index = 0;
		for (int k = x; k > 0; k >>= 1) {
			if ((k & 1) == 1) {
				// charArrayForSku[index] = '1';
				str += String.valueOf(transaction.get(index)) + ",";
			}
			index++;
		}
		if (str == "") {
			return str;
		}
		return str.substring(0, str.length() - 1);
	}

}
