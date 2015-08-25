package DataHolder;

import java.io.File;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * Using Lucene to implement a disk-based HashTable
 * 
 * @author kaifengxu
 * 
 */
public class LuceneMap {

	private String dir;
	private IndexWriter putter;
	private IndexSearcher getter;
	public static String KEY_FIELD = "K";
	public static String KEY_ORIGIN = "O";
	public static String VALUE_FIELD = "V";

	public LuceneMap(String indexDir) {
		this.dir = indexDir;
	}

	/**
	 * initialize putter
	 * 
	 * @throws Exception
	 */
	private void initPutter() {
		try {
			boolean c;
			File dir = new File(this.dir);
			if (dir.exists() && dir.isDirectory())
				c = false;
			else
				c = true;
			IndexWriter.unlock(FSDirectory.getDirectory(dir));
			putter = new IndexWriter(dir, new StandardAnalyzer(), c,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * initialize getter
	 * 
	 * @throws Exception
	 */
	private void initGetter() {
		try {
			IndexWriter.unlock(FSDirectory.getDirectory(dir));
			getter = new IndexSearcher(dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		if (putter != null) {
			putter.optimize();
		}
		super.finalize();
	}

	/**
	 * close writer
	 * 
	 * @throws Exception
	 */
	public void commit() {
		try {
			if (putter != null)
				putter.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * put key and data into index
	 * 
	 * @param key
	 * @param data
	 * @throws Exception
	 */
	public void put(String key, String data) {
		try {
			if (putter == null) {
				initPutter();
			}
			Document doc = new Document();
			doc.add(new Field(KEY_FIELD, key.toLowerCase(), Field.Store.NO,
					Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field(KEY_ORIGIN, key, Field.Store.YES,
							Field.Index.NO));
			doc.add(new Field(VALUE_FIELD, data, Field.Store.YES,
					Field.Index.NO));
			putter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * put key and data into index
	 * 
	 * @param key
	 * @param data
	 * @throws Exception
	 */
	public void put(String key, String[] data) {
		try {
			if (putter == null) {
				initPutter();
			}
			Document doc = new Document();
			doc.add(new Field(KEY_FIELD, key.toLowerCase(), Field.Store.NO,
					Field.Index.NOT_ANALYZED_NO_NORMS));
			doc.add(new Field(KEY_ORIGIN, key, Field.Store.YES,
							Field.Index.NO));
			for (String d : data)
				doc.add(new Field(VALUE_FIELD, d, Field.Store.YES,
						Field.Index.NO));
			putter.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search the data for the key
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> get(String key) {
		try {
			if (getter == null) {
				initGetter();
			}
			Query query = new TermQuery(new Term(KEY_FIELD, key.toLowerCase()));
			TopDocs hits = getter.search(query, 100);
			ArrayList<String> res1 = null, res2 = null;
			if (hits != null && hits.totalHits != 0) {
				res1 = new ArrayList<String>();
				res2 = new ArrayList<String>();
				for (ScoreDoc sd : hits.scoreDocs) {
					if (getter.doc(sd.doc).get(KEY_ORIGIN).equals(key))
						for (String v : getter.doc(sd.doc).getValues(VALUE_FIELD))
							res1.add(v);
					else
						for (String v : getter.doc(sd.doc).getValues(VALUE_FIELD))
							res2.add(v);
				}
			}
			if (res1 != null && res1.size() != 0)
				return res1;
			else if (res2 != null && res2.size() != 0)
				return res2;
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Print all triples for basic-put map only
	 * 
	 * @throws Exception
	 */
	protected void printAllTriples() {
		try {
			if (getter == null) {
				initGetter();
			}
			for (int i = 0; i < getter.maxDoc(); i++) {
				Document doc = getter.doc(i);
				System.out.println(doc.get(KEY_FIELD) + " | "
						+ doc.get(VALUE_FIELD));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LuceneMap li = new LuceneMap("test");
		li.put("key1", "data1");
		li.put("key2", "data2");
		li.put("key1", "data2");
		li.put("key2", "data2");
		li.put("Key1", "Data1");
		li.commit();
		li.printAllTriples();

		ArrayList<String> res = li.get("KEy1");
		if (res != null)
			for (String r : res)
				System.out.println("key1 = " + r);

		res = li.get("Key2");
		if (res != null)
			for (String r : res)
				System.out.println("key2 = " + r);
	}

}
