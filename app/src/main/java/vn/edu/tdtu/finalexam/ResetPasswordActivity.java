package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editpasswordReset;
    private Button resetConfirm;
    private ProgressBar resetProgress;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String phoneReset;
    private DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        editpasswordReset = findViewById(R.id.passwordReset);
        resetProgress = findViewById(R.id.forgotPassword_progressbarConfirm);
        resetConfirm = findViewById(R.id.btn_resetConfirm);
        phoneReset = getIntent().getStringExtra("phoneReset");
        resetConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String passwordReset = editpasswordReset.getText().toString().trim();
        if(passwordReset.isEmpty()){
            editpasswordReset.setError("Vui lòng nhập mật khẩu");
            editpasswordReset.requestFocus();
            return;
        }

        if(editpasswordReset.length() < 6){
            editpasswordReset.setError("Mật khẩu phải có độ dài tối thiểu 6 ký tự");
            editpasswordReset.requestFocus();
            return;
        }
        user.child("users").child(phoneReset).child("password").setValue(passwordReset);
        sendToLogin();
    }

    private void sendToLogin(){
        Toast.makeText(ResetPasswordActivity.this, "Khôi phục mật khẩu thành công", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ResetPasswordActivity.this , LoginActivity.class));
        finish();
    }
}