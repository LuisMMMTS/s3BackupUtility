package app_logic;

import app_logic.model.FolderCrawler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Uploader {
    private final static int CHUNK_SIZE = 5 * 1024 * 1024;

    public static void main(String[] args) throws IOException {
        String bucketName = "jetbrainsexecutormode18h";
        String blob_name = "filename";
        try (FileOutputStream in = new FileOutputStream("test.zip"); ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(in))) {

            FolderCrawler folderCrawler = new FolderCrawler("./");
            Stream<Path> paths = folderCrawler.createPathStream();

            paths.filter(x -> !x.toString().equals("."))
                    .map(x -> Path.of(x.toString()
                            .replace("./", "")))
                    .forEach(node -> {
                        try {
                            byte[] byteArray = new byte[0];
                            boolean isDir = node.toFile().isDirectory();
                            String fileName = node.toString();
                            if (isDir) {
                                if (folderCrawler.folderIsEmpty(node)) {
                                    fileName=node.toString() + "/.keep";
                                }else{
                                    return;
                                }
                            } else {
                                //new FileStructure(node);
                                try (FileInputStream fileReader = new FileInputStream(fileName)) {
                                    byteArray = fileReader.readAllBytes();
                                }
                            }

                            zip.putNextEntry(new ZipEntry(fileName));
                            zip.write(byteArray, 0, byteArray.length);
                            zip.closeEntry();
                            zip.flush();
                            //in.flush();

                        } catch (Exception e) {
                            System.out.println(e);
                        }


                    });
            zip.close();
            in.close();


            Region region = Region.EU_NORTH_1;
            S3Client s3Client = S3Client.builder().region(region).build();
            s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(blob_name).build(), Path.of("./test.zip"));
            File file=new File("./test.zip");
            file.delete();


        }


    }


}



//TODO:authenticate in S3
//TODO:Check if bucket already exists and create or reuse it
//TODO: ask for blob name and if repeated ask if want to replace
//todo:skips files and dirs