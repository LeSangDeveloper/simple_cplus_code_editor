/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String output = null;
            StringBuilder stringb = new StringBuilder();
            while ((output = bufferedReader.readLine()) != null) {
                System.out.println(output);
                stringb.append(output);
                stringb.append("\n");
            }

            //wait for the process to complete
            process.waitFor();

            //close the resources
            bufferedReader.close();
            process.destroy();
            return stringb.toString();
    }
    
}
