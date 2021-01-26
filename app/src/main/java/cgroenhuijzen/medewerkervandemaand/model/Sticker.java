package cgroenhuijzen.medewerkervandemaand.model;

import android.net.Uri;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class Sticker {
    /*
    Class to create Sticker objects.
    Used when the user wants to load their own Stickers into the application.
    Requires String and Uri to create an object.
     */

    private String name;
    private Uri uri;

    //Constructor of the Sticker class.
    public Sticker(String name, Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    //Returns the String name.
    public String getName() {
        return name;
    }

    //Returns the Uri uri.
    public Uri getUri() {
        return uri;
    }

}
