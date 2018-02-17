package my.home.uploader.service.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import my.home.uploader.service.impl.DownloadHelper;
import my.home.uploader.service.impl.DownloadInfo;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SanHtmlParser extends HtmlParser {

    private static final Logger LOGGER = Logger.getLogger(SanHtmlParser.class.getName());

    public SanHtmlParser(Document document, DownloadInfo downloadInfo) {
        super(document, downloadInfo);
    }

    private String fixLink(String link) {
        return link == null ? "" : String.format("https:%s", link);
    }

    @Override
    public void parse() {
        Element containPush = document.selectFirst("div[class=contain-push]");
        Elements previewThumbnails = containPush.select("div[class=thumbnail-preview]");
        List<String> thumbnails = new ArrayList<>(previewThumbnails.size());
        for (Element thumbnail : previewThumbnails) {
            Element imagePageLink = thumbnail.selectFirst("a");
            thumbnails.add(fixLink(imagePageLink.attr("href")));
        }
        thumbnails.forEach(this::processPreview);
    }

    private String readTag(Element tagElement) {
        if (tagElement.children().isEmpty()) {
            return "";
        }
        Element lastAElement = tagElement.child(0);
        for (Element child : tagElement.children()) {
            if ("a".equals(child.tagName())) {
                lastAElement = child;
            }
        }
        return lastAElement.text();
    }

    private void processPreview(String thumbnail) {
        try {
            byte[] imageContent = DownloadHelper.download(thumbnail, downloadInfo);
            if (imageContent == null) {
                return;
            }
            Document imageDocument = Jsoup.parse(new String(imageContent, "utf-8"));
            /*Document imageDocument = Jsoup.parse(FileUtils
                    .readFileToString(new File("d:\\webConsolidation\\Uploader\\index2.html"), "utf-8"));*/
            Element tagsElement = imageDocument.selectFirst("ul[id=tag-list]");
            Elements tagsElements = tagsElement.select("li[class]");
            List<String> tags = new ArrayList<>(tagsElements.size());
            for (Element tagElement : tagsElements) {
                tags.add(readTag(tagElement));
            }
            Element imageContainer = imageDocument.selectFirst("div[id=post-view]");
            Element img = imageContainer.selectFirst("img");
            images.add(new ImageInfo(tags, img.attr("src")));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
