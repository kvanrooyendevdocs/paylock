package com.KeenOx.paylock;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Random;

public class OverlayLockService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private CountDownTimer countDownTimer;

    private int correctAnswer;
    private boolean timerFinished = false;
    private String blockedApp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (overlayView != null) {
            return START_NOT_STICKY;
        }

        blockedApp = intent != null ? intent.getStringExtra("blocked_app") : null;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_lock, null);

        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;

        windowManager.addView(overlayView, params);

        setupLogic();

        return START_NOT_STICKY;
    }

    private void setupLogic() {
        TextView tvTimer = overlayView.findViewById(R.id.tvTimer);
        TextView tvQuestion = overlayView.findViewById(R.id.tvQuestion);
        EditText etAnswer = overlayView.findViewById(R.id.etAnswer);
        Button btnSubmit = overlayView.findViewById(R.id.btnSubmit);
        Button btnUseCredit = overlayView.findViewById(R.id.btnUseCredit);
        TextView tvCredits = overlayView.findViewById(R.id.tvCredits);

        updateCreditsText(tvCredits);

        timerFinished = false;

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Wait " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("You may answer now");
                timerFinished = true;
            }
        };
        countDownTimer.start();

        Random random = new Random();
        int a = random.nextInt(10);
        int b = random.nextInt(10);
        correctAnswer = a + b;

        tvQuestion.setText("What is " + a + " + " + b + "?");

        btnSubmit.setOnClickListener(v -> {
            if (!timerFinished) {
                Toast.makeText(this, "Wait first!", Toast.LENGTH_SHORT).show();
                return;
            }

            String input = etAnswer.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Enter an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            int answer;
            try {
                answer = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (answer == correctAnswer) {
                unlock();
            } else {
                Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
                etAnswer.setText("");
            }
        });

        btnUseCredit.setOnClickListener(v -> {
            int currentCredits = PrefsHelper.getCredits(this);

            if (currentCredits > 0) {
                PrefsHelper.setCredits(this, currentCredits - 1);
                updateCreditsText(tvCredits);
                unlock();
            } else {
                Toast.makeText(this, "No credits left", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCreditsText(TextView tvCredits) {
        int credits = PrefsHelper.getCredits(this);
        tvCredits.setText("Credits: " + credits);
    }

    private void unlock() {
        if (blockedApp != null && !blockedApp.isEmpty()) {
            PrefsHelper.allowAppTemporarily(this, blockedApp);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}