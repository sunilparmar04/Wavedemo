package in.wavedemo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.io.IOException;

import in.sp.waveform.SimpleWaveformView;
import in.sp.waveform.soundfile.SoundFile;


public class MainActivity extends AppCompatActivity implements SimpleWaveformView.WaveformListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SimpleWaveformView mWaveformView;
    private NumberProgressBar mLoadFileProgressBar;
    private int mStartPos;
    private int mEndPos;
    private int mOffset;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWaveformView = (SimpleWaveformView) findViewById(R.id.waveform);
        mWaveformView.setWaveformListener(this);

        mLoadFileProgressBar = (NumberProgressBar) findViewById(R.id.load_file_progressbar);
        mLoadFileProgressBar.setMax(100);


        final File file = getPath();


        Thread loadSongfileThread = new Thread() {
            @Override
            public void run() {

                try {
                    final SoundFile soundFile = SoundFile.create(file.getPath(), new SoundFile.ProgressListener() {

                        int lastProgress = 0;

                        @Override
                        public boolean reportProgress(double fractionComplete) {
                            final int progress = (int) (fractionComplete * 100);
                            if (lastProgress == progress) {
                                return true;
                            }
                            lastProgress = progress;
                            Log.i(TAG, "LOAD FILE PROGRESS:" + progress);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLoadFileProgressBar.setProgress(progress);
                                }
                            });
                            return true;
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mLoadFileProgressBar.setVisibility(View.GONE);

                            DisplayMetrics metrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(metrics);

                            mWaveformView.setAudioFile(soundFile);

                            mPlayer = new MediaPlayer();
                            try {
                                mPlayer.setDataSource(file.getPath());
                                mPlayer.prepare();
                                //  mPlayer.start();
                                mIsPlaying = true;
                                updateDisplay();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG, "Duration:" + mPlayer.getCurrentPosition());
                        }
                    });
//            mWaveformView.invalidate();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SoundFile.InvalidInputException e) {
                    e.printStackTrace();
                }
            }
        };
        loadSongfileThread.start();
    }


    private synchronized void updateDisplay() {
        if (mIsPlaying && mPlayer != null) {
            int now = mPlayer.getCurrentPosition();
            //mWaveformView.setPlaybackPosition(now);
        }
    }


    @Override
    public void onWaveformDraw() {
        if (mIsPlaying) {
            updateDisplay();
        }
    }

    public static File getPath() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Waveform/Chogada.mp3");
        if (!file.exists()) {
            file.mkdir();
        }
        String path = file.getAbsolutePath();
        Log.d("path", "Path " + path);

        return file;
    }
}
