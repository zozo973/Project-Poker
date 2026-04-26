package com.example.projectpoker;

import com.example.projectpoker.controller.RoundController;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.Difficulty;
import com.example.projectpoker.database.DatabaseManager;
import com.example.projectpoker.model.User;
import com.example.projectpoker.service.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.example.projectpoker.model.statistics.SkewNormalSampler.safeRoundToInt;

public class PokerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.initializeDatabase();
        FXMLLoader fxmlLoader = new FXMLLoader(PokerApplication.class.getResource("/com/example/projectpoker/Account & Profile UI/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 400);

        stage.setTitle("App Name");
        stage.setScene(scene);
        stage.show();
    }

    public void createPokerGame() throws IOException {
        //  Call this method to create a new poker game.

        // TODO add methods in createPokerGame controller to get all fields required to make an instance of a game
        //      Player user: should be retrieved from database,
        //      int userBalance: User will interact to choose an amount, if they don't have enought it defaults to 1000,
        //      int numPlayers: min 3 max 8 - estimate,
        //      int initBlind: default if 0.03 times the user balance,
        //      int whenInceaseBlinds: how many rounds until blinds increase, default 10,
        //      int gameLength: num of rounds game goes for, default 30,
        //      Difficulty difficulty: HBox of buttons, can only select one, on hover shows display for difficulty

        // change to retire user data from database and other vars can be retrieved from user input into ui
        FXMLLoader loader = new FXMLLoader(
                PokerApplication.class.getResource("poker-round-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1025, 525);

        RoundController controller = loader.getController();
        PokerGameUI pokerUI = new PokerGameUI();
        controller.setUI(pokerUI);

        User loggedInUser = SessionManager.getCurrentUser();
        Player user = new Player(loggedInUser.getUsername(), loggedInUser.getCurrentBalance());
        int userBuyIn = loggedInUser.getCurrentBalance();
        int blind = safeRoundToInt((userBuyIn * 0.03));

        Game game = new Game(user, loggedInUser, userBuyIn, 6, blind, 10, 40, Difficulty.GAMBLINGADDICT);
        controller.setGame(game);
        controller.setRound(game.getRound(), user);
        game.init();

        Stage gameStage = new Stage();
        gameStage.setScene(scene);
        gameStage.setTitle("Poker Game");
        gameStage.show();
    }
}
