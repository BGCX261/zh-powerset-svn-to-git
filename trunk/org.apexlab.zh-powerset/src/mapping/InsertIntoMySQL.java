package mapping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.tools.bzip2.CBZip2InputStream;

public class InsertIntoMySQL {

	public static void main(String[] args) throws Exception {
		HashMap<Integer, String> tags = new HashMap<Integer, String>();

		String path2 = "C:\\Users\\Hay\\Desktop\\apex-chinese-ontology-datasets\\"+args[0]+"_tag_title_zh.nt.bz2";
		String path1 = "C:\\Users\\Hay\\Desktop\\apex-chinese-ontology-datasets\\"+args[0]+"_article_tag.nt.bz2";
		
		InputStream input2 = new FileInputStream(path2);
		input2.read();
		input2.read();
		
		CBZip2InputStream in2 = new CBZip2InputStream(input2);
		LineNumberReader reader2 = new LineNumberReader(new InputStreamReader(in2, "utf8"));

		while(true){
			String line = reader2.readLine();
			if(line == null)
				break;
			
			if(reader2.getLineNumber() %100000==0)
				System.out.println(reader2.getLineNumber());
			//System.out.println(line);
			String id = line.substring(line.indexOf("tag/")+4,line.indexOf('>'));
			String name = line.substring(line.indexOf('\"')+1, line.lastIndexOf('\"'));
			tags.put(Integer.valueOf(id), name);
		}
		
		reader2.close();
		
		
		InputStream input1 = new FileInputStream(path1);
		input1.read();
		input1.read();
		
		CBZip2InputStream in1 = new CBZip2InputStream(input1);
		LineNumberReader reader1 = new LineNumberReader(new InputStreamReader(in1, "utf8"));

		
        Statement stmt=null; 
        Class.forName("com.mysql.jdbc.Driver");  
        String host = "jdbc:mysql://localhost:3306/instance_matchings";
        String auth = "?useUnicode=true&characterEncoding=UTF-8";
        Connection conn = DriverManager.getConnection(host + auth,"root","root");   
        
        stmt = (Statement)conn.createStatement();
        int error = 0;
        
        
		while(true){
			String line = reader1.readLine();
			if(line == null)
				break;
			
			if(reader1.getLineNumber() %100000==0)
				System.out.println(reader1.getLineNumber());
			
			String id1 = line.substring(line.indexOf("article/")+8,line.indexOf('>'));
			String id2 = line.substring(line.lastIndexOf("tag/")+4,line.lastIndexOf('>'));
			
			String name = tags.get(Integer.valueOf(id2));
			if(name.indexOf("\u0027") != -1)
			name = "\""+name+"\"";
		else name = "'"+name+"'";
			if(name.indexOf('\\') != -1)
				name = name.replace("\\", "");
			
			try{
				
				int r = stmt.executeUpdate("INSERT INTO "+args[0]+"_instance_tags VALUES("+id1+","+name+")");
	        	if(r != 1)
	        		error ++;
				}catch(Exception e){
					e.printStackTrace();
					System.out.println(id1+"\t"+name);
					String nname = "";
					for(int i=2; i<name.length(); i++)
						nname += "?";
					stmt.executeUpdate("INSERT INTO "+args[0]+"_instance_tags VALUES("+id1+",'"+nname+"')");
					//return;
				}
				
		}
		
		reader1.close();
		
		System.out.println("error:"+error);
	}
	
	public static void main2(String[] args) throws Exception {
        Statement stmt=null; 
        Class.forName("com.mysql.jdbc.Driver");  
        String host = "jdbc:mysql://localhost:3306/instance_matchings";
        String auth = "?useUnicode=true&characterEncoding=UTF-8";
        Connection conn = DriverManager.getConnection(host + auth,"root","root");   
        
        stmt = (Statement)conn.createStatement();
        int error = 0;
        
		String path1 = "C:\\Users\\Hay\\Desktop\\apex-chinese-ontology-datasets\\"+args[0]+"_article_related_link.nt.bz2";
		String path2 = "C:\\Users\\Hay\\Desktop\\apex-chinese-ontology-datasets\\"+args[0]+"_article_page_link.nt.bz2";
		
		String[] path = new String[]{path1, path2};
		String[] table = new String[]{args[0]+"_related_links", args[0]+"_page_links"};
		
		for(int i=0; i<path.length; i++){
			
		InputStream input = new FileInputStream(path[i]);
		input.read();
		input.read();
		
		CBZip2InputStream in = new CBZip2InputStream(input);
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in, "utf8"));
		
		boolean switcher = true;
		
