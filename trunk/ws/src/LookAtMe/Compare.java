package LookAtMe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.TreeMap;

public class Compare {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		TreeMap<Integer, Integer> ret = new TreeMap<Integer, Integer>();
		
		String[] e = new String[]{"google","bing"};
		String[] m = new String[]{"baseline2-100","wikipedia-10-0.25-8"};
		
		for(String engine: e){
			BufferedReader br1 = new BufferedReader(new FileReader("D:\\wsdata\\data\\corpus\\"+m[0]+"\\"+engine+"\\eval-10.txt"));
			BufferedReader br2 = new BufferedReader(new FileReader("D:\\wsdata\\data\\corpus\\"+m[1]+"\\"+engine+"\\eval-10.txt"));
			while(true){
				String line1 = br1.readLine();
				String line2 = br2.readLine();
				if(line1 == null || line1.startsWith("HAS_ANS:"))break;
				String[] part1 = line1.split(" ");
				String[] part2 = line2.split(" ");
				double d1 = Double.parseDouble(part1[1].substring(part1[1].indexOf(':')+1));
				double d2 = Double.parseDouble(part2[1].substring(part2[1].indexOf(':')+1));
				if(d2==0 && d1!=0){
					if(ret.containsKey(Integer.parseInt(part2[0])))
						ret.put(Integer.parseInt(part2[0]), ret.get(Integer.parseInt(part2[0]))+1);
					else ret.put(Integer.parseInt(part2[0]), 1);
				}
			}
		}
		System.out.println(ret);
	}

}
