package com.rit.pgdumpUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class ImportScriptBuilder {
    private FileFinder finder;
    private Iterator<File> schemas;

    public ImportScriptBuilder(String filePath) {
        finder = new FileFinder(filePath);
    }

    private void unParseSchema(String outFileName, File schema) {
        ArrayList<String> foreignKeys = new ArrayList<>();
        ArrayList<String> triggers = new ArrayList<>();
        unParseTables(outFileName + "Tables.sql", finder, schema, foreignKeys, triggers);
        unParseForeignKeys(outFileName + "ForeignKeys.sql", foreignKeys, schema);
        unParseTriggers(outFileName + "Triggers.sql", triggers, schema);
        unParseSequences(outFileName + "Sequences.sql", outFileName + "Tables.sql", finder, schema);
        unParseTypes(outFileName + "Types.sql", finder, schema);
        unParseViews(outFileName + "Views.sql", finder, schema);
        unParseCode(outFileName + "Procs.sql", finder, schema);
    }

    public void build(String outFileName) {
        /* в схеме partn хранятся все партиции из других схем, поэтому ее таблицы нужно ставить в первую очередь*/
        schemas = finder.getSchemas();
        while (schemas.hasNext()) {
            File schema = schemas.next();
            if (schema.getName().equalsIgnoreCase("PARTN")) {
                unParseSchema(outFileName, schema);
            }
        }
        /* теперь разбираем остальные схемы*/
        schemas = finder.getSchemas();
        while (schemas.hasNext()) {
            File schema = schemas.next();
            if (!schema.getName().equalsIgnoreCase("PARTN")) {
                unParseSchema(outFileName, schema);
            }
        }
    }

    private void unParseCode(String outFileName, FileFinder finder, File schema) {
        try {
            File[] files = finder.getCode(schema);
            System.out.println("found " + files.length + " files");
            File outFile = new File(outFileName);

            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    String buffer;
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");

                    for (File file : files) {
                        writer.write("\n\n-- FUNCTION/PROCEDURE " + file.getName().replace(".sql", "") + "\n");

                        FileInputStream inFileStream = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inFileStream, StandardCharsets.UTF_8));

                        while ((buffer = reader.readLine()) != null) {
                            writer.write(buffer + "\n");
                        }
                        writer.flush();
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseCode  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void unParseViews(String outFileName, FileFinder finder, File schema) {
        try {
            File[] files = finder.getViews(schema);
            File outFile = new File(outFileName);

            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    String buffer;
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");

                    for (File file : files) {
                        writer.write("\n\n-- VIEW " + file.getName().replace(".sql", "") + "\n");

                        FileInputStream inFileStream = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inFileStream, StandardCharsets.UTF_8));

                        while ((buffer = reader.readLine()) != null) {
                            writer.write(buffer + "\n");
                        }
                        writer.flush();
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseViews  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void unParseTypes(String outFileName, FileFinder finder, File schema) {
        try {
            File[] files = finder.getTypes(schema);
            File outFile = new File(outFileName);

            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    String buffer;
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");

                    for (File file : files) {
                        writer.write("\n\n-- TYPE " + file.getName().replace(".sql", "") + "\n");

                        FileInputStream inFileStream = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inFileStream, StandardCharsets.UTF_8));

                        while ((buffer = reader.readLine()) != null) {
                            writer.write(buffer + "\n");
                        }
                        writer.flush();
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseTypes  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void unParseSequences(String outFileName, String outFileNameTables, FileFinder finder, File schema) {
        // Строки вида ALTER SEQUENCE .... OWNED BY {table name}. Это должно быть в самом конце таблиц
        ArrayList<String> sequenceToTable = new ArrayList<>();

        try {
            File[] files = finder.getSequences(schema);
            File outFile = new File(outFileName);

            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    String buffer;
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");

                    for (File file : files) {
                        FileInputStream inFileStream = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inFileStream, StandardCharsets.UTF_8));

                        while ((buffer = reader.readLine()) != null) {
                            if (buffer.startsWith("ALTER SEQUENCE") && buffer.contains("OWNED BY")) {
                                sequenceToTable.add(buffer);
                            } else {
                                writer.write(buffer + "\n");
                            }
                        }
                        writer.flush();
                    }
                }
            }

            try (FileOutputStream outFileStream = new FileOutputStream(outFileNameTables, true)) {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    String buffer;
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");

                    for (String line: sequenceToTable) {
                        writer.write(line + "\n");
                    }
                    writer.flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseSequences  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void unParseForeignKeys(String outFileName, ArrayList<String> foreignKeys, File schema) {
        try {
            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");
                    for (String foreignKey: foreignKeys) {
                        writer.write(foreignKey);
                        writer.write("\n\n");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseForeignKeys  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void unParseTriggers(String outFileName, ArrayList<String> triggers, File schema) {
        try {
            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");
                    for (String foreignKey: triggers) {
                        writer.write(foreignKey);
                        writer.write("\n\n");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseTriggers  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    private void unParseTables(String outFileName, FileFinder finder, File schema,
                                   ArrayList<String> foreignKeys, ArrayList<String> triggers) {
        boolean alterTableFlag = false;
        String preBuffer = "";

        try {
            File[] files = finder.getTables(schema);
            File outFile = new File(outFileName);

            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            try (FileOutputStream outFileStream = new FileOutputStream(outFileName, true)) {
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outFileStream, StandardCharsets.UTF_8))) {
                    String buffer;
                    writer.write("\n\n-- ******************************* SCHEMA " + schema.getName().toUpperCase() + "*********************\n\n");

                    for (File file : files) {
                        writer.write("\n\n-- TABLE " + file.getName().replace(".sql", "") + "\n");

                        FileInputStream inFileStream = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inFileStream, StandardCharsets.UTF_8));

                        while ((buffer = reader.readLine()) != null) {
                            // Появился ALTER TABLE, возможно, что это FOREIGN KEY
                            if (!alterTableFlag && buffer.startsWith("ALTER TABLE")) {
                                alterTableFlag = true;
                                preBuffer = buffer;
                            // Появился FOREIGN KEY, запомним его для вывода в самом конце файла таблиц
                            } else if (alterTableFlag && buffer.contains("FOREIGN KEY")) {
                                foreignKeys.add(preBuffer + " " + buffer);
                                alterTableFlag = false;
                                preBuffer = "";
                            //  Обычный вариант
                            } else {
                                // Не, это не FOREIGN KEY, тогда пишем в этом месте
                                if (alterTableFlag && !preBuffer.equals("")) {
                                    writer.write(preBuffer + "\n");
                                    preBuffer = "";
                                    alterTableFlag = false;
                                }
                                if (buffer.startsWith("CREATE TRIGGER")) { // Тригера выносим после процедур и функций
                                    triggers.add(buffer);
                                } else {
                                    writer.write(buffer + "\n");
                                }
                            }
                        }
                        // Цикл кончился, но в preBuffer могли остаться данные
                        if (!preBuffer.equals("")) {
                            writer.write(preBuffer + "\n");
                            preBuffer = "";
                            alterTableFlag = false;
                        }
                        writer.flush();
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("!!!!!!!!!!!!!  unParseTables  !!!!!!!!!!!!!!!!!!!!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}
