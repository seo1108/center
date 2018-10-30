package yonsei_church.yonsei.center.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import yonsei_church.yonsei.center.data.ResponseModel;

public interface CommonAPI {
    @FormUrlEncoded
    @POST("/token.php")
    void token(@Field("mseq") String mseq,
               @Field("tel") String tel,
               @Field("token") String token,
               StringCallback<String> responseString);
}
