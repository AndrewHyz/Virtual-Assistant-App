package assistant.app.deepseek;

import java.util.Arrays;
import java.util.List;

import assistant.app.Action;
import assistant.app.App;

public class DeepSeekApp extends App {
	public List<Action> getActions() {
		return Arrays.asList(new AskDeepSeekAction());
	}
}
