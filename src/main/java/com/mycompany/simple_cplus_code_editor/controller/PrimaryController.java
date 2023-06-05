/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.controller;

import com.mycompany.simple_cplus_code_editor.util.Command;
import com.mycompany.simple_cplus_code_editor.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * FXML Controller class
 *
 * @author lesan
 */
public class PrimaryController implements Initializable {

    @FXML
    private CodeArea codeArea;
    @FXML
    private TextFlow outputConsole;
    @FXML
    private Label statusMessage;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button btnLoadChanges;
    
    private File loadedFileReference = null;
    private FileTime lastModifiedTime = null;
    private ExecutorService executor;
    private Command cmd;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        executor = Executors.newSingleThreadExecutor();
        cmd = new Command();
        
        btnLoadChanges.setVisible(false);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setContextMenu( new ContextMenu() );
        
        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .retainLatestUntilLater(executor)
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);
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
        codeArea.clear();
        statusMessage.setText("File closed: " + temp);
    }
    
    public void compileProgram(ActionEvent event) {
        Text prepare = new Text(String.format("Compiling %s....", loadedFileReference.getAbsolutePath()));
        String outputLocation = loadedFileReference.getAbsolutePath().replace(".cpp", ".exe");
        outputConsole.getChildren().add(prepare);
        try {
            String result1 = cmd.runCommand("g++.exe");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
//            InputText
            Text failedCompiled = new Text("Failed! Please install MINGW or G++ compiler and add to PATH environment!!!\n\n");
            failedCompiled.setStroke(Paint.valueOf("red"));
            outputConsole.getChildren().add(failedCompiled);
            return;
        }

        String result = null;
        try {
            result = cmd.runCommand("g++.exe", loadedFileReference.getAbsolutePath(), "-o", outputLocation);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Text success = new Text("Success!!\n\n");
        success.setStroke(Paint.valueOf("green"));
        outputConsole.getChildren().add(success);
        
        if (result != null) {
            Text resultFinal = new Text(result);
            outputConsole.getChildren().add(resultFinal);
        }
    }
    
    public void showErrorToOutput(String error) {
        Text failedCompiled = new Text(error);
        failedCompiled.setStroke(Paint.valueOf("red"));
        outputConsole.getChildren().add(failedCompiled);
    }
    
    public void runProgram(ActionEvent e) {
        String fileLocation = loadedFileReference.getAbsolutePath().replace(".cpp", ".exe");
            Runtime runTime = Runtime.getRuntime();

            String executablePath = "cmd /c start \"\" " + fileLocation + " & pause";

        try {
            Process process = runTime.exec(executablePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
                codeArea.clear();
                codeArea.replaceText(0, 0, loadFileTask.get());
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
        scheduledService.setPeriod(javafx.util.Duration.seconds(1));
        return scheduledService;
    }
    
    public void loadChanges(ActionEvent event) {
        loadFileToTextArea(loadedFileReference);
        btnLoadChanges.setVisible(false);
    }
    
    private void notifyUserOfChanges() {
        btnLoadChanges.setVisible(true);
    }
    
    public int saveFile(ActionEvent e) {
        return saveFile();
    }
    
    public int saveFile() {
        if (loadedFileReference == null) {
            return 0;
        }
        try {
            try (FileWriter myWriter = new FileWriter(loadedFileReference)) {
                myWriter.write(codeArea.getText());
            }
            lastModifiedTime = FileTime.fromMillis(System.currentTimeMillis() + 3000);
            System.out.println("Successfully wrote to the file.");
            return 1;
        } catch (IOException e) {
            // TODO Log error
            return 0;
        }
    }
    
    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }
    
    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = Utils.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("PREPROCESSINGWORD") != null ? "preprocessing-word":
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
}
