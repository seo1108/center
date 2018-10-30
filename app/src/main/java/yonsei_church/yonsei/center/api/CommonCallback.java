package yonsei_church.yonsei.center.api;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import yonsei_church.yonsei.center.data.ResponseModel;

public abstract class CommonCallback<T extends ResponseModel> implements Callback<T> {
    @Override
    public final void success(T responseModel, Response response) {
        apiSuccess(responseModel);
    }

    @Override
    public void failure(RetrofitError error) {
        apiError(error);
    }

    public abstract void apiSuccess(T model);

    public abstract void apiError(RetrofitError error);
}