package my.home.uploader.service.impl;

import my.home.uploader.service.ConfigurationService;
import my.home.uploader.service.DownloadManagerService;
import my.home.uploader.service.MessageService;
import my.home.uploader.service.parser.ImageInfo;
import my.home.uploader.service.parser.MultiThreadSaver;
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jsoup.Jsoup;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadManagerServiceImpl implements DownloadManagerService {

    private static final Logger LOGGER = Logger.getLogger(DownloadManagerServiceImpl.class.getName());

    private ConfigurationService configuration;

    @Inject
    private MessageService messageService;

    @Inject
    public DownloadManagerServiceImpl(ConfigurationService configuration) {
        this.configuration = configuration;
    }

    private void fixDubls(Set<ImageInfo> images) {
        Map<String, List<ImageInfo>> dublicates = new HashMap<>(images.size());
        for (ImageInfo image : images) {
            if (!dublicates.containsKey(image.getFullName())) {
                dublicates.put(image.getFullName(), new ArrayList<>());
            }
            dublicates.get(image.getFullName()).add(image);
        }
        dublicates.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> {
                    String name = entry.getValue().get(0).getName();
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        entry.getValue().get(i).setName(String.format("%s (%d)", name, i));
                    }
                });
    }

    @Override
    public String download(MultipartFormDataInput formData) {
        // 1. download all page and collect images
        DownloadInfo downloadInfo = new DownloadInfo(formData, configuration);
        try {
            Set<ImageInfo> images = processPage(downloadInfo, -1);
            if (downloadInfo.getUrlType() != UrlType.SAN) {
                for (int i = downloadInfo.getFirstPage(); i >= downloadInfo.getEndPage(); i--) {
                    images.addAll(processPage(downloadInfo, i));
                }
            } else {
                for (int i = downloadInfo.getFirstPage(); i < downloadInfo.getEndPage(); i++) {
                    images.addAll(processPage(downloadInfo, i));
                }
            }
            // 2. download and save images async
            fixDubls(images);
            MultiThreadSaver threadSaver = new MultiThreadSaver(downloadInfo, images);
            threadSaver.run();
            // 3. create result file
            messageService.createResultFile(images, downloadInfo);
        } finally {
            downloadInfo.getClient().close();
        }
        return "OK";
    }

    private String fixLink(String link) {
        try {
            URL url = new URL(link);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());
            return uri.toString();
        } catch (Exception e) {
            return link;
        }
    }

    private String getDownloadUrl(DownloadInfo downloadInfo, int currentPage) {
        if (currentPage == -1) {
            return downloadInfo.getDownloadUrl();
        }
        return downloadInfo.getUrlType() != UrlType.SAN
                ? String.format("%s%d", DownloadHelper.checkPath(downloadInfo.getDownloadUrl()), currentPage)
                : String.format("%s&pid=%d", downloadInfo.getDownloadUrl(), 42 * currentPage);
    }

    private Set<ImageInfo> processPage(DownloadInfo downloadInfo, int currentPage) {
        String downloadUrl = getDownloadUrl(downloadInfo, currentPage);
        if (downloadInfo.getUrlType() != UrlType.SAN) {
            downloadUrl = fixLink(downloadUrl);
        }
        try {
            byte[] pageContent = DownloadHelper.download(downloadUrl, downloadInfo);
            return DownloadHelper.parseHtml(Jsoup.parse(new String(pageContent, "utf-8")), downloadInfo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            return new HashSet<>();
        }
    }
}
