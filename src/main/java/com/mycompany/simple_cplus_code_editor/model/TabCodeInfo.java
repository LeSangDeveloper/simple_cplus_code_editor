/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.model;

import java.io.File;
import javafx.scene.control.Tab;
import org.fxmisc.richtext.CodeArea;

/**
 *
 * @author lesan
 */
public class TabCodeInfo {
 
    private final Tab tab;
    
    private final File file;
    
    private final CodeArea codeArea;
    
    public TabCodeInfo(Tab tab, File file, CodeArea codeArea) {
        this.tab = tab;
        this.file = file;
        this.codeArea = codeArea;
    }

    public Tab getTab() {
        return tab;
    }

    public File getFile() {
        return file;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }
    
    
    
}
