package edu.temple.templetag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dilloncoffman on 2020-03-18
 */
public class Tag implements Parcelable {
    private int mTagID;
    private String mTagLocationName;
    private int mTagDuration;
    private String mTagImageURI;
    private String mTagDescription;
    private double mTagLocationLat;
    private double mTagLocationLong;
    private int mTagUpvoteCount;
    private int mTagDownvoteCount;
    private int mTagPopularity;
    private String mTagCreatedBy;

    public Tag(int mTagID, String mTagLocationName, int mTagDuration, String mTagImageURI, String mTagDescription, double mTagLocationLat, double mTagLocationLong, int mTagUpvoteCount, int mTagDownvoteCount, int mTagPopularity, String mTagCreatedBy) {
        this.mTagID = mTagID;
        this.mTagLocationName = mTagLocationName;
        this.mTagDuration = mTagDuration;
        this.mTagImageURI = mTagImageURI;
        this.mTagDescription = mTagDescription;
        this.mTagLocationLat = mTagLocationLat;
        this.mTagLocationLong = mTagLocationLong;
        this.mTagUpvoteCount = mTagUpvoteCount;
        this.mTagDownvoteCount = mTagDownvoteCount;
        this.mTagPopularity = mTagPopularity;
        this.mTagCreatedBy = mTagCreatedBy;
    }

    protected Tag(Parcel in) {
        mTagID = in.readInt();
        mTagDuration = in.readInt();
        mTagImageURI = in.readString();
        mTagDescription = in.readString();
        mTagLocationLat = in.readDouble();
        mTagLocationLong = in.readDouble();
        mTagUpvoteCount = in.readInt();
        mTagDownvoteCount = in.readInt();
        mTagPopularity = in.readInt();
        mTagCreatedBy = in.readString();
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public int getmTagID() {
        return mTagID;
    }

    public void setmTagID(int mTagID) {
        this.mTagID = mTagID;
    }

    public String getmTagLocationName() {
        return mTagLocationName;
    }

    public void setmTagLocationName(String mTagLocationName) {
        this.mTagLocationName = mTagLocationName;
    }


    public int getmTagDuration() {
        return mTagDuration;
    }

    public void setmTagDuration(int mTagDuration) {
        this.mTagDuration = mTagDuration;
    }

    public String getmTagImageURI() {
        return mTagImageURI;
    }

    public void setmTagImageURI(String mTagImageURI) {
        this.mTagImageURI = mTagImageURI;
    }

    public String getmTagDescription() {
        return mTagDescription;
    }

    public void setmTagDescription(String mTagDescription) {
        this.mTagDescription = mTagDescription;
    }

    public double getmTagLocationLat() {
        return mTagLocationLat;
    }

    public void setmTagLocationLat(double mTagLocationLat) {
        this.mTagLocationLat = mTagLocationLat;
    }

    public double getmTagLocationLong() {
        return mTagLocationLong;
    }

    public void setmTagLocationLong(double mTagLocationLong) {
        this.mTagLocationLong = mTagLocationLong;
    }

    public int getmTagUpvoteCount() {
        return mTagUpvoteCount;
    }

    public void setmTagUpvoteCount(int mTagUpvoteCount) {
        this.mTagUpvoteCount = mTagUpvoteCount;
    }

    public int getmTagDownvoteCount() {
        return mTagDownvoteCount;
    }

    public void setmTagDownvoteCount(int mTagDownvoteCount) {
        this.mTagDownvoteCount = mTagDownvoteCount;
    }

    public int getmTagPopularity() {
        return mTagPopularity;
    }

    public void setmTagPopularity(int mTagPopularity) {
        this.mTagPopularity = mTagPopularity;
    }

    public String getmTagCreatedBy() {
        return mTagCreatedBy;
    }

    public void setmTagCreatedBy(String mTagCreatedBy) {
        this.mTagCreatedBy = mTagCreatedBy;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "mTagID=" + mTagID +
                ", mTagDuration=" + mTagDuration +
                ", mTagImageURI='" + mTagImageURI + '\'' +
                ", mTagDescription='" + mTagDescription + '\'' +
                ", mTagLocationLat=" + mTagLocationLat +
                ", mTagLocationLong=" + mTagLocationLong +
                ", mTagUpvoteCount=" + mTagUpvoteCount +
                ", mTagDownvoteCount=" + mTagDownvoteCount +
                ", mTagPopularity=" + mTagPopularity +
                ", mTagCreatedBy='" + mTagCreatedBy + '\'' +
                '}';
    }

    public void voteUp() {

    }

    public void voteDown() {

    }

    public void increaseTagMapMarkerSize() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTagID);
        dest.writeString(mTagImageURI);
        dest.writeInt(mTagDuration);
        dest.writeString(mTagDescription);
        dest.writeDouble(mTagLocationLat);
        dest.writeDouble(mTagLocationLong);
        dest.writeInt(mTagUpvoteCount);
        dest.writeInt(mTagDownvoteCount);
        dest.writeInt(mTagPopularity);
        dest.writeString(mTagCreatedBy);
    }
}
