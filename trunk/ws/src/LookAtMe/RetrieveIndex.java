package LookAtMe;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class RetrieveIndex {

	private static IndexSearcher search = null;
	
	private RetrieveIndex(){
	}
	
	public static String[] retrieve(String label) throws Exception{
		if(search == null){
			search = new IndexSearcher("pagelink");
		}
		Query query = new TermQuery(new Term("L", label));
		TopDocs hits = search.search(query, 1);
		if (hits != null && hits.totalHits != 0) {
			for (ScoreDoc sd : hits.scoreDocs) {
				return search.doc(sd.doc).getValues("P");
			}
		}
		return new String[0];
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		for(String r:retrieve("!!!Fuck You!!!"))
			System.out.print(r+" ");;
	}

}
