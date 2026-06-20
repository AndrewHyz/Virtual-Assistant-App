package assistant;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import assistant.app.Action;
import assistant.app.App;
import assistant.app.Displayable;
import assistant.app.MarkdownResponse;
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
  private List<Action> actions = new ArrayList<>();
  private VBox conversationBox = new VBox(5);
  private ScrollPane scrollPane = new ScrollPane();
  
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
  }
  
  private void initUILayout(Stage stage) {
    
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10, 10, 10, 10));
    
    scrollPane.setContent(conversationBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    root.setCenter(scrollPane);
    
    HBox hbox = new HBox();
    hbox.setSpacing(10);
    hbox.setPrefHeight(50);
    hbox.setAlignment(Pos.CENTER);
    
    Button settingsButton = new Button("\u2699");
    settingsButton.setOnAction(e -> showSettingsDialog());
    
    HBox.setHgrow(commandTextField, Priority.ALWAYS);
    hbox.getChildren().addAll(new Label("Command:"), commandTextField, goButton, settingsButton);
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

  private void showSettingsDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Settings");
    dialog.setHeaderText("DeepSeek API Configuration");

    String existingKey = getDeepSeekConfig("DEEPSEEK_API_KEY", "");
    String existingUrl = getDeepSeekConfig("DEEPSEEK_API_URL", "https://api.deepseek.com/chat/completions");
    String existingModel = getDeepSeekConfig("DEEPSEEK_MODEL", "deepseek-v4-flash");

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 20, 10, 10));

    TextField apiKeyField = new TextField(existingKey);
    apiKeyField.setPromptText("sk-...");
    apiKeyField.setPrefWidth(300);

    TextField apiUrlField = new TextField(existingUrl);
    apiUrlField.setPromptText("https://api.deepseek.com/chat/completions");

    TextField modelField = new TextField(existingModel);
    modelField.setPromptText("deepseek-v4-flash");

    grid.add(new Label("API Key:"), 0, 0);
    grid.add(apiKeyField, 1, 0);
    grid.add(new Label("API URL:"), 0, 1);
    grid.add(apiUrlField, 1, 1);
    grid.add(new Label("Model:"), 0, 2);
    grid.add(modelField, 1, 2);

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      saveDeepSeekConfig("DEEPSEEK_API_KEY", apiKeyField.getText());
      saveDeepSeekConfig("DEEPSEEK_API_URL", apiUrlField.getText());
      saveDeepSeekConfig("DEEPSEEK_MODEL", modelField.getText());
      displayItem(new Response("Settings saved. DeepSeek is now configured."));
    }
  }

  private String getDeepSeekConfig(String key, String defaultValue) {
    File configFile = new File("deepseek.properties");
    if (!configFile.exists()) {
      return defaultValue;
    }
    Properties properties = new Properties();
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      properties.load(inputStream);
      return properties.getProperty(key, defaultValue);
    } catch (IOException e) {
      System.out.println("Could not read deepseek.properties: " + e);
      return defaultValue;
    }
  }

  private void saveDeepSeekConfig(String key, String value) {
    File configFile = new File("deepseek.properties");
    Properties properties = new Properties();
    if (configFile.exists()) {
      try (FileInputStream inputStream = new FileInputStream(configFile)) {
        properties.load(inputStream);
      } catch (IOException e) {
        // Start fresh if we can't read
      }
    }
    if (value != null && value.length() > 0) {
      properties.setProperty(key, value);
    } else {
      properties.remove(key);
    }
    try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
      properties.store(outputStream, "DeepSeek Configuration");
    } catch (IOException e) {
      System.out.println("Could not write deepseek.properties: " + e);
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
    displayItem(new EnteredCommand(command));
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
   * Adds an item to the display and scrolls to it.
   * @param item
   */
  public void displayItem(Displayable item) {
    Node node;
    
    if (item instanceof MarkdownResponse) {
      MarkdownResponse mr = (MarkdownResponse) item;
      WebView webView = new WebView();
      webView.setPrefHeight(80);
      webView.setMinHeight(40);
      webView.prefWidthProperty().bind(conversationBox.widthProperty().subtract(20));
      
      webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal == Worker.State.SUCCEEDED) {
          Platform.runLater(() -> {
            Object h = webView.getEngine().executeScript(
              "Math.max(document.body.scrollHeight || 0, " +
              "document.documentElement.scrollHeight || 0, " +
              "document.body.offsetHeight || 0, " +
              "document.documentElement.offsetHeight || 0)");
            if (h instanceof Number) {
              webView.setPrefHeight(((Number)h).doubleValue() + 10);
            }
          });
        }
      });
      
      String html = wrapMarkdownHtml(mr.getHtmlContent());
      byte[] utf8Bytes = html.getBytes(StandardCharsets.UTF_8);
      String dataUri = "data:text/html;charset=utf-8;base64," + Base64.getEncoder().encodeToString(utf8Bytes);
      webView.getEngine().load(dataUri);
      node = webView;
    } else {
      Label label = new Label();
      label.setWrapText(true);
      label.setMaxWidth(Double.MAX_VALUE);
      label.prefWidthProperty().bind(conversationBox.widthProperty().subtract(30));
      item.update(label);
      node = label;
    }
    
    conversationBox.getChildren().add(node);
    Platform.runLater(() -> scrollPane.setVvalue(1.0));
  }
  
  private static App[] getAvailableApps(){
    // TODO: add more apps avilable to the Virtual Assistant here
    return new App[]{new WeatherApp(), new TimeApp(), new TodoListApp(), new DeepSeekApp(), new CalculatorApp()};
  }
  
  /**
   * Wraps markdown HTML content with styling for display in a WebView.
   */
  private static String wrapMarkdownHtml(String body) {
    return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
      "<style>" +
      "body { font-family: -apple-system, 'Segoe UI', 'Microsoft YaHei', sans-serif; " +
      "  font-size: 14px; margin: 8px; padding: 0; color: #333; line-height: 1.6; " +
      "  word-wrap: break-word; overflow-wrap: break-word; }" +
      "pre { background: #f5f5f5; padding: 12px; border-radius: 6px; " +
      "  overflow-x: auto; font-size: 13px; border: 1px solid #e0e0e0; }" +
      "code { background: #f0f0f0; padding: 2px 5px; border-radius: 3px; " +
      "  font-family: 'Consolas', 'Courier New', monospace; font-size: 13px; }" +
      "pre code { background: none; padding: 0; }" +
      "table { border-collapse: collapse; margin: 8px 0; }" +
      "th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }" +
      "th { background: #f5f5f5; }" +
      "blockquote { border-left: 3px solid #4a90d9; margin: 8px 0; " +
      "  padding: 4px 12px; color: #555; background: #f9f9f9; }" +
      "h1, h2, h3, h4 { margin: 12px 0 6px 0; }" +
      "h1 { font-size: 1.4em; } h2 { font-size: 1.2em; } h3 { font-size: 1.1em; }" +
      "p { margin: 4px 0 8px 0; }" +
      "ul, ol { margin: 4px 0; padding-left: 24px; }" +
      "li { margin: 2px 0; }" +
      "a { color: #4a90d9; }" +
      "hr { border: none; border-top: 1px solid #ddd; margin: 12px 0; }" +
      "</style></head><body>" + encodeSupplementaryChars(body) + "</body></html>";
  }

  /**
   * Encodes supplementary Unicode characters (code points > U+FFFF)
   * as HTML numeric character references to work around JavaFX WebView
   * UTF-16 surrogate pair handling issues.
   */
  private static String encodeSupplementaryChars(String text) {
    StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      int cp = text.codePointAt(i);
      if (cp > 0xFFFF) {
        sb.append("&#x").append(Integer.toHexString(cp)).append(";");
        i++; // skip low surrogate
      } else {
        sb.append((char) cp);
      }
    }
    return sb.toString();
  }
}
