package mapping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class ExactMatch {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String SAME_AS = "<http://www.w3.org/2002/07/owl#sameAs>";
		String baidu_path = "D:\\data\\baike\\nt\\baidu_article_title_zh.nt";
		String hudong_path = "D:\\data\\hudong\\nt\\hudong_article_title_zh.nt";
		String wikipedia_path = "D:\\data\\zhwiki\\nt\\zhwiki_article_title_zh.nt";
		
		String output1 = "C:\\Users\\kaifengxu\\Desktop\\zhwiki_baidu_article_matchings.nt";
		String output2 = "C:\\Users\\kaifengxu\\Desktop\\zhwiki_hudong_article_matchings.nt";
		String output3 = "C:\\Users\\kaifengxu\\Desktop\\baidu_hudong_article_matchings.nt";
		
		HashMap<String, Integer> wiki = new HashMap<String, Integer>();
		HashMap<String, Integer> baidu = new HashMap<String, Integer>();
		
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(wikipedia_path),"UTF8"));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(baidu_path),"UTF8"));
		BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(hudong_path),"UTF8"));
		
		PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output1),"UTF8"));
		PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output2),"UTF8"));
		PrintWriter pw3 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output3),"UTF8"));
		
		//PrintWriter tmp_pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(baidu_path+".tmp"),"UTF8"));
		
		System.out.println("step 1");
		while(true){
			String line = br1.readLine();
			if(line == null)
				break;
			
			String name = line.substring(line.indexOf('\"')+1, line.lastIndexOf('\"'));
			String id = line.substring(line.indexOf("article/")+8, line.indexOf('>'));
			wiki.put(name, Integer.valueOf(id));
		}
		
		br1.close();
		
		System.out.println("step 2");
		while(true){
			String line = br2.readLine();
			if(line == null)
				break;
			//System.out.println(line);
			//System.out.println(line);
//			int l1 = line.indexOf('\"');
//			int l2 = line.indexOf('\"',l1+1);
//			if(l1==-1 || l2==-1){
//				line = line.substring(0, line.lastIndexOf('@'))+"\"@zh .";
//			}
			
			//tmp_pw.println(line);
			
			String name = line.substring(line.indexOf('\"')+1, line.lastIndexOf('\"'));
			String id = line.substring(line.indexOf("article/")+8, line.indexOf('>'));
			baidu.put(name, Integer.valueOf(id));
			
			if(wiki.containsKey(name)){
				String triple = "<http://apexlab.org/zhwiki/article/"+
								wiki.get(name)+"> "+
								SAME_AS+
								" <http://apexlab.org/baidu/article/"+
								id+"> .";
				
				//String comment = "\t\\\\\""+name+"\" - \""+name+"\"";
				
				pw1.println(triple);
			}
		}
		
		br2.close();
		pw1.close();
		//tmp_pw.close();
		
		System.out.println("step 3");
		while(true){
			String line = br3.readLine();
			if(line == null)
				break;
			
			String name = line.substring(line.indexOf('\"')+1, line.lastIndexOf('\"'));
			String id = line.substring(line.indexOf("article/")+8, line.indexOf('>'));
			
			if(wiki.containsKey(name)){
				String triple = "<http://apexlab.org/zhwiki/article/"+
								wiki.get(name)+"> "+
								SAME_AS+
								" <http://apexlab.org/hudong/article/"+
								id+"> .";
				
				String comment = "\t\\\\\""+name+"\" - \""+name+"\"";				
					
					
				pw2.println(triple);
			}
			
			if(baidu.containsKey(name)){
				String triple = "<http://apexlab.org/baidu/article/"+
								baidu.get(name)+"> "+
								SAME_AS+
								" <http://apexlab.org/hudong/article/"+
								id+"> .";
				
				String comment = "\t\\\\\""+name+"\" - \""+name+"\"";
				
				pw3.println(triple);
			}
		}
		
		br3.close();
		pw2.close();
		pw3.close();
		
	}

}
