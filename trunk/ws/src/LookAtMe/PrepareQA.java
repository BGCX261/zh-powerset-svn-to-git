package LookAtMe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

public class PrepareQA {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main4(String[] args) throws Exception {
		// TODO Auto-generated method stub
		PrintWriter pw = new PrintWriter("question.txt");
		BufferedReader[] brs = new BufferedReader[2];
		brs[0] = new BufferedReader(new FileReader(
				"C:\\root\\wqa\\trec\\topics.qa_questions.txt"));
		brs[1] = new BufferedReader(new FileReader(
				"C:\\root\\wqa\\trec\\qa_questions_201-893.txt"));
		int count = 1;
		for (BufferedReader br : brs) {
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (line.equals("<desc> Description:")) {
					pw.println((count++)+" "+br.readLine());
				}
			}
			br.close();
		}
		pw.close();
	}

	public static void main1(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(
				"question.txt"));
//		HashSet<String> set = new HashSet<String>();
			while (true) {
				String line = br.readLine();
				if (line == null )
					break;
				
				if(!line.replaceAll("[a-zA-Z0-9 \\?]", "").equals("")){
					System.out.println(line.replaceAll("[a-zA-Z0-9 \\?]", ""));
				}

			}
	}
	public static void main5(String[] args) throws Exception {
		// TODO Auto-generated method stub
		PrintWriter pw = new PrintWriter("question_answer.txt");
		BufferedReader[] brs = new BufferedReader[2];
		brs[0] = new BufferedReader(new FileReader(
				"question.txt"));
		brs[1] = new BufferedReader(new FileReader(
				"answer.txt"));
			while (true) {
				String line1 = brs[0].readLine();
				String line2 = brs[1].readLine();
				if (line1 == null || line2 == null)
					break;

					pw.println(line1);
					pw.println(line2);
					pw.println();

			}
	
		pw.close();
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		PrintWriter pw = new PrintWriter("answer.txt");
		BufferedReader[] brs = new BufferedReader[2];
		brs[0] = new BufferedReader(new FileReader(
				"C:\\root\\wqa\\trec\\orig.qanswers.only"));
		brs[1] = new BufferedReader(new FileReader(
				"C:\\root\\wqa\\trec\\original_answers.txt"));
		int count = 0;
		String id = null;
		while (true) {
			String line = brs[0].readLine();
			if (line == null)
				break;
			if(id == null || !id.equals(line.substring(0, line.indexOf(' ')))){
				count ++;
				id = line.substring(0, line.indexOf(' '));
			}
			pw.println((count)+" "+line.substring(line.indexOf(' ') + 1));
		}
		while (true) {
			String line = brs[1].readLine();
			if (line == null)
				break;
			if(line.startsWith("Question ")){
				brs[1].readLine();
				brs[1].readLine();
				pw.println((++count)+" "+brs[1].readLine());
			}
		}
		pw.close();
	}
}
