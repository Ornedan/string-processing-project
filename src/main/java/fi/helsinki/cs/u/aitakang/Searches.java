package fi.helsinki.cs.u.aitakang;


public class Searches {
	
	public static final char EOT = '\u0003';
	
	
	public static Range binarySearch(String text, int[] sa, String pattern) {
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
				if(cmid == c && (mid == hi || text.charAt(sa[mid + 1] + i) > c)) {
					hi = mid;
					break;
				}
				
				if(cmid <= c)
					hilo = mid + 1;
				else
					hihi = mid - 1;
			}
			
			// No upper bound, no matches
			if(hihi < hilo)
				return null;
		}
		
		return new Range(lo, hi + 1);
	}
	
	public static Range backwardsSearch(String text, int[] sa, BackwardsSearchData bsd, String pattern) {
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
		
		return new Range(lo, hi);
	}
	
	protected static final class Range {
		public final int begin; // Inclusive
		public final int end;   // Exclusive
		
		public Range(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}

		@Override
		public String toString() {
			return "Range [begin=" + begin + ", end=" + end + "]";
		}
	}
}
