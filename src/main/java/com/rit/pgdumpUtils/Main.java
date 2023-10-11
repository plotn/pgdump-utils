package com.rit.pgdumpUtils;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] executeArguments) throws IOException {
        String outFilePath = "";
        String inFilePath = "";
        boolean withPartitions = false;
        String pathToFile = "";
        String pathToPgFormatter = "";
        String fileName = "";
        String substitutes = "";
        String excludes = "";
        boolean executeParse = false;
        boolean executeBuild = false;

        if (executeArguments.length==0) {
            System.out.println("Parameters are:");
            System.out.println(" -TP/-TB: parse export file or build file for import");
            System.out.println("Parse mode:");
            System.out.println(" -F<filename>: filename (required)");
            System.out.println(" -T: tables with partitions (default is false)");
            System.out.println(" -P<path>: path to file (optional)");
            System.out.println(" -O<path>: output path (optional)");
            System.out.println("Build mode:");
            System.out.println(" -O<path>: output path");
            System.out.println(" -I<path>: input path");
            System.out.println(" -S: substitutes, comma separated (a=b,c=d)");
            System.out.println(" -E{v1,v2,...}: Exclude some commands, supported now:");
            System.out.println("     SEQ_COL_OWN = exclude ALTER SEQUENCE [seq_schema].[seq_name] OWNED BY [schema].[table_name].[column_name]");
            System.out.println("     ALTER_OWN = exclude ALTER ... OWNER TO ...");
            System.out.println(" -M<path_to_pg_formatter>: path to pgFormatter (optionally) - use pgFormatter if specified");
            return;
        }
        for (String currentArgument: executeArguments) {
            if (currentArgument.toUpperCase().startsWith("-I")) {
                inFilePath = currentArgument.substring(2);
                if (!inFilePath.endsWith("\\")) {
                    inFilePath = inFilePath + "/";
                }
            } else if (currentArgument.toUpperCase().startsWith("-O")) {
                outFilePath = currentArgument.substring(2);
            } else if (currentArgument.equalsIgnoreCase("-T")) {
                withPartitions = true;
            } else if (currentArgument.equalsIgnoreCase("-TP")) {
                executeParse = true;
            } else if (currentArgument.equalsIgnoreCase("-TB")) {
                executeBuild = true;
            } else if (currentArgument.toUpperCase().startsWith("-P")) {
                pathToFile = currentArgument.substring(2);
                if ((!pathToFile.endsWith("/"))&&(!pathToFile.endsWith("\\"))) {
                    pathToFile = pathToFile + "/";
                }
            } else if (currentArgument.toUpperCase().startsWith("-M")) {
                pathToPgFormatter = currentArgument.substring(2);
            } else if (currentArgument.toUpperCase().startsWith("-F")) {
                fileName = currentArgument.substring(2);
            } else if (currentArgument.toUpperCase().startsWith("-S")) {
                substitutes = currentArgument.substring(2);
            } else if (currentArgument.toUpperCase().startsWith("-E")) {
                excludes = currentArgument.substring(2);
            } else {
                System.out.println("Unknown option " + currentArgument + ". Exit.");
                return;
            }
        }
        if (!executeParse && !executeBuild) {
            System.out.println("Nothing to do (not parse not build). Exit.");
            return;
        }
        if (executeBuild) {
            if (inFilePath.isEmpty()) {
                System.out.println("Input is empty. Exit.");
                return;
            }
            File inFile = new File(inFilePath);
            if (!inFile.exists()) {
                System.out.println("Input file (-F<file_name>) did not specified. Or file does not exists. Exit.");
                return;
            }
            if (!inFile.isDirectory()) {
                System.out.println("Input must be a directory. Exit.");
                return;
            }
            if (outFilePath.isEmpty()) {
                System.out.println("Output is empty!");
                return;
            }

            ImportScriptBuilder importScriptBuilder = new ImportScriptBuilder(inFilePath);
            importScriptBuilder.build(outFilePath);


            System.out.println("************************************\nBuild import scripts finished");
        } else {
            if (fileName.equals("")) {
                System.err.println("Input file (-F<file_name>) did not specified. Exit.");
                System.exit(1);
            }
            if (outFilePath.equals("")) {
                outFilePath = pathToFile;
            }

            File file = new File(outFilePath);
            if (!file.exists()) {
                file.mkdirs();
                file = new File(outFilePath);
            }

            ExportScriptParser parser = new ExportScriptParser(pathToFile, fileName, outFilePath, withPartitions, substitutes, excludes, pathToPgFormatter);
            parser.parseExportFile();

            FileSorter fileSorter = new FileSorter(outFilePath);
            fileSorter.sortAllFiles();
        }
    }

}
