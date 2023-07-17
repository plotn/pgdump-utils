package com.rit.pgdumpUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSorter {

    private FileFinder finder;
    private Iterator<File> schemas;

    public FileSorter(String filePath) {
        finder = new FileFinder(filePath);
    }

    public void sortTable(File f) throws IOException {
        BufferedReader inputFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        ArrayList<String> arrObj = new ArrayList<>();
        TreeMap<String, String> mapFK = new TreeMap<>();
        for(String line; (line = inputFileReader.readLine()) != null; ) arrObj.add(line);
        arrObj.add("");
        // извлекаем все констрейнты
        int posFK = 999;
        while (posFK >= 0) {
            posFK = -1;
            for (int i = 0; i < arrObj.size(); i++) {
                String line = arrObj.get(i);
                if (i > 1) {
                    String prev2 = arrObj.get(i - 2);
                    String prev1 = arrObj.get(i - 1);
                    if (
                        (prev2.trim().startsWith("ALTER TABLE")) &&
                        (prev1.trim().startsWith("ADD CONSTRAINT")) &&
                        (line.trim().equals(""))
                        ) {
                        posFK = i - 2;
                        break;
                    }
                }
            }
            if (posFK >= 0) {
                mapFK.put(arrObj.get(posFK + 1), arrObj.get(posFK));
                //System.out.println(arrObj.get(posFK + 1) + "~" + arrObj.get(posFK));
                arrObj.remove(posFK);
                arrObj.remove(posFK);
                arrObj.remove(posFK);
            }
        }
        if (arrObj.size() > 0)
            if (arrObj.get(arrObj.size() - 1).equals(""))
                arrObj.remove(arrObj.size() - 1);
        // Поищем конструкцию ALTER TABLE [ONLY] schema.table_name ALTER COLUMN [column_name] SET DEFAULT nextval('[sequence_name]'::regclass);
        int seqColInd = -1;
        for (int i = 0; i < arrObj.size(); i++) {
            String line = arrObj.get(i);
            if (line.trim().startsWith("ALTER TABLE") && line.trim().contains("ALTER COLUMN")
                    && line.trim().contains("SET DEFAULT nextval")) {
                seqColInd = i;
            }
        }
        // Теперь найдем колонку, которой мы это вставим
        if (seqColInd >= 0) {
            String seqColStmt = arrObj.get(seqColInd);
            String seqCol = "";
            Pattern pattern = Pattern.compile(".*(ALTER COLUMN .* SET).*");
            Matcher matcher = pattern.matcher(seqColStmt);
            if (matcher.find()) {
                seqCol = matcher.group(1).replace("ALTER COLUMN ", "");
                seqCol = seqCol.substring(0, seqCol.length()-3).trim();
                System.out.println("seqCol " + seqCol);
            }
            String seqName = seqColStmt.substring(seqColStmt.indexOf("SET DEFAULT") + 4);
            if (seqName.endsWith(";")) seqName = seqName.substring(0, seqName.length() - 1);
            System.out.println("seqName " + seqName);
            if (!seqCol.equals("")) // на всякий случай
                for (int i = 0; i < arrObj.size(); i++) {
                    String line = arrObj.get(i);
                    String replLine = "";
                    if (line.trim().startsWith(seqCol + " ") && (!line.trim().contains("DEFAULT"))) {
                        if (line.trim().endsWith("NOT NULL,"))
                            replLine = line.replace("NOT NULL,", seqName + " NOT NULL,");
                        else
                            if (line.trim().endsWith("NOT NULL"))
                                replLine = line.replace("NOT NULL,", seqName + " NOT NULL");
                            else
                                if (line.trim().endsWith(","))
                                    replLine = line.substring(0, line.length()-1) + " " + seqName;
                                else
                                    replLine = line + " " + seqName;
                    }
                    if (!replLine.equals("")) { // Запишем все окончательно
                        arrObj.set(i, replLine);
                        arrObj.remove(seqColInd);
                        if (arrObj.size() > seqColInd) // И уберем еще пустую строку
                            if (arrObj.get(seqColInd).trim().equals(""))
                                arrObj.remove(seqColInd);
                        break;
                    }
                }
        }
        // Запишем все назад
        for(Map.Entry<String, String> entry : mapFK.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            arrObj.add("");
            arrObj.add(value);
            arrObj.add(key);
        }
        // Уберем двойные пустые строки
        int pos2Lines = 999;
        while (pos2Lines >= 0) {
            pos2Lines = -1;
            for (int i = 0; i < arrObj.size(); i++) {
                String line = arrObj.get(i);
                if (i > 0) {
                    String prev1 = arrObj.get(i - 1);
                    if (
                            (prev1.trim().equals("")) &&
                                    (line.trim().equals(""))
                    ) {
                        pos2Lines = i - 1;
                        break;
                    }
                }
            }
            if (pos2Lines >= 0) {
                arrObj.remove(pos2Lines);
            }
        }
        // И в файл
        FileOutputStream outFile = new FileOutputStream(f);
        BufferedWriter writer =	new BufferedWriter(new OutputStreamWriter(outFile, StandardCharsets.UTF_8));
        for (String str : arrObj) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();
    }

    public void sortFunction(File f) throws IOException {
        BufferedReader inputFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        ArrayList<String> arrObj = new ArrayList<>();
        TreeMap<String, String> mapFK = new TreeMap<>();
        for(String line; (line = inputFileReader.readLine()) != null; ) arrObj.add(line.replaceAll("\\s+$", ""));
        // Поищем $$; перед которыми пробел
        for (int i = 1; i < arrObj.size(); i++) {
            String prev = arrObj.get(i-1).trim();
            String line = arrObj.get(i).trim();
            if (("".equals(prev)) && ("$$;".equals(line))) {
              arrObj.remove(i-1);
              break;
            }
        }
        // И в файл
        FileOutputStream outFile = new FileOutputStream(f);
        BufferedWriter writer =	new BufferedWriter(new OutputStreamWriter(outFile, StandardCharsets.UTF_8));
        for (String str : arrObj) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();
    }

    public void sortAllFiles() throws IOException {
        schemas = finder.getSchemas();
        while (schemas.hasNext()) {
            File schema = schemas.next();
            File[] files = finder.getTables(schema);
            for (File f: files) {
                sortTable(f);
            }
            files = finder.getFunctions(schema);
            for (File f: files) {
                sortFunction(f);
            }
            files = finder.getProcedures(schema);
            for (File f: files) {
                sortFunction(f);
            }
        }
    }

}
