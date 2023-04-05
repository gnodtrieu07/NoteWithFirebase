package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class OTPResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editCountrycode,editphoneNumber,editOTPreset;
    private Button reset,btnOTPReset;
    private FirebaseAuth mAuth;
    private ProgressBar resetProgress;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId;
    private DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_resetpassword);
        
        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        mAuth = FirebaseAuth.getInstance();
        editCountrycode = findViewById(R.id.country_codeReset);
        editphoneNumber = findViewById(R.id.phoneReset);
        editOTPreset = findViewById(R.id.OTPStringReset);
        btnOTPReset = findViewById(R.id.btnOTPReset);
        btnOTPReset.setOnClickListener(this);
        resetProgress = findViewById(R.id.forgotPassword_progressbar);
        reset = findViewById(R.id.btn_reset);
        reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                resetPassword();
                break;
            case R.id.btnOTPReset:
                receiveOTPreset();
                break;
            default:
                break;
        }
    }

    private void receiveOTPreset() {
        String phone = editphoneNumber.getText().toString().trim();
        String Country_code = editCountrycode.getText().toString().trim();

        if(Country_code.isEmpty()){
            editCountrycode.setError("Vui lòng nhập mã vùng");
            editCountrycode.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            editphoneNumber.setError("Vui lòng nhập số điện thoại");
            editphoneNumber.requestFocus();
            return;
        }

        if(!phone.matches("^[0-9]{9,11}$")){
            editphoneNumber.setError("Vui lòng nhập số điện hợp lệ");
            editphoneNumber.requestFocus();
            return;
        }


        String formatPhone = "+" + Country_code + phone;
        resetProgress.setVisibility(View.VISIBLE);

        user.child("users").child(formatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                {
                    resetProgress.setVisibility(View.GONE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(OTPResetPasswordActivity.this);
                    builder.setMessage("Số điện thoại chưa đăng ký");
                    builder.setCancelable(true);

                    builder.setPositiveButton(
                            "Xác nhận",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else {
                    sendVerificationCode(formatPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void resetPassword() {
        String phone = editphoneNumber.getText().toString().trim();
        String Country_code = editCountrycode.getText().toString().trim();
        String OTPreset = editOTPreset.getText().toString().trim();

        if(Country_code.isEmpty()){
            editCountrycode.setError("Vui lòng nhập mã vùng");
            editCountrycode.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            editphoneNumber.setError("Vui lòng nhập số điện thoại");
            editphoneNumber.requestFocus();
            return;
        }

        if(!phone.matches("^[0-9]{9,11}$")){
            editphoneNumber.setError("Vui lòng nhập số điện hợp lệ");
            editphoneNumber.requestFocus();
            return;
        }

        if(OTPreset.isEmpty()){
            editOTPreset.setError("Vui lòng nhập mã OTP");
            editOTPreset.requestFocus();
            resetProgress.setVisibility(View.GONE);
            return;
        }
        else
        {
            verifyCodeReset(OTPreset);
        }
    }

    private void sendVerificationCode(String formatPhone) {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(OTPResetPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                resetProgress.setVisibility(View.GONE);
                Toast.makeText(OTPResetPasswordActivity.this, "Mã OTP đã được gửi đến số điện thoại", Toast.LENGTH_SHORT).show();
                verificationId = s;
            }

        };
//        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(formatPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPResetPasswordActivity.this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signIn(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    sendToReset();
                }else{
                    Toast.makeText(OTPResetPasswordActivity.this, "Xác minh thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyCodeReset(String code) {
        if(verificationId == null)
        {
            Toast.makeText(OTPResetPasswordActivity.this, "Xác minh thất mã không tồn tại", Toast.LENGTH_SHORT).show();
        }
        else
        {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signIn(credential);
        }
    }

    private void sendToReset() {
        String fullphone = "+" + editCountrycode.getText().toString().trim() + editphoneNumber.getText().toString().trim();
        Intent i = new Intent(OTPResetPasswordActivity.this , ResetPasswordActivity.class);
        i.putExtra("phoneReset",fullphone);
        startActivity(i);
        finish();
    }
}