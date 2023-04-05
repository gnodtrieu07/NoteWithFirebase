package vn.edu.tdtu.finalexam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editFullname,editCountrycode,editPhone,editPassword;
    private Button register;
    private ProgressBar progressBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String verificationId;
    private DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();
        mAuth = FirebaseAuth.getInstance();

        editFullname = findViewById(R.id.username);
        editCountrycode = findViewById(R.id.country_codeRegister);
        editPhone = findViewById(R.id.phoneNumber);
        editPassword = findViewById(R.id.password);

        register = findViewById(R.id.btn_register);
        register.setOnClickListener(this);

        progressBar = findViewById(R.id.register_progressbar);
    }

    @Override
    public void onClick(View v) {
        registerUser();
    }

    private void registerUser() {
        String username = editFullname.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String Country_code = editCountrycode.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if(username.isEmpty()){
            editFullname.setError("Vui lòng nhập họ tên");
            editFullname.requestFocus();
            return;
        }

        if(Country_code.isEmpty()){
            editCountrycode.setError("Vui lòng nhập mã vùng");
            editCountrycode.requestFocus();
            return;
        }

        if(phone.isEmpty()){
            editPhone.setError("Vui lòng nhập số điện thoại");
            editPhone.requestFocus();
            return;
        }

        if(!phone.matches("^[0-9]{9,11}$")){
            editPhone.setError("Vui lòng nhập số điện hợp lệ");
            editPhone.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editPassword.setError("Vui lòng nhập mật khẩu");
            editPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            editPassword.setError("Mật khẩu phải có độ dài tối thiểu 6 ký tự");
            editPassword.requestFocus();
            return;
        }

        String formatPhone = "+" + Country_code + phone;

        progressBar.setVisibility(View.VISIBLE);

//        mAuth.getFirebaseAuthSettings().set(true);

        Log.d("TAG",formatPhone);
        user.child("users") .orderByChild("phone").equalTo(formatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null)
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, "Số điện thoại đã được đăng ký", Toast.LENGTH_SHORT).show();
                }
                else {
                    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("TAG",e.getMessage());
                        }


                        @Override
                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(verificationId, forceResendingToken);
                            User u = new User(username, formatPhone, password,false);
                            user.child("users")
                                    .child(formatPhone)
                                    .setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        SharedPreferences sp= getSharedPreferences("User", MODE_PRIVATE);
                                        SharedPreferences.Editor Ed=sp.edit();
                                        Ed.putString("phone",formatPhone );
                                        Ed.apply();

                                        Toast.makeText(RegisterActivity.this, "Mã OTP đã được gửi đến số điện thoại", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(RegisterActivity.this, OTPActivity.class);
                                        i.putExtra("verificationId", verificationId);
                                        i.putExtra("phone",formatPhone);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }

                    };

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(formatPhone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(RegisterActivity.this)
                            .setCallbacks(mCallbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
//            @Override
//            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
//                if (task.getResult().getSignInMethods().size() == 0){
//                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if(task.isSuccessful())
//                            {
//                                User user = new User(username,email,password);
//                                FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com").getReference("users")
//                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if(task.isSuccessful())
//                                        {
//                                            SendM
//                                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
//                                        }
//                                        else
//                                        {
//                                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
//                                        }
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                });
//                            }
//                            else
//                            {
//                                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
//                                progressBar.setVisibility(View.GONE);
//                            }
//                        }
//                    });
//                }else {
//                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại, email đã tồn tại", Toast.LENGTH_SHORT).show();
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
//            }
//        });

    }
}