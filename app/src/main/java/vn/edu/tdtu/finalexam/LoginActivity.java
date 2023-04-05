package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import java.text.NumberFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView createAccount,forgotPassword;
    private EditText editPhone,editCountrycode,OTPString,editPassword;
    private Button btnLogin,btnOTP;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar3;
    private String verificationId;
    private DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        
        mAuth = FirebaseAuth.getInstance();
        editPhone = findViewById(R.id.phone);
        editCountrycode = findViewById(R.id.country_code);
        editPassword = findViewById(R.id.txtPassword);
        OTPString = findViewById(R.id.OTPString);
        createAccount = findViewById(R.id.createAccount);
        forgotPassword = findViewById(R.id.forgotPassword);
        progressBar3 = findViewById(R.id.progressBar3);

        btnLogin = findViewById(R.id.btn_login);
        btnOTP  = findViewById(R.id.btnOTP);
        btnLogin.setOnClickListener(this);
        btnOTP.setOnClickListener(this);
        createAccount.setOnClickListener(this);

//        SharedPreferences sp =LoginActivity.this.getSharedPreferences("User", MODE_PRIVATE);
//
//        String phone = sp.getString("phone", "");
//        Toast.makeText(this, phone, Toast.LENGTH_SHORT).show();

//        if(phone.length() == 0)
//        {
//            btnLogin = findViewById(R.id.btn_login);
//            btnOTP  = findViewById(R.id.btnOTP);
//            btnLogin.setOnClickListener(this);
//            btnOTP.setOnClickListener(this);
//            createAccount.setOnClickListener(this);
//        }
//        else
//        {
//            sendToMain();
//        }
        btnLogin = findViewById(R.id.btn_login);
        btnOTP  = findViewById(R.id.btnOTP);
        btnLogin.setOnClickListener(this);
        btnOTP.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        createAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.createAccount:
                Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(i);
                finish();
                break;

            case R.id.forgotPassword:
                Intent i1 = new Intent(LoginActivity.this,OTPResetPasswordActivity.class);
                startActivity(i1);
                finish();
                break;

            case R.id.btn_login:
                LoginVerification();
                break;
            case R.id.btnOTP:
                ReceiveOTP();
            default:
                break;
        }

    }

    private void sendToMain(){
        startActivity(new Intent(LoginActivity.this , MainActivity.class));
        finish();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void LoginVerification()
    {
        hideSoftKeyboard(LoginActivity.this);
        String phoneNumber = editPhone.getText().toString().trim();
        String Country_code = editCountrycode.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String OTP = OTPString.getText().toString().trim();
        String fullPhone =  "+" + Country_code + removeZeroPhoneNumber(phoneNumber);

        if(Country_code.isEmpty()){
            editCountrycode.setError("Vui lòng nhập mã vùng");
            editCountrycode.requestFocus();
            return;
        }

        if(phoneNumber.isEmpty()){
            editPhone.setError("Vui lòng nhập số điện thoại");
            editPhone.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editPassword.setError("Vui lòng nhập mật khẩu");
            editPassword.requestFocus();
            return;
        }

        if(OTP.isEmpty()){
            OTPString.setError("Vui lòng nhập mã OTP");
            OTPString.requestFocus();
            progressBar3.setVisibility(View.GONE);
            return;
        }
        else
        {
            progressBar3.setVisibility(View.VISIBLE);
            user.child("users").child(fullPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists())
                    {
                        User user = snapshot.getValue(User.class);
                        if(user.getPassword().equals(password))
                        {
                            verifyCode(OTP);
                        }
                        else
                        {
                            progressBar3.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Mật khẩu không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        progressBar3.setVisibility(View.GONE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void ReceiveOTP()
    {
        String phoneNumber = editPhone.getText().toString().trim();
        String Country_code = editCountrycode.getText().toString().trim();
        String fullPhone =  "+" + Country_code + removeZeroPhoneNumber(phoneNumber);

        if(Country_code.isEmpty()){
            editCountrycode.setError("Vui lòng nhập mã vùng");
            editCountrycode.requestFocus();
            return;
        }

        if(phoneNumber.isEmpty()){
            editPhone.setError("Vui lòng nhập số điện thoại");
            editPhone.requestFocus();
            return;
        }

        if(!phoneNumber.matches("^[0-9]{9,11}$")){
            editPhone.setError("Vui lòng nhập số điện hợp lệ");
            editPhone.requestFocus();
            return;
        }

        progressBar3.setVisibility(View.VISIBLE);
        user.child("users").orderByChild("phone").equalTo(fullPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                {
                    progressBar3.setVisibility(View.GONE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                    sendVerificationCode(fullPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
//        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(LoginActivity.this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            progressBar3.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Mã OTP đã được gửi đến số điện thoại", Toast.LENGTH_SHORT).show();
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                progressBar3.setVisibility(View.GONE);
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            progressBar3.setVisibility(View.GONE);
            // displaying error message with firebase exception.
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String phoneNumber = editPhone.getText().toString().trim();
                            String Country_code = editCountrycode.getText().toString().trim();
                            String fullPhone =  "+" + Country_code + removeZeroPhoneNumber(phoneNumber);

                            SharedPreferences sp= getSharedPreferences("User", MODE_PRIVATE);
                            SharedPreferences.Editor Ed=sp.edit();
                            Ed.putString("phone",fullPhone);
                            Ed.apply();
                            sendToMain();
                        } else {
                            Toast.makeText(LoginActivity.this, "Xác minh thất bại", Toast.LENGTH_SHORT).show();
                        }
                        progressBar3.setVisibility(View.GONE);
                    }
                });
    }

    private void verifyCode(String code) {
        if(verificationId == null)
        {
            Toast.makeText(LoginActivity.this, "Xác minh thất mã không tồn tại", Toast.LENGTH_SHORT).show();
            progressBar3.setVisibility(View.GONE);
        }
        else
        {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sp =LoginActivity.this.getSharedPreferences("User", MODE_PRIVATE);
        String phone = sp.getString("phone", "");

        if(phone.length() > 0)
        {
            sendToMain();
        }
    }

    private String removeZeroPhoneNumber(String phone)
    {
        return phone.replaceFirst("^0+(?!$)", "");
    }
}