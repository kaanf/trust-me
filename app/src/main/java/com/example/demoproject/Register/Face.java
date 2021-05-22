package com.example.demoproject.Register;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoproject.Dashboard.Home;
import com.example.demoproject.Helpers.SimilarityClassifier;
import com.example.demoproject.Helpers.StoredUser;
import com.example.demoproject.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

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

public class Face extends AppCompatActivity implements SimilarityClassifier {

    ImageView back;
    LinearLayout scan_face;
    TextView button_text;
    ProgressBar loading;
    FaceDetector detector;
    Interpreter tfLite;
    Bitmap pp;
    ImageView vector_image, user_image;
    boolean start = true, flipX = false;
    Context context = Face.this;
    StorageReference mStorageReference;
    int[] intValues;
    int inputSize = 112;
    boolean isModelQuantized = false;
    float[][] embeddings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    CardView card_image;
    int OUTPUT_SIZE = 192;
    CircularProgressBar progress_bar;
    private static final int SELECT_PICTURE = 1;
    String id, name, state, department, phone, mail;
    StoredUser user;
    String modelFile = "mobile_face_net.tflite";
    private final HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        // Hide navigation bar & top bar.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // Item declaration
        assignItems();
        back.setOnClickListener(v -> finish());
        mStorageReference = FirebaseStorage.getInstance().getReference().child(id + "/profile.jpeg");
        try {
            final File localFile = File.createTempFile("profile", "jpeg");
            mStorageReference.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        pp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        setPPImage(pp);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            tfLite = new Interpreter(loadModelFile(Face.this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
        scan_face.setOnClickListener(v -> askCameraPermissions());
    }

    // Item declaration
    @SuppressLint("SetTextI18n")
    private void assignItems() {
        back = findViewById(R.id.back);
        scan_face = findViewById(R.id.scan_face);
        button_text = scan_face.findViewById(R.id.button_text);
        button_text.setText("Scan Now");
        loading = scan_face.findViewById(R.id.loading);
        progress_bar = findViewById(R.id.progress_bar);
        vector_image = findViewById(R.id.vector_image);
        card_image = findViewById(R.id.card_image);
        user_image = findViewById(R.id.user_image);
        user = getIntent().getExtras().getParcelable("user");
        id = user.getId();
        name = user.getName();
        state = user.getState();
        mail = user.getMail();
        phone = user.getPhone();
        department = user.getDepartment();
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

    // Camera permissions
    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, SELECT_PICTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            Bitmap camera_photo = (Bitmap) bundle.get("data");
            loading.setVisibility(View.VISIBLE);
            vector_image.setVisibility(View.GONE);
            card_image.setVisibility(View.VISIBLE);
            user_image.setImageBitmap(camera_photo);
            progress_bar.setVisibility(View.VISIBLE);
            long animationDuration = 5000;
            progress_bar.setProgressWithAnimation(100f, animationDuration);
            setCameraImage(camera_photo);
        }
    }

    // Virtual-memory mapping with JVM memory
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
                    addFace();
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void intentClass() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Face.this, Home.class);
            intent.putExtra("user", user);
            startActivity(intent);
            loading.setVisibility(View.GONE);
            finish();
        }, 1000);
    }
}