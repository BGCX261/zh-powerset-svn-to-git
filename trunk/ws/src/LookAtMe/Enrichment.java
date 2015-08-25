package LookAtMe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;


public class Enrichment {

	private String dir;
	private int topk;
//	private ArrayList<String> query;
	private ArrayList<String> question;
	private ArrayList<ScoredTerm[]> res;
	
	public Enrichment(String dir) throws Exception{
		this.dir = dir;
		File d = new File(dir);
		if(!d.exists() || !d.isDirectory()){
			System.err.println("root not exist || not a dir!");
			return;
		}
		res = new ArrayList<ScoredTerm[]>();
		Evaluation.loadQuestions("question.txt");
		question = Evaluation.questions();
//		Evaluation.loadQueries("wikipedia.txt");
//		query = Evaluation.queries();
	}
	
	public void setTop(int k){
		this.topk = k;
	}
	
	public void tfidf(String engine) throws Exception{
		TreeMap<String, Integer> df = new TreeMap<String, Integer>();
		TreeMap<String, Integer> tf = new TreeMap<String, Integer>();
		String doc = "";
		int count = 0;
		if(res.size() != 0) res.clear();
		
		for(int i=1; i<894; i++){
			BufferedReader br = new BufferedReader(new FileReader(dir+File.separator+engine+File.separator+i+".txt"));
			while(true){
				String line = br.readLine();
				if(line == null || count == topk)
					break;

				if(line.startsWith("Title: ")){
					doc = line.substring(line.indexOf(' ')+1);
				}
				else if(line.startsWith("Snippet: ")){
					doc += "\r\n"+line.substring(line.indexOf(' ')+1);
					TreeMap<String, Integer> freq = getTermFreq(doc.toLowerCase());
					for(String t: freq.keySet()){
						Integer f = df.get(t);
						if(f == null) df.put(t, 1);
						else df.put(t, f+1);
					}
					for(String t: freq.keySet()){
						Integer f = tf.get(t);
						if(f == null) tf.put(t, freq.get(t));
						else tf.put(t, f + freq.get(t));
					}
					count ++;
				}
			}
			br.close();
			
			if(tf.size() != df.size()){
				System.err.println("term number not coherent");
				return;
			}
			ScoredTerm[] st = new ScoredTerm[tf.size()];
			int j = 0;
			for(String t: tf.keySet())
				st[j++] = new ScoredTerm(t, (double)tf.get(t)/df.get(t));
			Arrays.sort(st);
			res.add(st);
			
			df.clear();
			tf.clear();
			count = 0;
		}
	}
	
	public TreeMap<String, Integer> getTermFreq(String doc){
		String[] term = doc.replaceAll("[^\\p{Alpha}\\p{Digit}]+", " ").split(" ");
		TreeMap<String, Integer> ret = new TreeMap<String, Integer>();
		outside:
		for(int i=0; i<term.length; i++){
			String name = "";
			for(int j=0; j<3 && i+j < term.length; j++){
				name += " "+term[i+j];
				name = name.trim();
				if(new StopwordsEnglish().isStopword(term[i+j]))
					continue outside;
				Integer f = ret.get(name);
				if(f==null)
					ret.put(name, 1);
				else ret.put(name, f+1);
			}
		}
		return ret;
	}
	
