package org.apexlab.zh_powerset.datasource;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * This class collect zh wikipedia data and related en wikipedia art-cat cat-lables and catskos
 * @author kaifengxu
 *
 */
public class WikipediaZhCollector {
	
	private static Logger logger = Logger.getLogger(WikipediaZhCollector.class);

	/**
	 * Get possible art-cat from enArtCatFile by considering the arts from zhArtLabelFile
	 * @param zhArtLabelFile
	 * @param enArtCatFile
	 * @param output
	 */
	public void collectZhCategories(String zhArtLabelFile, String enArtCatFile, String oArtLabelFile, String oArtCatFile){
		try{
//			build a set from zhArtLabelFile
			logger.info("start scanning zhArtLabel!");
			HashSet<String> zhArtSet = new HashSet<String>();
			LineNumberReader lnr = new LineNumberReader(new FileReader(zhArtLabelFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine())
				zhArtSet.add(line.substring(0, line.indexOf("> <")+1).toLowerCase());
			lnr.close();
			logger.info("finish scanning!");
			
//			check and reserve related triples in enArtCatFile
			logger.info("start scanning enArtCat!");
			lnr = new LineNumberReader(new FileReader(enArtCatFile));
			PrintWriter pw = new PrintWriter(new FileWriter(oArtCatFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(lnr.getLineNumber() % 100000 == 0)
					logger.info("check line "+lnr.getLineNumber());
				String resURI = line.substring(0, line.indexOf("> <")+1).toLowerCase();
				if(zhArtSet.contains(resURI))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish scanning!");
			pw.close();
			logger.info("write to "+oArtCatFile);
			
//			filter zhArtLabelFile with cat-existed art
			logger.info("start scanning oArtCat!");
			zhArtSet.clear();
			lnr = new LineNumberReader(new FileReader(oArtCatFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine())
				zhArtSet.add(line.substring(0, line.indexOf("> <")+1).toLowerCase());
			lnr.close();
			logger.info("finish scanning!");
			
			logger.info("start filter zhArtLabel!");
			lnr = new LineNumberReader(new FileReader(zhArtLabelFile));
			pw = new PrintWriter(new FileWriter(oArtLabelFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				String resURI = line.substring(0, line.indexOf("> <")+1).toLowerCase();
				if(zhArtSet.contains(resURI))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish scanning!");
			pw.close();
			logger.info("write to "+oArtLabelFile);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Collect the categories from oArtCatFile and filter enCatSkos and enCatLabel.
	 * @param oArtCatFile
	 * @param enCatSkosFile
	 * @param enCatLabelFile
	 * @param skosOutput
	 * @param catLabelOutput
	 */
	public void collectZhCatSkosAndLabel(String oArtCatFile, String enCatSkosFile, String enCatLabelFile, String skosOutput, String catLabelOutput){
		try {
			logger.info("start scanning oArtCatFile!");
			LineNumberReader lnr = new LineNumberReader(new FileReader(oArtCatFile));
			HashSet<String> oCatSet = new HashSet<String>();
			for(String line = lnr.readLine(); line != null; line = lnr.readLine())
				oCatSet.add(line.substring(line.lastIndexOf('<'), line.lastIndexOf('>')+1).toLowerCase());
			lnr.close();
			logger.info("finish scanning! total cat: "+oCatSet.size());
			
//			get catskos from en skos
			logger.info("start scanning enCatSkosFile!");
			HashMap<String, HashSet<String>> catSkos = new HashMap<String, HashSet<String>>();
			lnr = new LineNumberReader(new FileReader(enCatSkosFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(lnr.getLineNumber() % 100000 == 0)
					logger.info("check line "+lnr.getLineNumber());
				if(!line.contains("<http://www.w3.org/2004/02/skos/core#broader>")) continue;
				String[] part = line.substring(0, line.lastIndexOf('>')+1).toLowerCase().split(" <http://www.w3.org/2004/02/skos/core#broader> ");
				HashSet<String> parents = catSkos.get(part[0]);
				if(parents == null){
					parents = new HashSet<String>();
					catSkos.put(part[0], parents);
				}
				parents.add(part[1]);
			}
			lnr.close();
//			for(String key: catSkos.keySet())
//				logger.info(key+" | "+catSkos.get(key));
			logger.info("finish scanning!"+" total subcat: "+catSkos.size());
			
//			compute the clustering of cat
			logger.info("start computing cat clustering!");
			LinkedList<String> queue = new LinkedList<String>(oCatSet);

			while(queue.size() != 0){
				String cat = queue.poll();
				HashSet<String> parents = catSkos.get(cat);
				if(parents != null){
					for(String p: parents){
						if(!oCatSet.contains(p)){
							oCatSet.add(p);
							queue.offer(p);
						}
					}
				}
			}
			logger.info("finish computing! total cat: "+oCatSet.size());
			
//			filter enCatSkos
			logger.info("start filtering enCatSkos!");
			lnr = new LineNumberReader(new FileReader(enCatSkosFile));
			PrintWriter pw = new PrintWriter(new FileWriter(skosOutput));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(lnr.getLineNumber() % 100000 == 0)
					logger.info("check line "+lnr.getLineNumber());
				if(!line.contains("<http://www.w3.org/2004/02/skos/core#broader>")) continue;
				String[] part = line.substring(0, line.lastIndexOf('>')+1).toLowerCase().split(" <http://www.w3.org/2004/02/skos/core#broader> ");
				if(oCatSet.contains(part[0]) && oCatSet.contains(part[1]))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish filtering!");
			pw.close();
			logger.info("write to "+skosOutput);
			
//			filter enCatLabel
			logger.info("start filtering enCatLabel!");
			lnr = new LineNumberReader(new FileReader(enCatLabelFile));
			pw = new PrintWriter(new FileWriter(catLabelOutput));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(oCatSet.contains(line.substring(0, line.indexOf("> <")+1).toLowerCase()))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish filtering!");
			pw.close();
			logger.info("write to "+catLabelOutput);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Collect infoFile, shortAbstract and longAbstract through the articles in oArtLabelFile
	 * @param oArtLabelFile
	 * @param zhInfoFile
	 * @param zhShortAbsFile
	 * @param zhLongAbsFile
	 * @param infoboxOutput
	 * @param shAbsOutput
	 * @param longAbsOutput
	 */
	public void collectInfoboxAndAbstract(String oArtLabelFile, String enInfoFile, String zhShortAbsFile, String zhLongAbsFile, String infoboxOutput, String shAbsOutput, String longAbsOutput){
		try {
			logger.info("start scanning oArtLabel!");
			HashSet<String> oArtSet = new HashSet<String>();
			LineNumberReader lnr = new LineNumberReader(new FileReader(oArtLabelFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine())
				oArtSet.add(line.substring(0, line.indexOf("> <")+1).toLowerCase());
			lnr.close();
			logger.info("finish scanning!");
			
//			filter zhinfofile
			logger.info("start scanning zhInfoFile!");
			lnr = new LineNumberReader(new FileReader(enInfoFile));
			PrintWriter pw = new PrintWriter(new FileWriter(infoboxOutput));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				String resURI = line.substring(0, line.indexOf("> <")+1).toLowerCase();
				if(oArtSet.contains(resURI))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish scanning!");
			pw.close();
			logger.info("write to "+infoboxOutput);
			
//			filter abstracctfile
			logger.info("start scanning zhShortAbsFile!");
			lnr = new LineNumberReader(new FileReader(zhShortAbsFile));
			pw = new PrintWriter(new FileWriter(shAbsOutput));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				String resURI = line.substring(0, line.indexOf("> <")+1).toLowerCase();
				if(oArtSet.contains(resURI))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish scanning!");
			pw.close();
			logger.info("write to "+shAbsOutput);
			
			logger.info("start scanning zhLongAbsFile!");
			lnr = new LineNumberReader(new FileReader(zhLongAbsFile));
			pw = new PrintWriter(new FileWriter(longAbsOutput));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				String resURI = line.substring(0, line.indexOf("> <")+1).toLowerCase();
				if(oArtSet.contains(resURI))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish scanning!");
			pw.close();
			logger.info("write to "+longAbsOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Collect properties which exists in oInfoboxFile.
	 * @param oInfoboxFile
	 * @param zhPropertyFile
	 * @param output
	 */
	public void collectProperty(String oInfoboxFile, String enPropertyFile, String output){
		try {
			logger.info("start scanning oInfoboxFile!");
			HashSet<String> oPropSet = new HashSet<String>();
			LineNumberReader lnr = new LineNumberReader(new FileReader(oInfoboxFile));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				int loc = line.lastIndexOf("> \"");
				if(loc == -1)
					loc = line.lastIndexOf("> <");
//				logger.info(line.substring(line.indexOf("> <")+2, loc+1).toLowerCase());
				oPropSet.add(line.substring(line.indexOf("> <")+2, loc+1).toLowerCase());
			}
			lnr.close();
			logger.info("finish scanning!");
			
//			filter zhPropertyFile
			logger.info("start scanning zhPropertyFile!");
			lnr = new LineNumberReader(new FileReader(enPropertyFile));
			PrintWriter pw = new PrintWriter(new FileWriter(output));
			for(String line = lnr.readLine(); line != null; line = lnr.readLine()){
				if(!line.endsWith("\" .")) continue;
				String resURI = line.substring(0, line.indexOf("> <")+1).toLowerCase();
				if(oPropSet.contains(resURI))
					pw.println(line);
			}
			lnr.close();
			logger.info("finish scanning!");
			pw.close();
			logger.info("write to "+output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
