package org.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FolderCrawler {
    public Stream<Path> createPathStream(String base_path){
        //From Java docs
        try {
            return Files.walk(Paths.get(base_path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
