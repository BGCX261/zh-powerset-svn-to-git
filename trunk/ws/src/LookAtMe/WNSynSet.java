package LookAtMe;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class WNSynSet {

	public static String[] get(String wordForm)
	{
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(wordForm);

		if(synsets.length > 0){
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<synsets.length; i++){
				String[] wordForms = synsets[i].getWordForms();
				for(String wf: wordForms)
					if(wordForm.split(" ").length > wf.split(" ").length && !wordForm.toLowerCase().contains(wf.toLowerCase()))
						sb.append(wf+"\t");
			}
			return sb.toString().trim().split("\t");
		}
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.setProperty("wordnet.database.dir", "D:\\wsdata\\data\\dict");
		//  Concatenate the command-line arguments
		String wordForm = "monetary value";
		for(String g: get(wordForm))
			System.out.println(g);
	}

}
