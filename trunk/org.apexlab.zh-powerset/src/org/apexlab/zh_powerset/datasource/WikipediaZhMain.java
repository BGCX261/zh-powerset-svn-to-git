package org.apexlab.zh_powerset.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This class is the main entry of zh-wikipedia data generation. 
 * Function readCFGFile will read the configuration file to get necessary parameters.
 * Function main is the main function to run all the data collection, decoding and translation functions.
 * @author kaifengxu
 *
 */
public class WikipediaZhMain {
	
	private static Logger logger = Logger.getLogger(WikipediaZhMain.class);

//	input files
	public static String zhArtLabelFile, zhShortAbsFile, zhLongAbsFile;
	public static String enCatLabelFile, enCatSkosFile, enArtCatFile, enInfoFile, enPropertyFile;
	
//	output files
	public static String oArtLabelFile = "zh_articles_label.nt", 
							oCatLabelFile = "en_categories_label.nt", 
							oArtCatFile = "en_article_category.nt", 
							oCatSkosFile = "en_categories_skos.nt", 
							oInfoboxFile = "en_infobox.nt", 
							oPropertyFile = "en_properties_label.nt",
							oShortAbsFile = "zh_short_abstract.nt",
							oLongAbsFile = "zh_long_abstract.nt";
	
//	decode and translate files
	public static final String decodeDir = "decoded"+File.separator, translateDir = "translated"+File.separator;
	public static String oArtLabelFileDecoded, oShortAbsFileDecoded, oLongAbsFileDecoded;
	public static String oCatLabelFileTranslated, oPropertyFileTranslated, oInfoboxFileTranslated,
	oArtLabelFileDecodedTranslated, oShortAbsFileDecodedTranslated, oLongAbsFileDecodedTranslated;
	
//	input and output root
	public static String outputDir, inputDir;
	
