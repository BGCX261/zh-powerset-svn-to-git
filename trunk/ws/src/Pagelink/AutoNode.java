package Pagelink;

import java.util.HashMap;
import java.util.Set;

public class AutoNode extends Node {
	public AutoNode(String name, Set<String[]> link) {
		this.name = name;
		int c = 0, p = 0;
		this.link = new HashMap<String, Double>();
		for (String[] e : link) {
			int power = Integer.parseInt(e[1]);
			String tag = e[0];
			if (tag.length() >= 8)
				p += power;
			else
				c += power;
			if (this.link.containsKey(tag))
				this.link.put(tag, this.link.get(tag) + power);
			else
				this.link.put(tag, (double) power);
		}
		for (String key : this.link.keySet()) {
			double pop;
			if (key.length() >= 8)
				pop = this.link.get(key) / p; // link score for property
			else
				pop = this.link.get(key) / c; // link score for concept
			this.link.put(key, pop);
		}
	}

	public Double getValue(String tge) {
		return this.link.get(tge);
	}

	public String getName() {
		return this.name;
	}

	public double getNameProb() {
		return this.nameprob;
	}

	public String toString() {
		if (this.name.length() >= 8)
			return "[P, " + this.nameprob + "]";
		else
			return "[C, " + this.nameprob + "]";
	}
	
	public Set<String> getConnectedNodes(){
		return this.link.keySet();
	}
}
