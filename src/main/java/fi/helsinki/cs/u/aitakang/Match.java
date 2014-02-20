package fi.helsinki.cs.u.aitakang;

/**
 * A match is an interval of the suffix array containing the starting points
 * of all the matched substrings, and the length of the matching substring.
 */
public class Match extends Range {

	/** Length of the match. */
	public final int length;
	
	public Match(int begin, int end, int length) {
		super(begin, end);
		
		this.length = length;
	}

	@Override
	public String toString() {
		return "Match [begin=" + begin + ", end=" + end + ", length=" + length
				+ "]";
	}
}
