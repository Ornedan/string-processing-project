package fi.helsinki.cs.u.aitakang;

public class Match extends Range {

	/** Length of the inexact match. */
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
