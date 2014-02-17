package fi.helsinki.cs.u.aitakang;

public class InexactMatch extends Match {
	
	/** String specifying edit operations leading to this match. */
	public final String spec;

	public InexactMatch(int begin, int end, int length, String spec) {
		super(begin, end, length);
		
		this.spec = spec;
	}

	@Override
	public String toString() {
		return "InexactMatch [length=" + length + ", begin=" + begin + ", end="
				+ end + ", spec=" + spec + "]";
	}	
}
