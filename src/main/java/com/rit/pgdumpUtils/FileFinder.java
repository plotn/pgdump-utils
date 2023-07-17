package com.rit.pgdumpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FileFinder {
    File rootDirectory;
    ArrayList<File> schemaDirectories = new ArrayList<>();

    public FileFinder(String filePath) {
        rootDirectory = new File(filePath);
        System.out.println(rootDirectory.getName() + "   " + rootDirectory.exists() + "  " + rootDirectory.isDirectory());
        schemaDirectories.addAll(
                Stream.of(rootDirectory.listFiles())
                    .filter(file -> file.isDirectory())
                    .collect(Collectors.toList()));
    }

    public Iterator<File> getSchemas() {
        return schemaDirectories.iterator();
    }

    private boolean isFileSQL(File file) {
        String fileName = file.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return !file.isDirectory() && (fileExtension.equals("sql") || fileExtension.equals("fnc") || fileExtension.equals("prc") || fileExtension.equals("vw"));
    }

    private File[] getSqlFiles(File schemaDirectory, String fileType) throws NullPointerException {
        System.out.print("getSqlFiles for " + schemaDirectory.getAbsolutePath() + "   ");

        Optional<File> currentDirectory = Stream.of(schemaDirectory.listFiles())
                .filter(file -> file.isDirectory() && file.getName().equals(fileType))
                .findAny();
        if (!currentDirectory.isPresent()) {
            System.out.print(fileType + "  not found   ");
        } else {
            System.out.print(currentDirectory.get().getName() + "  ");
        }

        if (!currentDirectory.isPresent()) {
            System.out.println("0");
            return new File[0];
        }

        Object[] result = Stream.of(currentDirectory.get().listFiles())
                .filter(file -> !file.isDirectory() && isFileSQL(file))
                .toArray();

        if (result == null || result.length == 0) {
            System.out.println("0");
            return new File[0];
        }

        File[] outArray = new File[result.length];
        for (int i = 0; i < result.length; i++) {
            outArray[i] = (File)result[i];
        }
        System.out.println(outArray.length);
        return outArray;
    }

    public File[] getTables(File schemaDirectory) throws NullPointerException {
        return getSqlFiles(schemaDirectory, "tables");
    }

    public File[] getFunctions(File schemaDirectory) throws NullPointerException {
        return getSqlFiles(schemaDirectory, "functions");
    }

    public File[] getProcedures(File schemaDirectory) throws NullPointerException {
        return getSqlFiles(schemaDirectory, "procedures");
    }

    public File[] getSequences(File schemaDirectory) throws NullPointerException {
        return getSqlFiles(schemaDirectory, "sequences");
    }

    public File[] getTypes(File schemaDirectory) throws NullPointerException {
        return getSqlFiles(schemaDirectory, "types");
    }

    public File[] getViews(File schemaDirectory) throws NullPointerException {
        return getSqlFiles(schemaDirectory, "views");
    }

    public File[] getCode(File schemaDirectory) throws NullPointerException {
        ArrayList<File> result = new ArrayList<>();
        result.addAll(Arrays.asList(getSqlFiles(schemaDirectory, "functions")));
        result.addAll(Arrays.asList(getSqlFiles(schemaDirectory, "procedures")));
        return result.toArray(new File[0]);
    }
}
