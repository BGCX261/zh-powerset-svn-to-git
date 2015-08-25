package LookAtMe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;

public class FrameWork {

	private static String HELP = "Usage: java -jar baseline [engine(-)] [top-rank] [top-eval]\r\n" +
		"Usage: java -jar wikipedia [engine(-)] [top-rank] [top-eval] [rename-deg] [keyword-num]\r\n" +
		"Usage: java -jar entity [rename-deg] [keyword-num]\r\n";
	
	private static HashSet<String> stopwords;
	
	static {
		String[] stopword = new String[]{"a","able","about","across","after","ago","all","almost","also","am","among","an","and","any","are","as","at","be","because","been","before","between","but","by","can","cannot","came","could","dear","did","do","does","during","each","either","else","etc.","ever","every","far","for","from","get","given","go","goes","got","had","has","have","he","her","hers","him","his","how","however","i","if","in","into","is","it","its","just","least","let","like","likely","long","many", "may","me","might","most","much","must","my","name", "near","neither","no","nor","not","of","off","often","on","one","only","or","other","our","over","own","rather","s","said","say","says","she","should","since","so","some","still","such","t","than","that","the","their","them","then","there","these","they","this","tis","to","too","twas","up","upon","us","very","wants","was","we","were","what","when","where","which","while","who","whom","why","will","with","would","yet","you","your"};
		stopwords = new HashSet<String>();
		for(String sw: stopword)
			stopwords.add(sw);
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if(args.length < 3){
			System.out.println(HELP);
			return;
		}
//		define path
		String questionPath = "question.txt";
		String segmentation = "segmentation.txt";
		String questionTaggedPath = "question_tagged.txt";
		String answerPath = "answer.txt";
		String answer_patternPath = "answer_pattern.txt";
		String corpusPath = "corpus";
		
//		load questions
		Evaluation.loadQuestions(questionPath);
		System.out.println("loading questions finished!");
		
//		load answers and patterns
		Evaluation.loadAnsPattern(answerPath, answer_patternPath);
		System.out.println("loading answers finished!");
		
//		get queries
		ArrayList<String> queries = null;
//		if(args[0].equals("baseline1")){
//			queries = getNoTrunkQuery(Evaluation.questions());
//		}
//		else if(args[0].equals("baseline2")){
//			queries = getTrunkQuery(questionTaggedPath);
//		}
		if(args[0].equals("baseline")){
			queries = new ArrayList<String>();
			int count = 1;
			for(String q: Evaluation.questions())
				queries.add((count++)+" \""+q+"\"");
		}
		else if(args[0].equals("wikipedia")){
			queries = getWikipediaQuery(segmentation, Double.parseDouble(args[4]), Integer.parseInt(args[5]), false);
		}
		else if(args[0].equals("entity")){
			queries = getWikipediaQuery(segmentation, Double.parseDouble(args[1]), Integer.parseInt(args[2]), true);
			return;
		}
		System.out.println("getting queries finished!");
			
//		crawl the queries
		String[] se = args[1].split("-");
		for(String line: queries){
			int qid = Integer.parseInt(line.substring(0, line.indexOf(' ')));
			String query = line.substring(line.indexOf(' ')+1);
//			NOTE: "corpus/method-top/engine/id.txt"
			String parent = null;
			if(args[0].equals("wikipedia"))
				parent = corpusPath+File.separator+args[0]+"-"+args[2]+"-"+args[4]+"-"+args[5];
			else parent = corpusPath+File.separator+args[0]+"-"+args[2];
			for(String engine: se)
				search(parent, engine, engine, qid, query, Integer.parseInt(args[2]));
		}
		System.out.println("crawling pages finished!");
		
//		evaluation
		ArrayList<Integer> queryList = new ArrayList<Integer>();
		for(String line: queries){
			int qid = Integer.parseInt(line.substring(0, line.indexOf(' ')));
			queryList.add(qid);
		}
		for(String engine: se){
			String parent = null;
			if(args[0].equals("wikipedia"))
				parent = corpusPath+File.separator+args[0]+"-"+args[2]+"-"+args[4]+"-"+args[5]+File.separator+engine;
			else parent = corpusPath+File.separator+args[0]+"-"+args[2]+File.separator+engine;
			TreeMap<Integer, String> qid_file = new TreeMap<Integer, String>();
			for(Integer qid: queryList)
				qid_file.put(qid, qid+".txt");
			evaluation(qid_file, parent, Integer.parseInt(args[3]), "eval-"+args[3]+".txt");
		}
		System.out.println("evaluating pages finished!");
		
	}
	
