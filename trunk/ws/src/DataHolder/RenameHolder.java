package DataHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Pagelink.AutoNode;
import Pagelink.Env;

public class RenameHolder {

	/* =========================== singleton ===================== */
	private static RenameHolder instance;

	public static RenameHolder Instance() {
		if (RenameHolder.instance == null)
			RenameHolder.instance = new RenameHolder();
		return instance;
	}

	private RenameHolder() {
		if (Env.GetEnv().TO_MEMORY == 1)
			this.rename = new HashMap<String, Set<String>>();
		else if (Env.GetEnv().TO_DISK == 1)
			this.rename2 = new LuceneMap(Env.GetEnv().RENAME_PATH.substring(0,
					Env.GetEnv().RENAME_PATH.lastIndexOf('.')));
	}

	/* =========================================================== */

	private Map<String, Set<String>> rename;
	public LuceneMap rename2;

	public void add(String name, String[] term) {
		if (Env.GetEnv().TO_MEMORY == 1) {
			Set<String> v = this.rename.get(name);
			if (v == null) {
				v = new HashSet<String>();
				this.rename.put(name, v);
			}
			v.add(term[0]);
		} else if (Env.GetEnv().TO_DISK == 1) {
			rename2.put(name, term);
		}
	}

	public Set<AutoNode> get(String name) {
		Collection<String> c = null;
		if (Env.GetEnv().TO_MEMORY == 1)
			c = this.rename.get(name);
		else if (Env.GetEnv().TO_DISK == 1)
			c = this.rename2.get(name);

		Set<AutoNode> ret = null;
		if (c != null && c.size() > 0) {
			ret = new HashSet<AutoNode>();
			int prop = 0, res = 0;
			for (String id : c) {
				// the id of property is start from 10000000
				if (id.length() >= 8)
					prop++;
				else
					res++;
			}
			for (String id : c) {
				AutoNode node = PagelinkHolder.Instance().get(id);
				if (node != null) {
					if (id.length() >= 8)
						node.nameprob = 1.0 / prop;
					else
						node.nameprob = 1.0 / res;
					ret.add(node);
				}
			}
		}
		return ret;
	}
}
