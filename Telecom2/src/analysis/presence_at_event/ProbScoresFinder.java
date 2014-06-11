package analysis.presence_at_event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.Config;

public class ProbScoresFinder {

	public String[] getAvailableProbScores() {
		try {
			List<String> prob_scores = new ArrayList<String>();
			File bdir = new File(Config.getInstance().base_folder+"/PresenceCounter");
			
			for(File dir: bdir.listFiles()) {
				File prob_scores_dir = new File(dir+"/ProbScores");
				if(prob_scores_dir.exists())
				for(File f: prob_scores_dir.listFiles())
					prob_scores.add(f.getAbsolutePath());
			}
			
			
			String[] ps = new String[prob_scores.size()];
			return prob_scores.toArray(ps);

		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		ProbScoresFinder psf = new  ProbScoresFinder();
		for(String f: psf.getAvailableProbScores())
			System.out.println(f);

	}
}
