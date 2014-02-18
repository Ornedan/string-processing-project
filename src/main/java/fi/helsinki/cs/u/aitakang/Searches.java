package fi.helsinki.cs.u.aitakang;

import java.util.ArrayList;
import java.util.List;

public class Searches {

	public static final char EOT = '\u0003';

	public static Match binarySearch(String text, int[] sa, String pattern) {
		int lo = 0;
		int hi = sa.length - 1;

		for(int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);

			// Find new lower bound
			int lolo = lo;
			int lohi = hi;
			while(lohi >= lolo) {
				int mid = (lolo + lohi) / 2;
				char cmid = text.charAt(sa[mid] + i);

				// Found lowest included?
				if(cmid == c && (mid == lo || text.charAt(sa[mid - 1] + i) < c)) {
					lo = mid;
					break;
				}

				if(cmid < c)
					lolo = mid + 1;
				else
					lohi = mid - 1;
			}

			// No lower bound, no matches
			if(lohi < lolo)
				return null;

			// Find new upper bound
			int hilo = lo;
			int hihi = hi;
			while(hihi >= hilo) {
				int mid = (hilo + hihi) / 2;
				char cmid = text.charAt(sa[mid] + i);

				// Found lowest included?
				if (cmid == c
						&& (mid == hi || text.charAt(sa[mid + 1] + i) > c)) {
					hi = mid;
					break;
				}

				if (cmid <= c)
					hilo = mid + 1;
				else
					hihi = mid - 1;
			}

			// No upper bound, no matches
			if (hihi < hilo)
				return null;
		}

		return new Match(lo, hi + 1, pattern.length());
	}

	public static Match backwardsSearch(String text, int[] sa,
			BackwardsSearchData bsd, String pattern) {
		int lo = 0;
		int hi = sa.length;

		for(int i = pattern.length() - 1; i >= 0; i--) {
			char c = pattern.charAt(i);

			// Character point greater than any in the string? No matches
			if(c >= bsd.counts.length)
				return null;

			lo = bsd.counts[c] + bsd.rank(c, lo);
			hi = bsd.counts[c] + bsd.rank(c, hi);

			// Range closed, no matches
			if(lo == hi)
				return null;
		}

		return new Match(lo, hi, pattern.length());
	}

	
	public static List<InexactMatch> inexactBinarySearch(String text, int[] sa,
			BackwardsSearchData bsd, int limit, String pattern) {
		List<InexactMatch> results = new ArrayList<>();
		
		inexactBinarySearch(text, sa, bsd, pattern, limit, results, 0, 0, 0, 0,
				sa.length - 1, "");
		
		return results;
	}
	
	private static void inexactBinarySearch(String text, int[] sa,
			BackwardsSearchData bsd, String pattern, int limit,
			List<InexactMatch> results, int pos, int diff, int length, int lo,
			int hi, String spec) {
		// Stop looking if the difference exceeds the limit
		if(diff > limit)
			return;
		
		// We've reached end of the pattern, record a match
		if(pos == pattern.length()) {
			results.add(new InexactMatch(lo, hi + 1, length, spec));
			return;
		}
		
		
		// Process each character in the alphabet separately. The ranges at
		// next step after a replacement or insertion (or match) may not be
		// contiguous.
		for(char c: bsd.alphabet) {
			// Try finding new bounds
			int newLo = lo;
			int newHi = hi;
			
			int lolo = lo;
			int lohi = hi;
			while(lohi >= lolo) {
				int mid = (lolo + lohi) / 2;
				// For the purposes of inexact searching, there are infinite EOT
				// characters after end of actual text 
				char cmid = sa[mid] + length < text.length() ? text.charAt(sa[mid] + length) : EOT;
	
				if(cmid == c && (mid == lo || (sa[mid - 1] + length < text.length() ? text.charAt(sa[mid - 1] + length) : EOT) < c)) {
					newLo = mid;
					break;
				}
	
				if(cmid < c)
					lolo = mid + 1;
				else
					lohi = mid - 1;
			}
			
			int hilo = lo;
			int hihi = hi;
			while(hihi >= hilo) {
				int mid = (hilo + hihi) / 2;
				char cmid = sa[mid] + length < text.length() ? text.charAt(sa[mid] + length) : EOT;
	
				if(cmid == c && (mid == hi || (sa[mid + 1] + length < text.length() ? text.charAt(sa[mid + 1] + length) : EOT) > c)) {
					newHi = mid;
					break;
				}
	
				if(cmid <= c)
					hilo = mid + 1;
				else
					hihi = mid - 1;
			}
			
			// There are extensions of current prefix with c
			if(lolo <= lohi && hilo <= hihi) {
				// Match
				if(c == pattern.charAt(pos)) {
					inexactBinarySearch(text, sa, bsd, pattern, limit, results,
							pos + 1, diff, length + 1, newLo, newHi, spec + "M");
				}
				// Replacement
				else {
					inexactBinarySearch(text, sa, bsd, pattern, limit, results,
							pos + 1, diff + 1, length + 1, newLo, newHi, spec + "R");
				}
		
				// Insertion
				inexactBinarySearch(text, sa, bsd, pattern, limit, results,
						pos, diff + 1, length + 1, newLo, newHi, spec + "I");				
			}
		}
		
		// Deletion
		inexactBinarySearch(text, sa, bsd, pattern, limit, results,
				pos + 1, diff + 1, length, lo, hi, spec + "D");
	}
	
	
	public static List<InexactMatch> inexactBackwardsSearch(String text,
			int[] sa, BackwardsSearchData bsd, int limit, String pattern) {
		List<InexactMatch> results = new ArrayList<>();
		
		inexactBackwardsSearch(text, sa, bsd, pattern, limit, results,
				pattern.length() - 1, 0, 0, 0, sa.length, "");
		
		return results;
	}
	
	private static void inexactBackwardsSearch(String text, int[] sa,
			BackwardsSearchData bsd, String pattern, int limit,
			List<InexactMatch> results, int pos, int diff, int length, int lo,
			int hi, String spec) {
		// Prune branches that exceed the limit
		if(diff > limit)
			return;
		
		// Reaching the start of the pattern => match
		if(pos < 0) {
			results.add(new InexactMatch(lo, hi, length, spec));
			return;
		}
		
		// For match, replacement and insertion, each character in the alphabet
		// needs to be processed separately, since their ranges in the suffix
		// array can be disjoint.
		// So we need to handle separately each case that it was a particular
		// character in the current position.
		for(char c: bsd.alphabet) {
			int newLo = bsd.counts[c] + bsd.rank(c, lo);
			int newHi = bsd.counts[c] + bsd.rank(c, hi);
			
			// There are some suffixes of the form c + current suffix
			if(newLo != newHi) {
				if(c == pattern.charAt(pos)) {
					// Match
					inexactBackwardsSearch(text, sa, bsd, pattern, limit,
							results, pos - 1, diff, length + 1, newLo, newHi,
							"M" + spec);
				}
				else {
					// Replacement
					inexactBackwardsSearch(text, sa, bsd, pattern, limit,
							results, pos - 1, diff + 1, length + 1, newLo, newHi,
							"R" + spec);
				}
				
				// Insertion
				inexactBackwardsSearch(text, sa, bsd, pattern, limit,
						results, pos, diff + 1, length + 1, newLo, newHi,
						"I" + spec);
			}
		}
		
		// Deletion
		inexactBackwardsSearch(text, sa, bsd, pattern, limit,
				results, pos - 1, diff + 1, length, lo, hi,
				"D" + spec);
	}
}
