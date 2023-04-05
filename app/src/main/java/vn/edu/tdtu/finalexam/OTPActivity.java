package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText otpVerify;
    private TextView skip;
    private Button verify;
    private String verificationId,phone;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_resgister);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        
        mAuth = FirebaseAuth.getInstance();
        otpVerify = findViewById(R.id.otpVerify);
        verify = findViewById(R.id.verify);
        skip = findViewById(R.id.skip);
        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phone");
        verify.setOnClickListener(this);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToMain();
            }
        });
    }

    @Override
    public void onClick(View view) {
        String otp = otpVerify.getText().toString();
        if(otp.isEmpty())
        {
            Toast.makeText(OTPActivity.this, "Hãy nhập mã OTP", Toast.LENGTH_SHORT).show();
        }
        else
        {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId , otp);
            signIn(credential);
        }
    }

    private void signIn(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    user.child("users").child(phone).child("active").setValue(true);
                    sendToMain();
                }else{
                    Toast.makeText(OTPActivity.this, "Xác minh thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToMain(){
        startActivity(new Intent(OTPActivity.this , MainActivity.class));
        finish();
    }

}