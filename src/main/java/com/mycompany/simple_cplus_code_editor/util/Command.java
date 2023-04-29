/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.*;
/**
 *
 * @author lesan
 */
public class Command {
    
    public String runCommand(String... command) throws InterruptedException, IOException {
    
        var processBuilder = new ProcessBuilder().command(command); 
             
            Process process = processBuilder.start();
 
            //read the output
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
            
            StringBuilder stringb;
        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            String output = null;
            stringb = new StringBuilder();
            bufferedWriter.write("2" + "\n");
            bufferedWriter.flush();
            while ((output = bufferedReader.readLine()) != null) {
                System.out.println(output);
                stringb.append(output);
                stringb.append("\n");
            }
            //wait for the process to complete
            process.waitFor();
            //close the resources
        }
            process.destroy();
            return stringb.toString();
    }
    
}
