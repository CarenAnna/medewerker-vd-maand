package cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cgroenhuijzen.medewerkervandemaand.GalleryActivity;
import cgroenhuijzen.medewerkervandemaand.PhotoUtils;
import cgroenhuijzen.medewerkervandemaand.R;
import cgroenhuijzen.medewerkervandemaand.model.EditedPhoto;
import cgroenhuijzen.medewerkervandemaand.model.Model;
import cgroenhuijzen.medewerkervandemaand.model.Photo;
import cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.viewmodel.Layer;
import cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.widget.MotionView;
import cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.widget.entity.ImageEntity;

import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.FROM_INTENT_KEY;
import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.PHOTO_PARCELABLE_KEY;
import static cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.ui.StickerSelectActivity.EXTRA_STICKER_URI;

/**
 * Medewerker van de maand app
 * Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 * <p>
 * Dit bestand is aangepast overgenomen van AndriyBas.
 * Op GitHub: <a href = https://github.com/uptechteam/MotionViews-Android>MotionViews-Android</a>
 * <p>
 * Aangepaste methods:
 * onCreate(), onOptionsItemSelected() en onActivityResult().
 * <p>
 * Eigen methods:
 * initToolbar(), printText(), getEmployeeName(),
 * saveImage(), checkPermissions() en onRequestPermissionResult().
 * <p>
 * Eigen inner class:
 * SaveImageTask.
 */

public class StickerActivity extends AppCompatActivity {
    /*
     * Activity that controls photo edit screen.
     * Contains static inner class SaveImageTask.
     * Displays a photo and lets user add Stickers and text to it.
     */

    public static final String FROM_INTENT_STICKER = "sticker";
    public static final int SELECT_STICKER_REQUEST_CODE = 123;
    private static final String FOLDER = "MvdM/EditedPhotos";
    private static final int REQUEST_PERMISSION_FOLDER = 100;
    private static final int REQUEST_PERMISSION_SAVE = 101;

    protected MotionView motionView;
    private String month;
    private String employeeText;

    private File editedFolder;
    private String employeeName;
    private String[] permissions;

    private int width;
    private int height;
    private int x;
    private int y;
    private ProgressBar progress;
    private TextView textProgress;

