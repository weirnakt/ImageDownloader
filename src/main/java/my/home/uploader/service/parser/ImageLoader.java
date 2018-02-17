package my.home.uploader.service.parser;

import java.util.Queue;
import java.util.concurrent.Callable;

import my.home.uploader.service.impl.DownloadHelper;
import my.home.uploader.service.impl.DownloadInfo;

public class ImageLoader implements Callable<Boolean> {

    private final DownloadInfo downloadInfo;
    private final Queue<ImageInfo> imagesToLoad;

    public ImageLoader(DownloadInfo downloadInfo, Queue<ImageInfo> imagesToLoad) {
        this.downloadInfo = downloadInfo;
        this.imagesToLoad = imagesToLoad;
    }

    @Override
    public Boolean call() {
        if (imagesToLoad.isEmpty()) {
            return false;
        }
        ImageInfo image;
        do {
            image = imagesToLoad.poll();
            if (image != null) {
                DownloadHelper.processImage(downloadInfo, image);
            }
        } while (image != null);
        return true;
    }
}
