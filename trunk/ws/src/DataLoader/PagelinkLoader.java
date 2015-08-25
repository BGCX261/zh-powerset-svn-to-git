package DataLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import DataHolder.PagelinkHolder;
import Pagelink.Env;

public class PagelinkLoader {

	public static void load(String path){
		if(Env.GetEnv().TO_MEMORY == 1)
			load2memory(path);
		else if(Env.GetEnv().TO_DISK == 1)
			load2disk(path);
	}

	private static void load2memory(String path) {
		Map<String, Set<String[]>> pagelink = new HashMap<String, Set<String[]>>();
		System.out.println("Start loading pagelink to memory...");

		try {
			// read file
			LineNumberReader br = new LineNumberReader(new InputStreamReader(
					new FileInputStream(path), "utf8"));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (br.getLineNumber() % 100000 == 0)
					System.out.println(br.getLineNumber() + "@pagelink");

				// split with \t
				String[] e = line.split("\t");
				Set<String[]> v = pagelink.get(e[0]);
				if (v == null) {
					v = new HashSet<String[]>();
					pagelink.put(e[0], v);
				}
				if (e.length == 2)
					v.add(new String[] { e[1], "1" });
				else
					v.add(new String[] { e[1], e[2] });
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add into map
		for (Map.Entry<String, Set<String[]>> e : pagelink.entrySet()) {
			PagelinkHolder.Instance().add(e.getKey(), e.getValue());
		}
		pagelink.clear();
		pagelink = null;

	}
	
	private static void load2disk(String path) {
		System.out.println("Start loading pagelink to disk...");

		File dir = new File(Env.GetEnv().PAGELINK_PATH.substring(0, Env.GetEnv().PAGELINK_PATH.lastIndexOf('.')));
		if(dir.exists() && dir.isDirectory())
			return;
		
		try {
			// read file
			LineNumberReader br = new LineNumberReader(new InputStreamReader(
					new FileInputStream(path), "utf8"));
			String pre = null;
			ArrayList<String> info = null;
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (br.getLineNumber() % 100000 == 0)
					System.out.println(br.getLineNumber() + "@pagelink");

				// split with \t
				String[] e = line.split("\t");
				
				//check
				if(pre == null || !pre.equals(e[0])){
					if(pre !=null && info != null && info.size() > 0){
						String[] tmp = new String[info.size()];
						info.toArray(tmp);
						HashSet<String[]> set = new HashSet<String[]>();
						set.add(tmp);
						PagelinkHolder.Instance().add(pre, set);
					}
					info = new ArrayList<String>();
					pre = e[0];
				}
				info.add(e[1]);
			}
			
			//last check
			if(pre !=null && info != null && info.size() > 0){
				String[] tmp = new String[info.size()];
				info.toArray(tmp);
				HashSet<String[]> set = new HashSet<String[]>();
				set.add(tmp);
				PagelinkHolder.Instance().add(pre, set);
			}
			PagelinkHolder.Instance().pagelink2.commit();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
