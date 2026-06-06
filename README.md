# KTByte Virtual Assistant

This is a JavaFX virtual assistant app. The assistant opens a small desktop window where you can type commands. It reads your command, chooses the app action that best matches it, and displays a response in the conversation list.

## How To Start The App

Open the project in Eclipse, then run:

```text
src/assistant/Main.java
```

The main class is:

```text
assistant.Main
```

When the app starts, a window titled `Virtual Assistant` will appear.

If Eclipse shows this error:

```text
缺少 JavaFX 运行时组件, 需要使用该组件来运行此应用程序
```

make sure your Run Configuration uses this main class:

```text
assistant.Main
```

Do not run `assistant.Assistant` directly.

The JavaFX native-access warning can appear when running JavaFX 12 on a newer JDK. It is a warning, not the crash. The app can still run.

The app icon is stored here:

```text
src/assistant/assets/app-icon.png
```

To change the icon again, replace that PNG file with another PNG image and rerun the app.

## How To Use The App

1. Type a command in the text box next to `Command:`.
2. Press `Enter` or click the `Go` button.
3. The assistant will show your command and then display its response.

The assistant currently supports Weather, Time, To-Do List, DeepSeek AI, and Calculator commands.

## Time Commands

The Time app displays the current local time.

Example commands:

```text
what time is it
time
show me the time
```

Example response:

```text
Local time: 14:35:20.123
```

## Weather Commands

The Weather app gets weather information from OpenWeatherMap. Temperatures are shown in Celsius.

Example commands:

```text
weather in Boston
what is the weather in Shanghai
weather zip 02139
```

Example response:

```text
The temperature in Boston is 22.5 degrees Celsius with clear sky
```

Note: the Weather app requires an internet connection.

## To-Do List Commands

The To-Do List app lets you add, view, remove, and clear to-do items while the assistant is running.

Add an item:

```text
add todo buy milk
add task finish homework
add to-do call mom
```

Show the list:

```text
list todos
show todo list
todo list
```

Remove an item:

```text
remove buy milk
remove finish homework
```

Clear the whole list:

```text
clear todo list
clear todos
```

Example responses:

```text
Added to your to-do list: buy milk
To-do list: 1. buy milk, 2. finish homework
Removed from your to-do list: buy milk
Your to-do list is now empty
```

Note: to-do items are stored only while the program is running. If you close the app, the list will reset.

## Calculator Commands

The Calculator app sends math expressions to the math.js web service and displays the result.

Example commands:

```text
calculate 2 + 2
calc 2 * (7 - 3)
math sqrt(16)
solve 5.08 cm in inch
```

Example response:

```text
2 + 2 = 4
```

Note: the Calculator app requires an internet connection.

## DeepSeek AI Commands

The DeepSeek app sends a question or prompt to DeepSeek through the OpenAI-compatible chat completions API.

Before using it, set your API key as an environment variable:

```text
DEEPSEEK_API_KEY=your_api_key_here
```

If Eclipse does not pass environment variables to the app, create a file named `deepseek.properties` in the project root:

```text
DEEPSEEK_API_KEY=your_api_key_here
DEEPSEEK_API_URL=https://api.deepseek.com/chat/completions
DEEPSEEK_MODEL=deepseek-v4-flash
```

There is an example file at:

```text
deepseek.properties.example
```

Example commands:

```text
ask deepseek explain Java arrays
deepseek write a haiku about Shanghai
ai give me three study tips
chat summarize what polymorphism means
```

Example response:

```text
DeepSeek will answer your prompt in the assistant window.
```

The app uses this model by default:

```text
deepseek-v4-flash
```

You can override the model with:

```text
DEEPSEEK_MODEL=deepseek-chat
```

The default API URL is:

```text
https://api.deepseek.com/chat/completions
```

You can override it with:

```text
DEEPSEEK_API_URL=https://api.deepseek.com/chat/completions
```

The DeepSeek app code is here:

```text
src/assistant/app/deepseek/AskDeepSeekAction.java
```

## How Commands Are Chosen

Each app has one or more `Action` classes. Every action gives the command a likelihood score. The assistant runs the action with the highest score if that score is greater than `0.5`.

Important files:

```text
src/assistant/Assistant.java
src/assistant/app/time/TimeApp.java
src/assistant/app/time/GetTimeAction.java
src/assistant/app/weather/WeatherApp.java
src/assistant/app/weather/GetWeatherAction.java
src/assistant/app/todo/TodoListApp.java
src/assistant/app/todo/TodoListAction.java
src/assistant/app/deepseek/DeepSeekApp.java
src/assistant/app/deepseek/AskDeepSeekAction.java
src/assistant/app/calculator/CalculatorApp.java
src/assistant/app/calculator/CalculateAction.java
```

## Adding A New App

To add another assistant feature:

1. Create a new package under `src/assistant/app/`.
2. Create an app class that extends `App`.
3. Create one or more action classes that extend `Action`.
4. Return the actions from the app class using `getActions()`.
5. Add the app to `getAvailableApps()` in `Assistant.java`.

Example:

```java
return new App[]{new WeatherApp(), new TimeApp(), new TodoListApp(), new DeepSeekApp(), new CalculatorApp()};
```
