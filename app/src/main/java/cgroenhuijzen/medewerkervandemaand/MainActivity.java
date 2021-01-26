package cgroenhuijzen.medewerkervandemaand;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cgroenhuijzen.medewerkervandemaand.model.Model;
import cgroenhuijzen.medewerkervandemaand.model.Photo;

import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.FROM_INTENT_KEY;
import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.REQUEST_DETAIL_ACTIVITY;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class MainActivity extends AppCompatActivity {
    /*
     * Activity that controls the Home window.
     * Displays all photos taken with and loaded into the application.
     */

    public static final String ADAPTER_KEY = "savedAdapter";
    public static final String FROM_INTENT_MAIN = "main";
    private static final String FOLDER = "MvdM/Photos";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_LOAD = 0;

    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_PERMISSION_CAMERA = 101;
    private static final int REQUEST_PERMISSION_LOAD = 102;

    TextView textAvailable;
    private GalleryRecViewAdapter adapter;
    private BottomNavigationView bottomNav;

    private File imagesFolder;
    private File currentImage;

    private String[] permissions;
    private Boolean galleryLoaded;

    /*
     * Method called when the activity is created.
     * Sets up the view by creating an adapter and loading the gallery.
     * Sets the onClickLister for the floating action button.
     * Checks permissions. And initiates the top app and bottom navigation bar.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        textAvailable = findViewById(R.id.textNoPhotosHome);
        galleryLoaded = false;

        adapter = new GalleryRecViewAdapter(this, FROM_INTENT_MAIN);

        /*
         * Load ArrayList photos from savedInstanceState if possible.
         * Use example: on screen rotation.
         */
        if (savedInstanceState != null) {
            galleryLoaded = true;
            ArrayList<Photo> photos = savedInstanceState.getParcelableArrayList(ADAPTER_KEY);
            adapter.setPhotos(photos);
        }
        RecyclerView galleryRecView = findViewById(R.id.galleryRecView);
        galleryRecView.setAdapter(adapter);
        galleryRecView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        ExtendedFloatingActionButton fabCamera = findViewById(R.id.fab);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions(REQUEST_PERMISSION_CAMERA)) {
                    takePhoto();
                }
            }
        });

        initToolbar();
        initBottomNav();
        if (checkPermissions(REQUEST_PERMISSIONS)) {
            imagesFolder = PhotoUtils.makeFolder(this, FOLDER);
            if (savedInstanceState == null) {
                loadGallery();
            }
        }

        //Needed to be able to use the camera.
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    //Method that saves the ArrayList of photos to the savedInstanceState.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList(ADAPTER_KEY, adapter.getPhotos());
    }

    /*
     * Method called after OnCreate or when a paused activity is made active again.
     * Highlights the correct icon in the bottom navigation.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Menu bottom = bottomNav.getMenu();
        MenuItem item = bottom.getItem(0);
        item.setChecked(true);
    }

    /*
     * Method to initiate the bottom navigation bar.
     * Flag used to reorder the activity to front if it is not yet destroyed.
     * Otherwise a new Activity instance would be unnecessarily created.
     */
    private void initBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.pageHome);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.pageGallery) {
                    Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                    intent.putExtra(FROM_INTENT_KEY, FROM_INTENT_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    //Method to set the top app bar as toolbar.
    private void initToolbar() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
    }

    /*
     * Method to get an ArrayList of photos to give to the adapter.
     * Uses initPhotos method of Gallery class.
     * Runs on a new thread to alleviate the main thread.
     * Then runs on UI thread to update the view.
     */
    public void loadGallery() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Model.getInstance().getGallery().initPhotos(getApplicationContext(), FOLDER);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setPhotos(Model.getInstance().getGallery().getPhotos());
                        if (adapter.getPhotos().size() == 0) {
                            textAvailable.setVisibility(View.VISIBLE);
                        } else {
                            textAvailable.setVisibility(View.GONE);
                        }
                        galleryLoaded = true;
                    }
                });
            }
        };
        thread.start();
    }

    //Method to inflate the top app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_main, menu);
        return true;
    }

    /*
     * Method that sets functions to the top app bar.
     * Checks runtime permissions before calling loadPhoto() and takePhoto().
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.top_add_photo:
                if (checkPermissions(REQUEST_PERMISSION_LOAD)) {
                    loadPhoto();
                }
                return true;

            case R.id.top_camera:
                if (checkPermissions(REQUEST_PERMISSION_CAMERA)) {
                    takePhoto();
                }
                return true;

            case R.id.top_expand_all:
                adapter.expandAll();
                return true;

            case R.id.top_collapse_all:
                adapter.collapseAll();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Method to take a photo with the camera of the phone.
     * Creates a file to save the image to.
     * Starts an intent, code continues in onActivityResult.
     */
    public void takePhoto() {
        Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Locale nl = new Locale("NL");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", nl).format(new Date());

        File image = new File(imagesFolder, "MvdM_" + timeStamp + ".png");
        Uri uriSavedImage = Uri.fromFile(image);

        currentImage = image;
        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);
    }

    /*
     * Method to take load a photo from the phones memory.
     * Starts an intent, code continues in onActivityResult.
     */
    public void loadPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_LOAD);
    }

    //Method called after an acitivity is started for result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //Called after takePhoto()
                case REQUEST_IMAGE_CAPTURE:
                    addPhoto(currentImage);
                    break;

                /*
                 * Called after loadPhoto().
                 * Gets the filename of the chosen file.
                 * Copies the file to the Photos folder of the application.
                 */
                case REQUEST_IMAGE_LOAD:
                    Uri uri = null;
                    if (data != null) {
                        uri = data.getData();
                    }

                    if (uri != null) {
                        String scheme = uri.getScheme();

                        String filepath = uri.getPath();
                        String filename = null;

                        if (scheme != null && scheme.equals("content")) {
                            filename = PhotoUtils.getFileName(uri, this);
                        } else {
                            if (filepath != null) {
                                String[] pathParts = filepath.split(":");
                                String[] fileParts = pathParts[pathParts.length - 1].split("/");
                                String lastPart = fileParts[fileParts.length - 1];
                                if (lastPart.contains(".")) {
                                    filename = lastPart;
                                } else filename = null;
                            }
                        }

                        if (filename == null) {
                            Locale nl = new Locale("NL");
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", nl).format(new Date());
                            filename = "MvdM_" + timeStamp + ".png";
                        }

                        File destination = new File(imagesFolder.getAbsolutePath() + "/" + filename);
                        try {
                            InputStream input = getContentResolver().openInputStream(uri);
                            if (input != null) {
                                PhotoUtils.copy(input, destination);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                        addPhoto(destination);
                    }
                    break;

                /*
                 * This method is called when a photo is deleted from the PhotoDetailActivity.
                 * Redirects to a function of the adapter.
                 */
                case REQUEST_DETAIL_ACTIVITY:
                    adapter.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    /*
     * This method is called when a photo is added to the Photos folder of the application.
     * Scans the file with the MediaScanner to let the phone know there is a new photo.
     * Loads the exif data of the photo to get the date.
     * Creates a new Photo object and adds it to the adapter.
     */
    public void addPhoto(File photo) {
        MediaScannerConnection.scanFile(MainActivity.this, new String[]{photo.getPath()}, new String[]{"image/*"}, null);
        String date = null;
        ExifInterface exif;

        try {
            exif = new ExifInterface(photo.getPath());
            date = exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (date == null) {
            Locale nl = new Locale("NL");
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy, HH:mm", nl);
            Date dateNow = new Date();
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
            date = sdf.format(dateNow);
        }
        String name = photo.getName();

        //Create Photo
        Photo newPhoto = new Photo(name, date, Uri.fromFile(photo), adapter.getItemCount() + 1);
        Model.getInstance().getGallery().addPhoto(newPhoto);

        //Update recyclerview
        if (!adapter.getPhotoByName(name)) {
            adapter.addPhoto(newPhoto);
        }
        adapter.notifyItemInserted(adapter.getItemCount() - 1);

        textAvailable.setVisibility(View.GONE);
    }

    /*
     * Method to check if all permissions are given already.
     * Used to check runtime permissions.
     * Returns true if all permissions are given.
     * If not: returns false and requestPermissions is called.
     */
    public boolean checkPermissions(int requestCode) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), requestCode);
            return false;
        }
        return true;
    }

    /*
     * If not all permissions were granted yet, this method is called.
     * Looks at the requestCode to determine what to do.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            //Creates the photos folder and loads the gallery if it isn't loaded yet.
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    imagesFolder = PhotoUtils.makeFolder(this, FOLDER);
                    if (!galleryLoaded) {
                        loadGallery();
                    }

                }
                break;

            //Calls takePhoto() if all permissions are given
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                }
                break;

            //Calls loadPhoto() if all permissions are given.
            case REQUEST_PERMISSION_LOAD:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPhoto();
                }
                break;
        }
    }

}