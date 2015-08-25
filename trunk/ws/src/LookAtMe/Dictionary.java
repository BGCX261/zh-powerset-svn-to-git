package LookAtMe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class Dictionary {
	private static final String LABEL = "L", PAGELINK = "P", RENAME = "R";
	private static final String WEB_SERVICE_URL = "http://192.168.4.148:8080/esa/esa?q=%s&limit=%d";

	protected static double penalty;
	private static IndexSearcher plSearch = null;
	private static IndexSearcher rnSearch = null;

	public static void clearCache() {
		vectorCache.clear();
		getCache.clear();
		getESACache.clear();
	}

	public static double sim(String term1, String term2) throws Exception {
		if (term1.equals(term2))
			return penalty;
		Integer[] v1 = vector(term1);
		Integer[] v2 = vector(term2);
		Arrays.sort(v1);
		Arrays.sort(v2);

		int p1 = 0, p2 = 0;
		int union = 0, inter = 0;
		while (p1 < v1.length && p2 < v2.length) {
			if (v1[p1] < v2[p2])
				p1++;
			else if (v1[p1] > v2[p2])
				p2++;
			else {
				inter++;
				p1++;
				p2++;
			}
			union++;
		}
		if (p1 == v1.length)
			union += v2.length - p2;
		else
			union += v1.length - p1;
		if (union != 0 && inter != 0)
			return (double) inter / union;
		else
			return penalty;
	}

	private static HashMap<String, Integer[]> vectorCache = new HashMap<String, Integer[]>();

	private static Integer[] vector(String term) throws Exception {
		if (vectorCache.containsKey(term))
			return vectorCache.get(term);
		if (plSearch == null)
			plSearch = new IndexSearcher("pagelink");
		Query query = new TermQuery(new Term(LABEL, term));
		TopDocs hits = plSearch.search(query, 1);
		String[] ret = null;
		if (hits != null && hits.totalHits != 0) {
			for (ScoreDoc sd : hits.scoreDocs) {
				ret = plSearch.doc(sd.doc).getValues(PAGELINK);
			}
		}
		if (ret == null) {
			Integer[] i = new Integer[0];
			vectorCache.put(term, i);
			return i;
		}
		Integer[] v = new Integer[ret.length];
		for (int i = 0; i < v.length; i++)
			v[i] = Integer.parseInt(ret[i]);
		vectorCache.put(term, v);
		return v;
	}

	private static HashMap<String, DictTerm[]> getCache = new HashMap<String, DictTerm[]>();

	private static String getDecoded(String name){
		if(name.equals("Bj\uFFFDrn Borg"))
			return "Bjorn Borg";
		else if(name.equals("Garc\uFFFDa"))
			return "Garcia";
		return name;
	}
	public static DictTerm[] get(String words, int top) throws Exception {
		if (getCache.containsKey(words))
			return getCache.get(words);
		if (rnSearch == null)
			rnSearch = new IndexSearcher("rename");
		Query query = new TermQuery(new Term(RENAME, words.toLowerCase()));
		TopDocs hits = rnSearch.search(query, 2);
		String[][] title = new String[2][];
		if (hits != null && hits.totalHits != 0) {
			int count = 0;
			for (ScoreDoc sd : hits.scoreDocs) {
				title[count++] = rnSearch.doc(sd.doc).getValues(LABEL);
			}
		}
		String[] reTitle = title[0];
		if (reTitle == null || reTitle.length == 0)
			reTitle = title[1];
		int len = reTitle == null ? 0 : title[0].length;
		if (len == 0) {
			DictTerm[] dt = new DictTerm[0];
			getCache.put(words, dt);
			return dt;
		}
		DictTerm[] dt = new DictTerm[len];
		int count = 0;
		String wordss = words.replaceAll(" ", "").toLowerCase();

		// double prob = 1.0/t.length;
		for (String tt : reTitle) {
			String ttt = tt.replaceAll(" ", "").toLowerCase();
			int m = LevenshteinDistance.getLevenshteinDistance(wordss, ttt);
			dt[count++] = new DictTerm(getDecoded(tt), 1.0 / (1 + m), vector(tt).length);
		}

		Arrays.sort(dt);
		if (top < dt.length)
			dt = Arrays.copyOf(dt, top);
		getCache.put(words, dt);
		return dt;
	}

	private static HashMap<String, DictTerm[]> getESACache = new HashMap<String, DictTerm[]>();

	public static DictTerm[] getESA(String words, int top) throws Exception {
		if (getESACache.containsKey(words))
			return getESACache.get(words);
		String url = String.format(WEB_SERVICE_URL, URLEncoder.encode(words,
				"utf8"), top);
		String res = query(url);
		if (res.equals("")) {
			DictTerm[] dt = new DictTerm[0];
			getESACache.put(words, dt);
			return dt;
		}
		String[] line = res.split("\r\n");
		DictTerm[] dt = new DictTerm[line.length];
		// String wordss = words.replaceAll(" ", "").toLowerCase();
		for (int i = 0; i < line.length; i++) {
			int pos = line[i].lastIndexOf(" ");
			double s = Double.parseDouble(line[i].substring(pos + 1));
			String name = line[i].substring(0, pos - 1);
			// String ttt = name.replaceAll(" ", "").toLowerCase();
			s = s >= 5 ? 1.0 : s / 5;
			// int m = LevenshteinDistance.getLevenshteinDistance(wordss, ttt);
			dt[i] = new DictTerm(getDecoded(name), s, vector(name).length);

		}
		Arrays.sort(dt);
		getESACache.put(words, dt);
		return dt;
	}

	private static String query(String url) throws Exception {
		StringBuffer sb = new StringBuffer();
		URL hp = new URL(url);
		HttpURLConnection hpCon = (HttpURLConnection) hp.openConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(hpCon
				.getInputStream()));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			if (line.indexOf(']') != -1)
				sb.append(line.substring(line.indexOf(']') + 1) + "\r\n");
		}
		br.close();
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// System.out.println(sim("!!!Fuck You!!!",
		// "!!!Fuck You!!! and Then Some"));
		// System.out.println(query("http://192.168.4.148:8080/esa/esa?&limit=100&q=apple"));
		int c = 0;
		System.out.println("\uFFFD");
		for (DictTerm dt : get("Garcia", 10)){
			System.out.println((++c) + " " + dt);
			System.out.println((int)dt.name().charAt(4));
		}
	}

	static class DictTerm implements Comparable<DictTerm> {
		private String label;
		private double score;
		private int pagelink;

		public DictTerm(String l, double s, int p) {
			label = l;
			score = s;
			pagelink = p;
		}

		public String name() {
			return label;
		}

		public double score() {
			return score;
		}

		public int pagelink() {
			return pagelink;
		}

		public String toString() {
			return label + " [" + score + ", " + pagelink + "]";
		}

		@Override
		public int compareTo(DictTerm o) {
			// TODO Auto-generated method stub
			if (this.score == o.score)
				return 0;
			else if (this.score > o.score)
				return -1;
			else
				return 1;
		}
	}
}
