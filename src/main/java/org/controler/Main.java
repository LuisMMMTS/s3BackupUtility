package org.controler;

import org.model.FolderCrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        Blob blob = new Blob();
        blob.createUploadStream("./");
        File nf= new File("output.txt");
        FileOutputStream os= new FileOutputStream(nf);
        blob.getUploadableStream().forEachOrdered(b -> {
            try {
                os.write(b);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        os.close();
    }
}

//TODO:authenticate in S3
//TODO:Check if bucket already exists and create or reuse it
//TODO: ask for blob name and if repeated ask if want to replace
//todo:skips files and dirs