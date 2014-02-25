package fi.helsinki.cs.u.aitakang;

/**
 * End-point data structure composed of one end of a range, inclusive, and
 * a pointer to the range structure.
 * 
 * Should be an inner class of IntervalTree, but Java generics don't play
 * nice with arrays.
 */
public class Endpoint {
	final int point;
	final Range range;
	
	public Endpoint(int point, Range range) {
		this.point = point;
		this.range = range;
	}

	@Override
	public String toString() {
		return "Endpoint [point=" + point + ", range=" + range + "]";
	}
}