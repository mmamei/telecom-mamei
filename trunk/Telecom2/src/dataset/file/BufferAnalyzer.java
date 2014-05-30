package dataset.file;

import java.util.Calendar;

import utils.Config;

public abstract class BufferAnalyzer {
	
	public Calendar getStartTime() {
		return Config.getInstance().pls_start_time;
	}
	public Calendar getEndTime() {
		return Config.getInstance().pls_end_time;
	}
	
	
	
	protected abstract void analyze(String line);
	protected abstract void finish();
	
	
	void run() {
		try {
		PLSParser.parse(this);
		finish();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}