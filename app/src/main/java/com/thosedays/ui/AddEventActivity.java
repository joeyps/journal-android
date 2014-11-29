package com.thosedays.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.thosedays.provider.EventContract;
import com.thosedays.sync.SyncHelper;
import com.thosedays.util.AccountUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import joey.thosedays.R;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/18.
 */
public class AddEventActivity extends BaseActivity {

    private static final String TAG = makeLogTag(AddEventActivity.class);

    private static final int REQUEST_PICK_PHOTO = 10001;

    private EditText mEditViewDescription;
    private ImageView mImageViewMain;
    private Uri mPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mEditViewDescription = (EditText) findViewById(R.id.edit_description);

        View btnPost = findViewById(R.id.button_post);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
                SyncHelper.requestManualSync(AccountUtils.getActiveAccount(AddEventActivity.this), true);
                Toast.makeText(AddEventActivity.this, "Event has posted", Toast.LENGTH_LONG).show();
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, null);
                } else {
                    getParent().setResult(Activity.RESULT_OK, null);
                }
                finish();
            }
        });

        mImageViewMain = (ImageView)findViewById(R.id.img_main);
        mImageViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
            }
        });

        Uri imageUri = getIntent().getExtras().getParcelable(Intent.EXTRA_STREAM);
        if (imageUri != null)
            loadPhoto(imageUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            loadPhoto(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void loadPhoto(Uri imageUri) {
        LOGD(TAG, "select photo uri=" + imageUri);
        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            final Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);

            LOGD(TAG, "bitmap w=" + bitmap.getWidth() + " h=" + bitmap.getHeight());
            mImageViewMain.setImageBitmap(bitmap);
            mPhotoUri = imageUri;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the session feedback using the appropriate content provider.
     */
    private void save() {

        ContentValues values = new ContentValues();
        values.put(EventContract.Events.EVENT_ID, "");
        values.put(EventContract.Events.EVENT_DESCRIPTION, mEditViewDescription.getText().toString());
        values.put(EventContract.Events.PHOTO_URL, mPhotoUri == null ? "" : mPhotoUri.toString());
        values.put(EventContract.Events.PHOTO_WIDTH, 0);
        values.put(EventContract.Events.PHOTO_HEIGHT, 0);
        values.put(EventContract.Events.SYNCED, 0);

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        //Time in GMT
        values.put(EventContract.Events.EVENT_TIME, dateFormatGmt.format(new Date()));


        Uri uri = getContentResolver().insert(EventContract.Events.CONTENT_URI, values);
        LOGD(TAG, null == uri ? "No event was saved" : uri.toString());
    }
}
