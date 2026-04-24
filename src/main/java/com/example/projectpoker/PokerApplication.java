package com.example.projectpoker;

import com.example.projectpoker.controller.RoundController;
import com.example.projectpoker.handler.GameStatusChangeHandler;
import com.example.projectpoker.handler.PlayerStatusChangeHandler;
import com.example.projectpoker.handler.RoundStatusChangeHandler;
import com.example.projectpoker.model.game.AiPlayer;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Difficulty;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.projectpoker.model.statistics.SkewNormalSampler.safeRoundToInt;

public class PokerApplication extends Application {

    public void start(Stage stage) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        PokerApplication.class.getResource(
                                "poker-round-view.fxml"
                        )
                );

        Parent root = loader.load();

        Scene scene = new Scene(root, 1025, 450);

        stage.setTitle("App Name");
        stage.setScene(scene);
        stage.show();

        RoundController controller =
                loader.getController();

        PokerGameUI pokerUI = new PokerGameUI();
        controller.setUI(pokerUI);
        createPokerGame(controller);
    }

    private void createPokerGame(RoundController controller){

        // TODO add methods in createPokerGame controller to get all fields required to make an instance of a game
        //      Player user: should be retrieved from database,
        //      int userBalance: User will interact to choose an amount, if they don't have enought it defaults to 1000,
        //      int numPlayers: min 3 max 8 - estimate,
        //      int initBlind: default if 0.03 times the user balance,
        //      int whenInceaseBlinds: how many rounds until blinds increase, default 10,
        //      int gameLength: num of rounds game goes for, default 30,
        //      Difficulty difficulty: HBox of buttons, can only select one, on hover shows display for difficulty

        // change to retire user data from database and other vars can be retrieved from user input into ui
        Player user = new Player("User",10000);
        int userBuyIn = 1000;
        int blind = safeRoundToInt((userBuyIn*0.03));
        Game game = new Game(user,userBuyIn,4,blind,10,40, Difficulty.GAMBLINGADDICT);


        PlayerStatusChangeHandler playerHandler = new PlayerStatusChangeHandler(controller);


        RoundStatusChangeHandler roundHandler = new RoundStatusChangeHandler(controller, game);
        GameStatusChangeHandler gameHandler = new GameStatusChangeHandler(controller);

        game.setRoundHandler(roundHandler);
        game.addPropertyChangeListener(gameHandler);


        Round currentRound = game.getRound();
        controller.setGame(game);
        controller.setRound(currentRound,user);
        game.init();
        game.start();
        ArrayList<AiPlayer> AiPlayers = game.getAiPlayers();
        user.addPropertyChangeListener(playerHandler);
        for (AiPlayer p : AiPlayers) p.addPropertyChangeListener(playerHandler);
    }

}
