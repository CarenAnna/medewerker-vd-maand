package cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cgroenhuijzen.medewerkervandemaand.PhotoUtils;
import cgroenhuijzen.medewerkervandemaand.R;
import cgroenhuijzen.medewerkervandemaand.model.Model;
import cgroenhuijzen.medewerkervandemaand.model.Sticker;

/**
 * Medewerker van de maand app
 * Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 * <p>
 * Dit bestand is aangepast overgenomen van AndriyBas.
 * Op GitHub: <a href = https://github.com/uptechteam/MotionViews-Android>MotionViews-Android</a>
 * <p>
 * Aangepaste method:
 * onCreate(), onStickerSelected()
 * <p>
 * Eigen methods:
 * initToolbar(), onOptionsItemSelected(), loadNewSticker(), idToUri(), loadStickers()
 * checkPermissions(), onRequestPermissionResult() en onActivityResult()
 * <p>
 * Aangespaste adapter:
 * onBindViewHolder() en onClickListener()
 * Neemt nu een ArrayList met Uris en gebruikt Glide om hiermee de ImageViews te vullen.
 * OnClickListener maakt onderscheid tussen drawable sticker en eigen stickers.
 * <p>
 * Pikachu sticker borrowed from: http://www.flaticon.com/packs/pokemon-go
 */

public class StickerSelectActivity extends AppCompatActivity {
    /*
     * Activity that controls sticker select window.
     * Displays all available stickers.
     * Contains an inner class: StickersAdapter.
     */

    public static final String EXTRA_STICKER_ID = "extra_sticker_id";
    public static final String EXTRA_STICKER_URI = "extra_sticker_uri";

    private static final int REQUEST_STICKER_LOAD = 56;
    private static final int REQUEST_PERMISSION_FOLDER = 100;
    private static final int REQUEST_PERMISSION_LOAD = 101;

    private static final String FOLDER = "MvdM/Sticker";
    private File stickerFolder;
    private String[] permissions;

    private ArrayList<Uri> stickerUris = new ArrayList<>();
    private final int[] stickerIds = {
            R.drawable.pikachu,
            R.drawable.rainbow,
            R.drawable.tophat,
            R.drawable.glasses,
            R.drawable.cat,
            R.drawable.heart,
            R.drawable.medal
    };

    private StickersAdapter adapter;
    private int newSticker = 1;

