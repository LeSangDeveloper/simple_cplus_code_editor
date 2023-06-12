package com.mycompany.simple_cplus_code_editor;

import com.mycompany.simple_cplus_code_editor.controller.PrimaryController;
import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader primaryFXML = loadFXML("primary");
        scene = new Scene(primaryFXML.load(), 640, 480);
        scene.getStylesheets().add(App.class.getResource("cplus-keywords.css").toExternalForm());
        
        PrimaryController primaryController = primaryFXML.getController();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            final KeyCombination keyComb = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
            @Override
            public void handle(KeyEvent ke) {
                if (keyComb.match(ke)) {
                    System.out.println("Key Pressed: " + keyComb);
                    primaryController.saveFile();
                    ke.consume(); // <-- stops passing the event to next node
                }
            }
        });
        
        stage.setScene(scene);
        stage.setTitle("Simple C++ Editor");
        stage.getIcons().add(new Image(App.class.getResource("app_icon.png").toExternalForm()));
        stage.show();
    }

    private static FXMLLoader loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader;
    }
    
    public static void main(String[] args) {
        launch();
    }

}