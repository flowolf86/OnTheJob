package adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ui.fragment.PieChartFragment;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 15/12/15.
 */
public class StatisticsPagerAdapter extends FragmentStatePagerAdapter{

    private int numberOfPages = 0;

    public StatisticsPagerAdapter(FragmentManager fm, int numberOfPages) {
        super(fm);
        this.numberOfPages = numberOfPages;
    }

    @Override
    public Fragment getItem(int position) {
        return new PieChartFragment();  //TODO
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }
}