    /*
     * Method called when the activity is created.
     * Loads all stickers and sets up the adapter.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_sticker_activity);

        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (checkPermissions(REQUEST_PERMISSION_FOLDER)) {
            stickerFolder = PhotoUtils.makeFolder(this, FOLDER);
            Model.getInstance().initStickers(this, FOLDER);
            loadStickers();
        }

        RecyclerView recyclerView = findViewById(R.id.stickers_recycler_view);
        GridLayoutManager glm = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(glm);

        List<Integer> stickers = new ArrayList<>(stickerIds.length);
        for (Integer id : stickerIds) {
            stickers.add(id);
        }

        adapter = new StickersAdapter(stickers, stickerUris, this);
        recyclerView.setAdapter(adapter);

        initToolbar();
    }

    //Method to inflate the top app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sticker_select_menu, menu);
        return true;
    }

    //Method to set function to the button on the top app bar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_load_sticker) {
            if (checkPermissions(REQUEST_PERMISSION_LOAD)) {
                loadNewSticker();
            }
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    /*
     * Method to set the top app bar as toolbar.
     * Also sets the up button to finish this activity when pressed.
     */
    private void initToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSelect);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Method that starts a new intent to load a sticker from the phones memory.
    private void loadNewSticker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select sticker"), REQUEST_STICKER_LOAD);
    }

    /*
     * Method to get the uri from the drawable stickers.
     * Adds these uris to the stickerUris ArrayList.
     */
    public void idToUri() {
        for (int id : stickerIds) {
            Resources resources = this.getResources();
            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + resources.getResourcePackageName(id) + '/' + resources.getResourceTypeName(id)
                    + '/' + resources.getResourceEntryName(id));
            stickerUris.add(uri);
        }
    }

    /*
     * Method to load all stickers to the stickerUris ArrayList.
     * First the drawables stickers, then the loaded stickers.
     */
    public void loadStickers() {
        stickerUris.clear();
        idToUri();
        ArrayList<Sticker> loadedStickers = Model.getInstance().getStickers();
        for (Sticker sticker : loadedStickers) {
            Uri uri = sticker.getUri();
            stickerUris.add(uri);
        }
    }

    //Method to set the result of this activity and return to StickerActivity.
    private void onStickerSelected(int stickerId, Uri uri) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_STICKER_ID, stickerId);
        intent.putExtra(EXTRA_STICKER_URI, uri.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    /*
     * Method called after starting an intent to load a new sticker.
     * Copies the sticker to the sticker folder of the application.
     * Adds the sticker to the stickerUris ArrayList and the adapter.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_STICKER_LOAD) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }

                if (uri != null) {
                    String defaultName = "sticker";
                    String filename = null;
                    String scheme = uri.getScheme();
                    if (scheme != null && scheme.equals("content")) {
                        filename = PhotoUtils.getFileName(uri, this);
                    } else {
                        String path = uri.getPath();
                        if (path != null) {
                            String[] pathParts = path.split(":");
                            String[] fileParts = pathParts[1].split("/");
                            String lastPart = fileParts[fileParts.length - 1];
                            if (lastPart.contains(".")) {
                                filename = lastPart;
                            }
                        }
                    }

                    if (filename == null) {
                        filename = defaultName + newSticker;
                        newSticker++;
                    }
                    File destination = new File(stickerFolder.getAbsolutePath() + "/" + filename);

                    try {
                        InputStream input = getContentResolver().openInputStream(uri);
                        if (input != null) {
                            PhotoUtils.copy(input, destination);
                            MediaScannerConnection.scanFile(this, new String[]{destination.getPath()}, new String[]{"image/*"}, null);
                            Model.getInstance().addSticker(destination.getName(), Uri.fromFile(destination));

                            loadStickers();
                            adapter.setStickerUris(stickerUris);
                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
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
        if (requestCode == REQUEST_PERMISSION_FOLDER) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                stickerFolder = PhotoUtils.makeFolder(this, FOLDER);
                Model.getInstance().initStickers(this, FOLDER);
                loadStickers();
            }
        } else if (requestCode == REQUEST_PERMISSION_LOAD) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadNewSticker();
            }
        }
    }

    class StickersAdapter extends RecyclerView.Adapter<StickersAdapter.StickerViewHolder> {
        /*
         * Inner class StickersAdapter.
         * Used for the RecyclerView displaying all stickers to the user.
         * Contains inner class ViewHolder.
         */

        private final List<Integer> stickerIds;
        private final Context context;
        private final LayoutInflater layoutInflater;
        private ArrayList<Uri> stickerUris;

        //Constructor of the StickersAdapter class.
        StickersAdapter(@NonNull List<Integer> stickerIds, ArrayList<Uri> stickerUris, @NonNull Context context) {
            this.stickerIds = stickerIds;
            this.stickerUris = stickerUris;
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
        }

        //Method to create a StickerViewHolder.
        @NonNull
        @Override
        public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new StickerViewHolder(layoutInflater.inflate(R.layout.sticker_item, parent, false));
        }

        //Sets the ImageViews using Glide.
        @Override
        public void onBindViewHolder(StickerViewHolder holder, int position) {
            Glide.with(context).asBitmap()
                    .load(stickerUris.get(position))
                    .into(holder.image);
        }

        //Returns the itemCount.
        @Override
        public int getItemCount() {
            return stickerUris.size();
        }

        //Returns the stickerId.
        private int getItem(int position) {
            return stickerIds.get(position);
        }

        //Sets the stickerUris ArrayList to stickerUris.
        public void setStickerUris(ArrayList<Uri> stickerUris) {
            this.stickerUris = stickerUris;
        }

        class StickerViewHolder extends RecyclerView.ViewHolder {
            /*
             * Inner class StickerViewHolder.
             * Initiates the ImageView and sets the onClickListener for the items.
             */

            ImageView image;

            StickerViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.sticker_image);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int maxDrawable = stickerIds.size();
                        int position = getAdapterPosition();

                        if (position >= 0) {
                            if (position < maxDrawable) {
                                int stickerId = getItem(position);
                                Uri uri = stickerUris.get(position);
                                onStickerSelected(stickerId, uri);
                            } else {
                                int stickerId = 0;
                                Uri uri = stickerUris.get(position);
                                onStickerSelected(stickerId, uri);
                            }
                        }

                    }
                });
            }

        }

    }
}
