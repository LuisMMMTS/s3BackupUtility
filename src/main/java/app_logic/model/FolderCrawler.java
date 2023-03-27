package app_logic.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FolderCrawler {
    private String basePath;

    public FolderCrawler(String basePath) {
        this.basePath = basePath;
    }

    public Stream<Path> createPathStream(){
        //From Java docs
        try {
            return Files.walk(Paths.get(this.basePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean folderIsEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }

        return false;
    }
}