	public static void evaluation(TreeMap<Integer, String> qid_file, String parent, int topk, String output) throws Exception{
		PrintWriter pw = new PrintWriter(parent+File.separator+output);
		double gmrr = 0, gmap = 0;
		int hasAnswer = 0;
		for(Integer qid: qid_file.keySet()){
			File f = new File(parent+File.separator+qid_file.get(qid));
			if(f.exists() && f.isFile()){
				ArrayList<Integer> correct = Evaluation.judgeDoc(qid, f.getAbsolutePath(), topk);
				String list = "";
				double mrr = 0, map = 0;
				for(int i=0; i<correct.size(); i++){
					int rank = correct.get(i);
					list += rank+" ";
					if(i==0)
						mrr = 1.0/rank;
					map += (i+1.0)/rank;
				}
				if(correct.size() > 0)
					map /= correct.size();
				if(mrr != 0){
					gmrr += mrr;
					gmap += map;
					hasAnswer ++;
				}
				pw.println(qid+" MRR:"+mrr+" MAP:"+map+" "+list.trim());
			}
		}
		pw.println("HAS_ANS:"+hasAnswer+" NO_ANS:"+(qid_file.size()-hasAnswer));
		pw.println("AVG_ANS_MRR:"+(gmrr/hasAnswer)+" AVG_ANS_MAP:"+(gmap/hasAnswer));
		pw.println("AVG_QUES_MRR:"+(gmrr/qid_file.size())+" AVG_QUES_MAP:"+(gmap/qid_file.size()));
		pw.close();
	}

	private static ArrayList<String> getNoTrunkQuery(ArrayList<String> questions) throws Exception{
		ArrayList<String> queries = new ArrayList<String>();
		for(int qid=1; qid<=questions.size(); qid++){
//				symbol involved [", &, ', $, :, (, ), ., ?, /, ,, -]
			String q = questions.get(qid-1);
			String[] words = q.replaceAll("\\p{Punct}", " ").trim().split(" +");
			String format = "";
			for(String w: words)
				if(!new StopwordsEnglish().isStopword(w))
					format += " \""+w+"\" ";
			queries.add(qid+" "+format.trim());
		}
		return queries;
	}
	
