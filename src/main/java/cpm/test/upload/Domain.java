package cpm.test.upload;

import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Domain {
    public static void main(String[] args) throws InterruptedException, IOException {
        String ACCEPT_HEADER_JSON = "application/json";
        int MAX_CONN_TOTAL = 10000;
        int MAX_CONN_PER_ROUTE = 10000;
        int CONNECTION_TIMEOUT_MS = 3000;
        int SOCKET_TIMEOUT_MS = 5000;
        int CONNECTION_REQUEST_TIMEOUT_MS = 5000;
        String domain = "" + UUID.randomUUID();
        String fullUrl = "" + domain;
        HttpUriRequest request = RequestBuilder
                .get()
                .setUri(fullUrl)
                .setHeader(HttpHeaders.ACCEPT, ACCEPT_HEADER_JSON)
                .setHeader("", "")
                .build();
//        HttpResponse response = httpClient.execute(request);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
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
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet(fullUrl);
                    RequestConfig requestConfig = RequestConfig.custom()
                            .setSocketTimeout(SOCKET_TIMEOUT_MS)
                            .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
                            .build();
                    httpGet.setConfig(requestConfig);
                    try {
                        CloseableHttpResponse result = httpClient.execute(httpGet);
                        if (result.getStatusLine().getStatusCode() != 200) {
                            System.out.println(result.getStatusLine().getReasonPhrase());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        httpGet.releaseConnection();
                        Thread.sleep(100);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1000, TimeUnit.HOURS);
        long duration = (System.currentTimeMillis() - start) / 1000L;
        System.out.println("qps : " + actionCount / duration);
    }

}

