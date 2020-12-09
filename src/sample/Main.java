package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{


        Pane gamePane = new GamePane();
        Scene scene = new Scene(gamePane,940, 600);
        primaryStage.setTitle("Wall Breaker");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        gamePane.requestFocus();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
