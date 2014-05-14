package pls_parser;

import java.util.Calendar;

import utils.Config;

public abstract class BufferAnalyzer {
	public int process(char[] buffer, int length){
		int start = 0;
		for(int i=0; i<length; i++){
			if(buffer[i]=='\n'){
				analyze(new String(buffer, start, i-start));
				start = i+1;
			}
		}
		return start;
	}
	
	
	public Calendar getStartTime() {
		return Config.getInstance().pls_start_time;
	}
	public Calendar getEndTime() {
		return Config.getInstance().pls_end_time;
	}
	
	
	
	abstract void analyze(String line);
	abstract void finish();
	
	
	void run() {
		try {
		PLSParser.parse(this);
		finish();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}