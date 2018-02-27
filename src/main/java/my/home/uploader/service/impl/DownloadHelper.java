package my.home.uploader.service.impl;

import my.home.uploader.service.parser.HtmlParser;
import my.home.uploader.service.parser.ImageInfo;
import my.home.uploader.service.parser.JoyHtmlParser;
import my.home.uploader.service.parser.SanHtmlParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jsoup.nodes.Document;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DownloadHelper {

    private static final Logger LOGGER = Logger.getLogger(DownloadHelper.class.getName());
    private static final Integer CONNECTION_TIMEOUT = 20000;

    private DownloadHelper() {
        //
    }

    public static byte[] download(String url, DownloadInfo downloadInfo) {
        int count = 0;
        Throwable t;
        do {
            try (CloseableHttpResponse response = downloadInfo.getClient().getClient().execute(new HttpGet(url))) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException(String.format("Ошибка получения страницы %s (код %d) %s",
                            url, response.getStatusLine().getStatusCode(),
                            IOUtils.toString(response.getEntity().getContent(), "utf-8")));
                }
                HttpEntity entity = response.getEntity();
                if (entity == null || entity.getContent() == null) {
                    throw new RuntimeException(String.format("По адресу %s нет контента", url));
                }
                return IOUtils.toByteArray(response.getEntity().getContent());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, null, e);
                t = e;
            }
            count++;
        } while (count < 3);
        throw new RuntimeException(t);
    }

    public static CloseableHttpClient createHttpClient(DownloadInfo downloadInfo) {
        try {
            RequestConfig.Builder builder = RequestConfig.custom()
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                    .setSocketTimeout(CONNECTION_TIMEOUT);
            if (downloadInfo.getUrlType() != UrlType.SAN) {
                builder.setProxy(new HttpHost(downloadInfo.getProxyAddress(), downloadInfo.getProxyPort(),
                        downloadInfo.getProxyScheme()));
            }
            RequestConfig config = builder.build();
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            HttpsURLConnection.setDefaultHostnameVerifier((string, ssls) -> true);
            /*CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(downloadInfo.getProxyAddress(), downloadInfo.getProxyPort()),
                    new UsernamePasswordCredentials("login", "pass"));*/
            return HttpClients.custom()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultRequestConfig(config)
                    //.setDefaultCredentialsProvider(credsProvider)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new RuntimeException(String.format("Ошибка создания менеджера закачек: %s", e.getMessage()));
        }
    }

    public static Set<ImageInfo> parseHtml(Document document, DownloadInfo downloadInfo) {
        HtmlParser parser = downloadInfo.getUrlType() != UrlType.SAN ? new JoyHtmlParser(document, downloadInfo)
                : new SanHtmlParser(document, downloadInfo);
        parser.parse();
        return parser.getImages();
    }

    private static void addToMap(Map<String, String> map, String rusLetter, String engLetter) {
        map.put(rusLetter, engLetter);
    }

    private static void fillTransliterationMap(Map<String, String> map) {
        String[] ruChars = ("а|б|в|г|д|е|ё|ж|з|и|й|к|л|м|н|о|п|р|с|т|у|ф|х|ц|ч|ш|щ|ъ|ы|ь|э|ю|я|"
                + "А|Б|В|Г|Д|Е|Ё|Ж|З|И|Й|К|Л|М|Н|О|П|Р|С|Т|У|Ф|Х|Ц|Ч|Ш|Щ|Ъ|Ы|Ь|Э|Ю|Я").split("\\|");
        String[] enChars = ("a|b|v|g|d|e|e|zh|z|i|y|c|l|m|n|o|p|r|s|t|u|f|h|c|ch|sh|sh||y||e|u|ya|"
                + "A|B|V|G|D|E|E|ZH|Z|I|Y|C|L|M|N|O|P|R|S|T|U|F|H|C|CH|SH|SH||Y||E|U|YA|").split("\\|");
        for (int i = 0; i < ruChars.length; i++) {
            addToMap(map, ruChars[i], enChars[i]);
        }
    }

    private static String transliterateLetter(char letter, Map<String, String> transliterationMap) {
        String stringLetter = String.valueOf(letter);
        return transliterationMap.getOrDefault(stringLetter, stringLetter);
    }

    /**
     * Транслитеровать русское имя в английское.
     *
     * @param ruName русское имя
     * @return анлийская транслитерация
     */
    private static String transliterateName(String ruName) {
        if (ruName == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Map<String, String> transliterationMap = new HashMap<>();
        fillTransliterationMap(transliterationMap);
        for (int i = 0; i < ruName.length(); i++) {
            result.append(transliterateLetter(ruName.charAt(i), transliterationMap));
        }
        return result.toString();
    }

    /**
     * Поправить имя файла (если есть запрещенные символы).
     *
     * @param fileName имя файла
     * @return безопасное имя
     */
    public static String fixFileName(String fileName) {
        return transliterateName(fileName)
                .replace("\\", "_").replace("/", "_")
                .replace(":", "_").replace("*", "_")
                .replace("?", "_").replace("\"", "_")
                .replace("<", "_").replace(">", "_")
                .replace("|", "_").replace("+", "_");
    }

    /**
     * Поправить путь.
     *
     * @param path путь
     * @return исправленный путь
     */
    public static String checkPath(String path) {
        return path.endsWith("/") ? path : (path + "/");
    }

    public static void processImage(DownloadInfo downloadInfo, ImageInfo image) {
        try {
            byte[] pageContent = DownloadHelper.download(image.getLink(), downloadInfo);
            File imageFile = new File(downloadInfo.getSaveDirectory(), image.getFullName());
            FileUtils.writeByteArrayToFile(imageFile, pageContent);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            downloadInfo.addErrorLoad(image.getFullName());
        }
    }
}