	public void cluster(String engine) throws Exception{
		String doc = "";
		int count = 0;
		if(res.size() != 0) res.clear();
		int topic_num = (int)(Math.sqrt(topk/2.0)-0.000001)+1;
		
		for(int i=1; i<894; i++){
			BufferedReader br = new BufferedReader(new FileReader(dir+File.separator+engine+File.separator+i+".txt"));
			PrintWriter pw = new PrintWriter(engine+"_doc_tmp.txt");
			while(true){
				String line = br.readLine();
				if(line == null || count == topk)
					break;

				if(line.startsWith("Title: ")){
					doc = line.substring(line.indexOf(' ')+1);
				}
				else if(line.startsWith("Snippet: ")){
					doc += " "+line.substring(line.indexOf(' ')+1);
					count ++;
					pw.println(count+" "+doc);
				}
			}
			br.close();
			pw.close();
			
			Process p = Runtime.getRuntime().exec("perl doc2mat -nlskip=1 "+engine+"_doc_tmp.txt "+engine+"_doc_tmp.mat");
			p.getInputStream().close();
			p.waitFor();
			p = Runtime.getRuntime().exec("vcluster -clmethod=direct "+engine+"_doc_tmp.mat "+topic_num);
			p.getInputStream().close();
			p.waitFor();
//			Thread.sleep(100);
			
			if(i%10==0)
				System.out.println("id:"+i+" finish clustering");
			
			String output = engine+"_doc_tmp.mat.clustering."+topic_num;
			HashMap<Integer, Integer> docID2clstID = new HashMap<Integer, Integer>();
			br = new BufferedReader(new FileReader(output));
			count = 1;
			while(true){
				String line = br.readLine();
				if(line == null)
					break;
				docID2clstID.put(count++, Integer.parseInt(line));
			}
			br.close();
			
			TreeMap<String, Integer>[] dfs = new TreeMap[topic_num];
			TreeMap<String, Integer>[] tfs = new TreeMap[topic_num];
			for(int j=0; j<topic_num; j++){
				dfs[j] = new TreeMap<String, Integer>();
				tfs[j] = new TreeMap<String, Integer>();
			}
			doc = "";
			count = 0;
			
			br = new BufferedReader(new FileReader(dir+File.separator+engine+File.separator+i+".txt"));
			while(true){
				String line = br.readLine();
				if(line == null || count == topk)
					break;

				if(line.startsWith("Title: ")){
					doc = line.substring(line.indexOf(' ')+1);
				}
				else if(line.startsWith("Snippet: ")){
					doc += "\r\n"+line.substring(line.indexOf(' ')+1);
					TreeMap<String, Integer> freq = getTermFreq(doc.toLowerCase());
					int id = docID2clstID.get(count+1);
					if(id != -1){
						for(String t: freq.keySet()){
							Integer f = dfs[id].get(t);
							if(f == null) dfs[id].put(t, 1);
							else dfs[id].put(t, f+1);
						}
						for(String t: freq.keySet()){
							Integer f = tfs[id].get(t);
							if(f == null) tfs[id].put(t, freq.get(t));
							else tfs[id].put(t, f + freq.get(t));
						}
					}
					count ++;
				}
			}
			br.close();
			int idx = 0, len = 0;
			for(int j=0; j<tfs.length; j++)
				len += tfs[j].size();
			ScoredTerm[] st = new ScoredTerm[len];
			for(int j=0; j<tfs.length; j++){
				for(String t: tfs[j].keySet())
					st[idx++] = new ScoredTerm(t, (double)tfs[j].get(t)/dfs[j].get(t));
				dfs[j].clear();
				tfs[j].clear();
			}
			Arrays.sort(st);
			res.add(st);
			count = 0;
		}
		if(!new File(engine+"_doc_tmp.txt").delete())
			new File(engine+"_doc_tmp.txt").deleteOnExit();
		if(!new File(engine+"_doc_tmp.mat").delete())
			new File(engine+"_doc_tmp.mat").deleteOnExit();
		if(!new File(engine+"_doc_tmp.mat.clabel").delete())
			new File(engine+"_doc_tmp.mat.clabel").deleteOnExit();
		if(!new File(engine+"_doc_tmp.mat.rlabel").delete())
			new File(engine+"_doc_tmp.mat.rlabel").deleteOnExit();
		if(!new File(engine+"_doc_tmp.mat.clustering."+topic_num).delete())
			new File(engine+"_doc_tmp.mat.clustering."+topic_num).deleteOnExit();
	}
	
