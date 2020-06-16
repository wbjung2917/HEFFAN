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

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static org.tensorflow.demo.HEFFAN_filter.checkFinishedFiltering;
import static org.tensorflow.demo.HEFFAN_filter.collectTexts;
import static org.tensorflow.demo.HEFFAN_filter.getFilterResults;

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
  private static final String YOLO_MODEL_FILE = "file:///android_asset/image_to_label.pb"; // 안쓰는거
  private static final String YOLO_MODEL_CONTENT_FILE = "file:///android_asset/label_to_content.pb"; // 실제 사용
  //private static final String YOLO_MODEL_CONTENT_FILE = "file:///android_asset/tiny-yolo-4c.pb"; // 실제 사용
  private static final int YOLO_INPUT_SIZE = 416;
  private static final String YOLO_INPUT_NAME = "input";
  private static final String YOLO_OUTPUT_NAMES = "output";
  private static final int YOLO_BLOCK_SIZE = 32;
  private static final int OCR_CNT=1;

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

  // cloud vision variable
  private static final String CLOUD_VISION_API_KEY = "AIzaSyCe_JD2TYO_1UL_mNUmnXL2FGZAfen6FI0";
  public static final String FILE_NAME = "temp.jpg";
  private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
  private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
  private static final int MAX_LABEL_RESULTS = 10;

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
    //LOGGER.i(" protected void processImage() ");
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
    //LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

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
            //LOGGER.i("Running detection on image " + currTimestamp);
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
                int secondWidth = Math.round(location.right- location.left + 2);
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
    //LOGGER.i(" protected int getLayoutId() ");
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    //LOGGER.i(" protected Size getDesiredPreviewFrameSize() ");
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onSetDebug(final boolean debug) {
    //LOGGER.i("  public void onSetDebug(final boolean debug) ");
    detector.enableStatLogging(debug);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) { // 액티비티 시작
    //LOGGER.i("  protected void onCreate(Bundle savedInstanceState) ");
    super.onCreate(savedInstanceState); // extend한 Camera Activity로
    getApplicationContext();
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
        for(int i=0;i<OCR_CNT;i++){
          // sleep 0.5초하고
          try{
            Thread.sleep((long) 0.5);
          }catch(InterruptedException e){}

          // 디텍트
          CameraActivity.stopDetection = true;
          capture();
        }

        // 비동기로 필터링 끝나길 기다리기
        AsyncWait asyncWait = new AsyncWait();
        asyncWait.execute();
      }
    });


    Button tr_btn=findViewById(R.id.to_result);
    tr_btn.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        Intent res=new Intent(getApplicationContext(),org.tensorflow.demo.ResultActivity.class);     //Uri 를 이용하여 웹브라우저를 통해 웹페이지로 이동하는 기능
        /*
        ArrayList<ArrayList<String>> arr=new ArrayList<>();
        ArrayList<String> a1=new ArrayList<>();
        a1.add("Sodium");
        a1.add("70mg");
        a1.add("50%");
        ArrayList<String> a2=new ArrayList<>();
        a2.add("Protein");
        a2.add("10g");
        a2.add("90%");
        ArrayList<String> a3=new ArrayList<>();
        a3.add("Vitamin C");
        a3.add("40mg");
        a3.add("100%");
        ArrayList<String> a4=new ArrayList<>();
        a4.add("Dietary Fiber");
        a4.add("10g");
        a4.add("50%");
        arr.add(a1);
        arr.add(a2);
        arr.add(a3);
        arr.add(a4);
        res.putExtra("delivered_arraylist",arr);
        startActivity((res));//우빈테스트용
        */
        if(getFilterResults()!=null){
          ArrayList<ArrayList<String>> result=getFilterResults();
          res.putExtra("delivered_arraylist", result);         // 필터링 끝난 결과, ArrayList<ArrayList<String>> 타입
          startActivity((res));
        }
        else{
          Handler mHandler = new Handler(Looper.getMainLooper());
          mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              // 사용하고자 하는 코드
              Toast.makeText(getApplicationContext(), "필터링에 실패했습니다..^^;;", Toast.LENGTH_LONG).show();
            }
          }, 0);
        }
        /*
        Log.d("InDetector",result.get(0).get(0));
        Log.d("InDetector",result.get(0).get(1));
        Log.d("InDetector",result.get(0).get(2));
        Log.d("InDetectorLength",Integer.toString(result.size()));
        */
      }
    });

    rotateMatrix = new Matrix();
    rotateMatrix.postRotate(90);


    //System.out.println("  tessBaseAPI = new TessBaseAPI();");
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
    //LOGGER.i("  private void capture() ");
    /*
      final Bitmap[] arr = new Bitmap[recognizedContent.size()];
      for(int i = 0; i < recognizedContent.size(); i++) {
        arr[i] = recognizedContent.get(i);
        Log.e("ocr list::", arr[i].toString());
      }

     */
    // cloud vision 사용
    for(final Bitmap content:recognizedContent){
      LOGGER.i("called cloud vision");
      callCloudVision(content);
    }
    // detect 시작
    imageView.setImageBitmap(recognizedContent[0]);
    //AsyncTess asyncTess = new AsyncTess();
    //asyncTess.execute(recognizedContent);

  }



  private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {


    @Override
    protected void onPreExecute() {
      //super.onPreExecute();
      button.setEnabled(false);
    }

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
      //text_adapter(result);
      //return "result::::"+result + "\n" + (endTime - startTime);
      return result;
    }

    @Override
    protected void onPostExecute(String result) {
      LOGGER.i("  protected void onPostExecute(String result) ");
      //완료 후 버튼 속성 변경 및 결과 출력

      button.setEnabled(true);
      Log.i("ocr::", result);

      collectTexts(OCR_CNT,result);
    }
  }

  // 필터링 결과를 받기위한 비동기 클래스
  public class AsyncWait extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects){
      while(true){
        /*
        try {
          Thread.sleep((long) 2);
        } catch (InterruptedException e) {
          System.out.println("필터링 대기중 에러발생");
          e.printStackTrace();
        }

         */

        if(checkFinishedFiltering()==true){
          LOGGER.i("토스트를 띄웁니다.");
          Handler mHandler = new Handler(Looper.getMainLooper());
          mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              // 사용하고자 하는 코드
              Toast.makeText(getApplicationContext(), "필터링이 끝났습니다^^", Toast.LENGTH_LONG).show();
            }
          }, 0);
          return null;
        }
      }
    }
  }

  private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    VisionRequestInitializer requestInitializer =
            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
              /**
               * We override this so we can inject important identifying fields into the HTTP
               * headers. This enables use of a restricted cloud platform API key.
               */
              @Override
              protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                      throws IOException {
                super.initializeVisionRequest(visionRequest);

                String packageName = getPackageName();
                visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
              }
            };

    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
    builder.setVisionRequestInitializer(requestInitializer);

    Vision vision = builder.build();

    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
            new BatchAnnotateImagesRequest();
    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
      AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

      // Add the image
      Image base64EncodedImage = new Image();
      // Convert the bitmap to a JPEG
      // Just in case it's a format that Android understands but Cloud Vision
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
      byte[] imageBytes = byteArrayOutputStream.toByteArray();

      // Base64 encode the JPEG
      base64EncodedImage.encodeContent(imageBytes);
      annotateImageRequest.setImage(base64EncodedImage);

      // add the features we want
      annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
        Feature labelDetection = new Feature();
        labelDetection.setType("DOCUMENT_TEXT_DETECTION");
                /*
                  TYPE_UNSPECIFIED	Unspecified feature type.
                  FACE_DETECTION	Run face detection.
                  LANDMARK_DETECTION	Run landmark detection.
                  LOGO_DETECTION	Run logo detection.
                  LABEL_DETECTION	Run label detection.
                  TEXT_DETECTION	Run text detection / optical character recognition (OCR). Text detection is optimized for areas of text within a larger image; if the image is a document, use DOCUMENT_TEXT_DETECTION instead.
                  DOCUMENT_TEXT_DETECTION	Run dense text document OCR. Takes precedence when both DOCUMENT_TEXT_DETECTION and TEXT_DETECTION are present.
                  SAFE_SEARCH_DETECTION	Run Safe Search to detect potentially unsafe or undesirable content.
                  IMAGE_PROPERTIES	Compute a set of image properties, such as the image's dominant colors.
                  CROP_HINTS	Run crop hints.
                  WEB_DETECTION	Run web detection.
                  PRODUCT_SEARCH	Run Product Search.
                  OBJECT_LOCALIZATION	Run localizer for object detection.
                */
        labelDetection.setMaxResults(MAX_LABEL_RESULTS);
        add(labelDetection);
      }});

      // Add the list of one thing to the request
      add(annotateImageRequest);
    }});

    Vision.Images.Annotate annotateRequest =
            vision.images().annotate(batchAnnotateImagesRequest);
    // Due to a bug: requests to Vision API containing large images fail when GZipped.
    annotateRequest.setDisableGZipContent(true);
    LOGGER.i("created Cloud Vision request object, sending request");

    return annotateRequest;
  }

  private static String convertResponseToString(BatchAnnotateImagesResponse response) {
    StringBuilder message = new StringBuilder();

    List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
    if (labels != null) {
      for (EntityAnnotation label : labels) {
        message.append(String.format(Locale.US, "%s", label.getDescription()));
        break;
      }
    } else {
      //message.append("nothing");
    }

    return message.toString();
  }

  private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
    private final WeakReference<DetectorActivity> mActivityWeakReference;
    private Vision.Images.Annotate mRequest;

    LableDetectionTask(DetectorActivity activity, Vision.Images.Annotate annotate) {
      mActivityWeakReference = new WeakReference<>(activity);
      mRequest = annotate;
    }

    @Override
    protected String doInBackground(Object... params) {
      try {
        LOGGER.i("created Cloud Vision request object, sending request");
        BatchAnnotateImagesResponse response = mRequest.execute();
        return convertResponseToString(response);

      } catch (GoogleJsonResponseException e) {
        LOGGER.e("failed to make API request because " + e.getContent());
      } catch (IOException e) {
        LOGGER.e("failed to make API request because of other IOException " +
                e.getMessage());
      }
      return "Cloud Vision API request failed. Check logs for details.";
    }

    protected void onPostExecute(String result) {
      // 개행문자 제거
      LOGGER.i("\n\n########\n"+result+"\n########\n\n");
      collectTexts(OCR_CNT,result);
    }
  }

  public void callCloudVision(final Bitmap bitmap) {
    if(bitmap==null){
      return;
    }
    // Switch text to loading
    //mImageDetails.setText(R.string.loading_message);

    // Do the real work in an async task, because we need to use the network anyway
    try {
      AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
      labelDetectionTask.execute();
    } catch (IOException e) {
      System.out.println("failed to make API request because of other IOException " +
              e.getMessage());
    }
  }
}
