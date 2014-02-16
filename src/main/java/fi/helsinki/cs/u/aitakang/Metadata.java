package fi.helsinki.cs.u.aitakang;

public class Metadata <T> extends Range {
	// The data element
	public final T meta;

	public Metadata(int begin, int end, T meta) {
		super(begin, end);

		this.meta = meta;
	}

	@Override
	public String toString() {
		return "Metadata [meta=" + meta + ", begin=" + begin + ", end=" + end
				+ "]";
	}
}
