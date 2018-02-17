package my.home.uploader.service.parser;

import java.util.ArrayList;
import java.util.List;

import my.home.uploader.service.impl.DownloadInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JoyHtmlParser extends HtmlParser {

    public JoyHtmlParser(Document document, DownloadInfo downloadInfo) {
        super(document, downloadInfo);
    }

    @Override
    public void parse() {
        Elements postContainers = document.select("div[class=postContainer]");
        postContainers.forEach(this::processPostContainer);
    }

    private void processPostContainer(Element element) {
        Element tagsElement = element.selectFirst("h2[class=tagList]");
        List<String> tags = new ArrayList<>();
        if (tagsElement != null) {
            for (Element tagElement : tagsElement.children()) {
                if ("b".equals(tagElement.tagName())) {
                    tags.add(tagElement.text());
                }
            }
        }
        List<String> imageLinks = new ArrayList<>();
        Elements postElements = element.select("div[class=post_content]");
        for (Element postElement : postElements) {
            for (Element imageElement : postElement.select("div[class=image]")) {
                for (Element img : imageElement.select("img")) {
                    imageLinks.add(img.attr("src"));
                }
            }
        }
        imageLinks.forEach(imageLink -> images.add(new ImageInfo(tags, imageLink)));
    }
}
