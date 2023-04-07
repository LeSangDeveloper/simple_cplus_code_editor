/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author lesan
 */
public class SimpleFileEditorController implements EventHandler {
    
    private final Stage primaryStage;
    private final TextArea textArea;
    private final Button btnLoadChanges;
    private final Label statusMessage;
    
    public SimpleFileEditorController(Stage primaryStage, TextArea textArea, Button btnLoadChanges, Label statusMessage) {
        this.primaryStage = primaryStage;
        textArea.setEditable(true);
        this.textArea = textArea;
        this.btnLoadChanges = btnLoadChanges;
//        this.btnLoadChanges.setVisible(false);
        this.btnLoadChanges.setOnAction(this);
        this.statusMessage = statusMessage;
    }
    
    @Override
    public void handle(final Event event) {
        final Object source = event.getSource();

        if (source.equals(this.btnLoadChanges)) {
            System.out.println("ButtonA has been pressed");
        }
    }

    public void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        //only allow text files to be selected using chooser
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );
        //set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        //let user select file
        File fileToLoad = fileChooser.showOpenDialog(null);
        //if file has been chosen, load it using asynchronous method (define later)
        if(fileToLoad != null){
            Task<String> tasker = loadFileToTextArea(fileToLoad);
            // TODO test, remove later
            tasker.run();
        }
    }
    
    private Task<String> loadFileToTextArea(File fileToLoad) {
        Task<String> loadFileTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                BufferedReader reader = new BufferedReader(new FileReader(fileToLoad));
            //Use Files.lines() to calculate total lines - used for progress
            long lineCount;
            try (Stream<String> stream = Files.lines(fileToLoad.toPath())) {
                lineCount = stream.count();
            }
            //Load in all lines one by one into a StringBuilder separated by "\n" - compatible with TextArea
            String line;
            StringBuilder totalFile = new StringBuilder();
            long linesLoaded = 0;
            while((line = reader.readLine()) != null) {
                totalFile.append(line);
                totalFile.append("\n");
                updateProgress(++linesLoaded, lineCount);
            }
            return totalFile.toString();
            }
        };
        
        loadFileTask.setOnSucceeded(workerStateEvent -> {
            try {
                textArea.setText(loadFileTask.get());
                statusMessage.setText("File loaded: " + fileToLoad.getName());
            } catch (InterruptedException | ExecutionException e) {
                
            }
        });
        
        loadFileTask.setOnFailed(workerStateEvent -> {
            
        });
        
        return loadFileTask;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public Button getBtnLoadChanges() {
        return btnLoadChanges;
    }
 
    
    
}
