package com.example.demoproject.Dashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.demoproject.Helpers.SimilarityClassifier;
import com.example.demoproject.Helpers.StoredUser;
import com.example.demoproject.Login.Login;
import com.example.demoproject.R;
import com.example.demoproject.Room.Lobby;
import com.example.demoproject.Room.RoomDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Home extends AppCompatActivity {

    ImageView u_image;
    RelativeLayout join_pop_up, create_pop_up, exit;
    StoredUser user;
    View cr_view, jr_view;
    Random rand = new Random();
    TextView u_name, u_id, ex_button, cr_button, u_state;
    Bitmap pp;
    LinearLayout join, create;
    PinView code;
    CardView card;
    String name, id, total_id, roomId, state, department, phone, mail, image;
    Dialog ex_dialog, cr_dialog;
    FirebaseAuth firebaseAuth;
    ProgressBar loading;
    int number;
    int statement = 0;

    FaceDetector detector;
    Interpreter tfLite;
    boolean start = true, flipX = false;
    int[] intValues;
    int inputSize = 112;
    boolean isModelQuantized = false;
    Context context = Home.this;
    float[][] embeddings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192;
    private static final int SELECT_PICTURE = 1;
    String modelFile = "mobile_face_net.tflite";
    private final HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        // Firebase get profile image
        retrieveImage();
        // firebaseAuth = FirebaseAuth.getInstance();

        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child(id + "/profile.jpeg");
        try {
            final File localFile = File.createTempFile("profile", "jpeg");
            mStorageReference.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        pp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        u_image.setImageBitmap(pp);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Uri of student image
        mStorageReference.getDownloadUrl().addOnSuccessListener(uri -> studentImage = String.valueOf(uri)); */

        // Popup belirir
        join_pop_up.setOnClickListener(v -> examPopUp());
        create_pop_up.setOnClickListener(v -> createPopUp());

        create.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            intentClass();
        });

        exit.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Login.class);
            startActivity(intent);
            finish();
        });

        try {
            tfLite = new Interpreter(loadModelFile(Home.this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
    }

    // Item declaration
    private void assignItems() {
        // Dialog
        ex_dialog = new Dialog(this);
        cr_dialog = new Dialog(this);
        // Create Dialog declaration
        cr_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cr_dialog.setCanceledOnTouchOutside(true);
        cr_dialog.setContentView(R.layout.pop_up_create_room);
        // Join Dialog declaration
        ex_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ex_dialog.setCanceledOnTouchOutside(true);
        ex_dialog.setContentView(R.layout.pop_up_join_room);
        // Dialog button
        create = cr_dialog.findViewById(R.id.create);
        join = ex_dialog.findViewById(R.id.join);
        // View lines & pop-up buttons
        cr_view = findViewById(R.id.cr_view);
        jr_view = findViewById(R.id.jr_view);
        join_pop_up = findViewById(R.id.join_pop_up);
        create_pop_up = findViewById(R.id.create_pop_up);
        exit = findViewById(R.id.exit);
        // Home screen data
        u_state = findViewById(R.id.state);
        u_image = findViewById(R.id.user_image);
        u_name = findViewById(R.id.name);
        u_id = findViewById(R.id.id);
        // Parcelable intent of user
        user = getIntent().getExtras().getParcelable("user");
        name = user.getName();
        id = user.getId();
        total_id = "#" + id;
        mail = user.getMail();
        department = user.getDepartment();
        phone = user.getPhone();
        state = user.getState();
        // Set text
        u_state.setText(state);
        u_name.setText(name);
        u_id.setText(total_id);
        // Visibility of rooms
        checkStudent();
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

    private void checkStudent() {
        if (state.contains("Student")) {
            create_pop_up.setVisibility(View.GONE);
            cr_view.setVisibility(View.GONE);
            statement = 1;
        } else {
            join_pop_up.setVisibility(View.GONE);
            jr_view.setVisibility(View.GONE);
            statement = 2;
        }
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

    @SuppressLint("SetTextI18n")
    private void examPopUp() {
        code = ex_dialog.findViewById(R.id.code);
        code.setImeOptions(EditorInfo.IME_ACTION_DONE);
        ex_button = join.findViewById(R.id.button_text);
        card = join.findViewById(R.id.main_button_card);
        ex_button.setText("Join");
        ex_dialog.show();
        join.setOnClickListener(v -> joinRoom());
    }

    @SuppressLint("SetTextI18n")
    private void createPopUp() {
        code = cr_dialog.findViewById(R.id.code);
        number = rand.nextInt(10000) + 1;
        roomId = String.valueOf(number);
        code.setText(String.valueOf(number));
        code.setImeOptions(EditorInfo.IME_ACTION_DONE);
        cr_button = create.findViewById(R.id.button_text);
        cr_button.setText("Create");
        loading = create.findViewById(R.id.loading);
        card = create.findViewById(R.id.main_button_card);
        cr_dialog.show();
    }

    private void joinRoom() {
        roomId = Objects.requireNonNull(code.getText()).toString();
        Query room = FirebaseDatabase.getInstance().getReference().child("Rooms").orderByChild("roomId").equalTo(roomId);
        room.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, SELECT_PICTURE);
                    }
                } else {
                    ex_button.setText("Not Exist");
                    card.setCardBackgroundColor(0XFFD13D55);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            Bitmap camera_photo = (Bitmap) bundle.get("data");
            setPPImage(pp);
            setCameraImage(camera_photo);
        }
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Image recognition algorithm
    public void recognizeImage(final Bitmap bitmap) {
        //Create ByteBuffer to store normalized image
        ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);
        imgData.order(ByteOrder.nativeOrder());
        intValues = new int[inputSize * inputSize];
        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }

        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        // Output of model will be stored in this variable
        embeddings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeddings);
        // Run model
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        float distance;
        //Compare new face with saved Faces.
        if (registered.size() > 0) {
            final Pair<String, Float> nearest = findNearest(embeddings[0]);//Find closest matching face
            if (nearest != null) {
                final String name = nearest.first;
                distance = nearest.second;
                if (distance < 1.000f) {
                    intentClass();
                } else {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Hata, aynı yüz değil", Toast.LENGTH_SHORT).show();
                }
                System.out.println("nearest: " + name + " - distance: " + distance);
            }
        }
    }

    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {
            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];
            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }
        return ret;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawRect(//from  w w  w. ja v  a  2s. c  om
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);
        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);
        canvas.drawBitmap(source, matrix, paint);
        if (source != null && !source.isRecycled()) {
            source.recycle();
        }
        return resultBitmap;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    // Add face to tflite
    private void addFace() {
        {
            start = false;
            SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                    "0", "", -1f);
            result.setExtra(embeddings);
            registered.put(name, result);
            start = true;
        }
    }

    public void setPPImage(Bitmap pp) {
        InputImage pp_img = InputImage.fromBitmap(pp, 0);
        detector.process(pp_img).addOnSuccessListener(faces -> {
            if (faces.size() != 0) {
                com.google.mlkit.vision.face.Face face = faces.get(0);
                Bitmap frame_bmp;
                frame_bmp = pp;
                Bitmap frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false);
                RectF boundingBox = new RectF(face.getBoundingBox());
                Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
                recognizeImage(scaled);
                addFace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Set camera image to tflite
    public void setCameraImage(Bitmap camera_image) {
        InputImage camera_img = InputImage.fromBitmap(camera_image, 0);
        detector.process(camera_img).addOnSuccessListener(faces -> {
            if (faces.size() != 0) {
                com.google.mlkit.vision.face.Face face = faces.get(0);
                Bitmap frame_bmp;
                frame_bmp = camera_image;
                Bitmap frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false);
                RectF boundingBox = new RectF(face.getBoundingBox());
                Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
                recognizeImage(scaled);
                addFace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(e -> {
            start = true;
            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
        });
    }

    private void intentClass() {
        new Handler().postDelayed(() -> {
            if (state.contains("Student")) {
                Intent intent = new Intent(Home.this, Lobby.class);
                joinRoomFirebase();
                ex_button.setText("Join");
                card.setCardBackgroundColor(0XFF009FD4);
                intent.putExtra("user", user);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Home.this, RoomDetails.class);
                intent.putExtra("user", user);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
            }
        }, 1000);
    }

    private void joinRoomFirebase() {
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("id", id);
        userMap.put("image", image);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Rooms");
        reference.child(roomId).child("Participants").child(name).setValue(userMap);
    }

}