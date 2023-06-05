/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.simple_cplus_code_editor.util;

import java.util.regex.Pattern;

/**
 *
 * @author lesan
 */
public class Utils {
    
    public static final String[] KEYWORDS = new String[] {
            "align as", "align of", "and", "and_eq", "asm",
            "atomic_cancel", "atomic_commit", "atomic_noexcept", "auto", "bitand",
            "bitor", "bool", "break", "case", "catch",
            "char", "char8_t", "char16_t", "char32_t", "class",
            "compl", "concept", "const", "consteval", "constexpr",
            "constinit", "const_cast", "continue", "co_await", "co_return",
            "co_yield", "decltype", "default", "delete", "do",
            "double", "dynamic_cast", "else", "enum", "explicit",
            "export", "extern", "false", "float", "for",
            "friend", "goto", "if", "inline", "int", "long", "mutable", "namespace",
            "new", "noexcept", "not", "not_eq", "nullptr", "operator", "or", "or_eq",
            "private", "protected", "public", "reflexpr", "register", "reinterpret_cast",
            "requires", "return", "short", "signed", "sizeof", "static", "static_assert",
            "struct", "switch", "synchronized", "template", "this", "thread_local", "throw", "true",
            "try", "typedef", "typeid", "typename", "union", "unsigned", "using", "virtual", "void", "volatile",
            "wchar_t", "while", "xor", "xor_eq"
    };

    public static final String[] PREPROCESSING_WORDS = new String[] {
            "#if", "#elif", "#else", "#endif", "#ifdef", "#ifndef", "#elifdef", "#elifndef",
            "#define", "#undef", "#include", "#line", "#error", "#warning", "#pragma", "#defined"
    };
    
    public static final String PREPROCESSING_WORDS_PATTERN = "(" + String.join("|", PREPROCESSING_WORDS) + ")";
    public static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    public static final String PAREN_PATTERN = "\\(|\\)";
    public static final String BRACE_PATTERN = "\\{|\\}";
    public static final String BRACKET_PATTERN = "\\[|\\]";
    public static final String SEMICOLON_PATTERN = "\\;";
    public static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    public static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    public static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PREPROCESSINGWORD>" + PREPROCESSING_WORDS_PATTERN + ")"      
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public static final String sampleCode = String.join("\n", new String[] {
            "#include<iostream>",
            "",
            "using namespace std;",
            "",
            "int main() {",
            "       cout << \"Hello word\";",
            "       return 1",
            "    }",
            "",
            "}"
    });
    
}
