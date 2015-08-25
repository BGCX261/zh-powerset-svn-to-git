package LookAtMe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;

public class RunMe2 {

	private static String[][] load(String file) throws Exception {
		String[][] ret = new String[893][];
		BufferedReader br = new BufferedReader(new FileReader(file));
		int count = -1;
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			count++;
			if (line.indexOf(' ') == -1)
				continue;
			ret[count] = line.substring(line.indexOf(' ') + 1).split("\t");
		}
		br.close();
		return ret;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if(args.length < 2){
			System.out.println("Usage: java -jar rename pagelink dir[_candidate.txt]");
			return;
		}
		File[] candidate_file = new File(args[2]).listFiles(new FileFilter(){

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if(arg0.getName().endsWith("_candidate.txt"))
					return true;
				return false;
			}
			
		});
		String[][] entity = load(args[2]+File.separator+"entity.txt");
		for (File cf: candidate_file) {
			String t = args[2]+File.separator+cf.getName();
			if(new File(t.replace("candidate", "enrich")).exists())
				continue;
			PrintWriter pw = new PrintWriter(t.replace("candidate", "enrich"));
			String[][] topic = load(t);
			System.out.println("processing " + t);
			for (int i = 0; i < 893; i++) {
				HashSet<String> candidates = new HashSet<String>();
				String hit = "";
				for (String tt : topic[i]) {
					for (Dictionary.DictTerm dt : Dictionary.getESA(tt, 1)) {
						if (dt.score() > Double.parseDouble(args[0]))
							candidates.add(dt.name());
					}
				}
				for (String tt : candidates) {
					for (String en : entity[i]) {
//						boolean unlinked = en.substring(0, en.indexOf(':'))
//								.equals("U");
						en = en.substring(en.indexOf(':') + 1);
						double s = Dictionary.sim(tt, en);
						if (s > Double.parseDouble(args[1]) && s < 1 && tt.split(" ").length <= 3) {
							hit += "\"" + tt + "\" ";
							break;

						}
					}
				}
				pw.println((i + 1) + " " + hit.trim());
				if (i % 80 == 0)
					System.out.println("finish " + i);
			}
			pw.close();
		}
	}

}
