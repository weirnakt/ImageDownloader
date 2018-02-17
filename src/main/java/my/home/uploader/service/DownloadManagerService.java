package my.home.uploader.service;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Сервис загрузки.
 */
public interface DownloadManagerService {

    String download(MultipartFormDataInput formData);
}
