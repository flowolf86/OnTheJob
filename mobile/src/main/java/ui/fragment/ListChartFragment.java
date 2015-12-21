package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.florianwolf.onthejob.R;

import butterknife.ButterKnife;
import ui.base.BaseFragment;
import ui.dialog.DatePickerFragment;

/**
 * Created by Florian on 22.06.2015.
 *
 * This class is WIP
 */
public class ListChartFragment extends BaseFragment implements View.OnClickListener, DatePickerFragment.DatePickerCallback{

    public static final String FRAGMENT_TAG = "list_chart_fragment";
    private static final String START_PICKER_TAG = "START";
    private static final String END_PICKER_TAG = "END";

    private static final int START_DATE_REQUEST_CODE = 100;
    private static final int END_DATE_REQUEST_CODE = 200;

    private long mCurrentStartDate;
    private long mCurrentEndDate;

    public ListChartFragment() { }

    public static ListChartFragment newInstance(){

        return new ListChartFragment();
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_chart, container, false);
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

    }

    // TODO
    private void setChartDataAsync(final long startDate, final long endDate) {


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            default:
                break;
        }
    }

    @Override
    public void onDatePickerComplete(long timestamp, int targetRequestCode) {

        switch (targetRequestCode){
            case START_DATE_REQUEST_CODE:
                break;
            case END_DATE_REQUEST_CODE:
                break;
            default:
                break;
        }
    }
}
