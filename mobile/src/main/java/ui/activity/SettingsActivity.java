package ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.florianwolf.onthejob.R;

import data.Category;
import ui.base.BaseActivity;
import ui.base.BaseFragment;
import ui.fragment.AboutFragment;
import ui.fragment.CategoryDetailsFragment;
import ui.fragment.CategoryListFragment;
import ui.fragment.SettingsMenuFragment;
import util.MailUtils;

/**
 * Created by Florian on 24.06.2015.
 */
public class SettingsActivity extends BaseActivity implements
        BaseFragment.FragmentSnackbarInterface,
        SettingsMenuFragment.OnSettingsMenuFragmentActionListener,
        CategoryListFragment.CategoryListFragmentInterface{

    private BaseFragment mSelectedFragment;

    /*
        Lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        super.setUpToolbar(R.id.toolbar, R.string.settings, true);

        displayMenuFragment();
    }

    /*
        Logic
     */

    public void displayMenuFragment(){
        SettingsMenuFragment settingsMenuFragment = SettingsMenuFragment.newInstance();
        super.replaceFragment(R.id.fragment_content_wrapper, settingsMenuFragment, false, BaseActivity.ANIMATION_NONE, SettingsMenuFragment.FRAGMENT_TAG);
    }

    /*
        CategoryListFragment
     */

    @Override
    public void onCategoriesSelected() {
        CategoryListFragment categoryListFragment = CategoryListFragment.newInstance();
        super.replaceFragment(R.id.fragment_content_wrapper, categoryListFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, CategoryListFragment.FRAGMENT_TAG);
    }

    @Override
    public void onAboutSelected() {
        AboutFragment aboutFragment = AboutFragment.newInstance();
        super.replaceFragment(R.id.fragment_content_wrapper, aboutFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, AboutFragment.FRAGMENT_TAG);
    }

    @Override
    public void onFeedbackSelected() {
        MailUtils.startMailApplication(this, getString(R.string.feedback_email), getString(R.string.feedback_subject), getString(R.string.feedback_text), getString(R.string.feedback), null);
    }

    /*
        CategoryListFragmentInterface
     */

    @Override
    public void onCategoryCreate() {
        CategoryDetailsFragment categoryDetailsFragment = CategoryDetailsFragment.newInstance(null);
        super.replaceFragment(R.id.fragment_content_wrapper, categoryDetailsFragment, true, BaseActivity.ANIMATION_SLIDE_UP_DOWN, CategoryListFragment.FRAGMENT_TAG);
    }

    @Override
    public void onCategoryEdit(@NonNull Category category) {
        CategoryDetailsFragment categoryDetailsFragment = CategoryDetailsFragment.newInstance(category);
        super.replaceFragment(R.id.fragment_content_wrapper, categoryDetailsFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, CategoryListFragment.FRAGMENT_TAG);
    }

    /*
        FragmentSnackbarInterface
     */

    @Override
    public void onFragmentSnackbarRequest(String text, int duration) {
        Snackbar.make(findViewById(R.id.root), text, duration).show();
    }
}