		while(true){
			String line = reader.readLine();
			if(line == null)
				break;
			
			if(reader.getLineNumber() %100000==0)
				System.out.println(reader.getLineNumber());
			
			String id1 = line.substring(line.indexOf("article/")+8,line.indexOf('>'));
			String id2 = line.substring(line.lastIndexOf("article/")+8,line.lastIndexOf('>'));
			//System.out.println(line);
			
			//if(id1.equals("1364435") && id2.equals("19230"))
				//switcher = true;
			
			if(switcher){
			try{
				
			int r = stmt.executeUpdate("INSERT INTO "+table[i]+" VALUES("+id1+","+id2+")");
        	if(r != 1)
        		error ++;
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(id1+"\t"+id2);
				return;
			}}
		}
		
		reader.close();
		}
		
		System.out.println("finished loading @error: "+error);
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main1(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
        Statement stmt=null;  
        ResultSet rs=null;  
        Class.forName("com.mysql.jdbc.Driver");  
        String host = "jdbc:mysql://localhost:3306/instance_matchings";
        String auth = "?useUnicode=true&characterEncoding=UTF-8";
        Connection conn = DriverManager.getConnection(host + auth,"root","root");   
        
        stmt = (Statement)conn.createStatement();
        int error = 0;
		
		String path1 = "C:\\Users\\Hay\\Desktop\\apex-chinese-ontology-datasets\\"+args[0]+"_article_title_zh.nt.bz2";
		String path2 = "C:\\Users\\Hay\\Desktop\\apex-chinese-ontology-datasets\\"+args[0]+"_article_link.nt.bz2";
		
		
//		InputStream input1 = new FileInputStream(path1);
//		input1.read();
//		input1.read();
//		
//		CBZip2InputStream in1 = new CBZip2InputStream(input1);
//		LineNumberReader reader1 = new LineNumberReader(new InputStreamReader(in1, "utf8"));
//		boolean switcher = false;
//		while(true){
//			String line = reader1.readLine();
//			if(line == null)
//				break;
//			
//			if(reader1.getLineNumber() %100000==0)
//				System.out.println(reader1.getLineNumber());
//			String id = line.substring(line.indexOf("article/")+8,line.indexOf('>'));
//			String name = line.substring(line.indexOf('\"')+1, line.lastIndexOf('\"'));
//			//System.out.println(line);
//			//System.out.println(id+"\t"+name);
//			try{
//				if(name.indexOf("\u0027") != -1)
//					name = "\""+name+"\"";
//				else name = "'"+name+"'";
//				if(id.equals("823571"))
//					switcher = true;
//			if(switcher){
//        	int r = stmt.executeUpdate("INSERT INTO "+args[0]+"_instances VALUES("+id+", '', "+name+")");
//			
//        	if(r != 1)
//        		error ++;}
//        	}catch(Exception e){
//				e.printStackTrace();
//				System.out.println(id+"\t"+name);
//				String nname = "";
//				for(int i=0; i<name.length(); i++)
//					nname += "?";
//				stmt.executeUpdate("INSERT INTO "+args[0]+"_instances VALUES("+id+", '', '"+nname+"')");
//				//return;
//			}
//		}
//		
//		reader1.close();
		
		InputStream input2 = new FileInputStream(path2);
		input2.read();
		input2.read();
		
		CBZip2InputStream in2 = new CBZip2InputStream(input2);
		LineNumberReader reader2 = new LineNumberReader(new InputStreamReader(in2, "utf8"));
		
		while(true){
			String line = reader2.readLine();
			if(line == null)
				break;
			if(reader2.getLineNumber() %100000==0)
				System.out.println(reader2.getLineNumber());
			
			String id = line.substring(line.indexOf("article/")+8,line.indexOf('>'));
			String url = line.substring(line.lastIndexOf("> <")+3, line.lastIndexOf('>'));
			//System.out.println(line);
			try{
				
				if(url.indexOf("\u0027") != -1)
				url = "\""+url+"\"";
			else url = "'"+url+"'";
				
        	int r = stmt.executeUpdate("UPDATE "+args[0]+"_instances SET URL="+url+" WHERE ID="+id);
        	if(r != 1)
        		error ++;
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(id+"\t"+url);
				String name = url.substring(url.lastIndexOf('/')+1);
				String nname = "";
				for(int i=1; i<name.length(); i++)
					nname += "?";
				stmt.executeUpdate("UPDATE "+args[0]+"_instances SET URL='http://zh.wikipedia.org/zh-cn/"+nname+"' WHERE ID="+id);
			}
		}
		
		reader2.close();
		
		System.out.println("finished loading @error: "+error);

        
        
        
	}

}
