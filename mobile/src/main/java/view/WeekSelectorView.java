package view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:  Florian Wolf
 * Email:   florian.wolf@maibornwolff.de
 * on 09/11/15.
 */
public class WeekSelectorView extends RelativeLayout implements View.OnClickListener{

    @Bind(R.id.monday) TextView mMonday;
    @Bind(R.id.tuesday) TextView mTuesday;
    @Bind(R.id.wednesday) TextView mWednesday;
    @Bind(R.id.thursday) TextView mThursday;
    @Bind(R.id.friday) TextView mFriday;
    @Bind(R.id.saturday) TextView mSaturday;
    @Bind(R.id.sunday) TextView mSunday;

    View[] mWeekView;
    int[] mWeekSelection = new int[]{0,0,0,0,0,0,0};

    private WeekSelectorViewInterface mCallback;

    public interface WeekSelectorViewInterface {
        /**
         * 7 weekdays, int[6]. 1 = selected, 0 = not selected
         * @param selection
         */
        void onWeekdaySelectedOrDeselected(int[] selection);
    }

    public WeekSelectorView(Context context) {
        super(context);
        init(context, R.layout.view_week_selector);
    }

    public WeekSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, R.layout.view_week_selector);
    }

    private void init(@NonNull Context context, @LayoutRes int layoutId) {
        inflate(context, layoutId, this);
        ButterKnife.bind(this);
        mWeekView = new View[]{mMonday,mTuesday,mWednesday,mThursday,mFriday,mSaturday,mSunday};
        setOnClickListeners();
    }

    public void setInitialData(@Nullable WeekSelectorViewInterface callback, @Nullable int[] weekSelection){
        this.mCallback = callback;
        setUi(weekSelection);
    }

    public int getNumberOfActiveElements(){
        int activeElements = 0;
        for(int i : mWeekSelection){
            if(i == 1){
                activeElements++;
            }
        }
        return activeElements;
    }

    private void setUi(@Nullable int[] weekSelection) {

        if(weekSelection != null){
            int count = 0;
            mWeekSelection = weekSelection;
            for(int i : weekSelection){
                if(i == 1){
                    setBackground(mWeekView[count], true);
                    count++;
                }
            }
        }
    }

    private void setOnClickListeners() {

        mMonday.setOnClickListener(this);
        mTuesday.setOnClickListener(this);
        mWednesday.setOnClickListener(this);
        mThursday.setOnClickListener(this);
        mFriday.setOnClickListener(this);
        mSaturday.setOnClickListener(this);
        mSunday.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        boolean setActive;
        int index = 0;

        switch (v.getId()){
            case R.id.monday:
                index = 0;
                break;
            case R.id.tuesday:
                index = 1;
                break;
            case R.id.wednesday:
                index = 2;
                break;
            case R.id.thursday:
                index = 3;
                break;
            case R.id.friday:
                index = 4;
                break;
            case R.id.saturday:
                index = 5;
                break;
            case R.id.sunday:
                index = 6;
                break;
            default:
                break;
        }

        setActive = setSelection(index);

        // Do not allow the last element to be unchecked
        // 0 because we decremented the number of elements already in setSelection(index)
        if(!setActive && getNumberOfActiveElements() == 0){
            // Reverse the change
            setSelection(index);
            return;
        }

        setBackground(v, setActive);

        if(mCallback != null) {
            mCallback.onWeekdaySelectedOrDeselected(mWeekSelection);
        }
    }

    private boolean setSelection(int index){

        if(mWeekSelection[index] == 1){
            mWeekSelection[index] = 0;
        } else {
            mWeekSelection[index] = 1;
        }

        return mWeekSelection[index] == 1;
    }

    private void setBackground(View v, boolean setActive) {

        int background;
        if(setActive){
            background = R.drawable.oval_weekday_active;
            if(v instanceof TextView){
                ((TextView) v).setTextColor(Color.WHITE);
                ((TextView) v).setTypeface(Typeface.DEFAULT_BOLD);
            }
        } else {
            background = R.drawable.oval_weekday_inactive;
            if(v instanceof TextView){
                ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.weekday_inactive_text));
                ((TextView) v).setTypeface(Typeface.DEFAULT);
            }
        }
        v.setBackgroundResource(background);
    }
}
