package com.example.demoproject.Register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.demoproject.Dashboard.Home;
import com.example.demoproject.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Phone extends AppCompatActivity {

    EditText phone_text;
    LinearLayout otp;
    ImageView back;
    ProgressBar loading;
    TextView button_text;
    String s_phone, phone, id, name, mail, department, state;
    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        // Firebase Auth
        auth = FirebaseAuth.getInstance();
        // Back button declaration
        back.setOnClickListener(v -> finish());
        phone_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = phone_text.getText().toString();
                if(val.length() == 10) {
                    phone_text.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.check_icon, 0);
                } else {
                    phone_text.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // OTP functions
        otp.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            s_phone = phone_text.getText().toString();
            phone = "+90" + s_phone;
            if(!validatePhone()) {
                loading.setVisibility(View.GONE);
            } else {
                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phone)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(Phone.this)
                                .setCallbacks(mCallbacks)
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Intent OTPIntent = new Intent(Phone.this, Home.class);
                loading.setVisibility(View.GONE);
                startActivity(OTPIntent);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loading.setVisibility(View.GONE);
            }
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                loading.setVisibility(View.VISIBLE);
                Intent OTPIntent = new Intent(Phone.this, Otp.class);
                OTPIntent.putExtra("auth", s);
                OTPIntent.putExtra("name", name);
                OTPIntent.putExtra("id", id);
                OTPIntent.putExtra("mail", mail);
                OTPIntent.putExtra("department", department);
                OTPIntent.putExtra("state", state);
                OTPIntent.putExtra("phone", phone);
                loading.setVisibility(View.GONE);
                startActivity(OTPIntent);
            }
        };
    }

    // UI Flags
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // UI Flags
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // Item declaration
    @SuppressLint("SetTextI18n")
    private void assignItems() {
        phone_text = findViewById(R.id.phone_text);
        phone_text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        phone_text.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        otp = findViewById(R.id.otp);
        back = findViewById(R.id.back);
        button_text = otp.findViewById(R.id.button_text);
        button_text.setText("Send Code");
        loading = otp.findViewById(R.id.loading);
        name = getIntent().getStringExtra("name");
        id = getIntent().getStringExtra("id");
        state = getIntent().getStringExtra("state");
        mail = getIntent().getStringExtra("mail");
        department = getIntent().getStringExtra("department");
    }

    // Phone validation
    private boolean validatePhone() {
        String val = phone_text.getText().toString();
        if (val.isEmpty()) {
            return false;
        } else if (val.length() < 10) {
            return false;
        } else {
            phone_text.setError(null);
            return true;
        }
    }
}