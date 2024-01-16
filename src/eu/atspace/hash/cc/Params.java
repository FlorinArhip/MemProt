package eu.atspace.hash.cc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class Params {
	// number of peers
	private int GROUP_SIZE;	
	
	// single/mutli failure
	private boolean SINGLE_FAILURE;
	
	// message drop probability
	private double MSG_DROP_PROB;
	
	// rate of insertion
	private double STEP_RATE;
	
	// max message size
	private int MAX_MSG_SIZE;
	
	// indicates whether messages are dropped
	private boolean DROP_MSG;

	// whether to drop messages or not
	private boolean dropmsg;
	
	// global time, use it when you need current timestamp
	private int currTime;
	
	// default port number
	private short PORTNUM;

	
	public boolean isDropmsg() {
		return dropmsg;
	}

	public void setDropmsg(boolean dropmsg) {
		this.dropmsg = dropmsg;
	}

	public int getCurrTime() {
		return currTime;
	}
	
	public void setCurrTime(int globaltime) {
		this.currTime = globaltime;
	}

	public int getGROUP_SIZE() {
		return GROUP_SIZE;
	}

	public boolean isSINGLE_FAILURE() {
		return SINGLE_FAILURE;
	}

	public double getMSG_DROP_PROB() {
		return MSG_DROP_PROB;
	}

	public double getSTEP_RATE() {
		return STEP_RATE;
	}

	public int getMAX_MSG_SIZE() {
		return MAX_MSG_SIZE;
	}

	public boolean isDROP_MSG() {
		return DROP_MSG;
	}

	public short getPORTNUM() {
		return PORTNUM;
	}

	
	void setParams(String fn) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileReader(fn));
		
		GROUP_SIZE = Integer.valueOf(props.getProperty("GROUP_SIZE"));
		SINGLE_FAILURE = Boolean.valueOf(props.getProperty("SINGLE_FAILURE"));
		DROP_MSG = Boolean.valueOf(props.getProperty("DROP_MSG"));
		MSG_DROP_PROB = Double.valueOf(props.getProperty("MSG_DROP_PROB"));
		
		System.out.println("GROUP_SIZE: " + GROUP_SIZE);
		System.out.println("SINGLE_FAILURE: " + SINGLE_FAILURE);
		System.out.println("DROP_MSG: " + DROP_MSG);
		System.out.println("MSG_DROP_PROB: " + MSG_DROP_PROB);
		
		PORTNUM = 8001;
		STEP_RATE = 0.25;
		MAX_MSG_SIZE = 4000;
		currTime = 0;
		dropmsg = false;
	}	

}
