package edu.temple.templetag;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by dilloncoffman on 2020-03-18
 */
public class User implements Comparable<User>, Parcelable {
    private int mUserID;
    private String mUsername;
    private ArrayList<Tag> mUserTags;
    private double mLastPositionLat;
    private double mLastPositionLong;
    private float mDistanceToCurrentUser;


    protected User(Parcel in) {
        mUserID = in.readInt();
        mUsername = in.readString();
        mLastPositionLat = in.readDouble();
        mLastPositionLong = in.readDouble();
        mDistanceToCurrentUser = in.readFloat();
    }

    public User(){

    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mUserID);
        parcel.writeString(mUsername);
        parcel.writeDouble(mLastPositionLat);
        parcel.writeDouble(mLastPositionLong);
        parcel.writeFloat(mDistanceToCurrentUser);
    }

    @Override
    public int compareTo(User user) {
        return 0;
    }

    public int getmUserID() {
        return mUserID;
    }

    public void setmUserID(int mUserID) {
        this.mUserID = mUserID;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public ArrayList<Tag> getmUserTags() {
        return mUserTags;
    }

    public void setmUserTags(ArrayList<Tag> mUserTags) {
        this.mUserTags = mUserTags;
    }

    public double getmLastPositionLat() {
        return mLastPositionLat;
    }

    public void setmLastPositionLat(double mLastPositionLat) {
        this.mLastPositionLat = mLastPositionLat;
    }

    public double getmLastPositionLong() {
        return mLastPositionLong;
    }

    public void setmLastPositionLong(double mLastPositionLong) {
        this.mLastPositionLong = mLastPositionLong;
    }

    public float getmDistanceToCurrentUser() {
        return mDistanceToCurrentUser;
    }

    public void setmDistanceToCurrentUser(float mDistanceToCurrentUser) {
        this.mDistanceToCurrentUser = mDistanceToCurrentUser;
    }

    @Override
    public String toString() {
        return "User{" +
                "mUserID=" + mUserID +
                ", mUsername='" + mUsername + '\'' +
                ", mUserTags=" + mUserTags +
                ", mLastPositionLat=" + mLastPositionLat +
                ", mLastPositionLong=" + mLastPositionLong +
                ", mDistanceToCurrentUser=" + mDistanceToCurrentUser +
                '}';
    }
}
