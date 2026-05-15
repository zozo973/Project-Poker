# PokerPro+

PokerPro+ is a JavaFX Texas Hold'em project with:
- account login/register
- local SQLite persistence for users and game sessions
- playable rounds against AI opponents
- optional AI coaching suggestions during a round

## Tech Stack

- Java 21
- Maven (wrapper included: `mvnw`, `mvnw.cmd`)
- JavaFX 21
- SQLite (`sqlite-jdbc`)
- JUnit 5
- Google Gson + Java `HttpClient` (AI coaching requests)

## Requirements

Before running the app, install:
- JDK 21
- internet access if you want AI coaching

You do not need a global Maven install if you use the Maven wrapper.

## Quick Start

From the project root (`Project-Poker`):

### Windows PowerShell
```powershell
.\mvnw.cmd clean javafx:run
```

### macOS/Linux
```bash
./mvnw clean javafx:run
```

Main entry points in code:
- `src/main/java/com/example/projectpoker/Launcher.java`
- `src/main/java/com/example/projectpoker/PokerApplication.java`

## Running Tests

### Windows PowerShell
```powershell
.\mvnw.cmd test
```

### macOS/Linux
```bash
./mvnw test
```

## Database Notes

The app initializes required tables at startup via `DatabaseManager.initializeDatabase()`.

Main database logic is in:
- `src/main/java/com/example/projectpoker/database/DatabaseManager.java`

SQLite database files are stored in the project folder (for example `projectpoker.db`).
If you want a clean local reset, close the app and remove the local DB file(s).

## AI Coaching Setup (API Key)

AI coaching is implemented in:
- `src/main/java/com/example/projectpoker/AiCoaching.java`

### Current key location in this project
The Gemini key is read from a constant in `AiCoaching.java`:
- `GEMINI_API_KEY`

To enable AI coaching for your own environment, replace that value with your own key.

### Recommended secure setup (best practice)
For team use, do not keep real keys hardcoded in source control.
Use an environment variable instead.

1. Set environment variable:

Windows PowerShell (current session):
```powershell
$env:GEMINI_API_KEY="your-real-key"
```

Windows (persist for future shells):
```powershell
setx GEMINI_API_KEY "your-real-key"
```

2. Update `AiCoaching.java` to read from env var, for example:
```java
private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");
```

3. Add a null/blank check so the UI can show a clear message when no key is set.

## Gameplay Flow (High-Level)

- `PokerApplication` opens login UI.
- After login, `createPokerGame(...)` loads the table view.
- `RoundController` wires UI actions (call/raise/fold/all-in) to the game model.
- `Game` and `Round` drive hand progression (deal -> betting streets -> showdown).
- `PokerGameUI` handles visual table rendering.
- Property change listeners keep the UI in sync with model updates.

## Useful Project Paths

- Controllers: `src/main/java/com/example/projectpoker/controller`
- Core game model: `src/main/java/com/example/projectpoker/model/game`
- Database layer: `src/main/java/com/example/projectpoker/database`
- FXML/UI resources: `src/main/resources/com/example/projectpoker`
- Tests: `src/test/java/com/example/projectpoker`

## Troubleshooting

- If AI coaching fails, verify:
  - your API key is valid
  - internet access is available
  - a round is active (community cards/state are initialized)
- If JavaFX launch fails, confirm you are using Java 21 and running through Maven wrapper commands above.

## Security Reminder

If a real API key was ever committed to git history, rotate/revoke it in the provider console and replace it with a new key.

