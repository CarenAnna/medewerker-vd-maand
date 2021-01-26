package cgroenhuijzen.medewerkervandemaand;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import cgroenhuijzen.medewerkervandemaand.model.EditedPhoto;
import cgroenhuijzen.medewerkervandemaand.model.Photo;
import cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.ui.StickerActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static cgroenhuijzen.medewerkervandemaand.GalleryActivity.FROM_INTENT_GALLERY;
import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.FROM_INTENT_KEY;
import static cgroenhuijzen.medewerkervandemaand.GalleryRecViewAdapter.PHOTO_PARCELABLE_KEY;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class PhotoDetailActivity extends AppCompatActivity {
    /*
     * Activity that controls the photo details window.
     * Displays a photo with the name and date.
     * If an editedPhoto is displayed also shows the month and the name of the employee.
     */

    public static final String PHOTO_DELETED_KEY = "deleted_photo";
    public static final String ID_DELETED_KEY = "deleted_id";

    private TextView txtNameDetail, txtDateDetail, txtNameEmployee, txtMonth;
    private ImageView imgPhotoDetail;
    private FloatingActionButton btnShare, btnEdit;
    private RelativeLayout editedRelLayout;

    private Photo photo;

    /*
     * Method called when the activity is created.
     * Initiates the toolbar and views.
     * Gets the parcelable photo and string from the intent.
     * Hides part of the view depending on the string telling from which intent it comes.
     * Sets the onClickListeners for the two buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        initToolbar();
        initViews();

        Intent intent = getIntent();
        photo = null;
        if (intent != null) {
            String fromIntent = intent.getStringExtra(FROM_INTENT_KEY);
            photo = intent.getParcelableExtra(PHOTO_PARCELABLE_KEY);

            if (fromIntent != null) {
                if (fromIntent.equals("main")) {
                    btnShare.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.VISIBLE);
                    editedRelLayout.setVisibility(View.GONE);
                } else {
                    btnShare.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE);
                    editedRelLayout.setVisibility(View.VISIBLE);
                }
                if (photo != null) {
                    setViews(photo, fromIntent);
                }
            }

            final Photo displayedPhoto = photo;

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (displayedPhoto != null) {
                        Intent share = PhotoUtils.shareImage(displayedPhoto);
                        startActivity(Intent.createChooser(share, "Share Image"));
                    }
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (displayedPhoto != null) {
                        editPhoto(displayedPhoto);
                    }
                }
            });

        }
    }

    //Method to initiate all views.
    private void initViews() {
        txtNameDetail = findViewById(R.id.txtNameDetail);
        txtDateDetail = findViewById(R.id.txtDateDetail);
        txtNameEmployee = findViewById(R.id.txtNameEmployeeDetail);
        txtMonth = findViewById(R.id.txtMonthDetail);
        imgPhotoDetail = findViewById(R.id.imgPhotoDetail);
        btnShare = findViewById(R.id.detailFabShare);
        btnEdit = findViewById(R.id.detailFabEdit);
        editedRelLayout = findViewById(R.id.editedRelLayout);
    }

    /*
     * Method called to set up views in onCreate.
     * Used Glide to set the photo in the ImageView.
     * Sets the name and date text and month and employee if needed.
     */
    private void setViews(Photo photo, String intent) {
        if (photo != null) {
            txtNameDetail.setText(photo.getName());
            txtDateDetail.setText(photo.getDate());

            Glide.with(this).asBitmap()
                    .load(photo.getUri())
                    .into(imgPhotoDetail);

            if (intent.equals(FROM_INTENT_GALLERY)) {
                if (photo instanceof EditedPhoto) {
                    EditedPhoto editedPhoto = (EditedPhoto) photo;
                    txtNameEmployee.setText(editedPhoto.getEmployeeName());
                    String month = editedPhoto.getMonth() + ":";
                    txtMonth.setText(month);
                }
            }
        } else {
            String text = "None, error selecting a photo.";
            txtNameDetail.setText(text);
        }
    }

    /*
     * Method to set the top app bar as toolbar.
     * Also sets the up button to finish this activity when pressed.
     */
    private void initToolbar() {
        MaterialToolbar toolbarDetail = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbarDetail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbarDetail.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Method to inflate the top app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_details, menu);
        return true;
    }


    //Method that sets functions to the top app bar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final Photo displayedPhoto = photo;

        switch (item.getItemId()) {

            //Calls showDeleteDialog() if permission are granted
            case R.id.top_delete:
                if (displayedPhoto != null) {
                    if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                        showDeleteDialog(displayedPhoto, this);
                    } else {
                        Toast.makeText(this, "Geef de app toestemming tot het geheugen.", Toast.LENGTH_LONG).show();
                    }
                }
                return true;

            //calls editPhoto()
            case R.id.top_edit_photo:
                if (displayedPhoto != null) {
                    editPhoto(displayedPhoto);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
     * Method to ask the user if he wants to delete the displayed photo.
     * If clicked yes: calls deletePhoto().
     */
    private void showDeleteDialog(final Photo displayedPhoto, final Context context) {
        String text = "Weet je zeker dat je deze foto wil verwijderen?";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Verwijderen");
        builder.setMessage(text);

        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deletePhoto(displayedPhoto, context);
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Nee", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*
     * Method to set the result of this activity, which was start with startActivityForResult.
     * Lets the other Activity know which photo to delete.
     */
    public void deletePhoto(Photo displayedPhoto, Context context) {
        int id = displayedPhoto.getId();
        boolean deleted = PhotoUtils.deletePhoto(displayedPhoto, context);
        if (deleted) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(PHOTO_DELETED_KEY, true);
            resultIntent.putExtra(ID_DELETED_KEY, id);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    //Method to start the StickerActivity to edit the displayed photo.
    public void editPhoto(Photo displayedPhoto) {
        Intent sticker = new Intent(PhotoDetailActivity.this, StickerActivity.class);
        sticker.putExtra(PHOTO_PARCELABLE_KEY, displayedPhoto);
        startActivity(sticker);
        finish();
    }

}
