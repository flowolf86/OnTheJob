package ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Florian on 23.06.2015.
 */
public class SimpleDialogFragment extends DialogFragment {

    public static final int EXIT_WITHOUT_SAVING_DIALOG = 100;
    public static final int EDIT_WORK_DAY_DIALOG = 200;
    public static final int ENABLE_GEOFENCING_DIALOG = 500;
    public static final int DISABLE_GEOFENCING_DIALOG = 510;

    private String mTitle = null;
    private String mText = null;
    private String mPositiveButtonText = null;
    private String mNegativeButtonText = null;
    private int mRequestCode = 0;
    private Parcelable mParcelable = null;

    private Dialog mDialogInstance = null;

    public interface SimpleDialogCallback {
        void onPositiveDialogResponse(int responseCode, Parcelable data);
        void onNegativeDialogResponse(int responseCode);
    }

    public SimpleDialogFragment() { }

    public static SimpleDialogFragment newInstance(@Nullable String title, @NonNull String text, String positiveText, String negativeText, int requestCode, @Nullable Parcelable parcelable) {

        SimpleDialogFragment f = new SimpleDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("text", text);
        args.putString("positiveText", positiveText);
        args.putString("negativeText", negativeText);
        args.putInt("requestCode", requestCode);

        if(parcelable != null) {
            args.putParcelable("parcelable", parcelable);
        }

        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mTitle = getArguments().getString("title");
        mText = getArguments().getString("text");
        mPositiveButtonText = getArguments().getString("positiveText");
        mNegativeButtonText = getArguments().getString("negativeText");
        mRequestCode = getArguments().getInt("requestCode");
        mParcelable = getArguments().getParcelable("parcelable");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(mText);

        if(mTitle != null) {
            builder.setTitle(mTitle);
        }

        builder.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    mDialogInstance.dismiss();
                    ((SimpleDialogCallback) getTargetFragment()).onPositiveDialogResponse(mRequestCode, mParcelable);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Parent has to implement simple dialog callbacks");
                }
            }
        })
                .setNegativeButton(mNegativeButtonText, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    mDialogInstance.dismiss();
                                    ((SimpleDialogCallback) getTargetFragment()).onNegativeDialogResponse(mRequestCode);
                                } catch (ClassCastException e) {
                                    throw new IllegalArgumentException("Parent has to implement simple dialog callbacks");
                                }
                            }
                        }
                );

        mDialogInstance = builder.create();
        return mDialogInstance;
    }
}
