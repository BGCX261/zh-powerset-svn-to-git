package LookAtMe;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.tools.bzip2.CBZip2InputStream;

public class BuildIndex {

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(
				"rename_title.data"));
		IndexWriter writer = new IndexWriter("rename", new StandardAnalyzer(),
				true, IndexWriter.MaxFieldLength.LIMITED);
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] part = line.split("\t");
			if (part.length != 2) {
				System.out.println(line);
				continue;
			}
			String rename = part[0].toLowerCase();
			if (rename.endsWith("(disambiguation)"))
				rename = rename.replace("(disambiguation)", "").trim();
			String title = part[1];
			HashSet<String> set = map.get(rename);
			if (set == null) {
				set = new HashSet<String>();
				map.put(rename, set);
			}
			set.add(title);
		}
		for (String rename : map.keySet()) {
			Document doc = new Document();
			doc.add(new Field("R", rename, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
			for (String title : map.get(rename)) {
				doc.add(new Field("L", title, Field.Store.YES,
						Field.Index.NO));
			}
			writer.addDocument(doc);
		}

		br = new BufferedReader(new FileReader("infoboxproperties_en.nt"));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			line = br.readLine();

			String url = line.substring(0, line.indexOf(' '));
			url = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('>'))
					.trim();
			String rename = line.substring(line.indexOf('\"') + 1,
					line.lastIndexOf('\"')).trim();
			if (url.equals("") || rename.equals(""))
				continue;
			Document doc = new Document();
			doc.add(new Field("L", url, Field.Store.YES, Field.Index.NO));
			doc.add(new Field("R", rename.toLowerCase(), Field.Store.NO,
					Field.Index.NOT_ANALYZED_NO_NORMS));
			writer.addDocument(doc);
		}
		writer.commit();
		writer.optimize();
	}

	public static void main2(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(
				"articles_label_en.nt"));
		PrintWriter pw = new PrintWriter("rename_title.data");
		HashSet<String> articles = new HashSet<String>();
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			String url = line.substring(0, line.indexOf(' '));
			String localname = URLDecoder.decode(
					url.substring(url.lastIndexOf('/') + 1, url
							.lastIndexOf('>')), "utf8").replaceAll("_", " ")
					.trim();
			if (!localname.equals("")
					&& !localname.endsWith("(disambiguation)")) {
				articles.add(localname);
				pw.println(localname + '\t' + localname);
			}
		}
		br.close();

		BufferedReader[] b = new BufferedReader[] {
				new BufferedReader(new FileReader("redirect_en.nt")),
				new BufferedReader(new FileReader("disambiguation_en.nt")) };
		for (int i = 0; i < b.length; i++) {
			while (true) {
				String line = b[i].readLine();
				if (line == null)
					break;
				String[] part = line.split(" ");
				if (part.length != 4)
					continue;
				String pre = URLDecoder.decode(
						part[0].substring(part[0].lastIndexOf('/') + 1, part[0]
								.lastIndexOf('>')), "utf8")
						.replaceAll("_", " ").trim();
				String post = URLDecoder.decode(
						part[2].substring(part[2].lastIndexOf('/') + 1, part[2]
								.lastIndexOf('>')), "utf8")
						.replaceAll("_", " ").trim();
				// System.out.println(line+" | "+pre+" | "+post);
				if (pre.equals("") || post.equals("") || !articles.contains(post))
					continue;
				pw.println(pre+'\t'+post);
			}
		}

		pw.close();
	}

	/**
	 * @param args
	 */
	public static void main1(String[] args) throws Exception {
		// TODO Auto-generated method stub
		InputStream input = new FileInputStream(args[0]);
		input.read();
		input.read();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
				new CBZip2InputStream(input), "utf8"));
		HashMap<String, Integer> idMap = new HashMap<String, Integer>();
		int cout = 1;
		for (int i = 0; i < 100; i++) {
			// while(true){
			String line = reader.readLine();
			if (line == null)
				break;
			if (reader.getLineNumber() % 100000 == 0)
				System.out.println("scanning " + reader.getLineNumber());
			String[] part = line.split(" ");
			if (part.length != 4) {
				System.out.println("error @ triple len!\t" + line);
				break;
			}
			if (!idMap.containsKey(part[0]))
				idMap.put(part[0], cout++);
			if (!idMap.containsKey(part[2]))
				idMap.put(part[2], cout++);
		}
		PrintWriter pw = new PrintWriter("res_id.txt");
		for (String res : idMap.keySet()) {
			String label = res.substring(res.lastIndexOf('/') + 1, res
					.lastIndexOf('>'));
			label = URLDecoder.decode(label, "utf8").replaceAll("_", " ");
			pw.println(idMap.get(res) + "\t" + label + "\t" + res);
		}
		pw.close();
		reader.close();

		IndexWriter writer = new IndexWriter("pagelink",
				new StandardAnalyzer(), true,
				IndexWriter.MaxFieldLength.LIMITED);
		input = new FileInputStream(args[0]);
		input.read();
		input.read();
		reader = new LineNumberReader(new InputStreamReader(
				new CBZip2InputStream(input), "utf8"));
		String pre = null;
		ArrayList<Integer> pagelink = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			// while(true){
			String line = reader.readLine();
			if (line == null)
				break;
			if (reader.getLineNumber() % 100000 == 0)
				System.out.println("indexing " + reader.getLineNumber());
			String[] part = line.split(" ");
			if (part.length != 4) {
				System.out.println("error @ triple len!\t" + line);
				break;
			}
			if (pre == null || !pre.equals(part[0])) {
				if (pre != null) {
					String label = pre.substring(pre.lastIndexOf('/') + 1, pre
							.lastIndexOf('>'));
					label = URLDecoder.decode(label, "utf8").replaceAll("_",
							" ");
					pagelink.add(idMap.get(pre));
					Document doc = get(label, pagelink);
					writer.addDocument(doc);
				}
				pre = part[0];
				pagelink.clear();
			}
			pagelink.add(idMap.get(part[2]));
		}
		if (pre != null) {
			String label = pre.substring(pre.lastIndexOf('/') + 1, pre
					.lastIndexOf('>'));
			label = URLDecoder.decode(label, "utf8").replaceAll("_", " ");
			pagelink.add(idMap.get(pre));
			Document doc = get(label, pagelink);
			writer.addDocument(doc);
		}
		writer.commit();
		writer.optimize();
		reader.close();

		IndexSearcher search = new IndexSearcher("pagelink");
		for (int i = 0; i < search.maxDoc(); i++) {
			Document d = search.doc(i);
			System.out.println(d.get("L"));
			String[] list = d.getValues("P");
			for (String l : list)
				System.out.print(l + " ");
			System.out.println();
		}
	}

	public static Document get(String label, ArrayList<Integer> pagelink) {
		Document doc = new Document();
		doc.add(new Field("L", label, Field.Store.YES,
				Field.Index.NOT_ANALYZED_NO_NORMS));
		for (Integer i : pagelink)
			doc.add(new Field("P", i.toString(), Field.Store.YES,
					Field.Index.NO));
		return doc;
	}
}
