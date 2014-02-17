package fi.helsinki.cs.u.aitakang;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sais.sais;

public class SearchesTest {

	private String text;
	private int[] sa;
	private BackwardsSearchData bsd;

	@Before
	public void setUp() throws Exception {
		this.text = "banana" + Searches.EOT;
		this.sa = new int[this.text.length()];

		sais.suffixsort(this.text, this.sa, this.text.length());

		this.bsd = new BackwardsSearchData(this.text, sa);
	}

	@After
	public void tearDown() throws Exception {
		this.text = null;
		this.sa = null;
	}

	@Test
	public void testBinarySearch() {
		// Full text matches only itself
		Range fullSearch = Searches.binarySearch(this.text, this.sa, "banana");
		
		assertThat(fullSearch, notNullValue());
		assertThat(fullSearch.begin, is(4));
		assertThat(fullSearch.end, is(5));
		
		// Mismatch finds nothing
		assertThat(Searches.binarySearch(this.text, this.sa, "bn"), nullValue());
		assertThat(Searches.binarySearch(this.text, this.sa, "foo"), nullValue());
		
		// Correctly handle edge cases:
		// Pattern longer than text, and one that has text as prefix
		assertThat(Searches.binarySearch(this.text, this.sa, "frobnozzle"), nullValue());
		assertThat(Searches.binarySearch(this.text, this.sa, "bananana"), nullValue());
	}
	
	@Test
	public void testBackwardsSearch() {
		// Full text matches only itself
		Range fullSearch = Searches.backwardsSearch(this.text, this.sa, this.bsd, "banana");
		
		assertThat(fullSearch, notNullValue());
		assertThat(fullSearch.begin, is(4));
		assertThat(fullSearch.end, is(5));
		
		// Mismatch finds nothing
		assertThat(Searches.backwardsSearch(this.text, this.sa, this.bsd, "bn"), nullValue());
		assertThat(Searches.backwardsSearch(this.text, this.sa, this.bsd, "foo"), nullValue());
		
		// Correctly handle edge cases:
		// Pattern longer than text, and one that has text as prefix
		assertThat(Searches.backwardsSearch(this.text, this.sa, this.bsd, "frobnozzle"), nullValue());
		assertThat(Searches.backwardsSearch(this.text, this.sa, this.bsd, "bananana"), nullValue());
	}
	
	@Test
	public void testInexactBinarySearch() {
		// Behaves like exact search when error limit is 0
		assertThat(Searches.inexactBinarySearch(text, sa, 0, "banana").size(), is(1));
		assertThat(Searches.inexactBinarySearch(text, sa, 0, "bn").isEmpty(), is(true));
		assertThat(Searches.inexactBinarySearch(text, sa, 0, "foo").isEmpty(), is(true));
		assertThat(Searches.inexactBinarySearch(text, sa, 0, "frobnozzle").isEmpty(), is(true));
		assertThat(Searches.inexactBinarySearch(text, sa, 0, "bananana").isEmpty(), is(true));
		
		
		// bananana matches if we allow at least 2 characters error
		assertThat(Searches.inexactBinarySearch(text, sa, 1, "bananana").isEmpty(), is(true));
		assertThat(Searches.inexactBinarySearch(text, sa, 2, "bananana").isEmpty(), is(false));
		
		// 'bn' with 1 error generates matches 'ba', 'b', 'ban', 'n'
		assertThat(Searches.inexactBinarySearch(text, sa, 1, "bn").size(), is(4));
	}
}
