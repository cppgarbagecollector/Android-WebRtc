package com.myhexaville.androidwebrtc.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.myhexaville.androidwebrtc.R;
import com.myhexaville.androidwebrtc.databinding.ActivitySampleCameraRenderBinding;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import static com.myhexaville.androidwebrtc.web_rtc.PeerConnectionClient.VIDEO_TRACK_ID;

/*
* Example of how to render camera with WebRTC SDK without any abstraction classes
* */
public class SampleCameraRenderActivity extends AppCompatActivity {
    private ActivitySampleCameraRenderBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample_camera_render);

        // Create video renderers.
        EglBase rootEglBase = EglBase.create();
        binding.surfaceView.init(rootEglBase.getEglBaseContext(), null);

        binding.surfaceView.setEnableHardwareScaler(true);
        binding.surfaceView.setMirror(true);

        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        PeerConnectionFactory factory = new PeerConnectionFactory(null);

        VideoCapturer videoCapturer = createVideoCapturer();

        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
        createVideoTrack(videoCapturer, factory);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private void createVideoTrack(VideoCapturer capturer, PeerConnectionFactory factory) {
        VideoSource videoSource = factory.createVideoSource(capturer);
        capturer.startCapture(1280, 720, 30);

        VideoTrack localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView));
    }

}