package LookAtMe;

import java.net.URLEncoder;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class YahooSearch implements SearchEngine{

	private static String YAHOO_URL = "http://boss.yahooapis.com/ysearch/web/v1/%s?appid=C9Rapk_V34FVIAlz_rnNYuyeeuSzaDYnscvn7KxwBL.0vtLTs3o6sz.S6SaNo0ZsQGSG4A--&format=json&start=%d&count=%d&lang=en";
	private static int SIZE = 50;
	private String query;
	private int num;
	private JSONArray[] res;

	public YahooSearch(String q, int n) {
		this.query = q;
		this.num = n;
		int s = (n-1)/SIZE+1;
		res = new JSONArray[s];
		for(int i=0; i<s; i++)
			retry(0, i*SIZE, n-i*SIZE>SIZE?SIZE:n-i*SIZE, i);
	}

	private void retry(int i, int start, int num, int idx) {
		try {
			Thread.sleep(i*1000*60);
			System.out.println(new Date().toString()+" yahoo start:"+start+" num:"+num);
			String context = WebService.get(String.format(YAHOO_URL, URLEncoder
					.encode(query, "utf8"), start, num));
			JSONObject json = new JSONObject(context);
			JSONObject response = (JSONObject) json.get("ysearchresponse");
			try{
				this.res[idx] = (JSONArray) response.get("resultset_web");
			} catch (Exception e) {
				System.out.println("query down!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("sleeping "+(i*2+1)+" min");
			retry(i*2+1, start, num, idx);
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
		return get(i, "title").replaceAll("<b>", "").replaceAll("</b>", "");
	}

	public String snippet(int i) {
		if (i >= num)
			return "";
		return get(i, "abstract").replaceAll("<b>", "").replaceAll("</b>", "");
	}

	public String url(int i) {
		if (i >= num)
			return "";
		return get(i, "url");
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
		YahooSearch bs = new YahooSearch("\"David\"  \"Koresh\"  \"ask\"  \"FBI\"  \"word\"  \"processor\"", 50);
		for (int i = 0; i < bs.size(); i++) {
			System.out.println(bs.title(i));
			System.out.println(bs.snippet(i));
			System.out.println(bs.url(i));
			System.out.println();
		}
	}

}
