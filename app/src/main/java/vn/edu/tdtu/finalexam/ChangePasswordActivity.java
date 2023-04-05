package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText newPassword,oldPassword;
    private DatabaseReference user;
    private String phone;
    private Button btnChangeConfirm;
    private ProgressBar changePassword_progressbarConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();

        newPassword = findViewById(R.id.newPassowrd);
        oldPassword = findViewById(R.id.oldPassword);
        btnChangeConfirm = findViewById(R.id.btn_changeConfirm);
        changePassword_progressbarConfirm = findViewById(R.id.changePassword_progressbarConfirm);

        SharedPreferences sp = ChangePasswordActivity.this.getSharedPreferences("User", MODE_PRIVATE);
        phone = sp.getString("phone", "");
        btnChangeConfirm.setOnClickListener(this);
    }

    private void changePassword()
    {
        String newpass = newPassword.getText().toString().trim();
        String oldpass = oldPassword.getText().toString().trim();

        if(newpass.isEmpty()){
            changePassword_progressbarConfirm.setVisibility(View.GONE);
            newPassword.setError("Vui lòng nhập mật khẩu mới");
            newPassword.requestFocus();
            return;
        }

        if(oldpass.isEmpty()){
            changePassword_progressbarConfirm.setVisibility(View.GONE);
            oldPassword.setError("Vui lòng nhập mật khẩu cũ");
            oldPassword.requestFocus();
            return;
        }

        user.child("users").child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    User u = snapshot.getValue(User.class);
                    if(u.getPassword().equals(oldpass))
                    {
                        user.child("users").child(phone).child("password").setValue(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                changePassword_progressbarConfirm.setVisibility(View.GONE);
                                sendToMain();
                            }
                        });
                    }
                    else
                    {
                        changePassword_progressbarConfirm.setVisibility(View.GONE);
                        Toast.makeText(ChangePasswordActivity.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendToMain(){
        startActivity(new Intent(ChangePasswordActivity.this , MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        changePassword_progressbarConfirm.setVisibility(View.VISIBLE);
        changePassword();
    }
}