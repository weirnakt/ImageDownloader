package my.home.uploader.service.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import my.home.uploader.service.impl.DownloadInfo;
import my.home.uploader.service.impl.DownloadManagerServiceImpl;

/**
 * Многопоточный скачиватель изображений.
 */
public class MultiThreadSaver {

    private static final Logger LOGGER = Logger.getLogger(DownloadManagerServiceImpl.class.getName());

    // максимальное количество потоков
    private final int maxThreadsCount;
    private ExecutorService executorService;
    private final Queue<Future<Boolean>> runningTasks = new LinkedList<>();
    private final Queue<ImageInfo> imagesToLoad = new ConcurrentLinkedQueue<>();
    private final DownloadInfo downloadInfo;

    public MultiThreadSaver(DownloadInfo downloadInfo, Set<ImageInfo> images) {
        this.maxThreadsCount = downloadInfo.getMaxThreadCount();
        this.downloadInfo = downloadInfo;
        imagesToLoad.addAll(images);
    }

    public void run() {
        saveMultithreaded();
    }

    private void saveMultithreaded() {
        int threadsCount = Math.min(maxThreadsCount, imagesToLoad.size());
        executorService = Executors.newFixedThreadPool(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            appendTask();
        }
        try {
            while (!runningTasks.isEmpty()) {
                Future<Boolean> task = runningTasks.poll();
                try {
                    task.get(1, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    appendTask();
                } catch (TimeoutException e) {
                    runningTasks.add(task);
                } catch (InterruptedException | CancellationException e) {
                    LOGGER.log(Level.SEVERE, null, e);
                    throw new RuntimeException("Download error:", e);
                }
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    private void appendTask() {
        ImageLoader callable = new ImageLoader(downloadInfo, imagesToLoad);
        Future<Boolean> task = executorService.submit(callable);
        runningTasks.add(task);
    }
}
