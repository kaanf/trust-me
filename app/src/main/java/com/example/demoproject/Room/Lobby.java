package com.example.demoproject.Room;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.demoproject.Dashboard.Home;
import com.example.demoproject.Helpers.Adapter.UserAdapter;
import com.example.demoproject.Helpers.Adapter.UserInformation;
import com.example.demoproject.Helpers.StoredUser;
import com.example.demoproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends AppCompatActivity {

    RecyclerView user_list;
    UserAdapter userAdapter;
    StoredUser user;
    LinearLayout exit_room;
    CardView main_cv;
    String name, id, roomId, user_time, user_lesson;
    List<UserInformation> userInformationList;
    TextView lesson_code, room_code, timer, button_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        // Firebase retrieve data
        retrieveData();
        // Back button declaration
        readUser();
        exit_room.setOnClickListener(view -> {
            Intent intent = new Intent(Lobby.this, Home.class);
            intent.putExtra("user", user);
            exitRoom();
            startActivity(intent);
        });
    }

    // Item declaration
    @SuppressLint("SetTextI18n")
    private void assignItems() {
        user_list = findViewById(R.id.user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        user_list.setLayoutManager(linearLayoutManager);
        user = getIntent().getExtras().getParcelable("user");
        roomId = getIntent().getStringExtra("roomId");
        name = user.getName();
        id = user.getId();
        timer = findViewById(R.id.timer);
        lesson_code = findViewById(R.id.lesson_code);
        room_code = findViewById(R.id.room_code);
        exit_room = findViewById(R.id.exit_room);
        button_text = exit_room.findViewById(R.id.button_text);
        main_cv = exit_room.findViewById(R.id.main_button_card);
        button_text.setText("Exit Room");
        room_code.setText(roomId);
        main_cv.setCardBackgroundColor(0XFFD13D55);
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

    private void retrieveData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Rooms").child(roomId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user_time = snapshot.child("timer").getValue(String.class);
                user_lesson = snapshot.child("lessonCode").getValue(String.class);
                timer.setText(user_time);
                lesson_code.setText(user_lesson);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUser() {
        userInformationList = new ArrayList<>();
        Query query = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId).child("Participants");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInformationList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    UserInformation userInformation = ds.getValue(UserInformation.class);
                    userInformationList.add(userInformation);
                    userAdapter = new UserAdapter(Lobby.this, userInformationList);
                    user_list.setAdapter(userAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void exitRoom() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);

        reference.child("Participants").child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()) {
                    data.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}