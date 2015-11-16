package ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.florianwolf.onthejob.R;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String ARG_TIMESTAMP = "arg_timestamp";

    public interface TimePickerCallback{
        void onTimePickerComplete(long timestamp, int targetRequestCode);
    }

    public static TimePickerFragment newInstance(long timestamp){

        TimePickerFragment fragment = new TimePickerFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(ARG_TIMESTAMP, timestamp);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getArguments().getLong(ARG_TIMESTAMP));

        // Set those to 0 to make creation of work blocks with flat values easier
        c.set(Calendar.MINUTE, 0);

        return new TimePickerDialog(getContext(), R.style.Theme_Default_Dialog, this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try{
            ((TimePickerCallback)getTargetFragment()).onTimePickerComplete(calendar.getTimeInMillis(), getTargetRequestCode());
        }catch(ClassCastException e){
            throw new IllegalArgumentException("Parent has to implement timepicker callbacks");
        }
    }
}
