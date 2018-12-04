package cn.lemon.view.adapter;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by linlongxin on 2015/12/19.
 */
public class BaseViewHolder<T> extends RecyclerView.ViewHolder{

    private T mData;

    public BaseViewHolder(ViewGroup parent, int layoutId) {
        this(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    public BaseViewHolder(View itemView) {
        super(itemView);
        onInitializeView();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v,mData);
            }
        });
    }

    protected void onInitializeView() {

    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findViewById(@IdRes int resId) {
        if (itemView != null) {
            return (T) itemView.findViewById(resId);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected void setData(final T data) {
        if (data == null) {
            return;
        }
        mData = data;
    }

    protected void bind(int pos) {

    }

    protected T getData() {
        return mData;
    }

    protected void onItemViewClick(View view,T data) {

    }
}
