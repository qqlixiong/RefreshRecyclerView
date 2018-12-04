package cn.lemon.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.lemon.view.adapter.Action;
import cn.lemon.view.adapter.IGridLayoutManagerCount;
import cn.lemon.view.adapter.RecyclerAdapter;


/**
 * Created by linlongxin on 2016/1/24.
 */
public class RefreshRecyclerView extends FrameLayout implements SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = "RefreshRecyclerView";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private List<Action> mRefreshActions;
    private boolean mLoadMoreEnable;
    private boolean mShowNoMoreEnable;

    public RefreshRecyclerView(Context context) {
        this(context, null);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = inflate(context, R.layout.view_refresh_recycler, this);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.lemon_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.lemon_refresh_layout);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RefreshRecyclerView);
        mLoadMoreEnable = typedArray.getBoolean(R.styleable.RefreshRecyclerView_load_more_enable, true);
        mShowNoMoreEnable = typedArray.getBoolean(R.styleable.RefreshRecyclerView_show_no_more_enable, true);
        boolean refreshEnable = typedArray.getBoolean(R.styleable.RefreshRecyclerView_refresh_enable, true);
        if (!refreshEnable) {
            mSwipeRefreshLayout.setEnabled(false);
        } else {
            mSwipeRefreshLayout.setOnRefreshListener(this);
        }
        typedArray.recycle();
    }

    public void setAdapter(RecyclerAdapter adapter) {
        if (adapter == null) {
            return;
        }
        mRecyclerView.setAdapter(adapter);
        mAdapter = adapter;
        setLoadMoreEnable(true);
    }

    public void setLoadMoreEnable(boolean b) {
        if (mAdapter == null)return;
        mAdapter.setLoadMoreEnable(b ? mLoadMoreEnable : true);
        mAdapter.setShowNoMoreEnable(b ? mShowNoMoreEnable : true);
    }

    public void setLayoutManager(LinearLayoutManager linearLayoutManager) {
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    public void setLayoutManager(final GridLayoutManager gridLayoutManager, final IGridLayoutManagerCount iGridLayoutManagerCount) {
        mRecyclerView.setLayoutManager(gridLayoutManager);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                /**
                 * 根据GridLayoutManager的getSpanSize方法可以动态的设置item跨列数
                 * 需要设置：4个参数的GridLayoutManager
                 * new GridLayoutManager(getActivity(),6,GridLayoutManager.VERTICAL,false);
                 * 这里的6（自己设置的最好设置成偶数）就相当于分母，6默认显示一整行（1列），下面的3 和2 就相当于分子，返回3就是（1/2）所以此类型对应的是2列，返回2就是（1/3）所以此类型对应的是3列
                 * 例如：根据getItemViewType返回的itemtype，第一个position显示为轮播图，所以返回的是gridManager.getSpanCount();（即：6）
                 * 例如：这里的轮播图类型和head  返回默认的spansize（即是6）
                 * 如果要显示两列就返回3   3除以6  二分之一
                 * */
                int type = mAdapter.getItemViewType(position);
                if (type == RecyclerAdapter.HEADER_TYPE
                        || type == RecyclerAdapter.FOOTER_TYPE
                        || type == RecyclerAdapter.STATUS_TYPE) {
                    return gridLayoutManager.getSpanCount();
                } else {
                    return iGridLayoutManagerCount.count(type);
                }
            }
        });
    }

    public void addRefreshAction(final Action action) {
        if (action == null) {
            return;
        }
        if (mRefreshActions == null) {
            mRefreshActions = new ArrayList<>();
        }
        mRefreshActions.add(action);
    }

    public void setItemAnimator() {
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mRecyclerView.getItemAnimator().setChangeDuration(300);
        mRecyclerView.getItemAnimator().setMoveDuration(300);
    }

    public void scrollToPositionWithOffset(int position) {
        if (position != -1) {
            mRecyclerView.scrollToPosition(position);
            LinearLayoutManager mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            if (mLayoutManager == null)return;
            mLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    public int getScollYDistance() {
        try {
            LinearLayoutManager layoutManager = (LinearLayoutManager) this.mRecyclerView.getLayoutManager();
            int position = layoutManager.findFirstVisibleItemPosition();
            View firstVisiableChildView = layoutManager.findViewByPosition(position);
            int itemHeight = firstVisiableChildView.getHeight();
            int itemTop = firstVisiableChildView.getTop();
            return position * itemHeight - itemTop;
        } catch (Exception var6) {
            var6.printStackTrace();
            return 0;
        }
    }

    public void setLoadMoreAction(final Action action) {
        if (mAdapter.isShowNoMoring() || !mLoadMoreEnable) {
            return;
        }
        mAdapter.setLoadMoreAction(action);
    }

    public void setLoadMoreErrorAction(final Action action) {
        Log.d(TAG, "setLoadMoreErrorAction");
        if (mAdapter.isShowNoMoring() || !mLoadMoreEnable) {
            return;
        }
        mAdapter.setLoadMoreErrorAction(action);
    }

    public void showNoMore() {
        mAdapter.showNoMore();
    }

    public void setItemSpace(int left, int top, int right, int bottom) {
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(left, top, right, bottom));
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public void setSwipeRefreshColorsFromRes(@ColorRes int... colors) {
        mSwipeRefreshLayout.setColorSchemeResources(colors);
    }

    /**
     * 8位16进制数 ARGB
     */
    public void setSwipeRefreshColors(@ColorInt int... colors) {
        mSwipeRefreshLayout.setColorSchemeColors(colors);
    }

    public void showSwipeRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    public void dismissSwipeRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        for (Action a : mRefreshActions) {
            a.onAction();
        }
    }
}
