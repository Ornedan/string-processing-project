package fi.helsinki.cs.u.aitakang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntervalTree<T extends Range> {
	
	/** Root of the interval nodes tree. */
	protected final Node root;
	
	/** List of included interval endpoints, inclusive, sorted ascending. */
	// Has to be a List type instead of an array because how Java generics
	// work - arrays of Endpoint cannot be created.
	protected final ArrayList<Endpoint> endpoints;
	
	
	public IntervalTree(List<T> metas) {
		this.root = mkNode(metas);
		this.endpoints = mkEndpoints(metas);
	}

	/**
	 * Recursively construct the interval tree nodes.
	 */
	protected Node mkNode(List<T> elems) {
		// No elements left? No node structure here
		if(elems.isEmpty())
			return null;
		
		// Select a point, hopefully one that generates a balanced tree
		int center = (minBegin(elems) + maxEnd(elems)) / 2;
		
		List<T> toLeft = new ArrayList<>();
		List<T> toRight = new ArrayList<>();
		List<T> overlap = new ArrayList<>();
		
		// Split the intervals to ones entirely before that point, ones entirely
		// after that point and ones that contain the point.
		for(T elem: elems) {
			if(elem.begin <= center) {
				if(elem.end <= center) // End-point is exclusive, so if center = end, center is not in the range
					toLeft.add(elem);
				else
					overlap.add(elem);
			}
			else {
				toRight.add(elem);
			}
		}
		
		// Recursively handle the intervals that didn't contain the center point.
		Node left = mkNode(toLeft);
		Node right = mkNode(toRight);
		
		return new Node(center, left, right, overlap);
	}
	
	/**
	 * Gather the metadata endpoints into a sorted array.
	 */
	protected ArrayList<Endpoint> mkEndpoints(List<T> ranges) {
		ArrayList<Endpoint> endpoints = new ArrayList<>();
		
		for(T range: ranges) {
			endpoints.add(new Endpoint(range.begin, range));
			// Range end-point is exclusive, but we only want inclusions in endpoints
			endpoints.add(new Endpoint(range.end - 1, range));
		}
		
		Collections.sort(endpoints, new Comparator<Endpoint>() {
			@Override
			public int compare(Endpoint o1, Endpoint o2) {
				return Integer.compare(o1.point, o2.point);
			}
		});
		
		return endpoints;
	}
	
	/**
	 * Find the metadata elements that overlap the given range.
	 */
	public List<T> find(Range range) {
		// Construct results in a set to ensure a particular metadata object
		// only gets included once. Use a HashSet for fast membership queries.
		Set<T> results = new HashSet<>();
		
		// Binary search the first endpoint included in the range
		int first = 0;
		int lo = 0;
		int hi = this.endpoints.size() - 1;
		
		while(hi >= lo) {
			int mid = (lo + hi) / 2;
			Endpoint midpoint = this.endpoints.get(mid);
			
			if(midpoint.point >= range.begin &&
			   (mid == 0 || this.endpoints.get(mid - 1).point < range.begin)) {
				first = mid;
				break;
			}
			
			if(midpoint.point < range.begin)
				lo = mid + 1;
			else
				hi = mid - 1;
		}
		
		// All intervals with an endpoint in the query interval are results
		// Assuming there are any
		if (lo <= hi) {
			for (int i = first; i < this.endpoints.size(); i++) {
				if (this.endpoints.get(i).point < range.end)
					results.add(this.endpoints.get(i).range);
				else
					break;
			}
		}
		
		// Remaining results to be found are the intervals that contain the
		// query interval. Do a point search to find them.
		results.addAll(find(range.begin));
		
		return new ArrayList<>(results);
	}
	
	/**
	 * Find the metadata elements that contain the given point.
	 */
	public List<T> find(int point) {
		ArrayList<T> results = new ArrayList<>();
		
		find(this.root, point, results);
		
		return results;
	}
	
	
	/**
	 * Recursively gather the intervals containing the given point into the
	 * accumulator list.
	 */
	protected void find(Node node, int point, ArrayList<T> results) {
		if(node == null)
			return;
		
		// Special easy case, the point we're searching for is exactly the
		// center point.
		if(point == node.center) {
			for(Range elem: node.elemsByBegin)
				results.add((T)elem);
		}
		// The point is left of this node's center. Some of the intervals may
		// contain it, if they begin farther to the left.
		else if(point < node.center) {
			for(Range elem: node.elemsByBegin) {
				if(elem.begin <= point) // Inclusive
					results.add((T)elem);
				else
					break;
			}
			
			// Recurse to left child, there might be more
			find(node.left, point, results);
		}
		// The point is right of this node's center. Some of the intervals may
		// contain it, if they begin farther to the right.
		else {
			for(Range elem: node.elemsByEnd) {
				if (elem.end > point) // Exclusive
					results.add((T)elem);
				else
					break;
			}
			
			// Recurse to right child, there might be more
			find(node.right, point, results);
		}
	}
	
	/**
	 * Find the lowest starting point among the given intervals.
	 */
	protected int minBegin(List<T> elems) {
		int min = elems.get(0).begin;
		
		for(T elem: elems)
			if(elem.begin < min)
				min = elem.begin;
		
		return min;
	}
	
	/**
	 * Find the highest end point among the given intervals.
	 */
	protected int maxEnd(List<T> elems) {
		int max = elems.get(0).end;
		
		for(T elem: elems)
			if(elem.end > max)
				max = elem.end;
		
		return max;
	}

	
	/**
	 * Interval tree node, composed of the center point, left and right
	 * children, and the intervals containing the center point. The containing
	 * intervals are stored twice, sorted by their start and end points for
	 * fast searching.
	 */
	protected class Node {
		final int center;

		final Node left;
		final Node right;

		// Range[] instead of T[], because Java does not allow using generic types with arrays
		final Range[] elemsByBegin;
		final Range[] elemsByEnd;
		
		public Node(int center, Node left, Node right, List<T> elems) {
			this.center = center;
			this.left = left;
			this.right = right;
			
			Range[] arr = elems.toArray(new Range[0]);
			
			Arrays.sort(arr, new Comparator<Range>() {
				@Override
				public int compare(Range o1, Range o2) {
					return o1.begin - o2.begin;
				}
			});
			
			this.elemsByBegin = Arrays.copyOf(arr, arr.length);
			
			Arrays.sort(arr, new Comparator<Range>() {
				@Override
				public int compare(Range o1, Range o2) {
					return -(o1.end - o2.end);
				}
			});
			
			this.elemsByEnd = arr;
		}

		@Override
		public String toString() {
			return "Node [center=" + center + ", left=" + left + ", right="
					+ right + ", elemsByBegin=" + Arrays.toString(elemsByBegin)
					+ ", elemsByEnd=" + Arrays.toString(elemsByEnd) + "]";
		}
	}
	
	/**
	 * End-point data structure composed of one end of a range, inclusive, and
	 * a pointer to the range structure.
	 */
	protected class Endpoint {
		final int point;
		final T range;
		
		public Endpoint(int point, T range) {
			this.point = point;
			this.range = range;
		}

		@Override
		public String toString() {
			return "Endpoint [point=" + point + ", range=" + range + "]";
		}
	}
}