	public void lda(String engine) throws Exception{
		String doc = "";
		int count = 0;
		if(res.size() != 0) res.clear();
		int topic_num = (int)(Math.sqrt(topk/2.0)-0.000001)+1;
		
		for(int i=1; i<894; i++){
			BufferedReader br = new BufferedReader(new FileReader(dir+File.separator+engine+File.separator+i+".txt"));
			String tmp_root = engine+"_tmp_root";
			File tmp = new File(tmp_root);
			if(!tmp.exists() || !tmp.isDirectory())
				tmp.mkdir();
			while(true){
				String line = br.readLine();
				if(line == null || count == topk)
					break;

				if(line.startsWith("Title: ")){
					doc = line.substring(line.indexOf(' ')+1);
				}
				else if(line.startsWith("Snippet: ")){
					doc += " "+line.substring(line.indexOf(' ')+1);
					count ++;
					PrintWriter pw = new PrintWriter(tmp_root+File.separator+count+".txt");
					pw.println(doc);
					pw.close();
				}
			}
			br.close();

			String mallet = engine+"_tmp_root.mallet";
			String topic_keys = engine+"_lda_topic.txt";
			PrintWriter pw = new PrintWriter(topic_keys);
			pw.close();
			
			String arg = "--input "+tmp_root+" --output "+mallet+" --keep-sequence --remove-stopwords --gram-sizes 1,2,3";
			Process p = Runtime.getRuntime().exec("java -Xmx300m -cp mallet.jar;mallet-deps.jar cc.mallet.classify.tui.Text2Vectors "+arg);
			p.getInputStream().close();
			p.waitFor();
			
			arg = "--input " + mallet + " --topic-word-weights-file " + topic_keys + " --num-topics " + topic_num;
			p = Runtime.getRuntime().exec("java -Xmx300m -cp mallet.jar;mallet-deps.jar cc.mallet.topics.tui.Vectors2Topics "+arg);
			p.getInputStream().close();
			p.waitFor();
			
			if(i%10==0)
				System.out.println("id:"+i+" finish lda");
			
			br = new BufferedReader(new FileReader(topic_keys));
			ArrayList<ScoredTerm> list = new ArrayList<ScoredTerm>();
			while(true){
				String line = br.readLine();
				if(line == null)
					break;
				String[] part = line.split("\t");
				if(part.length != 3)
					continue;
				String phrase = part[1].replaceAll("_", " ");
				list.add(new ScoredTerm(phrase, Double.parseDouble(part[2])));
			}
			br.close();
			ScoredTerm[] st = new ScoredTerm[list.size()];
			list.toArray(st);
			Arrays.sort(st);
			res.add(st);
			count = 0;
		}
		for(File f: new File(engine+"_tmp_root").listFiles())
			if(!f.delete())
				f.deleteOnExit();
		if(!new File(engine+"_tmp_root").delete())
			new File(engine+"_tmp_root").deleteOnExit();
		if(!new File(engine+"_tmp_root.mallet").delete())
			new File(engine+"_tmp_root.mallet").deleteOnExit();
		if(!new File(engine+"_lda_topic.txt").delete())
			new File(engine+"_lda_topic.txt").deleteOnExit();
	}
	
	public String getQuestion(int qid){
		return question.get(qid-1);
	}
	
//	public String getQuery(int qid){
//		return query.get(qid-1);
//	}
	
	public ScoredTerm[] getEnrich(int qid){
		return res.get(qid-1);
	}
	
	public void output(String localPath) throws Exception{
		PrintWriter pw = new PrintWriter(dir + File.separator + localPath);
		for(int qid=1; qid<894; qid++){
			ScoredTerm[] st = getEnrich(qid);
			String enrich = "";
			int count = 0;
			String question = getQuestion(qid).toLowerCase();
			for(int i=0; i<st.length && count<10; i++)
				if(!question.contains(st[i].name)){
					enrich += st[i].name + '\t';
					count ++;
				}
			pw.println(qid+" "+enrich.trim());
		}
		pw.close();
	}
	
