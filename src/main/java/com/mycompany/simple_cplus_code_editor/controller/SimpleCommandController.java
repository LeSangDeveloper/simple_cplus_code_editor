/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.controller;

import com.mycompany.simple_cplus_code_editor.util.Command;
import java.io.IOException;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.InlineCssTextArea;

/**
 *
 * @author lesan
 */
public class SimpleCommandController {
    
    private final TextFlow outputConsole;
    private final Command cmd;
    
    public SimpleCommandController() {
        this.outputConsole = new TextFlow();
        this.outputConsole.setStyle("-fx-background-color: white");
        this.outputConsole.setPrefSize(400, 200);
        this.cmd = new Command();
    }
    
    public SimpleCommandController(TextFlow outputConsole) {
        this.outputConsole = outputConsole;
        this.cmd = new Command();
    }
    
    public void compileProgram(String inputLocation, String outputLocation) {
        Text prepare = new Text(String.format("Compiling %s....", inputLocation));
        outputConsole.getChildren().add(prepare);
        try {
            String result1 = cmd.runCommand("g++1.exe");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Text failedCompiled = new Text("Failed! Please install MINGW or G++ compiler and add to PATH environment!!!\n\n");
            failedCompiled.setStroke(Paint.valueOf("red"));
            outputConsole.getChildren().add(failedCompiled);
            return;
        }

        String result = null;
        try {
            result = cmd.runCommand("g++.exe", inputLocation, "-o", outputLocation);
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
    
    public void runProgram(String fileLocation) {
        String result = null;
        Text prepare = new Text(String.format("Running %s....\n\n", fileLocation));
        outputConsole.getChildren().add(prepare);
        try {
            result = cmd.runCommand(fileLocation);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Text resultFinal = new Text(result);
        outputConsole.getChildren().add(resultFinal);
    }

    public TextFlow  getOutputConsole() {
        return outputConsole;
    }
    
}
