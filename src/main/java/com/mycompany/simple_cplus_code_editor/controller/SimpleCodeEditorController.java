/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.controller;

import com.mycompany.simple_cplus_code_editor.view.CodeEditorView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxmisc.richtext.CodeArea;

/**
 *
 * @author lesan
 */
public class SimpleCodeEditorController implements EventHandler {
    
    private File loadedFileReference = null;
    private FileTime lastModifiedTime = null;
    
    private final CodeEditorView codeEditorView;
    private final Button btnLoadChanges;
    private final Label statusMessage;
    private final ProgressBar progressBar;
    
    
    public SimpleCodeEditorController(CodeEditorView codeEditorView, Button btnLoadChanges, Label statusMessage, ProgressBar progressBar) {
        this.codeEditorView = codeEditorView;
        this.btnLoadChanges = btnLoadChanges;
        this.btnLoadChanges.setVisible(false);
        this.btnLoadChanges.setOnAction(this);
        this.statusMessage = statusMessage;
        this.progressBar = progressBar;
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
                new FileChooser.ExtensionFilter("Source code (*.cpp)", "*.cpp")
        );
        //set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        //let user select file
        File fileToLoad = fileChooser.showOpenDialog(null);
        //if file has been chosen, load it using asynchronous method (define later)
        if(fileToLoad != null){
            loadFileToTextArea(fileToLoad);
        }
    }
    
    public void closeFile(ActionEvent event) {
        String temp = loadedFileReference.getName();
        loadedFileReference = null;
        lastModifiedTime = null;
        codeEditorView.getCodeArea().clear();
        statusMessage.setText("File closed: " + temp);
    }
    
    private void loadFileToTextArea(File fileToLoad) {
        Task<String> loadTask = fileLoaderTask(fileToLoad);
        progressBar.progressProperty().bind(loadTask.progressProperty());
        loadTask.run();
    }
    
    private Task<String> fileLoaderTask(File fileToLoad) {
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
            totalFile.deleteCharAt(totalFile.length() - 1);
            return totalFile.toString();
            }
        };
        
        loadFileTask.setOnSucceeded(workerStateEvent -> {
            try {
                codeEditorView.getCodeArea().clear();
                codeEditorView.getCodeArea().replaceText(0, 0, loadFileTask.get());
                statusMessage.setText("File loaded: " + fileToLoad.getName());
                loadedFileReference = fileToLoad;
                lastModifiedTime = Files.readAttributes(fileToLoad.toPath(), BasicFileAttributes.class).lastModifiedTime();
            } catch (InterruptedException | ExecutionException | IOException e) {

            }
            scheduleFileChecking(loadedFileReference);
        });
        
        loadFileTask.setOnFailed(workerStateEvent -> {
            // TODO show failed
            statusMessage.setText("Failed to load file");
        });
        
        return loadFileTask;
    }

    private void scheduleFileChecking(File file) {
        ScheduledService<Boolean> fileChangeCheckingService = createFileChangesCheckingService(file);
        fileChangeCheckingService.setOnSucceeded(workerStateEvent -> {
            if (fileChangeCheckingService.getLastValue() == null) return;
            if (fileChangeCheckingService.getLastValue()) {
                //no need to keep checking
                fileChangeCheckingService.cancel();
                notifyUserOfChanges();
            }
        });
        System.out.println("Starting Checking Service...");
        fileChangeCheckingService.start();
    }
    
    private ScheduledService<Boolean> createFileChangesCheckingService(File file) {
        ScheduledService<Boolean> scheduledService = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        FileTime lastModifiedAsOfNow = Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime();
                        return lastModifiedAsOfNow.compareTo(lastModifiedTime) > 0;
                    }
                };
            }
        };
        scheduledService.setPeriod(Duration.seconds(1));
        return scheduledService;
    }
    
    private void notifyUserOfChanges() {
        btnLoadChanges.setVisible(true);
    }
    
    public void loadChanges(ActionEvent event) {
        loadFileToTextArea(loadedFileReference);
        btnLoadChanges.setVisible(false);
    }
    
    public int saveFile(ActionEvent event) {
        if (loadedFileReference == null) {
            return 0;
        }
        try {
            try (FileWriter myWriter = new FileWriter(loadedFileReference)) {
                myWriter.write(codeEditorView.getCodeArea().getText());
            }
            lastModifiedTime = FileTime.fromMillis(System.currentTimeMillis() + 3000);
            System.out.println("Successfully wrote to the file.");
            return 1;
        } catch (IOException e) {
            // TODO Log error
            return 0;
        }
    }
    
    public CodeArea getCodeArea() {
        return codeEditorView.getCodeArea();
    }

    public Button getBtnLoadChanges() {
        return btnLoadChanges;
    }

    public File getLoadedFileReference() {
        return loadedFileReference;
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }
  
}
