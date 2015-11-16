package ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import view.ColorPickerView;

/**
 * Created by Florian on 23.06.2015.
 */
public class ColorPickerDialogFragment extends DialogFragment implements ColorPickerView.ColorPickerViewInterface {

    public static final int COLOR_PICKER_DIALOG = 842;

    private String mTitle = null;
    private String mText = null;
    private String mPositiveButtonText = null;
    private String mNegativeButtonText = null;
    private int mRequestCode = 0;
    private @ColorInt int mColor;

    private Dialog mDialogInstance = null;

    @Bind(R.id.color_picker) ColorPickerView mColorPicker;

    public interface ColorPickerDialogFragmentInterface {
        void onPositiveDialogResponse(int responseCode, @ColorInt int color);
        void onNegativeDialogResponse(int responseCode);
    }

    public ColorPickerDialogFragment() { }

    public static ColorPickerDialogFragment newInstance(@Nullable String title, @Nullable String text, String positiveText, String negativeText, int requestCode, @ColorInt int color) {

        ColorPickerDialogFragment f = new ColorPickerDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("text", text);
        args.putString("positiveText", positiveText);
        args.putString("negativeText", negativeText);
        args.putInt("requestCode", requestCode);
        args.putInt("color", color);

        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_color_picker, null);
        mColorPicker = (ColorPickerView) v.findViewById(R.id.color_picker);

        mTitle = getArguments().getString("title");
        mText = getArguments().getString("text");
        mPositiveButtonText = getArguments().getString("positiveText");
        mNegativeButtonText = getArguments().getString("negativeText");
        mRequestCode = getArguments().getInt("requestCode");
        mColor = getArguments().getInt("color");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(v);

        if(mText != null) {
            builder.setMessage(mText);
        }

        if(mTitle != null) {
            builder.setTitle(mTitle);
        }

        mColorPicker.setCallback(this);
        mColorPicker.setColor(mColor);

        builder.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    mDialogInstance.dismiss();
                    ((ColorPickerDialogFragmentInterface) getTargetFragment()).onPositiveDialogResponse(mRequestCode, mColor);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Parent has to implement simple dialog callbacks");
                }
            }
        })
                .setNegativeButton(mNegativeButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            mDialogInstance.dismiss();
                            ((ColorPickerDialogFragmentInterface) getTargetFragment()).onNegativeDialogResponse(mRequestCode);
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException("Parent has to implement simple dialog callbacks");
                        }
                    }
                });

        mDialogInstance = builder.create();
        return mDialogInstance;
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        mColor = color;
    }
}
