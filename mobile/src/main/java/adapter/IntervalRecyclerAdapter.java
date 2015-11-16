package adapter;

import android.content.Context;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cache.DataCacheHelper;
import data.Category;
import data.Interval;
import data.WorkEntryType;
import database.DatabaseUiCallback;
import ipc.RecyclerViewOnClickListener;
import util.DateUtils;

/**
 * Created by Florian on 22.06.2015.
 */
public class IntervalRecyclerAdapter extends AbstractItemAdapter {

    private static List<Interval> mData = null;
    private static Map<Integer, Interval> mDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Interval>());
    private static Map<Integer, Interval> mUndoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Interval>());

    private Context mContext = null;

    private TextView mEmptyView;

    private static RecyclerViewOnClickListener mOnItemClickListener;

    private static int mLastRestorePosition = 0;

    private DataCacheHelper mDataCacheHelper = null;

    public IntervalRecyclerAdapter(@NonNull Context context, List<Interval> intervalList, RecyclerViewOnClickListener onItemClickListener, @Nullable TextView emptyView){
        mContext = context;
        setEmptyView(emptyView);
        setData(intervalList);
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public IntervalItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_icon_title_subtitle, viewGroup, false);
        return new IntervalItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        final int daysBetween = DateUtils.getNumberOfDays(mData.get(i).getStartDate(), mData.get(i).getEndDate());
        final String subtitle = mContext.getString(R.string.list_interval_subtitle, DateUtils.getDateShorter(mData.get(i).getStartDate()), DateUtils.getDateShorter(mData.get(i).getEndDate()), daysBetween);

        ((IntervalItemViewHolder)viewHolder).entryTitle.setText(mData.get(i).getTitle());
        ((IntervalItemViewHolder)viewHolder).entryDescription.setText(subtitle);
        ((IntervalItemViewHolder)viewHolder).entryIcon.setImageResource(mData.get(i).getCategory().getId() == Category.VACATION ? WorkEntryType.VACATION_ICON_ID : WorkEntryType.SICK_LEAVE_ICON_ID);
        ((IntervalItemViewHolder)viewHolder).isSelected = false;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private DataCacheHelper getDataCacheHelper(Context context){
        if(mDataCacheHelper == null){
            mDataCacheHelper = new DataCacheHelper(context);
        }
        return mDataCacheHelper;
    }

    public void addItem(Context context, Interval item, int position){

        if(position == -1){
            mData.add(mLastRestorePosition, item);
            notifyItemInserted(mLastRestorePosition);
        } else {
            mData.add(position, item);
            notifyItemInserted(position);
        }

        getDataCacheHelper(context).addNewInterval(item, null);
        checkEmpty();
    }

    public void addItem(Context context, Interval item){
        mData.add(mLastRestorePosition, item);
        notifyItemInserted(mLastRestorePosition);
        getDataCacheHelper(context).addNewInterval(item, null);
        checkEmpty();
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
            addItem(context, (Interval) entry.getValue(), (Integer) entry.getKey());
        }
        nullifyUndoDeleteData();
    }

    public void removeItem(Context context, DatabaseUiCallback callback, Interval item){
        int position = mData.indexOf(item);
        if(position != -1){
            mLastRestorePosition = position;
            mData.remove(item);
            notifyItemRemoved(position);
            getDataCacheHelper(context).deleteInterval(item, callback);
        }

        checkEmpty();
    }

    public void removeAllSelected(Context context){

        for(Map.Entry entry : mDeleteData.entrySet()){

            Interval item = (Interval) entry.getValue();
            removeItem(context, null, item);
            getDataCacheHelper(context).deleteInterval(item, null);
        }

        // Copy the delete mData in case we want to restore the mData
        mUndoDeleteData = Collections.synchronizedMap(new TreeMap<>(mDeleteData));

        // Purge the original deleted mData
        nullifyDeleteData();
    }

    private void setData(List<Interval> data){

        mData = data == null ? Collections.synchronizedList(new ArrayList<Interval>()) : Collections.synchronizedList(data);
        sort();
        checkEmpty();
    }

    public List<Interval> getData(){
        return mData == null ? Collections.synchronizedList(new ArrayList<Interval>()) : mData;
    }

    public Map<Integer, Interval> getDeleteData(){
        return mDeleteData;
    }

    public void nullifyDeleteData(){
        mDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Interval>());
    }

    public void nullifyUndoDeleteData(){
        mUndoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, Interval>());
    }

    protected void sort(){
        Collections.sort(getData());
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

    public void purgeData(){
        mLastRestorePosition = 0;
        nullifyDeleteData();
        nullifyUndoDeleteData();
        uncheckData();
    }

    @Override
    public void uncheckData() {
        for(Interval interval : mData){
            interval.setSelected(false);
        }
        nullifyDeleteData();
        super.notifyDataSetChanged();
    }

    public void swapData(List<Interval> data){
        setData(data);
        nullifyDeleteData();
        this.notifyDataSetChanged();
    }

    public static class IntervalItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        boolean isSelected = false;

        View root;
        ImageView entryIcon;
        TextView entryTitle;
        TextView entryDescription;

        IntervalItemViewHolder(View view) {
            super(view);

            root = view;
            entryIcon = (ImageView) view.findViewById(R.id.icon);
            entryTitle = (TextView) view.findViewById(R.id.title);
            entryDescription = (TextView) view.findViewById(R.id.subtitle);

            entryIcon.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.icon:
                    isSelected = !isSelected;
                    root.setSelected(isSelected);

                    if(isSelected) {
                        mDeleteData.put(this.getLayoutPosition(), mData.get(this.getLayoutPosition()));
                        entryIcon.setImageResource(WorkEntryType.CHECK_ICON_ID);
                    }else{
                        mDeleteData.remove(this.getLayoutPosition());
                        entryIcon.setImageResource(WorkEntryType.MANUAL_ICON_ID);
                    }

                    mOnItemClickListener.recyclerViewListImageClicked(v, this.getLayoutPosition());
                    break;
                default:
                    mOnItemClickListener.recyclerViewListClicked(v, this.getLayoutPosition());
                    break;
            }
        }
    }
}
