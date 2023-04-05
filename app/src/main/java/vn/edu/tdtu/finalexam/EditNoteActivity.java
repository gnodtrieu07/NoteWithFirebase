package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MenuItem;
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

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EditNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTitle,editDescrip;
    private TextView backMain,confirmEdit;
    private ImageView editImage;
    private VideoView editVideo;
    private String phone;
    private String id;
    private String title;
    private String desc;
    private String imgPath;
    private String videoPath;

    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private LinearLayout layoutFeatures;

    private static final int REQUEST_STORAGE_CODE = 5;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private  static final int REQUEST_CODE_SELECT_VIDEO = 2;
    private DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        
        SharedPreferences sp = EditNoteActivity.this.getSharedPreferences("User", MODE_PRIVATE);
        phone = sp.getString("phone", "");
        editTitle = findViewById(R.id.editTitle);
        editDescrip = findViewById(R.id.editDescrip);
        backMain = findViewById(R.id.editbackMain);
        confirmEdit = findViewById(R.id.confirmEdit);

        editImage = findViewById(R.id.editImage);
        editVideo = findViewById(R.id.editVideo);

        id = getIntent().getStringExtra("id");
        title = getIntent().getStringExtra("title");
        desc = getIntent().getStringExtra("desc");
        imgPath = getIntent().getStringExtra("imgPath");
        videoPath = getIntent().getStringExtra("videoPath");
        editTitle.setText(title);
        editDescrip.setText(desc);
        if(!imgPath.isEmpty())
        {
            editImage.setImageBitmap(BitmapFactory.decodeFile(imgPath));
            editImage.setVisibility(View.VISIBLE);
        }
         else
        {
            editImage.setVisibility(View.GONE);
        }


        layoutFeatures = findViewById(R.id.layoutFeatures);
        showBottomSheetFeatures();

        backMain.setOnClickListener(this);
        confirmEdit.setOnClickListener(this);
        editImage.setOnClickListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editbackMain:
                startActivity(new Intent(EditNoteActivity.this,MainActivity.class));
                finish();
                break;
            case R.id.confirmEdit:
                editNewNote(phone);
                break;
            case R.id.editImage:
                PopupMenu menu = new PopupMenu(this,v, Gravity.CENTER);
                menu.getMenu().add("Xóa");
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals("Xóa")) {
                            editImage.setImageBitmap(null);
                            editImage.setVisibility(View.GONE);

                            imgPath = "";
                        }
                        return true;
                    }
                });
                menu.show();
            case R.id.editVideo:
                PopupMenu videomenu = new PopupMenu(this,v);
                videomenu.getMenu().add("Xóa");
                videomenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals("Xóa")) {
                            editVideo.stopPlayback();
                            editVideo.setVisibility(View.GONE);

                            videoPath = "";
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

    private void editNewNote(String phone) {
        String title = editTitle.getText().toString().trim();
        String desc = editDescrip.getText().toString().trim();
        Date date = Calendar.getInstance().getTime();
        DateFormat today = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault());
        String createdTime = today.format(date);

        Note note = new Note(id,title,desc,createdTime,imgPath,videoPath);
        user.child("users").child(phone).child("notes").child(id).setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(EditNoteActivity.this, "Sửa ghi chú thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditNoteActivity.this,MainActivity.class));
                    finish();
                }
                else {
                    Toast.makeText(EditNoteActivity.this, "Sửa ghi chú thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
    }

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
                        editImage.setImageBitmap(bitmap);
                        editImage.setVisibility(View.VISIBLE);

                        imgPath = getPathImageFromUri(selectedImageUri);
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
                    controller.setAnchorView(editVideo);
                    controller.setMediaPlayer(editVideo);
                    editVideo.setMediaController(controller);
                    editVideo.setVideoPath(getPathImageFromUri(selectedImageUri));
                    editVideo.setVisibility(View.VISIBLE);
                    videoPath = getPathImageFromUri(selectedImageUri);
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