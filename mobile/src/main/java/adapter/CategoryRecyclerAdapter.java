package adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cache.CategoryCacheHelper;
import data.Category;
import database.DatabaseUiCallback;
import ipc.RecyclerViewOnClickListener;
import util.ColorUtils;

/**
 * Created by Florian on 22.06.2015.
 */
public class CategoryRecyclerAdapter extends AbstractItemAdapter{

    private static List<Category> mData = null;
    private static Map<Integer, Category> mDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Category>());
    private static Map<Integer, Category> mUndoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Category>());

    private TextView mEmptyView;

    private static RecyclerViewOnClickListener onItemClickListener;

    private static int mLastRestorePosition = 0;

    private CategoryCacheHelper mCategoryCacheHelper = null;

    public CategoryRecyclerAdapter(List<Category> categoryList, RecyclerViewOnClickListener onItemClickListener, @NonNull TextView emptyView){
        setEmptyView(emptyView);
        setData(categoryList);
        CategoryRecyclerAdapter.onItemClickListener = onItemClickListener;
    }

    @Override
    public CategoryItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_category, viewGroup, false);
        return new CategoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        int index = viewHolder.getLayoutPosition();

        ((CategoryItemViewHolder)viewHolder).categoryName.setText(getData().get(index).name);
        ((CategoryItemViewHolder)viewHolder).categoryDescription.setText(getData().get(index).description);
        ((CategoryItemViewHolder)viewHolder).selector.setColorFilter(getData().get(index).color, PorterDuff.Mode.ADD);

        boolean isDarkColor = ColorUtils.isDarkColor(getData().get(index).color);
        boolean isSelected = getData().get(index).getSelected();

        int drawableId = 0;

        //TODO Color the drawable instead of having two separate ones
        if(isSelected) {
            drawableId = isDarkColor ? R.drawable.ic_check_white_24dp : R.drawable.ic_check_black_24dp;
        }else{
            drawableId = isDarkColor ? R.drawable.fw_category_default : R.drawable.fw_category_default; //TODO
        }
        ((CategoryItemViewHolder)viewHolder).icon.setImageResource(drawableId); //TODO get from blob byte[]
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    private CategoryCacheHelper getCategoryCacheHelper(){
        if(mCategoryCacheHelper == null){
            mCategoryCacheHelper = new CategoryCacheHelper();
        }
        return mCategoryCacheHelper;
    }

    public void addItem(Context context, Category item, int position){

        if(position == -1){
            addData(mLastRestorePosition, item);
            notifyItemInserted(mLastRestorePosition);
        } else {
            addData(position, item);
            notifyItemInserted(position);
        }

        getCategoryCacheHelper().addCategoryInDatabase(context, item);
    }

    public void addItem(Context context, Category item){
        getData().add(mLastRestorePosition, item);
        notifyItemInserted(mLastRestorePosition);
        getCategoryCacheHelper().addCategoryInDatabase(context, item);
    }

    /**
     * This is where the undo magic happens
     * @param context
     */
    public void restoreAllDeleted(Context context){
        // This is where we need TreeMap for the keys to be sorted from 0 onwards.
        // If we would not start with 0 we could run into a NullPointerException here when
        // inserting at positions > 0.
        for(Map.Entry entry : mUndoDeleteData.entrySet()){
            addItem(context, (Category) entry.getValue(), (Integer) entry.getKey());
        }
        nullifyUndoDeleteData();
    }

    public void removeItem(Context context, DatabaseUiCallback callback, Category item){
        int position = getData().indexOf(item);
        if(position != -1){
            mLastRestorePosition = position;
            getData().remove(item);
            notifyItemRemoved(position);
            getCategoryCacheHelper().removeCategoryInDatabase(context, item);
        }
    }

    public void removeAllSelected(Context context){

        for(Map.Entry entry : mDeleteData.entrySet()){

            Category item = (Category) entry.getValue();
            getCategoryCacheHelper().removeCategoryInDatabase(context, item);
            removeItem(context, null, item);
        }

        // Copy the delete mData in case we want to restore the mData
        mUndoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Category>(mDeleteData));

        // Purge the original deleted mData
        nullifyDeleteData();
    }

    protected void sort(){
        Collections.sort(getData(), Category.CategoryNameComperator);
    }

    private void setData(List<Category> data){

        mData = data == null ? Collections.synchronizedList(new ArrayList<Category>()) : Collections.synchronizedList(data);

        // Remove system categories here
        for (Iterator<Category> iterator = getData().iterator(); iterator.hasNext();) {
            Category category = iterator.next();
            if (category.category_type == Category.CATEGORY_TYPE_SYSTEM) {
                iterator.remove();
            }
        }

        sort();

        checkEmpty();
    }

    public List<Category> getData(){
        return mData;
    }

    private void addData(int position, Category data){
        getData().add(position, data);
        sort();
        checkEmpty();
    }

    public Map<Integer, Category> getDeleteData(){
        return mDeleteData;
    }

    public void nullifyDeleteData(){
        mDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Category>());
    }

    public void nullifyUndoDeleteData(){
        mUndoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Category>());
    }

    @Override
    public void purgeData() {
        mLastRestorePosition = 0;
        nullifyDeleteData();
        nullifyUndoDeleteData();
        uncheckData();
    }

    @Override
    public void uncheckData() {
        for(Category category : getData()){
            category.setSelected(false);
        }
        super.notifyDataSetChanged();
    }

    public void swapData(List<Category> data){
        setData(data == null ? Collections.synchronizedList(new ArrayList<Category>()) : Collections.synchronizedList(data));
        nullifyDeleteData();
        this.notifyDataSetChanged();
    }

    @Override
    protected void checkEmpty() {

        if(getEmptyView() != null){
            if(getData().size() > 0){
                getEmptyView().setVisibility(View.GONE);
            } else {
                getEmptyView().setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void setEmptyView(@Nullable TextView emptyView) {
        mEmptyView = emptyView;
    }

    @Override
    protected @Nullable TextView getEmptyView() {
        return  mEmptyView;
    }

    public static class CategoryItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        View root;
        ImageView icon, selector;
        TextView categoryName;
        TextView categoryDescription;

        CategoryItemViewHolder(View view) {
            super(view);

            root = view;
            icon = (ImageView) view.findViewById(R.id.icon);
            selector = (ImageView) view.findViewById(R.id.selector);
            categoryName = (TextView) view.findViewById(R.id.title);
            categoryDescription = (TextView) view.findViewById(R.id.subtitle);

            icon.setOnClickListener(this);
            selector.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.icon:
                case R.id.selector:

                    boolean isSelected = !mData.get(this.getLayoutPosition()).getSelected();
                    root.setSelected(isSelected);

                    boolean isDarkColor = ColorUtils.isDarkColor(mData.get(this.getLayoutPosition()).color);
                    if(isSelected) {
                        mDeleteData.put(this.getLayoutPosition(), mData.get(this.getLayoutPosition()));
                        int drawableId = isDarkColor ? R.drawable.ic_check_white_24dp : R.drawable.ic_check_black_24dp;
                        icon.setImageResource(drawableId);
                    }else{
                        mDeleteData.remove(this.getLayoutPosition());
                        int drawableId = isDarkColor ? R.drawable.fw_category_work_day : R.drawable.fw_category_work_day;
                        icon.setImageResource(drawableId);
                    }

                    mData.get(this.getLayoutPosition()).setSelected(isSelected);
                    onItemClickListener.recyclerViewListImageClicked(v, this.getLayoutPosition());
                    break;
                default:
                    onItemClickListener.recyclerViewListClicked(v, this.getLayoutPosition());
                    break;
            }
        }
    }
}
