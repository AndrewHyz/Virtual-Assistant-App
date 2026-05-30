package assistant.app.time;

import java.util.Arrays;
import java.util.List;

import assistant.app.Action;
import assistant.app.App;

public class TimeApp extends App{
	public List<Action> getActions() {
		return Arrays.asList(new GetTimeAction());
	}
}
