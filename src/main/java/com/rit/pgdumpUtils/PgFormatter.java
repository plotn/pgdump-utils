package com.rit.pgdumpUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PgFormatter {

    public static List<String> getFormatted(String formatterPath, List<String> objText)
            throws IOException, InterruptedException, FormatterException {
        if (objText.size() == 0) return objText;
        if (StrUtils.isEmptyStr(objText.stream().collect(Collectors.joining(" "))))
            throw new FormatterException("Input object is empty");
        Process process = Runtime.getRuntime().exec(formatterPath);
        String line;
        ArrayList<String> res = new ArrayList<>();
        ArrayList<String> err = new ArrayList<>();
        final OutputStream stdin = process.getOutputStream();
        final InputStream stderr = process.getErrorStream();
        final InputStream stdout = process.getInputStream();
        objText.forEach(s -> {
            try {
                System.out.println ("[Stdin] " + s);
                stdin.write((s + System.lineSeparator()).getBytes());
                stdin.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        stdin.close();
        try (BufferedReader brOutput =
                     new BufferedReader(new InputStreamReader(stdout))) {
            while ((line = brOutput.readLine()) != null) {
                res.add(line);
                //System.out.println ("[Stdout] " + line);
            }
        }
        try (BufferedReader brOutput =
                     new BufferedReader(new InputStreamReader(stderr))) {
            while ((line = brOutput.readLine()) != null) {
                err.add(line);
                System.out.println ("[Sterr] " + line);
            }
        }
        process.waitFor();
        process.destroy();
        String firstLine = "";
        if (objText.size() > 0)
            firstLine = objText.get(0);
        if (!StrUtils.isEmptyStr(err.stream().collect(Collectors.joining(" "))))
            throw new FormatterException("Formatter exception (" + firstLine + "): " +
                    err.stream().collect(Collectors.joining("\n ")));
        if (StrUtils.isEmptyStr(res.stream().collect(Collectors.joining(" "))))
            throw new FormatterException("Output object is empty");
        return res;
    }

    public static List<String> getFormatted(String formatterPath, String fileName)
            throws IOException, InterruptedException, FormatterException {
        Process process = Runtime.getRuntime().exec(formatterPath + " " + fileName);
        String line;
        ArrayList<String> res = new ArrayList<>();
        ArrayList<String> err = new ArrayList<>();
        final InputStream stderr = process.getErrorStream();
        final InputStream stdout = process.getInputStream();
        try (BufferedReader brOutput =
                     new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8))) {
            while ((line = brOutput.readLine()) != null) {
                res.add(line);
                System.out.println ("[Stdout] " + line);
            }
        }
        try (BufferedReader brOutput =
                     new BufferedReader(new InputStreamReader(stderr, StandardCharsets.UTF_8))) {
            while ((line = brOutput.readLine()) != null) {
                err.add(line);
                System.out.println ("[Sterr] " + line);
            }
        }
        process.waitFor();
        process.destroy();
        if (!StrUtils.isEmptyStr(err.stream().collect(Collectors.joining(" "))))
            throw new FormatterException("Formatter exception (" + fileName + "): " +
                    err.stream().collect(Collectors.joining("\n ")));
        if (StrUtils.isEmptyStr(res.stream().collect(Collectors.joining(" "))))
            throw new FormatterException("Output object is empty");
        return res;
    }

}
