package io.typebrook.fiveminsmore.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HP-HP on 05-12-2015.
 */
public class TimeLineModel implements Parcelable {

    public static final Creator<TimeLineModel> CREATOR = new Creator<TimeLineModel>() {
        @Override
        public TimeLineModel createFromParcel(Parcel source) {
            return new TimeLineModel(source);
        }

        @Override
        public TimeLineModel[] newArray(int size) {
            return new TimeLineModel[size];
        }
    };
    private String mMessage;
    private long mTime;

    public TimeLineModel() {
    }

    public TimeLineModel(String mMessage, long mDate) {
        this.mMessage = mMessage;
        this.mTime = mDate;
    }

    protected TimeLineModel(Parcel in) {
        this.mMessage = in.readString();
        this.mTime = in.readLong();
        int tmpMStatus = in.readInt();
    }

    public String getMessage() {
        return mMessage;
    }

    public void semMessage(String message) {
        this.mMessage = message;
    }

    public long getTime() {
        return mTime;
    }

    public void setDate(long time) {
        this.mTime = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mMessage);
        dest.writeLong(this.mTime);
    }
}
