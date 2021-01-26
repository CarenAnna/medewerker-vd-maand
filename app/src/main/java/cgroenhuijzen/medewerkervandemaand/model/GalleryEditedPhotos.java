package cgroenhuijzen.medewerkervandemaand.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class GalleryEditedPhotos {
    /*
     * Class to create GalleryEditedPhotos objects.
     * Used to store data of all photos previously edited in the application.
     * Requires an ArrayList to create an object.
     */

    private ArrayList<EditedPhoto> photos;
    private int size;

    //Constructor of class GalleryEditedPhotos.
    public GalleryEditedPhotos(ArrayList<EditedPhoto> photos) {
        this.photos = photos;
        this.size = calculateSize();
    }

    /*
     * Method to create EditedPhoto objects from all photos previously acquired by the application.
     * Adds all EditedPhoto objects to photos ArrayList.
     */
    public void initPhotos(Context context, String folder) {
        this.photos.clear();
        int i = 1;

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
        };

        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[]{"%/" + folder + "/%"},
                MediaStore.Images.Media.DEFAULT_SORT_ORDER
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    long dateSeconds = cursor.getInt(dateColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    String employeeName;
                    String[] nameParts = name.split("_");
                    String[] employeeParts = nameParts[0].split("-");
                    if (employeeParts.length == 1) {
                        employeeName = employeeParts[0];
                    } else {
                        employeeName = TextUtils.join(" ", employeeParts);
                    }

                    Date date = new Date(dateSeconds * 1000);
                    Locale nl = new Locale("NL");
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy, HH:mm", nl);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    String formattedDate = sdf.format(date);
                    String[] dateParts = formattedDate.split(" ");
                    String month = dateParts[2];

                    photos.add(new EditedPhoto(name, formattedDate, contentUri, i, employeeName, month));

                    i++;
                }
            }
        }
    }

    //Method to add a photo to photos ArrayList.
    public void addPhoto(EditedPhoto photo) {
        photos.add(photo);
        size = calculateSize();
    }

    //Method that returns the EditedPhoto with the id given as parameter.
    public EditedPhoto getPhotoById(int id) {
        for (EditedPhoto photo : photos) {
            if (photo.getId() == id) {
                return photo;
            }
        }
        return null;
    }

    //Method to delete a photo from the photos ArrayList using an integer id.
    public void deletePhotoById(int id) {
        EditedPhoto photoToRemove = getPhotoById(id);
        if (photoToRemove != null) {
            photos.remove(photoToRemove);
            size = calculateSize();
        }
    }

    //Method to calculate the size of the photos ArrayList.
    private int calculateSize() {
        return photos.size();
    }

    //Method that returns the photos ArrayList.
    public ArrayList<EditedPhoto> getEditedPhotos() {
        return photos;
    }

    //Method that returns the size of the photos ArrayList.
    public int getSize() {
        return size;
    }
}
