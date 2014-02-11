package fi.helsinki.cs.u.aitakang;

public class Metadata <T> {
	// Index of the first character the metadata is attached to, inclusive
	public final int begin;
	
	// Index of the last character the metadata is attached to, exclusive
	public final int end;
	
	// The data element itself
	public final T meta;

	public Metadata(int begin, int end, T meta) {
		this.begin = begin;
		this.end = end;
		this.meta = meta;
	}
}
