package assistant.app.todo;

import java.util.Arrays;
import java.util.List;

import assistant.app.Action;
import assistant.app.App;

public class TodoListApp extends App {
	public List<Action> getActions() {
		return Arrays.asList(new TodoListAction());
	}
}
