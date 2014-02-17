package fi.helsinki.cs.u.aitakang;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fi.helsinki.cs.u.aitakang.IntervalTree;
import fi.helsinki.cs.u.aitakang.Metadata;
import fi.helsinki.cs.u.aitakang.Range;


public class IntervalTreeTest {

	/** 10 non-overlapping elements with intervals of length 1. */
	private List<Metadata<Integer>> simple1;
	
	/** 10 non-overlapping elements with intervals of length 2. */
	private List<Metadata<Integer>> simple2;
	
	/**
	 * 10 elements where each element's interval overlaps with it's
	 * predecessor's and successor's.
	 */
	private List<Metadata<Integer>> overlap;
	
	/**
	 * 10 elements where all start at same point and end successively farther.
	 */
	private List<Metadata<Integer>> bottomHeavy;
	
	// And the corresponding trees;
	private IntervalTree<Metadata<Integer>> simple1Tree;
	private IntervalTree<Metadata<Integer>> simple2Tree;
	private IntervalTree<Metadata<Integer>> overlapTree;
	private IntervalTree<Metadata<Integer>> bottomHeavyTree;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.simple1 = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			this.simple1.add(new Metadata<Integer>(i * 2, i * 2 + 1, i));
		}
		
		this.simple2 = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			this.simple2.add(new Metadata<Integer>(i * 3, i * 3 + 2, i));
		}
		
		this.overlap = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			this.overlap.add(new Metadata<Integer>(i * 2, i * 2 + 3, i));
		}
		
		this.bottomHeavy = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			this.bottomHeavy.add(new Metadata<Integer>(0, i + 1, i));
		}
		
		this.simple1Tree = new IntervalTree<>(this.simple1);
		this.simple2Tree = new IntervalTree<>(this.simple2);
		this.overlapTree = new IntervalTree<>(this.overlap);
		this.bottomHeavyTree = new IntervalTree<>(this.bottomHeavy);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.simple1 = null;
		this.simple2 = null;
		this.overlap = null;
		this.bottomHeavy = null;
		
		this.simple1Tree = null;
		this.simple2Tree = null;
		this.overlapTree = null;
		this.bottomHeavyTree = null;
	}

	@Test
	public void testConstruction() {
		// Just check that it constructs at all
		List<Range> empty = Collections.emptyList();
		new IntervalTree<Range>(empty);
		new IntervalTree<Range>(Collections.singletonList(new Range(0, 1)));
	}
	
	@Test
	public void testFindInt() {
		// A point outside any intervals should find nothing
		assertThat(this.simple1Tree.find(-1).isEmpty(), is(true));
		
		// A point inside an interval should find it
		assertThat(this.simple1Tree.find(0).size(), is(1));
		
		// A point inside several intervals should find them
		assertThat(this.bottomHeavyTree.find(0).size(), is(10));
	}
	
	@Test
	public void testFindRange() {
		// An interval outside any intervals in the tree should find nothing
		assertThat(this.simple1Tree.find(new Range(20, 21)).isEmpty(), is(true));
		assertThat(this.simple1Tree.find(new Range(-2, -1)).isEmpty(), is(true));
		
		// An interval overlapping with an interval should find it
		assertThat(this.simple1Tree.find(new Range(0, 1)).size(), is(1));
		
		// An interval overlapping with many intervals should find them all
		assertThat(this.bottomHeavyTree.find(new Range(0, 1)).size(), is(10));
	}
	
	@Test
	public void testSimple1() {
		// Nothing before, nothing after
		assertThat(this.simple1Tree.find(new Range(Integer.MIN_VALUE, 0)).isEmpty(), is(true));
		assertThat(this.simple1Tree.find(new Range(20, Integer.MAX_VALUE)).isEmpty(), is(true));
		
		// Check all points
		for(int i = 0; i < 20; i++) {
			List<Metadata<Integer>> found = this.simple1Tree.find(i);
			
			switch(i % 2) {
			// Even positions have an element
			case 0:
				assertThat(i + " should have one element", found.size(), is(1));
				
				// And that element should have ID equal to i / 2
				assertThat(found.get(0).meta, is(i / 2)); 
				break;
			// Odd positions should be empty
			case 1:
				assertThat(i + " should be empty", found.isEmpty(), is(true));
				break;
			}
		}
		
		// Check a range overlapping with everything
		assertThat(new HashSet<>(this.simple1Tree.find(new Range(0, 20))).size(), is(10));
	}
	
	@Test
	public void testSimple2() {
		// Nothing before, nothing after
		assertThat(this.simple2Tree.find(new Range(Integer.MIN_VALUE, 0)).isEmpty(), is(true));
		assertThat(this.simple2Tree.find(new Range(30, Integer.MAX_VALUE)).isEmpty(), is(true));
		
		for(int i = 0; i < 30; i++) {
			List<Metadata<Integer>> found = this.simple2Tree.find(i);
			
			switch(i % 3) {
			// 0 and 1 positions have an element
			case 0:
			case 1:
				assertThat(i + " should have one element", found.size(), is(1));
				
				// And that element should have ID equal to i / 3
				assertThat(found.get(0).meta, is(i / 3)); 
				break;
			// 2 positions should be empty
			case 2:
				break;
			}
		}
		
		// Check a range overlapping with everything
		assertThat(new HashSet<>(this.simple1Tree.find(new Range(0, 30))).size(), is(10));
	}
	
	@Test
	public void testOverlap() {
		// Nothing before, nothing after
		assertThat(this.overlapTree.find(new Range(Integer.MIN_VALUE, 0)).isEmpty(), is(true));
		assertThat(this.overlapTree.find(new Range(21, Integer.MAX_VALUE)).isEmpty(), is(true));
		
		for(int i = 0; i < 21; i++) {
			List<Metadata<Integer>> found = this.overlapTree.find(i);
			
			switch(i % 2) {
			// 0 positions, except first and last, should have two elements
			case 0:
				if(i == 0 || i == 20)
					assertThat(found.size(), is(1));
				else
					assertThat(found.size(), is(2));
					
				break;
			// 1 positions should have one element
			case 1:
				assertThat(found.size(), is(1));
				break;
			}
		}
	}
	
	@Test
	public void testBottomHeavy() {
		// Nothing before, nothing after
		assertThat(this.bottomHeavyTree.find(new Range(Integer.MIN_VALUE, 0)).isEmpty(), is(true));
		assertThat(this.bottomHeavyTree.find(new Range(10, Integer.MAX_VALUE)).isEmpty(), is(true));
		
		//
		for(int i = 0; i < 10; i++) {
			// Point
			assertThat(this.bottomHeavyTree.find(i).size(), is(10 - i));
			
			// Interval to i
			assertThat(this.bottomHeavyTree.find(new Range(Integer.MIN_VALUE, i + 1)).size(), is(10));
			
			// Interval from i
			assertThat(this.bottomHeavyTree.find(new Range(i, Integer.MAX_VALUE)).size(), is(10 - i));
		}
	}
}
