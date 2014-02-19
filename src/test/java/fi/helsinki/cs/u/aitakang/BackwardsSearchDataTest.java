package fi.helsinki.cs.u.aitakang;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sais.sais;

public class BackwardsSearchDataTest {

	private String textA;
	private int[] saA;
	private BackwardsSearchData bsdA;
	
	private String textRand;
	private int[] saRand;
	private BackwardsSearchData bsdRand;
	
	@Before
	public void setUp() throws Exception {
		this.textA = "";
		for(int i = 0, end = 1 + 3 * BackwardsSearchData.BLOCK_SIZE; i < end; i++) {
			this.textA += "a";
		}
		this.textA += Searches.EOT;

		this.saA = new int[this.textA.length()];
		sais.suffixsort(this.textA, this.saA, this.textA.length());
		this.bsdA = new BackwardsSearchData(this.textA, this.saA);
		
		
		Random rng = new Random(0);
		this.textRand = "";
		for(int i = 0, end = 1 + 3 * BackwardsSearchData.BLOCK_SIZE; i < end; i++) {
			this.textRand += ' ' + rng.nextInt(127 - ' ');
		}
		this.textRand += Searches.EOT;
		
		this.saRand = new int[this.textRand.length()];
		sais.suffixsort(this.textRand, this.saRand, this.textRand.length());
		this.bsdRand = new BackwardsSearchData(this.textRand, this.saRand);
	}

	@After
	public void tearDown() throws Exception {
		this.textA = null;
		this.saA = null;
		this.bsdA = null;
		
		this.textRand = null;
		this.saRand = null;
		this.bsdRand = null;
	}
	
	@Test
	public void testCounts() {
		for(char c = 0; c < this.bsdA.counts.length; c++) {
			int count = 0;
			for(char c2: this.textA.toCharArray()) {
				if(c2 < c)
					count += 1;
			}
			
			assertThat(this.bsdA.counts[c], is(count));
		}
		

		for(char c = 0; c < this.bsdRand.counts.length; c++) {
			int count = 0;
			for(char c2: this.textRand.toCharArray()) {
				if(c2 < c)
					count += 1;
			}
			
			assertThat(this.bsdRand.counts[c], is(count));
		}
	}

	@Test
	public void testRank() {
		for(char c: this.bsdA.alphabet) {
			for(int n = 0; n < this.textA.length(); n++) {
				int naive = 0;
				for(int i = 0; i < n; i++)
					if(this.bsdA.bwt[i] == c)
						naive += 1;
				
				assertThat(String.format("rank(%c, %d)", c, n), this.bsdA.rank(c, n), is(naive));
			}
		}
		
		for(char c: this.bsdRand.alphabet) {
			for(int n = 0; n < this.textRand.length(); n++) {
				int naive = 0;
				for(int i = 0; i < n; i++)
					if(this.bsdRand.bwt[i] == c)
						naive += 1;
				
				assertThat(String.format("rank(%c, %d)", c, n), this.bsdRand.rank(c, n), is(naive));
			}
		}
	}
}
