package assistant.app;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class Response implements Displayable {
  
  private String response;
  
  public Response(String response) {
    this.response = response;
  }

  @Override
  public void update(Label label) {
    label.setAlignment(Pos.CENTER_RIGHT);
    label.setText(response);
  }

}


