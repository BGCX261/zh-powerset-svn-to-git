package LookAtMe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class EvalOnScreen {

	static String[] CAT = new String[]{"IS1","IS2","IS3","IS4","IS5","IS6","IS7","IS8","DS1","DS2","DS3",""};
	
	public static void baseline1(String root, int top) throws Exception{
		baseline(root, "baseline1-100", "", top);
	}
	public static void baseline2(String root, int top) throws Exception{
		baseline(root, "baseline2-100", "", top);
	}
	public static void ours0(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.0-30", "", top);
	}
	public static void ours1_google(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.2-9", "", top);
	}
	public static void ours1_yahoo(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.1-4", "", top);
	}
	public static void ours1_bing(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.0-6", "", top);
	}
	public static void ours2_yahoo(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.1-4", "_lda_20_100", top);
	}
	public static void ours2_bing(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.0-6", "_lda_30_100", top);
	}
	public static void ours2_google(String root, int top) throws Exception{
		baseline(root, "wikipedia-100-0.2-9", "_lda_20_100", top);
	}
	public static void baseline(String root, String local, String post, int top) throws Exception{
		String dir = root + local+"\\";
		String[] engine = new String[]{"google"+post, "yahoo"+post, "bing"+post};
		System.out.println("engine\tmetric\tis1~is8\tds1~ds3\tall");
		for(String e: engine){
			if(!new File(dir+e).exists())
				continue;
			String mrr = "";
			String ans = "";
			for(String c: CAT){
				if(!c.equals("")) c = "_"+c;
				BufferedReader br = new BufferedReader(new FileReader(dir+e+File.separator+"eval"+c+"-"+top+".txt"));
//				HAS_ANS:2 NO_ANS:2
//				AVG_ANS_MRR:0.5625
				while(true){
					String line = br.readLine();
					if(line == null)
						break;
					if(line.startsWith("HAS_ANS:")){
						int has = Integer.parseInt(line.substring(line.indexOf(':')+1, line.indexOf(' ')));
						int no = Integer.parseInt(line.substring(line.lastIndexOf(':')+1));
						ans += "\t"+(double)has/(has+no);
					}
					else if(line.startsWith("AVG_QUES_MRR"))
						mrr += "\t"+line.substring(line.indexOf(':')+1, line.indexOf(' '));
				}
				br.close();
			}
			System.out.println(mrr.trim());
			System.out.println(ans.trim());
		}
	}
	public static void distribution(String root, String engine) throws Exception{
		System.out.println("#keywords\\#rename");
		String mrr = "";
		String ans = "";
		for(int i=1; i<=15; i++){
			String m = "";
			String a = "";
			for(int j=0; j<=10; j++){
				String file = root+File.separator+"wikipedia-10-"+((double)j/10)+"-"+i+File.separator+engine+File.separator+"eval-10.txt";
				BufferedReader br = new BufferedReader(new FileReader(file));
				while(true){
					String line = br.readLine();
					if(line == null)
						break;
					if(line.startsWith("HAS_ANS:")){
						int has = Integer.parseInt(line.substring(line.indexOf(':')+1, line.indexOf(' ')));
						int no = Integer.parseInt(line.substring(line.lastIndexOf(':')+1));
						a += "\t"+(double)has/(has+no);
					}
					else if(line.startsWith("AVG_QUES_MRR"))
						m += "\t"+line.substring(line.indexOf(':')+1, line.indexOf(' '));
				}
				br.close();
			}
			ans += a.trim() + "\r\n";
			mrr += m.trim() + "\r\n";
		}
		System.out.println("mrr");
		System.out.println(mrr);
		System.out.println("ans");
		System.out.println(ans);
	}
	
	public static void feedback(String root, String engine) throws Exception{
		String[] local = new String[]{"wikipedia-100-0.2-9", "wikipedia-100-0.1-4", "wikipedia-100-0.0-6"};
		String[] eng = new String[]{"google", "yahoo", "bing"};
		String[] m = new String[]{"tfidf", "cluster", "lda"};
		String[] n = new String[]{"10","20","30","40","50"};
		for(int i=0; i<local.length; i++){
			if(!engine.equals(eng[i]))
				continue;
			String dir = root+local[i]+File.separator+eng[i];
			System.out.println(eng[i]);
			String mrr = "";
			String ans = "";
			for(String mm: m){
				String mrrr = "";
				String anss = "";
				for(String nn: n){
					String d = dir + "_"+mm+"_"+nn+"_10";
					BufferedReader br = new BufferedReader(new FileReader(d+File.separator+"eval_all-10.txt"));
					while(true){
						String line = br.readLine();
						if(line == null)
							break;
						if(line.startsWith("HAS_ANS:")){
							int has = Integer.parseInt(line.substring(line.indexOf(':')+1, line.indexOf(' ')));
							int no = Integer.parseInt(line.substring(line.lastIndexOf(':')+1));
							anss += "\t"+(double)has/(has+no);
						}
						else if(line.startsWith("AVG_QUES_MRR"))
							mrrr += "\t"+line.substring(line.indexOf(':')+1, line.indexOf(' '));
					}
				}
				ans = ans + anss.trim() + "\r\n";
				mrr = mrr + mrrr.trim() + "\r\n";
			}
			System.out.println("mrr");
			System.out.println(mrr);
			System.out.println("ans");
			System.out.println(ans);
		}
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String root = "D:\\wsdata\\data\\corpus\\";
//		baseline1(root, 10);
//		baseline2(root, 10);
//		ours0(root, 10);
//		ours1_google(root, 10);
//		ours1_yahoo(root, 10);
//		ours1_bing(root, 10);
//		ours2_google(root, 10);
//		ours2_yahoo(root, 10);
//		ours2_bing(root, 10);
		feedback(root, "google");
//		distribution(root, "bing");
	}

}
