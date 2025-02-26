package org.example;

public class Main {
    public static void main(String[] args) {
        FileHandler fileHandler = new FileHandler(args);
        fileHandler.processFiles();
    }
}