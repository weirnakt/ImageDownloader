package my.home.uploader.service.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.html.HTMLEditorKit;

import my.home.uploader.service.impl.DownloadInfo;
import org.jsoup.nodes.Document;

public abstract class HtmlParser {

    protected final List<String> imageLinks = new ArrayList<>();
    protected final Set<ImageInfo> images = new HashSet<>();
    protected final DownloadInfo downloadInfo;
    protected final Document document;

    public HtmlParser(Document document, DownloadInfo downloadInfo) {
        this.document = document;
        this.downloadInfo = downloadInfo;
    }

    public abstract void parse();

    public List<String> getImageLinks() {
        return imageLinks;
    }

    public Set<ImageInfo> getImages() {
        return images;
    }
}
