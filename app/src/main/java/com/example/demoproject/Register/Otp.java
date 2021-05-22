package com.example.demoproject.Register;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chaos.view.PinView;
import com.example.demoproject.Helpers.StoredUser;
import com.example.demoproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;

public class Otp extends AppCompatActivity {

    ImageView back;
    TextView phone_text, button_text;
    LinearLayout check_otp;
    ProgressBar loading;
    StoredUser user;
    PinView pin;
    String phone, otp, name, mail, department, state, id;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        // Back button declaration
        back.setOnClickListener(v -> finish());
        // Onclick function of check button
        check_otp.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            //String code = pin.getText().toString().trim();
            String code = Objects.requireNonNull(pin.getText()).toString().trim();
            if(code.isEmpty() || code.length() < 6) {
                loading.setVisibility(View.GONE);
                pin.requestFocus();
            } else {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otp, code);
                signIn(credential);
            }
        });
    }

    // PhoneAuthCredential function (if code is true, application redirects to Face activity.)
    private void signIn(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(authResult -> {
            //String p = firebaseAuth.getCurrentUser().getPhoneNumber();
            sendToMain();
        }).addOnFailureListener(e -> loading.setVisibility(View.GONE));
    }

    // Store user data & intent function
    private void sendToMain() {
        Intent intent = new Intent(Otp.this, Face.class);
        user = new StoredUser(name, id, mail, department, state, phone);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    // Item declaration
    @SuppressLint("SetTextI18n")
    private void assignItems() {
        back = findViewById(R.id.back);
        phone_text = findViewById(R.id.phone_text);
        check_otp = findViewById(R.id.check_otp);
        button_text = check_otp.findViewById(R.id.button_text);
        loading = check_otp.findViewById(R.id.loading);
        pin = findViewById(R.id.pin);
        pin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        otp = getIntent().getStringExtra("auth");
        name = getIntent().getStringExtra("name");
        id = getIntent().getStringExtra("id");
        state = getIntent().getStringExtra("state");
        mail = getIntent().getStringExtra("mail");
        department = getIntent().getStringExtra("department");
        phone = getIntent().getStringExtra("phone");
        phone_text.setText(phone);
        button_text.setText("Check Code");
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
}