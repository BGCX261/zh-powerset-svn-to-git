package org.apexlab.zh_powerset.datasource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

/**
 * This class is responsible for the decoding task, e.g. decode /uxxxx to chinese word.
 * @author kaifengxu
 *
 */
public class WikipediaDecoder {

	private static Logger logger = Logger.getLogger(WikipediaDecoder.class);
	
	/**
	 * decode /uxxxx in "..." to Chinese traditional words for inFile.
	 * @param inFile
	 * @param outFile
	 */
	public void decodeFile(String inFile, String outFile){
		try {
			logger.info("start decoding "+inFile);
			LineNumberReader lnr = new LineNumberReader(new FileReader(inFile));
			new File(outFile).getParentFile().mkdirs();
			OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8");
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(lnr.getLineNumber() % 100000 == 0)
					logger.info("check line "+lnr.getLineNumber());
				int loc1 = line.indexOf('\"'), loc2 = line.lastIndexOf('\"');
				if(loc1 >= loc2){
					pw.write(line + "\r\n");
					continue;
				}
				String encoding = line.substring(loc1+1, loc2);
				String decoding = decodeLine(encoding);
//				System.out.println(encoding+"\r\n"+decoding.toString()+"\r\n\r\n");
				pw.write(line.substring(0, loc1+1));
				pw.write(decoding.toString());
				pw.write(line.substring(loc2));
				pw.write("\r\n");
			}
			lnr.close();
			logger.info("finish decoding!");
			pw.close();
			logger.info("write to "+outFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Decode one line.
	 * @param encoding
	 * @return
	 */
	private String decodeLine(String encoding){
		String[] part = encoding.split("\\\\u");
		StringBuffer decoding = new StringBuffer();
		decoding.append(part[0]);
		for(int i=1; i<part.length; i++){
			if(part[i].length() >= 4 && part[i].substring(0, 4).matches("[0-91-fA-F]{4}?")){
				decoding.append((char)Integer.valueOf(part[i].substring(0, 4), 16).intValue());
				if(part[i].length() > 4)
					decoding.append(part[i].substring(4));
			}
			else decoding.append(part[i]);
		}
		return decoding.toString();
	}

}
