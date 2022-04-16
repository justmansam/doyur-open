package com.neftisoft.doyur;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SigninActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private Boolean exit = false;
    ProgressDialog dialog;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // To change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = SigninActivity.this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(SigninActivity.this,R.color.colorNavigationBar));
        }

        emailEditText = findViewById(R.id.user_email_text_view);
        emailEditText.setHintTextColor(Color.WHITE);
        passwordEditText = findViewById(R.id.pass_in_text_view);
        passwordEditText.setHintTextColor(Color.WHITE);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String email = intent.getStringExtra("User email"); //If user has a registered email. And if user comes from signup activity
        if (email != null) {
            displayUserEmail(email);
        }

        emailEditText.requestFocus(); //TO Get focus on the editText programmatically!
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
    }

    /*
    This method is called if user directed here from SignUp Activity with his/her registered email.
     */
    private void displayUserEmail(String email) {
        emailEditText.setText(email);
        emailEditText.requestFocus(); //TO Get focus on the editText programmatically!
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
    }

    /*
    This method is called when the Reset Password Button is clicked.
     */
    public void resetPassword(View view) {
        String email = emailEditText.getText().toString().trim();
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SigninActivity.this, "Lütfen e-posta adresinize gönderilen bağlantıyı tıklayarak şifrenizi yenileyin!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(SigninActivity.this, "Lütfen geçerli e-posta adresinizi girin!",
                    Toast.LENGTH_LONG).show();
        }
    }

    /*
    This method is called when the SignUp Button is clicked.
     */
    public void signIn(View view) {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        dialog = ProgressDialog.show(SigninActivity.this, "",
                "Giriş yapılıyor...", true);

        checkGivenInformationAndSignin(email, password);
    }
    private void checkGivenInformationAndSignin(String email, String password) {

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { //VALIDATION of EMAIL
            dialog.cancel();
            Toast.makeText(SigninActivity.this, "Lütfen geçerli e-posta adresinizi girin!",
                    Toast.LENGTH_LONG).show();
        }
        else if (password.length() < 6) {
            dialog.cancel();
            Toast.makeText(SigninActivity.this, "Lütfen en az 6 karakter uzunluğundaki şifrenizi girin!",
                    Toast.LENGTH_LONG).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            }
                            else {
                                dialog.cancel();
                                Toast.makeText(SigninActivity.this, "E-posta adresi veya şifre hatalı! \nLütfen tekrar deneyin.",
                                        Toast.LENGTH_LONG).show();
                                updateUI(null);
                            }
                        }
                    });
        }
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            emailEditText.setText(""); //TO Forget everything!
            passwordEditText.setText("");

            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0); //TO Hide keyboard!

            Intent myIntent = new Intent(SigninActivity.this, NewPartyActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            dialog.cancel();
            SigninActivity.this.startActivity(myIntent);
        }
    }

    /*
    This method is called when the bottom SignUp Button is clicked.
     */
    public void gotoMain(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0); //TO Hide keyboard!
        Intent myIntent = new Intent(SigninActivity.this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To kill next activity when user clicks the back button!!!
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //To kill next activity when user clicks the back button!!!
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //To kill next activity when user clicks the back button!!!
        SigninActivity.this.startActivity(myIntent);
    }
}
