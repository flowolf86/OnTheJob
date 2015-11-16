package ui.base;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.florianwolf.onthejob.R;

import java.util.concurrent.atomic.AtomicBoolean;

import adapter.AbstractItemAdapter;

/**
 * Created by Florian on 25.06.2015.
 */
public abstract class BaseListFragment extends BaseFragment {

    private static final int ANIMATION_REMOVE_DURATION = 300;
    private static final int ANIMATION_RESTORE_DURATION = 3500;

    RecyclerView mContentRecycler = null;
    LinearLayoutManager mContentRecyclerLayoutManager = null;
    private AbstractItemAdapter mContentRecyclerAdapter = null;

    /*
        RecyclerView
     */
    public void setRecyclerView(RecyclerView contentRecylcer){
        mContentRecycler = contentRecylcer;
    }

    public <T extends AbstractItemAdapter> void setRecyclerAdapter(T object){

        if(mContentRecycler == null){
            throw new IllegalArgumentException("Please set RecyclerView first.");
        }

        mContentRecyclerAdapter = object;
        mContentRecycler.setAdapter(mContentRecyclerAdapter);
    }

    public void setContentRecyclerLayoutManager(LinearLayoutManager layoutManager){

        if(mContentRecycler == null){
            throw new IllegalArgumentException("Please set RecyclerView first.");
        }

        mContentRecyclerLayoutManager = layoutManager;
        mContentRecycler.setLayoutManager(mContentRecyclerLayoutManager);
        mContentRecycler.setHasFixedSize(true);
    }

    public <T extends RecyclerView.ItemAnimator> void setRecyclerViewItemAnimator(T animator){

        if(mContentRecycler == null){
            throw new IllegalArgumentException("Please set RecyclerView first.");
        }

        animator.setRemoveDuration(ANIMATION_REMOVE_DURATION);
        mContentRecycler.setItemAnimator(animator);
    }

    public @Nullable RecyclerView getRecyclerView(){
        return mContentRecycler;
    }

    public AbstractItemAdapter getRecyclerAdapter(){
        return mContentRecyclerAdapter;
    }

    /*
        ActionMode
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            ((FragmentActionModeInterface)getActivity()).onFragmentSetActionModeRequest(mode);

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.actionmenu_delete, menu);

            ((FragmentActionModeInterface)getActivity()).onFragmentSetActionModeTitleRequest(getString(R.string.selected_elements, mContentRecyclerAdapter.getDeleteData().size()));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            final AtomicBoolean allowNullify = new AtomicBoolean(true);

            switch (item.getItemId()) {
                case R.id.action_delete_all:

                    int numberOfItems = mContentRecyclerAdapter.getDeleteData().size();
                    mContentRecyclerAdapter.removeAllSelected(getActivity());

                    Snackbar snackbar = Snackbar.make(mContentRecycler, numberOfItems == 1 ? getString(R.string.item_deleted, numberOfItems) : getString(R.string.items_deleted, numberOfItems), Snackbar.LENGTH_LONG);
                    snackbar.setAction(getString(R.string.undo).toUpperCase(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            allowNullify.set(false);
                            mContentRecyclerAdapter.nullifyDeleteData();
                            mContentRecyclerAdapter.restoreAllDeleted(getActivity());
                        }
                    });
                    snackbar.show();

                    // Cleanup after the restore option expires
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (allowNullify.get()) {
                                mContentRecyclerAdapter.nullifyUndoDeleteData();
                                allowNullify.set(true);
                            }
                        }
                    }, ANIMATION_RESTORE_DURATION);

                    ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            getRecyclerAdapter().uncheckData();
            ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
        }
    };

    public ActionMode.Callback getActionModeCallback(){
        return mActionModeCallback;
    }

    public void handleActionMode(){

        ((FragmentActionModeInterface)getActivity()).onFragmentStartActionModeRequest(getActionModeCallback());
        ((FragmentActionModeInterface)getActivity()).onFragmentSetActionModeTitleRequest(getString(R.string.selected_elements, getRecyclerAdapter().getDeleteData().size()));

        if(getRecyclerAdapter().getDeleteData().size() == 0){
            getRecyclerAdapter().purgeData();
            ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
        }
    }

    /*
        Convenience
     */

    /** Set toolbar title of the parent activity
     *
      * @param customTitle
      */

    protected void setToolbarTitle(String customTitle) {
        super.setToolbarTitle(customTitle);
    }

    /*
        Tagging
     */

    public void setFragmentTag(String tag){
        super.CURRENT_FRAGMENT_TAG = tag;
    }
}

