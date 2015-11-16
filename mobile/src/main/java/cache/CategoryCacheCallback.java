package cache;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import data.Category;
import data.WorkEntry;

/**
 * Created by Florian on 22.06.2015.
 */
public interface CategoryCacheCallback {

    void onDbReadCategoriesComplete(@NonNull List<Category> resultSet);
    void triggerDatabaseRead(@NonNull Context context);
}