	public File[] listEnrichFiles(){
		return new File(this.dir).listFiles(new FileFilter(){

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if(arg0.getName().endsWith("enrich.txt"))
					return true;
				return false;
			}
			
		});
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		if(args.length < 4){
			System.out.println("Usage: java -jar candidate [root] [engine(-)] [method(-)] [topk]");
			System.out.println("Usage: java -jar enrich [root] [topk-crawl] [topk-eval] [.enrich?]");
			return;
		}
		Enrichment en = new Enrichment(args[1]);
		if(args[0].equals("candidate")){
			int topk = Integer.parseInt(args[4]);
			en.setTop(topk);
			String[] engine = args[2].split("-");
			String[] method = args[3].split("-");
			for(String e: engine){
				for(String m: method){
					if(new File(en.dir+File.separator+e+"_"+m+"_"+topk+"_candidate.txt").exists())
						continue;
					if(m.equals("tfidf"))
						en.tfidf(e);
					else if(m.equals("cluster"))
						en.cluster(e);
					else if(m.equals("lda"))
						en.lda(e);
					else System.out.println("no such method");
					en.output(e+"_"+m+"_"+topk+"_candidate.txt");
				}
			}
		}
		else if(args[0].equals("enrich")){
			Evaluation.loadAnsPattern("answer.txt", "answer_pattern.txt");
			System.out.println("loading answers finished!");
			File[] enrich = null;
			if(args.length <= 4) enrich = en.listEnrichFiles();
			else enrich = new File[]{new File(args[1]+File.separator+args[4])};
			for(File f: enrich){
				String[] tmp = f.getName().split("_");
				String engine = tmp[0];
				String method = tmp[1];
				String topk = tmp[2];
				TreeMap<Integer, String> qid_file = new TreeMap<Integer, String>();
				BufferedReader br = new BufferedReader(new FileReader(f));
				while(true){
					String line = br.readLine();
					if(line == null)
						break;
					if(line.trim().indexOf(' ') == -1)
						continue;
					int qid = Integer.parseInt(line.substring(0, line.indexOf(' ')));
					String plus = line.substring(line.indexOf(' ')+1);
					BufferedReader brr = new BufferedReader(new FileReader(args[1]+File.separator+engine+File.separator+qid+".txt"));
					String origin = brr.readLine();
					brr.close();
					origin = origin.substring(origin.indexOf(' ')+1);
					String query = origin+" "+plus;
					FrameWork.search(args[1], engine, engine+"_"+method+"_"+topk+"_"+args[2], qid, query, Integer.parseInt(args[2]));
					qid_file.put(qid, engine+"_"+method+"_"+topk+"_"+args[2]+File.separator+qid+".txt");
				}
				br.close();
				FrameWork.evaluation(qid_file, args[1], Integer.parseInt(args[3]), engine+"_"+method+"_"+topk+"_"+args[2]+File.separator+"eval_here-"+args[3]+".txt");
				
				TreeMap<Integer, String> additional = new TreeMap<Integer, String>();
				for(int i=1; i<894; i++){
					if(!qid_file.containsKey(i))
						additional.put(i, engine+File.separator+i+".txt");
				}
				qid_file.putAll(additional);
				FrameWork.evaluation(qid_file, args[1], Integer.parseInt(args[3]), engine+"_"+method+"_"+topk+"_"+args[2]+File.separator+"eval-"+args[3]+".txt");
			}
		}
	}
	
	private class ScoredTerm implements Comparable<ScoredTerm>{
		double score;
		String name;
		public ScoredTerm(String n, double s){
			score = s;
			name = n;
		}
		public String toString(){
			return name+"("+score+")";
		}
		@Override
		public int compareTo(ScoredTerm arg0) {
			// TODO Auto-generated method stub
			if(arg0.score != this.score)
				return Double.compare(arg0.score, this.score);
			return arg0.name.split(" ").length - this.name.split(" ").length;
		}
	}

}
