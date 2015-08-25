package mapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

public class SplitIntoFiles {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String path = "C:\\Users\\Hay\\Desktop\\tmp.txt";
		
		String prefix = "C:\\Users\\Hay\\Desktop\\";
		String postfix = "_article_matchings_2nd_iter.nt";
		
		PrintWriter pw1 = new PrintWriter(prefix+"baidu_baidu"+postfix);
		PrintWriter pw2 = new PrintWriter(prefix+"hudong_hudong"+postfix);
		PrintWriter pw3 = new PrintWriter(prefix+"zhwiki_zhwiki"+postfix);
		PrintWriter pw4 = new PrintWriter(prefix+"baidu_hudong"+postfix);
		PrintWriter pw5 = new PrintWriter(prefix+"zhwiki_baidu"+postfix);
		PrintWriter pw6 = new PrintWriter(prefix+"zhwiki_hudong"+postfix);
		
		PrintWriter[] pw = new PrintWriter[]{pw1, pw2, pw3, pw4, pw5, pw6};
		
		BufferedReader br = new BufferedReader(new FileReader(path));
		
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			
			String[] seg = line.split(" ");
			
			int idx = getIdx(seg[0], seg[2]);
			
			if(idx >= 0)
				pw[idx].println(line);
			else
				pw[idx*-1].println(seg[2]+" "+seg[1]+" "+seg[0]+" .");
		}
		
		br.close();
		
		for(PrintWriter p: pw)
			p.close();
	}
	
	public static int getIdx(String s1, String s2){
		
		if(s1.contains("baidu") && s2.contains("baidu")){
			return 0;
		}
		else if(s1.contains("hudong") && s2.contains("hudong")){
			return 1;
		}
		else if(s1.contains("zhwiki") && s2.contains("zhwiki")){
			return 2;
		}
		else if(s1.contains("baidu") && s2.contains("hudong")){
			return 3;
		}
		else if(s1.contains("hudong") && s2.contains("baidu")){
			return -3;
		}
		else if(s1.contains("zhwiki") && s2.contains("baidu")){
			return 4;
		}
		else if(s1.contains("baidu") && s2.contains("zhwiki")){
			return -4;
		}
		else if(s1.contains("zhwiki") && s2.contains("hudong")){
			return 5;
		}
		else if(s1.contains("hudong") && s2.contains("zhwiki")){
			return -5;
		}
		return 6;
	}

}
