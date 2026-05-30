package assistant.app.time;

import java.time.LocalTime;

import assistant.Assistant;
import assistant.app.Action;
import assistant.app.Response;

public class GetTimeAction extends Action {
	String[] keywords = { "time", "clock", "what", "is", "it" };
	double[] scores =   { 3,      3,       0.2,    0.2,  0.2 };

	public void doCommand(String command) {
		Assistant assistant = Assistant.getInstance();
		
		String result = LocalTime.now().toString();
		
		assistant.displayItem(new Response("Local time: " + result));
	}
	
	public double getLikelihood(String command) {
		double score = 0;
		for (int i = 0; i < keywords.length; i++) {
			String keyword = keywords[i];
			if (command.contains(keyword)) {
				score += scores[i];
			}
		}
		return score;
	}
}
