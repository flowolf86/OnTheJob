package ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.florianwolf.onthejob.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.CategoryCacheHelper;
import cache.DataCacheHelper;
import data.Category;
import data.WorkBlock;
import data.WorkEntry;
import ui.base.BaseFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class PieChartFragment extends BaseFragment{

    public static final String FRAGMENT_TAG = "pie_chart_fragment";

    @Bind(R.id.chart) PieChart mChart;

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
        //verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Logic
     */

    private void initializeChart() {

        mChart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.background_gray));
        mChart.setCenterText("% of time per category");
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

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        CategoryCacheHelper categoryCacheHelper = new CategoryCacheHelper();

        setChartDataAsync(dataCacheHelper.getAllWorkEntries(), categoryCacheHelper.getCategories());
    }

    private void setChartDataAsync(final List<WorkEntry> workEntryList, final List<Category> categoryList) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<Category, Long> categoryToTimeReference = new HashMap<>();

                // Create an entry in the hash set for each category
                for(Category category : categoryList){
                    categoryToTimeReference.put(category, null);
                }

                long absoluteTotalTime = 0;

                // Calculate the total time per category in our work entries
                for(Category category : categoryList){

                    long categoryTotalTime = 0L;

                    for(WorkEntry workEntry : workEntryList) {
                        for (WorkBlock workBlock : workEntry.getWorkBlocks()) {
                            if(category.getId() == workBlock.getCategory().getId()){
                                categoryTotalTime += (workBlock.getWorkEnd() - workBlock.getWorkStart());
                            }
                        }
                    }

                    for(HashMap.Entry entry : categoryToTimeReference.entrySet()){
                        if(((Category) entry.getKey()).getId() == category.getId()){
                            entry.setValue(new Long(categoryTotalTime));
                            absoluteTotalTime += categoryTotalTime;
                            break;
                        }
                    }
                }

                // Set the data in a pie chart readable format
                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();

                int counter = 0;
                for(HashMap.Entry entry : categoryToTimeReference.entrySet()){

                    Category category = (Category)entry.getKey();
                    long totalCategoryTime = (Long)entry.getValue();

                    if(totalCategoryTime == 0){
                        continue;
                    }

                    // Not needed using use mChart.setUsePercentValues(true);
                    // float percentOfTime = totalCategoryTime / (absoluteTotalTime * 100);

                    entries.add(new Entry(totalCategoryTime, counter));
                    labels.add(category.getName());
                    colors.add(category.color);
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
                    }
                });
            }
        }).start();
    }
}
