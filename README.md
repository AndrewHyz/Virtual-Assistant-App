# Virtual Assistant App

A JavaFX desktop virtual assistant with a chat-like interface. Type commands and the assistant picks the best matching app to handle them.

**Latest release:** [VirtualAssistant-1.1.1.exe](https://github.com/AndrewHyz/Virtual-Assistant-App/releases/tag/v1.1.1) (Windows installer, includes bundled JRE — no Java install needed)

## Features

| App | Description |
|---|---|
| DeepSeek AI | Chat with DeepSeek, responses rendered as Markdown |
| Weather | Current temperature and conditions via OpenWeatherMap |
| Time | Local time display |
| To-Do List | Add, list, remove, and clear items |
| Calculator | Math expressions via math.js API |

Long responses automatically wrap to fit the window. Resize the window and text reflows.

## How To Start The App

Open the project in Eclipse, then run:

```text
src/assistant/Main.java
```

Main class:

```text
assistant.Main
```

Do not run `assistant.Assistant` directly.

If Eclipse shows:

```text
JavaFX runtime components are missing, and are required to run this application
```

make sure your Run Configuration uses `assistant.Main` as the main class.

## How To Use The App

1. Type a command in the text box next to `Command:`.
2. Press `Enter` or click `Go`.
3. The assistant shows your command and its response.

### Settings

Click the **gear button** next to `Go` to open the settings dialog. You can configure:

- **API Key** — your DeepSeek API key (`sk-...`)
- **API URL** — defaults to `https://api.deepseek.com/chat/completions`
- **Model** — defaults to `deepseek-v4-flash`

Settings are saved to `deepseek.properties` in the project root.

## Time Commands

Examples:

```text
what time is it
time
show me the time
```

Response:

```text
Local time: 14:35:20.123
```

## Weather Commands

Requires internet. Temperatures in Celsius.

Examples:

```text
weather in Boston
what is the weather in Shanghai
weather zip 02139
```

Response:

```text
The temperature in Boston is 22.5 degrees Celsius with clear sky
```

## To-Do List Commands

Items are stored in memory (reset when the app closes).

Add:

```text
add todo buy milk
add task finish homework
```

Show:

```text
list todos
todo list
```

Remove:

```text
remove buy milk
```

Clear:

```text
clear todos
```

## Calculator Commands

Requires internet. Sends expressions to math.js.

Examples:

```text
calculate 2 + 2
calc 2 * (7 - 3)
math sqrt(16)
```

Response:

```text
2 + 2 = 4
```

## DeepSeek AI Commands

Requires internet and a DeepSeek API key.

### Setup

Click the **Settings** button in the app, enter your API key, and click OK. Or create `deepseek.properties` manually:

```text
DEEPSEEK_API_KEY=your_api_key_here
DEEPSEEK_API_URL=https://api.deepseek.com/chat/completions
DEEPSEEK_MODEL=deepseek-v4-flash
```

A template is at `deepseek.properties.example`.

### Usage

Examples:

```text
ask deepseek explain Java arrays
deepseek write a haiku about Shanghai
ai give me three study tips
```

### Markdown Rendering

DeepSeek responses are rendered as Markdown:

- Headings, paragraphs
- **Bold** and *italic*
- Code blocks with syntax highlighting style
- Inline `code`
- Tables, blockquotes
- Ordered and unordered lists
- Links

Implementation:

```text
src/assistant/app/MarkdownResponse.java
```

Uses [flexmark](https://github.com/vsch/flexmark-java) to convert Markdown to HTML, displayed in a JavaFX WebView.

## How Commands Are Chosen

Each app has `Action` classes that score the user's command. The highest-scoring action above `0.5` wins.

## Project Structure

```text
src/assistant/
  Assistant.java          Main application class (UI + command routing)
  EnteredCommand.java     User command display item
  Main.java               Entry point
  app/
    Action.java           Abstract action (score + execute)
    App.java              Abstract app (provides actions)
    Displayable.java      Interface for display items
    Response.java         Plain text response
    MarkdownResponse.java Markdown response (via WebView)
    calculator/           Calculator app
    deepseek/             DeepSeek AI app
    time/                 Time app
    todo/                 To-do list app
    weather/              Weather app
  assets/
    app-icon.png          Application icon
```

## Dependencies

Managed via Maven (`pom.xml`):

| Dependency | Purpose |
|---|---|
| JavaFX 12 | UI framework |
| flexmark 0.64.8 | Markdown to HTML |
| unirest-java 1.4.9 | HTTP client |
| Gson 2.8.6 | JSON parsing |

Run `Maven > Update Project` in Eclipse before building.

## Adding A New App

1. Create a package under `src/assistant/app/`.
2. Create an app class extending `App`.
3. Create action classes extending `Action`.
4. Return actions via `getActions()`.
5. Register the app in `Assistant.getAvailableApps()`.

Example:

```java
return new App[]{new WeatherApp(), new TimeApp(), new TodoListApp(),
                 new DeepSeekApp(), new CalculatorApp(), new MyApp()};
```

## Building the Installer

Uses `jpackage` (JDK 14+) with WiX on Windows:

```bash
mvn clean package -DskipTests
jpackage --type exe --name VirtualAssistant --app-version 1.1.1 \
  --input target --main-jar AssistantApp.jar --main-class assistant.Main \
  --icon app-icon.ico --win-dir-chooser --win-menu --win-shortcut
```
