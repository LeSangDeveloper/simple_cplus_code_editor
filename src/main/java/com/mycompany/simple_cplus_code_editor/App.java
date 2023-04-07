package com.mycompany.simple_cplus_code_editor;

import java.time.Duration;
import java.util.Optional;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.reactfx.Subscription;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var editorView = new CodeEditorView();
        
        var textArea = new TextArea();
        
        var btnLoadChanges = new Button("Load Changes");
        var labelLeftHbox = new Label("Checking for changes...");
        var simpleController = new SimpleFileEditorController(stage, textArea, btnLoadChanges, labelLeftHbox);
        
        var menuBar = new MenuBar();
        var menu = new Menu("File");
        
        var openMenu = new MenuItem("Open");
        openMenu.setOnAction((event) -> simpleController.chooseFile(event));
        var saveMenu = new MenuItem("Save");
        var closeMenu = new MenuItem("Close");
        
        menu.getItems().addAll(openMenu, saveMenu, closeMenu);
        menuBar.getMenus().add(menu);
        
        textArea.setEditable(true);
        
        var anchorPane = new AnchorPane(editorView.getCodeArea());

        var progressBar = new ProgressBar();
        var leftHbox = new HBox(labelLeftHbox, progressBar);
        
        var rightHbox = new HBox(simpleController.getBtnLoadChanges());
        
        var parentHbox = new HBox(leftHbox, rightHbox);
        
        var leftText = new Text("Left");
        var rightText = new Text("Right");
        
        BorderPane.setAlignment(editorView.getCodeArea(), Pos.CENTER);
        BorderPane.setAlignment(menuBar, Pos.TOP_CENTER);
        BorderPane.setAlignment(leftText, Pos.CENTER_LEFT);
        BorderPane.setAlignment(rightText, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(parentHbox, Pos.BOTTOM_CENTER);
        
        var root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(editorView.getCodeArea());
        root.setBottom(parentHbox);
        
        // Set the Size of the VBox
        root.setPrefSize(400, 400);     
        // Set the Style-properties of the BorderPane
        root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: transparent;");
        
        var scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(App.class.getResource("java-keywords.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}