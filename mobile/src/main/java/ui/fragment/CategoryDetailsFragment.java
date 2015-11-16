package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.CategoryCacheHelper;
import data.Category;
import data.factory.CategoryFactory;
import ui.base.BaseFragment;
import ui.dialog.ColorPickerDialogFragment;
import ui.dialog.SimpleDialogFragment;
import util.ColorUtils;
import util.TextUtils;

public class CategoryDetailsFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener,
        SimpleDialogFragment.SimpleDialogCallback, ColorPickerDialogFragment.ColorPickerDialogFragmentInterface {

    public static final String FRAGMENT_TAG = "category_details_fragment";

    /*
        Variables & Statics
     */
    @Bind(R.id.store_category) FloatingActionButton mStoreCategoryButton;

    @Bind(R.id.root) CoordinatorLayout mRootLayout;

    @Bind(R.id.title) EditText mTitleText;
    @Bind(R.id.title_counter) TextView mTitleCounter;

    @Bind(R.id.description) EditText  mDescriptionText;
    @Bind(R.id.description_counter) TextView mDescriptionCounter;

    @Bind(R.id.icon) ImageView mIcon;
    @Bind(R.id.selector) ImageView mSelector;

    private boolean mHasDataChanged = false;

    private static final String ARG_CATEGORY = "arg_category";

    private static int TITLE_LENGTH = 0;
    private static int DESCRIPTION_LENGTH = 0;

    private Category mOriginalCategory;
    private Category mEditedCategory;

    private CategoryCacheHelper mCategoryCacheHelper;

    /*
        Interface
     */

    public CategoryDetailsFragment() { }

    public static CategoryDetailsFragment newInstance(@Nullable Category category) {

        CategoryDetailsFragment f = new CategoryDetailsFragment();

        Bundle args = new Bundle();
        if(category != null) {
            args.putParcelable(ARG_CATEGORY, category);
        }
        f.setArguments(args);

        return f;
    }

    /*
        Fragment data
     */

    private CategoryCacheHelper getCategoryCacheHelper(){

        if(mCategoryCacheHelper == null){
            mCategoryCacheHelper = new CategoryCacheHelper();
        }
        return mCategoryCacheHelper;
    }

    private void loadCategory(){

        if(getArguments() != null){
            mOriginalCategory = getArguments().getParcelable(ARG_CATEGORY);

            if(mOriginalCategory != null) {
                mEditedCategory = mOriginalCategory.getCopy();
                setToolbarTitle(R.string.title_edit_category);
            } else {
                mEditedCategory = CategoryFactory.getNewEmtpyUserCategory();
                setToolbarTitle(R.string.title_new_category);
            }
        }
    }

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_details, container, false);

        ButterKnife.bind(this, view);
        loadCategory();
        getDefaultIntegers();
        refreshFragmentData();
        setOnClickListeners();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public void onStart() {
        CURRENT_FRAGMENT_TAG = FRAGMENT_TAG;
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        unsetOnFocusChangedListeners();
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof FragmentBackHandlerInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    /*
        Logic
     */

    private void getDefaultIntegers() {
        TITLE_LENGTH = getResources().getInteger(R.integer.title_length);
        DESCRIPTION_LENGTH = getResources().getInteger(R.integer.block_description_length);
    }

    private void setDataChanged() {
        mHasDataChanged = true;
    }

    private boolean hasDataChanged(){
        return mHasDataChanged;
    }

    private void refreshFragmentData() {

        mTitleText.setText(mEditedCategory.name);
        mTitleCounter.setText(getString(R.string.default_text_counter, TextUtils.isValidText(mEditedCategory.name) ? mEditedCategory.name.length() : 0, TITLE_LENGTH));

        mDescriptionText.setText(mEditedCategory.description);
        mDescriptionCounter.setText(getString(R.string.default_text_counter, TextUtils.isValidText(mEditedCategory.description) ? mEditedCategory.description.length() : 0, DESCRIPTION_LENGTH));

        setCategoryColor(mEditedCategory.color);
    }

    /*
        Listeners
     */

    private void setOnClickListeners() {

        mTitleText.setOnFocusChangeListener(this);
        mTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mEditedCategory.name = s.toString();
                mTitleCounter.setText(getString(R.string.default_text_counter, s.length(), TITLE_LENGTH));
                setDataChanged();
            }
        });

        mDescriptionText.setOnFocusChangeListener(this);
        mDescriptionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mEditedCategory.description = s.toString();
                mDescriptionCounter.setText(getString(R.string.default_text_counter, s.length(), DESCRIPTION_LENGTH));
                setDataChanged();
            }
        });

        mStoreCategoryButton.setOnClickListener(this);
        mSelector.setOnClickListener(this);
    }

    private void unsetOnFocusChangedListeners() {

        mTitleText.setOnFocusChangeListener(null);
        mDescriptionText.setOnFocusChangeListener(null);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.store_category:

                if(!TextUtils.isValidText(mEditedCategory.name)){
                    showIncompleteCategoryNameSnackbar();
                    return;
                }

                if(mOriginalCategory != null) {
                    mOriginalCategory.setAllDataExcludingId(mEditedCategory);
                    getCategoryCacheHelper().modifyCategoryInDatabase(getContext(), mOriginalCategory);
                } else {
                    getCategoryCacheHelper().addCategoryInDatabase(getContext(), mEditedCategory);
                }

                ((FragmentSnackbarInterface) getActivity()).onFragmentSnackbarRequest(getString(R.string.category_stored), Snackbar.LENGTH_SHORT);
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;

            case R.id.selector:
                showColorPickerDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        switch(v.getId()){
            case R.id.title:
                if(mTitleCounter != null) {
                    mTitleCounter.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
                break;
            case R.id.description:
                if(mTitleCounter != null) {
                    mDescriptionCounter.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onBackPressed() {

        return onActivityFinishFragmentRequest();
    }

    @Override
    public boolean onActivityFinishFragmentRequest() {

        if(!hasDataChanged()){
            return false;
        }

        showExitWithoutSavingDialog();
        return true;
    }

    public void showExitWithoutSavingDialog(){
        SimpleDialogFragment newFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.dialog_text_exit_without_saving),
                getString(R.string.dialog_button_exit).toUpperCase(Locale.getDefault()),
                getString(R.string.dialog_button_stay).toUpperCase(Locale.getDefault()),
                SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG,
                null);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    public void showColorPickerDialog(){
        ColorPickerDialogFragment newFragment = ColorPickerDialogFragment.newInstance(
                getString(R.string.dialog_text_select_category_color),
                null,
                getString(R.string.dialog_button_select).toUpperCase(Locale.getDefault()),
                getString(R.string.dialog_button_cancel).toUpperCase(Locale.getDefault()),
                ColorPickerDialogFragment.COLOR_PICKER_DIALOG,
                mEditedCategory.color);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getChildFragmentManager(), "color_picker_dialog");
    }

    public void showIncompleteCategoryNameSnackbar(){
        Snackbar.make(mRootLayout, getString(R.string.category_no_name), Snackbar.LENGTH_SHORT).show();
    }

    private void setCategoryColor(@ColorInt int color){
        mEditedCategory.color = color;
        mSelector.setColorFilter(color, PorterDuff.Mode.ADD);

        int drawableId = ColorUtils.isDarkColor(color) ? R.drawable.ic_receipt_white_24dp : R.drawable.ic_receipt_black_24dp;
        mIcon.setImageResource(drawableId);
    }

    @Override
    public void onPositiveDialogResponse(int responseCode, Parcelable data) {

        switch (responseCode){
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:
                ((FragmentNavigationInterface) getActivity()).onFragmentFinished();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPositiveDialogResponse(int responseCode, @ColorInt int color) {

        switch (responseCode){
            case ColorPickerDialogFragment.COLOR_PICKER_DIALOG:
                setCategoryColor(color);
                setDataChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeDialogResponse(int responseCode) {
        switch (responseCode){
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:
            case ColorPickerDialogFragment.COLOR_PICKER_DIALOG:
            default:
                break;
        }
    }
}
