package com.rit.pgdumpUtils;

import java.util.ArrayList;

public class IndexParsedData {
    private String schema;
    private String objectName;
    private ArrayList<String> objectArray;
    public IndexParsedData() {
        this.schema = "";
        this.objectName = "";
        this.objectArray = new ArrayList<>();
    }

    public String getSchema() {
        return schema;
    }

    public String getObjectName() {
        return objectName;
    }

    public ArrayList<String> getObjectArray() {
        return objectArray;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void AddObjectLine(String line) {
        objectArray.add(line);
    }
}
