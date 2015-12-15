package view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 09/11/15.
 */
public class ColorPickerView extends RelativeLayout implements SeekBar.OnSeekBarChangeListener{

    private static final int MAX_COLOR = 255;

    @Bind(R.id.color_seekbar_red) SeekBar mColorSeekbarRed;
    @Bind(R.id.color_seekbar_green) SeekBar mColorSeekbarGreen;
    @Bind(R.id.color_seekbar_blue) SeekBar mColorSeekbarBlue;
    @Bind(R.id.color_visual) RelativeLayout mColorVisual;

    @ColorInt int mCurrentColor;
    ColorPickerViewInterface mCallback;

    public interface ColorPickerViewInterface {
        void onColorSelected(@ColorInt int color);
    }

    public ColorPickerView(Context context) {
        super(context);
        init(context, R.layout.view_color_picker);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, R.layout.view_color_picker);
    }

    private void init(@NonNull Context context, @LayoutRes int layoutId) {
        inflate(context, layoutId, this);
        ButterKnife.bind(this);
        configureSeekbars();
    }

    public void setCallback(@NonNull final ColorPickerViewInterface callback){
        mCallback = callback;
    }

    public void setColor(@ColorInt int color){

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        mColorSeekbarRed.setProgress(red);
        mColorSeekbarGreen.setProgress(green);
        mColorSeekbarBlue.setProgress(blue);
    }

    private void configureSeekbars() {

        mColorSeekbarRed.setOnSeekBarChangeListener(this);
        mColorSeekbarRed.setMax(MAX_COLOR);

        mColorSeekbarGreen.setOnSeekBarChangeListener(this);
        mColorSeekbarGreen.setMax(MAX_COLOR);

        mColorSeekbarBlue.setOnSeekBarChangeListener(this);
        mColorSeekbarBlue.setMax(MAX_COLOR);
    }

    /*
        Seekbar
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        mCurrentColor = Color.argb(255, mColorSeekbarRed.getProgress(), mColorSeekbarGreen.getProgress(), mColorSeekbarBlue.getProgress());
        mColorVisual.setBackgroundColor(mCurrentColor);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        if(mCallback != null){
            mCallback.onColorSelected(mCurrentColor);
        }
    }
}
