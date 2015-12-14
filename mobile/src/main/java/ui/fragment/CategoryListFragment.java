package ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.List;

import adapter.CategoryRecyclerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import cache.CategoryCacheHelper;
import data.Category;
import data.WorkBlock;
import data.factory.CategoryFactory;
import ipc.RecyclerViewOnClickListener;
import support.AppBroadcaster;
import ui.base.BaseListFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class CategoryListFragment extends BaseListFragment implements View.OnClickListener, RecyclerViewOnClickListener {

    public static final String FRAGMENT_TAG = "category_list_fragment";

    private static final String ARG_WORK_BLOCK = "arg_work_block";

    private WorkBlock mWorkBlock;

    private boolean mIsRegistered = false;
    private final BroadcastReceiver mCategoryCacheUpdateReceiver = new CategoryCacheUpdateReceiver();
    private final IntentFilter mCategoryCacheUpdateFilter = new IntentFilter(AppBroadcaster.BC_CATEGORY_CACHE_UPDATED);

    @Bind(R.id.content_recycler) RecyclerView mRecyclerView;
    @Bind(R.id.empty_recycler) TextView mEmptyRecyclerView;
    @Bind(R.id.work_day_category_selector) ImageView mWorkDayColor;
    @Bind(R.id.vacation_category_selector) ImageView mVacationColor;
    @Bind(R.id.sick_leave_category_selector) ImageView mSickLeaveColor;
    @Bind(R.id.add_category) FloatingActionButton mAddCategoryFab;
    @Bind(R.id.category_regular) RelativeLayout mCategoryRegular;
    @Bind(R.id.category_vacation) RelativeLayout mCategoryVacation;
    @Bind(R.id.category_sick_leave) RelativeLayout mCategorySickLeave;

    /*
        Interface
     */
    public interface CategoryListFragmentInterface{
        void onCategoryCreate();
        void onCategoryEdit(@NonNull Category category);
    }

    public CategoryListFragment() { }

    public static CategoryListFragment newInstance() {

        return new CategoryListFragment();
    }

    public static CategoryListFragment newInstance(WorkBlock workBlock) {

        final CategoryListFragment categoryListFragment = new CategoryListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_WORK_BLOCK, workBlock);
        categoryListFragment.setArguments(bundle);
        return categoryListFragment;
    }

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);

        ButterKnife.bind(this, view);

        // Register here first time to get the lazy init broadcast in time
        registerBroadcastReceivers();

        loadWorkBlock();
        setDefaults();
        setOnClickListeners();
        requestCategoryData();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements(getActivity());
    }

    @Override
    public boolean onBackPressed() {

        if(getActivity() != null) {
            if (super.getRecyclerAdapter() != null) {
                super.getRecyclerAdapter().purgeData();
            }
            ((FragmentActionModeInterface) getActivity()).onFragmentFinishActionModeRequest();
        }
        return false;
    }

    @Override
    public void onStart() {
        super.setFragmentTag(FRAGMENT_TAG);
        super.onStart();
        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarTitleChange(getString(R.string.title_categories));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceivers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcastReceivers();
        ButterKnife.unbind(this);
    }

    /*
        Logic
     */

    private void loadWorkBlock() {

        if(getArguments() != null) {
            mWorkBlock = getArguments().getParcelable(ARG_WORK_BLOCK);
        }
    }

    private boolean isFragmentInSelectMode() {
        return mWorkBlock != null;
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentActionModeInterface
                && activity instanceof FragmentBackHandlerInterface
                && activity instanceof CategoryListFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private synchronized void registerBroadcastReceivers() {

        if(!mIsRegistered) {
            AppBroadcaster.registerReceiver(getContext(), mCategoryCacheUpdateReceiver, mCategoryCacheUpdateFilter);
            mIsRegistered = true;
        }
    }

    private synchronized void unregisterBroadcastReceivers() {

        if(mIsRegistered){
            AppBroadcaster.unregisterReceiver(getContext(), mCategoryCacheUpdateReceiver);
            mIsRegistered = false;
        }
    }

    /**
     * We request category data here and receive it via broadcast when the background thread has
     * finished its db query
     */
    private void requestCategoryData() {
        CategoryCacheHelper.getInstance().refreshCategoriesFromDatabase(getContext());
    }

    private void setDefaults() {

        super.setRecyclerView(mRecyclerView);
        mAddCategoryFab.setEnabled(false);

        mWorkDayColor.setColorFilter(CategoryFactory.getWorkDayCategory().color, PorterDuff.Mode.ADD);
        mVacationColor.setColorFilter(CategoryFactory.getVacationCategory().color, PorterDuff.Mode.ADD);
        mSickLeaveColor.setColorFilter(CategoryFactory.getSickLeaveCategory().color, PorterDuff.Mode.ADD);
    }

    private void setOnClickListeners() {
        mAddCategoryFab.setOnClickListener(this);

        if(isFragmentInSelectMode()){
            mCategoryRegular.setOnClickListener(this);
            mCategoryVacation.setOnClickListener(this);
            mCategorySickLeave.setOnClickListener(this);
        }
    }

    private void setUpContentRecycler(List<Category> dataSet) {

        super.setRecyclerAdapter(new CategoryRecyclerAdapter(dataSet, this, mEmptyRecyclerView));
        super.setContentRecyclerLayoutManager(new LinearLayoutManager(getContext()));
        super.setRecyclerViewItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.add_category:
                ((CategoryListFragmentInterface)getActivity()).onCategoryCreate();
                ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();
                break;
            case R.id.category_regular:
                mWorkBlock.setCategory(CategoryFactory.getWorkDayCategory());
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            case R.id.category_vacation:
                mWorkBlock.setCategory(CategoryFactory.getVacationCategory());
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            case R.id.category_sick_leave:
                mWorkBlock.setCategory(CategoryFactory.getSickLeaveCategory());
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            default:
                break;
        }
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

        final Category category = ((CategoryRecyclerAdapter)CategoryListFragment.super.getRecyclerAdapter()).getData().get(position);

        ((FragmentActionModeInterface)getActivity()).onFragmentFinishActionModeRequest();

        if(isFragmentInSelectMode()){
            mWorkBlock.setCategory(category);
            ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
        } else {
            ((CategoryListFragmentInterface)getActivity()).onCategoryEdit(category);
        }
    }

    @Override
    public void recyclerViewListImageClicked(final View v, final int position) {
        super.handleActionMode();
    }

    private class CategoryCacheUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            mAddCategoryFab.setEnabled(true);

            // TODO This refreshes from DB although we have a cache in place
            setUpContentRecycler(CategoryCacheHelper.getInstance().getCategories());

            // Update recycler view
            /*if(getRecyclerAdapter() != null){
                ((CategoryRecyclerAdapter)CategoryListFragment.super.getRecyclerAdapter()).swapData(CategoryCacheHelper.getInstance().getCategories());
            }else{
                // Lazy init by broadcast
                setUpContentRecycler(CategoryCacheHelper.getInstance().getCategories());
            }*/
        }
    }
}
