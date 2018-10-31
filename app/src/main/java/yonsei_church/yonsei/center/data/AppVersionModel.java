package yonsei_church.yonsei.center.data;

import com.google.gson.annotations.SerializedName;

public class AppVersionModel extends ResponseModel {
    @SerializedName("flag")
    private String flag;
    @SerializedName("version")
    private String version;

    public String getFlag() {
        return flag;
    }

    public String getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    @SerializedName("message")
    private String message;
}
