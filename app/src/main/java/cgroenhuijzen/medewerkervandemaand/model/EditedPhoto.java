package cgroenhuijzen.medewerkervandemaand.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class EditedPhoto extends Photo implements Parcelable {
    /*
     * Class to create EditedPhoto objects.
     * Extends Photo class.
     * Used to store data of photos edited with the application.
     * Requires super variables and String employeeName and String month to create an object.
     */

    public static final Parcelable.Creator<EditedPhoto> CREATOR = new Parcelable.Creator<EditedPhoto>() {
        @Override
        public EditedPhoto createFromParcel(Parcel in) {
            return new EditedPhoto(in);
        }

        @Override
        public EditedPhoto[] newArray(int size) {
            return new EditedPhoto[size];
        }
    };
    private String employeeName;
    private String month;

    //Constructor of EditedPhoto class.
    public EditedPhoto(String name, String date, Uri uri, int id, String employeeName, String month) {
        super(name, date, uri, id);
        this.employeeName = employeeName;
        this.month = month;
    }

    //Code below is written to implement Parcelable interface.
    protected EditedPhoto(Parcel in) {
        super(in);
        employeeName = in.readString();
        month = in.readString();
    }

    //Returns the employeeName String.
    public String getEmployeeName() {
        return employeeName;
    }

    //Returns the month String.
    public String getMonth() {
        return month;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(date);
        dest.writeValue(uri);
        dest.writeInt(id);
        dest.writeByte((byte) (expanded ? 0x01 : 0x00));
        dest.writeString(employeeName);
        dest.writeString(month);
    }
}
