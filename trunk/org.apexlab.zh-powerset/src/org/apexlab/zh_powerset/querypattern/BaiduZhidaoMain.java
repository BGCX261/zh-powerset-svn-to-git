package org.apexlab.zh_powerset.querypattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apexlab.zh_powerset.datasource.WikipediaZhMain;


public class BaiduZhidaoMain {
	private static Logger logger = Logger.getLogger(WikipediaZhMain.class);
	public static String inputDir, outputDir, zhidaoDir;
	public static String allQuestionsFile;
	public static void readCFGFile(String path){
		try{
			Properties prop = new Properties();
			InputStream input = new FileInputStream(path);
			prop.load(input);
			
			//get cfg root
			BaiduZhidaoMain.inputDir = prop.getProperty("BaiduZhidaoMain.inputDir");
			if(inputDir != null)
				inputDir = inputDir.trim() + File.separator;
			BaiduZhidaoMain.outputDir = prop.getProperty("BaiduZhidaoMain.outputDir");
			if(outputDir != null)
				outputDir = outputDir.trim() + File.separator;
			BaiduZhidaoMain.zhidaoDir = prop.getProperty("BaiduZhidaoMain.zhidaoDir");
			if(zhidaoDir != null)
				zhidaoDir = zhidaoDir.trim() + File.separator;
			
			//get cfg file
			BaiduZhidaoMain.allQuestionsFile = prop.getProperty("BaiduZhidaoMain.allQuestionsFile");
			if(allQuestionsFile != null)
				allQuestionsFile = inputDir + allQuestionsFile.trim();
			
			printParameters();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void printParameters(){
		logger.info("================= finished reading cfg file ===============");
		logger.info("inputDir: " + inputDir);
		logger.info("outputDir: " + outputDir);
		logger.info("zhidaoDir: " + zhidaoDir);
		logger.info("allQuestionsFile: " + allQuestionsFile);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure(args[0]);
		readCFGFile(args[1]);
		new BaiduZhidaoCollector().collectFromDir(BaiduZhidaoMain.zhidaoDir, BaiduZhidaoMain.allQuestionsFile);
	}

}
