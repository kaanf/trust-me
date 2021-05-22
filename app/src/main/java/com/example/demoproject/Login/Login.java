package com.example.demoproject.Login;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoproject.Dashboard.Home;
import com.example.demoproject.R;
import com.example.demoproject.Register.Mail;

public class Login extends AppCompatActivity {

    LinearLayout login_button;
    ProgressBar loading;
    TextView button_text, create_account;
    EditText phone_text;
    String COUNTRY_CODE = "+90";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        // Phone validation
        phone_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = phone_text.getText().toString();
                if (val.length() == 10) {
                    phone_text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.phone_icon, 0, R.drawable.check_icon, 0);
                }
                else {
                    phone_text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.phone_icon, 0, 0, 0);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // Onclick function of login button
        login_button.setOnClickListener(v -> {
            String userPhone = phone_text.getText().toString();
            String totalPhone = COUNTRY_CODE + userPhone;
            if(!validatePhone()) {
                // Show error dialog to user
                phone_text.requestFocus();
            }
            else {
                Intent intent = new Intent(Login.this, Home.class);
                intent.putExtra("UserPhone", totalPhone);
                startActivity(intent);
            }
        });
        // Onclick function of create account
        create_account.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Mail.class);
            startActivity(intent);
        });
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

    // Item declaration
    @SuppressLint("SetTextI18n")
    private void assignItems() {
        login_button = findViewById(R.id.login_button);
        loading = login_button.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        button_text = login_button.findViewById(R.id.button_text);
        button_text.setText("Login");
        phone_text = findViewById(R.id.phone_text);
        phone_text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.phone_icon, 0, 0, 0);
        phone_text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        create_account = findViewById(R.id.create_account);
    }

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