package cgroenhuijzen.medewerkervandemaand.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

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

public class Gallery {
    /*
     * Class to create Gallery objects.
     * Used to store data of all photos used in the application.
     * Requires an ArrayList to create an object.
     */

    private ArrayList<Photo> photos;

    public Gallery(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    /*
     * Method to create Photo objects from all photos previously acquired by the application.
     * Adds all Photo objects to photos ArrayList.
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

                    Date date = new Date(dateSeconds * 1000);
                    Locale nl = new Locale("NL");
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy, HH:mm", nl);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    String formattedDate = sdf.format(date);

                    photos.add(new Photo(name, formattedDate, contentUri, i));

                    i++;
                }
            }
        }
    }

    //Method that returns the Photo with the id given as parameter.
    public Photo getPhotoById(int id) {
        for (Photo photo : photos) {
            if (photo.getId() == id) {
                return photo;
            }
        }
        return null;
    }

    //Method to delete a photo from the photos ArrayList using an integer id.
    public void deletePhotoById(int id) {
        Photo photoToRemove = getPhotoById(id);
        if (photoToRemove != null) {
            photos.remove(photoToRemove);
        }
    }

    //Method to add a photo to photos ArrayList.
    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    //Method that returns the photos ArrayList.
    public ArrayList<Photo> getPhotos() {
        return photos;
    }

}
