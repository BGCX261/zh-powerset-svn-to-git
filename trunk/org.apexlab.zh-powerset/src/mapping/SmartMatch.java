package mapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class SmartMatch {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 * @throws Exception 
	 */
//	public static void main2(String[] args) throws Exception {
//        String d1 = args[0];
//        String d2 = args[1];
//        String f = args[2];
//        
//        Statement stmt=null; 
//        Class.forName("com.mysql.jdbc.Driver");  
//        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/instance_matchings?useUnicode=true&characterEncoding=UTF-8","root","root");   
//        
//        stmt = (Statement)conn.createStatement();
//        
//        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Hay\\Desktop\\"+d1+"_"+d2+"_"+f+"_compressed.txt"));
//        
//        int count = 0;
//        while(true){
//        	String line = br.readLine();
//        	if(line == null)
//        		break;
//        	if(++count % 100000 == 0)
//        		System.out.println(count);
//        	
//        	String[] seg = line.split(" ");
//        	insert(stmt, d1, d2, f, seg[0], seg[1], seg[2]);
//        }
//        
//        br.close();
//        stmt.close();
//        conn.close();
//	}
	
	public static void main2(String[] args) throws Exception {
        String d1 = args[0];
        String d2 = args[1];
        String f = args[2];
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Hay\\Desktop\\"+d1+"_"+d2+"_"+f+"_sorted.txt"));
        PrintWriter pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\"+d1+"_"+d2+"_"+f+"_compressed.txt");
       
        String pre = null;
        int count = 0;
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        while(true){
        	String line = br.readLine();
        	if(line == null)
        		break;
        	
        	if(pre == null)
        		pre = line;
        	else if(!pre.equals(line)){
        		pw.println(pre + " "+count);
        		Integer i = map.get(count);
        		if(i==null)
        			map.put(count, 1);
        		else map.put(count, i+1);
        		pre = line;
        		count = 0;
        	}
        	
        	count ++;
        }
        
        pw.println(pre + " "+count);
		Integer i = map.get(count);
		if(i==null)
			map.put(count, 1);
		else map.put(count, i+1);
        
        pw.close();
		br.close();
		
		for(Integer j: map.keySet()){
			System.out.println(j+"\t"+map.get(j));
		}
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
        Statement stmt=null; 
        Class.forName("com.mysql.jdbc.Driver");  
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/instance_matchings?useUnicode=true&characterEncoding=UTF-8","root","root");   
        
        stmt = (Statement)conn.createStatement();
        
        String d1 = args[0];
        String d2 = args[1];
        String f = args[2];
     
         //HashMap<Long, Integer> mm = new HashMap<Long, Integer>();
         PrintWriter pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\"+d1+"_"+d2+"_"+f+".txt");
        
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Hay\\Desktop\\matchings\\"+d1+"_"+d2+"_article_matchings_1st.nt"));
        int count = 0;
        //HashSet<Long> hs = new HashSet<Long>();
    	//boolean [][] aaa = aaa(hs);
        
        HashMap<Integer, ArrayList<Integer>> l1 = fetch(stmt, d1, f);
        HashMap<Integer, ArrayList<Integer>> l2 = fetch(stmt, d2, f);
    	
        while(true){
        	
        	String line = br.readLine();
        	if(line == null)
        		break;
        	
        	if(++count % 1000 == 0)
        		System.out.println(count);
        	
        	String linne = line.substring(0, line.indexOf('>'));
        	String id1 = linne.substring(linne.lastIndexOf('/')+1);
        	String id2 = line.substring(line.lastIndexOf('/')+1, line.lastIndexOf('>'));
        	
        	int idd1 = Integer.parseInt(id1);
        	int idd2 = Integer.parseInt(id2);
        	//ArrayList<Integer> l1 = fetch(stmt, d1, f, id1);
        	//ArrayList<Integer> l2 = fetch(stmt, d2, f, id2);
        	if(l1.containsKey(idd1) && l2.containsKey(idd2)){
        		for(int i: l1.get(idd1))
	        		for(int j: l2.get(idd2)){
	        	
	//		        	int c = fetch(stmt, d1, d2, f, i, j);
	//		        	if(c == 0){
	//		        		insert(stmt, d1, d2, f, i, j, 1);
	//		        	}
	//		        	else{
	//		        		update(stmt, d1, d2, f, i, j, c+1);
	//		        	}
	//        			if(aaa[0][i] && aaa[1][j]){
	//        				long k = i;
	//        				k = (k<<24) + j;
	//        				if(hs.contains(k))
	        					pw.println(i+" "+j);
	        			//}
	        		}
	
	        			
        			
        		
        	}
        }
        
        pw.close();
        br.close();
        stmt.close();
        conn.close();
	}
	
	public static boolean[][] aaa(HashSet<Long> hs) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Hay\\Desktop\\zhwiki_baidu_related_compressed.txt"));
		boolean[][] aaa = new boolean[2][10000000];
		
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			String[] seg = line.split(" ");
			int id1 = Integer.parseInt(seg[0]);
			int id2 = Integer.parseInt(seg[1]);
			
			aaa[0][id1] = true;
			aaa[1][id2] = true;
			
			long id = id1;
			id = (id<<24)+id2;
			hs.add(id);
		}
		
		br.close();
		return aaa;
	}

	
	static HashMap<String, String> col = new HashMap<String, String>();
	static{
		col.put("page", "PAGELINK_ID");
		col.put("related", "RELATED_ID");
	}
	
	public static void update(Statement stmt, String d1, String d2, String f, int id1, int id2, int c) throws Exception{
        stmt.executeUpdate("UPDATE "+d1+"_"+d2+"_co_"+f+" SET COUNT="+c+" WHERE ID1="+id1+" AND ID2="+id2+";");
	}
	
	public static void insert(Statement stmt, String d1, String d2, String f, String id1, String id2, String c) throws Exception{
		stmt.executeUpdate("INSERT INTO _"+d1+"_"+d2+"_co_"+f+" VALUES("+id1+", "+id2+", "+c+");");
	}
	
	public static int fetch(Statement stmt, String d1, String d2, String f, int id1, int id2) throws Exception{
		ResultSet rs = stmt.executeQuery("SELECT COUNT FROM "+d1+"_"+d2+"_co_"+f+" WHERE ID1="+id1+" AND ID2="+id2+";");   
		if(rs.next()){
       	 	String c = rs.getString("COUNT");  
       	 	return Integer.parseInt(c);
		}
		return 0;
	}
	
	public static HashMap<Integer, ArrayList<Integer>> fetch(Statement stmt, String domain, String field) throws Exception{
		ResultSet rs = stmt.executeQuery("SELECT * FROM "+domain+"_"+field+"_links;");   
		HashMap<Integer, ArrayList<Integer>> ret = new HashMap<Integer, ArrayList<Integer>>();
        while (rs.next())   
        {   
       	 	int id = Integer.parseInt(rs.getString("ID"));  
       	 	int idd = Integer.parseInt(rs.getString(col.get(field)));
       	 	//System.out.println(id+"\t"+idd);
       	 	ArrayList<Integer> list = ret.get(idd);
       	 	if(list == null)
       	 	{
       	 		list = new ArrayList<Integer>();
       	 		ret.put(idd, list);
       	 	}
       	 	list.add(id);
        } 
        
        rs.close();
		return ret;
	}
	
	public static ArrayList<Integer> fetch(Statement stmt, String domain, String field, String id) throws Exception{
		ResultSet rs = stmt.executeQuery("SELECT ID FROM "+domain+"_"+field+"_links WHERE "+col.get(field)+"="+id+";");   
		ArrayList<Integer> ret = new ArrayList<Integer>();
        while (rs.next())   
        {   
       	 	String idd = rs.getString("ID");  
       	 	ret.add(Integer.valueOf(idd));
        } 
        
        rs.close();
		return ret;
	}
}
