package yonsei_church.yonsei.center.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.data.DownloadVideoItem;

public class DownloadVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<DownloadVideoItem> mList;

    private OnItemClickListener onItemClickListener;
    private OnViewClickListener onViewClickListener;

    public DownloadVideoAdapter(Activity activity, List<DownloadVideoItem> list) {
        this.mActivity = activity;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        this.mList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.
                from(mActivity).inflate(R.layout.item_download_video, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecyclerView.ViewHolder) {
            ViewHolder vHolder = ((ViewHolder) holder);
            DownloadVideoItem item = getList().get(position);
            String image = item.getImage();
            String title = item.getTitle();
            String downDate = item.getDownDate();
            String path = item.getPath();

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path.replaceAll("file:///", ""),MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            Matrix matrix = new Matrix();
            Bitmap bitmap = Bitmap.createBitmap(thumb, 0, 0,
                    thumb.getWidth(), thumb.getHeight(), matrix, true);
            vHolder.imageView.setImageBitmap(bitmap);

            //Glide.with(mActivity).load(image).diskCacheStrategy(DiskCacheStrategy.ALL).into(vHolder.imageView);
            vHolder.txtTitle.setText(title);
            vHolder.txtDownDate.setText(downDate);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public List<DownloadVideoItem> getList() {
        return mList;
    }

    public void setList(List<DownloadVideoItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageButton btnPlay;
        TextView txtTitle;
        TextView txtDownDate;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            btnPlay = (ImageButton) itemView.findViewById(R.id.btn_play);
            txtTitle = (TextView) itemView.findViewById(R.id.txt_title);
            txtDownDate = (TextView) itemView.findViewById(R.id.txt_downloadDate);
            imageView = (ImageView) itemView.findViewById(R.id.image);

            btnPlay.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final int postPosition = this.getAdapterPosition();
            onItemClickListener.onItemClick(v, postPosition);
            //onItemClickListener.onItemClick(v, postPosition);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    public interface OnViewClickListener {
        void onViewClick(View v, int position);
    }

    public void setOnViewClickListener(OnViewClickListener listener) {
        this.onViewClickListener = listener;
    }
}
