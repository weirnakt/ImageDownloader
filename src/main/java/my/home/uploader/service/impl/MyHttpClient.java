package my.home.uploader.service.impl;

import org.apache.http.impl.client.CloseableHttpClient;

public class MyHttpClient {

    private CloseableHttpClient client;

    public MyHttpClient(DownloadInfo downloadInfo) {
        this.client = DownloadHelper.createHttpClient(downloadInfo);
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                //
            }
        }
    }
}
