package application;

import android.content.Context;
import android.content.res.Resources;
import android.support.multidex.MultiDexApplication;

import cache.CategoryCacheHelper;
import cache.EntryCache;
import cache.IntervalCache;

/**
 * Created by Florian on 22.06.2015.
 */
public class OtjApplication extends MultiDexApplication {

    private static Context mContext = null;
    private static Resources mResources = null;

    public static Context getContext(){
        return mContext;
    }

    public static Resources getAppResources(){
        return mResources;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        mResources = getResources();

        // Initialize the caches
        //DatabaseManager databaseManager = new DatabaseManager(this);
        //databaseManager.wipeDatabase();

        CategoryCacheHelper.init(this);
        EntryCache.init(this);
        IntervalCache.init(this);

        //deleteDatabase(DatabaseManager.DATABASE_NAME);
        //DataCache.getInstance(this).wipeDatabase(null);
    }
}
