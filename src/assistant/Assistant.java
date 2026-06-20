package assistant;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import assistant.app.Action;
import assistant.app.App;
import assistant.app.Displayable;
import assistant.app.Response;
import assistant.app.calculator.CalculatorApp;
import assistant.app.deepseek.DeepSeekApp;
import assistant.app.todo.TodoListApp;
import assistant.app.time.TimeApp;
import assistant.app.weather.WeatherApp;

/**
 * Main class for our Virtual Assistant. Include application UI elements
 * and provides functionality to handle command by delegating
 * it to different apps. 
 */
public class Assistant extends Application {

  private static Assistant instance;

  /**
   * Provide a way to get the instance of the only Assistant
   * object created through the lifecycle of the application
   * @return
   */
  public static Assistant getInstance() {
    return instance;
  }
  
  public static void main(String[] args) {
    launch();
  }
  
  public Assistant() {
    instance = this;
  }
  
  private TextField commandTextField = new TextField();
  private Button goButton = new Button("Go");
  private ObservableList<Displayable> displayItems = FXCollections.observableArrayList();
  
  private List<Action> actions = new ArrayList<>();
  private ListView<Displayable> displayItemsListView = new ListView<Displayable>(displayItems);
  
  private App[] apps = getAvailableApps();

  @Override
  public void start(Stage stage) {
    initUILayout(stage);
    initUIListener();
    initAppActions();
  }
  
  /**
   * Registers available actions from each app
   */
  private void initAppActions() {
    for(App app : apps) {
      actions.addAll(app.getActions());
    }
  }
  
  private void initUIListener() {
    
    EventHandler<ActionEvent> doCommand = new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        String command = commandTextField.getText();
        commandTextField.setText("");
        handleCommand(command);
      }
    };
    
    goButton.setOnAction(doCommand);
    commandTextField.setOnAction(doCommand);
    
    displayItemsListView.setCellFactory(new Callback<ListView<Displayable>, ListCell<Displayable>>() {
      
      @Override
      public ListCell<Displayable> call(ListView<Displayable> listView) {
        return new ListCell<Displayable>() {
          private Label label = new Label();
          
          {
            label.setWrapText(true);
            label.maxWidthProperty().bind(listView.widthProperty().subtract(30));
          }
          
          @Override 
          protected void updateItem(Displayable item, boolean empty) {
            super.updateItem(item, empty);
            super.setText(null);
            if(item == null || empty) {
              super.setGraphic(null);
            } else {
              item.update(label);
              super.setGraphic(label);
            }
          }
        };
      }
    });
  }
  
  private void initUILayout(Stage stage) {
    
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10, 10, 10, 10));
    root.setCenter(displayItemsListView);
    
    HBox hbox = new HBox();
    hbox.setSpacing(10);
    hbox.setPrefHeight(50);
    hbox.setAlignment(Pos.CENTER);
    
    HBox.setHgrow(commandTextField, Priority.ALWAYS);
    hbox.getChildren().addAll(new Label("Command:"), commandTextField, goButton);
    root.setBottom(hbox);
    stage.setTitle("Virtual Assistant");
    loadAppIcon(stage);
    stage.setScene(new Scene(root, 600, 400));
    stage.show();
  }

  private void loadAppIcon(Stage stage) {
    InputStream iconStream = getClass().getResourceAsStream("/assistant/assets/app-icon.png");
    if(iconStream != null) {
      stage.getIcons().add(new Image(iconStream));
      return;
    }

    File iconFile = new File("src/assistant/assets/app-icon.png");
    if(iconFile.exists()) {
      stage.getIcons().add(new Image(iconFile.toURI().toString()));
    }
  }
  
  /**
   * Handle an inputed command by 
   * finding the most relevant AppAction to run it.
   * If most relevant AppAction does not have enough relevance score, 
   * respond by indicating so
   * 
   * @param command
   */
  private void handleCommand(String command) {
    // 
    Action bestAction = null;
    double bestActionScore = 0;
    String commandForLikelihood = command.toLowerCase();
    for(Action action : actions) {
      double actionScore = action.getLikelihood(commandForLikelihood);
      if(actionScore >= bestActionScore) {
        bestAction = action;
        bestActionScore = actionScore;
      }
    }
    
    if(bestAction != null) {
      System.out.println(command + ", best action: " + bestAction.getClass() + ", score: " + bestActionScore);
    }
    displayItems.add(new EnteredCommand(command));
    if(bestActionScore > 0.5) {
      try {
        bestAction.doCommand(command);
      } catch(Exception e) {
	e.printStackTrace();
        displayItem(new Response("Sorry, there was an error"));
      }
    } else {
      displayItem(new Response("Sorry, I can't understand your command"));
    }
    
  }
  
  /**
   * Adds an item to the displayable list
   * @param item
   */
  public void displayItem(Displayable item) {
    displayItems.add(item);
    displayItemsListView.scrollTo(item);
  }
  
  private static App[] getAvailableApps(){
    // TODO: add more apps avilable to the Virtual Assistant here
    return new App[]{new WeatherApp(), new TimeApp(), new TodoListApp(), new DeepSeekApp(), new CalculatorApp()};
  }
}


