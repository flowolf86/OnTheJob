package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import configuration.WorkConfiguration;
import data.WorkEntry;
import data.helper.WorkItemHelper;
import data.manager.SharedPreferencesManager;
import ui.base.BaseFragment;
import ui.base.BaseListFragment;
import util.DateUtils;
import util.UserUtils;

/**
 * Created by Florian on 22.06.2015.
 */
public class DayOffFragment extends BaseListFragment implements View.OnClickListener{

    public static final String FRAGMENT_TAG = "day_off_fragment";

    // Sick leave
    @Bind(R.id.call_in_sick_view) CardView mCallInSickView;
    @Bind(R.id.manage_sick_view) CardView mManageSickView;
    @Bind(R.id.sick_leave_statistics) TextView mSickStatisticsView;

    // Vacation
    @Bind(R.id.call_in_vacation_view) CardView mCallInVacationView;
    @Bind(R.id.manage_vacation_view) CardView mManageVacationView;
    @Bind(R.id.vacation_statistics) TextView mVacationStatisticsView;

    public DayOffFragment() { }

    public static DayOffFragment newInstance() {
        return new DayOffFragment();
    }

    /*
        Interface
     */

    public interface DayOffFragmentInterface {
        void onManageSickDays();
        void onManageVacationDays();
    }

    /*
        Lifecycle
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_off, container, false);

        ButterKnife.bind(this, view);
        super.setToolbarTitle(getString(R.string.title_day_off));
        setOnClickListeners();
        setDefaults();

        return view;
    }

    private void setDefaults() {

        final SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        final DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());

        setVacationsStatistics(sharedPreferencesManager, dataCacheHelper);
        setSickDaysStatistics(sharedPreferencesManager, dataCacheHelper);
    }

    private void setVacationsStatistics(SharedPreferencesManager sharedPreferencesManager, DataCacheHelper dataCacheHelper){

        StringBuilder vacationString = new StringBuilder(String.format(Locale.ENGLISH, Double.toString(dataCacheHelper.getTakenVacationDaysThisYear(sharedPreferencesManager))));
        vacationString.append(" / ");
        vacationString.append(getString(R.string.x_days_in_year, UserUtils.getUserYearlyVacationDays(sharedPreferencesManager), DateUtils.getCurrentYear()));
        mVacationStatisticsView.setText(vacationString);
    }

    private void setSickDaysStatistics(SharedPreferencesManager sharedPreferencesManager, DataCacheHelper dataCacheHelper) {

        StringBuilder sickDaysString = new StringBuilder();
        int userSickDays = UserUtils.getUserYearlySickDays(sharedPreferencesManager);
        if(userSickDays != WorkConfiguration.UNLIMITED_YEARLY_SICK_LEAVE) {
            sickDaysString.append(String.format(Locale.ENGLISH, Double.toString(dataCacheHelper.getSickDaysThisYear(sharedPreferencesManager))));
            sickDaysString.append(" / ");
            sickDaysString.append(getString(R.string.x_days_in_year, UserUtils.getUserYearlySickDays(sharedPreferencesManager), DateUtils.getCurrentYear()));
        } else {
            sickDaysString.append(getString(R.string.x_days_string_in_year, String.format(Locale.getDefault(), Double.toString(dataCacheHelper.getSickDaysThisYear(sharedPreferencesManager))), DateUtils.getCurrentYear()));
        }
        mSickStatisticsView.setText(sickDaysString);
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

    private void verifyActivityFulfillsRequirements(@NonNull Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof FragmentBackHandlerInterface
                && activity instanceof DayOffFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private void setOnClickListeners() {
        mCallInSickView.setOnClickListener(this);
        mCallInVacationView.setOnClickListener(this);
        mManageSickView.setOnClickListener(this);
        mManageVacationView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        final DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        WorkEntry todayEntry = dataCacheHelper.getWorkEntryForTimestampDay(System.currentTimeMillis());

        switch (v.getId()){
            case R.id.call_in_sick_view:

                // Create or modify the today entry
                if(todayEntry == null){
                    dataCacheHelper.addNewEntry(WorkItemHelper.generateSickDayWorkEntryOrWorkBlock(null, getContext()), null);
                }else{
                    todayEntry = WorkItemHelper.generateSickDayWorkEntryOrWorkBlock(todayEntry, getContext());
                    dataCacheHelper.modifyWorkEntry(todayEntry, null);
                }

                ((BaseFragment.FragmentNavigationInterface)getActivity()).onFragmentFinished();
                ((BaseFragment.FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.called_in_sick_today), Snackbar.LENGTH_SHORT);
                break;
            case R.id.call_in_vacation_view:

                // Create or modify the today entry
                if(todayEntry == null){
                    dataCacheHelper.addNewEntry(WorkItemHelper.generateVacationDayWorkEntryOrWorkBlock(null, getContext()), null);
                }else{
                    todayEntry = WorkItemHelper.generateVacationDayWorkEntryOrWorkBlock(todayEntry, getContext());
                    dataCacheHelper.modifyWorkEntry(todayEntry, null);
                }

                ((BaseFragment.FragmentNavigationInterface)getActivity()).onFragmentFinished();
                ((BaseFragment.FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.called_in_vacation_today), Snackbar.LENGTH_SHORT);
                break;
            case R.id.manage_sick_view:
                ((DayOffFragment.DayOffFragmentInterface)getActivity()).onManageSickDays();
                break;
            case R.id.manage_vacation_view:
                ((DayOffFragment.DayOffFragmentInterface)getActivity()).onManageVacationDays();
                break;
        }
    }
}
