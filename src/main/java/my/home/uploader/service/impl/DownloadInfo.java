package my.home.uploader.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import my.home.uploader.service.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public class DownloadInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String downloadUrl;
    private final int firstPage;
    private final int endPage;
    private final String proxyAddress;
    private final int proxyPort;
    private final UrlType urlType;
    private final String proxyScheme;
    private final String saveDirectory;
    private final transient MyHttpClient httpClient;
    private final int maxThreadCount;
    private final List<String> errorLoad = new CopyOnWriteArrayList<>();

    public DownloadInfo(MultipartFormDataInput formData, ConfigurationService configuration) {
        Properties proxySettings = configuration.getProxySettings();
        Properties params = processFormData(formData);
        this.firstPage = NumberUtils.toInt(params.getProperty("startPage", "0"));
        this.endPage = NumberUtils.toInt(params.getProperty("endPage", "0"));
        this.urlType = UrlType.valueOf(params.getProperty("urlType"));
        this.downloadUrl = String.format("%s%s", urlType.getUrl(), params.getProperty("inputAddress"));
        this.proxyAddress = proxySettings.getProperty("address");
        this.proxyScheme = proxySettings.getProperty("scheme", "http");
        this.proxyPort = NumberUtils.toInt(proxySettings.getProperty("port"), 8080);
        if (configuration.getBaseDirectory() == null) {
            throw new RuntimeException("Не задан базовый каталог для сохранения в settings.yml");
        }
        File fSaveDirectory;
        if (StringUtils.isNotBlank(params.getProperty("saveDir"))) {
            fSaveDirectory = new File(DownloadHelper.checkPath(configuration.getBaseDirectory()),
                    DownloadHelper.fixFileName(params.getProperty("saveDir")));
        } else {
            fSaveDirectory = new File(DownloadHelper.checkPath(configuration.getBaseDirectory()));
        }
        if (!fSaveDirectory.exists() && !fSaveDirectory.mkdirs()) {
            throw new RuntimeException("Не удалось создать каталог для сохранения");
        }
        this.saveDirectory = fSaveDirectory.getAbsolutePath();
        this.httpClient = new MyHttpClient(this);
        this.maxThreadCount = configuration.getMaxThreadCount();
    }

    private Properties processFormData(MultipartFormDataInput formData) {
        Properties result = new Properties();
        for (Map.Entry<String, List<InputPart>> field : formData.getFormDataMap().entrySet()) {
            String fieldName = Optional.ofNullable(field.getKey()).orElse("");
            InputPart rawValue = field.getValue().get(0);
            String value = null;
            try {
                value = rawValue.getBodyAsString();
            } catch (IOException e) {
                //
            }
            if (value != null) {
                result.setProperty(fieldName, value);
            }
        }
        return result;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getEndPage() {
        return endPage;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public String getProxyScheme() {
        return proxyScheme;
    }

    public UrlType getUrlType() {
        return urlType;
    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public void addErrorLoad(String fileName) {
        errorLoad.add(fileName);
    }

    public List<String> getErrorLoad() {
        return errorLoad;
    }

    public MyHttpClient getClient() {
        return httpClient;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }
}
