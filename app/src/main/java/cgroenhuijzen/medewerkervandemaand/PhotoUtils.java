package cgroenhuijzen.medewerkervandemaand;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cgroenhuijzen.medewerkervandemaand.model.Photo;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class PhotoUtils {
    /*
     * Utility class containing only static functions.
     * These functions are used in multiple Activities.
     */

    /*
     * Method to delete a photo from the phones memory.
     * Returns a boolean indicating whether the file was successfully deleted.
     */
    public static boolean deletePhoto(Photo photoToDelete, Context context) {
        Uri uri = photoToDelete.getUri();
        String path = null;
        String scheme = uri.getScheme();

        if (scheme != null) {
            //Photos loaded on startup have a content URI
            if (scheme.equals("content")) {
                path = getRealPathFromURI(context, uri);
            }
            //New photos have an absolute URI
            else if (scheme.equals("file")) {
                path = uri.getPath();
            }
        }

        if (path != null) {
            File file = new File(path);
            boolean isDeleted = file.delete();
            if (isDeleted) {
                MediaScannerConnection.scanFile(context, new String[]{path}, new String[]{"image/*"}, null);
                return true;
            } else {
                Toast.makeText(context, "Sorry, de foto kon niet worden verwijderd.", Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            Toast.makeText(context, "Sorry, de foto kon niet worden gevonden.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //Method to get a real path from a content Uri.
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //Method to share a photo using an intent.
    public static Intent shareImage(Photo photo) {
        Uri uri = photo.getUri();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        return share;
    }

    //Method to create a folder with the name given as parameter.
    public static File makeFolder(Context context, String folderName) {
        File folder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                Toast.makeText(context, "Map kon niet gemaakt worden.", Toast.LENGTH_LONG).show();
            }
        }
        return folder;
    }

    //Function to copy a file using an InputStream and a destination file.
    public static void copy(InputStream in, File destination) throws IOException {
        try {
            try (OutputStream out = new FileOutputStream(destination)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } finally {
            in.close();
        }
    }

    //Method to get the filename of a file with a content Uri.
    public static String getFileName(Uri uri, Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor =
                contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name;
        } else return null;
    }

}
