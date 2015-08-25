package Segment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import DataLoader.PagelinkLoader;
import DataLoader.RenameLoader;
import Pagelink.Env;

public class Driver2 {

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//create a process
		Env env = Env.GetEnv();
		Processor2 prc = new Processor2();
		System.out.println(env);
		init();
		
		//read sentence and prepare output
		LineNumberReader br = new LineNumberReader(new InputStreamReader(new FileInputStream(env.INPUT_PATH), "utf8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(env.OUTPUT_PATH), "utf8"));
		pw.println(env);
		
		String v;
		while ((v = br.readLine()) != null) {
			//process a sentence
			long startTime = System.currentTimeMillis();
			String cut = prc.Segment(v);
			long endTime = System.currentTimeMillis();
			
			System.out.println("#"+br.getLineNumber()+" "+cut); 
			pw.println("=============================================================");
			pw.println(br.getLineNumber()+".\t" + cut + "\ttakes "+(endTime-startTime)+"ms");
		}
		pw.close();
	}
	
	public static void init(){
		long initTimeStart = System.currentTimeMillis();
		PagelinkLoader.load(Env.GetEnv().PAGELINK_PATH);
		long initTimeEnd = System.currentTimeMillis();
		System.out.println("Loading pagelink finished. Takes "
				+ (initTimeEnd - initTimeStart) + "ms");
		initTimeStart = initTimeEnd;
		RenameLoader.load(Env.GetEnv().RENAME_PATH);
		initTimeEnd = System.currentTimeMillis();
		System.out.println("Loading rename finished. Takes "
				+ (initTimeEnd - initTimeStart) + "ms");
	}
}
