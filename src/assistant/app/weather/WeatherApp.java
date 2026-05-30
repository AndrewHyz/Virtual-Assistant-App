package assistant.app.weather;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.mashape.unirest.http.JsonNode;

import assistant.app.Action;
import assistant.app.App;

public class WeatherApp extends App{

  @Override
  public List<Action> getActions() {
    return Arrays.asList(new GetWeatherAction());
  }
}
