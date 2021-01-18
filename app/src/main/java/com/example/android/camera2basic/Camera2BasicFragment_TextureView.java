/*
 * Copyright 2017 The Android Open Source Project
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

package com.example.android.camera2basic;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/* 拍照预览流程图
UI控件分辨率：protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
预览分辨率：texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
图片分辨率：mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),ImageFormat.JPEG, 2);

TextureView设置预览方向：mTextureView.setTransform(matrix); 其他预览控件，不是这个方法。
图片方向：captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

屏幕旋转：private void configureTransform(int viewWidth, int viewHeight)

1-> public void onViewCreated(final View view, Bundle savedInstanceState) {
  2-> mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);//获取mTextureView 
1-> public void onResume() {
  2-> startBackgroundThread();	//为相机开启了一个后台线程，这个进程用于后台执行保存图片等相关的工作
    3-> mBackgroundThread = new HandlerThread("bruceCameraBackground");
    3-> mBackgroundThread.start();
    3-> mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  2-> if (mTextureView.isAvailable()) {openCamera(mTextureView.getWidth(), mTextureView.getHeight());	//mTextureView已经创建，SurfaceTexture已经有效，则直接openCamera，用于屏幕熄灭等情况，这时onSurfaceTextureAvailable不会回调。
  2-> else {mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);		//SurfaceTexture处于无效状态中，则通过SurfaceTextureListener确保surface准备好。
	3-> public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
      4-> openCamera(width, height);		//SurfaceTexture有效即可openCamera
        5-> requestCameraPermission();//请求权限
        5-> setUpCameraOutputs(width, height);//包括对相机设备的选择，ImageReader的初始化和参数、回调设置。设置显示的转化矩阵，即将预览的图片调整至显示图层的大小。
          6-> for (String cameraId : manager.getCameraIdList()) {//获取摄像头可用列表
          6-> CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);////获取相机的特性
          6-> Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);// 不使用前置摄像头
          6-> mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),ImageFormat.JPEG, 2);//设置ImageReader接收的图片格式，以及允许接收的最大图片数目
          6-> mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);//设置图片存储的监听，但在创建会话，调用capture后才能有数据
            7-> public void onImageAvailable(ImageReader reader) {//图片有效回调
              8-> reader.acquireNextImage()//获取图片image
              8-> mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));///通知ImageSaver线程保存图片
                9-> private static class ImageSaver implements Runnable {//保存图片线程 
                  10-> public void run() {
                    11-> output = new FileOutputStream(mFile);output.write(bytes);//保存图片到文件
          6-> int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();//获取显示方向
          6-> mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);//获取sensor方向
          6-> mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),//获取最优的预览分辨率
          6-> mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置TextureView预览分辨率
          6-> mCameraId = cameraId;//获取当前ID
        5-> configureTransform(width, height);//配置transformation，主要是矩阵旋转相关
        5-> CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        5-> manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);//打开相机---------------------
          6-> private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {//打开相机设备状态回调---------------------
            7-> public void onError(@NonNull CameraDevice cameraDevice, int error) {//打开错误  
            7-> public void onDisconnected(@NonNull CameraDevice cameraDevice) {//断开相机
            7-> public void onOpened(@NonNull CameraDevice cameraDevice) {//打开成功
              8-> mCameraDevice = cameraDevice;//从onOpened参数获取mCameraDevice
              8-> createCameraPreviewSession();//创建会话
                9-> SurfaceTexture texture = mTextureView.getSurfaceTexture();//获取SurfaceTexture
                9-> texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置TextureView大小 
                9-> Surface surface = new Surface(texture);//创建Surface来预览
                9-> mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//创建TEMPLATE_PREVIEW预览CaptureRequest.Builder
                9-> mPreviewRequestBuilder.addTarget(surface);//CaptureRequest.Builder中添加Surface
                9-> mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),new CameraCaptureSession.StateCallback() {//创建会话---------------------
                  10-> public void onConfigureFailed(//创建会话失败
                  10-> public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {//创建会话成功
                    11-> mCaptureSession = cameraCaptureSession;//从onConfigured参数获取mCaptureSession
                    11-> mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//设置AF自动对焦模式
                    11-> mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);//设置AE模式
                    11-> mPreviewRequest = mPreviewRequestBuilder.build();//转换为CaptureRequest
                    11-> mCaptureSession.setRepeatingRequest(mPreviewRequest,mCaptureCallback, mBackgroundHandler);//设置预览，和拍照是同一个回调mCaptureCallback---------------------
                      12-> private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {//预览回调---------------------
                        13-> public void onCaptureProgressed(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request,@NonNull CaptureResult partialResult) {//预览过程中
                        13-> public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request,@NonNull TotalCaptureResult result) {//预览完成，和拍照是同一个回调mCaptureCallback
                          14-> process(result); //从onCaptureCompleted参数获取CaptureResult
                            15-> case STATE_PREVIEW: {//预览状态，则什么都不做
                            15-> case STATE_WAITING_LOCK: {//等待焦点被锁时，由设置拍照流时设置的STATE_WAITING_LOCK
                              16->captureStillPicture();//进行拍照
                                17-> final CaptureRequest.Builder captureBuilder =  mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);//设置TEMPLATE_STILL_CAPTURE拍照CaptureRequest.Builder
                                17-> captureBuilder.addTarget(mImageReader.getSurface());//添加拍照mImageReader为Surface
                                17-> captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//设置AF
                                17-> captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));//设置图片方向
                                17-> mCaptureSession.stopRepeating();//停止预览 
                                17-> mCaptureSession.abortCaptures();//中断Capture
                                17-> mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);//重新Capture进行拍照，这时mImageReader的回调会执行并保存图片---------------------
                                  18-> CameraCaptureSession.CaptureCallback CaptureCallback  = new CameraCaptureSession.CaptureCallback() { //拍照流程执行完成回调---------------------
                                    19-> public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                      20-> showToast("Saved: " + mFile);//提示拍照图片已经保存
                                      20-> unlockFocus();//释放焦点锁，重新开启预览。
                                        21-> mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);//重新设置AE/AF
                                        21-> mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,mBackgroundHandler);//重新设置AE/AF,通过mPreviewRequestBuilder.build()只发送一次请求，而不是mPreviewRequest。
                                        21-> mState = STATE_PREVIEW;//设置预览状态,通知mCaptureCallback回到预览状态
                                        21-> mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,mBackgroundHandler);//重新进入预览,使用mPreviewRequest
3-> public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) { 
      4-> configureTransform(width, height);
3-> public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
    3-> public void onSurfaceTextureUpdated(SurfaceTexture texture) { 
      4-> //镜像模式获取bitmap
1-> public void onClick(View view) {
  2-> takePicture();//拍照
    3-> lockFocus() {//拍照过程中锁住焦点
      4-> mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START); //通知camera锁住对焦
      4-> mState = STATE_WAITING_LOCK;//设置状态,通知mCaptureCallback等待锁定
      4-> mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,mBackgroundHandler);//通知camera锁住对焦和状态只发送一次请求，是同一个回调mCaptureCallback，只是等待焦点被锁，切换为STATE_WAITING_LOCK再真正进行拍照---------------------

*/
public class Camera2BasicFragment_TextureView extends Fragment
        implements View.OnClickListener,CountDownView.OnCountDownFinishedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Conversion from screen rotation to JPEG orientation.根据屏幕方向转换JPEG图片方向
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private CountDownView mCountDownView;
    private View mRootView;
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Fragment_TextureView";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;//预览状态

    /**
     * Camera state: Waiting for the focus to be locked.等待自动对焦的焦点被锁状态
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.等待曝光为预捕获状态
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.等待曝光不是预捕获状态
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.	拍照状态，APP开始获取图片数据流进行保存
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {//TextureView回调

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);//SurfaceTexture有效即可openCamera
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {//屏幕旋转时，摄像头/HAL捕捉到的原始图像也会跟着旋转，因此软件层面无需旋转。
            configureTransform(width, height);// 当屏幕旋转时，不会回调此方法。只有当size改变时, 才回调执行转换操作，屏幕旋转size并没有改变.
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {//可获取bitmap
        }

    };

    /**
     * ID of the current {@link CameraDevice}.正在使用的相机id
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.预览使用的自定义TextureView控件
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.预览用的获取会话
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.正在使用的相机
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.预览数据的尺寸
     */
    private Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.相机状态改变的回调函数
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {//打开相机设备状态回调

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {//打开成功
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();//释放访问许可
            mCameraDevice = cameraDevice;//从onOpened参数获取mCameraDevice
            createCameraPreviewSession();//创建会话
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {//断开相机
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {//打开错误
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.处理拍照等工作的子线程
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    private File mFile;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     ImageReader的回调函数, 其中的onImageAvailable会在照片准备好可以被保存时调用*/
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {//图片有效回调
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));//通知ImageSaver线程保存图片，//reader.acquireNextImage()获取图片image
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview预览请求构建器, 用来构建"预览请求"(下面定义的)通过pipeline发送到Camera device
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}预览请求, 由上面的构建器构建出来
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.用于拍照的相机状态的当前状态
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;//默认状态为预览状态

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.信号量控制器, 防止相机没有关闭时退出本应用
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.当前摄像头设备是否支持Flash
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.处理与JPEG捕获相关的事件
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {//预览回调

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {//预览状态，则什么都不做
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {//等待自动对焦的焦点被锁时，由设置拍照流时设置的STATE_WAITING_LOCK
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);//获取当前 AF 算法状态
                    if (afState == null) {//某些设备完成锁定后CONTROL_AF_STATE可能为null
                        captureStillPicture();//进行拍照
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||	/*AF 算法认为已对焦。镜头未移动。*/
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {	/*AF 算法认为无法对焦。镜头未移动。*/
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);//获取当前 AF 算法状态
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {//AE 已经为当前场景找到了理想曝光值，且曝光参数不会变化。
                            mState = STATE_PICTURE_TAKEN;//设置拍照状态，APP开始获取图片数据流进行保存
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();//如果没有找到理想曝光值，则运行捕获静止图像的预捕获序列操作。
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {//等待曝光为预捕获状态
                    // CONTROL_AE_STATE can be null on some devices某些设备CONTROL_AE_STATE可能为null
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);//获取当前 AE 算法状态
                    if (aeState == null ||  //某些设备CONTROL_AE_STATE可能为null
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||	/*HAL 正在处理预拍序列。*/
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {/*HAL 已聚焦曝光，但认为需要启动闪光灯才能保证照片亮度充足。*/
                        mState = STATE_WAITING_NON_PRECAPTURE;//设置等待曝光不是预捕获状态
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices 某些设备CONTROL_AE_STATE可能为null
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);//获取当前 AE 算法状态
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {/*HAL 正在处理预拍序列。*/
                        mState = STATE_PICTURE_TAKEN;//设置拍照状态，APP开始获取图片数据流进行保存
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {//预览过程中
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {//预览完成
            process(result);//从onCaptureCompleted参数获取CaptureResult
        }

    };

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("bruce线程");
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     * 
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class 相机支持的尺寸list
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen 能够选择的最大宽度
     * @param maxHeight         The maximum height that can be chosen 能够选择的醉倒高度
     * @param aspectRatio       The aspect ratio 图像的比例(pictureSize, 只有当pictureSize和textureSize保持一致, 才不会失真)
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough 返回最合适的预览尺寸
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
            int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface// 存放小于等于限定尺寸, 大于等于texture控件尺寸的Size
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface// 存放小于限定尺寸, 小于texture控件尺寸的Size
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                    option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        // 1. 若存在bigEnough数据, 则返回最大里面最小的
        // 2. 若不存bigEnough数据, 但是存在notBigEnough数据, 则返回在最小里面最大的
        // 3. 上述两种数据都没有时, 返回空, 并在日志上显示错误信息
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static Camera2BasicFragment_TextureView newInstance() {
        return new Camera2BasicFragment_TextureView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.picture).setOnClickListener(this);
        view.findViewById(R.id.info).setOnClickListener(this);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);//获取mTextureView
        initCountDownView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {//当Fragment所在的Activity被启动完成后回调该方法。
        super.onActivityCreated(savedInstanceState);
        mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
    }

    private void initializeCountDown() {
        mRootView = (ViewGroup)this.getActivity().getWindow().getDecorView();
        this.getActivity().getLayoutInflater().inflate(R.layout.count_down_to_capture,
                (ViewGroup) mRootView, true);
        mCountDownView = (CountDownView) (mRootView.findViewById(R.id.count_down_to_capture));
        mCountDownView.setCountDownFinishedListener((CountDownView.OnCountDownFinishedListener) this);
        mCountDownView.bringToFront();
//        mCountDownView.setOrientation(mOrientation);
    }

    public boolean isCountingDown() {
        return mCountDownView != null && mCountDownView.isCountingDown();
    }

    public void cancelCountDown() {
        if (mCountDownView == null) return;
        mCountDownView.cancelCountDown();
//        showUIAfterCountDown();
    }

    public void initCountDownView() {
        if (mCountDownView == null) {
            initializeCountDown();
        } else {
            mCountDownView.initSoundPool();
        }
    }

    public void releaseSoundPool() {
        if (mCountDownView != null) {
            mCountDownView.releaseSoundPool();
        }
    }

    public void startCountDown(int sec, boolean playSound) {
        mCountDownView.startCountDown(sec, playSound);
//        hideUIWhileCountDown();
    }
    @Override
    public void onCountDownFinished() {
//            checkSelfieFlashAndTakePicture();
//            mUI.showUIAfterCountDown();
    }
    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();//为相机开启了一个后台线程，这个进程用于后台执行相关的工作
        startCountDown(10, true);
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {//mTextureView已经创建，SurfaceTexture已经有效，则直接openCamera，用于屏幕熄灭等情况，这时onSurfaceTextureAvailable不会回调。
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {//SurfaceTexture处于无效状态中，则通过SurfaceTextureListener确保surface准备好。
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);//设置mTextureView回调
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview 预览有效宽度
     * @param height The height of available size for camera preview 预览有效高度
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {////包括对相机设备的选择，ImageReader的初始化和参数、回调设置。设置显示的转化矩阵，即将预览的图片调整至显示图层的大小。
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {//获取摄像头可用列表
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);////获取相机的特性

                // We don't use a front facing camera in this sample.// 如果该摄像头是前置摄像头, 则看下一个摄像头(本应用不使用前置摄像头)
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);// 不使用前置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.对于静态图像捕捉，我们使用最大的可用尺寸
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);//设置ImageReader接收的图片格式，以及允许接收的最大图片数目
                Log.e(TAG, "JPEG size: " + largest.getWidth() + "*" + largest.getHeight());
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);//设置图片存储的监听，但在创建会话，调用capture后才能有数据

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.看看我们是否需要交换尺寸，以获得相对于传感器坐标的预览大小。
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();//获取屏幕显示自然方向，屏幕旋转时依然是固定值。屏幕旋转需通过onOrientationChanged来监听。
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);//获取sensor方向
                boolean swappedDimensions = false;//交换尺寸
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);//Android获取屏幕分辨率displaySize
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;	//获取屏幕分辨率的宽
                int maxPreviewHeight = displaySize.y;	//获取屏幕分辨率的高

                if (swappedDimensions) {// 如果需要进行画面旋转, 将宽度和高度对调
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);//获取最优的预览分辨率

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {// 如果方向是横向(landscape)
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());;//设置TextureView预览分辨率。
                } else {// 方向不是横向(即竖向)
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.检查是否支持flash。
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;//获取当前ID
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link Camera2BasicFragment_TextureView#mCameraId}.
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();//请求权限
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);//openCamera时TextureView通过此方法设置默认预览方向
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("bruceCameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();//通过mTextureView获取SurfaceTexture。
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.我们将默认缓冲区的大小配置为我们想要的相机预览的大小
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置SurfaceTexture大小
            Log.e(TAG, "preview size: " + mPreviewSize.getWidth() + "*" + mPreviewSize.getHeight());
            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);//通过SurfaceTexture创建Surface来预览。

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//创建TEMPLATE_PREVIEW预览模板CaptureRequest.Builder
            mPreviewRequestBuilder.addTarget(surface);//CaptureRequest.Builder中添加Surface，即mTextureView获取创建的Surface

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {//创建会话

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {//创建会话成功
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;//从onConfigured参数获取mCaptureSession
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//设置快速连续对焦，用于快门零延迟静像拍摄。
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();//转换为CaptureRequest
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);//设置预览,setRepeatingRequest不断的重复mPreviewRequest请求捕捉画面，常用于预览或者连拍场景。
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(//创建会话失败
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     * 屏幕方向发生改变时调用转换数据方法，
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     TextureView通过此方法设置预览方向*/
    private void configureTransform(int viewWidth, int viewHeight) {//配置transformation，主要是矩阵旋转相关
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
            //Log.e(TAG, "preview orientation: " + rotation);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
            //Log.e(TAG, "preview orientation: " + rotation);
        } else if (Surface.ROTATION_0 == rotation) {
            Log.e(TAG, "preview orientation: " + rotation);
            //matrix.postRotate(180, centerX, centerY);//rotation默认为0，不处理，如果注释打开，则预览方向被旋转了180度。
        }
        mTextureView.setTransform(matrix);//设置mTextureView的transformation
    }

    /**
     * Initiate a still image capture.
     */
    private void takePicture() {//拍照
        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {//拍照过程中锁住焦点
        try {
            // This is how to tell the camera to lock focus.通知camera锁住对焦
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,//触发 AF 扫描
                    CameraMetadata.CONTROL_AF_TRIGGER_START);//触发 AF 扫描的启动操作。扫描效果取决于模式和状态。
            // Tell #mCaptureCallback to wait for the lock.通知mCaptureCallback等待锁定
            mState = STATE_WAITING_LOCK;//设置等待自动对焦的焦点被锁状态,通知mCaptureCallback等待锁定
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);//通知camera锁住对焦和状态通过mPreviewRequestBuilder.build()只发送一次请求，而不是mPreviewRequest。是同一个回调mCaptureCallback，发送一次请求只是等待自动对焦的焦点被锁，切换为STATE_WAITING_LOCK再真正进行拍照
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     运行捕获静止图像的预捕获序列。*/
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,//用于在拍摄高品质图像之前启动测光序列的控件。
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);//启动预拍序列。HAL 应使用后续请求进行衡量并达到理想的曝光/白平衡，以便接下来拍摄高分辨率的照片。
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;//设置等待曝光为预捕获状态
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {//进行拍照
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);//设置TEMPLATE_STILL_CAPTURE拍照模板CaptureRequest.Builder
            captureBuilder.addTarget(mImageReader.getSurface());//添加拍照mImageReader为Surface

            // Use the same AE and AF modes as the preview.//设置AF和AE
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//设置快速连续对焦，用于快门零延迟静像拍摄。
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();//获取屏幕显示自然方向，屏幕旋转时依然是固定值。屏幕旋转需通过onOrientationChanged来监听。
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));//设置图片方向，默认竖屏rotation=0。如果改成180，拍成照片则倒置旋转180度。

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {//拍照流程执行完成回调

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    showToast("Saved: " + mFile);//提示拍照图片已经保存
                    Log.d(TAG, mFile.toString());
                    unlockFocus();//释放焦点锁，重新开启预览。
                }
            };

            mCaptureSession.stopRepeating();//停止预览,停止任何一个正常进行的重复请求。
            mCaptureSession.abortCaptures();//中断Capture,尽可能快的取消当前队列中或正在处理中的所有捕捉请求。
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);//重新Capture进行拍照，这时mImageReader的回调会执行并保存图片
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.根据屏幕固定自然方向转换成JPEG图片方向。
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        int picRotation = (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
        Log.e(TAG, "JPEG orientation: " + picRotation + "; screen自然orientation: " + rotation);
        return picRotation;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);//取消当前 AF 扫描（如有），并将算法重置为默认值。
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);//通过mPreviewRequestBuilder.build()只发送一次请求，而不是mPreviewRequest。
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;//设置预览状态,通知mCaptureCallback回到预览状态
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);//重新进入预览,使用mPreviewRequest
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture: {
                takePicture();//拍照
                break;
            }
            case R.id.info: {
                Activity activity = getActivity();
                if (null != activity) {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.intro_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;
            }
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);//标准自动曝光，闪光灯听从 HAL 指令开启，以进行预拍摄和静像拍摄。
        }
    }

    private byte[] getJpegData(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }	

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {//保存图片线程 

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("bruce线程5");
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);//保存图片到文件
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

}
