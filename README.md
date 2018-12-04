# RefreshRecyclerView

- RecyclerAdapter : 支持下拉刷新，上拉加载，添加Header，Footer
- MultiTypeAdapter/CustomMultiTypeAdapter : 针对 复杂数据类型列表 展示Adapter 
        
**注意**
所有的 adapter 可以配合任意的 RecyclerView 或者 它的子类 使用，而不是仅仅局限于 RefreshRecyclerView 这个组件。

## 使用方法

- xml布局文件

```xml
<cn.lemon.view.RefreshRecyclerView
    android:id="@+id/recycler_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:refresh_able="true"
    app:load_more_able="false"/>
```
         
- java代码

```java
mRecyclerView = (RefreshRecyclerView) findViewById(R.id.recycler_view);
mRecyclerView.setSwipeRefreshColors(0xFF437845,0xFFE44F98,0xFF2FAC21);
mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
mRecyclerView.setAdapter(mAdapter);
mRecyclerView.addRefreshAction(new Action() {
    @Override
    public void onAction() {
        // TODO：刷新数据
    }
});

mRecyclerView.setLoadMoreAction(new Action() {
    @Override
    public void onAction() {
        // TODO：加载更多
    }
});
mRecyclerView.setLoadMoreErrorAction(new Action() {
   @Override
   public void onAction() {
       // TODO：加载更多错误，点击重新加载
   }
});
```

```java
// Header 和 Footer 支持
mAdapter.setHeader(textView);
mAdapter.setFooter(footer);
```
                
### RecyclerAdapter

> 针对相同数据类型列表，可添加 Header，Footer

自定义 Adapter 应该继承 RecyclerAdapter<T>，如：

```java
class CardRecordAdapter extends RecyclerAdapter<Consumption> {

    public CardRecordAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder<Consumption> onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new CardRecordHolder(parent);
    }
}
```

### MultiTypeAdapter

> 复杂数据类型列表的 Adapter，没有 Header,Footer 的概念，每个 Item 对应一个 ViewHolder
> 注意：通过反射实现，支持 ViewHolder 的带有一个参数（ViewGroup）和无参两种形式构造函数，性能方面微小的损耗。
> 构造函数为保证反射时能获取到，应该写成 public 静态内部类 或者 public 的单独类。

```
private MultiTypeAdapter mAdapter = new MultiTypeAdapter(this);
mAdapter.add(ImageViewHolder.class, getImageVirtualData());
mAdapter.addAll(TextViewHolder.class, getTextVirtualData());
mAdapter.addAll(TextImageViewHolder.class, getTextImageVirualData());
mAdapter.addAll(CardRecordHolder.class, getRecordVirtualData());
```

### 设置一个页面既有流线布局也有网格布局（可以加载不同的网格）

```
使用方法：
setLayoutManager(final GridLayoutManager gridLayoutManager, final IGridLayoutManagerCount iGridLayoutManagerCount)
设置详解
/**
                 * 根据GridLayoutManager的getSpanSize方法可以动态的设置item跨列数
                 * 需要设置：4个参数的GridLayoutManager
                 * new GridLayoutManager(getActivity(),6,GridLayoutManager.VERTICAL,false);
                 * 这里的6（自己设置的最好设置成偶数）就相当于分母，6默认显示一整行（1列），下面的3 和2 就相当于分子，返回3就是（1/2）所以此类型对应的是2列，返回2就是（1/3）所以此类型对应的是3列
                 * 例如：根据getItemViewType返回的itemtype，第一个position显示为轮播图，所以返回的是gridManager.getSpanCount();（即：6）
                 * 例如：这里的轮播图类型和head  返回默认的spansize（即是6）
                 * 如果要显示两列就返回3   3除以6  二分之一
                 * */
```

### 设置RecycleView滑动距离

```
使用方法：
scrollToPositionWithOffset(int position)
```

### CustomMultiTypeAdapter （推荐使用）

> 功能和 MultiTypeAdapter 一样，但避免了反射带来的弊端，需要实现 IViewHolderFactory 接口类来管理viewtype 和 ViewHolder 的映射关系。 

```java
// 映射 viewtype 和 ViewHolder
@Override
public <V extends BaseViewHolder> V getViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
        case VIEW_TYPE_TEXT:
            return (V) new TextViewHolder(parent);
        case VIEW_TYPE_IAMGE:
            return (V) new ImageViewHolder(parent);
        case VIEW_TYPE_TEXT_IMAGE:
            return (V) new TextImageViewHolder(parent);
        case VIEW_TYPE_CARD:
            return (V) new CardRecordHolder(parent);
        default:
            return (V) new TextViewHolder(parent);
    }
}

// 绑定数据
mAdapter.add(getImageVirtualData(), VIEW_TYPE_IAMGE);
mAdapter.addAll(getTextVirtualData(), VIEW_TYPE_TEXT);
mAdapter.addAll(getTextImageVirualData(), VIEW_TYPE_TEXT_IMAGE);
mAdapter.addAll(getRecordVirtualData(), VIEW_TYPE_CARD);
```

### ViewHolder

> 自定义 ViewHolder 需继承 BaseViewHolder<T>，如：

 ```java
class CardRecordHolder extends BaseViewHolder<Consumption> {

 //当使用MultiTypeAdapter时，务必加上此构造方法
 public CardRecordHolder(ViewGroup parent) {
     super(parent, R.layout.holder_consume);
 }

 @Override
 public void setData(Consumption object) {
     super.setData(object);
     name.setText("Demo");
     //UI绑定数据
 }

 @Override
 public void onInitializeView() {
     super.onInitializeView();
     name = findViewById(R.id.name);
     //初始化View
 }

 @Override
 public void onItemViewClick(Consumption object) {
     super.onItemViewClick(object);
     //点击事件
 }
}
 ```
 
 ### 设置item的展开和折叠

```
使用方法：
1)在viewHolder类中重写bind(int pos)方法
@Override
    protected void bind(int pos) {
        super.bind(pos);
        if (pos == mAdapter.getOpened()) {
            //展开
        } else {
            //折叠
        }
    }
2）在item的点击事件里处理item的刷新操作：
if (mAdapter.getOpened() == getLayoutPosition()) {
                    mAdapter.setOpened(-1);                               
                    mAdapter.notifyItemChanged(getLayoutPosition());
                } else {
                    mAdapter.setObject(object);
                    int oldOpened = mAdapter.getOpened();
                    mAdapter.setOpened(getLayoutPosition());
                    mAdapter.notifyItemChanged(oldOpened);
                    mAdapter.notifyItemChanged(mAdapter.getOpened());
                }
```


[详细用法请看Demo](https://github.com/llxdaxia/RecyclerView/tree/master/demo)

<img src="screenshot/RecyclerAdapter.png" width="270" height="480"/>
<img src="screenshot/MultiTypeAdapter.png" width="270" height="480"/>
