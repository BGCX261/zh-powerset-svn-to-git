package Segment;

import java.util.ArrayList;
import java.util.Set;

import DataHolder.*;
import Pagelink.AutoNode;
import Pagelink.Env;

public class Processor2 {

	private String substring(String[] str, int startpoint, int endpoint) {
		if (startpoint >= str.length)
			return "";
		String strs = str[startpoint];
		for (int i = startpoint + 1; i < endpoint; i++)
			strs += " " + str[i];
		return strs;
	}

	private AutoNode[] getAutoNode(String[] str, int startpoint, int endpoint) {

		Set<AutoNode> nodeset = RenameHolder.Instance().get(
				substring(str, startpoint, endpoint));
		if (nodeset == null)
			return null;
		if (nodeset.size() == 0)
			return null;
		AutoNode[] tmp = new AutoNode[nodeset.size()];
		nodeset.toArray(tmp);
		return tmp;
	}

	private double getRelation(AutoNode src, AutoNode dest) {
		if (dest == null)
			return Env.GetEnv().getNoLinkPunishment();
		else
			return PagelinkHolder.Instance().getRelation(src, dest);
	}

	private String cut;
	private String detail;

	private double prob;

	/* ========================= main algorithm ========================= */
	private double process(String strs, AutoNode[] lasttag, double[] lastprob, String[] details,
			String currentcut) {
		double ret = 0;

		// stop condition
		if (strs.length() == 0) {
			double v = 0;
			String d = null;
			for(int i=0; i<lastprob.length; i++){
				if(v<lastprob[i]){
					v = lastprob[i];
					d = details[i];
				}
			}
			if (prob < v) {
				cut = currentcut;
				detail = d;
				prob = v;
				return v;
			} else
				return prob;
		}

		String[] str = strs.split(" ");
		// look forward
		for (int i = (str.length > Env.GetEnv().MAX_LOOK_FORWARD) ? Env
				.GetEnv().MAX_LOOK_FORWARD : str.length; i > 0; --i) {
			String tmpcut = currentcut;
			AutoNode[] v = getAutoNode(str, 0, i);

			ArrayList<Double> tprobList = new ArrayList<Double>();
			ArrayList<AutoNode> tnodeList = new ArrayList<AutoNode>();
			ArrayList<String> detailList = new ArrayList<String>();

			// if no registered phrase
			if (v == null) {
				tmpcut = tmpcut + " (" + substring(str, 0, i)+")";

				// get current prob
				for (int j = 0; j < lastprob.length; ++j) {
					double punish1 = Math.pow(Env.GetEnv()
							.getNoLinkPunishment(), i);
					double punish2 = Env.GetEnv()
					.getNoIDPunishment() / Math.pow(2, i);
					double curprob = lastprob[j] * punish1 * punish2;
					if (curprob > prob) {
						tprobList.add(curprob);
						tnodeList.add(lasttag[j]);
						detailList.add(details[j]+" ("+i+")");
					}
				}
			}

			// cope with registered phrases
			else {
				tmpcut = tmpcut + " [" + substring(str, 0, i)+"]";
				// get current prob
				for (int j = 0; j < v.length; ++j) {
					double max = 0, maxlink = 0;
					int maxK = -1;
					for (int k = 0; k < lasttag.length; ++k) {
						double link = getRelation(v[j], lasttag[k]);
						double tmp = lastprob[k]
								* link
								* v[j].getNameProb();
						if (tmp > max){
							max = tmp;
							if(link > Env.GetEnv().getNoLinkPunishment())
								maxlink = link;
							maxK = k;
						}
					}
					if (max > prob) {
						tprobList.add(max);
						tnodeList.add(v[j]);
						detailList.add(details[maxK]+" "+(maxlink>0?maxlink:"")+" "+v[j].toString());
					}
				}
			}

			// package qualified prob
			if (tprobList.size() > 0) {
				double[] tprob = new double[tprobList.size()];
				AutoNode[] tnode = new AutoNode[tnodeList.size()];
				String[] tdetail = new String[detailList.size()];
				int d = 0;
				for (Double tmp : tprobList)
					tprob[d++] = tmp;
				tnodeList.toArray(tnode);
				detailList.toArray(tdetail);
				ret = Math.max(ret, process(substring(str, i, str.length),
						tnode, tprob, tdetail, tmpcut));
			} else
				ret = Math.max(ret, prob);
		}
		return ret;
	}

	/* =========================== main entry ======================= */
	public String Segment(String b) throws Exception {
		this.prob = 0;
		this.cut = "";
		process(b, new AutoNode[] { null }, new double[] { 1 }, new String[]{""}, "");
		return this.cut + "\r\n" + detail +" = " + this.prob+"\r\n";
	}
}
