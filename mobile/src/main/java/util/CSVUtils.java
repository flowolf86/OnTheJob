package util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.florianwolf.onthejob.R;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cache.DataCacheHelper;
import data.Interval;
import data.WorkBlock;
import data.WorkEntry;
import ui.dialog.SimpleDialogFragment;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 11/12/15.
 */
public class CSVUtils {

    public interface ExportCallback {
        void exportFailed(@NonNull String message);
        void exportSucceeded(@NonNull String filePath);
    }

    /**
     * Always check storage permissions first!!!
     *
     * @param context
     * @param workEntryData
     * @param callback
     */
    private static void exportDataToCSV(@NonNull final Context context, @Nullable final List<WorkEntry> workEntryData, @Nullable final List<Interval> intervalData, @NonNull final ExportCallback callback){

        if(workEntryData == null && intervalData == null){
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                CSVWriter writer = null;
                final String FILE_PATH = context.getExternalCacheDir() + "/onthejob_backup_" + System.currentTimeMillis() + ".csv";
                try {
                    writer = new CSVWriter(new FileWriter(FILE_PATH));

                    String[] entries = {"Timestamp (WorkEntry)", "Title (WorkEntry)" , "Description (WorkEntry)", "Timestamp start (WorkBlock)", "Timestamp end (WorkBlock)", "Minutes (WorkBlock)", "Title (WorkBlock)", "Description (WorkBlock)", "Category (WorkBlock)"};
                    writer.writeNext(entries);

                    if(workEntryData != null) {
                        for (WorkEntry entry : workEntryData) {
                            for (WorkBlock block : entry.getWorkBlocks()) {
                                entries = new String[]{String.valueOf(entry.getDate()), entry.getTitle(), entry.getText(), String.valueOf(block.getWorkStart()), String.valueOf(block.getWorkEnd()), String.valueOf(TimeUnit.MILLISECONDS.toMinutes(block.getWorkEnd() - block.getWorkStart())), block.getTitle(), block.getText(), block.getCategory().getName()};
                                writer.writeNext(entries);
                            }
                        }
                    }

                    // Empty line
                    writer.writeNext(new String[]{});

                    // Write new header
                    entries = new String[]{"Start day", "End day" , "Number of days (rounded)", "Title", "Description", "Category"};
                    writer.writeNext(entries);

                    if(intervalData != null) {
                        for (Interval interval : intervalData) {
                            entries = new String[]{String.valueOf(interval.getStartDate()), String.valueOf(interval.getEndDate()), String.valueOf(DateUtils.getNumberOfDays(interval.getStartDate(), interval.getEndDate())), interval.getTitle(), interval.getDescription(), interval.getCategory().getName()};
                            writer.writeNext(entries);
                        }
                    }

                    callback.exportSucceeded(FILE_PATH);
                }catch (Exception e){
                    callback.exportFailed(context.getString(R.string.csv_unable_to_create_file));
                } finally {
                    try{
                        if(writer != null){
                            writer.close();
                        }
                    }catch(IOException e){
                        callback.exportFailed(context.getString(R.string.csv_general_error));
                    }
                }
            }
        }).start();
    }

    public static void exportData(@NonNull final Context context, @NonNull final FragmentManager fragmentManager){

        DataCacheHelper dataCacheHelper = new DataCacheHelper(context);
        CSVUtils.exportDataToCSV(context, dataCacheHelper.getAllWorkEntries(), dataCacheHelper.getAllIntervals(), new CSVUtils.ExportCallback() {
            @Override
            public void exportFailed(@NonNull String message) {
                SimpleDialogFragment.newInstance(null, message, context.getString(R.string.dialog_button_ok), null, SimpleDialogFragment.NO_REQUEST_CODE, null).show(fragmentManager, null);
            }

            @Override
            public void exportSucceeded(@NonNull String filePath) {

                if (!new File(filePath).exists()) {
                    exportFailed(context.getString(R.string.csv_unable_to_create_file));
                    return;
                }

                // TODO Texts translations
                MailUtils.startMailApplication(context, null, "OnTheJob Export " + DateUtils.getDate(System.currentTimeMillis()), "An export of all data from OnTheJob!", "Export", filePath);
            }
        });
    }
}
