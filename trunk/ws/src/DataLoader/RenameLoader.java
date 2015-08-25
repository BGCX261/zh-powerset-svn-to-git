package DataLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import DataHolder.RenameHolder;
import Pagelink.Env;

public class RenameLoader {
	
	public static void load(String path){
		if(Env.GetEnv().TO_MEMORY == 1)
			load2memory(path);
		else if(Env.GetEnv().TO_DISK == 1)
			load2disk(path);
	}
	
	private static void load2memory(String path) {
		System.out.println("Start loading rename to memory...");
		try {
			// read file
			LineNumberReader br = new LineNumberReader(new InputStreamReader(new FileInputStream(path), "utf8"));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (br.getLineNumber() % 100000 == 0)
					System.out.println(br.getLineNumber() + "@rename");
				
				//split with \t
				String[] e = line.split("\t");
				
				//add into map
				RenameHolder.Instance().add(e[0], new String[]{e[1]});
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void load2disk(String path) {
		System.out.println("Start loading rename to disk...");
		
		File dir = new File(Env.GetEnv().RENAME_PATH.substring(0, Env.GetEnv().RENAME_PATH.lastIndexOf('.')));
		if(dir.exists() && dir.isDirectory())
			return;
		
		try {
			// read file
			LineNumberReader br = new LineNumberReader(new InputStreamReader(new FileInputStream(path), "utf8"));
			String pre = null;
			ArrayList<String> info = null;
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (br.getLineNumber() % 100000 == 0)
					System.out.println(br.getLineNumber() + "@rename");
				
				//split with \t
				String[] e = line.split("\t");
				
				//check
				if(pre == null || !pre.equals(e[0])){
					if(pre !=null && info != null && info.size() > 0){
						String[] tmp = new String[info.size()];
						info.toArray(tmp);
						RenameHolder.Instance().add(pre, tmp);
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
				RenameHolder.Instance().add(pre, tmp);
			}
			RenameHolder.Instance().rename2.commit();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