    /*
     * Method called when the activity is created.
     * Sets the ImageView with the parcelable photo using Glide.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker);

        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        motionView = findViewById(R.id.main_motion_view);
        ImageView toEditPhoto = findViewById(R.id.toEdit);

        Intent intent = getIntent();
        Photo photo = null;
        if (intent != null) {
            photo = intent.getParcelableExtra(PHOTO_PARCELABLE_KEY);
        }

        if (photo != null) {
            Glide.with(this).asBitmap()
                    .load(photo.getUri())
                    .into(toEditPhoto);
        }

        initToolbar();
        if (checkPermissions(REQUEST_PERMISSION_FOLDER)) {
            editedFolder = PhotoUtils.makeFolder(this, FOLDER);
        }
    }

    /*
     * Method to set the top app bar as toolbar.
     * Also sets the up button to finish this activity when pressed.
     */
    private void initToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSticker);
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

    /*
     * Method to print the employee of the month text.
     * Creates a bitmap of a String and adds this to the motionView.
     */
    private void printText() {
        motionView.post(new Runnable() {
            @Override
            public void run() {
                Locale locale = new Locale("NL");
                Calendar cal = Calendar.getInstance();
                month = new SimpleDateFormat("MMMM", locale).format(cal.getTime());
                employeeText = "Medewerker van de maand " + month + "!";

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(17 * getResources().getDisplayMetrics().density);
                paint.setColor(Color.BLACK);
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setShadowLayer(20, 0, 0, Color.GRAY);
                float baseline = -paint.ascent();
                int width = (int) (paint.measureText(employeeText) + 0.5f);
                int height = (int) (baseline + paint.descent() + 0.5f);
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(employeeText, 0, baseline, paint);

                Layer layer = new Layer();
                ImageEntity employeeText = new ImageEntity(layer, bitmap, motionView.getWidth(), motionView.getHeight());
                motionView.addEntityAndPosition(employeeText);
            }
        });

    }

    //Method to add a sticker to the motionView using a drawable resource id.
    private void addSticker(final int stickerResId) {
        motionView.post(new Runnable() {
            @Override
            public void run() {
                Layer layer = new Layer();
                Bitmap pica = BitmapFactory.decodeResource(getResources(), stickerResId);

                ImageEntity entity = new ImageEntity(layer, pica, motionView.getWidth(), motionView.getHeight());

                motionView.addEntityAndPosition(entity);
            }
        });
    }

    //Method to inflate the top app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sticker_menu, menu);
        return true;
    }

    //Method that sets functions to the top app bar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_add_sticker) {
            Intent intent = new Intent(this, StickerSelectActivity.class);
            startActivityForResult(intent, SELECT_STICKER_REQUEST_CODE);
            return true;
        } else if (item.getItemId() == R.id.main_add_text) {
            printText();
            return true;
        } else if (item.getItemId() == R.id.main_add_save) {
            getEmployeeName();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Method called when getting back from StickerSelectActivity.
     * If the chosen sticker is in the drawable folder: calls addSticker with the id.
     * Otherwise creates a bitmap from the sticker and adds it to motionView.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_STICKER_REQUEST_CODE) {
                if (data != null) {
                    int stickerId = data.getIntExtra(StickerSelectActivity.EXTRA_STICKER_ID, 0);
                    if (stickerId != 0) {
                        addSticker(stickerId);
                    } else {
                        String uriString = data.getStringExtra(EXTRA_STICKER_URI);
                        final Uri uri = Uri.parse(uriString);
                        final ContentResolver contentResolver = this.getContentResolver();
                        motionView.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap;
                                try {
                                    //MediaStore.Images.Media.getBitmap deprecated in API 29
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri));
                                    } else {
                                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
                                    }
                                    Layer layer = new Layer();
                                    ImageEntity selectedSticker = new ImageEntity(layer, bitmap, motionView.getWidth(), motionView.getHeight());
                                    motionView.addEntityAndPosition(selectedSticker);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            }
        }
    }

    /*
     * Method called when the user clicks on save.
     * Shows a dialog where the name of the employee of the month can be entered.
     * When clicked on the positive button calls saveImage()
     */
    public void getEmployeeName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto opslaan");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.save_dialog_layout, (ViewGroup) findViewById(R.id.save_dialog), false);

        final EditText input = viewInflated.findViewById(R.id.inputEmployeeName);
        builder.setView(viewInflated);

        builder.setPositiveButton("Opslaan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                employeeName = input.getText().toString();
                if (checkPermissions(REQUEST_PERMISSION_SAVE)) {
                    saveImage();
                } else {
                    String text = "Geef toestemming tot het geheugen en probeer het opnieuw.";
                    Toast.makeText(StickerActivity.this, text, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Annuleren", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /*
     * Method to save the edited photo.
     * Creates a bitmap from the RelativeLayout that contains the MotionView and the ImageView.
     * Calculates the size of the Imageview and the offset (x, y) needed to crop the created bitmap.
     * Uses the AsyncTask SaveImageTask.
     */
    public void saveImage() {
        View relLayout = findViewById(R.id.stickerRelLayout);
        View imageEdit = findViewById(R.id.toEdit);
        int screenWidth = relLayout.getWidth();
        int screenHeight = relLayout.getHeight();

        width = imageEdit.getWidth();
        height = imageEdit.getHeight();

        x = (screenWidth - width) / 2;
        y = (screenHeight - height) / 2;

        Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        relLayout.draw(c);

        progress = findViewById(R.id.progressBar);
        textProgress = findViewById(R.id.textProgress);

        SaveImageTask task = new SaveImageTask(this);
        task.execute(bitmap);
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

                editedFolder = PhotoUtils.makeFolder(this, FOLDER);
            }
        } else if (requestCode == REQUEST_PERMISSION_SAVE) {
            String text;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                text = "Bedankt voor het geven van toestemming. Je kan nu een foto opslaan.";
            } else {
                text = "Er is toestemming tot het geheugen nodig om een foto op te slaan.";
            }
            Toast.makeText(StickerActivity.this, text, Toast.LENGTH_LONG).show();
        }
    }

    private static class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        /*
         * Static inner class SaveImageTask extending AsyncTask.
         * Param: Bitmap, Progress: Void, Result: Boolean.
         * AsyncTask used to avoid doing heavy work on the main thread.
         * Class is made static to avoid memory leak.
         * Contains a WeakReference to the Activity.
         */
        private WeakReference<StickerActivity> activityReference;

        //Constructor of SaveImageTask, it needs a reference to the StickerActivity.
        SaveImageTask(StickerActivity context) {
            activityReference = new WeakReference<>(context);
        }

        //Show the progress dialog when the task starts.
        @Override
        protected void onPreExecute() {
            StickerActivity activity = activityReference.get();
            if (!(activity == null) && !activity.isFinishing()) {
                activity.textProgress.setVisibility(View.VISIBLE);
                activity.progress.setVisibility(View.VISIBLE);
            }
        }

        /*
         * Work done on the new thread.
         * Creates a cropped version of the input Bitmap and saves this to the destination file.
         * Scans the destination file with the MediaScanner.
         * Creates a new EditedPhoto object and adds it to the editedGallery.
         */
        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];
            boolean success = false;

            StickerActivity activity = activityReference.get();
            if (!(activity == null) && !activity.isFinishing()) {

                Bitmap cropped = Bitmap.createBitmap(bitmap, activity.x, activity.y, activity.width, activity.height);

                String nameToSave;
                String[] nameParts = activity.employeeName.split(" ");
                if (nameParts.length == 1) {
                    nameToSave = activity.employeeName;
                } else {
                    nameToSave = TextUtils.join("-", nameParts);
                }

                Locale nl = new Locale("NL");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", nl).format(new Date());
                String filename = nameToSave + "_" + timeStamp + ".png";
                File destination = new File(activity.editedFolder.getAbsolutePath() + "/" + filename);

                FileOutputStream out;
                try {
                    out = new FileOutputStream(destination);
                    success = cropped.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (success) {
                    MediaScannerConnection.scanFile(activity, new String[]{destination.getPath()}, new String[]{"image/*"}, null);

                    String date = new SimpleDateFormat("EEEE d MMMM yyyy, HH:mm", nl).format(new Date());
                    String[] dateParts = date.split(" ");
                    String month = dateParts[2];
                    Uri uri = Uri.fromFile(destination);
                    int id = Model.getInstance().getEditedGallery().getSize() + 1;
                    EditedPhoto newEdited = new EditedPhoto(filename, date, uri, id, activity.employeeName, month);

                    Model.getInstance().getEditedGallery().addPhoto(newEdited);
                }
            }
            return success;
        }

        /*
         * Work done after receiving boolean from doInBackground.
         * If the photo is saved correctly, starts a new intent to reorder GalleryActivity to front.
         */
        @Override
        protected void onPostExecute(Boolean saved) {
            StickerActivity activity = activityReference.get();
            if (!(activity == null) && !activity.isFinishing()) {
                activity.textProgress.setVisibility(View.GONE);
                activity.progress.setVisibility(View.GONE);
                if (saved) {
                    Intent intent = new Intent(activity, GalleryActivity.class);
                    intent.putExtra(FROM_INTENT_KEY, FROM_INTENT_STICKER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    String text = "Foto kon niet worden opgeslagen.";
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
