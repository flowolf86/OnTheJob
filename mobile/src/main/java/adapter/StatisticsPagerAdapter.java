package adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ui.fragment.ListChartFragment;
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
        switch (position){
            case 0:
                return PieChartFragment.newInstance();
            case 1:
                return ListChartFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }
}
