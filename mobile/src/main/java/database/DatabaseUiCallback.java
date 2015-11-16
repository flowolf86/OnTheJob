package database;

/**
 * Created by Florian on 22.06.2015.
 */
public interface DatabaseUiCallback {

    void onDbOperationSuccess(String operationSuccessText);
    void onDbOperationFail(String operationFailText);
}
