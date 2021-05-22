package com.example.demoproject.Room;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.demoproject.Helpers.StoredUser;
import com.example.demoproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RoomDetails extends AppCompatActivity {

    ImageView back;
    EditText room_code, lesson_code, timer;
    LinearLayout room_details;
    TextView button_text;
    ProgressBar loading;
    String roomId, name, user_time, user_lesson, id, image;
    StoredUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Assign items
        assignItems();
        // Firebase retrieve profile image
        retrieveImage();
        // Back button declaration
        back.setOnClickListener(v -> finish());
        room_details.setOnClickListener(view -> {
            if(timer.getText().toString().isEmpty() || lesson_code.getText().toString().isEmpty()) {
                loading.setVisibility(View.GONE);
            } else {
                loading.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    createRoomFirebase();
                    Intent intent = new Intent(RoomDetails.this, Lobby.class);
                    intent.putExtra("user", user);
                    intent.putExtra("roomId", roomId);
                    startActivity(intent);
                }, 1000);
            }
        });
    }

    private void retrieveImage() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child(id).child("image");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void assignItems() {
        back = findViewById(R.id.back);
        room_code = findViewById(R.id.room_code);
        lesson_code = findViewById(R.id.lesson_code);
        timer = findViewById(R.id.timer);
        timer.setImeOptions(EditorInfo.IME_ACTION_DONE);
        room_details = findViewById(R.id.room_details);
        button_text = room_details.findViewById(R.id.button_text);
        loading = room_details.findViewById(R.id.loading);
        roomId = getIntent().getStringExtra("roomId");
        user = getIntent().getExtras().getParcelable("user");
        name = user.getName();
        id = user.getId();
        room_code.setHint(roomId);
    }


    private void createRoomFirebase() {
        user_time = timer.getText().toString();
        user_lesson = lesson_code.getText().toString();
        HashMap<String, String> room = new HashMap<>();
        room.put("roomId", "" + roomId);
        room.put("createdBy", "" + name);
        room.put("timer", user_time);
        room.put("lessonCode", user_lesson);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Rooms");
        ref.child(roomId).setValue(room)
                .addOnSuccessListener(aVoid -> {
                    HashMap<String, String> academician = new HashMap<>();
                    academician.put("name", name);
                    academician.put("id", "Academician");
                    academician.put("image", image);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Rooms");
                    reference.child(roomId).child("Participants").child(name).setValue(academician)
                            .addOnSuccessListener(aVoid1 -> loading.setVisibility(View.GONE));
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