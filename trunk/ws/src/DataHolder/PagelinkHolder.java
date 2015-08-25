package DataHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Pagelink.AutoNode;
import Pagelink.Env;

public class PagelinkHolder {

	/* ======================== singleton ======================== */
	private static PagelinkHolder instance;

	public static PagelinkHolder Instance() {
		if (PagelinkHolder.instance == null)
			PagelinkHolder.instance = new PagelinkHolder();
		return instance;
	}

	private PagelinkHolder() {
		if (Env.GetEnv().TO_MEMORY == 1)
			this.pagelink = new HashMap<String, AutoNode>();
		else if (Env.GetEnv().TO_DISK == 1)
			this.pagelink2 = new LuceneMap(Env.GetEnv().PAGELINK_PATH.substring(0, Env.GetEnv().PAGELINK_PATH.lastIndexOf('.')));
	}

	/* =========================================================== */

	private Map<String, AutoNode> pagelink;
	public LuceneMap pagelink2;

	public AutoNode get(String term) {
		if (Env.GetEnv().TO_MEMORY == 1) {
			AutoNode v = pagelink.get(term);
			if (v == null)
				v = new AutoNode(term, new HashSet<String[]>());
			return v;
		} else if (Env.GetEnv().TO_DISK == 1) {
			return getDiskNode(term, null);
		}
		return null;
	}

	private AutoNode getDiskNode(String term, String stop){
		if(term.length() >= 8)
			return new AutoNode(term, new HashSet<String[]>());
		ArrayList<String> infoes = pagelink2.get(term);
		if (infoes == null || infoes.size() == 0)
			return new AutoNode(term, new HashSet<String[]>());
		HashSet<String[]> res = new HashSet<String[]>();
		for (String info : infoes){
			if(stop == null || !stop.equals(info))
				res.add(new String[] { info, "1" });
		}
		return new AutoNode(term, res);
	}
	
	public void add(String term, Set<String[]> info) {
		if (Env.GetEnv().TO_MEMORY == 1)
			this.pagelink.put(term, new AutoNode(term, info));
		else if (Env.GetEnv().TO_DISK == 1) {
			pagelink2.put(term, info.iterator().next());
		}
	}

	public double getRelation(AutoNode ansrc, AutoNode andest) {
		return getRelationRec(ansrc, andest, 1);
	}
	
	private double getRelationRec(AutoNode ansrc, AutoNode andest, double cur){
		// the id of property is start from 10000000
		if (Integer.parseInt(ansrc.getName()) > Integer.parseInt(andest.getName())){
			AutoNode tmp = ansrc;
			ansrc = andest;
			andest = tmp;
		}
		Double s = ansrc.getValue(andest.getName());
		
		if(s == null){
			s = Env.GetEnv().getNoLinkPunishment();
			for(String connected: ansrc.getConnectedNodes()){
				cur *= ansrc.getValue(connected);
				if(cur >= Env.GetEnv().getNoLinkPunishment()){
					AutoNode con = this.getDiskNode(connected, ansrc.getName());
					s = Math.max(s, getRelationRec(con, andest, cur));
				}
			}
			return s;
		}
		else{
			cur *= s;
			if(cur < Env.GetEnv().getNoLinkPunishment())
				return Env.GetEnv().getNoLinkPunishment();
			else return cur;
		}
	}
	
	public static void main(String[] args){
		PagelinkHolder p = PagelinkHolder.Instance();
		System.out.println(p.getRelation(p.getDiskNode("2", null), p.getDiskNode("10000025", null)));
	}
}
