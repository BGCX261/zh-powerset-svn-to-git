package org.apexlab.zh_powerset.querypattern;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class beta4BaiduZhidaoPattern {

	private static String[] patterns = new String[]{"吗","怎么","什么","哪","如何","怎样","呢","谁","多","么","几","有没有","否","和","与","还是","那","X不X"};
//	private static String[] patterns = new String[]{"什么","哪","吗","怎么","如何","几","多少","谁"};
//	private static String[] patterns = new String[]{"怎么","什么","哪","如何","怎样","呢","谁","多.","么",".否",".不.","几","那"};
	private static int[] n = new int[patterns.length];
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void test1() throws Exception{
		LineNumberReader reader= new LineNumberReader(new FileReader("D:\\download\\allquestions.txt"));
		int bad=0;
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			boolean covered = false;
			int find = line.substring(1, line.length()-1).indexOf("不");
			int loc = find +1;
			while(find != -1){
				if(line.charAt(loc-1) == line.charAt(loc+1)){
					n[patterns.length-1]++;
					covered = true;
//					System.out.println(line);
					break;
				}
				find = line.substring(loc+1, line.length()-1).indexOf("不");
				loc = find +loc+1;
			}
			for(int i=0; i<patterns.length; i++){
				if(!covered && line.contains(patterns[i])){
					n[i]++;
					covered = true;
					break;
				}
			}
			if(!covered){
				bad ++;
				System.out.println(line);
				if(reader.getLineNumber() %10000 == 0)
					System.out.println(reader.getLineNumber()+" "+(bad+.0)/reader.getLineNumber());
			}
		}
		for(int i=0; i<patterns.length; i++){
			System.out.println(patterns[i]+": "+(n[i]+.0)/reader.getLineNumber());
		}
		reader.close();
	}
	
	public static void test2() throws Exception{
		LineNumberReader reader= new LineNumberReader(new FileReader("D:\\download\\SogouLabDic.dic"));
		HashSet<String> dic = new HashSet<String>();
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			dic.add(line.split("\t")[0]);
		}
		reader.close();
		System.out.println("finish reading");
		reader= new LineNumberReader(new FileReader("D:\\download\\allquestions.txt"));
		HashMap<String, Integer> termFreq = new HashMap<String, Integer>();
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			if(reader.getLineNumber() % 10000 == 0)
				System.out.println("@"+reader.getLineNumber());
			line = line.replaceAll("\\s+", "");
			for(int i=0; i<line.length(); i++){
				for(int j=4; j>0; j--){
					if(i+j>line.length()) continue;
					String term = line.substring(i, i+j);
					if(dic.contains(term) || j==1){
						if(termFreq.containsKey(term))
							termFreq.put(term, termFreq.get(term)+1);
						else termFreq.put(term, Integer.valueOf(1));
						i+=j-1;
						break;
					}
				}
			}
		}
		reader.close();
		Term[] terms = new Term[termFreq.size()];
		int count = 0;
		for(String name: termFreq.keySet())
			terms[count++] = new Term(name, termFreq.get(name).intValue());
		Arrays.sort(terms);
		PrintWriter pw = new PrintWriter("D:\\download\\statistic.txt");
		for(Term term: terms)
			pw.println(term.name+"\t"+term.freq);
		pw.println();
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		test1();
	}
	

}

class Term implements Comparable{
	public String name;
	public int freq;
	public Term(String name, int freq){
		this.name =name;
		this.freq = freq;
	}
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Term t = (Term)arg0;
		if(t.freq > freq)
			return 1;
		else if(t.freq < freq)
			return -1;
		else if(t.name.length() > name.length())
			return -1;
		else if(t.name.length() < name.length())
			return 1;
		return 0;
	}
}