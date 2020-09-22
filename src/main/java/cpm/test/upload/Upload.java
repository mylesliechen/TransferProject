package cpm.test.upload;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import java.io.File;

public class Upload {
    public static void fileUpload(String endPoint, String accessKey, String secretKey, String bucketName, String keyPrefix, String dirPath) {

        ClientConfiguration config = new ClientConfiguration();
        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(endPoint, "");
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        AmazonS3 amazonS3 = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .withPathStyleAccessEnabled(true)
                .build();

        TransferManager xfer_mgr = TransferManagerBuilder.standard()
                .withS3Client(amazonS3)
                .build();
        long start = System.currentTimeMillis();

        try {
            MultipleFileUpload multipleFileUpload = xfer_mgr.uploadDirectory(bucketName,
                    keyPrefix, new File(dirPath), true);
            multipleFileUpload.waitForCompletion();
        } catch (AmazonServiceException e) {
            System.out.println("AmazonServiceException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("InterruptedException");
            e.printStackTrace();
        }
        xfer_mgr.shutdownNow();

        System.out.println(String.format("upload success cost %d ms", System.currentTimeMillis() - start));
    }

    public static void main(String[] args) {
        if (6 != args.length) {
            System.out.println("参数错误");
            System.exit(1);
        }
        String endPoint = args[0];
        String ak = args[1];
        String sk = args[2];
        String bucketName = args[3];
        String keyPrefix = args[4];
        String dirPath = args[5];
        File file = new File(dirPath);
        if (!(file.exists() && file.isDirectory())) {
            System.out.println("file directory does not exists");
            System.exit(1);
        }


        fileUpload(endPoint, ak, sk, bucketName, keyPrefix, dirPath);
    }
}
