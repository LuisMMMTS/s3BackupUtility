package org.controler;

import org.model.FolderCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class Blob {

    public void createBlob(String base_path){
        FolderCrawler folderCrawler = new FolderCrawler(base_path);
        Stream<Path> paths = folderCrawler.createPathStream();

        paths.forEach(x->{if (x.toFile().isFile()) {
            File file = x.toFile();
            try {
                FileInputStream blob=new FileInputStream(file);
                byte[] file_contents = blob.readAllBytes();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            System.out.println(x + " is directory");
        }
        });
        /*paths.forEach(path->{

        });*/


        //paths
        //        .filter(Files::isRegularFile)
        //        .forEach(System.out::println);

    }
}
