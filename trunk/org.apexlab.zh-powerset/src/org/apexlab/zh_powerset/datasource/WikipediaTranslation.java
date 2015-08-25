package org.apexlab.zh_powerset.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

/**
 * This class has two main mission: one is to translate en to zh and another is to translate zh-wt to zh-cn.
 * @author kaifengxu
 *
 */
public class WikipediaTranslation {

	private static Logger logger = Logger.getLogger(WikipediaTranslation.class);
	
//	import to adjust intervalTime a reasonable value!!!
	private static int waitingTime = 1*1000*3600;
	
	/**
	 * Translate en in "..." to zh for inputFile.
	 * @param input
	 * @param output
	 */
	public void translateFileFromEnToZh(String input, String output){
		translateFile(input, output, Language.ENGLISH, Language.CHINESE_SIMPLIFIED);
	}
	
	/**
	 * Translate zh-wt in "..." to zh-cn for inputFile.
	 * @param input
	 * @param output
	 */
	public void translateFileFromTwToCn(String input, String output){
		translateFile(input, output, Language.CHINESE_TRADITIONAL, Language.CHINESE_SIMPLIFIED);
	}
	
	/**
	 * Check the "..." and translate from-lang to to-lang.
	 * @param input
	 * @param output
	 * @param from
	 * @param to
	 */
	private void translateFile(String input, String output, String from, String to){
		try {
			logger.info("start translating " + input + "(" + from + " to " + to + ")");
//			if output exists then get the break point line
			int bPoint = -1;
			File outputFile = new File(output);
			if(outputFile.exists()){
				LineNumberReader lnr = new LineNumberReader(new FileReader(outputFile));
				for(String line = lnr.readLine(); line != null; line = lnr.readLine());
				bPoint = lnr.getLineNumber();
				logger.info("break point " + bPoint);
			}
			
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
			if(bPoint == -1)
				outputFile.getParentFile().mkdirs();
			OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(outputFile, true), "UTF-8");
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(lnr.getLineNumber() % 1000 == 0)
					logger.info("check line "+lnr.getLineNumber());
				if(lnr.getLineNumber() <= bPoint) continue;
				int loc1 = line.indexOf('\"'), loc2 = line.lastIndexOf('\"');
				if(loc1 >= loc2){
					pw.append(line + "\r\n");
					pw.flush();
					continue;
				}
				String src = line.substring(loc1+1, loc2);
				String tar = translateLine(src, from, to, waitingTime);
				pw.append(line.substring(0, loc1+1));
				pw.append(tar.toString());
				pw.append(line.substring(loc2));
				pw.append("\r\n");
				pw.flush();
			}
			lnr.close();
			logger.info("finish translating!");
			pw.close();
			logger.info("write to " + output);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * Translate one line by calling google language api.
	 * The mechanism is 100ms per translation query and to wait 1 hour when google is down = =!!!
	 * @param src
	 * @param from
	 * @param to
	 * @return
	 */
	private String translateLine(String src, String from, String to, int waitingTime) {
		try {
//			logger.debug(src+" | "+from +" | " +to);
			String translatedText = Translate.translate(src, from, to);
			Thread.sleep(randomIntervalTime(500, 1000));
			return translatedText;
		} catch (Exception e) {
			try {
				if(waitingTime > 4) return src;
				logger.fatal("waiting " + (waitingTime/(1000*3600)) + " hour...");
				Thread.sleep(waitingTime);
				return translateLine(src, from, to, waitingTime*2);
			} catch (Exception e2) {}
		}
		return null;
	}
	
	/**
	 * Get random interval time
	 * @return
	 */
	private int randomIntervalTime(int low, int high){
		return (int)(Math.random()*(high-low)) + low;
	}

}
