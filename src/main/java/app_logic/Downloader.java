package app_logic;

import app_logic.model.FolderCrawler;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static app_logic.model.FolderCrawler.newFile;

public class Downloader {
    public static void main(String[] args) throws IOException {
        final String usage = "\n" +
                "Usage:\n" +
                "    <bucketName><blobName><folderPath><filePath>\n\n" +
                "Where:\n" +
                "    bucketName - The Amazon S3 bucket to delete the policy from (for example, bucket1).\n"+
                "    blobName - the filename of the S3 blob file"+
                "    folderPath - path to the folder to be uploaded"+
                "    filePath - relative file path of single file to download";

        if (args.length < 3|| args.length > 4) {
            System.out.println(usage);
            System.exit(1);
        }

        String bucketName = args[0];
        String blob_name = args[1];
        Path path = Path.of(args[2]);
        if (!path.toFile().isDirectory() || !path.toFile().exists()){
            System.out.println("Directory doesnt exist");
            System.exit(1);
        }

        String tempFileName= path.toString()+"/"+String.valueOf(System.currentTimeMillis())+".zip";

        Region region = Region.EU_NORTH_1;
        S3Client s3Client = S3Client.builder().region(region).build();
        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(blob_name).build());

        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFileName));

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = object.read(buffer)) !=  -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        object.close();
        outputStream.close();


        buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFileName));
        ZipEntry zipEntry;

        while ((zipEntry = zis.getNextEntry())!= null) {
            if (args.length==4){
                if (!zipEntry.getName().equals(args[3])){
                        continue;
                }
            }
            File newFile = newFile(path.toFile(), zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            //zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
        new File(tempFileName).delete();
    }


}
