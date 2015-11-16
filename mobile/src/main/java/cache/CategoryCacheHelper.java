package cache;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.Category;
import database.DatabaseManager;

/**
 * This db has no cache because it would be overkill...
 *
 * Created by Florian on 24.06.2015.
 */
public class CategoryCacheHelper implements CategoryCacheCallback{

    private static CategoryCacheHelper INSTANCE = null;

    private static List<Category> mCategories = Collections.synchronizedList(new ArrayList<Category>());

    public List<Category> getCategories(){
        return mCategories;
    }

    public CategoryCacheHelper(){ }

    public static CategoryCacheHelper getInstance(){
        if(INSTANCE == null){
            INSTANCE = new CategoryCacheHelper();
        }
        return INSTANCE;
    }

    public static void init(Context context){
        getInstance().refreshCategoriesFromDatabase(context);
    }

    /**
     * If you refresh categories you'll receive a BC_CATEGORY_CACHE_UPDATED broadcast.
     * After that you can read the categories via CategoryCacheHelper#getCategories()
     *
     * @param context
     */
    public void refreshCategoriesFromDatabase(@NonNull Context context){
        DatabaseManager databaseManager = new DatabaseManager(context);

        try {
            databaseManager.readCategoriesFromDatabase(this);
        }catch(Exception ignore){ }
    }

    public void addCategoryInDatabase(@NonNull Context context, @NonNull Category category){
        DatabaseManager databaseManager = new DatabaseManager(context);

        try {
            databaseManager.createCategoryInDatabase(category, this);
        }catch(Exception ignore){ }
    }

    public void modifyCategoryInDatabase(@NonNull Context context, @NonNull Category category){
        DatabaseManager databaseManager = new DatabaseManager(context);

        try {
            databaseManager.modifyCategoryInDatabase(category, this);
        }catch(Exception ignore){ }
    }

    public void removeCategoryInDatabase(@NonNull Context context, @NonNull Category category){
        DatabaseManager databaseManager = new DatabaseManager(context);

        try {
            databaseManager.removeCategoryFromDatabase(category, this);
        }catch(Exception ignore){ }
    }

    @Override
    public void onDbReadCategoriesComplete(@NonNull List<Category> resultSet) {
        mCategories = Collections.synchronizedList(resultSet);
    }

    @Override
    public void triggerDatabaseRead(@NonNull Context context) {
        refreshCategoriesFromDatabase(context);
    }
}
