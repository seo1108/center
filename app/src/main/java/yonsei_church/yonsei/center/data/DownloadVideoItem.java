package yonsei_church.yonsei.center.data;

public class DownloadVideoItem {
    private String url;
    private String path;
    private String title;
    private String image;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDownDate() {
        return downDate;
    }

    public void setDownDate(String downDate) {
        this.downDate = downDate;
    }

    private String downDate;


}
