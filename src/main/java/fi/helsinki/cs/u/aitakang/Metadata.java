package fi.helsinki.cs.u.aitakang;

/**
 * A range of text augmented with the metadata that is associated with that
 * range.
 */
public class Metadata <T> extends Range {
	/** The data element. */
	public final T value;

	public Metadata(int begin, int end, T meta) {
		super(begin, end);

		this.value = meta;
	}

	@Override
	public String toString() {
		return "Metadata [value=" + value + ", begin=" + begin + ", end=" + end
				+ "]";
	}
}
