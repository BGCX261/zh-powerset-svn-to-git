package LookAtMe;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class RunMe {
	private static int topk;
	private static int iter;
	private static double prob;
	private static int seg;
	private static String cut;
	private static QGraph g;

	private static void segment(String v) throws Exception {
		prob = 0;
		seg = 1;
		iter = 0;
		cut = "";
		g = null;
		Dictionary.clearCache();
		process(v, new ArrayList<QGraph>(), "");
	}

	private static String substring(String[] str, int startpoint, int endpoint) {
		if (startpoint >= str.length)
			return "";
		String strs = str[startpoint];
		for (int i = startpoint + 1; i < endpoint; i++)
			strs += " " + str[i];
		return strs;
	}

	private static void process(String remain, ArrayList<QGraph> curG,
			String curcut) throws Exception {
		iter++;
		// stop condition
		if (remain.length() == 0) {
			double max = 0;
			QGraph maxG = null;
			for (QGraph g : curG) {
				double normal = g.normalized_score();
				if (max < normal) {
					max = normal;
					maxG = g;
				}
			}
			if (prob < max) {
				cut = curcut;
				prob = max;
				g = maxG;
				seg = maxG.seg();
			}
			return;
		}

		String[] str = remain.split(" ");
		// look forward
		for (int i = (str.length > 5) ? 5 : str.length; i > 0; i--) {
			String word = substring(str, 0, i);
			String[] words = null;
			if (i > 2)
				words = WNSynSet.get(word);
			if (words == null)
				words = new String[] { word };
//			else System.out.println(words[0]);
			
			for (String w : words) {
				if(!w.equals(word))
					word += "{"+w+"}";
				Dictionary.DictTerm[] v = Dictionary.get(w, topk);

//				System.out.println(w);
				if (v.length == 0) {
					String tmpcut = curcut + " (" + word + ")";
					Dictionary.DictTerm[] unmatch = Dictionary.getESA(w, 1);
					double score;
					if (unmatch.length != 0
							&& unmatch[0].name().toLowerCase().contains(
									w.toLowerCase()))
						score = Dictionary.penalty;
					else
						score = Math.pow(Dictionary.penalty, 2 * i);
					ArrayList<QGraph> todoG = new ArrayList<QGraph>();
					if (curcut.equals("")) {
						QGraph todoqg = new QGraph();
						todoqg.addNode(w, score, 0);
						todoG.add(todoqg);
					}
					else{
						for (QGraph qg : curG) {
							if (QGraph.test(qg, score, seg) <= prob)
								continue;
							QGraph todoqg = qg.clone();
							todoqg.addUnmatches(w, score);
							todoG.add(todoqg);
						}
					}
					if (todoG.size() > 0)
						process(substring(str, i, str.length), todoG, tmpcut);
				} else {
					// cope with registered phrases
					String tmpcut = curcut + " [" + word + "]";
					// get current prob
//					if(word.equals("what")){
//						for(QGraph q: curG)
//							System.out.println(q+"\r\n"+prob);
//					}
					ArrayList<QGraph> todoG = new ArrayList<QGraph>();
					for (Dictionary.DictTerm dt : v) {
						if (curcut.equals("")) {
							QGraph todoqg = new QGraph();
							todoqg.addNode(dt.name(), dt.score(), dt.pagelink());
							todoG.add(todoqg);
						} else
							for (QGraph qg : curG) {
								if (QGraph.test(qg, dt.score(), seg) <= prob)
									continue;
								double max = 0;
								String dest = null;
								for (int j = 0; j < qg.nodeSize(); j++) {
									String n = qg.node(j);
									double s = Dictionary.sim(n, dt.name());
									if (s > max) {
										max = s;
										dest = n;
									}
								}
								if (QGraph.test(qg, dt.score() * max, seg) <= prob)
									continue;
								QGraph todoqg = qg.clone();
								todoqg.addNode(dt.name(), dt.score(), dt.pagelink());
								todoqg.addEdge(dest, dt.name(), max);
								todoG.add(todoqg);
							}
					}

					// package qualified prob
					if (todoG.size() > 0) {
						process(substring(str, i, str.length), todoG, tmpcut);
					}
				}
			}
		}
	}
	
	private static void formatSegmentation(String segmentation, String filename) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(segmentation));
		PrintWriter pw = new PrintWriter(filename);
		String query = null, qid = null;
		ArrayList<String> node = new ArrayList<String>(), edge = new ArrayList<String>();
		HashSet<String> umatch = new HashSet<String>();
		int cur = 0;
		while(true){
			String line = br.readLine();
			if(line == null)
				break;
			if(line.indexOf(' ')!=-1 && line.substring(0, line.indexOf(' ')).matches("[0-9]+")){
				qid = line.substring(0, line.indexOf(' '));
				query = line.substring(line.indexOf(' '), line.lastIndexOf('(')).trim();
			}
			else if(line.startsWith("N "))
				node.add(line.substring(line.indexOf(":")+2));
			else if(line.startsWith("E "))
				edge.add(line.substring(line.indexOf(":")+2));
			else if(line.startsWith("U "))
				umatch.add(line.substring(line.indexOf(":")+2, line.indexOf(" | ")));
			else if(line.startsWith("PROB ")){
				int loc = 0;
				pw.println("START: ======================================================");
				pw.println("QID: "+qid);
				while(loc < query.length()){
					char ch = query.charAt(loc);
					if(ch == '('){
						int end = query.indexOf(')', loc);
						String n = query.substring(loc+1, end);
						if(umatch.contains(n)){
							if(!FrameWork.stopPhrase(n))
								pw.println("NODE: "+n + " | unmatch");
						}
						else {
							if(!FrameWork.stopPhrase(n))
								pw.println("NODE: "+node.get(cur));
							cur ++;
						}
						loc = end + 2;
					}
					else if(ch == '['){
						int end = query.indexOf(']', loc);
						String n = query.substring(loc+1, end);
						if(!FrameWork.stopPhrase(n))
							pw.println("NODE: "+cur+" | " + n +" | "+node.get(cur));
						else {
							String[] edges = new String[edge.size()];
							edge.toArray(edges);
							for(String e: edges)
								if(e.startsWith(cur+" |") || e.endsWith("| "+cur))
									edge.remove(e);
						}
						cur ++;
						loc = end + 2;
					}
				}
				for(String e: edge)
					pw.println("EDGE: "+e);
				pw.println("END: ======================================================");
				pw.println();
				query = null;
				qid = null;
				node.clear();
				edge.clear();
				umatch.clear();
				cur = 0;
			}
		}
		pw.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 4) {
			System.out.println("Usage: java -jar [input] [(qid)from-to] [topk1-wordsize-topk2] [penalty]\r\n");
			System.out.println("Usage: java -jar format");
			if(args.length==1 && args[0].equals("format"))
				formatSegmentation("segmentation.txt", "format_segmentation.txt");
			return;
		}
		
		System.setProperty("wordnet.database.dir", "dict");
		LineNumberReader br = new LineNumberReader(new InputStreamReader(
				new FileInputStream(args[0]), "utf8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("segmentation.txt"), "utf8"));
		String[] idrange = args[1].split("-");
		int from = Integer.parseInt(idrange[0]);
		int to = Integer.parseInt(idrange[1]);
		String[] toprange = args[2].split("-");
		int topk1 = Integer.parseInt(toprange[0]);
		int topk2 = Integer.parseInt(toprange[2]);
		int wordsize = Integer.parseInt(toprange[1]);
		Dictionary.penalty = Double.parseDouble(args[3]);

		String v;
		while ((v = br.readLine()) != null) {
			// process a sentence
			int qid = Integer.parseInt(v.substring(0, v.indexOf(' ')));
			if(qid < from || qid > to)
				continue;
			
			String s = v.substring(v.indexOf(' ')+1);
			s = s.replaceAll("['\":()?/,-]", " ").replaceAll(" s ", " ").replaceAll(" +", " ").trim();
			
			if(s.split(" ").length > wordsize)
				topk = topk2;
			else topk = topk1;
			
			long startTime = System.currentTimeMillis();
			segment(s);
			long endTime = System.currentTimeMillis();

			System.out.println(qid + " " + cut + " (" + (endTime - startTime)
					+ "ms with " + iter + " iter)");
			System.out
					.println("======================================================");
			System.out.println(g.toString());
			System.out.println();

			pw.println(qid + " " + cut + " (" + (endTime - startTime)
					+ "ms with " + iter + " iter)");
			pw
					.println("======================================================");
			pw.println(g.toString());
			pw.println();
		}
		pw.close();
	}

	static class QGraph {
		private String[] nodes;
		private double[] nscores;
		private int[] npagelinks;
		private int nodeSize;

		private Integer[][] edges;
		private double[] escores;
		private int edgeSize;

		private String[] unmatches;
		private double[] umscores;
		private int umSize;

		private double score;
		private int len = 20;

		public QGraph() {
			nodes = new String[len];
			nscores = new double[len];
			npagelinks = new int[len];
			edges = new Integer[len][];
			escores = new double[len];
			unmatches = new String[len];
			umscores = new double[len];
			score = 1;
			nodeSize = 0;
			edgeSize = 0;
			umSize = 0;
		}

		@Override
		public QGraph clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			QGraph g = new QGraph();
			copy(g, len);
			return g;
		}

		private void copy(QGraph g, int newlen) {
			g.nodes = Arrays.copyOf(this.nodes, newlen);
			g.nscores = Arrays.copyOf(this.nscores, newlen);
			g.npagelinks = Arrays.copyOf(this.npagelinks, newlen);
			g.nodeSize = this.nodeSize;
			g.edges = Arrays.copyOf(this.edges, newlen);
			g.escores = Arrays.copyOf(this.escores, newlen);
			g.edgeSize = this.edgeSize;
			g.unmatches = Arrays.copyOf(this.unmatches, newlen);
			g.umscores = Arrays.copyOf(this.umscores, newlen);
			g.umSize = this.umSize;
			g.score = this.score;
			g.len = newlen;
		}

		public int nodeSize() {
			return nodeSize;
		}

		public String node(int i) {
			return nodes[i];
		}

		public double score() {
			return score;
		}

		public double normalized_score() {
			return Math.pow(score, 1.0 / (nodeSize + umSize));
		}

		public static double test(QGraph g, double score, int seg) {
			return Math.pow(g.score * score, 1.0 / seg);
		}

		public void addNode(String n, double s, int p) {
			if (nodeSize == len)
				copy(this, len * 2);
			nodes[nodeSize] = n;
			nscores[nodeSize] = s;
			npagelinks[nodeSize] = p;
			score *= s;
			nodeSize++;
		}

		public void addUnmatches(String n, double s) {
			if (umSize == len)
				copy(this, len * 2);
			unmatches[umSize] = n;
			umscores[umSize] = s;
			score *= s;
			umSize++;
		}

		public int indexNodeOf(String n) {
			for (int i = nodeSize - 1; i >= 0; i--)
				if (nodes[i].equals(n))
					return i;
			return -1;
		}

		public void addEdge(String n1, String n2, double s) throws Exception {
			if (s == Dictionary.penalty)
				score *= s;
			else {
				int id1 = indexNodeOf(n1);
				int id2 = indexNodeOf(n2);
				if (id1 == -1 || id2 == -1) {
					throw new IndexOutOfBoundsException();
				}
				edges[edgeSize] = new Integer[] { id1, id2 };
				escores[edgeSize] = s;
				score *= s;
				edgeSize++;
			}
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < nodeSize; i++)
				sb.append("N " + i + " : " + nodes[i] + " | " + nscores[i] + " | " + npagelinks[i]
						+ "\r\n");
			for (int i = 0; i < edgeSize; i++)
				sb.append("E : " + edges[i][0] + " | " + escores[i] + " | "
						+ edges[i][1] + "\r\n");
			for (int i = 0; i < umSize; i++)
				sb.append("U : " + unmatches[i] + " | " + umscores[i] + "\r\n");
			sb.append("PROB : " + score);
			return sb.toString();
		}
		
		public int seg(){
			return this.nodeSize + this.umSize;
		}
	}
}
