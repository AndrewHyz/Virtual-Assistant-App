package assistant.app.todo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import assistant.Assistant;
import assistant.app.Action;
import assistant.app.Response;

public class TodoListAction extends Action {
	private static List<String> todoItems = new ArrayList<String>();

	String[] keywords = { "todo", "to-do", "task", "list", "add", "clear", "remove" };
	double[] scores =   { 3,      3,       2,      1,      1,     1,       1 };

	public void doCommand(String command) {
		Assistant assistant = Assistant.getInstance();
		List<String> words = Arrays.asList(command.split(" "));

		if (words.contains("clear")) {
			todoItems.clear();
			assistant.displayItem(new Response("Your to-do list is now empty"));
		} else if (words.contains("add")) {
			String item = getItemAfterWord(command, "add");
			if (item.length() > 0) {
				todoItems.add(item);
				assistant.displayItem(new Response("Added to your to-do list: " + item));
			} else {
				assistant.displayItem(new Response("What should I add to your to-do list?"));
			}
		} else if (words.contains("remove")) {
			String item = getItemAfterWord(command, "remove");
			if (todoItems.remove(item)) {
				assistant.displayItem(new Response("Removed from your to-do list: " + item));
			} else {
				assistant.displayItem(new Response("I could not find that item on your to-do list"));
			}
		} else {
			showTodoList(assistant);
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

	private String getItemAfterWord(String command, String word) {
		int index = command.indexOf(word);
		if (index < 0) {
			return "";
		}

		String item = command.substring(index + word.length()).trim();
		item = item.replace("to my todo list", "").trim();
		item = item.replace("to my to-do list", "").trim();
		item = item.replace("to todo list", "").trim();
		item = item.replace("to to-do list", "").trim();
		return item;
	}

	private void showTodoList(Assistant assistant) {
		if (todoItems.size() == 0) {
			assistant.displayItem(new Response("Your to-do list is empty"));
			return;
		}

		String result = "To-do list: ";
		for (int i = 0; i < todoItems.size(); i++) {
			result += (i + 1) + ". " + todoItems.get(i);
			if (i < todoItems.size() - 1) {
				result += ", ";
			}
		}

		assistant.displayItem(new Response(result));
	}
}
