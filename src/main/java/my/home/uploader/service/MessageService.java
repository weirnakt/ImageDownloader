package my.home.uploader.service;

import java.util.Set;

import my.home.uploader.service.impl.DownloadInfo;
import my.home.uploader.service.parser.ImageInfo;

public interface MessageService {

    void sendMessage(String message);

    void createResultFile(Set<ImageInfo> images, DownloadInfo downloadInfo);
}
