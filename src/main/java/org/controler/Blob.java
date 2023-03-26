package org.controler;

import org.model.FileStructure;
import org.model.FolderCrawler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class Blob {

    private Stream<byte[]>uploadableStream=Stream.empty();

    public void createUploadStream(String base_path) {
        FolderCrawler folderCrawler = new FolderCrawler(base_path);
        Stream<Path> paths = folderCrawler.createPathStream();

        paths.forEach(
                node -> {
                    if (node.toFile().isFile()) {
                        FileStructure file= null;
                        try {
                            file = new FileStructure(node);
                            addToStream(file);
                        } catch (IOException e) {
                            //throw new RuntimeException(e);
                            System.err.println(e.toString());
                        }
                    } else {
                        System.out.println(node + " is directory");
                    }
                });

    }

    public void addToStream(FileStructure file){
        addToStream(file.getByteArray());
    }
    public void addToStream(byte[] sequence){
        byte[] size = ByteBuffer.allocate(4).putInt(sequence.length).array();
        Stream<byte []> tempStream = Stream.concat(Stream.of(size), Stream.of(sequence));
        this.uploadableStream = Stream.concat(this.uploadableStream, tempStream);
    }

    public Stream<byte[]> getUploadableStream() {
        return uploadableStream;
    }
}
