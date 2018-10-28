package yonsei_church.yonsei.center.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.adapters.DownloadVideoAdapter;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.data.DownloadVideoItem;

public class DownloadListActivity  extends AppCompatActivity {
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private DownloadVideoAdapter mAdapter;
    SQLiteDatabase contentDB = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());
        setTitle("다운로드 영상");
        initializeView();
        setDownloadList();
    }

    private void initializeView() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.scrollToPosition(0);
        mRecyclerView = findViewById(R.id.ac_fast_delivery_recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DownloadVideoAdapter(this, new ArrayList<DownloadVideoItem>()) {
        };


    }

    public void setDownloadList() {
        List<DownloadVideoItem> list = new ArrayList<>();
        contentDB = this.openOrCreateDatabase(AppConst.DB_NAME, MODE_PRIVATE, null);
        String sql = "SELECT url, path, title, image, downDate from TB_DOWNLOAD";
        Cursor results = contentDB.rawQuery(sql, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            DownloadVideoItem item =  new DownloadVideoItem();

            item.setUrl(results.getString(0));
            item.setPath(results.getString(1));
            item.setTitle(results.getString(2));
            item.setImage(results.getString(3));
            item.setDownDate(results.getString(4));

            Log.d("DownloadManager1", item.getUrl() + "_________" + item.getPath());

            list.add(item);
            Log.d("DownloadManager2", item.getImage());
            results.moveToNext();

        }
        results.close();

        contentDB.close();

        mAdapter.setList(list);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new DownloadVideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()) {
                    case R.id.btn_play : {
                        String filepath = mAdapter.getList().get(position).getPath().replaceAll("file:///", "");
                        Log.d("FILEPATH", urlDecode(filepath));
                        File file = new File(filepath);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "video/*");
                        startActivity(intent);
                    }
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String urlDecode(String str) {
        String result = "";
        try {
            result = URLDecoder.decode(str, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
