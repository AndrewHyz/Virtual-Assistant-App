package assistant.app.qwen;

import java.util.Arrays;
import java.util.List;

import assistant.app.Action;
import assistant.app.App;

public class QwenApp extends App {
	public List<Action> getActions() {
		return Arrays.asList(new AskQwenAction());
	}
}
