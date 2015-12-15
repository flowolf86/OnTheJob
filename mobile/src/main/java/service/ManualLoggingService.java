package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.florianwolf.onthejob.R;

import cache.DataCacheHelper;
import data.WorkBlock;
import data.WorkEntry;
import data.factory.WorkEntryFactory;
import data.helper.WorkItemHelper;

import static listing.ManualLoggingState.IManualLoggingState;
import static listing.ManualLoggingState.STARTED;
import static listing.ManualLoggingState.STOPPED;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 15/12/15.
 */
public class ManualLoggingService extends Service {

    @IManualLoggingState static int mStatus = STOPPED;

    private static long loggingStartedAt = 0L;

    private final IBinder mBinder = new LocalBinder();

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public ManualLoggingService getService() {
            return ManualLoggingService.this;
        }
    }

    public @IManualLoggingState int getStatus(){
        return mStatus;
    }

    public long getLoggingStartedAt(){
        return loggingStartedAt;
    }

    public long getTimeBetweenStartOfLoggingAndNow(){
        return System.currentTimeMillis() - loggingStartedAt;
    }

    public synchronized void startLogging(){

        loggingStartedAt = System.currentTimeMillis();
        mStatus = STARTED;
    }

    public synchronized void stopLoggingAndStoreEntry(){

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getApplicationContext());
        WorkEntry workEntry = dataCacheHelper.getWorkEntryForTimestampDay(loggingStartedAt);

        if(workEntry == null){
            workEntry = WorkEntryFactory.buildNewEmptyManualWorkEntry();
            workEntry.setDate(loggingStartedAt);
            workEntry.setTitle(getString(R.string.manual_widget_entry_title));
        }

        WorkBlock workBlock = WorkItemHelper.generateManuallyStartStoppedWorkBlock(workEntry, loggingStartedAt, loggingStartedAt + (System.currentTimeMillis() - loggingStartedAt), getApplicationContext());
        workEntry.addWorkBlock(workBlock);
        dataCacheHelper.addNewEntry(workEntry, null);

        loggingStartedAt = 0L;
        mStatus = STOPPED;
    }

    public synchronized void stopLogging(){

        loggingStartedAt = 0L;
        mStatus = STOPPED;
    }
}
