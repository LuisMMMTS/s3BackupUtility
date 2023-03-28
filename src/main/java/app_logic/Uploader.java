package app_logic;

import app_logic.model.FileStructure;
import app_logic.model.FolderCrawler;
import app_logic.model.S3;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static app_logic.model.S3.sendPart;


public class Uploader {
    private final static int CHUNK_SIZE = 5 * 1024 * 1024;

    public static void main(String[] args) throws IOException {

        final String usage = "\n" + "Usage:\n" + "    <bucketName><blobName><folderPath>\n\n" + "Where:\n" + "    bucketName - The Amazon S3 bucket to delete the policy from (for example, bucket1).\n" + "    blobName - the filename of the S3 blob file" + "    folderPath - path to the folder to be uploaded";

        if (args.length != 3) {
            System.out.println(usage);
            System.exit(1);
        }

        String bucketName = args[0];
        String blob_name = args[1];
        System.setProperty("user.dir", args[2]);
        String tempFileName = System.currentTimeMillis() + ".zip";

        Region region = Region.EU_NORTH_1;
        S3Client s3Client = S3Client.builder().region(region).build();
        CreateMultipartUploadResponse uploader = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder().bucket(bucketName).key(blob_name).build());
        List<CompletedPart> completedParts = new ArrayList<CompletedPart>();


        try (PipedInputStream in=new PipedInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             //PipedOutputStream out = new PipedOutputStream(in);
             //ObjectInputStream ois=new ObjectInputStream(in);
             ObjectOutputStream oos=new ObjectOutputStream(bos);
        ) {

            FolderCrawler folderCrawler = new FolderCrawler("./");
            Stream<Path> paths = folderCrawler.createPathStream();

            paths.filter(x -> !x.toString().equals(".")).map(x -> Path.of(x.toString().replace("./", ""))).filter(x -> !x.toString().contains(tempFileName)).forEach(node -> {
                try {
                    byte[] byteArray = new byte[0];
                    boolean isDir = node.toFile().isDirectory();
                    String fileName = node.toString();
                    FileStructure file;
                    if (isDir) {
                        if (folderCrawler.folderIsEmpty(node)) {
                            fileName = node + "/.keep";
                        } else {
                            return;
                        }
                    } else {
                        //new FileStructure(node);
                        try (FileInputStream fileReader = new FileInputStream(fileName)) {
                            byteArray = fileReader.readAllBytes();
                        }
                    }
                    FileStructure fileStructure = new FileStructure(Path.of(fileName));

                    oos.writeObject(fileStructure);
                    oos.flush();
                    //out.flush();


                    while (bos.size()>=CHUNK_SIZE){
                        //byte[] buffer = new byte[CHUNK_SIZE];
                        //in.read(buffer);
                        completedParts.add(sendPart(s3Client,uploader,bucketName,blob_name, completedParts.size()+1, bos.toByteArray()));
                        bos.reset();
                    }


                } catch (Exception e) {
                    System.out.println(e);
                }



            });
            oos.close();
            //out.close();
            while (bos.size()!=0){
                //byte[] buffer = new byte[CHUNK_SIZE];
                //byte[] buffer=in.readAllBytes();
                completedParts.add(sendPart(s3Client,uploader,bucketName,blob_name, completedParts.size()+1, bos.toByteArray()));
                bos.reset();
            }
            bos.close();
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(completedParts).build();
            CompleteMultipartUploadResponse completedUploadResponse = s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(bucketName)
                            .key(blob_name)
                            .uploadId(uploader.uploadId())
                            .multipartUpload(completedMultipartUpload).build());


        }


    }


}


//TODO:authenticate in S3
//TODO:Check if bucket already exists and create or reuse it
//TODO: ask for blob name and if repeated ask if want to replace
//todo:skips files and dirs