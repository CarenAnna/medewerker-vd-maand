package cgroenhuijzen.medewerkervandemaand;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import cgroenhuijzen.medewerkervandemaand.model.Model;
import cgroenhuijzen.medewerkervandemaand.model.Photo;
import cgroenhuijzen.medewerkervandemaand.photoedit.motionviews.ui.StickerActivity;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static cgroenhuijzen.medewerkervandemaand.GalleryActivity.FROM_INTENT_GALLERY;
import static cgroenhuijzen.medewerkervandemaand.MainActivity.FROM_INTENT_MAIN;
import static cgroenhuijzen.medewerkervandemaand.PhotoDetailActivity.ID_DELETED_KEY;
import static cgroenhuijzen.medewerkervandemaand.PhotoDetailActivity.PHOTO_DELETED_KEY;

/**
 * Medewerker van de maand app
 *
 * @author Caren Groenhuijzen
 * NOVI Hogeschool - SD-Praktijk 1
 * 14-08-2020
 */

public class GalleryRecViewAdapter extends RecyclerView.Adapter<GalleryRecViewAdapter.ViewHolder> implements ActionMode.Callback {
    /*
     * Adapter used for the RecyclerViews from both the MainActivity and the GalleryActivity.
     * Implements ActionMode.Callback to allow multiselect with a contextual action bar.
     * Contains an inner class: ViewHolder.
     */

    public static final String FROM_INTENT_KEY = "fromIntent";
    public static final String PHOTO_PARCELABLE_KEY = "photo";
    public static final int REQUEST_DETAIL_ACTIVITY = 25;

    private ArrayList<Photo> photos = new ArrayList<>();
    private Context context;
    private String fromIntent;

    private MenuItem menuItemEdit;
    private MenuItem menuItemShare;
    private ActionMode actionMode;

    private boolean multiSelect = false;
    private ArrayList<Photo> selectedPhotos = new ArrayList<>();
    private int numberSelected;

    /*
     * Constructor of the adapter.
     * Needs a Context and a String.
     * String is used to know if the adapter is used for Photos or EditedPhotos.
     */
    public GalleryRecViewAdapter(Context context, String fromIntent) {
        this.context = context;
        this.fromIntent = fromIntent;
    }

