package ui.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.BetterViewPager;
import android.support.v4.view.PagerAdapter;

import com.florianwolf.onthejob.R;

import adapter.StatisticsPagerAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import support.ZoomOutPageTransformer;
import ui.base.BaseActivity;
import ui.base.BaseFragment;

/**
 * Created by Florian on 24.06.2015.
 */
public class StatisticsActivity extends BaseActivity implements
        BaseFragment.FragmentSnackbarInterface {

    private static final int NUMBER_OF_FRAGMENTS_IN_PAGER = 3;

    @Bind(R.id.view_pager) BetterViewPager mViewPager;
    PagerAdapter mPagerAdapter;

    private BaseFragment mSelectedFragment;

    /*
        Lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        super.setUpToolbar(R.id.toolbar, R.string.statistics, true);
        ButterKnife.bind(this);

        setUpViewPager();
    }

    private void setUpViewPager() {

        mPagerAdapter = new StatisticsPagerAdapter(getSupportFragmentManager(), NUMBER_OF_FRAGMENTS_IN_PAGER);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    /*
        FragmentSnackbarInterface
     */

    @Override
    public void onFragmentSnackbarRequest(String text, int duration) {
        Snackbar.make(findViewById(R.id.root), text, duration).show();
    }
}
