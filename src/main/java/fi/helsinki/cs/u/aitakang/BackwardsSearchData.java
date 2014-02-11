package fi.helsinki.cs.u.aitakang;

import java.util.Arrays;
import java.util.TreeSet;

public class BackwardsSearchData {

	public static final int BLOCK_SIZE = 1024;

	// Count of lesser characters in the string
	public final int[] counts;
	
	// Burrows-Wheeler Transform of the text
	protected final char[] bwt;
	
	// Character-count-in-BWT lookup blocks
	protected final int[][] blocks;
	
	
	public BackwardsSearchData(String text, int[] sa) {
		TreeSet<Character> alphabet = new TreeSet<Character>();
		for(char c: text.toCharArray())
			alphabet.add(c);
		
		this.counts = lesserThanCounts(text, sa, alphabet);
		this.bwt = burrowsWheelerTransform(text, sa);
		
		// Calculate lookup blocks
		this.blocks = new int[sa.length / BLOCK_SIZE + (sa.length % BLOCK_SIZE == 0 ? 0 : 1)][];
		
		int[] running = new int[alphabet.last() + 1];
		for(int i = 0; i < bwt.length; i++) {
			if(i % BLOCK_SIZE == 0)
				this.blocks[i / BLOCK_SIZE] = Arrays.copyOf(running, running.length);
			running[bwt[i]] += 1;
		}
		
		this.blocks[this.blocks.length - 1] = running;
	}
	
	public int rank(char c, int n) {
		int rank = 0;
		if(n >= BLOCK_SIZE)
			rank = blocks[n / BLOCK_SIZE][c];
		
		for(int i = (n / BLOCK_SIZE) * BLOCK_SIZE; i < n; i++) {
			if(bwt[i] == c)
				rank += 1;
		}
		
		return rank;
	}
	
	
	
	public static char[] burrowsWheelerTransform(String text, int[] sa) {
		char[] bwt = new char[sa.length];
		for(int i = 0; i < sa.length; i++)
			bwt[i] = sa[i] == 0 ? Searches.EOT : text.charAt(sa[i] - 1);
		
		return bwt;
	}
	
	public static int[] lesserThanCounts(String text, int[] sa, TreeSet<Character> alphabet) {
		int[] counts = new int[alphabet.last() + 1];
		for(int i = 0, n = 0; n < counts.length; n++) {
			char c = (char)n;
			
			while(i < sa.length && text.charAt(sa[i]) < c)
				i += 1;
			
			counts[n] = i;
		}
		
		return counts;
	}
}
