package Pagelink;

import java.io.FileInputStream;
import java.util.Properties;

public class Env {
	private static Env env;

	public static Env GetEnv() {
		if (Env.env == null)
			Env.env = new Env();
		return Env.env;
	}

	private Env() {
		Properties p = new Properties();
		try{
			p.load(new FileInputStream("env.txt"));
			this.MAX_LOOK_FORWARD = Integer.parseInt(p.getProperty(MLF));
			this.UNLINKED_PENALTY = Integer.parseInt(p.getProperty(UP));
			this.UNID_PENALTY = Integer.parseInt(p.getProperty(UI));
			this.TO_MEMORY = Integer.parseInt(p.getProperty(TM));
			this.TO_DISK = Integer.parseInt(p.getProperty(TD));
			this.INPUT_PATH = p.getProperty(IP);
			this.OUTPUT_PATH = p.getProperty(OP);
			this.RENAME_PATH = p.getProperty(RP);
			this.PAGELINK_PATH = p.getProperty(PP);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// env constant
	public final String MLF = "MAX_LOOK_FORWARD";
	public final String IP = "INPUT_PATH";
	public final String OP = "OUTPUT_PATH";
	public final String RP = "RENAME_PATH";
	public final String PP = "PAGELINK_PATH";
	public final String UP = "UNLINKED_PENALTY";
	public final String UI = "UNID_PENALTY";
	public final String TM = "TO_MEMORY";
	public final String TD = "TO_DISK";
	
	public int MAX_LOOK_FORWARD = -1;
	public int UNLINKED_PENALTY = -1;
	public int UNID_PENALTY = -1;
	public int TO_MEMORY = -1;
	public int TO_DISK = -1;
	public String INPUT_PATH = null;
	public String OUTPUT_PATH = null;
	public String RENAME_PATH = null;
	public String PAGELINK_PATH = null;
	
	private double nlp = -1;
	public double getNoLinkPunishment(){
		if(nlp == -1)
			nlp = 1.0 / Math.pow(10, this.UNLINKED_PENALTY);
		return nlp;
	}
	
	private double nip = -1;
	public double getNoIDPunishment(){
		if(nip == -1)
			nip = 1.0 / Math.pow(10, this.UNID_PENALTY);
		return nip;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("========================================\r\n");
		sb.append("= "+this.IP+":\t"+this.INPUT_PATH+"\r\n");
		sb.append("= "+this.OP+":\t"+this.OUTPUT_PATH+"\r\n");
		sb.append("= "+this.RP+":\t"+this.RENAME_PATH+"\r\n");
		sb.append("= "+this.PP+":\t"+this.PAGELINK_PATH+"\r\n");
		sb.append("= "+this.UP+":\t"+this.UNLINKED_PENALTY+"\r\n");
		sb.append("= "+this.UI+":\t"+this.UNID_PENALTY+"\r\n");
		sb.append("= "+this.MLF+":\t"+this.MAX_LOOK_FORWARD+"\r\n");
		sb.append("= "+this.TM+":\t"+this.TO_MEMORY+"\r\n");
		sb.append("= "+this.TD+":\t"+this.TO_DISK+"\r\n");
		sb.append("========================================\r\n");
		return sb.toString();
	}
}
