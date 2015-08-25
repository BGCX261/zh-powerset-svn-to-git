package LookAtMe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeMap;

public class EvalTenCategory {

	private static String[] CAT = new String[]{"IS1","IS2","IS3","IS4","IS5","IS6","IS7","IS8","DS1","DS2","DS3"};
	
	private static TreeMap<Integer, Integer> loadDistribution(String path) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(path));
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		int count = 1;
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			String[] cat = line.split("\t");
			for(int i=0; i<cat.length; i++)
				if(cat[i].equals("1")){
					map.put(count, i);
					break;
				}
			count ++;
		}
		br.close();
		return map;
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if(args.length < 2){
			System.out.println("Usage: java -jar root topk [dir?]");
			return;
		}
		TreeMap<Integer, Integer> cat = loadDistribution("question_distribution.txt");
		Evaluation.loadQuestions("question.txt");
		Evaluation.loadAnsPattern("answer.txt", "answer_pattern.txt");
		File[] dir = new File(args[0]).listFiles();
		for(File d: dir){
			if(args.length > 2 && !d.getName().equals(args[2]))
				continue;
			String engine = null;
			if(d.getName().indexOf('_')!= -1)
				engine = d.getName().substring(0, d.getName().indexOf('_'));
			TreeMap<Integer, String>[] qf = new TreeMap[CAT.length];
			for(int i=0; i<qf.length; i++)
				qf[i] = new TreeMap<Integer, String>();
			for(int qid=1; qid<894; qid++){
				if(new File(d.getAbsoluteFile()+File.separator+qid+".txt").exists())
					qf[cat.get(qid)].put(qid, d.getName()+File.separator+qid+".txt");
				else qf[cat.get(qid)].put(qid, engine+File.separator+qid+".txt");
			}
			for(int i=0; i<qf.length; i++){
				FrameWork.evaluation(qf[i], args[0], Integer.parseInt(args[1]), d.getName()+File.separator+"eval_"+CAT[i]+"-"+args[1]+".txt");
			}
		}
	}

}