    //Method to create the ViewHolder.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_photo, parent, false);
        return new ViewHolder(view);
    }

    /*
     * Method to set the views.
     * Uses Glide library to set the ImageViews.
     * Sets the onClick and onLongClick listeners.
     * Sets the transition from expanded to collapsed.
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Photo currentPhoto = photos.get(position);

        holder.txtName.setText(currentPhoto.getName());
        holder.txtDate.setText(currentPhoto.getDate());

        Glide.with(context).asBitmap()
                .load(currentPhoto.getUri())
                .into(holder.imgPhoto);

        if (selectedPhotos.contains(currentPhoto)) {
            holder.parent.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            holder.parent.setCardBackgroundColor(Color.WHITE);
        }

        /*
         * Sets the onClickListener on the RecyclerView items.
         * When in multiselect mode, calls selectPhoto().
         * Otherwise starts a new intent to go to the PhotoDetailActivity.
         * Adds the clicked photo as Parcelable and the fromIntent as String to the intent.
         */
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (multiSelect) {
                    selectPhoto(holder, currentPhoto);

                    if (numberSelected > 1) {
                        menuItemEdit.setVisible(false);
                    } else {
                        menuItemEdit.setVisible(true);
                    }
                } else {
                    Intent intent = new Intent(context, PhotoDetailActivity.class);
                    intent.putExtra(FROM_INTENT_KEY, fromIntent);
                    intent.putExtra(PHOTO_PARCELABLE_KEY, currentPhoto);
                    ((Activity) context).startActivityForResult(intent, REQUEST_DETAIL_ACTIVITY);
                }
            }
        });

        /*
         * Sets the onLongClickListener on the RecyclerView items.
         * Sets multiSelect to true and starts the action mode.
         * Calls selectPhoto() with the long clicked photo.
         */
        holder.parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!multiSelect) {
                    multiSelect = true;
                    ((Activity) context).startActionMode(GalleryRecViewAdapter.this);
                    selectPhoto(holder, currentPhoto);
                }
                return true;
            }
        });

        if (currentPhoto.isExpanded()) {
            TransitionManager.beginDelayedTransition(holder.parent);
            holder.detailRelLayout.setVisibility(View.VISIBLE);
            holder.arrowDown.setVisibility(View.GONE);
        } else {
            TransitionManager.beginDelayedTransition(holder.parent);
            holder.detailRelLayout.setVisibility(View.GONE);
            holder.arrowDown.setVisibility(View.VISIBLE);
        }

    }

    /*
     * Method to add or remove a photo from the selectedPhotos ArrayList.
     * Used in the action mode.
     * Changes the color of selected items and sets the title to the number of items selected.
     */
    private void selectPhoto(ViewHolder holder, Photo currentPhoto) {
        if (selectedPhotos.contains(currentPhoto)) {
            selectedPhotos.remove(currentPhoto);
            holder.parent.setCardBackgroundColor(Color.WHITE);
        } else {
            selectedPhotos.add(currentPhoto);
            holder.parent.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
        numberSelected = selectedPhotos.size();
        actionMode.setTitle(numberSelected + " geselecteerd");
    }

    //Method to expand all cards in the RecyclerView at once.
    public void expandAll() {
        for (Photo photo : photos) {
            photo.setExpanded(true);
        }
        notifyDataSetChanged();
    }

    //Method to collapse all cards in the RecyclerView at once.
    public void collapseAll() {
        for (Photo photo : photos) {
            photo.setExpanded(false);
        }
        notifyDataSetChanged();
    }

    //Method to add photo to the photos ArrayList.
    public void addPhoto(Photo photo) {
        photos.add(photo);
        notifyItemInserted(photos.size() - 1);
    }

    //Returns the size of the photos ArrayList.
    @Override
    public int getItemCount() {
        return photos.size();
    }

    //Returns the photos ArrayList.
    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    //Sets the photos ArrayList to photos. Notifies data set changed.
    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    public boolean getPhotoByName(String name) {
        for(Photo photo : photos) {
            if(photo.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Method to delete a photo from the model and the adapter.
     * Used when a photo is deleted from the PhotoDetailActivity.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL_ACTIVITY) {
            if (data != null) {
                boolean deleted = data.getBooleanExtra(PHOTO_DELETED_KEY, false);
                int id = data.getIntExtra(ID_DELETED_KEY, -1);
                if (deleted && id != -1) {
                    photos.remove(id - 1);
                    notifyItemRemoved(id - 1);
                    notifyItemRangeChanged(id - 1, photos.size());

                    if (fromIntent.equals(FROM_INTENT_MAIN)) {
                        Model.getInstance().getGallery().deletePhotoById(id);
                    } else if (fromIntent.equals(FROM_INTENT_GALLERY)) {
                        Model.getInstance().getEditedGallery().deletePhotoById(id);
                    }
                }
            }
        }
    }

    //Method to create the action mode menu.
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.clear();
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.top_contextual, menu);
        actionMode = mode;
        Menu actionMenu = mode.getMenu();

        menuItemShare = actionMenu.findItem(R.id.action_share);
        menuItemEdit = actionMenu.findItem(R.id.action_edit);

        return true;
    }

    //Hides the share icon in the contextual action bar if in MainActivity.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (fromIntent.equals(FROM_INTENT_MAIN)) {
            menuItemShare.setVisible(false);
            return true;
        } else return false;
    }


    // Method to set functions to the items in the contextual action bar.
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (selectedPhotos.size() > 0) {
            switch (item.getItemId()) {

                //Calls actionDelete with the selected photos.
                case R.id.action_delete:
                    ArrayList<Photo> toDelete = new ArrayList<>(selectedPhotos);
                    actionDelete(toDelete);
                    mode.finish();
                    return true;

                //Creates a new intent to go to StickerActivity, with the selected photo as Parcelable.
                case R.id.action_edit:
                    Photo toEdit = selectedPhotos.get(0);
                    Intent sticker = new Intent(context, StickerActivity.class);
                    sticker.putExtra(PHOTO_PARCELABLE_KEY, toEdit);
                    context.startActivity(sticker);
                    mode.finish();
                    return true;

                case R.id.action_share:
                    //To share one photo, shareImage function is SharedFunctions is used.
                    if (selectedPhotos.size() == 1) {
                        Intent share = PhotoUtils.shareImage(selectedPhotos.get(0));
                        context.startActivity(Intent.createChooser(share, "Share Image"));
                        context.startActivity(Intent.createChooser(share, "Share Image"));

                        //To share multiple photos at once.
                    } else {
                        ArrayList<Uri> photoUris = new ArrayList<>();
                        for (Photo photo : selectedPhotos) {
                            photoUris.add(photo.getUri());
                        }
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Medewerker van de maand foto's.");
                        intent.setType("image/*");
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photoUris);
                        context.startActivity(intent);
                    }
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        } else {
            String select = "Selecteer eerst een foto.";
            Toast.makeText(context, select, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /*
     * Method to ask the user if he wants to delete the selected items using a dialog.
     * If clicked yes: calls deletePhotos with toDelete ArrayList of photos.
     */
    private void actionDelete(final ArrayList<Photo> toDelete) {
        String text;
        int numberSelected = toDelete.size();
        if (numberSelected == 1) {
            String name = toDelete.get(0).getName();
            text = "Weet je zeker dat je " + name + " wil verwijderen?";
        } else {
            text = "Weet je zeker dat je " + numberSelected + " foto's wil verwijderen?";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Verwijderen");
        builder.setMessage(text);

        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
                    deletePhotos(toDelete);
                } else {
                    Toast.makeText(context, "Geef de app toestemming tot het geheugen.", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Nee", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*
     * Method to delete photos from the model and the adapter.
     * Used when photos are deleted in the action mode.
     */
    public void deletePhotos(ArrayList<Photo> toDelete) {
        for (Photo photo : toDelete) {
            int id = photo.getId();
            boolean deleted = PhotoUtils.deletePhoto(photo, context);
            if (deleted) {
                if (fromIntent.equals(FROM_INTENT_MAIN)) {
                    Model.getInstance().getGallery().deletePhotoById(id);
                } else if (fromIntent.equals(FROM_INTENT_GALLERY)) {
                    Model.getInstance().getEditedGallery().deletePhotoById(id);
                }
                photos.remove(photo);
                notifyItemRemoved(id - 1);
                notifyItemRangeChanged(id - 1, photos.size());
            }
        }
    }

    //Method called when action mode is stopped. Clears the selectedPhotos ArrayList.
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        multiSelect = false;
        selectedPhotos.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        /*
         * Inner class Viewholder.
         * RecyclerView - Adapter - ViewHolder pattern used in this application.
         * This class stores all the important Views.
         * Sets the onClick listeners for the arrowUp and arrowDown buttons.
         */
        private MaterialCardView parent;
        private ImageView imgPhoto;
        private ImageView arrowDown;
        private TextView txtName, txtDate;
        private RelativeLayout detailRelLayout;

        //Constructor of ViewHolder class, needs a View.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            txtName = itemView.findViewById(R.id.txtName);

            arrowDown = itemView.findViewById(R.id.btnArrowDown);
            ImageView arrowUp = itemView.findViewById(R.id.btnArrowUp);
            txtDate = itemView.findViewById(R.id.txtDate);
            detailRelLayout = itemView.findViewById(R.id.detailRelLayout);

            arrowDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandOrCollapse();
                }
            });

            arrowUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandOrCollapse();
                }
            });

        }

        //Method to expand or collapse a card, used with the arrow buttons.
        public void expandOrCollapse() {
            int position = getAdapterPosition();
            Photo photo = photos.get(position);
            photo.setExpanded(!photo.isExpanded());
            notifyItemChanged(position);
        }
    }
}
