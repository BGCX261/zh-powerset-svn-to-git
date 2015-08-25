package mapping;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class LookInside {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main1(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> l1 = new ArrayList<String>();
		fetchAll(l1, "baidu");
		
		ArrayList<String> l2 = new ArrayList<String>();
		fetchAll(l2, "hudong");
		
		PrintWriter pw = new PrintWriter("letmcc.txt");
		int pair_count = 0;
		int hits = 0;
		
		for(String t1: l1)
			for(String t2: l2)
			{
				pair_count ++;
				if(pair_count % 100000000 == 0)
					System.out.println("pair_count: "+pair_count+"\t hits: "+hits);
				
				if(t1.contains(t2) || t2.contains(t1) && !t1.equals(t2)){
					pw.println(t1+"\t"+t2);
					hits ++;
				}
			}
		System.out.println("pair_count: "+pair_count+"\t hits: "+hits);
		
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		
		HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>();

		fetchAll(m, args[1]);
			
		PrintWriter pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\"+args[0]+"_"+args[1]+"_article_matchings_1st.nt");
			
        Statement stmt=null;  
        ResultSet rs=null;  
        Class.forName("com.mysql.jdbc.Driver");  
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/instance_matchings?useUnicode=true&characterEncoding=UTF-8","root","root");   
        
        stmt = (Statement)conn.createStatement();

        rs = stmt.executeQuery("SELECT * FROM "+args[0]+"_instances;");   

        while (rs.next())   
        {   
       	 	String id = rs.getString("ID");  
       	 	String title =rs.getString("TITLE");
       	 	
       	 	if(title.matches("\\p{ASCII}+"))
       	 		continue;
       	 	
       	 	if(m.containsKey(title)){
				ArrayList<String> list = m.get(title);
				
				if(list.size() > 1)
					System.out.println(title+" @"+args[1]+" "+list.size());
				
				for(String idd: list){
					pw.println("<http://apexlab.org/"+args[0]+"/article/"+id+"> <http://www.w3.org/2002/07/owl#sameAs> <http://apexlab.org/"+args[1]+"/article/"+idd+"> .");
					
				}
       	 	}
       
        } 
			
			pw.close();
	}
	
	public static void main2(String[] args) throws Exception {
		
		String[] t = new String[]{"baidu", "hudong", "zhwiki"};
		
		HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>();
		for(String table : t){
			m.clear();
			fetchAll(m, table);
			
			PrintWriter pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\"+table+"_"+table+"_article_matchings_1st.nt");
			
			for(String key: m.keySet()){
				
				if(key.matches("\\p{ASCII}+"))
					continue;
				
				ArrayList<String> list = m.get(key);
				for(int i=0; i<list.size()-1; i++){
					for(int j=i+1; j<list.size(); j++){
						if(!list.get(i).equals(list.get(j)))
							pw.println("<http://apexlab.org/"+table+"/article/"+list.get(i)+"> <http://www.w3.org/2002/07/owl#sameAs> <http://apexlab.org/"+table+"/article/"+list.get(j)+"> .");
					}
				}
			}
			
			pw.close();
		}
	}
	
	public static void fetchAll(ArrayList<String> list, String table){
        try  
        {  
            Statement stmt=null;  
            ResultSet rs=null;  
            Class.forName("com.mysql.jdbc.Driver");  
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/instance_matchings?useUnicode=true&characterEncoding=UTF-8","root","root");   
            
            stmt = (Statement)conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM "+table+"_instances;");   

            while (rs.next())   
            {   
           	 //System.out.println("0");
//                 System.out.println(rs.getString("ID"));  
//                 System.out.println(rs.getString("URL"));  
//                 System.out.println(rs.getString("TITLE"));  
            	list.add(rs.getString("TITLE"));
            }   
        }  
        catch(Exception ex)  
        {  
            ex.printStackTrace();  
        }  
	}
	
	public static void fetchAll(HashMap<String, ArrayList<String>> map, String table){
        try  
        {  
            Statement stmt=null;  
            ResultSet rs=null;  
            Class.forName("com.mysql.jdbc.Driver");  
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/instance_matchings?useUnicode=true&characterEncoding=UTF-8","root","root");   
            
            stmt = (Statement)conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM "+table+"_instances;");   

            while (rs.next())   
            {   
           	 //System.out.println("0");
//                 System.out.println(rs.getString("ID"));  
//                 System.out.println(rs.getString("URL"));  
//                 System.out.println(rs.getString("TITLE"));  
            	//list.add(rs.getString("TITLE"));
            	String title = rs.getString("TITLE");
            	String id = rs.getString("ID");
            	ArrayList<String> list = map.get(title);
            	if(list == null){
            		list = new ArrayList<String>();
            		map.put(title, list);
            	}
            	list.add(id);
            }   
        }  
        catch(Exception ex)  
        {  
            ex.printStackTrace();  
        }  
	}

}
