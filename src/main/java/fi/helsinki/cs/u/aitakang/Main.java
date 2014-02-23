package fi.helsinki.cs.u.aitakang;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sais.sais;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main {
	
	/** Number of times a query is repeated before starting the timing. */
	private static final int PREPARATION_REPEATS = 1;
	
	/** Number of times a query is executed during a timing test. */
	private static final int QUERY_REPEATS = 10;

	
	public static void main(String[] args) throws Throwable {
		String pathToText = args[0];
		String pathToMetas = args[1];
		String pathToQueries = args[2];
		
		// Load the sample file into a string
		String text = 
				new String(
				Files.readAllBytes(Paths.get(pathToText)),
				StandardCharsets.UTF_8) + Searches.EOT;
		
		// Construct the suffix array and other data structures
		int[] sa = makeSuffixArray(text);
		BackwardsSearchData bsd = makeBSD(text, sa);
		
		// Load the metadata spec
		Type metaListType =
				new TypeToken<List<Metadata<Integer>>>() {}.getType();
		List<Metadata<Integer>> metas = new Gson().fromJson(new FileReader(
				pathToMetas), metaListType);
		
		// Load the queries
		Type queryListType =
				new TypeToken<List<QuerySpec>>() {}.getType();
		List<QuerySpec> queries = new Gson().fromJson(new FileReader(
				pathToQueries), queryListType);
		
		
		// Process the metadatas into an interval tree, reporting timing
		IntervalTree<Metadata<Integer>> metaTree = makeMetaTree(metas);
		
		// Run the queries on the data, reporting results
		for(QuerySpec query: queries)
			testQuery(text, sa, bsd, metaTree, query);
	}
	

	private static int[] makeSuffixArray(String text) {
		System.out.println("Suffix array construction started.");
		
		// Encourage garbage collection before timing an operation
		System.gc();
		
		long start = System.currentTimeMillis();
		
		int[] sa = new int[text.length()];
		sais.suffixsort(text, sa, text.length());
		
		long stop = System.currentTimeMillis();
		
		System.out.printf("Suffix array construction done, took %dms\n", stop - start);
		
		return sa;
	}
	
	private static BackwardsSearchData makeBSD(String text, int[] sa) {
		System.out.println("Auxiliary preprocessed data construction started.");
		
		// Encourage garbage collection before timing an operation
		System.gc();
		
		long start = System.currentTimeMillis();
		
		BackwardsSearchData bsd = new BackwardsSearchData(text, sa);
		
		long stop = System.currentTimeMillis();
		
		System.out.printf("Auxiliary preprocessed data construction done, took %dms\n", stop - start);
		
		return bsd;
	}
	
	private static IntervalTree<Metadata<Integer>> makeMetaTree(
			List<Metadata<Integer>> metas) {
		System.out.println("Interval tree construction started.");
		
		// Encourage garbage collection before timing an operation
		System.gc();
		
		long start = System.currentTimeMillis();
		
		IntervalTree<Metadata<Integer>> metaTree = new IntervalTree<>(metas);
		
		long stop = System.currentTimeMillis();
		
		System.out.printf("Interval tree construction done, took %dms\n", stop - start);
		
		return metaTree;
	}


	/**
	 * Run timing test on the given query.
	 */
	private static void testQuery(String text, int[] sa, BackwardsSearchData bsd,
			IntervalTree<Metadata<Integer>> metaTree, QuerySpec query) {
		System.out.println("Testing query " + query);
		
		// Run a few thousand repetitions first to hopefully get JIT done
		for(int i = 0; i < PREPARATION_REPEATS; i++)
			searchMetadata(sa, metaTree, searchText(text, sa, bsd, metaTree, query));
		
		// Encourage GC so it will interfere with the actual test less
		System.gc();
		
		// Timing test - text search
		List<? extends Match> textMatches = null;
		long start = System.currentTimeMillis();
		
		for(int i = 0; i < QUERY_REPEATS; i++)
			textMatches = searchText(text, sa, bsd, metaTree, query);
		
		long stop = System.currentTimeMillis();
		
		long time = stop - start;
		System.out.printf("Text search testing complete %.2fms per query, %dms total, %d matches.\n",
				time * 1.0 / QUERY_REPEATS, time, countMatches(textMatches));
		
		// Timing test - metadata search
		System.gc();
		
		List<QueryResult> results = null;
		start = System.currentTimeMillis();
		
		for(int i = 0; i < QUERY_REPEATS; i++)
			results = searchMetadata(sa, metaTree, textMatches);
		
		stop = System.currentTimeMillis();
		
		time = stop - start;
		System.out.printf("Metadata search testing complete %.2fms per query, %dms total.\n",
				time * 1.0 / (QUERY_REPEATS * results.size()), time);
		
		
		// Show the results
		/*
		System.out.println("Matches: ");
		for(QueryResult result: results) {
			for(int i = result.match.begin; i < result.match.end; i++) {
				System.out.printf(" '%s'\n", text.substring(sa[i],
						Math.min(sa[i] + result.match.length, text.length())));
			}
			System.out.println(result.metas);
			System.out.println();
		}
		*/
	}
	
	
	private static int countMatches(List<? extends Match> matches) {
		int count = 0;
		for(Match match: matches)
			count += match.end - match.begin;
		return count;
	}


	/**
	 * Call the appropriate Searches method for the given query.
	 * @return 
	 */
	private static List<? extends Match> searchText(String text, int[] sa,
			BackwardsSearchData bsd, IntervalTree<Metadata<Integer>> metaTree,
			QuerySpec query) {
		List<? extends Match> matches = null;
		
		// Find suffix array hit range
		switch(query.type) {
		case backwards:
			if(query.isInexact)
				matches = Searches.inexactBackwardsSearch(text, sa, bsd,
						query.threshold, query.query);
			else
				matches = Collections.singletonList(Searches.backwardsSearch(text, sa, bsd, query.query));
			break;
			
		case binary:
			if(query.isInexact)
				matches = Searches.inexactBinarySearch(text, sa, bsd,
						query.threshold, query.query);
			else
				matches = Collections.singletonList(Searches.binarySearch(text, sa, query.query));
			break;
		}
		
		return matches;
	}
	
	private static List<QueryResult> searchMetadata(int[] sa,
			IntervalTree<Metadata<Integer>> metaTree,
			List<? extends Match> matches) {
		// Find metadatas for each of the actual text hits
		List<QueryResult> results = new ArrayList<>(matches.size());
		for (Match match : matches) {
			List<Metadata<Integer>> metas = new ArrayList<>();

			for (int i = match.begin; i < match.end; i++) {
				metas.addAll(metaTree.find(new Range(sa[i], sa[i]
						+ match.length)));
			}

			results.add(new QueryResult(match, metas));
		}

		return results;
	}
	
	/**
	 * One matching range in the suffix array and the metadatas associated
	 * with the matching strings.
	 */
	private static class QueryResult {
		final Match match;
		final List<Metadata<Integer>> metas;
		
		public QueryResult(Match match, List<Metadata<Integer>> metas) {
			this.match = match;
			this.metas = metas;
		}
	}
	
	// Query test specification stuff
	
	public enum QueryType { binary, backwards };
	
	public static class QuerySpec {
		final String query;
		final QueryType type;
		final boolean isInexact;
		final int threshold;
		
		public QuerySpec(String query, QueryType type, boolean isInexact,
				int threshold) {
			this.query = query;
			this.type = type;
			this.isInexact = isInexact;
			this.threshold = threshold;
		}

		@Override
		public String toString() {
			return "QuerySpec [query=" + query + ", type=" + type
					+ ", isInexact=" + isInexact + ", threshold=" + threshold
					+ "]";
		}
	}
}