	private static ArrayList<String> getTrunkQuery(String questions_tagged) throws Exception{
		ArrayList<String> queries = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(questions_tagged));
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			String qid = line.substring(0, line.indexOf(' '));
			String format = line.substring(line.indexOf(' ')+1);
			String[] part = format.split(" ");
			boolean ne = false;
			String keywords = "";
			String nestr = "";
			for(String p: part){
				if(!ne){
					if(p.matches(".*<.+>.+")){
						ne = true;
					}
					else{
						String[] words = p.replaceAll("\\p{Punct}", " ").trim().split(" +");
						for(String w: words)
							if(!w.equals("") && !new StopwordsEnglish().isStopword(w))
								keywords += " \""+w+"\" ";
					}
				}
				if(ne){
					nestr += p+" ";
					if(ne && p.matches(".+</.+>.*")){
						String neexact = nestr.substring(nestr.indexOf('>')+1, nestr.lastIndexOf('<'));
						keywords += " \""+neexact+"\" ";
						nestr = "";
						ne = false;
					}
				}
			}
			queries.add(qid+" "+keywords.trim());
		}
		return queries;
	}
	
	public static boolean stopPhrase(String phrase){
		String[] word = phrase.split(" ");
		boolean fullstop = true;
		for(String w: word){
			if(!stopwords.contains(w.toLowerCase())){
				fullstop = false;
				break;
			}
		}
		return fullstop;
	}
	
	private static ArrayList<String> getWikipediaQuery(String segmentation, double rename, int topk, boolean output_entity) throws Exception{
		String format_file = "format_segmentation.txt";
		BufferedReader br = new BufferedReader(new FileReader(format_file));
		ArrayList<String> queries = new ArrayList<String>();
		String add = rename+"_"+topk+"_"+"entity.txt";
		PrintWriter pw = null;
		if(output_entity) pw = new PrintWriter(add);
		String qid = null;
		ArrayList<String[]> node = new ArrayList<String[]>();
		HashSet<String> linked = new HashSet<String>();
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			if(line.startsWith("START: ")){
				qid = null;
				node.clear();
				linked.clear();
			}
			else if(line.startsWith("QID: ")){
				qid = line.substring(line.indexOf(' ')+1);
			}
			else if(line.startsWith("NODE: ")){
				String c = line.substring(line.indexOf(' ')+1);
				String[] part = c.split(" \\| ");
				node.add(part);
			}
			else if(line.startsWith("EDGE: ")){
				String c = line.substring(line.indexOf(' ')+1);
				String[] part = c.split(" \\| ");
				linked.add(part[0]);
				linked.add(part[2]);
			}
			else if(line.startsWith("END: ")){
				String query = "";
				RankedTerm[] rt = new RankedTerm[node.size()];
				for(int i=0; i<node.size(); i++){
					rt[i] = new RankedTerm(i, node.get(i), linked);
				}
				Arrays.sort(rt);
				String[] qs = new String[rt.length];
				String entity = "";
				for(int i=0; i<topk; i++){
					if(i >= rt.length) break;
					if(rt[i].dist > 0 && rt[i].dist <= rename)
						qs[rt[i].id] = "\""+rt[i].rename+"\"";
					else qs[rt[i].id] = "\""+rt[i].name+"\"";
					if(rt[i].rename != null)
						entity += (rt[i].linked?"L:":"U:")+rt[i].rename + '\t';
				}
				if(output_entity)
					pw.println(qid+" "+entity.trim());
				for(String q: qs)
					if(q != null)
						query += q + " ";
				queries.add(qid+" "+query.trim());
			}
		}
		if(output_entity)
			pw.close();
		return queries;
	}
	
	private static class RankedTerm implements Comparable<RankedTerm>{
		public int id = -1, pagelink = -1;
		public String name = null, rename = null;
		public double dist = 0;
		public boolean linked = false;
		
		public RankedTerm(int id, String[] info, HashSet<String> linked){
			this.id = id;
			if(info.length == 2 || info.length == 3){
				name = getNoStopWordsPrexPost(getRealName(info[0]));
			}else{
				if(linked.contains(info[0]))
					this.linked = true;
				name = getRealName(info[1]);
				rename = info[2];
				dist = 1.0/Double.parseDouble(info[3]) - 1;
				dist = dist / name.length();
				pagelink = Integer.parseInt(info[4]);
			}
		}
		
		private String getNoStopWordsPrexPost(String name){
			String[] words = name.split(" ");
			String format = name;
			for(int i=0; i<words.length; i++){
				if(stopwords.contains(words[i].toLowerCase()))
					format = format.substring(format.indexOf(' ')+1);
				else break;
			}
			words = format.split(" ");
			for(int i=words.length-1; i>-1; i--){
				if(stopwords.contains(words[i].toLowerCase()))
					format = format.substring(0, format.lastIndexOf(' '));
				else break;
			}
			return format.trim();
		}
		
		private String getRealName(String name){
			if(name.indexOf('{')!= -1)
				return name.substring(name.indexOf('{')+1, name.indexOf('}'));
			if(name.toLowerCase().startsWith("the "))
				return name.substring(name.indexOf(' ')+1);
			return name;
		}

		@Override
		public int compareTo(RankedTerm o) {
			// TODO Auto-generated method stub
			if(this.linked && ! o.linked)
				return -1;
			if(!this.linked && o.linked)
				return 1;
			if(o.pagelink != this.pagelink)
				return o.pagelink - this.pagelink;
			return Double.compare(this.dist, o.dist);
		}
	}
	
	public static void search(String parent, String engine, String dir, int qid,
			String query, int top) throws Exception {
//		check dir
		File root = new File(parent + File.separator + dir);
		if (!root.exists() || !root.isDirectory())
			root.mkdirs();
		
//		check file
		String line = "";
		File tar = new File(root.getAbsolutePath() + File.separator + qid
				+ ".txt");
		if (tar.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(tar));
			line = br.readLine();
			br.close();
			line = line.substring(line.indexOf(' ')+1);
//			line = query;
		}
		
//		if not exist or the previous query != current query
		if(!line.equals(query)){
			SearchEngine se = null;
			if(engine.equals("google"))
				se = new GoogleSearch(query, top);
			else if(engine.equals("yahoo"))
				se = new YahooSearch(query, top);
			else if(engine.equals("bing"))
				se = new BingSearch(query, top);
			
			PrintWriter pw = new PrintWriter(tar);
			pw.println("Query: " + query);
			pw.println();
			for (int i = 0; i < se.size(); i++) {
				pw.println("Title: "+se.title(i));
				pw.println("Snippet: "+se.snippet(i));
				pw.println("Url: "+se.url(i));
				pw.println();
			}
			pw.close();
		}
	}

}
