package geofence;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.florianwolf.onthejob.R;
import com.google.android.gms.location.Geofence;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;

import cache.DataCacheHelper;
import cache.GeofenceDbCallback;
import data.Category;
import data.GeofenceEvent;
import data.WorkBlock;
import data.WorkEntry;
import data.factory.WorkEntryFactory;
import database.DatabaseHelper;
import support.AppToaster;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 25/08/15.
 */
public class GeofenceAutoAdder {

    protected static final String TAG = "geofence-auto-adder";

    private Context mContext = null;

    public GeofenceAutoAdder(@NonNull final Context context){
        mContext = context;
    }

    public void createWorkEntryAndBlockIfNeeded(long blockStartTime, long blockEndTime, @NonNull final String fenceString) {

        DataCacheHelper dataCacheHelper = new DataCacheHelper(mContext);

        WorkEntry todayEntry = dataCacheHelper.getWorkEntryForTimestampDay(blockEndTime);

        if(todayEntry == null){
            // No work entry exists for today
            todayEntry = WorkEntryFactory.buildNewAutomaticWorkEntry(mContext, blockEndTime, fenceString);
            todayEntry.addWorkBlock(new WorkBlock(blockStartTime, blockEndTime, mContext.getResources().getString(R.string.work_block), mContext.getResources().getString(R.string.generated_work_block_text), Category.WORK_DAY, todayEntry.getId()));
            dataCacheHelper.addNewEntry(todayEntry, null);

            AppToaster.toast(mContext, "New entry created for today.");
        } else {
            // A work entry already exists for today
            WorkBlock workBlock = new WorkBlock(blockStartTime, blockEndTime, mContext.getResources().getString(R.string.work_block), mContext.getResources().getString(R.string.generated_work_block_text), Category.WORK_DAY, todayEntry.getId());
            dataCacheHelper.addNewBlock(todayEntry, workBlock, null);

            AppToaster.toast(mContext, "New block created for today.");
        }

        //TODO Send broadcast to fragments?!
    }

    public void saveGeofenceEventInDatabase(final long timestamp, final int type, final String fenceId) {

        // We always store slide_side_enter events without forther checks
        if(type == Geofence.GEOFENCE_TRANSITION_ENTER){

            DatabaseHelper.addGeofenceEvent(mContext, new GeofenceEvent(timestamp, timestamp, fenceId, type), new GeofenceDbCallback() {
                @Override
                public void onDbCreateGeofenceComplete(@NonNull GeofenceEvent createdEvent) {
                    Log.d(TAG, "Geofence event has been stored to database. ID: " + createdEvent._id);
                }

                @Override
                public void onDbReadGeofencesComplete(@NonNull List<GeofenceEvent> resultSet) {
                }

                @Override
                public void onDbGeofencesFail(@Nullable String msg) {
                }
            });
            return;
        }

        // If it is an slide_side_exit event, we check the last events for an slide_side_enter event
        if(type == Geofence.GEOFENCE_TRANSITION_EXIT) {

            DatabaseHelper.getLastXGeofenceEvents(mContext, 3, new GeofenceDbCallback() {

                @Override
                public void onDbCreateGeofenceComplete(@NonNull GeofenceEvent createdEvent) {
                }

                @Override
                public void onDbReadGeofencesComplete(@NonNull List<GeofenceEvent> resultSet) {

                    GeofenceEvent aValidEnterEvent = null;

                    // Check if we have results
                    if (resultSet.size() > 0) {

                        // If we have results, iterate from bottom up in DESC order

                        // e.g. GEOFENCE EXIT 1 EVENT looking for valid slide_side_enter 1 event in last entries
                        // ENTRY 15 = ENTER 1   => valid
                        // ENTRY 16 = EXIT 1    => valid = null
                        // ENTRY 17 = ENTER 2   => valid = null
                        // ENTRY 18 = EXIT 2    => valid = null
                        // ENTRY 19 = ENTER 1   => valid (!)

                        for(int i = resultSet.size() - 1; i >= 0; i--){

                            GeofenceEvent event = resultSet.get(0);

                            // Ignore if older than 24 hours
                            DateTime dateTime = new DateTime(event.timestamp);
                            boolean isToday = dateTime.toLocalDate().equals(new LocalDate());
                            if(isToday) {
                                // and look for the last slide_side_enter with the same fenceId
                                if (event.geofenceId.equals(fenceId)) {

                                    if (event.geofenceType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                                        aValidEnterEvent = event;
                                    } else {

                                        // If the last event with that slide_side_exit ID was an EXIT event, something is not OK...
                                        aValidEnterEvent = null;
                                    }
                                } else {
                                    Log.d(TAG, "Skipping event. Not the id we are looking for. Continue...");
                                }
                            } else {
                                Log.w(TAG, "No work block could be created. Last slide_side_enter was NOT today.");
                            }
                        }
                    } else {
                        Log.e(TAG, "No work block could be created. There was no geofence ENTER before EXIT.");
                    }

                    final GeofenceEvent finalEvent = aValidEnterEvent;

                    if(finalEvent != null){

                        // We are good to go!

                        // We found:
                        // One of the last entries was an ENTER event with the same fence ID that this event has.
                        // No EXIT was called after the ENTER event with the same ID.
                        // The last slide_side_enter was TODAY.

                        // => We have a valid ENTER event in our db that was today with the same geofence id.

                        // Store the new event in database
                        DatabaseHelper.addGeofenceEvent(mContext, new GeofenceEvent(timestamp, timestamp, fenceId, type), new GeofenceDbCallback() {
                            @Override
                            public void onDbCreateGeofenceComplete(@NonNull GeofenceEvent createdEvent) {

                                String fenceString = mContext.getResources().getString(R.string.automatic_other);

                                if(fenceId.equalsIgnoreCase(GeofenceConfiguration.PRIMARY_GEOFENCE_ID)){
                                    fenceString = mContext.getResources().getString(R.string.automatic_primary_work);
                                } else if(fenceId.equalsIgnoreCase(GeofenceConfiguration.SECONDARY_GEOFENCE_ID)) {
                                    fenceString = mContext.getResources().getString(R.string.automatic_secondary_work);
                                }

                                createWorkEntryAndBlockIfNeeded(finalEvent.timestamp, timestamp, fenceString);
                            }

                            @Override
                            public void onDbReadGeofencesComplete(@NonNull List<GeofenceEvent> resultSet) { }

                            @Override
                            public void onDbGeofencesFail(@Nullable String msg) {
                                Log.e(TAG, "Failed to create a new geofence event in database.");
                            }
                        });
                    } else {
                        Log.w(TAG, "No work block could be created. No valid last ENTER event found.");
                    }
                }

                @Override
                public void onDbGeofencesFail(@Nullable String msg) {
                }
            });
        }
    }
}
