package LookAtMe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Evaluation {

	private static HashMap<Integer, ArrayList<String>> answers;
	private static HashMap<Integer, ArrayList<String>> patterns;
	private static ArrayList<String> questions;
//	private static ArrayList<String> queries;

	public static void loadQuestions(String questionPath) throws Exception{
		questions = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(questionPath));
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			line = line.trim();
			questions.add(line.substring(line.indexOf(' ')+1));
		}
		br.close();
	}
	
	public static ArrayList<String> questions(){
		return questions;
	}
	
//	public static void loadQueries(String queryPath) throws Exception{
//		queries = new ArrayList<String>();
//		BufferedReader br = new BufferedReader(new FileReader(queryPath));
//		while(true){
//			String line = br.readLine();
//			if(line == null)
//				break;
//			line = line.trim();
//			queries.add(line.substring(line.indexOf(' ')+1));
//		}
//		br.close();
//	}
//	
//	public static ArrayList<String> queries(){
//		return queries;
//	}
	
	private static void load(String f, HashMap<Integer, ArrayList<String>> map)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			Integer qid = Integer.valueOf(line.substring(0, line.indexOf(' ')));
			ArrayList<String> ans = map.get(qid);
			if (ans == null) {
				ans = new ArrayList<String>();
				map.put(qid, ans);
			}
			ans.add(line.substring(line.indexOf(' ') + 1));
		}
		br.close();
	}

	public static void loadAnsPattern(String af, String pf) throws Exception {
		answers = new HashMap<Integer, ArrayList<String>>();
		load(af, answers);
		patterns = new HashMap<Integer, ArrayList<String>>();
		load(pf, patterns);
	}

	public static boolean correct(int qid, String doc) {
		if(doc.contains(questions.get(qid-1)))
			return false;
		if (answers.containsKey(qid))
			for (String a : answers.get(qid)) {
//				System.out.println(a);
				if(doc.contains(a))
					return true;
//				String[] each = a.split(" ");
//				boolean eachcontain = false;
//				for(String e: each){
//					String ee = e.replaceAll("\\p{Punct}", " ").trim();
//					if (!doc.toLowerCase().contains(ee))
//						eachcontain = false;
//				}
//				if(eachcontain) return true;
			}

		if (patterns.containsKey(qid))
			for (String p : patterns.get(qid)) {
//				System.out.println(p);
				if (doc.matches(".*" + p + ".*"))
					return true;
			}
		return false;
	}
	
	public static ArrayList<Integer> judgeDoc(int qid, String f, int top) throws Exception {
		ArrayList<Integer> correct = new ArrayList<Integer>();
		int cur = 0;
		BufferedReader br = new BufferedReader(new FileReader(f));
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			if(line.startsWith("Title: ")){
				cur ++;
				if(cur > top)
					break;
				if(correct(qid, line))
					correct.add(cur);
			}
			else if(line.startsWith("Snippet: ")){
				if((correct.size()==0 || correct.get(correct.size()-1) != cur) && correct(qid, line))
					correct.add(cur);
			}
			else if(line.startsWith("Url: ")){
				if(line.contains("trec.nist.gov"))
					if(correct.size()!=0 && correct.get(correct.size()-1) == cur)
						correct.remove(correct.size()-1);
			}
		}
		br.close();
		return correct;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int qid = Integer.parseInt(args[0]);
		loadQuestions("D:\\wsdata\\data\\question.txt");
		loadAnsPattern("D:\\wsdata\\data\\answer.txt",
				"D:\\wsdata\\data\\answer_pattern.txt");
		String q = "";
		for(int i=1; i<args.length; i++)
			q+="\""+args[i].replaceAll("_", " ")+"\" ";
		q.trim();
		System.out.println(q);
		GoogleSearch bs = new GoogleSearch(q, 8);
		for (int i = 0; i < bs.size(); i++) {
			String title = bs.title(i);
			String snippet = bs.snippet(i);
			System.out.println(title+"\r\n"+snippet+"\r\n");
			if(correct(qid, title) || correct(qid, snippet)){
				System.out.println(1/(i+1.0));
				return;
			}
		}
//		System.out.println(judgeDoc(1, "C:\\Documents and Settings\\kaifengxu.APEXLAB\\×ÀÃæ\\corpus\\baseline1-100\\google\\1.txt", 100));
//		System.out.println(correct(1, "Snippet: The wings are the dwindling remnant of the space ship technology. The story concerns the iron-bound traditions that determine who can have wings and the one young woman who wants to change the system. ..... ?2.50 for a book.¡± The lady serving me said laconically: ¡°It's old.¡± Priceless! Anyway thanks to Avaland, whose remark appealed to my sense of fun.  I wouldn't  have gone book-hunting if it wasn't for that! Message edited by its author, Oct 4, 2008, 6:36am. flag abuse ..."));
	}

}
