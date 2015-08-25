package org.apexlab.zh_powerset.querypattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BaiduZhidaoCollector {

	private static Logger logger = Logger.getLogger(BaiduZhidaoCollector.class);
	private static final String[] matchList = {"<title>", "_百度知道", "zhidao.baidu.com/q?ct=20&qid=", 
		"&pn=65535&rn=25&tn=rssqb", "<div class=\"path\">", "/question/", ".html?fr=qrl", "查看同主题问题"};
	
	public void collectFromHtmlFile(String filePath, BufferedReader input, PrintWriter output){
		try {
			boolean getStrandId = false, getHier = false, getRelative = false, getTopic = false;
			for(String line = input.readLine(); line != null; line = input.readLine()){
				if(!getStrandId){
					int loc1 = line.indexOf(matchList[2]);
					int loc2 = line.indexOf(matchList[3]);
					if(loc1 != -1 && loc2 != -1){
						String id = line.substring(loc1 + matchList[2].length(), loc2);
						output.print(id + "\t");
						getStrandId = true;
					}
					loc1 = line.indexOf(matchList[0]);
					loc2 = line.indexOf(matchList[1]);
					if(loc1 != -1 && loc2 != -1){
						String question = line.substring(loc1 + matchList[0].length(), loc2);
						output.print(question + "\t");
						getStrandId = true;
					}
				}
				else if(!getHier){
					if(line.indexOf(matchList[4]) != -1){
						String[] path = removeHTMLTag(line, "\t").split("\t\u0026gt\u003b\t");
						output.print(path[0]);
						for(int i=1; i<path.length; i++)
							output.print("," + path[i]);
						output.print("\t");
						getHier = true;
					}
				}
				else if(!getRelative){
					int loc1 = line.indexOf(matchList[5]);
					int loc2 = line.indexOf(matchList[6]);
					if(loc1 != -1 && loc2 != -1){
						StringBuffer sb = new StringBuffer();
						while(loc1 != -1 && loc2 != -1){
							sb.append(line.substring(loc1 + matchList[5].length(), loc2) + ",");
							line = input.readLine().trim();
							loc1 = line.indexOf(matchList[5]);
							loc2 = line.indexOf(matchList[6]);
						}
						output.print(sb.toString().substring(0, sb.length()-1) + "\t");
						getRelative = true;
					}
				}
				else if(!getTopic){
					if(line.indexOf(matchList[7]) != -1){
						String[] topic = removeHTMLTag(line, "\t").split("\t");
						output.print(topic[1]);
						for(int i=2; i<topic.length; i++)
							output.print("," + topic[i]);
						output.print("\t" + filePath);
						getTopic = true;
					}
				}
				else break;
			}
			if(getStrandId){
				output.println();
				output.flush();
			}
		} catch (Exception e) {
			logger.info(filePath);
			e.printStackTrace();
		}
	}
	
	private static  String removeHTMLTag(String line, String sep){
		StringBuilder res = new StringBuilder(line);
		int loc1 = res.indexOf("<");
		int loc2 = res.indexOf(">");
		while(loc1 != -1 && loc2 != -1){
			res.replace(loc1, loc2 +1, sep);
			loc1 = res.indexOf("<");
			loc2 = res.indexOf(">");
		}
		return res.toString().trim().replaceAll("(\\s*("+sep+")+)+\\s*", sep);
	}
	
	public void collectFromDir(String dir, String output){
		try {
			new File(output).getParentFile().mkdirs();
			PrintWriter pw = new PrintWriter(new FileWriter(output));
			BufferedReader br;
			File dirRoot = new File(dir);
			LinkedList<File> checkDir = new LinkedList<File>();
			checkDir.offer(dirRoot);
			int gcounter = 0;
			while(checkDir.size() != 0){
				File currentDir = checkDir.poll();
				File[] subFiles = currentDir.listFiles(new FileFilter(){
					@Override
					public boolean accept(File arg0) {
						return arg0.isDirectory() || arg0.getName().endsWith(".html");
					}
				});
				logger.info("checking dir "+currentDir.getAbsolutePath()+" ["+subFiles.length+" subFiles]");
				int lcounter = 0;
				for(File subFile: subFiles){
					if(subFile.isDirectory()){
						checkDir.offer(subFile);
						continue;
					}
					lcounter ++;
					br = new BufferedReader(new FileReader(subFile));
					collectFromHtmlFile(subFile.getParentFile().getName() + File.separator + subFile.getName(), br, pw);
					br.close();
				}
				gcounter += lcounter;
				logger.info(lcounter + " html files parsed!");
			}
			logger.info("Totally "+gcounter + " html files parsed!");
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
