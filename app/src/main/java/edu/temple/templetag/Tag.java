package edu.temple.templetag;

/**
 * Created by dilloncoffman on 2020-03-18
 */
public class Tag {
    private int mTagID;
    private int mTagDuration;
    private String mTagImageURI;
    private String mTagDescription;
    private double mTagLocationLat;
    private double mTagLocationLong;
    private int mTagUpvoteCount;
    private int mTagDownvoteCount;
    private int mTagPopularity;
    private String mTagCreatedBy;

    public Tag(int mTagID, int mTagDuration, String mTagImageURI, String mTagDescription, double mTagLocationLat, double mTagLocationLong, int mTagUpvoteCount, int mTagDownvoteCount, int mTagPopularity, String mTagCreatedBy) {
        this.mTagID = mTagID;
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

    public int getmTagID() {
        return mTagID;
    }

    public void setmTagID(int mTagID) {
        this.mTagID = mTagID;
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
}
