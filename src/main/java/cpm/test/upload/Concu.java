package cpm.test.upload;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Concu {
    public static void main(String[] args) throws InterruptedException {

        AmazonS3 amazonS3;
        String ak = "";
        String sk = "";
//        String endpoint = "";
//        String endpoint = "";
        String endpoint = "";
//        String endpoint = "";
//        String endpoint = "";


        AWSCredentials credentials = null;

        ClientConfiguration config = new ClientConfiguration();

        config.setMaxConnections(5000);

        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(endpoint, "");


        credentials = new BasicAWSCredentials(ak, sk);

        amazonS3 = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .disableChunkedEncoding()//目前不支持chunk
                .withPathStyleAccessEnabled(true)
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        int actionCount = 10000000;
        long start = System.currentTimeMillis();
        AtomicLong atomicLong = new AtomicLong(0);
        for (int i = 0; i < actionCount; ++i) {
            executorService.execute(() -> {
                try {
                    long count = atomicLong.incrementAndGet();
                    if (count % 200 == 0) {
                        System.out.println("count: " + count);
                    }
                    amazonS3.getObject("test-bucket" + UUID.randomUUID(), "key");
                } catch (AmazonServiceException ex) {
                    if (ex.getStatusCode() != 404) {
                        System.out.println(ex.getMessage());
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1000, TimeUnit.HOURS);
        long duration = (System.currentTimeMillis() - start) / 1000L;
        System.out.println("qps : " + actionCount / duration);

    }
}

///1.8.0-242.b08-1.el7.x86_64
