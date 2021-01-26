package cgroenhuijzen.medewerkervandemaand;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import cgroenhuijzen.medewerkervandemaand.model.EditedPhoto;
import cgroenhuijzen.medewerkervandemaand.model.Model;
import cgroenhuijzen.medewerkervandemaand.model.Photo;

import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.FROM_INTENT_KEY;
import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.REQUEST_DETAIL_ACTIVITY;
import static cgroenhuijzen.medewerkervandemaand.MainActivity.ADAPTER_KEY;
import static cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.ui.StickerActivity.FROM_INTENT_STICKER;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class GalleryActivity extends AppCompatActivity {
    /*
     * Activity that controls the Gallery window.
     * Displays all photos that were edited with the application.
     */

    public static final String FROM_INTENT_GALLERY = "gallery";
    private static final String FOLDER = "MvdM/EditedPhotos";
    private static final int REQUEST_PERMISSIONS = 100;

    TextView textAvailable;
    private GalleryRecViewAdapter adapter;
    private BottomNavigationView bottomNav;

    private String[] permissions;
    private boolean galleryLoaded;

    /*
     * Method called when the activity is created.
     * Sets up the view by creating an adapter and loading the gallery.
     * Checks permissions. And initiates the top app and bottom navigation bar.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        galleryLoaded = false;
        textAvailable = findViewById(R.id.textNoPhotosGallery);
        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        adapter = new GalleryRecViewAdapter(this, FROM_INTENT_GALLERY);
        RecyclerView galleryRecView = findViewById(R.id.galleryActRecView);
        galleryRecView.setAdapter(adapter);
        galleryRecView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        /*
         * Load ArrayList photos from savedInstanceState if possible.
         * Use example: on screen rotation.
         */
        if (savedInstanceState != null) {
            ArrayList<Photo> photos = savedInstanceState.getParcelableArrayList(ADAPTER_KEY);
            adapter.setPhotos(photos);
            galleryLoaded = true;
        }

        initToolbar();
        initBottomNav();
        if (checkPermissions()) {
            PhotoUtils.makeFolder(this, FOLDER);
            if (savedInstanceState == null) {
                loadGallery();
            }
        }

    }

    /*
     * Method called after OnCreate or when a paused activity is made active again.
     * Highlights the correct icon in the bottom navigation.
     * Gets the intent and call loadNewPhoto().
     */
    @Override
    protected void onResume() {
        super.onResume();
        Menu bottom = bottomNav.getMenu();
        MenuItem item = bottom.getItem(1);
        item.setChecked(true);

        //Add newly edited photo
        Intent intent = getIntent();
        if (intent != null) {
            String fromIntent = intent.getStringExtra(FROM_INTENT_KEY);
            loadNewPhoto(fromIntent);
        }
    }

    /*
     * Sets the intent to the most recent intent.
     * Otherwise when reordered to front, the intent would still be the original intent.
     * This would be a problem when checking for the intent in loadNewPhoto()
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    //Method that saves the ArrayList of photos to the savedInstanceState.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelableArrayList(ADAPTER_KEY, adapter.getPhotos());
    }

    /*
     * Method to get an ArrayList of photos to give to the adapter.
     * Uses initPhotos method of GalleryEditedPhotos class.
     * Runs on a new thread to alleviate the main thread.
     * Then runs on UI thread to update the view.
     */
    private void loadGallery() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Model.getInstance().getEditedGallery().initPhotos(getApplicationContext(), FOLDER);
                ArrayList<EditedPhoto> editedPhotos = Model.getInstance().getEditedGallery().getEditedPhotos();
                final ArrayList<Photo> photos = new ArrayList<Photo>(editedPhotos);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setPhotos(photos);
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

    /*
     * Method to initiate the bottom navigation bar.
     * Flag used to reorder the activity to front if it is not yet destroyed.
     * Otherwise a new Activity instance would be unnecessarily created.
     */
    private void initBottomNav() {
        bottomNav = findViewById(R.id.bottomNavGallery);
        bottomNav.setSelectedItemId(R.id.pageGallery);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.pageHome) {
                    Intent intent = new Intent(GalleryActivity.this, MainActivity.class);
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
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarGallery);
        setSupportActionBar(topAppBar);
    }

    /*
     * Method to add a newly edited photo to the adapter.
     * Called in onResume, checks if the incoming intent comes from StickerActivity.
     * If so, checks if the adapter already has the photo.
     * The adapter already has the photo if GalleryActivity is newly created when saving the photo.
     * Otherwise the photo is added to the adapter.
     */
    private void loadNewPhoto(String fromSticker) {
        if (fromSticker != null) {
            if (fromSticker.equals(FROM_INTENT_STICKER)) {
                textAvailable.setVisibility(View.GONE);
                ArrayList<EditedPhoto> editedPhotos = Model.getInstance().getEditedGallery().getEditedPhotos();
                if (editedPhotos.size() > adapter.getItemCount()) {
                    Photo toAddPhoto = editedPhotos.get(editedPhotos.size() - 1);
                    adapter.addPhoto(toAddPhoto);
                }
            }
        }

    }

    //Method to inflate the top app bar. Hiding two icons used in the MainActivity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_main, menu);
        MenuItem camera = menu.findItem(R.id.top_camera);
        camera.setVisible(false);
        MenuItem add = menu.findItem(R.id.top_add_photo);
        add.setVisible(false);
        return true;
    }

    //Method that sets functions for the top app bar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

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
     * Method to check if all permissions are given already.
     * Used to check runtime permissions.
     * Returns true if all permissions are given.
     * If not: returns false and requestPermissions is called.
     */
    boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), GalleryActivity.REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    /*
     * If not all permissions were granted yet, this method is called.
     * Makes the folder where the edited photos are stored.
     * Loads the gallery if it is not loaded yet.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                PhotoUtils.makeFolder(this, FOLDER);
                if (!galleryLoaded) {
                    loadGallery();
                }
            }
        }
    }

    /*
     * This method is called when a photo is deleted from the PhotoDetailActivity.
     * Redirects to a function of the adapter.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL_ACTIVITY) {
            adapter.onActivityResult(requestCode, resultCode, data);
        }
    }

}