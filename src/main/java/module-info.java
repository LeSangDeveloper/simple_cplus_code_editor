module com.mycompany.simple_cplus_code_editor {    
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires java.base;
    
    opens com.mycompany.simple_cplus_code_editor to javafx.fxml;
    opens com.mycompany.simple_cplus_code_editor.controller to javafx.fxml;
    exports com.mycompany.simple_cplus_code_editor;
}
