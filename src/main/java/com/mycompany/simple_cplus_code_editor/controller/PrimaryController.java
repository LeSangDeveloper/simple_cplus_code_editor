/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.controller;

import com.mycompany.simple_cplus_code_editor.model.FileViewElement;
import com.mycompany.simple_cplus_code_editor.model.TabCodeInfo;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

/**
 * FXML Controller class
 *
 * @author lesan
 */
public class PrimaryController implements Initializable {

    @FXML
    private TextFlow outputConsole;
    @FXML
    private Label statusMessage;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button btnLoadChanges;
    @FXML
    private TreeView folderTreeView;
    @FXML
    private TabPane tabCodeContainer;
    
    private CodeArea codeArea;
    private File loadedFileReference = null;
    private FileTime lastModifiedTime = null;
    private File currentRootFolder = null;
    private FileTime lastModifiedTimeFolder = null;
    private ExecutorService executor;
    private List<TabCodeInfo> listTabInfo = new ArrayList<>();
    private TabCodeInfo currentTab = null;
    private ScheduledService<Boolean> folderCheckingService = null;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        executor = Executors.newSingleThreadExecutor();
        btnLoadChanges.setVisible(false);
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
            listTabInfo.clear();
            tabCodeContainer.getTabs().clear();
            loadFileToTextArea(fileToLoad, true, true);
        }
    }
    
    public void closeFile(ActionEvent event) {
//        closeFile();
        this.listTabInfo.remove(this.currentTab);
        this.tabCodeContainer.getTabs().remove(this.currentTab.getTab());
        if (!this.listTabInfo.isEmpty())
            changeTab(this.listTabInfo.get(0));
    }
    
    public void openFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        //only allow text files to be selected using chooser
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFolder = directoryChooser.showDialog(null);
        
        listTabInfo.clear();
        tabCodeContainer.getTabs().clear();
        loadedFileReference = null;
        lastModifiedTime = null;
        codeArea = null;
        outputConsole.getChildren().clear();
        
        if (selectedFolder != null) {
            setRootTreeView(selectedFolder);
            
            if (folderCheckingService != null) {
                folderCheckingService.cancel();
                folderCheckingService = null;
            }
            
            folderCheckingService = scheduleFolderChecking(selectedFolder);
        }
    }
    
    public void reload(ActionEvent event) { 
        if (loadedFileReference != null) {
            setRootTreeView(loadedFileReference);
        }
    }
    
    public void selectItem() {
        TreeItem<FileViewElement> item = (TreeItem<FileViewElement>) folderTreeView.getSelectionModel().getSelectedItem();

        if (item != null) {
            boolean isOpenNewTab = true;
            for (TabCodeInfo info : listTabInfo) {
            String filePath = item.getValue().getFile().getAbsolutePath();
            if (info.getFile().getAbsolutePath().equals(filePath)) {
                    isOpenNewTab = false;
                    changeTab(info);
                }
            }
            
            System.out.println(item.getValue());
            if (loadedFileReference != null) {
//                closeFile();
            }
            if (item.getValue().getFile().listFiles() == null && !item.getValue().getFile().getName().endsWith(".exe") && isOpenNewTab) {
                loadFileToTextArea(item.getValue().getFile(), false, isOpenNewTab);
            }
        }
    }
    
    public void compileProgram(ActionEvent event) {
        if (loadedFileReference != null && loadedFileReference.getName().endsWith(".cpp")) {
            Text prepare = new Text(String.format("Compiling %s....", loadedFileReference.getAbsolutePath()));
            String outputLocation = loadedFileReference.getAbsolutePath().replace(".cpp", ".exe");
            outputConsole.getChildren().add(prepare);
            try {
                String result1 = Command.runCommand("g++.exe");
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
                result = Command.runCommand("g++.exe", loadedFileReference.getAbsolutePath(), "-o", outputLocation);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        
            if (result != null && result.length() > 0) {
                Text failed = new Text("Failed!!\n\n");
                failed.setStroke(Paint.valueOf("red"));
                outputConsole.getChildren().add(failed);
            
                Text resultFinal = new Text(result);
                outputConsole.getChildren().add(resultFinal);
            } else {
                Text success = new Text("Success!!\n\n");
                success.setStroke(Paint.valueOf("green"));
                outputConsole.getChildren().add(success);
            }
        }
    }
    
    public void showErrorToOutput(String error) {
        Text failedCompiled = new Text(error);
        failedCompiled.setStroke(Paint.valueOf("red"));
        outputConsole.getChildren().add(failedCompiled);
    }
    
    public void runProgram(ActionEvent e) {
        if (loadedFileReference!= null && loadedFileReference.getAbsolutePath().endsWith(".cpp")){
            String fileLocation = loadedFileReference.getAbsolutePath().replace(".cpp", ".exe");
            Runtime runTime = Runtime.getRuntime();

            String executablePath = "cmd /c start \"\" " + fileLocation + " & pause";

            try {
                Process process = runTime.exec(executablePath);
            } catch (IOException ex) {
            }
       }
    }
    
    private void loadFileToTextArea(File fileToLoad, boolean isResetTreeView, boolean isOpenNewTab) {
        Task<String> loadTask = fileLoaderTask(fileToLoad, isResetTreeView, isOpenNewTab);
        progressBar.progressProperty().bind(loadTask.progressProperty());
        loadTask.run();
    }
    
    private Task<String> fileLoaderTask(File fileToLoad, boolean isResetTreeView, boolean isOpenNewTab) {
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
            if (totalFile.length() >= 1)
                totalFile.deleteCharAt(totalFile.length() - 1);
            return totalFile.toString();
            }
        };
        
        loadFileTask.setOnSucceeded(workerStateEvent -> {
            try {
                if (isOpenNewTab)
                    openNewTab(fileToLoad.getName(), fileToLoad);
//                else changeTab(fileToLoad.getAbsolutePath());
                
                this.codeArea.clear();
                this.codeArea.replaceText(0, 0, loadFileTask.get());
                statusMessage.setText("File loaded: " + fileToLoad.getName());
                loadedFileReference = fileToLoad;
                lastModifiedTime = Files.readAttributes(fileToLoad.toPath(), BasicFileAttributes.class).lastModifiedTime();
                outputConsole.getChildren().clear();
                if (isResetTreeView){
                    setRootTreeView(loadedFileReference);
                    
                    if (folderCheckingService != null && !fileToLoad.getAbsolutePath().contains(currentRootFolder.getAbsolutePath())) {
                        folderCheckingService.cancel();
                        folderCheckingService = null;
                    }
                    
                    folderCheckingService = scheduleFolderChecking(loadedFileReference);
                }
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
        loadFileToTextArea(loadedFileReference, false, false);
        btnLoadChanges.setVisible(false);
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
    
    private TreeItem<FileViewElement> getItemTreeFolder(File folder) {
        URL url = PrimaryController.class.getResource("folder_icon.png");
        URL url_cplus = PrimaryController.class.getResource("cplusplus_icon.png");
        URL url_file = PrimaryController.class.getResource("file_icon.png");
        TreeItem<FileViewElement> result = new TreeItem<>(new FileViewElement(folder), new ImageView(new Image(url.toExternalForm())));
        File[] files = folder.listFiles();
        if (files.length > 0) {
            for (File file:files) {
                if (!file.getName().endsWith(".exe")) {
                    File[] subFiles = file.listFiles();
                    if (subFiles != null && subFiles.length > 0) {
                        TreeItem<FileViewElement> temp = getItemTreeFolder(file);
                        result.getChildren().add(temp);
                    } else {
                        if (file.getName().endsWith(".cpp"))
                            result.getChildren().add(new TreeItem<>(new FileViewElement(file), new ImageView(url_cplus.toExternalForm())));
                        else result.getChildren().add(new TreeItem<>(new FileViewElement(file), new ImageView(url_file.toExternalForm())));
                    }
                }
            }
        } 
        return result;
    }
    
    private List<File> getAllFiles(File folder) {
        ArrayList<File> result = new ArrayList<File>();
        File[] files = folder.listFiles();
        if (files.length > 0) {
            for (File file:files) {
                if (!file.getName().endsWith(".exe")) {
                    File[] subFiles = file.listFiles();
                    if (subFiles != null && subFiles.length > 0) {
                        List<File> temp = getAllFiles(file);
                        result.addAll(temp);
                    } else {
                        result.add(file);
                    }
                }
            }
        } 
        return result;
    }
    
    private void closeFile() {
        String temp = loadedFileReference.getName();
        loadedFileReference = null;
        lastModifiedTime = null;
        codeArea.clear();
        statusMessage.setText("File closed: " + temp);
    }
    
    private void setRootTreeView(File selectedFolder) {
        File[] files = selectedFolder.listFiles();
        URL url = PrimaryController.class.getResource("folder_icon.png");
        URL url_cplus = PrimaryController.class.getResource("cplusplus_icon.png");
        URL url_file = PrimaryController.class.getResource("file_icon.png");
        TreeItem<FileViewElement> rootItem;

        if (files != null) {
            rootItem = new TreeItem<>(new FileViewElement(selectedFolder), new ImageView(new Image(url.toExternalForm())));
            for (File file : files) {
                if (file != null && !file.getName().endsWith(".exe")) {
                    if (file.isDirectory()) {
                        TreeItem itemFolder = getItemTreeFolder(file);
                        rootItem.getChildren().add(itemFolder);
                    }
                    else {
                        if (file.getName().endsWith(".cpp"))
                            rootItem.getChildren().add(new TreeItem<>(new FileViewElement(file), new ImageView(new Image(url_cplus.toExternalForm()))));
                        else rootItem.getChildren().add(new TreeItem<>(new FileViewElement(file), new ImageView(url_file.toExternalForm())));
                    }
                }
            }
        } else {
            
            if (selectedFolder.getName().endsWith(".cpp"))
                            rootItem = new TreeItem<>(new FileViewElement(selectedFolder), new ImageView(url_cplus.toExternalForm()));
                        else rootItem = new TreeItem<>(new FileViewElement(selectedFolder));
        }
        
        currentRootFolder = selectedFolder;
        try {
            this.lastModifiedTime = Files.readAttributes(selectedFolder.toPath(), BasicFileAttributes.class).lastModifiedTime();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        folderTreeView.setRoot(rootItem);
        rootItem.setExpanded(true);
    }
    
    private void openNewTab(String tabName, File file) {
        // TODO remove after test
        CodeArea newCodeArea = createCodeArea(file);
        
        Tab newTab = new Tab(tabName, newCodeArea);
        tabCodeContainer.getTabs().add(newTab);
        newTab.setOnSelectionChanged (e -> 
        {
            if (newTab.isSelected()) {
                for(TabCodeInfo info : listTabInfo) {
                    if (newTab.equals(info.getTab())) {
                        changeTab(info);
                    }
                }
            }
        }
        );
        
        TabCodeInfo info = new TabCodeInfo(newTab, file, newCodeArea);
        listTabInfo.add(info);
                
        this.codeArea = newCodeArea;
        this.currentTab = info;
        tabCodeContainer.getSelectionModel().select(newTab);
    }
    
    private CodeArea createCodeArea(File file) {
        CodeArea codeArea = new CodeArea();
        
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setContextMenu( new ContextMenu() );
        
        if (file.getName().endsWith(".cpp")) {
            Subscription sub = codeArea.multiPlainChanges()
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
        
        return codeArea;
    }
    
    private void changeTab(TabCodeInfo info) {
        tabCodeContainer.getSelectionModel().select(info.getTab());
        this.codeArea = info.getCodeArea();
        this.loadedFileReference = info.getFile();
        this.currentTab = info;
        try {
            this.lastModifiedTime = Files.readAttributes(info.getFile().toPath(), BasicFileAttributes.class).lastModifiedTime();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private ScheduledService<Boolean> scheduleFolderChecking(File file) {
        ScheduledService<Boolean> fileChangeCheckingService = createFolderChangesCheckingService(file);
        fileChangeCheckingService.setOnSucceeded(workerStateEvent -> {
            boolean isUpdated = false;
            if (listTabInfo.isEmpty()) isUpdated = true;
            if (fileChangeCheckingService.getLastValue() == null) return;
            if (fileChangeCheckingService.getLastValue()) {
                //no need to keep checking
//                fileChangeCheckingService.cancel();
                List<File> files = getAllFiles(file);
                for (TabCodeInfo info : listTabInfo) {
                    boolean isFound = false;
                    for (File f : files) {
                        if (info.getFile().getAbsolutePath().equals(f.getAbsolutePath())) {
                            isFound = true;
                        }
                    }
                    if (!isFound) {
                        listTabInfo.remove(info);
                        tabCodeContainer.getTabs().remove(info.getTab());
                        changeTab(info);
                        isUpdated=true;
                    }
                }
                if (isUpdated) {
                    setRootTreeView(file);
                    
                    if (folderCheckingService != null) {
                        folderCheckingService.cancel();
                        folderCheckingService = null;
                    }
            
                    folderCheckingService = scheduleFolderChecking(file);
                }
            }
        });
        System.out.println("Starting Checking Service...");
        fileChangeCheckingService.start();
        return fileChangeCheckingService;
    }
        
    private ScheduledService<Boolean> createFolderChangesCheckingService(File file) {
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
    
}
