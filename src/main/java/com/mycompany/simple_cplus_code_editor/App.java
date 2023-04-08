package com.mycompany.simple_cplus_code_editor;

import com.mycompany.simple_cplus_code_editor.controller.SimpleCommandController;
import com.mycompany.simple_cplus_code_editor.controller.SimpleCodeEditorController;
import com.mycompany.simple_cplus_code_editor.util.Command;
import com.mycompany.simple_cplus_code_editor.view.CodeEditorView;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Stack;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
        
        var progressBar = new ProgressBar();
        progressBar.setPrefWidth(150.0);
        
        var btnLoadChanges = new Button("Load Changes");
        
        var labelLeftHbox = new Label("Checking for changes...");
        labelLeftHbox.setPrefWidth(150.0);
        
        var simpleController = new SimpleCodeEditorController(editorView, btnLoadChanges, labelLeftHbox, progressBar);
        var cmdController = new SimpleCommandController();
        
        var menuBar = new MenuBar();
        var fileMenu = new Menu("File");
        
        var openMenu = new MenuItem("Open");
        openMenu.setOnAction((event) -> simpleController.chooseFile(event));
        var saveMenu = new MenuItem("Save");
        saveMenu.setOnAction((event) -> {
            if (simpleController.saveFile(event) == 0) {
                cmdController.showErrorToOutput("Save file failed!!!\n\n");
            }
        });
        var closeMenu = new MenuItem("Close");
        closeMenu.setOnAction((event) -> {
            simpleController.closeFile(event);
        });
        
        fileMenu.getItems().addAll(openMenu, saveMenu, closeMenu);
        menuBar.getMenus().add(fileMenu);
        
        var menuProgram = new Menu("Program");
        
        var compileMenu = new MenuItem("Compile");
        compileMenu.setOnAction((event) -> {
            if (simpleController.getLoadedFileReference() != null) {
                cmdController.compileProgram(simpleController.getLoadedFileReference().getAbsolutePath(), simpleController.getLoadedFileReference().getAbsolutePath().replace(".cpp", ".exe"));
            } else {
                cmdController.showErrorToOutput("Error: cannot compile source code! Please Open source code first!!\n\n");
            }
        });
        var runMenu = new MenuItem("Run");
        runMenu.setOnAction((event) -> {
            if (simpleController.getLoadedFileReference() != null) {
                cmdController.runProgram(simpleController.getLoadedFileReference().getAbsolutePath().replace(".cpp", ".exe"));
            } else {
                cmdController.showErrorToOutput("Error: cannot run Program! Please Open source code and compile it first!!\n\n");
            }
        });
        menuProgram.getItems().addAll(compileMenu, runMenu);
        menuBar.getMenus().add(menuProgram);        
        
        var leftHbox = new HBox(labelLeftHbox, progressBar);
        HBox.setHgrow(leftHbox, Priority.ALWAYS);
        leftHbox.setAlignment(Pos.CENTER_LEFT);
        
        var rightHbox = new HBox(simpleController.getBtnLoadChanges());
        HBox.setHgrow(rightHbox, Priority.ALWAYS);
        rightHbox.setAlignment(Pos.CENTER_RIGHT);
        
        var parentHbox = new HBox(leftHbox, rightHbox);
        parentHbox.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        
        ScrollPane srollOutput = new ScrollPane(cmdController.getOutputConsole());
        srollOutput.setFitToWidth(true);
        
        var vBoxCenter = new VBox(10, editorView.getCodeArea(), srollOutput);
        editorView.getCodeArea().setPrefSize(400, 300);
        
        vBoxCenter.setAlignment(Pos.CENTER);
        
        BorderPane.setAlignment(vBoxCenter, Pos.CENTER);
        BorderPane.setAlignment(menuBar, Pos.TOP_CENTER);
        BorderPane.setAlignment(parentHbox, Pos.BOTTOM_CENTER);
        
        var root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(vBoxCenter);
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
        stage.setTitle("Simple Code C++ Editor");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}