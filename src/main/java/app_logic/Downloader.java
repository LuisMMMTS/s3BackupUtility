package app_logic;

import app_logic.model.FileStructure;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static app_logic.model.FolderCrawler.newFile;

public class Downloader {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final String usage = "\n" +
                "Usage:\n" +
                "    <bucketName><blobName><folderPath><filePath>\n\n" +
                "Where:\n" +
                "    bucketName - The Amazon S3 bucket to delete the policy from (for example, bucket1).\n" +
                "    blobName - the filename of the S3 blob file" +
                "    folderPath - path to the folder to be uploaded" +
                "    filePath - relative file path of single file to download";

        if (args.length < 3 || args.length > 4) {
            System.out.println(usage);
            System.exit(1);
        }

        String bucketName = args[0];
        String blob_name = args[1];
        Path path = Path.of(args[2]);
        if (!path.toFile().isDirectory() || !path.toFile().exists()) {
            System.out.println("Directory doesnt exist");
            System.exit(1);
        }

        String tempFileName = path + "/" + System.currentTimeMillis() + ".zip";

        Region region = Region.EU_NORTH_1;
        S3Client s3Client = S3Client.builder().region(region).build();
        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(blob_name).build());


        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();

        ByteArrayInputStream in = new ByteArrayInputStream(object.readAllBytes());
        ObjectInputStream is = new ObjectInputStream(in);
        object.close();

        while (true) {
            try {
                FileStructure a = (FileStructure) is.readObject();
                if (args[3]==null || Objects.equals(a.getFilePath(), args[3])) {
                    a.writeFile(path);
                }
            }catch (EOFException e){
                System.out.println("all objects read");
                break;
            }
        }
        in.close();
        is.close();

    }


}
