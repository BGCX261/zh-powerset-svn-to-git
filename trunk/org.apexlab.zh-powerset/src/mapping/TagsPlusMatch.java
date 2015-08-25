package mapping;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class TagsPlusMatch {

	/**
	 * @param args
	 * @throws Exception 
	 */
	static PrintWriter pw;
	static String src_table, dst_table;
	static int count = 0;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub 
        Class.forName("com.mysql.jdbc.Driver");  
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/instance_matchings?useUnicode=true&characterEncoding=UTF-8","root","root");   
        Statement stmt = (Statement)conn.createStatement();
        
        src_table = args[0];
        dst_table = args[1];
        pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\"+src_table+"_"+dst_table+"_article_matchings_2nd.nt");
        
        HashMap<Integer, ArrayList<String>> src_tags = fetchTags(stmt);
        HashMap<String, Integer> dst_titles = fetchTitle(stmt);
        Scan(stmt, src_tags, dst_titles, false);
        
        if(!src_table.equals(dst_table)){
	        src_tags.clear();
	        dst_titles.clear();
	        
	        String tmp = src_table;
	        src_table = dst_table;
	        dst_table = tmp;
	        
	        count = 0;
	        
	        src_tags = fetchTags(stmt);
	        dst_titles = fetchTitle(stmt);
	        Scan(stmt, src_tags, dst_titles, true);
        }
        
        pw.close();
        conn.close();
	}
	
	public static void Scan(Statement stmt, HashMap<Integer, ArrayList<String>> src_tags, 
			HashMap<String, Integer> dst_titles, boolean reverse) throws Exception{
		
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+src_table+"_instances;");   

        while (rs.next())   
        {   
        	String id = rs.getString("ID");
        	String title = rs.getString("TITLE");
        	
        	if(title.matches("\\p{ASCII}+"))
        		continue;

        	if(src_tags.containsKey(Integer.valueOf(id))){
        		
        		for(String tag: src_tags.get(Integer.valueOf(id))){
        			
        			String now_title1 = title+tag;
        			String now_title2 = tag+title;
        			
        			//System.out.println(id+"\t"+now_title1+"\t"+now_title2);
        			
        			Integer id2 = dst_titles.containsKey(now_title1) ? dst_titles.get(now_title1) : dst_titles.get(now_title2);
        			
        			if(id2 != null){
        				count ++;
        				
        				if(!reverse)
        					pw.println("<http://apexlab.org/"+src_table+"/article/"+id+"> <http://www.w3.org/2002/07/owl#sameAs> <http://apexlab.org/"+dst_table+"/article/"+id2+"> .");
        				else
        					pw.println("<http://apexlab.org/"+dst_table+"/article/"+id2+"> <http://www.w3.org/2002/07/owl#sameAs> <http://apexlab.org/"+src_table+"/article/"+id+"> .");
        				
        					
        			}
        		}
        	}
        }   
        
        rs.close();
        
        System.out.println("total matchings from '"+src_table+"' to '"+dst_table+"': "+count);
	}
	
	public static HashMap<Integer, ArrayList<String>> fetchTags(Statement stmt) throws Exception{
 
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+src_table+"_instance_tags;");   

        HashMap<Integer, ArrayList<String>> ret = new HashMap<Integer, ArrayList<String>>();
        
        while (rs.next())   
        {   
        	String id = rs.getString("ID");
        	String tag = rs.getString("TAG");
        	
        	ArrayList<String> list = ret.get(Integer.valueOf(id));
        	
        	if(list == null){
        		list = new ArrayList<String> ();
        		ret.put(Integer.valueOf(id), list);
        	}
        	
        	//System.out.println(id+"\t"+tag);

        	list.add(tag);
        }   
        
        rs.close();
        
        System.out.println(ret.size()+" records loaded into tags.");
        
        return ret;
	}
	
	public static HashMap<String, Integer> fetchTitle(Statement stmt) throws Exception{
		 
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+dst_table+"_instances;");   

        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        
        while (rs.next())   
        {   
        	String id = rs.getString("ID");
        	String title = rs.getString("TITLE");

        	//System.out.println(id+"\t"+title);
        	
        	ret.put(title, Integer.valueOf(id));
        }   
        
        rs.close();
        
        System.out.println(ret.size()+" records loaded into titles.");
        
        return ret;
	}

}
