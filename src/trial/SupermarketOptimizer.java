package trial;

/**
 * @author Maddy
 * Ver 1.0 Nov 3, 2017 10:13:54 AM
 * 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SupermarketOptimizer {
	public static void main(String[] args) {
		AprioriFrequentItemsetGenerator<String> generator = new AprioriFrequentItemsetGenerator<>();

		String fileName = args[0];
		int sigma = Integer.parseInt(args[1]);

		// list holding list of indexes pointing to an SKU
		List<Set<String>> allTransactions = new ArrayList<>();

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

				// adding HashSet of all SKUs in current transaction
				allTransactions.add(new HashSet<>(Arrays.asList(skuInTransaction)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		FrequentItemsetData<String> data = generator.generate(allTransactions, sigma);
		int i = 1;

		for (Set<String> itemset : data.getFrequentItemsetList()) {
			System.out.printf("%2d: %9s, support: %1.1f\n", i++, itemset, data.getSupport(itemset));
		}
	}
}
