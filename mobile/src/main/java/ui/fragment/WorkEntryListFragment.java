package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.ArrayList;
import java.util.List;

import adapter.WorkEntryRecyclerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.WorkEntry;
import ipc.RecyclerViewOnClickListener;
import ui.base.BaseListFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class WorkEntryListFragment extends BaseListFragment implements RecyclerViewOnClickListener {

    @Bind(R.id.empty_recycler) TextView mEmptyRecyclerView;

    public static final String FRAGMENT_TAG = "work_entry_list_fragment";

    private static String ARG_WORK_ENTRY_LIST = "work_entry_list";
    private static String ARG_CUSTOM_TOOLBAR_TITLE = "custom_toolbar_title";

    public WorkEntryListFragment() { }

    public static WorkEntryListFragment newInstance(@Nullable ArrayList<WorkEntry> workEntryList, @Nullable String customToolbarTitle) {

        WorkEntryListFragment f = new WorkEntryListFragment();

        Bundle args = new Bundle();

        if(workEntryList != null) {
            args.putParcelableArrayList(ARG_WORK_ENTRY_LIST, workEntryList);
        }
        if(customToolbarTitle != null) {
            args.putString(ARG_CUSTOM_TOOLBAR_TITLE, customToolbarTitle);
        }

        f.setArguments(args);

        return f;
    }

    /*
        Interface
     */

    public interface WorkEntryListFragmentInterface {
        void onListEntryItemSelected(WorkEntry workEntry);
    }

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_entry_list, container, false);

        ButterKnife.bind(this, view);
        setUpContentRecycler(getFragmentData(), view);
        setToolbarTitle(getArguments().getString(ARG_CUSTOM_TOOLBAR_TITLE));

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
                && activity instanceof WorkEntryListFragmentInterface;

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

    @Override
    public void recyclerViewListClicked(View v, int position) {

        ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
        ((WorkEntryListFragmentInterface)getActivity()).onListEntryItemSelected(((WorkEntryRecyclerAdapter) super.getRecyclerAdapter()).getData().get(position));
    }

    @Override
    public void recyclerViewListImageClicked(final View v, final int position) {
        super.handleActionMode();
    }

    /**
     * Checks if we have data in the intent to display. If not
     * we will just fallback and display all entries
     *
     * @return
     */
    private List<WorkEntry> getFragmentData() {

        List<WorkEntry> dataSet = getArguments().getParcelableArrayList(ARG_WORK_ENTRY_LIST);
        return dataSet == null ? getDefaultDataSet() : dataSet;
    }

    private List<WorkEntry> getDefaultDataSet(){
        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        return dataCacheHelper.getAllWorkEntries();
    }

    private void setUpContentRecycler(List<WorkEntry> dataSet, View view) {

        super.setRecyclerView((RecyclerView) view.findViewById(R.id.content_recycler));
        super.setRecyclerAdapter(new WorkEntryRecyclerAdapter(getContext(), dataSet, this, mEmptyRecyclerView));
        super.setContentRecyclerLayoutManager(new LinearLayoutManager(getContext()));
        super.setRecyclerViewItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onBackPressed() {

        super.getRecyclerAdapter().purgeData();
        ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
        return false;
    }
}
