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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import data.Category;
import data.WorkBlock;
import data.WorkEntry;
import database.DatabaseUiCallback;
import ipc.RecyclerViewOnClickListener;
import util.DateUtils;

/**
 * Created by Florian on 22.06.2015.
 */
public class WorkBlockRecyclerAdapter extends AbstractItemAdapter {

    private static WorkEntry parent = null;
    private static List<WorkBlock> mData = null;
    private static Map<Integer, WorkBlock> deleteData = Collections.synchronizedMap(new TreeMap<Integer, WorkBlock>());
    private static Map<Integer, WorkBlock> undoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, WorkBlock>());

    private TextView mEmptyView;

    private Context mContext = null;
    private static RecyclerViewOnClickListener mOnItemClickListener;

    private static int mLastRestorePosition = 0;

    public WorkBlockRecyclerAdapter(@NonNull Context context, WorkEntry parentEntry, RecyclerViewOnClickListener onItemClickListener, @Nullable TextView emptyView){
        mContext = context;
        setEmptyView(emptyView);
        setData(parentEntry);
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public WorkBlockItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_work_block, viewGroup, false);
        return new WorkBlockItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        WorkBlock block = getData().get(i);

        ((WorkBlockItemViewHolder)viewHolder).title.setText(block.getTitle());

        int[] duration = DateUtils.getDurationBetweenDates(block.work_start, block.work_end);

        String displayTime = null;
        if(block._category_reference_id == Category.SICK_LEAVE){
            displayTime = mContext.getString(R.string.work_block_subtitle_at, duration[0], duration[1], DateUtils.getTime(block.work_start));
        }else{
            displayTime = mContext.getString(R.string.work_block_subtitle, duration[0], duration[1], DateUtils.getTime(block.work_start), DateUtils.getTime(block.work_end));
        }

        ((WorkBlockItemViewHolder)viewHolder).icon.setImageResource(block.getIconId());
        ((WorkBlockItemViewHolder)viewHolder).selector.setColorFilter(block.getCategory().color, PorterDuff.Mode.ADD);
        ((WorkBlockItemViewHolder)viewHolder).time.setText(displayTime);
        ((WorkBlockItemViewHolder)viewHolder).isSelected = false;
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    public void addItem(Context context, WorkBlock item, int position){

        if(position == -1){
            getData().add(mLastRestorePosition, item);
            notifyItemInserted(mLastRestorePosition);
        } else {
            getData().add(position, item);
            notifyItemInserted(position);
        }
        checkEmpty();
    }

    public void addItem(Context context, WorkBlock item){

        // If block with id already exists, remove the old version
        WorkBlock blockToRemove = null;
        for(WorkBlock block : getData()){
            if(block._id == item._id){
                blockToRemove = block;
            }
        }
        if(blockToRemove != null) {
            getData().remove(blockToRemove);
        }

        getData().add(mLastRestorePosition, item);
        notifyItemInserted(mLastRestorePosition);
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
        for(Map.Entry entry : undoDeleteData.entrySet()){
            addItem(context, (WorkBlock) entry.getValue(), (Integer) entry.getKey());
        }
        nullifyUndoDeleteData();
    }

    public void removeItem(Context context, DatabaseUiCallback callback, WorkBlock item){
        int position = getData().indexOf(item);
        if(position != -1){
            mLastRestorePosition = position;
            getData().remove(item);
            notifyItemRemoved(position);
        }

        checkEmpty();
    }

    public void removeAllSelected(Context context){

        for(Map.Entry entry : deleteData.entrySet()){

            WorkBlock item = (WorkBlock) entry.getValue();
            removeItem(context, null, item);
        }

        // Copy the delete data in case we want to restore the data
        undoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, WorkBlock>(deleteData));

        // Purge the original deleted data
        nullifyDeleteData();
    }

    protected void sort(){
        Collections.sort(getData());
    }

    private void setData(WorkEntry workEntry){

        parent = workEntry;
        mData = parent == null ? Collections.synchronizedList(new ArrayList<WorkBlock>()) : Collections.synchronizedList(parent.getWorkBlocks());
        sort();
        checkEmpty();
    }

    public List<WorkBlock> getData(){
        return mData;
    }

    public Map<Integer, WorkBlock> getDeleteData(){
        return deleteData;
    }

    public void nullifyDeleteData(){
        deleteData = Collections.synchronizedMap(new TreeMap<Integer, WorkBlock>());
    }

    public void nullifyUndoDeleteData(){
        undoDeleteData = Collections.synchronizedMap(new TreeMap<Integer, WorkBlock>());
    }

    public void purgeData(){
        mLastRestorePosition = 0;
        nullifyDeleteData();
        nullifyUndoDeleteData();
        uncheckData();
    }

    @Override
    public void uncheckData() {
        for(WorkBlock block : parent.getWorkBlocks()){
            block.setSelected(false);
        }
        super.notifyDataSetChanged();
    }

    public void swapData(WorkEntry parentEntry){
        setData(parentEntry);
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

    public static class WorkBlockItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        boolean isSelected = false;

        View root;
        ImageView icon, selector;
        TextView title;
        TextView time;

        WorkBlockItemViewHolder(View view) {
            super(view);

            root = view;
            icon = (ImageView) view.findViewById(R.id.icon);
            selector = (ImageView) view.findViewById(R.id.selector);
            title = (TextView) view.findViewById(R.id.title);
            time = (TextView) view.findViewById(R.id.subtitle);

            icon.setOnClickListener(this);
            selector.setOnClickListener(this);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.icon:
                case R.id.selector:
                    isSelected = !isSelected;
                    root.setSelected(isSelected);

                    if(isSelected) {
                        deleteData.put(this.getLayoutPosition(), mData.get(this.getLayoutPosition()));
                        icon.setImageResource(R.drawable.ic_check_black_24dp);
                    }else{
                        deleteData.remove(this.getLayoutPosition());
                        icon.setImageResource(mData.get(this.getLayoutPosition()).getIconId());
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
