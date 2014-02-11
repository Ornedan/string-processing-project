package fi.helsinki.cs.u.aitakang;

import java.util.Arrays;

import sais.sais;

public class Main {

	
	public static void main(String[] args) {
		String str = "banana" + Searches.EOT;
		int[] sa = new int[str.length()];
		
		sais.suffixsort(str, sa, str.length());
		
		BackwardsSearchData bsd = new BackwardsSearchData(str, sa);
		
		System.out.println(Arrays.toString(sa));
		System.out.println(Searches.binarySearch(str, sa, "a"));
		System.out.println(Searches.backwardsSearch(str, sa, bsd, "a"));
	}
}
