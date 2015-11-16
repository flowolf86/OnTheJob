package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import adapter.WorkBlockRecyclerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import data.WorkBlock;
import data.WorkEntry;
import ipc.RecyclerViewOnClickListener;
import ui.base.BaseListFragment;
import util.DateUtils;

public class WorkBlockListFragment extends BaseListFragment implements View.OnClickListener, RecyclerViewOnClickListener {

    @Bind(R.id.add_block) FloatingActionButton mAddBlock;
    @Bind(R.id.empty_recycler) TextView mEmptyRecyclerView;

    public static final String FRAGMENT_TAG = "work_block_list_fragment";

    private static String ARG_WORK_ENTRY = "work_entry";
    private static String ARG_CUSTOM_TOOLBAR_TITLE = "custom_toolbar_title";

    public WorkBlockListFragment() { }

    public static WorkBlockListFragment newInstance(@Nullable WorkEntry workEntry, String customToolbarTitle) {

        if(workEntry == null){
            throw new IllegalArgumentException("WorkEntry may not be null.");
        }

        WorkBlockListFragment f = new WorkBlockListFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_WORK_ENTRY, workEntry);

        if(customToolbarTitle != null) {
            args.putString(ARG_CUSTOM_TOOLBAR_TITLE, customToolbarTitle);
        }

        f.setArguments(args);

        return f;
    }

    /*
        Interface
     */

    public interface WorkBlockListFragmentInterface {
        void onListBlockItemSelected(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock);
        void onAddWorkBlockRequest(@NonNull WorkEntry workEntry);
    }

    /*
        Fragment data
     */

    private WorkEntry getFragmentData() {
        return getArguments().getParcelable(ARG_WORK_ENTRY);
    }

    public void addWorkBlock(@NonNull WorkBlock block){
        ((WorkBlockRecyclerAdapter) super.getRecyclerAdapter()).addItem(getContext(), block);
    }

    /*
        Activity communication
     */

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_block_list, container, false);

        ButterKnife.bind(this, view);
        setOnClickListeners();
        setUpContentRecycler(view, getFragmentData());
        super.setToolbarTitle(getArguments().getString(ARG_CUSTOM_TOOLBAR_TITLE) == null ? getString(R.string.title_blocks, DateUtils.getDateShort(getFragmentData().getDate())) : getArguments().getString(ARG_CUSTOM_TOOLBAR_TITLE));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof FragmentActionModeInterface
                && activity instanceof FragmentBackHandlerInterface
                && activity instanceof WorkBlockListFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    @Override
    public void onStart() {
        super.setFragmentTag(FRAGMENT_TAG);
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /*
        Logic
     */

    private void setOnClickListeners() {
        mAddBlock.setOnClickListener(this);
    }

    private void setUpContentRecycler(View view, WorkEntry workEntry) {

        super.setRecyclerView((RecyclerView) view.findViewById(R.id.content_recycler));
        super.setRecyclerAdapter(new WorkBlockRecyclerAdapter(getContext(), workEntry, this, mEmptyRecyclerView));
        super.setContentRecyclerLayoutManager(new LinearLayoutManager(getContext()));
        super.setRecyclerViewItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

        ((WorkBlockListFragmentInterface)getActivity()).onListBlockItemSelected(getFragmentData(), (WorkBlock) super.getRecyclerAdapter().getData().get(position));
        ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
    }

    @Override
    public void recyclerViewListImageClicked(final View v, final int position) {
        super.handleActionMode();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.add_block:
                ((WorkBlockListFragmentInterface) getActivity()).onAddWorkBlockRequest(getFragmentData());
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onBackPressed() {

        super.getRecyclerAdapter().purgeData();
        ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
        return false;
    }
}
