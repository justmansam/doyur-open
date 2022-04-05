//BİSMİLLAH.

package com.neftisoft.doyur;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //Facebook Initials
    Button fb;
    LoginButton loginButton;
    CallbackManager mCallbackManager;

    //Firebase Initials
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    private FirebaseDatabase mUsersDatabase;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseVitalsRef;  //To store, update and get user vital information
    private DatabaseReference mUsersDatabaseRatingReviewRef;
    private DatabaseReference mUsersDatabaseTotPointRef;
    private DatabaseReference mUsersDatabaseTotPersonRef;

    //Google Initials
    private static final int RC_SIGN_IN = 8586;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    TextView termsAndConditionsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent myIntent = new Intent(MainActivity.this, NewPartyActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To kill next activity when user clicks the back button!!!
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //To kill next activity when user clicks the back button!!!
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //To kill next activity when user clicks the back button!!!
                    MainActivity.this.startActivity(myIntent);
                }
            },1000);
        }
        else {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_main);
                    if (Build.VERSION.SDK_INT >= 21) {
                        Window window = MainActivity.this.getWindow();
                        // clear FLAG_TRANSLUCENT_STATUS flag:
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        // finally change the color
                        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.colorNavigationBar));
                    }
                    fb = findViewById(R.id.fake_login_with_facebook);
                    termsAndConditionsTextView = findViewById(R.id.terms_and_conditions);

                    // FACEBOOK!!!!!!!
                    // TO Initialize Facebook Login button
                    mCallbackManager = CallbackManager.Factory.create();
                    loginButton = findViewById(R.id.login_with_facebook);
                    loginButton.setPermissions(Arrays.asList("email", "public_profile"));
                    loginButton.setAuthType("rerequest");
                    loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            AccessToken token = loginResult.getAccessToken();
                            handleFacebookAccessToken(token);
                        }
                        @Override
                        public void onCancel() {
                            Toast.makeText(MainActivity.this, "Giriş iptal edildi!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(FacebookException error) {
                            Toast.makeText(MainActivity.this, "Beklenmedik bir hata oluştu. Lütfen daha sonra tekrar deneyin!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    // GOOGLE!!!!!!!
                    // Configure Google Sign In
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id1))
                            .requestEmail()
                            .build();
                    // Build a GoogleSignInClient with the options specified by gso.
                    mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

                    //Clickable TextView !
                    String tAndC = "Kayıt olarak veya giriş yaparak Kullanım Koşulları ve Gizlilik Politikası'nı kabul etmiş olursun";
                    SpannableString tc = new SpannableString(tAndC);
                    ClickableSpan clickableSpan3 = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            Intent myIntent = new Intent(MainActivity.this, ConditionsActivity.class);
                            MainActivity.this.startActivity(myIntent);
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(Color.parseColor("#FF6347"));
                            ds.setUnderlineText(false);
                        }
                    };
                    ClickableSpan clickableSpan4 = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            Intent myIntent = new Intent(MainActivity.this, PrivacyActivity.class);
                            MainActivity.this.startActivity(myIntent);
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(Color.parseColor("#FF6347"));
                            ds.setUnderlineText(false);
                        }
                    };
                    tc.setSpan(clickableSpan3, 32, 50, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tc.setSpan(clickableSpan4, 54, 73, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    termsAndConditionsTextView.setText(tc);
                    termsAndConditionsTextView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            },1500);
        }
    }
  
    // PROXY GOOGLE LOGIN BUTTON
    public void initGoogleLogin (View view) {
        signInViaGoogle();
    }
    private void signInViaGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // PROXY FACEBOOK LOGIN BUTTON
    public void initFacebookLogin (View view) {
        if (view == fb) {
            loginButton.performClick();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // GOOGLE!!! Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(MainActivity.this, "Giriş başarısız. Lütfen tekrar deneyin!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // FACEBOOK!!! Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //FACEBOOK!!!
    private void handleFacebookAccessToken(AccessToken token) {
        final ProgressDialog dialogF = ProgressDialog.show(MainActivity.this, "",
                "Giriş yapılıyor...", true);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser newUser = mAuth.getCurrentUser();
                            if (newUser != null) {
                                String uid = newUser.getUid();
                                String username = newUser.getDisplayName();
                                String email = newUser.getEmail();
                                Uri avatarUri = newUser.getPhotoUrl();
                                mUsersDatabase = FirebaseDatabase.getInstance();  //To store and update user information
                                mUsersDatabaseRatingReviewRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "ratingReviewDatabase/");  //TO Initialize user rating information
                                mUsersDatabaseTotPointRef = mUsersDatabaseRatingReviewRef.child("totalPoint");
                                mUsersDatabaseTotPersonRef = mUsersDatabaseRatingReviewRef.child("totalPerson");
                                mUsersDatabaseVitalsRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "vitalsDatabase/");  //To store and update user information
                                mUsersDatabaseVitalsRef.child("username").setValue(username);
                                mUsersDatabaseVitalsRef.child("email").setValue(email);
                                if (avatarUri != null) {
                                    String avatar = avatarUri.toString();
                                    mUsersDatabaseVitalsRef.child("avatar").setValue(avatar);
                                } else {
                                    mUsersDatabaseVitalsRef.child("avatar").setValue("https://firebasestorage.$...$/defaultAvatarImage%2Favatar_icon.png$...$");
                                    //TO Update user profile picture with default avatar!
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(Uri.parse("https://firebasestorage.$...$/defaultAvatarImage%2Favatar_icon.png$...$"))
                                            .build();
                                    newUser.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //DO NOTHING!
                                                    } else {
                                                        //DO NOTHING!
                                                    }
                                                }
                                            });
                                }
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
                            }
                            //TO Let user in!
                            final Intent myIntent = new Intent(MainActivity.this, NewPartyActivity.class);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            dialogF.cancel();
                            MainActivity.this.startActivity(myIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            dialogF.cancel();
                            Toast.makeText(MainActivity.this, "Giriş başarısız. Lütfen tekrar deneyin!",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    //GOOOGLE!!!
    private void firebaseAuthWithGoogle() {

        final ProgressDialog dialogG = ProgressDialog.show(MainActivity.this, "",
                "Giriş yapılıyor...", true);

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser newUser = mAuth.getCurrentUser();
                            if (newUser != null) {
                                String uid = newUser.getUid();
                                String username = newUser.getDisplayName();
                                String email = newUser.getEmail();
                                Uri avatarUri = newUser.getPhotoUrl();
                                mUsersDatabase = FirebaseDatabase.getInstance();  //To store and update user information
                                mUsersDatabaseRatingReviewRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "ratingReviewDatabase/");  //TO Initialize user rating information
                                mUsersDatabaseTotPointRef = mUsersDatabaseRatingReviewRef.child("totalPoint");
                                mUsersDatabaseTotPersonRef = mUsersDatabaseRatingReviewRef.child("totalPerson");
                                mUsersDatabaseVitalsRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "vitalsDatabase/");  //TO Store and update user information
                                mUsersDatabaseVitalsRef.child("username").setValue(username);
                                mUsersDatabaseVitalsRef.child("email").setValue(email);
                                if (avatarUri != null) {
                                    String avatar = avatarUri.toString();
                                    mUsersDatabaseVitalsRef.child("avatar").setValue(avatar);
                                } else {
                                    mUsersDatabaseVitalsRef.child("avatar").setValue("https://firebasestorage.$...$/defaultAvatarImage%2Favatar_icon.png$...$");
                                    //TO Update user profile picture with default avatar!
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(Uri.parse("https://firebasestorage.$...$/defaultAvatarImage%2Favatar_icon.png$...$"))
                                            .build();
                                    newUser.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //DO NOTHING!
                                                    } else {
                                                        //DO NOTHING!
                                                    }
                                                }
                                            });
                                }
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
                            }
                            //TO Let user in!
                            final Intent myIntent = new Intent(MainActivity.this, NewPartyActivity.class);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            dialogG.cancel();
                            MainActivity.this.startActivity(myIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                            //Log.e("Bİ SIKINTI VAR GOOGLE", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Giriş başarısız. Lütfen tekrar deneyin!",
                                    Toast.LENGTH_SHORT).show();
                            dialogG.cancel();
                        }

                        // ...
                    }
                });
    }

    public void signUp(View view) {
        Intent myIntent = new Intent(MainActivity.this, SignupActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void signIn(View view) {
        Intent myIntent = new Intent(MainActivity.this, SigninActivity.class);
        MainActivity.this.startActivity(myIntent);
    }
}
