package com.biotag.opencv4androiddemo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout clayout;
    private CascadeClassifier cascadeClassifier;
    private JavaCameraView javaCameraView;
    private Mat gray;
    private Mat rgba;
    private int absoluteFaceSize = 0;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private float mRelativeFaceSize   = 0.2f;
    private String id;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(MainActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    private Button btn_finish;
    private int framecount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = getIntent().getStringExtra("id");
        initView();
    }

    private void initView() {
        clayout = (ConstraintLayout) findViewById(R.id.clayout);
        RelativeLayout ll_wrapcamera = clayout.findViewById(R.id.ll_wrapcamera);
        javaCameraView = new JavaCameraView(this, -1);
        javaCameraView.setCameraIndex(99);  //back 摄像头   98是前置摄像头
        javaCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
//                gray = new Mat(height, width, CvType.CV_8UC4);
                gray = new Mat();
                rgba = new Mat();
            }

            @Override
            public void onCameraViewStopped() {
                gray.release();
                rgba.release();
            }

            int faceSerialCount = 0;

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                framecount++;
                Log.i("tmsk", "framecount: " + framecount);
                rgba = inputFrame.rgba();
                gray = inputFrame.gray();
                if(absoluteFaceSize == 0){
                    int height = gray.rows();
                    int facesizetemp = Math.round(height * mRelativeFaceSize);
                    if(facesizetemp >0){
                        absoluteFaceSize = facesizetemp;
                    }

                }
                MatOfRect faces = new MatOfRect();
                if (cascadeClassifier != null) {
                    cascadeClassifier.detectMultiScale(gray, faces, 1.2, 4, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                }
                Rect[] facesArray = faces.toArray();
                //检测到的人脸的个数
                int facecount = facesArray.length;

                for (int i = 0; i < facecount; i++) {
                    Rect temprect = facesArray[i];
                    Imgproc.rectangle(rgba, temprect.tl(), temprect.br(),
                            FACE_RECT_COLOR, 3);
                }
                return rgba;
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll_wrapcamera.addView(javaCameraView, lp);
        //        javaCameraView.enableView();
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(this);
    }


    private void initializeOpenCVDependencies() {
        File mCascadeFile = null;
        // Copy the resource into a temp file so OpenCV can load it,加载训练好的模型文件
        try {
            InputStream is = null;
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            if (id.equalsIgnoreCase("human")) {
                mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                Toast.makeText(this, "人脸模型加载成功~", Toast.LENGTH_SHORT).show();
            } else {
                mCascadeFile = new File(cascadeDir, "haar_cascade_dog.xml");
                is = getResources().openRawResource(R.raw.haar_cascade_dog);
                Toast.makeText(this, "狗脸模型加载成功~", Toast.LENGTH_SHORT).show();
            }
            FileOutputStream fos = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            is.close();
            fos.close();
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tms", "error loading cascade");
        }
        //// And we are ready to go
        //        openCvCameraView.enableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            //            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this,
            //            baseLoaderCallback);
            Log.d("tms", "Internal OpenCV library not found. Using OpenCV Manager for initialization");

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            //            initializeOpenCVDependencies();
            Log.d("tms", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        javaCameraView.disableView();
        javaCameraView = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finish:
                finish();
                break;
        }
    }
}