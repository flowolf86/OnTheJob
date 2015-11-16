package ipc;

import android.view.View;

/**
 * Created by Florian on 22.06.2015.
 */
public interface RecyclerViewOnClickListener {

    void recyclerViewListClicked(View v, int position);
    void recyclerViewListImageClicked(View v, int position);
}
