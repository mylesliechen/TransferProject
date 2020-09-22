package cpm.test.upload;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestGet {

    public static void main(String[] args) throws Exception {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1000, 1000, 2, TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(5000));
        //模拟100人并发请求
        CountDownLatch latch = new CountDownLatch(1);
        //模拟100个用户
        long start = System.currentTimeMillis();
        int count = 10000;
        for (int i = 0; i < count; i++) {

            AnalogUser analogUser = new AnalogUser(latch);
            executor.execute(analogUser);
        }
        //计数器減一  所有线程释放 并发访问。
        latch.countDown();
        System.out.println("qps:{}" + count / (start * 1000));
    }


    static class AnalogUser implements Runnable {
        CountDownLatch latch;

        public AnalogUser(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            long starTime = 0;
            try {
                starTime = System.currentTimeMillis();
                latch.await();
                httpGet("" + UUID.randomUUID(), "");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            Long t = endTime - starTime;
            System.out.println(t / 1000F + "秒");
        }


    }

    static void httpGet(String url, String strParam) {

        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpPost = new HttpGet(url);
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpPost.setConfig(requestConfig);
        try {
            CloseableHttpResponse result = httpClient.execute(httpPost);
            //请求发送成功，并得到响应
            if (result.getStatusLine().getStatusCode() != 200) {
                System.out.println(result.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
        }
    }

}