	/**
	 * Read configuration file from "./ds.cfg".
	 * @param path
	 */
	public static void readCFGFile(String path){
		try{
			Properties prop = new Properties();
			InputStream input = new FileInputStream(path);
			prop.load(input);
			
			//get cfg root
			WikipediaZhMain.inputDir = prop.getProperty("WikipediaZhMain.inputDir");
			if(inputDir != null)
				inputDir = inputDir.trim() + File.separator;
			WikipediaZhMain.outputDir = prop.getProperty("WikipediaZhMain.outputDir");
			if(outputDir != null)
				outputDir = outputDir.trim() + File.separator;
			
			//get cfg file
			WikipediaZhMain.zhArtLabelFile = prop.getProperty("WikipediaZhMain.zhArtLabelFile");
			if(zhArtLabelFile != null)
				zhArtLabelFile = inputDir + zhArtLabelFile.trim();
			WikipediaZhMain.enInfoFile = prop.getProperty("WikipediaZhMain.enInfoFile");
			if(enInfoFile != null)
				enInfoFile = inputDir + enInfoFile.trim();
			WikipediaZhMain.enPropertyFile = prop.getProperty("WikipediaZhMain.enPropertyFile");
			if(enPropertyFile != null)
				enPropertyFile = inputDir + enPropertyFile.trim();
			WikipediaZhMain.zhShortAbsFile = prop.getProperty("WikipediaZhMain.zhShortAbsFile");
			if(zhShortAbsFile != null)
				zhShortAbsFile = inputDir + zhShortAbsFile.trim();
			WikipediaZhMain.zhLongAbsFile = prop.getProperty("WikipediaZhMain.zhLongAbsFile");
			if(zhLongAbsFile != null)
				zhLongAbsFile = inputDir + zhLongAbsFile.trim();
			
			WikipediaZhMain.enCatLabelFile = prop.getProperty("WikipediaZhMain.enCatLabelFile");
			if(enCatLabelFile != null)
				enCatLabelFile = inputDir + enCatLabelFile.trim();
			WikipediaZhMain.enCatSkosFile = prop.getProperty("WikipediaZhMain.enCatSkosFile");
			if(enCatSkosFile != null)
				enCatSkosFile = inputDir + enCatSkosFile.trim();
			WikipediaZhMain.enArtCatFile = prop.getProperty("WikipediaZhMain.enArtCatFile");
			if(enArtCatFile != null)
				enArtCatFile = inputDir + enArtCatFile.trim();
			
			input.close();
			
			initOutputParameters();
			printParameters();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Give values to the rest variables (mainly the output files' path) according to the input parameters.
	 */
	private static void initOutputParameters(){
//		init decode and translate files
		oArtLabelFileDecoded = outputDir + decodeDir + "dc_" + oArtLabelFile; 
		oShortAbsFileDecoded = outputDir + decodeDir + "dc_" + oShortAbsFile;
		oLongAbsFileDecoded = outputDir + decodeDir + "dc_" + oLongAbsFile;
		
		oCatLabelFileTranslated = outputDir + translateDir + "tl_" + oCatLabelFile;
		oPropertyFileTranslated = outputDir + translateDir + "tl_" + oPropertyFile;
		oInfoboxFileTranslated = outputDir + translateDir + "tl_" + oInfoboxFile;
		
		oArtLabelFileDecodedTranslated = outputDir + translateDir + "tl_dc_" + oArtLabelFile; 
		oShortAbsFileDecodedTranslated = outputDir + translateDir + "tl_dc_" + oShortAbsFile;
		oLongAbsFileDecodedTranslated = outputDir + translateDir + "tl_dc_" + oLongAbsFile;
		
//		init output files
		oArtLabelFile = outputDir + oArtLabelFile; 
		oCatLabelFile = outputDir + oCatLabelFile;
		oArtCatFile = outputDir + oArtCatFile;
		oCatSkosFile = outputDir + oCatSkosFile; 
		oInfoboxFile = outputDir + oInfoboxFile;
		oPropertyFile = outputDir + oPropertyFile;
		oShortAbsFile = outputDir + oShortAbsFile;
		oLongAbsFile = outputDir + oLongAbsFile;
	}
	
	/**
	 * Print the input files' path which are read from cfg file.
	 */
	private static void printParameters(){
		logger.info("================= finished reading cfg file ===============");
		logger.info("inputDir: " + inputDir);
		logger.info("outputDir: " + outputDir);
		logger.info("zhArtLabelFile: " + zhArtLabelFile);
		logger.info("enInfoFile: " + enInfoFile);
		logger.info("enPropertyFile: " + enPropertyFile);
		logger.info("zhShortAbsFile: " + zhShortAbsFile);
		logger.info("zhLongAbsFile: " + zhLongAbsFile);
		logger.info("enCatLabelFile: " + enCatLabelFile);
		logger.info("enCatSkosFile: " + enCatSkosFile);
		logger.info("enArtCatFile: " + enArtCatFile);
	}
	
	/**
	 * Run this function, you can get the entire zh-wiki-data needed.
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 2){
			System.out.println("Usage: jave -Xmx1G WikipediaZhMain [logCFGPath] [inputCFGPath]");
			System.exit(0);
		}
//		load cfg files
		PropertyConfigurator.configure(args[0]);
		readCFGFile(args[1]);
		
////		collect data
//		WikipediaZhCollector collector = new WikipediaZhCollector();
//		collector.collectZhCategories(zhArtLabelFile, enArtCatFile, oArtLabelFile, oArtCatFile);
//		collector.collectInfoboxAndAbstract(oArtLabelFile, enInfoFile, zhShortAbsFile, zhLongAbsFile, oInfoboxFile, oShortAbsFile, oLongAbsFile);
//		collector.collectProperty(oInfoboxFile, enPropertyFile, oPropertyFile);
//		collector.collectZhCatSkosAndLabel(oArtCatFile, enCatSkosFile, enCatLabelFile, oCatSkosFile, oCatLabelFile);
//	
////		decode data
//		WikipediaDecoder decoder = new WikipediaDecoder();
//		decoder.decodeFile(oArtLabelFile, oArtLabelFileDecoded);
//		decoder.decodeFile(oShortAbsFile, oShortAbsFileDecoded);
//		decoder.decodeFile(oLongAbsFile, oLongAbsFileDecoded);
//		
////		translate data
		WikipediaTranslation translator = new WikipediaTranslation();
//		translator.translateFileFromEnToZh(oCatLabelFile, oCatLabelFileTranslated);
//		translator.translateFileFromEnToZh(oPropertyFile, oPropertyFileTranslated);
//		translator.translateFileFromEnToZh(oInfoboxFile, oInfoboxFileTranslated);
//		
//		translator.translateFileFromTwToCn(oArtLabelFileDecoded, oArtLabelFileDecodedTranslated);
		translator.translateFileFromTwToCn(oShortAbsFileDecoded, oShortAbsFileDecodedTranslated);
		translator.translateFileFromTwToCn(oLongAbsFileDecoded, oLongAbsFileDecodedTranslated);
	}
}
