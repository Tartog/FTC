package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private String[] fileNames;
    private String outputPath = "";
    private String prefix = "";
    private boolean appendMode = false;
    private boolean statistic = false;

    private long integerCount = 0;
    private long floatCount = 0;
    private long stringCount = 0;

    private Long minInteger = null;
    private Long maxInteger = null;
    private double sumIntegers = 0;
    private double averageIntegers = 0;

    private Double minFloat = null;
    private Double maxFloat = null;
    private double sumFloats = 0;
    private double averageFloats = 0;

    private int minStringLength = Integer.MAX_VALUE;
    private int maxStringLength = 0;

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setAppendMode(boolean appendMode) {
        this.appendMode = appendMode;
    }

    public FileHandler(String[] args){
        parseArguments(args);
    }

    private void parseArguments(String[] args) {
        List<String> fileNamesList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-a")) {
                appendMode = true;
            } else if (arg.equals("-p") && i + 1 < args.length) {
                setPrefix(args[++i]);
            } else if (arg.equals("-o") && i + 1 < args.length) {
                setOutputPath(args[++i]);
            } else if (arg.equals("-f") && i + 1 < args.length) {
                statistic = true;
            } else if (arg.equals("-s") && i + 1 < args.length) {
                statistic = false;
            } else {
                fileNamesList.add(arg);
            }
        }
        fileNames = fileNamesList.toArray(new String[0]);
    }

    public void processFiles() {
        if (fileNames == null || fileNames.length == 0) {
            System.err.println("Нет файлов для обработки.");
            return;
        }

        List<List<String>> allLines = new ArrayList<>();
        for (String fileName : fileNames) {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при чтении файла: " + fileName);
                e.printStackTrace();
            }
            allLines.add(lines);
        }

        List<String> integers = new ArrayList<>();
        List<String> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        int maxLines = allLines.stream().mapToInt(List::size).max().orElse(0);
        for (int i = 0; i < maxLines; i++) {
            for (List<String> lines : allLines) {
                if (i < lines.size()) {
                    classifyLine(lines.get(i), integers, floats, strings);
                }
            }
        }

        writeToFile("integers.txt", integers);
        writeToFile("floats.txt", floats);
        writeToFile("strings.txt", strings);

        printStatistics(statistic);
    }

    private void classifyLine(String line, List<String> integers, List<String> floats, List<String> strings) {
        if (line.matches("-?\\d+")) {
            integers.add(line);
            integerCount++;
            long value = Long.parseLong(line);
            sumIntegers += value;
            if (minInteger == null || value < minInteger) {
                minInteger = value;
            }
            if (maxInteger == null || value > maxInteger) {
                maxInteger = value;
            }
        } else if (line.matches("-?\\d+\\.\\d+") || line.matches("-?\\d+\\.\\d+[eE][-+]?\\d+")) {
            floats.add(line);
            floatCount++;
            double value = Double.parseDouble(line);
            sumFloats += value;
            if (minFloat == null || value < minFloat) {
                minFloat = value;
            }
            if (maxFloat == null || value > maxFloat) {
                maxFloat = value;
            }
        } else {
            strings.add(line);
            stringCount++;
            int length = line.length();
            if (length < minStringLength) {
                minStringLength = length;
            }
            if (length > maxStringLength) {
                maxStringLength = length;
            }
        }
    }

    private void printStatistics(boolean fullStatistics) {
        System.out.println("Статистика по целым числам:");
        System.out.println("Количество: " + integerCount);
        if (fullStatistics) {
            System.out.println("Минимум: " + minInteger);
            System.out.println("Максимум: " + maxInteger);
            averageIntegers = integerCount > 0 ? sumIntegers / integerCount : 0;
            System.out.println("Сумма: " + sumIntegers);
            System.out.println("Среднее: " + averageIntegers);
        }

        System.out.println("\nСтатистика по дробным числам:");
        System.out.println("Количество: " + floatCount);
        if (fullStatistics) {
            System.out.println("Минимум: " + minFloat);
            System.out.println("Максимум: " + maxFloat);
            double averageFloats = floatCount > 0 ? sumFloats / floatCount : 0;
            System.out.println("Сумма: " + sumFloats);
            System.out.println("Среднее: " + averageFloats);
        }

        System.out.println("\nСтатистика по строкам:");
        System.out.println("Количество: " + stringCount);
        if (fullStatistics) {
            System.out.println("Минимальная длина: " + minStringLength);
            System.out.println("Максимальная длина: " + maxStringLength);
        }
    }

    private void writeToFile(String fileName, List<String> data) {
        if (!data.isEmpty()) {
            String fullPath;
            if (outputPath.isEmpty() || !outputPath.matches("^[A-Za-z]:.*")) {
                String currentDir = System.getProperty("user.dir");
                fullPath = currentDir + File.separator + outputPath + prefix + fileName;
            } else {
                fullPath = outputPath + File.separator + prefix + fileName;
            }
            File outputFile = new File(fullPath);
            outputFile.getParentFile().mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, appendMode))) {
                for (String line : data) {
                    bw.write(line);
                    bw.newLine();
                }
            } catch (IOException e) {
                System.err.println("Ошибка при записи в файл: " + fullPath);
                e.printStackTrace();
            }
        }
    }
}
