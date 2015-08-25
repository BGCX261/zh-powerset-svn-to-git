package mapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;

public class CoFilter {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main1(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader (new FileReader("C:\\Users\\Hay\\Desktop\\zhwiki_baidu_related_compressed.txt"));
		
		HashSet<String> set = new HashSet<String>();
		
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			set.add(line.substring(0, line.lastIndexOf(' ')));
		}
		br.close();
		
		br = new BufferedReader (new FileReader("D:\\zhwiki_baidu_page.txt"));
		PrintWriter pw = new PrintWriter("D:\\zhwiki_baidu_page_filtered.txt");
		
		int c = 0;
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			if(++c % 10000000 == 0)
				System.out.println(c);
			
			if(set.contains(line))
				pw.println(line);
		}
		
		pw.println();
		br.close();
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader (new FileReader("C:\\Users\\Hay\\Desktop\\zhwiki_hudong_related_compressed.txt"));
		
		HashSet<String> set = new HashSet<String>();
		
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			set.add(line.substring(0, line.lastIndexOf(' ')));
		}
		br.close();
		
		
		br = new BufferedReader (new FileReader("C:\\Users\\Hay\\Desktop\\zhwiki_hudong_page_compressed.txt"));
		PrintWriter pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\zhwiki_hudong_page_compressed_filtered.txt");
		
		int c = 0;
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			if(++c % 10000000 == 0)
				System.out.println(c);
			String l = line.substring(0, line.lastIndexOf(' '));
			if(set.contains(l))
				pw.println(line);
		}
		
		pw.println();
		br.close();
	}

}
