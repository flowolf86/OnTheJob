package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.florianwolf.onthejob.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.WorkEntry;
import ui.base.BaseFragment;
import ui.decorator.EventDecorator;

/**
 * Created by Florian on 22.06.2015.
 */
public class CalendarFragment extends BaseFragment implements OnDateSelectedListener{

    public static final String FRAGMENT_TAG = "calendar_fragment";

    private static String ARG_CUSTOM_TOOLBAR_TITLE = "custom_toolbar_title";

    @Bind(R.id.calendarView) MaterialCalendarView mCalendarView;

    public interface CalendarFragmentInterface{
        void onDateSelected(WorkEntry workEntry);
        void onDisplayList();
    }

    public CalendarFragment() { }

    public static CalendarFragment newInstance(@Nullable String customToolbarTitle){

        CalendarFragment f = new CalendarFragment();

        Bundle args = new Bundle();

        if(customToolbarTitle != null) {
            args.putString(ARG_CUSTOM_TOOLBAR_TITLE, customToolbarTitle);
        }

        f.setArguments(args);

        return f;
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ButterKnife.bind(this, view);
        super.setToolbarTitle(getArguments().getString(ARG_CUSTOM_TOOLBAR_TITLE));
        setHasOptionsMenu(true);
        setUpCalendarView();

        return view;
    }

    @Override
    public void onStart() {
        CURRENT_FRAGMENT_TAG = FRAGMENT_TAG;
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.list, menu);
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

    private void setUpCalendarView() {

        mCalendarView.setFirstDayOfWeek(Calendar.getInstance(Locale.getDefault()).getFirstDayOfWeek());
        mCalendarView.setSelectedDate(Calendar.getInstance());
        mCalendarView.setOnDateChangedListener(this);
        decorateEvents();
    }

    private void decorateEvents() {
        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());

        // WorkEntries
        mCalendarView.addDecorator(new EventDecorator(ContextCompat.getColor(getContext(), R.color.primary), dataCacheHelper.getAllEventDays()));
        // Sick days
        mCalendarView.addDecorator(new EventDecorator(ContextCompat.getColor(getContext(), R.color.orange_300), dataCacheHelper.getAllSickDays()));
        // Vacation
        mCalendarView.addDecorator(new EventDecorator(ContextCompat.getColor(getContext(), R.color.blue_200), dataCacheHelper.getAllVacationDays()));
    }

    /*
        Logic
     */

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentBackHandlerInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof CalendarFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        WorkEntry entry = dataCacheHelper.getWorkEntryForTimestampDay(date.getCalendar().getTimeInMillis());

        if(entry == null){
            ((FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest("No entry for this date.", Snackbar.LENGTH_SHORT);
        } else {
            ((CalendarFragmentInterface)getActivity()).onDateSelected(entry);
        }
    }

    /*
        OptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.action_display_history_list:
                ((CalendarFragmentInterface)getActivity()).onDisplayList();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
