package yonsei_church.yonsei.center.api;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class StringCallback<String> implements Callback<String> {

    @Override
    public final void success(String responseModel, Response response) {
        apiSuccess(responseModel);
    }

    @Override
    public void failure(RetrofitError error) {
        apiError(error);
    }


    public abstract void apiSuccess(String model);


    public abstract void apiError(RetrofitError error);
}