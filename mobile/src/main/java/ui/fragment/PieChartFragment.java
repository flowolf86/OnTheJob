package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.CategoryCacheHelper;
import cache.DataCacheHelper;
import data.Category;
import data.WorkBlock;
import data.WorkEntry;
import ui.base.BaseFragment;
import ui.dialog.DatePickerFragment;
import util.DateUtils;
import util.MapUtil;

/**
 * Created by Florian on 22.06.2015.
 *
 * This class is WIP
 */
public class PieChartFragment extends BaseFragment implements View.OnClickListener, DatePickerFragment.DatePickerCallback{

    public static final String FRAGMENT_TAG = "pie_chart_fragment";
    private static final String START_PICKER_TAG = "START";
    private static final String END_PICKER_TAG = "END";

    private static final int START_DATE_REQUEST_CODE = 100;
    private static final int END_DATE_REQUEST_CODE = 200;

    @Bind(R.id.chart) PieChart mChart;
    @Bind(R.id.start_date) TextView mStart;
    @Bind(R.id.end_date) TextView mStop;
    @Bind(R.id.category_details_container) LinearLayout mCategoryDetailsContainer;

    private long mCurrentStartDate;
    private long mCurrentEndDate;

    public PieChartFragment() { }

    public static PieChartFragment newInstance(){

        return new PieChartFragment();
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        ButterKnife.bind(this, view);
        initializeChart();
        setOnClickListeners();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Logic
     */

    private void setOnClickListeners() {
        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private void initializeChart() {

        mChart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_gray));
        mChart.setCenterText(getString(R.string.pie_chart_description));
        mChart.setHoleRadius(50f);
        mChart.setTransparentCircleRadius(55f);
        mChart.setHoleColorTransparent(false);
        mChart.setDrawHoleEnabled(true);
        mChart.setUsePercentValues(true);
        mChart.setHoleColor(ContextCompat.getColor(getContext(), R.color.background_gray));

        mChart.getLegend().setEnabled(false);
        mChart.setDescription(null);
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(false);
        mChart.setTouchEnabled(false);
        mChart.setHighlightPerTapEnabled(false);
        mChart.setOnChartValueSelectedListener(null);
        mChart.highlightValues(null);

        mCurrentStartDate = new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay().getMillis();
        mCurrentEndDate = new DateTime().dayOfMonth().withMaximumValue().getMillis();

        setStartLabel(mCurrentStartDate);
        setEndLabel(mCurrentEndDate);

        setChartDataAsync(mCurrentStartDate, mCurrentEndDate);
    }

    private void setChartDataAsync(final long startDate, final long endDate) {

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        CategoryCacheHelper categoryCacheHelper = new CategoryCacheHelper();

        final List<WorkEntry> workEntryList = dataCacheHelper.getAllWorkEntries();
        final List<Category> categoryList = categoryCacheHelper.getCategories();

        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<WorkEntry> selectedList = getFilteredList(startDate, endDate, workEntryList);

                Map<Category, Long> categoryToTimeReference = new HashMap<>();

                // Create an entry in the hash set for each category
                for(Category category : categoryList){
                    categoryToTimeReference.put(category, null);
                }

                long categoryTotalTime = 0L;

                // Calculate the total time per category in our work entries
                for(Category category : categoryList){

                    long thisCategoryTime = 0L;

                    for(WorkEntry workEntry : selectedList) {
                        for (WorkBlock workBlock : workEntry.getWorkBlocks()) {
                            if(category.getId() == workBlock.getCategory().getId()){
                                thisCategoryTime += (workBlock.getWorkEnd() - workBlock.getWorkStart());
                            }
                        }
                    }

                    for(HashMap.Entry entry : categoryToTimeReference.entrySet()){
                        if(((Category) entry.getKey()).getId() == category.getId()){
                            entry.setValue(new Long(thisCategoryTime));
                            categoryTotalTime += thisCategoryTime;
                            break;
                        }
                    }
                }

                // Set the data in a pie chart readable format
                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();

                categoryToTimeReference = MapUtil.sortByValue(categoryToTimeReference);

                final long total = categoryTotalTime;
                int counter = 0;
                for(HashMap.Entry entry : categoryToTimeReference.entrySet()){

                    final Category category = (Category)entry.getKey();
                    final long totalCategoryTime = (Long)entry.getValue();

                    if(totalCategoryTime == 0){
                        continue;
                    }

                    entries.add(new Entry(totalCategoryTime, counter++));
                    labels.add(category.getName());
                    colors.add(category.color);

                    // Display top 5
                    if(counter <= 5) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = new TextView(getContext());
                                textView.setText(category.getName() + ", " + totalCategoryTime + ", " + ((double) totalCategoryTime / (double) total) * 100. + "%");
                                mCategoryDetailsContainer.addView(textView);
                            }
                        });
                    }
                }

                final PieDataSet tempPieData = new PieDataSet(entries, null);
                tempPieData.setColors(colors);
                tempPieData.setSliceSpace(1f);
                tempPieData.setValueFormatter(new PercentFormatter());
                tempPieData.setValueTextSize(12f);
                final PieData finalPieData = new PieData(labels, tempPieData);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        mChart.setData(finalPieData);
                        mChart.invalidate();
                    }
                });
            }
        }).start();
    }

    /**
     * Returns the work entries between start and end date
     * @param startDate
     * @param endDate
     * @param unfilteredList
     * @return
     */
    private List<WorkEntry> getFilteredList(long startDate, long endDate, List<WorkEntry> unfilteredList) {

        final List<WorkEntry> resultList = new ArrayList<>();

        for(WorkEntry entry : unfilteredList){
            if(DateUtils.isSameDay(entry.getDate(), startDate) || DateUtils.isSameDay(entry.getDate(), endDate) || (entry.getDate() >= startDate && entry.getDate() <= endDate)) {
                resultList.add(entry);
            }
        }

        return resultList;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.start_date:
                DatePickerFragment startPickerFragment = DatePickerFragment.newInstance(mCurrentStartDate);
                startPickerFragment.setTargetFragment(this, START_DATE_REQUEST_CODE);
                startPickerFragment.show(getChildFragmentManager(), START_PICKER_TAG);
                break;
            case R.id.end_date:
                DatePickerFragment endPickerFragment = DatePickerFragment.newInstance(mCurrentEndDate);
                endPickerFragment.setTargetFragment(this, END_DATE_REQUEST_CODE);
                endPickerFragment.show(getChildFragmentManager(), END_PICKER_TAG);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDatePickerComplete(long timestamp, int targetRequestCode) {

        switch (targetRequestCode){
            case START_DATE_REQUEST_CODE:
                mCurrentStartDate = timestamp;
                setStartLabel(timestamp);
                break;
            case END_DATE_REQUEST_CODE:
                mCurrentEndDate = timestamp;
                setEndLabel(timestamp);
                break;
            default:
                break;
        }

        verifyDatesAndDisplayChart();
    }

    @UiThread
    private void verifyDatesAndDisplayChart() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mCategoryDetailsContainer.removeAllViews();
            }
        });

        if(mCurrentStartDate <= mCurrentEndDate) {
            setChartDataAsync(mCurrentStartDate, mCurrentEndDate);
        } else {
            ((FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.block_invalid_dates), Snackbar.LENGTH_SHORT);
            setChartDataAsync(0, 0);
        }
    }

    private void setStartLabel(long timestamp){
        mStart.setText(DateUtils.getDateShort(timestamp));
    }

    private void setEndLabel(long timestamp){
        mStop.setText(DateUtils.getDateShort(timestamp));
    }
}
