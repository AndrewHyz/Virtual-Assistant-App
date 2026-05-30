package assistant.app.calculator;

import java.util.Arrays;
import java.util.List;

import assistant.app.Action;
import assistant.app.App;

public class CalculatorApp extends App {
	public List<Action> getActions() {
		return Arrays.asList(new CalculateAction());
	}
}
