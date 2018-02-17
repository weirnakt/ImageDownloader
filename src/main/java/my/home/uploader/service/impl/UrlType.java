package my.home.uploader.service.impl;

/**
 * Тип url.
 */
public enum UrlType {

    JOY("http://joyreactor.cc/tag/"),
    SAN("https://gelbooru.com/index.php?page=post&s=list&"),
    CUS("");

    final String url;

    UrlType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
