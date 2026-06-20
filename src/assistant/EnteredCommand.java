package assistant;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import assistant.app.Displayable;


class EnteredCommand implements Displayable {
  
  private String command;
  
  public EnteredCommand(String command) {
    this.command = command;
  }
  
  public void update(Label label) {
    label.setText("Command: " + command);
    label.setAlignment(Pos.CENTER_LEFT);
  }
}
