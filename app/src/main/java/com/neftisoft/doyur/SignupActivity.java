package com.neftisoft.doyur;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignupActivity extends AppCompatActivity {

    private boolean isAccepted = false;

    FirebaseAuth mAuth;
    FirebaseUser user;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // To change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = SignupActivity.this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(SignupActivity.this,R.color.colorNavigationBar));
        }

        mAuth = FirebaseAuth.getInstance();

        RadioButton acceptConditionsRadioButton = findViewById(R.id.accept_conditions_radio_view);

        String radioButtonText = "doyur Kullanım Koşulları ve Gizlilik Politikası sözleşmelerini kabul ediyorum.";

        SpannableString ss = new SpannableString(radioButtonText);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent myIntent = new Intent(SignupActivity.this, ConditionsActivity.class);
                SignupActivity.this.startActivity(myIntent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#FF6347"));
                ds.setUnderlineText(false);
            }
        };

        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent myIntent = new Intent(SignupActivity.this, PrivacyActivity.class);
                SignupActivity.this.startActivity(myIntent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#FF6347"));
                ds.setUnderlineText(false);
            }
        };

        ss.setSpan(clickableSpan1, 6, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan2, 28, 47, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        acceptConditionsRadioButton.setText(ss);
        acceptConditionsRadioButton.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /*
    This method is called when the Conditions and Terms are accepted by user.
     */
    public void acceptedConditions(View view) {
        //check the radio and pass if it is true (boolean)
        isAccepted = true;
    }

    /*
    This methods are called when the SignUp Button is clicked.
     */
    public void signUp(View view) {
        TextView userNameTextView = findViewById(R.id.user_name_text_view);
        String username = userNameTextView.getText().toString().trim();

        TextView userEmailTextView = findViewById(R.id.email_text_view);
        String email = userEmailTextView.getText().toString().trim();

        TextView userPasswordTextView = findViewById(R.id.pass_text_view);
        String password = userPasswordTextView.getText().toString().trim();

        TextView userPasswordAgainTextView = findViewById(R.id.pass_again_text_view);
        String passwordAgain = userPasswordAgainTextView.getText().toString().trim();

        checkGivenInformationAndSignup(username, email, password, passwordAgain);
    }
    private void checkGivenInformationAndSignup(final String username, final String email, String password, String passwordAgain) {
        if (username.length() < 1) {
            Toast.makeText(SignupActivity.this, "Lütfen bir kullanıcı adı girin!",
                    Toast.LENGTH_LONG).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { //VALIDATION of EMAIL
            Toast.makeText(SignupActivity.this, "Lütfen geçerli bir e-posta adresi girin!",
                    Toast.LENGTH_LONG).show();
        }
        else if (password.length() < 6) {
            Toast.makeText(SignupActivity.this, "Lütfen en az 6 karakter uzunluğunda bir şifre girin!",
                    Toast.LENGTH_LONG).show();
        }
        else if (!passwordAgain.equals(password)) {
            Toast.makeText(SignupActivity.this, "Lütfen şifrenizi tekrar girin!",
                    Toast.LENGTH_LONG).show();
        }
        else if (!isAccepted) {
            Toast.makeText(SignupActivity.this, "Kayıt olmak için lütfen koşulları kabul ediniz!",
                    Toast.LENGTH_LONG).show();
        }
        else {
            dialog = ProgressDialog.show(SignupActivity.this, "",
                    "Giriş yapılıyor...\nLütfen bekleyin!", true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                              
                                user = mAuth.getCurrentUser();

                                //To save username!
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //do something to let user know..
                                                } else {
                                                    //Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                                }
                                            }
                                        });
                                updateUI(username, email);
                            } else {
                                // If sign in fails, display a message to the user.
                                dialog.cancel();
                                Toast.makeText(SignupActivity.this, "Bu e-posta adresi zaten kayıtlı! \nLütfen Girişi Yapın!",
                                        Toast.LENGTH_LONG).show();   // + task.getException()   after GİRİŞ YAP! if reason is needed.
                                signInViaEmail(email);
                            }
                        }
                    });
        }
    }
    private void updateUI(String username, String email) {
        FirebaseDatabase mUsersDatabase;  //To store, update and get user information
        DatabaseReference mUsersDatabaseVitalsRef;  //To store, update and get user vital information
        DatabaseReference mUsersDatabaseRatingReviewRef;
        final DatabaseReference mUsersDatabaseTotPointRef;
        final DatabaseReference mUsersDatabaseTotPersonRef;
        if (user != null) {
            String newUid = user.getUid();

            mUsersDatabase = FirebaseDatabase.getInstance();  //To store and update user information
            mUsersDatabaseRatingReviewRef = mUsersDatabase.getReference().child("usersDatabase/" + newUid + "/" + "ratingReviewDatabase/");  //TO Initialize user rating information
            mUsersDatabaseTotPointRef = mUsersDatabaseRatingReviewRef.child("totalPoint");
            mUsersDatabaseTotPersonRef = mUsersDatabaseRatingReviewRef.child("totalPerson");
            mUsersDatabaseVitalsRef = mUsersDatabase.getReference().child("usersDatabase/" + newUid + "/" + "vitalsDatabase/");  //TO Store and update user information
            mUsersDatabaseVitalsRef.child("username").setValue(username);
            mUsersDatabaseVitalsRef.child("email").setValue(email);
            mUsersDatabaseVitalsRef.child("avatar").setValue("https://firebasestorage.$...$.com/o/$...$avatar_icon.png$...$");

            mUsersDatabaseTotPointRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot totPointSnapshot) {
                    try {
                        if (totPointSnapshot.getValue() != null) {
                            try {
                                //Do nothing
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            mUsersDatabaseTotPointRef.setValue("0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            mUsersDatabaseTotPersonRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot totPersonSnapshot) {
                    try {
                        if (totPersonSnapshot.getValue() != null) {
                            try {
                                //Do nothing
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            mUsersDatabaseTotPersonRef.setValue("0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            TextView userNameTextView = findViewById(R.id.user_name_text_view); //Forget everything!
            userNameTextView.setText("");
            TextView userEmailTextView = findViewById(R.id.email_text_view);
            userEmailTextView.setText("");
            TextView userPasswordTextView = findViewById(R.id.pass_text_view);
            userPasswordTextView.setText("");
            TextView userPasswordAgainTextView = findViewById(R.id.pass_again_text_view);
            userPasswordAgainTextView.setText("");

            user.sendEmailVerification();

            Toast.makeText(SignupActivity.this, "Lütfen gönderilen e-posta ile kaydınızı tamamlayın!",
                    Toast.LENGTH_LONG).show();

            dialog.cancel();

            Intent myIntent = new Intent(SignupActivity.this, NewPartyActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            SignupActivity.this.startActivity(myIntent);
        }
    }

    /*
    This method is called when the user already Signed Up and knows its email.
     */
    private void signInViaEmail(String email) {
        Intent myIntent = new Intent(SignupActivity.this, SigninActivity.class); //This email is already registered. Take user to the signIn activity with his/her email.

        myIntent.putExtra("User email", email);

        SignupActivity.this.startActivity(myIntent);

    }

    /*
    This method is called when the bottom SignIn Button is clicked.
     */
    public void signIn(View view) {
        Intent myIntent = new Intent(SignupActivity.this, SigninActivity.class);
        SignupActivity.this.startActivity(myIntent);
        onStop();
    }
}
