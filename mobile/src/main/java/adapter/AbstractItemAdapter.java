package adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by Florian on 27.06.2015.
 */
public abstract class AbstractItemAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>{

    public abstract <G extends Parcelable> List<G> getData();
    public abstract <G extends Parcelable> Map<Integer, G> getDeleteData();
    public abstract void removeAllSelected(Context context);
    public abstract void restoreAllDeleted(Context context);
    public abstract void nullifyDeleteData();
    public abstract void nullifyUndoDeleteData();
    public abstract void purgeData();
    public abstract void uncheckData();
    protected abstract void sort();
    protected abstract void checkEmpty();
    protected abstract void setEmptyView(@Nullable TextView emptyView);
    protected abstract @Nullable TextView getEmptyView();
}