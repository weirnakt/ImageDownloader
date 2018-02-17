package my.home.uploader.service.parser;

import my.home.uploader.service.impl.DownloadHelper;
import org.apache.commons.io.FilenameUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class ImageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> tags;
    private final String link;
    private String name;
    private final String extension;

    public ImageInfo(List<String> tags, String link) {
        this.tags = tags;
        this.link = Optional.ofNullable(link).orElse("");
        this.extension = link == null ? "" : FilenameUtils.getExtension(link);
        String rawName = "";
        if (link != null) {
            rawName = link.substring(link.lastIndexOf('/') + 1, link.length() - extension.length() - 1);
            try {
                rawName = URLDecoder.decode(rawName, "utf-8");
            } catch (UnsupportedEncodingException e) {
                //
            }
        }
        this.name = DownloadHelper.fixFileName(rawName);
    }

    public List<String> getTags() {
        return tags;
    }

    public String getLink() {
        return link;
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return String.format("%s.%s", name, extension);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImageInfo && Objects.equals(((ImageInfo) obj).link, link);
    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }
}
