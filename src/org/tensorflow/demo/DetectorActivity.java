/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;
import org.tensorflow.demo.R; // Explicit import needed for internal Google builds.

import static org.tensorflow.demo.HEFFAN_filter.text_adapter;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  Button button;
  ImageView imageView;
  TessBaseAPI tessBaseAPI;
  //public static List<Bitmap> recognizedContent = new LinkedList<Bitmap>();
  public static Bitmap[] recognizedContent;
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged multibox model.
  private static final int MB_INPUT_SIZE = 224;
  private static final int MB_IMAGE_MEAN = 128;
  private static final float MB_IMAGE_STD = 128;
  private static final String MB_INPUT_NAME = "ResizeBilinear";
  private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
  private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
  private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
  private static final String MB_LOCATION_FILE =
      "file:///android_asset/multibox_location_priors.txt";

  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final String TF_OD_API_MODEL_FILE =
      "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

  // Configuration values for tiny-yolo-voc. Note that the graph is not included with TensorFlow and
  // must be manually placed in the assets/ directory by the user.
  // Graphs and models downloaded from http://pjreddie.com/darknet/yolo/ may be converted e.g. via
  // DarkFlow (https://github.com/thtrieu/darkflow). Sample command:
  // ./flow --model cfg/tiny-yolo-voc.cfg --load bin/tiny-yolo-voc.weights --savepb --verbalise
  private static final String YOLO_MODEL_FILE = "file:///android_asset/image_to_label.pb";
  private static final String YOLO_MODEL_CONTENT_FILE = "file:///android_asset/label_to_content.pb";
  private static final int YOLO_INPUT_SIZE = 416;
  private static final String YOLO_INPUT_NAME = "input";
  private static final String YOLO_OUTPUT_NAMES = "output";
  private static final int YOLO_BLOCK_SIZE = 32;

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
  // or YOLO.
  private enum DetectorMode {
    TF_OD_API, MULTIBOX, YOLO;
  }
  private static final DetectorMode MODE = DetectorMode.YOLO;

  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
  private static final float MINIMUM_CONFIDENCE_YOLO = 0.2f;

  private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.YOLO;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;

  private Integer sensorOrientation;

  private Classifier detector;
  private Classifier contentDetector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private byte[] luminanceCopy;

  private BorderedText borderedText;

    Matrix rotateMatrix;
  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    LOGGER.i("  public void onPreviewSizeChosen(final Size size, final int rotation)");
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;
    if (MODE == DetectorMode.YOLO) {
      detector =
          TensorFlowYoloDetector.create(
                  getAssets(),
                  YOLO_MODEL_FILE,
                  YOLO_INPUT_SIZE,
                  YOLO_INPUT_NAME,
                  YOLO_OUTPUT_NAMES,
                  YOLO_BLOCK_SIZE);
      cropSize = YOLO_INPUT_SIZE;

      contentDetector =
              TensorFlowYoloDetectorContent.create(
                  getAssets(),
                  YOLO_MODEL_CONTENT_FILE,
                  YOLO_INPUT_SIZE,
                  YOLO_INPUT_NAME,
                  YOLO_OUTPUT_NAMES,
                  YOLO_BLOCK_SIZE);
      cropSize = YOLO_INPUT_SIZE;
    } else if (MODE == DetectorMode.MULTIBOX) {
      detector =
          TensorFlowMultiBoxDetector.create(
              getAssets(),
              MB_MODEL_FILE,
              MB_LOCATION_FILE,
              MB_IMAGE_MEAN,
              MB_IMAGE_STD,
              MB_INPUT_NAME,
              MB_OUTPUT_LOCATIONS_NAME,
              MB_OUTPUT_SCORES_NAME);
      cropSize = MB_INPUT_SIZE;
    } else {
      try {
        detector = TensorFlowObjectDetectionAPIModel.create(
            getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        cropSize = TF_OD_API_INPUT_SIZE;
      } catch (final IOException e) {
        LOGGER.e(e, "Exception initializing classifier!");
        Toast toast =
            Toast.makeText(
                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
        toast.show();
        finish();
      }
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            if (!isDebug()) {
              return;
            }
            final Bitmap copy = cropCopyBitmap;
            if (copy == null) {
              return;
            }

            final int backgroundColor = Color.argb(100, 0, 0, 0);
            canvas.drawColor(backgroundColor);

            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                canvas.getWidth() - copy.getWidth() * scaleFactor,
                canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (detector != null) {
              final String statString = detector.getStatString();
              final String[] statLines = statString.split("\n");
              for (final String line : statLines) {
                lines.add(line);
              }
            }
            lines.add("");

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
          }
        });
  }

  OverlayView trackingOverlay;

  @Override
  protected void processImage() {
    LOGGER.i(" protected void processImage() ");
    ++timestamp;
    final long currTimestamp = timestamp;
    byte[] originalLuminance = getLuminance();
    tracker.onFrame(
        previewWidth,
        previewHeight,
        getLuminanceStride(),
        sensorOrientation,
        originalLuminance,
        timestamp);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = contentDetector.recognizeImage(croppedBitmap);
    //        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
              case MULTIBOX:
                minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                break;
              case YOLO:
                minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
                break;
            }

            final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

            recognizedContent = new Bitmap[results.size()];
            int contentIndex = 0;

            // 탐색이 성공하면 for문 들어감
            for (final Classifier.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {
                canvas.drawRect(location, paint);

                cropToFrameTransform.mapRect(location);
                result.setLocation(location);
                mappedRecognitions.add(result);

/////////////////////////OCR starts here
                Log.i("location:", location.left+":"+location.top);
                int leftPoint = Math.round(location.left);
                //int leftPoint = 0; // 왼쪽 끝까지 포인트
                //int topPoint = Math.round(location.top);
                int topPoint = 0;
                int secondWidth = Math.round(location.right- location.left);
                //int secondWidth = 416;
                //int secondHeight = Math.round(location.bottom - location.top);
                int secondHeight = 416;
                Log.i("Title : ", result.getTitle());
                Log.i("secondWidth: ", secondWidth+"");
                Log.i("secondHeight: ", secondHeight+"");
                Log.i("wid_size: ", croppedBitmap.getWidth()+"");

                if (leftPoint >= 416 || topPoint >= 416) continue;
                if (leftPoint+secondWidth > 416) secondWidth = 416-leftPoint;
                if (topPoint+secondHeight > 416) secondHeight = 416-topPoint;

                if (result.getTitle().equals("Content")) {

                  Bitmap ocrCroppedBitmap = Bitmap.createBitmap(rgbFrameBitmap, leftPoint, topPoint, secondWidth, secondHeight, rotateMatrix , false);
                  recognizedContent[contentIndex++] = ocrCroppedBitmap;
                }

              }
            }



            tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
            trackingOverlay.postInvalidate();

            requestRender();
            computingDetection = false;
          }
        });
  }

  @Override
  protected int getLayoutId() {
    LOGGER.i(" protected int getLayoutId() ");
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    LOGGER.i(" protected Size getDesiredPreviewFrameSize() ");
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onSetDebug(final boolean debug) {
    LOGGER.i("  public void onSetDebug(final boolean debug) ");
    detector.enableStatLogging(debug);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) { // 액티비티 시작
    LOGGER.i("  protected void onCreate(Bundle savedInstanceState) ");
    super.onCreate(savedInstanceState); // extend한 Camera Activity로
    /*
    setContentView(R.layout.activity_main);

    imageView = findViewById(R.id.imageView);
    surfaceView = findViewById(R.id.surfaceView);
    textView = findViewById(R.id.textView);
    */

    button = findViewById(R.id.button);
    imageView = findViewById(R.id.imageView);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // 캡처를 여러번 실행하도록 하자
        for(int i=0;i<10;i++){
          // sleep 0.1초하고
          try{
            Thread.sleep((long) 0.1);
          }catch(InterruptedException e){}

          // 디텍트
          CameraActivity.stopDetection = true;
          capture();
        }
      }
    });

    rotateMatrix = new Matrix();
    rotateMatrix.postRotate(90);


    System.out.println("  tessBaseAPI = new TessBaseAPI();");
    tessBaseAPI = new TessBaseAPI();
    String dir = getFilesDir() + "/tesseract";
    if(checkLanguageFile(dir+"/tessdata"))
      tessBaseAPI.init(dir, "eng");


  }

  boolean checkLanguageFile(String dir)
  {
    LOGGER.i("  boolean checkLanguageFile(String dir) ");
    File file = new File(dir);
    if(!file.exists() && file.mkdirs())
      createFiles(dir);
    else if(file.exists()){
      String filePath = dir + "/eng.traineddata";
      File langDataFile = new File(filePath);
      if(!langDataFile.exists())
        createFiles(dir);
    }
    return true;
  }


  private void createFiles(String dir)
  {
    LOGGER.i("  private void createFiles(String dir) ");
    AssetManager assetMgr = this.getAssets();

    InputStream inputStream = null;
    OutputStream outputStream = null;

    try {
      inputStream = assetMgr.open("eng.traineddata");

      String destFile = dir + "/eng.traineddata";

      outputStream = new FileOutputStream(destFile);

      byte[] buffer = new byte[1024];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
      inputStream.close();
      outputStream.flush();
      outputStream.close();
    }catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void capture()
  {
    LOGGER.i("  private void capture() ");
    /*
      final Bitmap[] arr = new Bitmap[recognizedContent.size()];
      for(int i = 0; i < recognizedContent.size(); i++) {
        arr[i] = recognizedContent.get(i);
        Log.e("ocr list::", arr[i].toString());
      }

     */
    for(int i=0;i<5;i++){
      // sleep 하고
      try{
        Thread.sleep((long)1);
      }catch (InterruptedException e){}

      // detect 시작
      button.setEnabled(false);
      imageView.setImageBitmap(recognizedContent[0]);
      new AsyncTess().execute(recognizedContent);
    }

  }



  private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {

    // 여기가 테저렉트 쓰는 부분임
    @Override
    protected String doInBackground(Bitmap... bitmapList) {
      LOGGER.i("  protected String doInBackground(Bitmap... bitmapList) ");
      long startTime = System.currentTimeMillis();
      String result = "";

      for (final Bitmap content : bitmapList) {

        if (content != null) {
          tessBaseAPI.setImage(content);
          result += tessBaseAPI.getUTF8Text() + '\n';
        }
      }

      long endTime = System.currentTimeMillis();

      //supplement_filter();
      text_adapter(result);
      return "result::::"+result + "\n" + (endTime - startTime);
    }

    protected void onPostExecute(String result) {
      LOGGER.i("  protected void onPostExecute(String result) ");
      //완료 후 버튼 속성 변경 및 결과 출력

      button.setEnabled(true);

      Log.i("ocr::", result);
    }




  }
}
