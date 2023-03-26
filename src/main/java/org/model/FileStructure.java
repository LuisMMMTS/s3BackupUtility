package org.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;


public class FileStructure implements Serializable {
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, Object> getFileAtributes() {
        return fileAtributes;
    }

    public void setFileAtributes(Map<String, Object> fileAtributes) {
        this.fileAtributes = fileAtributes;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    String filePath;
    Map<String, Object> fileAtributes;
    byte[] byteArray;

    public FileStructure(Path file) throws IOException {
        this.filePath=file.toString();
        this.fileAtributes= Files.readAttributes(file,"*");

        try (FileInputStream fileReader = new FileInputStream(file.toString())){
            this.byteArray=fileReader.readAllBytes();
         }
    }


}
