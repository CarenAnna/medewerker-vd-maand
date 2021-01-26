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

public class Photo implements Parcelable {
    /*
     * Class to create Photo objects.
     * Used to store data of photos taken and loaded with the application.
     * Requires String name, String date, Uri uri and int id to create objects.
     */

    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    protected String name;
    protected String date;
    protected Uri uri;
    protected int id;
    protected boolean expanded;

    //Constructor of the Photo class.
    public Photo(String name, String date, Uri uri, int id) {
        this.name = name;
        this.date = date;
        this.uri = uri;
        this.id = id;
        expanded = false;
    }

    //Code below is written to implement the Parcelable interface.
    protected Photo(Parcel in) {
        name = in.readString();
        date = in.readString();
        uri = (Uri) in.readValue(Uri.class.getClassLoader());
        id = in.readInt();
        expanded = in.readByte() != 0x00;
    }

    //Returns boolean isExpanded.
    public boolean isExpanded() {
        return expanded;
    }

    //Set boolean isExpanded to expanded.
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    //Returns integer id.
    public int getId() {
        return id;
    }

    //Set integer id to id.
    public void setId(int id) {
        this.id = id;
    }

    //Returns String name.
    public String getName() {
        return name;
    }

    //Set String name to name.
    public void setName(String name) {
        this.name = name;
    }

    //Returns String date.
    public String getDate() {
        return date;
    }

    //Returns Uri uri.
    public Uri getUri() {
        return uri;
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
    }
}
