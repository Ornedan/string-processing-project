package fi.helsinki.cs.u.aitakang;

/**
 * [begin, end), begin < end.
 */
public class Range {
	/** Index of the first element in the range, inclusive. */
	public final int begin;
	
	/** Index of the last element in the range, exclusive. */
	public final int end;

	public Range(int begin, int end) {
		assert begin < end;
		
		this.begin = begin;
		this.end = end;
	}

	@Override
	public String toString() {
		return "Range [begin=" + begin + ", end=" + end + "]";
	}
}
