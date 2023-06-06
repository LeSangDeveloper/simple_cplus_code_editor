/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.model;

import java.io.File;

/**
 *
 * @author lesan
 */
public class FileViewElement {
    
    private final File file;
    
    public FileViewElement(File file) {
        this.file = file;
    }
    
    @Override
    public String toString() {
        return file.getName();
    }
    
    
    public File getFile() {
        return file;
    }
    
}
