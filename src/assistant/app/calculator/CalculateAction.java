package assistant.app.calculator;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import assistant.Assistant;
import assistant.app.Action;
import assistant.app.Response;

public class CalculateAction extends Action {
	private static final String API_URL = "http://api.mathjs.org/v4/";

	String[] keywords = { "calculate", "calculator", "calcu", "math", "solve" };
	double[] scores =   { 4,           4,            4,      3,      3 };

	public void doCommand(String command) {
		Assistant assistant = Assistant.getInstance();
		String expression = getExpression(command);

		if (expression.length() == 0) {
			assistant.displayItem(new Response("What should I calculate?"));
			return;
		}

		try {
			HttpResponse<String> response = Unirest.get(API_URL)
					.queryString("expr", expression)
					.asString();

			if (response.getStatus() == 200) {
				assistant.displayItem(new Response(expression + " = " + response.getBody()));
			} else {
				assistant.displayItem(new Response("I could not calculate that: " + response.getBody()));
			}
		} catch (UnirestException e) {
			System.out.println("calculator request error occurred: " + e);
			assistant.displayItem(new Response("Sorry, I could not connect to the calculator service."));
		}
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

	private String getExpression(String command) {
		String expression = command.trim();
		expression = removeFirstWord(expression, "calculate");
		expression = removeFirstWord(expression, "calculator");
		expression = removeFirstWord(expression, "calc");
		expression = removeFirstWord(expression, "math");
		expression = removeFirstWord(expression, "solve");
		return expression.trim();
	}

	private String removeFirstWord(String text, String word) {
		String lowerText = text.toLowerCase();
		if (lowerText.equals(word)) {
			return "";
		}
		if (lowerText.startsWith(word + " ")) {
			return text.substring(word.length()).trim();
		}
		return text;
	}
}
