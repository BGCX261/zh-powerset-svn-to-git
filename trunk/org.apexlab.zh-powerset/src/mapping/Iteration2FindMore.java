package mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class Iteration2FindMore {

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	static PrintWriter pw;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String path = "C:\\Users\\Hay\\Desktop\\matchings\\";
		pw = new PrintWriter("C:\\Users\\Hay\\Desktop\\tmp.txt");
		
		HashMap<Integer, HashSet<Integer>> m = load(path);
		HashMap<Integer, HashSet<Integer>> mm = copy(m);
		
		int ins = -1;
		int iter = 0;
		
		while(ins != 0){
			iter ++;
			ins = multiply(mm, m);
			
			System.out.println("Iteration "+iter+": "+ins+" new matchings discovered.");
		}
		
		pw.close();
	}
	
	public static HashMap<Integer, HashSet<Integer>> copy(HashMap<Integer, HashSet<Integer>> src){
		HashMap<Integer, HashSet<Integer>> ret = new HashMap<Integer, HashSet<Integer>>();
		
		for(Integer k: src.keySet()){
			
			HashSet<Integer> v = new HashSet<Integer>();
			v.addAll(src.get(k));
			ret.put(k, v);
		}
		
		return ret;
	}
	
	public static HashMap<Integer, HashSet<Integer>> load(String path) throws Exception{
		
		HashMap<Integer, HashSet<Integer>> ret = new HashMap<Integer, HashSet<Integer>>();
		
		File dir = new File(path);
		
		for(File f: dir.listFiles()){
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			while(true){
				String line = br.readLine();
				if(line == null)
					break;
				
				String[] seg = line.split(" ");
				
				int id1 = getID(seg[0]);
				int id2 = getID(seg[2]);
				
				HashSet<Integer> iset = ret.get(id1);
				if(iset == null){
					iset = new HashSet<Integer>();
					ret.put(id1, iset);
				}
				iset.add(id2);
				
				iset = ret.get(id2);
				if(iset == null){
					iset = new HashSet<Integer>();
					ret.put(id2, iset);
				}
				iset.add(id1);
			}
			
			br.close();
		}
		
		return ret;
	}

	public static int multiply(HashMap<Integer, HashSet<Integer>> m1, HashMap<Integer, HashSet<Integer>> m2){
		
		int ret = 0;
		
		for(Integer mk1: m1.keySet()){
			
			HashSet<Integer> mvs1 = m1.get(mk1);
			HashSet<Integer> mvs1_copy = new HashSet<Integer>();
			mvs1_copy.addAll(mvs1);
			
			HashSet<String> cache = new HashSet<String>();
			
			for(Integer mv1: mvs1_copy){
				
				if(m2.containsKey(mv1)){
					
					for(Integer mv2: m2.get(mv1)){
						
						if(!mvs1_copy.contains(mv2)){
							
							// this is a new matching
							
							String d1 = getTable(mk1);
							String d2 = getTable(mv2);
							
							int id1 = getDecodeID(mk1);
							int id2 = getDecodeID(mv2);
							
							if(!d1.equals(d2) || id1 != id2){
								
								String triple = "<http://apexlab.org/"+d1+"/article/"+id1+"> <http://www.w3.org/2002/07/owl#sameAs> <http://apexlab.org/"+d2+"/article/"+id2+"> .";
								
								if(!cache.contains(triple)){
									ret ++;
									
									pw.println(triple);
									
									mvs1.add(mv2);
									
									cache.add(triple);
								}
							}
						}
					}
				}
			}
			
			cache.clear();
			
			mvs1_copy.clear();
		}
		
		return ret;
	}
	
	static String[] str = new String[]{"baidu", "hudong", "zhwiki"};
	static int t = 100000000;
	
	public static int getID(String url){
		
		int id = Integer.valueOf(url.substring
				(url.lastIndexOf('/')+1, url.lastIndexOf('>')));
		
		for(int i=0; i<str.length; i++){
			
			if(url.contains(str[i])){
				
				return id + (i+1)*t;
			}
		}
		
		return 0;
	}
	
	public static String getTable(int id){
		int idx = id/t - 1;
		
		return str[idx];
	}
	
	public static int getDecodeID(int id){
		int idx = id/t ;
		
		return id - idx*t;
	}
}
