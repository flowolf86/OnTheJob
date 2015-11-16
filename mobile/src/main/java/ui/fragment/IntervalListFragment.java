package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import adapter.IntervalRecyclerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.Category;
import data.Interval;
import ipc.RecyclerViewOnClickListener;
import ui.base.BaseListFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class IntervalListFragment extends BaseListFragment implements RecyclerViewOnClickListener, View.OnClickListener {

    @Bind(R.id.empty_recycler) TextView mEmptyRecyclerView;
    @Bind(R.id.add_entry) FloatingActionButton mAddEntryButton;

    private static final String FRAGMENT_TAG = "work_entry_container_list_fragment";

    private static String ARG_CUSTOM_TOOLBAR_TITLE = "custom_toolbar_title";
    private static String ARG_CATEGORY_MODE = "category_mode";

    public IntervalListFragment() { }

    public static IntervalListFragment newInstance(@Category.CategoryMode int mode, @Nullable String customToolbarTitle) {

        IntervalListFragment f = new IntervalListFragment();

        Bundle args = new Bundle();

        if(customToolbarTitle != null) {
            args.putString(ARG_CUSTOM_TOOLBAR_TITLE, customToolbarTitle);
        }
        args.putInt(ARG_CATEGORY_MODE, mode);

        f.setArguments(args);

        return f;
    }

    /*
        Interface
     */

    public interface IntervalListFragmentInterface {
        void onListIntervalItemSelected(Interval interval);
        void onPlanDaysOff(@Category.CategoryMode int mode);
    }

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interval_list, container, false);

        ButterKnife.bind(this, view);
        setUpContentRecycler(getFragmentData(), view);
        setToolbarTitle(getArguments().getString(ARG_CUSTOM_TOOLBAR_TITLE));
        setOnClickListener();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements(getActivity());
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof FragmentActionModeInterface
                && activity instanceof FragmentBackHandlerInterface
                && activity instanceof IntervalListFragmentInterface;

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

    private void setOnClickListener() {
        mAddEntryButton.setOnClickListener(this);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

        ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
        ((IntervalListFragmentInterface)getActivity()).onListIntervalItemSelected(((IntervalRecyclerAdapter) super.getRecyclerAdapter()).getData().get(position));
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
    private List<Interval> getFragmentData() {

        @Category.CategoryMode int mode = getArguments().getInt(ARG_CATEGORY_MODE);
        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        List<Interval> dataSet = dataCacheHelper.getIntervalsForMode(mode);
        return dataSet == null ? new ArrayList<Interval>() : dataSet;
    }

    private void setUpContentRecycler(List<Interval> dataSet, View view) {

        super.setRecyclerView((RecyclerView) view.findViewById(R.id.content_recycler));
        super.setRecyclerAdapter(new IntervalRecyclerAdapter(getContext(), dataSet, this, mEmptyRecyclerView));
        super.setContentRecyclerLayoutManager(new LinearLayoutManager(getContext()));
        super.setRecyclerViewItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.add_entry:
                @Category.CategoryMode int mode = getArguments().getInt(ARG_CATEGORY_MODE);
                ((IntervalListFragment.IntervalListFragmentInterface)getActivity()).onPlanDaysOff(mode);
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