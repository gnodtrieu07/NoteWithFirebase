package vn.edu.tdtu.finalexam;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editAddTitle,editAddDescrip;
    private TextView backMain,confirmSave;
    private ImageView AddImage;
    private String phone;
    private LinearLayout layoutFeatures;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private String selectedImagePath;

    private static final int REQUEST_STORAGE_CODE = 5;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private DatabaseReference user;
    private static final int REQUEST_CODE_SELECT_VIDEO = 2;
    private VideoView AddVideo;
    private String selectedVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        
        SharedPreferences sp = AddNoteActivity.this.getSharedPreferences("User", MODE_PRIVATE);
        phone = sp.getString("phone", "");
        editAddTitle = findViewById(R.id.addTitle);
        editAddDescrip = findViewById(R.id.addDescrip);
        AddImage = findViewById(R.id.addImage);
        AddVideo = findViewById(R.id.addVideo);
        backMain = findViewById(R.id.backMain);
        confirmSave = findViewById(R.id.confirmSave);

        selectedImagePath = "";
        selectedVideoPath = "";
        layoutFeatures = findViewById(R.id.layoutFeatures);
        showBottomSheetFeatures();

        backMain.setOnClickListener(this);
        confirmSave.setOnClickListener(this);
        AddImage.setOnClickListener(this);
        AddVideo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backMain:
                startActivity(new Intent(AddNoteActivity.this,MainActivity.class));
                finish();
                break;
            case R.id.confirmSave:
                saveNewNote(phone);
                break;
            case R.id.addImage:
                PopupMenu menu = new PopupMenu(this,v);
                menu.getMenu().add("Xóa");
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals("Xóa")) {
                            AddImage.setImageBitmap(null);
                            AddImage.setVisibility(View.GONE);

                            selectedImagePath = "";
                        }
                        return true;
                    }
                });
                menu.show();
                break;
            case R.id.addVideo:
                PopupMenu videomenu = new PopupMenu(this,v);
                videomenu.getMenu().add("Xóa");
                videomenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals("Xóa")) {
                            AddVideo.stopPlayback();
                            AddVideo.setVisibility(View.GONE);

                            selectedVideoPath = "";
                        }
                        return true;
                    }

                });
                videomenu.show();
                break;
            default:
                break;
        }
    }

    private void showBottomSheetFeatures() {
        bottomSheetBehavior = BottomSheetBehavior.from(layoutFeatures);
        layoutFeatures.findViewById(R.id.textFeatures).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

        });

        layoutFeatures.findViewById(R.id.uploadImageLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(hasPermission())
                {
                    selectedImage();
                }
                else
                {
                    requestPermission();
                }
            }
        });

        layoutFeatures.findViewById(R.id.uploadVideoLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(hasPermission())
                {
                    selectedVideo();
                }
                else
                {
                    requestPermission();
                }
            }
        });
    }

    private void saveNewNote(String phone) {
        String id = UUID.randomUUID().toString();
        String title = editAddTitle.getText().toString().trim();
        String desc = editAddDescrip.getText().toString().trim();
        Date date = Calendar.getInstance().getTime();
        DateFormat today = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault());
        String createdTime = today.format(date);

        Note note = new Note(id,title,desc,createdTime,selectedImagePath,selectedVideoPath);
        user.child("users").child(phone)
                .child("notes").child(id)
                .setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AddVideo.stopPlayback();
                    Toast.makeText(AddNoteActivity.this, "Thêm ghi chú thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddNoteActivity.this,MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(AddNoteActivity.this, "Thêm ghi chú thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//
//                Rect outRect = new Rect();
//                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            }
//        }
//        return super.dispatchTouchEvent(event);
//    }

    public void selectedImage()
    {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(i.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(i,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    public void selectedVideo()
    {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/*");
        if(i.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(i,REQUEST_CODE_SELECT_VIDEO);
        }
    }

    public String getPathImageFromUri(Uri imageUri)
    {
        String imgPath;
        Cursor cursor = getContentResolver().query(
                imageUri,null,null,null,null);
        if(cursor == null)
        {
            imgPath = imageUri.getPath();
        }
        else
        {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex("_data");
            imgPath = cursor.getString(idx);
            cursor.close();
        }
        return imgPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK)
        {
            if(data != null)
            {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null)
                {
                    try{
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        AddImage.setImageBitmap(bitmap);
                        AddImage.setVisibility(View.VISIBLE);

                        selectedImagePath = getPathImageFromUri(selectedImageUri);
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        if(requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK)
        {
            if(data != null)
            {
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null)
                {
                    MediaController controller = new MediaController(this);
                    controller.setAnchorView(AddVideo);
                    controller.setMediaPlayer(AddVideo);
                    AddVideo.setMediaController(controller);
                    AddVideo.setVideoPath(getPathImageFromUri(selectedImageUri));
                    AddVideo.setVisibility(View.VISIBLE);
                    selectedVideoPath = getPathImageFromUri(selectedImageUri);
                }
            }
        }

    }

    public boolean hasPermission(){

        int code = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return code == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
                ,REQUEST_STORAGE_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_STORAGE_CODE && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                selectedImage();
                selectedVideo();
            }
            else
            {
                Toast.makeText(this, "No Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

}