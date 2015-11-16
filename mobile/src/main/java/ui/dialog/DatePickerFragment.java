package ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.florianwolf.onthejob.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String ARG_TIMESTAMP = "arg_timestamp";

    public interface DatePickerCallback{
        void onDatePickerComplete(long timestamp, int targetRequestCode);
    }

    public static DatePickerFragment newInstance(long timestamp){

        DatePickerFragment fragment = new DatePickerFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(ARG_TIMESTAMP, timestamp);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getArguments().getLong(ARG_TIMESTAMP));
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getContext(), R.style.Theme_Default_Dialog, this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        Calendar calendar = new GregorianCalendar(year, month, day);

        try{
            ((DatePickerCallback)getTargetFragment()).onDatePickerComplete(calendar.getTimeInMillis(), getTargetRequestCode());
        }catch(ClassCastException e){
            throw new IllegalArgumentException("Parent activity has to implement datepicker callbacks");
        }
    }
}
