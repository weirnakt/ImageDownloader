package my.home.uploader.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import my.home.uploader.service.ConfigurationService;
import my.home.uploader.utils.ModYamlReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

@ApplicationScoped
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationServiceImpl.class.getName());

    @Inject
    @ConfigurationValue("swarm.http.port")
    private String httpPort;

    @Inject
    @ConfigurationValue("proxy.setting.path")
    private String proxySettingPath;

    @Inject
    @ConfigurationValue("test.content")
    private String testContent;

    @Inject
    @ConfigurationValue("root.download.directory")
    private String baseDirectory;

    @Inject
    @ConfigurationValue("image.max.thread.count")
    private String maxThreadCount;

    @Override
    public String getHttpPort() {
        return httpPort;
    }

    @Override
    public Properties getProxySettings() {
        try {
            return new ModYamlReader().read(proxySettingPath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        return new Properties();
    }

    @Override
    public Document getTestContent() {
        try {
            return Jsoup.parse(new File(testContent), "utf-8");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new RuntimeException("Не удалось прочитать тестовый контент");
        }
    }

    @Override
    public String getBaseDirectory() {
        return baseDirectory;
    }

    @Override
    public int getMaxThreadCount() {
        return NumberUtils.toInt(maxThreadCount, 4);
    }

}
