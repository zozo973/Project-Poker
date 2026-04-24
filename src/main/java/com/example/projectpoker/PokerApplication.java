package com.example.projectpoker;

import com.example.projectpoker.controller.RoundController;
import com.example.projectpoker.controller.RoundViewUpdater;
import com.example.projectpoker.handler.GameStatusChangeHandler;
import com.example.projectpoker.handler.PlayerStatusChangeHandler;
import com.example.projectpoker.handler.RoundStatusChangeHandler;
import com.example.projectpoker.model.game.AiPlayer;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.model.game.enums.GameStatus;
import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.model.User;
import com.example.projectpoker.service.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.projectpoker.model.statistics.SkewNormalSampler.safeRoundToInt;

public class PokerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.initializeDatabase();
        FXMLLoader fxmlLoader = new FXMLLoader(PokerApplication.class.getResource("/com/example/projectpoker/Account & Profile UI/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 400);

        // TODO implement stylesheet for welcome to app screen
        // String stylesheet = HelloApplication.class.getResource("welcome-stylesheet.css").toExternalForm();
        // scene.getStylesheets().add(stylesheet);

        stage.setTitle("App Name");
        stage.setScene(scene);
        stage.show();
        // createPokerGame();
    }

    public void createPokerGame() throws IOException {
        //  Call this method to create a new poker game.

        // TODO add methods in createPokerGame controller to get all fields required to make an instance of a game
        //      Player user: should be retrieved from database,
        //      int userBalance: User will interact to choose an amount, if they don't have enough it defaults to 1000,
        //      int numPlayers: min 3 max 8 - estimate,
        //      int initBlind: default if 0.03 times the user balance,
        //      int whenInceaseBlinds: how many rounds until blinds increase, default 10,
        //      int gameLength: num of rounds game goes for, default 30,
        //      Difficulty difficulty: HBox of buttons, can only select one, on hover shows display for difficulty

        // change to retire user data from database and other vars can be retrieved from user input into ui
        // Player user = new Player("User",10000); // Hardcoded values

        User loggedInUser = SessionManager.getCurrentUser();
        Player user = new Player(loggedInUser.getUsername(), loggedInUser.getCurrentBalance());
        // int userBuyIn = 1000;
        int userBuyIn = loggedInUser.getCurrentBalance();

        // calculate blind
        int blind = safeRoundToInt((userBuyIn*0.03));
        int numPlayer = 4;
        // create game
        Game game = new Game(user,userBuyIn,4,blind,10,40, Difficulty.GAMBLINGADDICT);

        RoundController controller = (RoundController) loadPokerGameView(game);

        PlayerStatusChangeHandler playerHandler = new PlayerStatusChangeHandler(controller);
        RoundStatusChangeHandler roundHandler = new RoundStatusChangeHandler(controller);
        GameStatusChangeHandler gameHandler = new GameStatusChangeHandler(controller);

        game.setRoundHandler(roundHandler);
        game.addPropertyChangeListener(gameHandler);
        game.init();

        Round currentRound = game.getRound();
        controller.setRound(currentRound,user);

        ArrayList<AiPlayer> AiPlayers = game.getAiPlayers();
        user.addPropertyChangeListener(playerHandler);
        for (AiPlayer p : AiPlayers) p.addPropertyChangeListener(playerHandler);
    }

    private RoundViewUpdater loadPokerGameView(Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader(PokerApplication.class.getResource("poker-round-view.fxml"));
        Scene scene = new Scene(loader.load(), 320,240);
        // TODO load css file ...

        RoundController controller = loader.getController();
        return controller;
    }
}
