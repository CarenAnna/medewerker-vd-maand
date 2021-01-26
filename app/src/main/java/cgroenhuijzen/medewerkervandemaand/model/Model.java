package cgroenhuijzen.medewerkervandemaand.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class Model {
    /*
     * Singleton class to create an instance of Model.
     * The Model instance is used by the adapters to access the model data.
     */

    private static Model instance;

    private ArrayList<Sticker> stickers;
    private Gallery gallery;
    private GalleryEditedPhotos editedGallery;

    //Private constructor of the Model class.
    private Model(ArrayList<Sticker> stickers, Gallery gallery, GalleryEditedPhotos editedGallery) {
        this.stickers = stickers;
        this.gallery = gallery;
        this.editedGallery = editedGallery;
    }

    /*
     * Method that returns the instance of Model.
     * Creates a new instance if no instance exists yet.
     */
    public static synchronized Model getInstance() {
        if (instance == null) {
            ArrayList<Photo> photos = new ArrayList<>();
            Gallery gallery = new Gallery(photos);

            ArrayList<EditedPhoto> editedPhotos = new ArrayList<>();
            GalleryEditedPhotos editedGallery = new GalleryEditedPhotos(editedPhotos);

            ArrayList<Sticker> stickers = new ArrayList<>();

            instance = new Model(stickers, gallery, editedGallery);
        }
        return instance;
    }

    /*
     * Method to create Sticker objects from Sticker images previously loaded in the application.
     * Adds the Sticker objects to the stickers ArrayList.
     */
    public void initStickers(Context context, String folder) {
        this.stickers.clear();

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
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

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);


                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    stickers.add(new Sticker(name, contentUri));
                }
            }
        }
    }

    //Method to add a Sticker to the stickers ArrayList.
    public void addSticker(String name, Uri uri) {
        stickers.add(new Sticker(name, uri));
    }

    //Returns stickers ArrayList.
    public ArrayList<Sticker> getStickers() {
        return stickers;
    }

    //Returns the Gallery gallery.
    public Gallery getGallery() {
        return gallery;
    }

    //Returns the GalleryEditedPhotos editedGallery.
    public GalleryEditedPhotos getEditedGallery() {
        return editedGallery;
    }

}
