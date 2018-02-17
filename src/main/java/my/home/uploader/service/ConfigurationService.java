package my.home.uploader.service;

import java.util.Properties;

import org.jsoup.nodes.Document;

public interface ConfigurationService {

    String getHttpPort();

    Properties getProxySettings();

    Document getTestContent();

    String getBaseDirectory();

    int getMaxThreadCount();
}
