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
	
	
	
	public abstract void analyze(String line);
	public abstract void finish();
}