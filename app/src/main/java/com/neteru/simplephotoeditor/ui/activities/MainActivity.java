package com.neteru.simplephotoeditor.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.neteru.simplephotoeditor.R;
import com.vorlonsoft.android.rate.AppRate;
import com.vorlonsoft.android.rate.StoreType;
import com.vorlonsoft.android.rate.Time;

import java.io.File;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.neteru.simplephotoeditor.core.classes.Constants.TEMP_PICTURE_DIRECTORY;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {

    @BindString(R.string.market_dev_name) String market_dev_name;

    private final static int COLLECTION_REQUEST = 2;
    private final static int GALLERY_REQUEST = 1;
    private final static int CAMERA_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setAppRateSystem();
    }

    @OnClick(R.id.collection)
    void onCollectionClick(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, COLLECTION_REQUEST);
    }

    @OnClick(R.id.camera)
    void onCameraClick(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST);
    }

    @OnClick(R.id.gallery)
    void onGalleryClick(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST);
    }

    @OnClick(R.id.rate_us)
    void onRateUsClick(){

        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        }else{

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        }

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" +getPackageName())));
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @OnClick(R.id.share_app)
    void onShareAppClick(){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_txt)+"\n https://play.google.com/store/apps/details?id="+getPackageName()+"\n\n");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_application)));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @OnClick(R.id.more_app)
    void onMoreAppClick(){

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:"+market_dev_name)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/developer?id="+market_dev_name)));
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case CAMERA_REQUEST:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri cameraUri;
                boolean success = true;
                File storageDir = new File(TEMP_PICTURE_DIRECTORY);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (!storageDir.exists()) {
                    success = storageDir.mkdirs();
                }

                if (success) {
                    File file = new File(storageDir, "temp-original.jpg");
                    cameraUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                    cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
                break;

            case GALLERY_REQUEST:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), GALLERY_REQUEST);
                break;

            case COLLECTION_REQUEST:
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(this, CollectionActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

        }

    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case CAMERA_REQUEST:
                    Uri cameraUri = null;
                    boolean success = true;
                    File storageDir = new File(TEMP_PICTURE_DIRECTORY);

                    if (!storageDir.exists()){success = storageDir.mkdirs();}

                    if (success) {
                        File file = new File(storageDir, "temp-original.jpg");
                        cameraUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                    }

                    if (cameraUri == null) return;

                    startActivity(new Intent(this, ImageEditorActivity.class).putExtra("uriStr", cameraUri.toString()));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;

                case GALLERY_REQUEST:
                    Uri galleryUri = data.getData();
                    if (galleryUri == null) return;
                    startActivity(new Intent(this, ImageEditorActivity.class).putExtra("uriStr", galleryUri.toString()));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;

            }
        }
    }

    /**
     * Initialisation du système de notation
     */
    private void setAppRateSystem() {

        AppRate.with(this)
                .setStoreType(StoreType.GOOGLEPLAY)
                .setTimeToWait(Time.DAY, (short) 10) // default is 10 days, 0 means install millisecond, 10 means app is launched 10 or more time units later than installation
                .setLaunchTimes((byte) 10)          // default is 10, 3 means app is launched 3 or more times
                .setRemindTimeToWait(Time.DAY, (short) 1) // default is 1 day, 1 means app is launched 1 or more time units after neutral button clicked
                .setRemindLaunchesNumber((byte) 0)  // default is 0, 1 means app is launched 1 or more times after neutral button clicked
                .setSelectedAppLaunches((byte) 1)   // default is 1, 1 means each launch, 2 means every 2nd launch, 3 means every 3rd launch, etc
                .setShowLaterButton(true)           // default is true, true means to show the Neutral button ("Remind me later").
                .setVersionCodeCheck(false)          // default is false, true means to re-enable the Rate Dialog if a new version of app with different version code is installed
                .setVersionNameCheck(false)          // default is false, true means to re-enable the Rate Dialog if a new version of app with different version name is installed
                .setDebug(false)                    // default is false, true is for development only, true ensures that the Rate Dialog will be shown each time the app is launched
                .setOnClickButtonListener(which -> Log.d(MainActivity.this.getLocalClassName(), Byte.toString(which)))
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);

    }

}
