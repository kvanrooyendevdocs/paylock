package com.KeenOx.paylock;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class LockScreenActivity extends AppCompatActivity {

    TextView tvBlockedMessage, tvChallengeQuestion, tvCreditInfo;
    EditText etChallengeAnswer;
    Button btnWait, btnChallenge, btnPay, btnSubmitAnswer;

    String blockedApp;
    String blockedAppLabel;
    int correctAnswer;
    boolean unlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        PrefsHelper.initializeCreditsIfNeeded(this);

        tvBlockedMessage = findViewById(R.id.tvBlockedMessage);
        tvChallengeQuestion = findViewById(R.id.tvChallengeQuestion);
        tvCreditInfo = findViewById(R.id.tvCreditInfo);
        etChallengeAnswer = findViewById(R.id.etChallengeAnswer);
        btnWait = findViewById(R.id.btnWait);
        btnChallenge = findViewById(R.id.btnChallenge);
        btnPay = findViewById(R.id.btnPay);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);

        blockedApp = getIntent().getStringExtra("blocked_app");
        blockedAppLabel = getAppLabel(blockedApp);

        tvBlockedMessage.setText(getString(R.string.opening_app_costs_you, blockedAppLabel));

        updateCreditInfo();

        btnWait.setOnClickListener(v -> startWaitTimer());
        btnChallenge.setOnClickListener(v -> showChallenge());
        btnPay.setOnClickListener(v -> unlockWithCredit());
        btnSubmitAnswer.setOnClickListener(v -> checkAnswer());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!unlocked) {
                    Toast.makeText(LockScreenActivity.this, "You must unlock first", Toast.LENGTH_SHORT).show();
                    sendUserToHomeScreen();
                } else {
                    finish();
                }
            }
        });
    }

    private String getAppLabel(String packageName) {
        if (packageName == null) {
            return "this app";
        }

        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    private void updateCreditInfo() {
        int credits = PrefsHelper.getCredits(this);
        tvCreditInfo.setText(getString(R.string.credits_available_format, credits));
    }

    private void startWaitTimer() {
        btnWait.setEnabled(false);
        btnChallenge.setEnabled(false);
        btnPay.setEnabled(false);
        btnSubmitAnswer.setEnabled(false);

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                btnWait.setText(getString(R.string.wait_seconds_format, secondsLeft));
            }

            @Override
            public void onFinish() {
                unlockBlockedApp();
            }
        }.start();
    }

    private void showChallenge() {
        Random random = new Random();
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        correctAnswer = a + b;

        tvChallengeQuestion.setText(getString(R.string.challenge_question_format, a, b));
        tvChallengeQuestion.setVisibility(View.VISIBLE);
        etChallengeAnswer.setText("");
        etChallengeAnswer.setVisibility(View.VISIBLE);
        btnSubmitAnswer.setVisibility(View.VISIBLE);
    }

    private void checkAnswer() {
        String userAnswerText = etChallengeAnswer.getText().toString().trim();

        if (userAnswerText.isEmpty()) {
            Toast.makeText(this, "Enter an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        int userAnswer = Integer.parseInt(userAnswerText);

        if (userAnswer == correctAnswer) {
            unlockBlockedApp();
        } else {
            Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
        }
    }

    private void unlockWithCredit() {
        boolean success = PrefsHelper.spendCredit(this);

        if (!success) {
            Toast.makeText(this, "Not enough credits", Toast.LENGTH_SHORT).show();
            return;
        }

        unlockBlockedApp();
    }

    private void unlockBlockedApp() {
        if (blockedApp == null) {
            finish();
            return;
        }

        unlocked = true;

        long allowedUntil = System.currentTimeMillis() + (5 * 60 * 1000);
        PrefsHelper.allowAppUntil(this, blockedApp, allowedUntil);

        Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();

        PackageManager packageManager = getPackageManager();
        Intent launchIntent = packageManager.getLaunchIntentForPackage(blockedApp);

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
        }

        finish();
    }

    private void sendUserToHomeScreen() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }
}