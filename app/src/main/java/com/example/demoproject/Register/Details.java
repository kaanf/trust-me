package com.example.demoproject.Register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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

public class Details extends AppCompatActivity {

    EditText etName, etId, etMail, etDepartment;
    LinearLayout check_button;
    ImageView back;
    ProgressBar loading;
    TextView not_me;
    String student_id, student_mail, name, id, department, mail, state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        // Retrieve user data
        isUser();
        // Back button declaration
        back.setOnClickListener(v -> finish());
        not_me.setOnClickListener(v -> finish());
        // Onclick function of check button
        check_button.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            Intent intent = new Intent(Details.this, Phone.class);
            intent.putExtra("name", name);
            intent.putExtra("id", id);
            intent.putExtra("mail", mail);
            intent.putExtra("department", department);
            intent.putExtra("state", state);
            loading.setVisibility(View.GONE);
            startActivity(intent);
        });
    }

    // Item declaration
    private void assignItems() {
        etName = findViewById(R.id.name);
        etId = findViewById(R.id.id);
        etMail = findViewById(R.id.mail);
        etDepartment = findViewById(R.id.department);
        check_button = findViewById(R.id.check_button);
        loading = check_button.findViewById(R.id.loading);
        back = findViewById(R.id.back);
        not_me = findViewById(R.id.not_me);
        // Student ID & Mail intent, for Firebase query
        student_id = getIntent().getStringExtra("StudentID");
        student_mail = getIntent().getStringExtra("StudentMail");
        state = getIntent().getStringExtra("State");
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

    // Retrieve user data & set text from Firebase
    private void isUser() {
        Query emailQ = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("mail").equalTo(student_mail);
        emailQ.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child(student_id).child("name").getValue(String.class);
                id = snapshot.child(student_id).child("id").getValue(String.class);
                department = snapshot.child(student_id).child("department").getValue(String.class);
                mail = snapshot.child(student_id).child("mail").getValue(String.class);
                etName.setText(name);
                etId.setText(id);
                etDepartment.setText(department);
                etMail.setText(mail);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}