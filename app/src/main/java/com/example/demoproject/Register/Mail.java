package com.example.demoproject.Register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.demoproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Mail extends AppCompatActivity {

    ImageView back;
    LinearLayout mail_button;
    EditText mail_text;
    AppCompatRadioButton student_radio, academician_radio;
    TextView back_to_login, button_text;
    ProgressBar loading;
    String student_mail, student_id, state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        assignItems();
        // Back button declaration
        back_to_login.setOnClickListener(v -> finish());
        back.setOnClickListener(v -> finish());
        // Student mail text listener
        mail_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                student_mail = mail_text.getText().toString();
                if(student_mail.contains("student.atilim.edu.tr")) {
                    student_radio.setChecked(true);
                    academician_radio.setChecked(false);
                    state = "Student";
                } else if(student_mail.contains("atilim.edu.tr")) {
                    student_radio.setChecked(false);
                    academician_radio.setChecked(true);
                    state = "Academician";
                } else {
                    student_radio.setChecked(false);
                    academician_radio.setChecked(false);
                }
            }
        });
        // Mail button action
        mail_button.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            student_mail = mail_text.getText().toString();
            // Firebase e-mail check
            Query emailQ = FirebaseDatabase.getInstance().getReference("Users").orderByChild("mail").equalTo(student_mail);
            emailQ.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getChildrenCount()>0) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loading.setVisibility(View.GONE);
                            for(DataSnapshot child: snapshot.getChildren()) {
                                student_id = child.getKey();
                            }
                            Intent intent = new Intent(Mail.this, Details.class);
                            intent.putExtra("StudentID", student_id);
                            intent.putExtra("StudentMail", student_mail);
                            intent.putExtra("State", state);
                            startActivity(intent);
                        }, 1000);
                    } else {
                        loading.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });
    }

    // Item declaration
    @SuppressLint("SetTextI18n")
    private void assignItems() {
        back = findViewById(R.id.back);
        mail_button = findViewById(R.id.mail_button);
        mail_text = findViewById(R.id.mail_text);
        mail_text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        back_to_login = findViewById(R.id.back_to_login);
        loading = mail_button.findViewById(R.id.loading);
        button_text = mail_button.findViewById(R.id.button_text);
        button_text.setText("Check Mail");
        student_radio = findViewById(R.id.student_radio);
        academician_radio = findViewById(R.id.academician_radio);
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
}