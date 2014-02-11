package fi.helsinki.cs.u.aitakang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class IntervalTree<T> {
	
	protected final Node root;
	
	public IntervalTree(List<Metadata<T>> metas) {
		this.root = mkNode(metas);
	}
	
	protected Node mkNode(List<Metadata<T>> elems) {
		int center = (minBegin(elems) + maxEnd(elems)) / 2;
		
		List<Metadata<T>> toLeft = new ArrayList<>();
		List<Metadata<T>> toRight = new ArrayList<>();
		List<Metadata<T>> overlap = new ArrayList<>();
		
		for(Metadata<T> elem: elems) {
			if(elem.begin <= center) {
				if(elem.end < center)
					toLeft.add(elem);
				else
					overlap.add(elem);
			}
			else {
				toRight.add(elem);
			}
		}
		
		Node left = mkNode(toLeft);
		Node right = mkNode(toRight);
		
		return new Node(center, left, right, overlap);
	}
	
	protected int minBegin(List<Metadata<T>> elems) {
		int min = elems.get(0).begin;
		
		for(Metadata<T> meta: elems)
			if(meta.begin < min)
				min = meta.begin;
		
		return min;
	}
	
	protected int maxEnd(List<Metadata<T>> elems) {
		int max = elems.get(0).end;
		
		for(Metadata<T> meta: elems)
			if(meta.end < max)
				max = meta.end;
		
		return max;
	}

	
	
	protected class Node {
		final int center;

		final Node left;
		final Node right;

		final Metadata<T>[] elemsByBegin;
		final Metadata<T>[] elemsByEnd;
		
		public Node(int center, Node left, Node right, List<Metadata<T>> elems) {
			this.center = center;
			this.left = left;
			this.right = right;
			
			@SuppressWarnings("unchecked")
			Metadata<T>[] arr = (Metadata<T>[])elems.toArray();
			
			Arrays.sort(arr, new Comparator<Metadata<T>>() {
				@Override
				public int compare(Metadata<T> o1, Metadata<T> o2) {
					return o1.begin - o2.begin;
				}
			});
			
			this.elemsByBegin = Arrays.copyOf(arr, arr.length);
			
			Arrays.sort(arr, new Comparator<Metadata<T>>() {
				@Override
				public int compare(Metadata<T> o1, Metadata<T> o2) {
					return -(o1.begin - o2.begin);
				}
			});
			
			this.elemsByEnd = arr;
		}
	}
}
