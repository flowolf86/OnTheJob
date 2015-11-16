package data;

import android.os.Parcel;
import android.os.Parcelable;

public class GeofenceEvent implements Parcelable {

    public long _id = Long.MIN_VALUE;
    public long timestamp = Long.MIN_VALUE;
    public String geofenceId = null;
    public int geofenceType = Integer.MIN_VALUE;

    // Init with default values. Set values later.
    public GeofenceEvent(long id, long timestamp, String geofenceId, int geofenceType) {
        this._id = id;
        this.timestamp = timestamp;
        this.geofenceId = geofenceId;
        this.geofenceType = geofenceType;
    }

    /*
        Parcelable
     */
    public GeofenceEvent(Parcel parcel){
        this._id = parcel.readLong();
        this.timestamp = parcel.readLong();
        this.geofenceType = parcel.readInt();
        this.geofenceId = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(_id);
        parcel.writeLong(timestamp);
        parcel.writeInt(geofenceType);
        parcel.writeString(geofenceId);
    }

    public static final Creator<GeofenceEvent> CREATOR =
            new Creator<GeofenceEvent>() {

                @Override
                public GeofenceEvent createFromParcel(Parcel source) {
                    return new GeofenceEvent(source);
                }

                @Override
                public GeofenceEvent[] newArray(int size) {
                    return new GeofenceEvent[size];
                }
            };
}
