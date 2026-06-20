package assistant.app.deepseek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import assistant.Assistant;
import assistant.app.Action;
import assistant.app.MarkdownResponse;
import assistant.app.Response;

public class AskDeepSeekAction extends Action {
	private static final String DEFAULT_API_URL = "https://api.deepseek.com/chat/completions";
	private static final String DEFAULT_MODEL = "deepseek-v4-flash";

	String[] keywords = { "deepseek", "ask", "ai", "chat" };
	double[] scores =   { 4,          1,     1,    1 };

	public void doCommand(String command) {
		Assistant assistant = Assistant.getInstance();
		String apiKey = getApiKey();

		if (apiKey == null || apiKey.length() == 0) {
			assistant.displayItem(new Response("Please set DEEPSEEK_API_KEY before using DeepSeek."));
			return;
		}

		String prompt = getPrompt(command);
		if (prompt.length() == 0) {
			assistant.displayItem(new Response("What should I ask DeepSeek?"));
			return;
		}

		JSONObject body = new JSONObject();
		JSONArray messages = new JSONArray();
		messages.put(new JSONObject().put("role", "system").put("content", "You are a helpful assistant. Keep answers concise."));
		messages.put(new JSONObject().put("role", "user").put("content", prompt));

		body.put("model", getModel());
		body.put("messages", messages);
		body.put("temperature", 0.7);

		try {
			HttpResponse<JsonNode> response = Unirest.post(getApiUrl())
					.header("Authorization", "Bearer " + apiKey)
					.header("Content-Type", "application/json")
					.body(body.toString())
					.asJson();

			if (response.getStatus() >= 400) {
				assistant.displayItem(new Response("DeepSeek request failed with status " + response.getStatus()));
				return;
			}

			handleResult(response.getBody());
		} catch (UnirestException e) {
			System.out.println("DeepSeek request error occurred: " + e);
			assistant.displayItem(new Response("Sorry, I could not connect to DeepSeek. Please check your API key and internet connection."));
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

	private String getPrompt(String command) {
		String prompt = command.trim();
		prompt = removeFirstWord(prompt, "ask");
		prompt = removeFirstWord(prompt, "deepseek");
		prompt = removeFirstWord(prompt, "ai");
		prompt = removeFirstWord(prompt, "chat");
		return prompt.trim();
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

	private String getApiKey() {
		String apiKey = System.getenv("DEEPSEEK_API_KEY");
		if (apiKey == null || apiKey.length() == 0) {
			apiKey = getConfigValue("DEEPSEEK_API_KEY");
		}
		return apiKey;
	}

	private String getApiUrl() {
		String apiUrl = System.getenv("DEEPSEEK_API_URL");
		if (apiUrl == null || apiUrl.length() == 0) {
			apiUrl = getConfigValue("DEEPSEEK_API_URL");
		}
		if (apiUrl == null || apiUrl.length() == 0) {
			apiUrl = DEFAULT_API_URL;
		}
		return apiUrl;
	}

	private String getModel() {
		String model = System.getenv("DEEPSEEK_MODEL");
		if (model == null || model.length() == 0) {
			model = getConfigValue("DEEPSEEK_MODEL");
		}
		if (model == null || model.length() == 0) {
			model = DEFAULT_MODEL;
		}
		return model;
	}

	private String getConfigValue(String key) {
		File configFile = new File("deepseek.properties");
		if (!configFile.exists()) {
			return "";
		}

		Properties properties = new Properties();
		try {
			FileInputStream inputStream = new FileInputStream(configFile);
			properties.load(inputStream);
			inputStream.close();
			return properties.getProperty(key, "");
		} catch (IOException e) {
			System.out.println("Could not read deepseek.properties: " + e);
			return "";
		}
	}

	private void handleResult(JsonNode node) {
		Assistant assistant = Assistant.getInstance();
		JSONObject json = node.getObject();
		JSONArray choices = json.optJSONArray("choices");
		String answer = "";
		if (choices != null && choices.length() > 0 && choices.optJSONObject(0) != null) {
			JSONObject message = choices.optJSONObject(0).optJSONObject("message");
			if (message != null) {
				answer = message.optString("content");
			}
		}

		if (answer == null || answer.length() == 0) {
			assistant.displayItem(new Response("DeepSeek did not return an answer."));
		} else {
			assistant.displayItem(new MarkdownResponse(answer));
		}
	}
}
