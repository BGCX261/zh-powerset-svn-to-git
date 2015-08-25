package LookAtMe;

import java.net.URLEncoder;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class BingSearch  implements SearchEngine{
	private static String BING_URL = "http://api.bing.net/json.aspx?AppId=98B627A4C4949A1D6575D49ADA13CDD04D770EE5&Version=2.2&Market=en-US&Query=%s&Sources=web&Web.Offset=%d&Web.Count=%d&JsonType=raw";
	private static int SIZE = 50;
	private String query;
	private int num;
	private JSONArray[] res;

	public BingSearch(String q, int n) {
		this.query = q;
		this.num = n;
		int s = (n-1)/SIZE+1;
		res = new JSONArray[s];
		for(int i=0; i<s; i++)
			retry(0, i*SIZE, n-i*SIZE>SIZE?SIZE:n-i*SIZE, i);
	}

	private void retry(int i, int offset, int num, int idx) {
		try {
			Thread.sleep(i*1000*60);
			System.out.println(new Date().toString()+" bing offset:"+offset+" num:"+num);
			String context = WebService.get(String.format(BING_URL, URLEncoder
					.encode(query, "utf8"), offset, num));
			JSONObject json = new JSONObject(context);
			JSONObject response = (JSONObject) json.get("SearchResponse");
			JSONObject web = (JSONObject) response.get("Web");
			try{
				this.res[idx] = (JSONArray) web.get("Results");
			} catch (Exception e) {
				System.out.println("query down!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("sleeping "+(i*2+1)+" min");
			retry(i*2+1, offset, num, idx);
		}
	}

	public int size() {
		return num;
	}

	private String get(int i, String field) {
		try {
			int a = i/SIZE;
			int b = i-a*SIZE;
			Object o = res[a].get(b);
			if (o != null) {
				JSONObject jo = (JSONObject) o;
				Object ret = jo.get(field);
				if (ret != null)
					return ret.toString();
			}
		} catch (Exception e) {
		}
		return "";
	}

	public String title(int i) {
		if (i >= num)
			return "";
		return get(i, "Title");
	}

	public String snippet(int i) {
		if (i >= num)
			return "";
		return get(i, "Description");
	}

	public String url(int i) {
		if (i >= num)
			return "";
		return get(i, "Url");
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// String url =
		// "http://api.bing.net/json.aspx?AppId=98B627A4C4949A1D6575D49ADA13CDD04D770EE5&Version=2.2&Market=en-US&Query=%s&Sources=web&Web.Count=%d&JsonType=raw";
		// String context = WebService.get(String.format(url, "abc", 1));
		// JSONObject json = new JSONObject(context);
		// JSONObject res = (JSONObject) json.get("SearchResponse");
		// JSONObject webres = (JSONObject) res.get("Web");
		// JSONArray singles = (JSONArray) webres.get("Results");
		// for (int i = 0; i < singles.length(); i++) {
		// JSONObject single = (JSONObject) singles.get(i);
		// System.out.println(single.get("Description"));
		// System.out.println(single.get("Url"));
		// System.out.println(single.get("DateTime"));
		// System.out.println(single.get("DisplayUrl"));
		// System.out.println(single.get("DeepLinks"));
		// System.out.println(single.get("CacheUrl"));
		// System.out.println(single.get("Title"));
		// }
		BingSearch bs = new BingSearch("\"Nobel Peace Prize\" \"1989\"", 50);
		for (int i = 0; i < bs.size(); i++) {
			System.out.println(bs.title(i));
			System.out.println(bs.snippet(i));
			System.out.println(bs.url(i));
			System.out.println();
		}
	}

}
