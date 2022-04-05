package com.neftisoft.doyur;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int RC_PHOTO_PICKER = 2222;
    private static final int REQUEST_TAKE_PHOTO = 1111;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage mAvatarsStorage;
    private StorageReference mAvatarsStorageRef;

    private AdView mProfileAdView;

    private FirebaseDatabase mUsersDatabase;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseRef;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseNameRef;
    private DatabaseReference mUsersDatabaseEmailRef;
    private DatabaseReference mUsersDatabaseAvatarRef;

    private FirebaseDatabase mLocationDatabase;  //To store, update and get user information
    private DatabaseReference mLocationDatabaseRef;  //To store, update and get user information
    private DatabaseReference mLocationDatabaseCityRef;
    private DatabaseReference mLocationDatabaseCountyRef;
    private DatabaseReference mLocationDatabaseNbhoodRef;

    private FirebaseDatabase mStoreDatabase;
    private DatabaseReference mStoreDatabaseRef;
    private DatabaseReference mStoreDatabaseNameRef;

    private FirebaseDatabase mDailyDatabase;
    private DatabaseReference mDailyDatabaseRef;
    private DatabaseReference mDailyDatabaseImageRef;

    private FirebaseDatabase mCharityDatabase;
    private DatabaseReference mCharityDatabaseRef;
    private DatabaseReference mCharityDatabaseImageRef;

    private DatabaseReference mUsersDatabaseRatingRef;
    private DatabaseReference mUsersDatabaseRatingTotalPointRef;
    private DatabaseReference mUsersDatabaseRatingTotalPersonRef;

    private String username;
    private String email;
    private String uid;
    private String otherUid;
    private String profileOwnerUid;
    private String avatarUri;
    private String cityName;
    private String refCity;
    private String profileCity;
    private String countyName;
    private String refCounty;
    private String profileCounty;
    private String nbhoodName;
    private String refNeighborhood;
    private String profileNeighborhood;
    private String locationInfo;
    private String storeName;

    private int userGavePermission = 1; //TO CHECK IF USER ALREADY GAVE LOCATION PERMISSION!!! (1:YES, 0:NO)

    private String imageFileName;
    private String currentPhotoPath;

    private String ratingTotalPoint;
    private String ratingTotalPerson;
    private float ratingTotalPointFloat;
    private float ratingTotalPersonFloat;

    //Heading
    TextView headingTextView;
    //Avatar
    RelativeLayout avatarRelativeLayout;
    ImageView changeAvatarImageView;
    ImageView avatarImageView;
    TextView changeAvatarTextView;
    //Username
    RelativeLayout vitalsRelativeLayout;
    ImageView userNameImageView;
    TextView userNameTextView;
    TextView changeUserNameTextView;
    EditText newUserNameEditText;
    Button noTonewUserNameButton;
    Button okTonewUserNameButton;
    View dividerBeforeUsername;
    //Email
    ImageView userEmailImageView;
    TextView userEmailTextView;
    TextView changeUserEmailTextView;
    EditText newEmailEditText;
    Button noTonewEmailButton;
    Button okTonewEmailButton;
    //Location
    ImageView userLocationImageView;
    TextView userLocationTextView;
    TextView changeUserLocationTextView;
    TextView newLocationHeadingTextView;
    LinearLayout newLocationLayout;
    Button noTonewLocationButton;
    Spinner profileCitySpinner;
    Spinner profileCountySpinner;
    Spinner profileNbhoodSpinner;
    //Password
    ImageView userPasswordImageView;
    TextView userPasswordTextView;
    TextView changeUserPasswordTextView;
    EditText newPasswordEditText;
    EditText newPasswordAgainEditText;
    Button noTonewPasswordButton;
    Button okTonewPasswordButton;
    //Location Permission
    ImageView userLocationPermissionImageView;
    Switch userLocationPermissionSwitcher;
    //Party
    RelativeLayout foodsRelativeLayout;
    TextView storeNameTextView;
    TextView goToStoreTextView;
    ImageView dailyFoodImageView;
    ImageView charityFoodImageView;
    View dividerBeforeStore;
    //Rating
    RelativeLayout reviewsRelativeLayout;
    TextView userRatingTextView;
    TextView userRateCountTextView;
    View dividerBeforeRating;
    //Report !
    RelativeLayout reportRelativeLayout;
    TextView reportIssueTextView;
    TextView reportProfileTextView;
    View dividerBeforeIssue;
    RelativeLayout reportingRelativeLayout;
    EditText reportingEditText;
    //Finals
    Button messagesButton;
    Button signOutButton;
    View dividerBeforeExit;
    //
    RelativeLayout privacyConditionsLayout;
    View dividerBeforePrivacyConditions;
    //LOGO
    TextView logoTextView;
    TextView versionTextView;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct;

    private Dialog dialog;

    private InputMethodManager immProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // TO Change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) { //REDUNDAND
            Window window = ProfileActivity.this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //For grey status bar!
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //For grey status bar!
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(ProfileActivity.this,R.color.colorNavigationBar)); //For grey status bar!
        }

        //TO INITIALIZE ADMOB ADS!!!
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //TO SHOW ADMOD ADS!!!
        mProfileAdView = findViewById(R.id.profile_ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mProfileAdView.loadAd(adRequest);

        //To access user information!!
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        acct = GoogleSignIn.getLastSignedInAccount(ProfileActivity.this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // User comes from ChatActivity
        Intent chatProfileIntent = getIntent();
        String chatterUid = chatProfileIntent.getStringExtra("Chatter Uid");

        // User comes from ReviewActivity
        Intent reviewProfileIntent = getIntent();
        String reviewerUid = reviewProfileIntent.getStringExtra("Reviewer Uid");

        // User comes from StoreActivity
        Intent storeProfileIntent = getIntent();
        otherUid = storeProfileIntent.getStringExtra("Other Uid");

        if (chatterUid != null) {
            otherUid = chatterUid;
        }

        if (reviewerUid != null) {
            otherUid = reviewerUid;
        }

        if (user != null) {
            uid = user.getUid();
        } else {
            //Çıkış yapılmış. Girişe yönlendir!!!
        }

        /*
        TO Initialize ImageViews, TextViews, & Buttons!
         */
        //Heading !
        headingTextView = findViewById(R.id.profile_heading_textview);
        //Avatar !
        avatarRelativeLayout = findViewById(R.id.profile_avatar_relative_layout);
        changeAvatarImageView = findViewById(R.id.profile_new_avatar);
        avatarImageView = findViewById(R.id.profile_avatar_view);
        changeAvatarTextView = findViewById(R.id.profile_change_avatar);
        //Username !
        vitalsRelativeLayout = findViewById(R.id.profile_vitals_layout);
        userNameImageView = findViewById(R.id.profile_user_name_imageview);
        userNameTextView = findViewById(R.id.profile_user_name_textview);
        changeUserNameTextView = findViewById(R.id.profile_change_username);
        newUserNameEditText = findViewById(R.id.profile_new_user_name_textview);
        noTonewUserNameButton = findViewById(R.id.profile_no_tonew_username_button);
        okTonewUserNameButton = findViewById(R.id.profile_ok_tonew_username_button);
        dividerBeforeUsername = findViewById(R.id.divider_before_username);
        //Email !
        userEmailImageView = findViewById(R.id.profile_user_email_imageview);
        userEmailTextView = findViewById(R.id.profile_user_email_textview);
        changeUserEmailTextView = findViewById(R.id.profile_change_email);
        newEmailEditText = findViewById(R.id.profile_new_email_edittext);
        noTonewEmailButton = findViewById(R.id.profile_no_tonew_email_button);
        okTonewEmailButton = findViewById(R.id.profile_ok_tonew_email_button);
        //Location !
        userLocationImageView = findViewById(R.id.profile_user_location_imageview);
        userLocationTextView = findViewById(R.id.profile_user_location_textview);
        changeUserLocationTextView = findViewById(R.id.profile_change_location);
        newLocationHeadingTextView = findViewById(R.id.profile_change_location_heading_textview);
        newLocationLayout = findViewById(R.id.profile_new_location_layout);
        noTonewLocationButton = findViewById(R.id.profile_no_tonew_location_button);
        //Password !
        userPasswordImageView = findViewById(R.id.profile_user_password_imageview);
        userPasswordTextView = findViewById(R.id.profile_user_password_textview);
        changeUserPasswordTextView = findViewById(R.id.profile_change_password);
        newPasswordEditText = findViewById(R.id.profile_password_textview);
        newPasswordAgainEditText = findViewById(R.id.profile_password_again_textview);
        noTonewPasswordButton = findViewById(R.id.profile_no_tonew_password_button);
        okTonewPasswordButton = findViewById(R.id.profile_ok_tonew_password_button);
        //Location Permission !
        userLocationPermissionImageView = findViewById(R.id.profile_user_location_permission_imageview);
        userLocationPermissionSwitcher = findViewById(R.id.profile_user_location_permission_switch);
        //Party !
        foodsRelativeLayout = findViewById(R.id.profile_foods_layout);
        storeNameTextView = findViewById(R.id.profile_user_store_textview);
        goToStoreTextView = findViewById(R.id.profile_goto_store);
        dailyFoodImageView = findViewById(R.id.profile_user_daily_image_view);
        charityFoodImageView = findViewById(R.id.profile_user_charity_image_view);
        dividerBeforeStore = findViewById(R.id.divider_before_store);
        //Rating !
        reviewsRelativeLayout = findViewById(R.id.profile_reviews_layout);
        userRatingTextView = findViewById(R.id.profile_user_rating_textview);
        userRateCountTextView = findViewById(R.id.profile_user_rate_count_textview);
        dividerBeforeRating = findViewById(R.id.divider_before_rating);
        //Report !
        reportRelativeLayout = findViewById(R.id.profile_report_issue_layout);
        reportIssueTextView = findViewById(R.id.profile_report_issue_textview);
        reportProfileTextView = findViewById(R.id.profile_report_profile_textview);
        dividerBeforeIssue = findViewById(R.id.divider_before_issue);
        reportingRelativeLayout = findViewById(R.id.profile_reporting_relative_layout);
        reportingEditText = findViewById(R.id.profile_reporting_edittext);
        //Finals !
        messagesButton = findViewById(R.id.profile_user_messages_button);
        signOutButton = findViewById(R.id.profile_signout_button);
        dividerBeforeExit = findViewById(R.id.divider_before_exit);
        //Privacy&Conditions
        privacyConditionsLayout = findViewById(R.id.privacy_conditions_layout);
        dividerBeforePrivacyConditions = findViewById(R.id.divider_before_privacy_conditions);
        //LOGO !
        logoTextView = findViewById(R.id.profile_logo_textview);
        versionTextView = findViewById(R.id.profile_version_textview);

        //City Location Setter !!
        profileCitySpinner = findViewById(R.id.profile_city_spinner);
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(ProfileActivity.this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileCitySpinner.setDropDownWidth(400);
        profileCitySpinner.setAdapter(cityAdapter);
        profileCitySpinner.setOnItemSelectedListener(ProfileActivity.this);

        if (otherUid != null) {
            if (!otherUid.equals(uid)) { //MEANS PROFILE IS NOT MINE!!!
                profileOwnerUid = otherUid;
                //Make some Views Dissapear and Non-Clickable!!!! (Change -username,email,location- buttons and add Avatar photo!)
                changeAvatarTextView.setVisibility(View.GONE);
                changeAvatarImageView.setClickable(false);
                changeUserNameTextView.setVisibility(View.GONE);
                changeUserLocationTextView.setVisibility(View.GONE);
                userEmailImageView.setVisibility(View.GONE);
                userEmailTextView.setVisibility(View.GONE);
                changeUserEmailTextView.setVisibility(View.GONE);
                userPasswordImageView.setVisibility(View.GONE);
                userPasswordTextView.setVisibility(View.GONE);
                changeUserPasswordTextView.setVisibility(View.GONE);
                messagesButton.setVisibility(View.GONE);
                dividerBeforeStore.setVisibility(View.GONE);
                goToStoreTextView.setVisibility(View.GONE);
                reportIssueTextView.setVisibility(View.GONE);
                reportProfileTextView.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.GONE);
                privacyConditionsLayout.setVisibility(View.GONE);
                dividerBeforePrivacyConditions.setVisibility(View.GONE);
                logoTextView.setVisibility(View.GONE);
                versionTextView.setVisibility(View.GONE);
            } else {
                profileOwnerUid = uid;
                if (user != null) {
                    Uri photoUrl = user.getPhotoUrl();
                    if (photoUrl != null) {
                        avatarUri = photoUrl.toString();
                        ImageView avatarImageView = findViewById(R.id.profile_avatar_view);
                        Glide.with(avatarImageView.getContext())
                                .load(avatarUri)
                                .centerCrop()
                                .into(avatarImageView);
                    }
                }
            }
        } else {
            profileOwnerUid = uid;
            if (user != null) {
                Uri photoUrl = user.getPhotoUrl();
                if (photoUrl != null) {
                    avatarUri = photoUrl.toString();
                    ImageView avatarImageView = findViewById(R.id.profile_avatar_view);
                    Glide.with(avatarImageView.getContext())
                            .load(avatarUri)
                            .centerCrop()
                            .into(avatarImageView);
                }
            }
        }

        mAvatarsStorage = FirebaseStorage.getInstance();
        mAvatarsStorageRef = mAvatarsStorage.getReference().child("avatarsStorage/" + uid + "/");

        /*
        TO DISPLAY PROFILE VITALS !!!
         */
        mUsersDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mUsersDatabaseRef = mUsersDatabase.getReference().child("usersDatabase/" + profileOwnerUid + "/" + "vitalsDatabase/");  //To store and update user information
        mUsersDatabaseNameRef = mUsersDatabaseRef.child("username");
        mUsersDatabaseEmailRef = mUsersDatabaseRef.child("email");
        mUsersDatabaseAvatarRef = mUsersDatabaseRef.child("avatar");
        mUsersDatabaseNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usernameSnapshot) {
                try {
                    if (usernameSnapshot.getValue() != null) {
                        try {
                            username = usernameSnapshot.getValue().toString();
                            userNameTextView.setText(username);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mUsersDatabaseEmailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot emailSnapshot) {
                try {
                    if (emailSnapshot.getValue() != null) {
                        try {
                            if (profileOwnerUid.equals(uid)) {
                                email = emailSnapshot.getValue().toString();
                                userEmailTextView.setText(email);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mUsersDatabaseAvatarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot avatarSnapshot) {
                try {
                    if (avatarSnapshot.getValue() != null) {
                        try {
                            String avatarImageString = avatarSnapshot.getValue().toString();
                            Uri avatarImageUrl = Uri.parse(avatarImageString);
                            Glide.with(avatarImageView.getContext())
                                    .load(avatarImageUrl)
                                    .centerCrop()
                                    .into(avatarImageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO DISPLAY PROFILE LOCATION !!!
         */
        mLocationDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mLocationDatabaseRef = mLocationDatabase.getReference().child("usersDatabase/" + profileOwnerUid + "/" + "locationDatabase/");  //To store and update user information
        mLocationDatabaseCityRef = mLocationDatabaseRef.child("city");
        mLocationDatabaseCountyRef = mLocationDatabaseRef.child("county");
        mLocationDatabaseNbhoodRef = mLocationDatabaseRef.child("nbhood");
        //mUsersDatabaseAvatarRef.setValue(avatarUri);
        mLocationDatabaseCityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot citySnapshot) {
                try {
                    if (citySnapshot.getValue() != null) {
                        try {
                            cityName = citySnapshot.getValue().toString();
                            refCity = cityName.replace("ç","c").replace("Ç","C") //TO Get refLocations to use them for listening the Database Reference.
                                    .replace("ğ","g").replace("Ğ","G")
                                    .replace("ı","i").replace("İ","I")
                                    .replace("ö","o").replace("Ö","O")
                                    .replace("ü","u").replace("Ü","U")
                                    .replace("ş","s").replace("Ş","S").replace(" ","");
                            if (refCity != null && refCounty != null && refNeighborhood != null) {
                                displayFoodPhotos();
                            } else {
                                findViewById(R.id.profile_user_daily_alert_textview).setVisibility(View.VISIBLE);
                                findViewById(R.id.profile_user_charity_alert_textview).setVisibility(View.VISIBLE);
                            }
                            locationInfo = cityName + ", " + countyName + ", " + nbhoodName;
                            userLocationTextView.setText(locationInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mLocationDatabaseCountyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot countySnapshot) {
                try {
                    if (countySnapshot.getValue() != null) {
                        try {
                            countyName = countySnapshot.getValue().toString();
                            refCounty = countyName.replace("ç","c").replace("Ç","C") //TO Get refLocations to use them for listening the Database Reference.
                                    .replace("ğ","g").replace("Ğ","G")
                                    .replace("ı","i").replace("İ","I")
                                    .replace("ö","o").replace("Ö","O")
                                    .replace("ü","u").replace("Ü","U")
                                    .replace("ş","s").replace("Ş","S").replace(" ","");
                            if (refCity != null && refCounty != null && refNeighborhood != null) {
                                displayFoodPhotos();
                            } else {
                                findViewById(R.id.profile_user_daily_alert_textview).setVisibility(View.VISIBLE);
                                findViewById(R.id.profile_user_charity_alert_textview).setVisibility(View.VISIBLE);
                            }
                            locationInfo = cityName + ", " + countyName + ", " + nbhoodName;
                            userLocationTextView.setText(locationInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mLocationDatabaseNbhoodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot nbhoodSnapshot) {
                try {
                    if (nbhoodSnapshot.getValue() != null) {
                        try {
                            nbhoodName = nbhoodSnapshot.getValue().toString();
                            refNeighborhood = nbhoodName.replace("ç","c").replace("Ç","C") //TO Get refLocations to use them for listening the Database Reference.
                                    .replace("ğ","g").replace("Ğ","G")
                                    .replace("ı","i").replace("İ","I")
                                    .replace("ö","o").replace("Ö","O")
                                    .replace("ü","u").replace("Ü","U")
                                    .replace("ş","s").replace("Ş","S").replace(" ","");
                            if (refCity != null && refCounty != null && refNeighborhood != null) {
                                displayFoodPhotos();
                            } else {
                                findViewById(R.id.profile_user_daily_alert_textview).setVisibility(View.VISIBLE);
                                findViewById(R.id.profile_user_charity_alert_textview).setVisibility(View.VISIBLE);
                            }
                            locationInfo = cityName + ", " + countyName + ", " + nbhoodName;
                            userLocationTextView.setText(locationInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO DISPLAY PROFILE STORE NAME !!!
         */
        mStoreDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mStoreDatabaseRef = mStoreDatabase.getReference().child("usersDatabase/" + profileOwnerUid + "/" + "storeFoodsDatabase/");  //To store and update user information
        mStoreDatabaseNameRef = mStoreDatabaseRef.child("storeName");
        mStoreDatabaseNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot storeNameSnapshot) {
                try {
                    if (storeNameSnapshot.getValue() != null) {
                        try {
                            storeName = storeNameSnapshot.getValue().toString();
                            storeNameTextView.setText(storeName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        storeNameTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        storeNameTextView.setText("Mutfak açılmamış!");
                        if (profileOwnerUid.equals(uid)) {
                            goToStoreTextView.setText("Oluştur");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO DISPLAY RATING !!!
         */
        mUsersDatabaseRatingRef = mUsersDatabase.getReference().child("usersDatabase/" + profileOwnerUid + "/" + "ratingReviewDatabase/");
        mUsersDatabaseRatingTotalPointRef = mUsersDatabaseRatingRef.getRef().child("totalPoint/");
        mUsersDatabaseRatingTotalPersonRef = mUsersDatabaseRatingRef.getRef().child("totalPerson/");
        mUsersDatabaseRatingTotalPointRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingTotPointSnapshot) {
                try {
                    if (ratingTotPointSnapshot.getValue() != null) {
                        try {
                            ratingTotalPoint = ratingTotPointSnapshot.getValue().toString();
                            ratingTotalPointFloat = Float.parseFloat(ratingTotalPoint);
                            calculateRating();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //
            }
        });
        mUsersDatabaseRatingTotalPersonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingTotPersonSnapshot) {
                try {
                    if (ratingTotPersonSnapshot.getValue() != null) {
                        try {
                            ratingTotalPerson = ratingTotPersonSnapshot.getValue().toString();
                            ratingTotalPersonFloat = Float.parseFloat(ratingTotalPerson);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //
            }
        });

        if (user != null && uid.equals(profileOwnerUid)) {
            String emailOfFacebookUsers = user.getEmail();
            String userDoesntWantToUpdateEmail = userEmailTextView.getText().toString();
            if (emailOfFacebookUsers == null && !userDoesntWantToUpdateEmail.equals("N/A")) {
                askForUpdatingEmail();
            }
        }

        checkUserLocationPermission();
    }

    /*
    TO ASK FOR USER TO UPDATE EMAIL (FOR SOME FB USERS!)
     */
    private void askForUpdatingEmail() {
        final EditText emailInput = new EditText(this);
        emailInput.setHint("E-posta adresinizi girin!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            emailInput.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        AlertDialog.Builder builderEmailProfile = new AlertDialog.Builder(ProfileActivity.this);
        builderEmailProfile.setTitle("E-posta adresini güncelle");
        builderEmailProfile.setMessage("\nE-posta adresiniz kayıt sırasında alınamadı!\nSizinle iletişim kurabilmemiz için e-posta adresinizi güncellemek ister misiniz?\n");
        builderEmailProfile.setCancelable(false);
        emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builderEmailProfile.setView(emailInput);

        builderEmailProfile.setPositiveButton(Html.fromHtml("<font color='#556B2F'>Güncelle</font>"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builderEmailProfile.setNeutralButton(Html.fromHtml("<font color='#707070'>Sonra</font>"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builderEmailProfile.setNegativeButton(Html.fromHtml("<font color='#FF6347'>Tekrar sorma!</font>"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        final AlertDialog alertEmailProfile = builderEmailProfile.create();
        alertEmailProfile.show();
        alertEmailProfile.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = emailInput.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) { //VALIDATION of EMAIL
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Lütfen geçerli bir e-posta adresi girin!");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                } else {
                    alertEmailProfile.dismiss();
                    Dialog dialogEmail = ProgressDialog.show(ProfileActivity.this, "",
                            "Kullanıcı e-postanız güncelleniyor...", true);

                    updateEmail(newEmail, dialogEmail);
                }
            }
        });
        alertEmailProfile.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertEmailProfile.dismiss();
            }
        });
        alertEmailProfile.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsersDatabaseEmailRef.setValue("N/A");
                alertEmailProfile.dismiss();
            }
        });
    }

    /*
    TO CHECK IF USER GAVE LOCATION PERMISSION BEFORE
     */
    private void checkUserLocationPermission() {
        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && profileOwnerUid.equals(uid)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                userLocationPermissionImageView.setVisibility(View.VISIBLE);
                userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                userLocationPermissionImageView.setVisibility(View.VISIBLE);
                userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
            }
            userGavePermission = 0;
        }
    }

    /*
    TO CALCULATE AND SET RATING OF STORE OWNER
     */
    private void calculateRating() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ratingTotalPersonFloat != 0) {
                    float calculatedRating = ratingTotalPointFloat/ratingTotalPersonFloat;
                    String finalRatingString = String.format(Locale.getDefault(),"%.1f", calculatedRating);
                    userRatingTextView.setText(finalRatingString);
                    userRateCountTextView.setText("(" + ratingTotalPerson + " oy)");
                    if (calculatedRating >= 9) {
                        userRatingTextView.setTextColor(Color.parseColor("#1B5E20"));
                    }
                    else if (9 > calculatedRating && calculatedRating >= 8) {
                        userRatingTextView.setTextColor(Color.parseColor("#66BB6A"));
                    }
                    else if (8 > calculatedRating && calculatedRating >= 7) {
                        userRatingTextView.setTextColor(Color.parseColor("#D4E157"));
                    }
                    else if (7 > calculatedRating && calculatedRating >= 6) {
                        userRatingTextView.setTextColor(Color.parseColor("#FFEE58"));
                    }
                    else if (6 > calculatedRating && calculatedRating >= 5) {
                        userRatingTextView.setTextColor(Color.parseColor("#FFA726"));
                    }
                    else if (5 > calculatedRating && calculatedRating >= 4) {
                        userRatingTextView.setTextColor(Color.parseColor("#FF7043"));
                    }
                    else if (4 > calculatedRating && calculatedRating > 0) {
                        userRatingTextView.setTextColor(Color.parseColor("#BF360C"));
                    }
                } else {
                    userRatingTextView.setText("0.0");
                    userRateCountTextView.setText("(Henüz oy verilmemiş!)");
                }
            }
        }, 500);
    }

    /*
    TO DISPLAY DAILY AND CHARITY FOODS AFTER THE REFERENCE LOCATIONS ARE SET !!!
     */
    private void displayFoodPhotos() {
        /*
        TO DISPLAY PROFILE DAILY FOOD !!! (NOTE THAT, IN ORDER TO MAKE SURE THE DAILY FOODS DELETED AND NEVER SEEN IF NOT RE-UPLOADED CURRENT DAY THE USERDATABASE IS NOT USED !!!)
         */
        mDailyDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mDailyDatabaseRef = mDailyDatabase.getReference().child("foodDailyPhotoDatabase/" + refCity + "/" + refCounty + "/" + refNeighborhood + "/" + profileOwnerUid + "/");  //To store and update user information
        mDailyDatabaseImageRef = mDailyDatabaseRef.child("photoUrl");
        mDailyDatabaseImageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dailyFoodSnapshot) {
                try {
                    if (dailyFoodSnapshot.getValue() != null) {
                        try {
                            findViewById(R.id.profile_user_daily_alert_textview).setVisibility(View.GONE);
                            findViewById(R.id.profile_user_daily_layout).setVisibility(View.VISIBLE);
                            String dailyFoodImageString = dailyFoodSnapshot.getValue().toString();
                            Uri dailyFoodImageUrl = Uri.parse(dailyFoodImageString);
                            Glide.with(dailyFoodImageView.getContext())
                                    .load(dailyFoodImageUrl)
                                    .centerCrop()
                                    .into(dailyFoodImageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Name is null.");
                        findViewById(R.id.profile_user_daily_alert_textview).setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO DISPLAY PROFILE CHARITY FOOD !!! (NOTE THAT IN ORDER TO MAKE SURE THE CHARITY FOODS DELETED AND NEVER SEEN IF NOT RE-UPLOADED CURRENT DAY THE USERDATABASE IS NOT USED !!!)
         */
        mCharityDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mCharityDatabaseRef = mCharityDatabase.getReference().child("foodCharityPhotoDatabase/" + refCity + "/" + refCounty + "/" + refNeighborhood + "/" + profileOwnerUid + "/");  //To store and update user information
        mCharityDatabaseImageRef = mCharityDatabaseRef.child("photoUrl");
        mCharityDatabaseImageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot charityFoodSnapshot) {
                try {
                    if (charityFoodSnapshot.getValue() != null) {
                        try {
                            findViewById(R.id.profile_user_charity_alert_textview).setVisibility(View.GONE);
                            findViewById(R.id.profile_user_charity_layout).setVisibility(View.VISIBLE);
                            String charityFoodImageString = charityFoodSnapshot.getValue().toString();
                            Uri charityFoodImageUrl = Uri.parse(charityFoodImageString);
                            Glide.with(charityFoodImageView.getContext())
                                    .load(charityFoodImageUrl)
                                    .centerCrop()
                                    .into(charityFoodImageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log
                        findViewById(R.id.profile_user_charity_alert_textview).setVisibility(View.VISIBLE);
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

    /*
    TO GET BACK !!!
     */
    public void getBack(View view) {
        if (immProfile != null) {
            immProfile.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
        onBackPressed();
    }

    /*
    TO CHANGE AVATAR !!!
     */
    public void changeAvatar(View view) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Profil resmini güncelle...");
        builder2.setMessage("\n");
        builder2.setCancelable(true);
        builder2.setPositiveButton(
                "Fotoğraf çek",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile(); //Take image file name for the avatar.
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(ProfileActivity.this,
                                        "com.neftisoft.android.fileprovider",
                                        photoFile);
                                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                            }
                        }
                        dialog.cancel();
                    }
                });
        builder2.setNegativeButton(
                "Galeriden seç",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        pictureIntent.setType("image/jpeg");
                        pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                        dialog.cancel();
                    }
                });
        builder2.setNeutralButton("İptal",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert12 = builder2.create();
        alert12.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent pictureIntent) { //ON ACTIVITY RESULT ACTION
        super.onActivityResult(requestCode, resultCode, pictureIntent);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            addPhotoShoot();
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = pictureIntent.getData();
            addPhotoPicked(selectedImageUri);
        }
    }
    /*
    TO CREATE IMAGE FILE
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        imageFileName = "_" + uid + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,   /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    /*
    TO ADD PHOTO TAKEN TO FIREBASE STORAGE
     */
    private void addPhotoShoot() {

        dialog = ProgressDialog.show(ProfileActivity.this, "",
                "Profil resminiz güncelleniyor...\nLütfen bekleyin!", true);

        final Uri file = Uri.fromFile(new File(currentPhotoPath));
        final StorageReference mavatarsStorageRef = mAvatarsStorageRef.child(imageFileName);

        mavatarsStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Show uploaded photo in the main screen

                        // Get a URL to the uploaded content
                        getDownloadUrl(mavatarsStorageRef, file);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialog.cancel();

                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Güncelleme başarısız oldu! Lütfen tekrar deneyin.");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                        // ...
                    }
                });
    }
    /*
    TO GET DOWNLOAD URL
     */
    private void getDownloadUrl(final StorageReference mAvatarsStorageRef, Uri file) {

        UploadTask uploadTask = mAvatarsStorageRef.putFile(file);

        final Task<Uri> photoUrl = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                final StorageReference[] mAvatarPhotoStorageDownloadRef = {null};
                final Task<Uri>[] downloadAvatarPhotoUri = new Task[1];
                if (!task.isSuccessful()) {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Beklenmedik bir hata oluştu! Lütfen tekrar deneyin.");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    throw task.getException();
                } else {
                    String mAvatarPhotoStorageUploadChildRefString = mAvatarsStorageRef.getName();
                    mAvatarPhotoStorageUploadChildRefString = mAvatarPhotoStorageUploadChildRefString + "_1024x1024";
                    StorageReference mStorePhotoStorageUploadRootRef = mAvatarsStorageRef.getParent();
                    Thread.sleep(2100); //WAIT FOR THE FIREBASE FUNCTION TO RESIZE THE IMAGE!!!
                    mAvatarPhotoStorageDownloadRef[0] = mStorePhotoStorageUploadRootRef.child(mAvatarPhotoStorageUploadChildRefString);
                    downloadAvatarPhotoUri[0] = mAvatarPhotoStorageDownloadRef[0].getDownloadUrl();
                    return downloadAvatarPhotoUri[0];
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadAvatarUri = task.getResult();

                    try {
                        getUrlandSetAvatar(downloadAvatarUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Beklenmedik bir hata oluştu! Lütfen tekrar deneyin.");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
    }
    private void getUrlandSetAvatar(Uri downloadAvatarUri) throws FileNotFoundException {
        avatarUri = downloadAvatarUri.toString();
        ImageView avatarImageView = findViewById(R.id.profile_avatar_view);
        Glide.with(avatarImageView.getContext())
                .load(downloadAvatarUri)
                .centerCrop()
                .into(avatarImageView);

        mUsersDatabaseAvatarRef.setValue(avatarUri);
      
        //TO Update user profile picture!
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(avatarUri))
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Profil resminiz başarıyla güncellendi!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
                        } else {
                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Güncelleme başarısız oldu! Lütfen tekrar deneyin.");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        }
                    }
                });
        dialog.cancel();
    }
    /*
    TO ADD PHOTO PICKED TO FIREBASE STORAGE
     */
    private void addPhotoPicked(Uri selectedImageUri) {

        dialog = ProgressDialog.show(ProfileActivity.this, "",
                "Profil resminiz güncelleniyor...\nLütfen bekleyin!", true);

        final Uri file = selectedImageUri; //selected image uri
        final StorageReference mavatarsStorageRef = mAvatarsStorageRef.child("_" + uid + "_");

        mavatarsStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Show uploaded photo in the main screen

                        // Get a URL to the uploaded content
                        getDownloadUrl(mavatarsStorageRef, file);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialog.cancel();

                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Güncelleme başarısız oldu lütfen tekrar deneyin!");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                        // ...
                    }
                });
    }

    /*
    TO CHANGE USERNAME !!!
     */
    public void changeUsername(View view) {
        headingTextView.setText("Kullanıcı Adı");
        avatarRelativeLayout.setVisibility(View.GONE);

        dividerBeforeUsername.setVisibility(View.INVISIBLE);
        userNameImageView.setVisibility(View.GONE);
        userNameTextView.setVisibility(View.GONE);
        changeUserNameTextView.setVisibility(View.GONE);

        userEmailImageView.setVisibility(View.GONE);
        userEmailTextView.setVisibility(View.GONE);
        changeUserEmailTextView.setVisibility(View.GONE);

        userLocationImageView.setVisibility(View.GONE);
        userLocationTextView.setVisibility(View.GONE);
        changeUserLocationTextView.setVisibility(View.GONE);

        userPasswordImageView.setVisibility(View.GONE);
        userPasswordTextView.setVisibility(View.GONE);
        changeUserPasswordTextView.setVisibility(View.GONE);

        userLocationPermissionImageView.setVisibility(View.GONE);
        userLocationPermissionSwitcher.setVisibility(View.GONE);

        dividerBeforeStore.setVisibility(View.GONE);
        foodsRelativeLayout.setVisibility(View.GONE);
        dividerBeforeRating.setVisibility(View.GONE);
        reviewsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeIssue.setVisibility(View.GONE);
        reportRelativeLayout.setVisibility(View.GONE);

        dividerBeforeExit.setVisibility(View.GONE);
        messagesButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);

        privacyConditionsLayout.setVisibility(View.GONE);
        dividerBeforePrivacyConditions.setVisibility(View.GONE);

        logoTextView.setVisibility(View.GONE);
        versionTextView.setVisibility(View.GONE);

        newUserNameEditText.setVisibility(View.VISIBLE);
        noTonewUserNameButton.setVisibility(View.VISIBLE);
        okTonewUserNameButton.setVisibility(View.VISIBLE);

        newUserNameEditText.requestFocus(); //TO Get focus on the first edittext programmatically!
        immProfile = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        if (immProfile != null) {
            immProfile.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
        }
    }
    /*
    CANCEL NEW USERNAME !!!
     */
    public void cancelNewUsername(View view) {
        //InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
        immProfile.hideSoftInputFromWindow(newUserNameEditText.getWindowToken(), 0); //TO Hide keyboard!
        immProfile = null;

        newUserNameEditText.setText("");
        newUserNameEditText.setVisibility(View.GONE);
        noTonewUserNameButton.setVisibility(View.GONE);
        okTonewUserNameButton.setVisibility(View.GONE);

        headingTextView.setText("Kullanıcı Bilgileri");
        avatarRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeUsername.setVisibility(View.VISIBLE);
        userNameImageView.setVisibility(View.VISIBLE);
        userNameTextView.setVisibility(View.VISIBLE);
        changeUserNameTextView.setVisibility(View.VISIBLE);

        userEmailImageView.setVisibility(View.VISIBLE);
        userEmailTextView.setVisibility(View.VISIBLE);
        changeUserEmailTextView.setVisibility(View.VISIBLE);

        userLocationImageView.setVisibility(View.VISIBLE);
        userLocationTextView.setVisibility(View.VISIBLE);
        changeUserLocationTextView.setVisibility(View.VISIBLE);

        userPasswordImageView.setVisibility(View.VISIBLE);
        userPasswordTextView.setVisibility(View.VISIBLE);
        changeUserPasswordTextView.setVisibility(View.VISIBLE);

        if (userGavePermission == 0) {
            userLocationPermissionImageView.setVisibility(View.VISIBLE);
            userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
        }

        dividerBeforeStore.setVisibility(View.VISIBLE);
        foodsRelativeLayout.setVisibility(View.VISIBLE);
        dividerBeforeRating.setVisibility(View.VISIBLE);
        reviewsRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeIssue.setVisibility(View.VISIBLE);
        reportRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeExit.setVisibility(View.VISIBLE);
        messagesButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.VISIBLE);

        privacyConditionsLayout.setVisibility(View.VISIBLE);
        dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

        logoTextView.setVisibility(View.VISIBLE);
        versionTextView.setVisibility(View.VISIBLE);
    }
    /*
    SAVE NEW USERNAME !!!
     */
    public void saveNewUsername(View view) {
        String newUsername;
        newUsername = newUserNameEditText.getText().toString().trim();
        if (newUsername.length() > 0) {
            Dialog dialogName = ProgressDialog.show(ProfileActivity.this, "",
                    "Kullanıcı adınız güncelleniyor...", true);

            immProfile.hideSoftInputFromWindow(newUserNameEditText.getWindowToken(), 0); //TO Hide keyboard!
            immProfile = null;

            newUserNameEditText.setText("");
            newUserNameEditText.setVisibility(View.GONE);
            noTonewUserNameButton.setVisibility(View.GONE);
            okTonewUserNameButton.setVisibility(View.GONE);

            headingTextView.setText("Kullanıcı Bilgileri");
            avatarRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeUsername.setVisibility(View.VISIBLE);
            userNameImageView.setVisibility(View.VISIBLE);
            userNameTextView.setVisibility(View.VISIBLE);
            changeUserNameTextView.setVisibility(View.VISIBLE);

            userEmailImageView.setVisibility(View.VISIBLE);
            userEmailTextView.setVisibility(View.VISIBLE);
            changeUserEmailTextView.setVisibility(View.VISIBLE);

            userLocationImageView.setVisibility(View.VISIBLE);
            userLocationTextView.setVisibility(View.VISIBLE);
            changeUserLocationTextView.setVisibility(View.VISIBLE);

            userPasswordImageView.setVisibility(View.VISIBLE);
            userPasswordTextView.setVisibility(View.VISIBLE);
            changeUserPasswordTextView.setVisibility(View.VISIBLE);

            if (userGavePermission == 0) {
                userLocationPermissionImageView.setVisibility(View.VISIBLE);
                userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
            }

            dividerBeforeStore.setVisibility(View.VISIBLE);
            foodsRelativeLayout.setVisibility(View.VISIBLE);
            dividerBeforeRating.setVisibility(View.VISIBLE);
            reviewsRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeIssue.setVisibility(View.VISIBLE);
            reportRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeExit.setVisibility(View.VISIBLE);
            messagesButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);

            privacyConditionsLayout.setVisibility(View.VISIBLE);
            dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

            logoTextView.setVisibility(View.VISIBLE);
            versionTextView.setVisibility(View.VISIBLE);

            updateUsername(newUsername, dialogName);
        } else {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen bir kullanıcı adı girin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }
    /*
    UPDATE USER NAME !!!
     */
    private void updateUsername(final String newUsername, final Dialog dialogName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mUsersDatabaseNameRef.setValue(newUsername);

                            dialogName.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Kullanıcı adınız güncellendi!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
                        } else {
                            dialogName.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Bir sorun oluştu! Lütfen tekrar deneyin.");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        }
                    }
                });
    }

    /*
    TO CHANGE EMAIL !!!
     */
    public void changeEmail(View view) {
        headingTextView.setText("Kullanıcı E-postası");
        avatarRelativeLayout.setVisibility(View.GONE);

        dividerBeforeUsername.setVisibility(View.INVISIBLE);
        userNameImageView.setVisibility(View.GONE);
        userNameTextView.setVisibility(View.GONE);
        changeUserNameTextView.setVisibility(View.GONE);

        userEmailImageView.setVisibility(View.GONE);
        userEmailTextView.setVisibility(View.GONE);
        changeUserEmailTextView.setVisibility(View.GONE);

        userLocationImageView.setVisibility(View.GONE);
        userLocationTextView.setVisibility(View.GONE);
        changeUserLocationTextView.setVisibility(View.GONE);

        userPasswordImageView.setVisibility(View.GONE);
        userPasswordTextView.setVisibility(View.GONE);
        changeUserPasswordTextView.setVisibility(View.GONE);

        userLocationPermissionImageView.setVisibility(View.GONE);
        userLocationPermissionSwitcher.setVisibility(View.GONE);

        dividerBeforeStore.setVisibility(View.GONE);
        foodsRelativeLayout.setVisibility(View.GONE);
        dividerBeforeRating.setVisibility(View.GONE);
        reviewsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeIssue.setVisibility(View.GONE);
        reportRelativeLayout.setVisibility(View.GONE);

        dividerBeforeExit.setVisibility(View.GONE);
        messagesButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);

        privacyConditionsLayout.setVisibility(View.GONE);
        dividerBeforePrivacyConditions.setVisibility(View.GONE);

        logoTextView.setVisibility(View.GONE);
        versionTextView.setVisibility(View.GONE);

        newEmailEditText.setVisibility(View.VISIBLE);
        noTonewEmailButton.setVisibility(View.VISIBLE);
        okTonewEmailButton.setVisibility(View.VISIBLE);

        newEmailEditText.requestFocus(); //TO Get focus on the first edittext programmatically!
        immProfile = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        if (immProfile != null) {
            immProfile.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
        }
    }
    /*
    CANCEL NEW EMAIL !!!
     */
    public void cancelNewEmail(View view) {
        immProfile.hideSoftInputFromWindow(newEmailEditText.getWindowToken(), 0); //TO Hide keyboard!
        immProfile = null;

        newEmailEditText.setText("");
        newEmailEditText.setVisibility(View.GONE);
        noTonewEmailButton.setVisibility(View.GONE);
        okTonewEmailButton.setVisibility(View.GONE);

        headingTextView.setText("Kullanıcı Bilgileri");
        avatarRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeUsername.setVisibility(View.VISIBLE);
        userNameImageView.setVisibility(View.VISIBLE);
        userNameTextView.setVisibility(View.VISIBLE);
        changeUserNameTextView.setVisibility(View.VISIBLE);

        userEmailImageView.setVisibility(View.VISIBLE);
        userEmailTextView.setVisibility(View.VISIBLE);
        changeUserEmailTextView.setVisibility(View.VISIBLE);

        userLocationImageView.setVisibility(View.VISIBLE);
        userLocationTextView.setVisibility(View.VISIBLE);
        changeUserLocationTextView.setVisibility(View.VISIBLE);

        userPasswordImageView.setVisibility(View.VISIBLE);
        userPasswordTextView.setVisibility(View.VISIBLE);
        changeUserPasswordTextView.setVisibility(View.VISIBLE);

        if (userGavePermission == 0) {
            userLocationPermissionImageView.setVisibility(View.VISIBLE);
            userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
        }

        dividerBeforeStore.setVisibility(View.VISIBLE);
        foodsRelativeLayout.setVisibility(View.VISIBLE);
        dividerBeforeRating.setVisibility(View.VISIBLE);
        reviewsRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeIssue.setVisibility(View.VISIBLE);
        reportRelativeLayout.setVisibility(View.VISIBLE);

        privacyConditionsLayout.setVisibility(View.VISIBLE);
        dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

        dividerBeforeExit.setVisibility(View.VISIBLE);
        messagesButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.VISIBLE);

        logoTextView.setVisibility(View.VISIBLE);
        versionTextView.setVisibility(View.VISIBLE);
    }
    /*
    SAVE NEW EMAIL !!!
     */
    public void saveNewEmail(View view) {
        //
        String newEmail;
        newEmail = newEmailEditText.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) { //VALIDATION of EMAIL
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen geçerli bir e-posta adresi girin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        } else {
            Dialog dialogEmail = ProgressDialog.show(ProfileActivity.this, "",
                    "Kullanıcı e-postanız güncelleniyor...", true);

            immProfile.hideSoftInputFromWindow(newEmailEditText.getWindowToken(), 0); //TO Hide keyboard!
            immProfile = null;

            newEmailEditText.setText("");
            newEmailEditText.setVisibility(View.GONE);
            noTonewEmailButton.setVisibility(View.GONE);
            okTonewEmailButton.setVisibility(View.GONE);

            headingTextView.setText("Kullanıcı Bilgileri");
            avatarRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeUsername.setVisibility(View.VISIBLE);
            userNameImageView.setVisibility(View.VISIBLE);
            userNameTextView.setVisibility(View.VISIBLE);
            changeUserNameTextView.setVisibility(View.VISIBLE);

            userEmailImageView.setVisibility(View.VISIBLE);
            userEmailTextView.setVisibility(View.VISIBLE);
            changeUserEmailTextView.setVisibility(View.VISIBLE);

            userLocationImageView.setVisibility(View.VISIBLE);
            userLocationTextView.setVisibility(View.VISIBLE);
            changeUserLocationTextView.setVisibility(View.VISIBLE);

            userPasswordImageView.setVisibility(View.VISIBLE);
            userPasswordTextView.setVisibility(View.VISIBLE);
            changeUserPasswordTextView.setVisibility(View.VISIBLE);

            if (userGavePermission == 0) {
                userLocationPermissionImageView.setVisibility(View.VISIBLE);
                userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
            }

            dividerBeforeStore.setVisibility(View.VISIBLE);
            foodsRelativeLayout.setVisibility(View.VISIBLE);
            dividerBeforeRating.setVisibility(View.VISIBLE);
            reviewsRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeIssue.setVisibility(View.VISIBLE);
            reportRelativeLayout.setVisibility(View.VISIBLE);

            privacyConditionsLayout.setVisibility(View.VISIBLE);
            dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

            dividerBeforeExit.setVisibility(View.VISIBLE);
            messagesButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);

            logoTextView.setVisibility(View.VISIBLE);
            versionTextView.setVisibility(View.VISIBLE);

            updateEmail(newEmail, dialogEmail);
        }
    }
    /*
    UPDATE EMAIL !!!
     */
    private void updateEmail(final String newEmail, final Dialog dialogEmail) {
        if (user != null) {
            user.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mUsersDatabaseEmailRef.setValue(newEmail);
                                //SEND A VERIFICATION EMAIL TO USER!!!
                                dialogEmail.cancel();
                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("E-posta adresiniz güncellendi!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            } else {
                                dialogEmail.cancel();
                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Güncelleme başarısız! Lütfen e-posta ve şifrenizle giriş yaparak tekrar deneyin!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(layout);
                                toast.show();
                            }
                        }
                    });
        } else {
            dialogEmail.cancel();
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen hesabınıza tekrar giriş yapın!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

    /*
    TO CHANGE LOCATION !!!
     */
    public void changeLocation(View view) {
        headingTextView.setText("Kullanıcı Konumu");
        avatarRelativeLayout.setVisibility(View.GONE);

        dividerBeforeUsername.setVisibility(View.INVISIBLE);
        userNameImageView.setVisibility(View.GONE);
        userNameTextView.setVisibility(View.GONE);
        changeUserNameTextView.setVisibility(View.GONE);

        userEmailImageView.setVisibility(View.GONE);
        userEmailTextView.setVisibility(View.GONE);
        changeUserEmailTextView.setVisibility(View.GONE);

        userLocationImageView.setVisibility(View.GONE);
        userLocationTextView.setVisibility(View.GONE);
        changeUserLocationTextView.setVisibility(View.GONE);

        userPasswordImageView.setVisibility(View.GONE);
        userPasswordTextView.setVisibility(View.GONE);
        changeUserPasswordTextView.setVisibility(View.GONE);

        userLocationPermissionImageView.setVisibility(View.GONE);
        userLocationPermissionSwitcher.setVisibility(View.GONE);

        dividerBeforeStore.setVisibility(View.GONE);
        foodsRelativeLayout.setVisibility(View.GONE);
        dividerBeforeRating.setVisibility(View.GONE);
        reviewsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeIssue.setVisibility(View.GONE);
        reportRelativeLayout.setVisibility(View.GONE);

        dividerBeforeExit.setVisibility(View.GONE);
        messagesButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);

        privacyConditionsLayout.setVisibility(View.GONE);
        dividerBeforePrivacyConditions.setVisibility(View.GONE);

        logoTextView.setVisibility(View.GONE);
        versionTextView.setVisibility(View.GONE);

        newLocationHeadingTextView.setVisibility(View.VISIBLE);
        newLocationLayout.setVisibility(View.VISIBLE);
        noTonewLocationButton.setVisibility(View.VISIBLE);
    }
    /*
    CANCEL NEW LOCATION !!!
     */
    public void cancelNewLocation(View view) {
        newLocationHeadingTextView.setVisibility(View.GONE);
        newLocationLayout.setVisibility(View.GONE);
        noTonewLocationButton.setVisibility(View.GONE);

        headingTextView.setText("Kullanıcı Bilgileri");
        avatarRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeUsername.setVisibility(View.VISIBLE);
        userNameImageView.setVisibility(View.VISIBLE);
        userNameTextView.setVisibility(View.VISIBLE);
        changeUserNameTextView.setVisibility(View.VISIBLE);

        userEmailImageView.setVisibility(View.VISIBLE);
        userEmailTextView.setVisibility(View.VISIBLE);
        changeUserEmailTextView.setVisibility(View.VISIBLE);

        userLocationImageView.setVisibility(View.VISIBLE);
        userLocationTextView.setVisibility(View.VISIBLE);
        changeUserLocationTextView.setVisibility(View.VISIBLE);

        userPasswordImageView.setVisibility(View.VISIBLE);
        userPasswordTextView.setVisibility(View.VISIBLE);
        changeUserPasswordTextView.setVisibility(View.VISIBLE);

        if (userGavePermission == 0) {
            userLocationPermissionImageView.setVisibility(View.VISIBLE);
            userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
        }

        dividerBeforeStore.setVisibility(View.VISIBLE);
        foodsRelativeLayout.setVisibility(View.VISIBLE);
        dividerBeforeRating.setVisibility(View.VISIBLE);
        reviewsRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeIssue.setVisibility(View.VISIBLE);
        reportRelativeLayout.setVisibility(View.VISIBLE);

        privacyConditionsLayout.setVisibility(View.VISIBLE);
        dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

        dividerBeforeExit.setVisibility(View.VISIBLE);
        messagesButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.VISIBLE);

        logoTextView.setVisibility(View.VISIBLE);
        versionTextView.setVisibility(View.VISIBLE);
    }

    /*
    TO CHANGE PASSWORD !!!
     */
    public void changePassword(View view) {
        headingTextView.setText("Kullanıcı Şifresi");
        avatarRelativeLayout.setVisibility(View.GONE);

        dividerBeforeUsername.setVisibility(View.INVISIBLE);
        userNameImageView.setVisibility(View.GONE);
        userNameTextView.setVisibility(View.GONE);
        changeUserNameTextView.setVisibility(View.GONE);

        userEmailImageView.setVisibility(View.GONE);
        userEmailTextView.setVisibility(View.GONE);
        changeUserEmailTextView.setVisibility(View.GONE);

        userLocationImageView.setVisibility(View.GONE);
        userLocationTextView.setVisibility(View.GONE);
        changeUserLocationTextView.setVisibility(View.GONE);

        userPasswordImageView.setVisibility(View.GONE);
        userPasswordTextView.setVisibility(View.GONE);
        changeUserPasswordTextView.setVisibility(View.GONE);

        userLocationPermissionImageView.setVisibility(View.GONE);
        userLocationPermissionSwitcher.setVisibility(View.GONE);

        dividerBeforeStore.setVisibility(View.GONE);
        foodsRelativeLayout.setVisibility(View.GONE);
        dividerBeforeRating.setVisibility(View.GONE);
        reviewsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeIssue.setVisibility(View.GONE);
        reportRelativeLayout.setVisibility(View.GONE);

        dividerBeforeExit.setVisibility(View.GONE);
        messagesButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);

        privacyConditionsLayout.setVisibility(View.GONE);
        dividerBeforePrivacyConditions.setVisibility(View.GONE);

        logoTextView.setVisibility(View.GONE);
        versionTextView.setVisibility(View.GONE);

        newPasswordEditText.setVisibility(View.VISIBLE);
        newPasswordAgainEditText.setVisibility(View.VISIBLE);
        noTonewPasswordButton.setVisibility(View.VISIBLE);
        okTonewPasswordButton.setVisibility(View.VISIBLE);
        
        newPasswordEditText.requestFocus(); //TO Get focus on the first edittext programmatically!
        immProfile = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        if (immProfile != null) {
            immProfile.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
        }
    }
    /*
    CANCEL NEW PASSWORD !!!
     */
    public void cancelNewPassword(View view) {
        immProfile.hideSoftInputFromWindow(newPasswordEditText.getWindowToken(), 0); //TO Hide keyboard!
        immProfile = null;

        newPasswordEditText.setText("");
        newPasswordAgainEditText.setText("");
        newPasswordEditText.setVisibility(View.GONE);
        newPasswordAgainEditText.setVisibility(View.GONE);
        noTonewPasswordButton.setVisibility(View.GONE);
        okTonewPasswordButton.setVisibility(View.GONE);

        headingTextView.setText("Kullanıcı Bilgileri");
        avatarRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeUsername.setVisibility(View.VISIBLE);
        userNameImageView.setVisibility(View.VISIBLE);
        userNameTextView.setVisibility(View.VISIBLE);
        changeUserNameTextView.setVisibility(View.VISIBLE);

        userEmailImageView.setVisibility(View.VISIBLE);
        userEmailTextView.setVisibility(View.VISIBLE);
        changeUserEmailTextView.setVisibility(View.VISIBLE);

        userLocationImageView.setVisibility(View.VISIBLE);
        userLocationTextView.setVisibility(View.VISIBLE);
        changeUserLocationTextView.setVisibility(View.VISIBLE);

        userPasswordImageView.setVisibility(View.VISIBLE);
        userPasswordTextView.setVisibility(View.VISIBLE);
        changeUserPasswordTextView.setVisibility(View.VISIBLE);

        if (userGavePermission == 0) {
            userLocationPermissionImageView.setVisibility(View.VISIBLE);
            userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
        }

        dividerBeforeStore.setVisibility(View.VISIBLE);
        foodsRelativeLayout.setVisibility(View.VISIBLE);
        dividerBeforeRating.setVisibility(View.VISIBLE);
        reviewsRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeIssue.setVisibility(View.VISIBLE);
        reportRelativeLayout.setVisibility(View.VISIBLE);

        privacyConditionsLayout.setVisibility(View.VISIBLE);
        dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

        dividerBeforeExit.setVisibility(View.VISIBLE);
        messagesButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.VISIBLE);

        logoTextView.setVisibility(View.VISIBLE);
        versionTextView.setVisibility(View.VISIBLE);
    }
    /*
    SAVE NEW PASSWORD !!!
     */
    public void saveNewPassword(View view) {
        String newPassword;
        String newPasswordAgain;
        newPassword = newPasswordEditText.getText().toString().trim();
        newPasswordAgain = newPasswordAgainEditText.getText().toString().trim();
        if (newPassword.length() < 6) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen en az 6 karakter uzunluğunda yeni şifrenizi girin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else if (!newPasswordAgain.equals(newPassword)) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen yeni şifrenizi tekrar girin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else {
            Dialog dialogPassword = ProgressDialog.show(ProfileActivity.this, "",
                    "Kullanıcı şifreniz güncelleniyor...", true);

            immProfile.hideSoftInputFromWindow(newPasswordEditText.getWindowToken(), 0); //TO Hide keyboard!
            immProfile = null;

            newPasswordEditText.setText("");
            newPasswordAgainEditText.setText("");
            newPasswordEditText.setVisibility(View.GONE);
            newPasswordAgainEditText.setVisibility(View.GONE);
            noTonewPasswordButton.setVisibility(View.GONE);
            okTonewPasswordButton.setVisibility(View.GONE);

            headingTextView.setText("Kullanıcı Bilgileri");
            avatarRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeUsername.setVisibility(View.VISIBLE);
            userNameImageView.setVisibility(View.VISIBLE);
            userNameTextView.setVisibility(View.VISIBLE);
            changeUserNameTextView.setVisibility(View.VISIBLE);

            userEmailImageView.setVisibility(View.VISIBLE);
            userEmailTextView.setVisibility(View.VISIBLE);
            changeUserEmailTextView.setVisibility(View.VISIBLE);

            userLocationImageView.setVisibility(View.VISIBLE);
            userLocationTextView.setVisibility(View.VISIBLE);
            changeUserLocationTextView.setVisibility(View.VISIBLE);

            userPasswordImageView.setVisibility(View.VISIBLE);
            userPasswordTextView.setVisibility(View.VISIBLE);
            changeUserPasswordTextView.setVisibility(View.VISIBLE);

            if (userGavePermission == 0) {
                userLocationPermissionImageView.setVisibility(View.VISIBLE);
                userLocationPermissionSwitcher.setVisibility(View.VISIBLE);
            }

            dividerBeforeStore.setVisibility(View.VISIBLE);
            foodsRelativeLayout.setVisibility(View.VISIBLE);
            dividerBeforeRating.setVisibility(View.VISIBLE);
            reviewsRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeIssue.setVisibility(View.VISIBLE);
            reportRelativeLayout.setVisibility(View.VISIBLE);

            privacyConditionsLayout.setVisibility(View.VISIBLE);
            dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

            dividerBeforeExit.setVisibility(View.VISIBLE);
            messagesButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);

            logoTextView.setVisibility(View.VISIBLE);
            versionTextView.setVisibility(View.VISIBLE);

            updatePassword(newPassword, dialogPassword);
        }
    }
    /*
    UPDATE USER PASSWORD !!!
     */
    private void updatePassword(String newPassword, final Dialog dialogPassword) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialogPassword.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Şifreniz güncellendi!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
                        } else {
                            dialogPassword.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Bir sorun oluştu! Lütfen tekrar deneyin.");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        }
                    }
                });
    }

    /*
    TO ASK FOR LOCATION PERMISSION
     */
    public void askForLocationPermission(View view) {
        //To ask for permission! (After a short delay to let user see why is permission for!)
        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,}, 1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Konum izni verildi!",
                    Toast.LENGTH_SHORT).show();

            userLocationPermissionImageView.setVisibility(View.GONE);
            userLocationPermissionSwitcher.setVisibility(View.GONE);
            userGavePermission = 1;
        } else {
            userLocationPermissionSwitcher.setChecked(false);

            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Yakındaki yemekleri görmek ve yemeğinizin haritada doğru yerde görünmesi için 'konum' izni verin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

            userGavePermission = 0;
        }
    }

    /*
    SEND USER TO REVIEWS !!!
     */
    public void gotoReviews(View view) {
        Intent myIntent = new Intent(ProfileActivity.this, ReviewActivity.class);
        myIntent.putExtra("Profile Uid", profileOwnerUid);
        ProfileActivity.this.startActivity(myIntent);
    }

    /*
    SEND USER TO CHAT LOBBY !!!
     */
    public void gotoChatLobby(View view) {
        Intent myIntent = new Intent(ProfileActivity.this, LobbyActivity.class);
        ProfileActivity.this.startActivity(myIntent);
    }

    /*
    SEND USER TO STORE!!!
     */
    public void gotoStoreFromProfile(View view) {
        String userLocation = userLocationTextView.getText().toString();
        //Log.e("Location: ", userLocation);
        if (userLocation.length() == 0) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen konum bilgilerinizi eksiksik girin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        } else {
            Intent myIntent = new Intent(ProfileActivity.this, StoreActivity.class);
            ProfileActivity.this.startActivity(myIntent);
        }
    }

    /*
    TO REPORT ISSUE
     */
    public void reportIssue(View view) {
        headingTextView.setText("Sorun Bildir");
        mProfileAdView.setVisibility(View.GONE);
        avatarRelativeLayout.setVisibility(View.GONE);

        dividerBeforeUsername.setVisibility(View.INVISIBLE);
        vitalsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeStore.setVisibility(View.GONE);
        foodsRelativeLayout.setVisibility(View.GONE);
        dividerBeforeRating.setVisibility(View.GONE);
        reviewsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeIssue.setVisibility(View.GONE);
        reportRelativeLayout.setVisibility(View.GONE);

        dividerBeforeExit.setVisibility(View.GONE);
        messagesButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);

        logoTextView.setVisibility(View.GONE);
        versionTextView.setVisibility(View.GONE);

        privacyConditionsLayout.setVisibility(View.GONE);
        dividerBeforePrivacyConditions.setVisibility(View.GONE);

        reportingRelativeLayout.setVisibility(View.VISIBLE);

        reportingEditText.requestFocus(); //TO Get focus on the first edittext programmatically!
        immProfile = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        if (immProfile != null) {
            immProfile.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
        }
    }
    /*
    TO REPORT PROFILE
     */
    public void reportProfile(View view) {
        headingTextView.setText("Şikayet Bildir");
        mProfileAdView.setVisibility(View.GONE);
        avatarRelativeLayout.setVisibility(View.GONE);

        dividerBeforeUsername.setVisibility(View.INVISIBLE);
        vitalsRelativeLayout.setVisibility(View.GONE);

        foodsRelativeLayout.setVisibility(View.GONE);
        dividerBeforeRating.setVisibility(View.GONE);
        reviewsRelativeLayout.setVisibility(View.GONE);

        dividerBeforeIssue.setVisibility(View.GONE);
        reportRelativeLayout.setVisibility(View.GONE);

        privacyConditionsLayout.setVisibility(View.GONE);
        dividerBeforePrivacyConditions.setVisibility(View.GONE);

        dividerBeforeExit.setVisibility(View.GONE);

        reportingRelativeLayout.setVisibility(View.VISIBLE);

        reportingEditText.requestFocus(); //TO Get focus on the first edittext programmatically!
        immProfile = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        if (immProfile != null) {
            immProfile.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
        }
    }
    /*
    TO CANCEL SENDING REPORT
     */
    public void profileCancelReport(View view) {
        immProfile.hideSoftInputFromWindow(reportingEditText.getWindowToken(), 0); //TO Hide keyboard!
        immProfile = null;

        reportingEditText.setText("");
        reportingRelativeLayout.setVisibility(View.GONE);

        headingTextView.setText("Kullanıcı Bilgileri");
        avatarRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeUsername.setVisibility(View.VISIBLE);
        vitalsRelativeLayout.setVisibility(View.VISIBLE);

        foodsRelativeLayout.setVisibility(View.VISIBLE);
        dividerBeforeRating.setVisibility(View.VISIBLE);
        reviewsRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeIssue.setVisibility(View.VISIBLE);
        reportRelativeLayout.setVisibility(View.VISIBLE);

        dividerBeforeExit.setVisibility(View.VISIBLE);
        mProfileAdView.setVisibility(View.VISIBLE);

        if (profileOwnerUid.equals(uid)) {
            privacyConditionsLayout.setVisibility(View.VISIBLE);
            dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

            messagesButton.setVisibility(View.VISIBLE);
            dividerBeforeStore.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);

            logoTextView.setVisibility(View.VISIBLE);
            versionTextView.setVisibility(View.VISIBLE);
        }
    }
    /*
    TO SEND REPORT
     */
    public void profileSendReport(View view) {
        String reporterName = user.getDisplayName();
        String reporterEmail = user.getEmail();
        String report = reportingEditText.getText().toString().trim();
        String reportDate = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
        FirebaseDatabase mReportDatabase = FirebaseDatabase.getInstance();

        if (!report.equals("")) {
            if (profileOwnerUid.equals(uid)) {
                reportingEditText.setText("");
                immProfile.hideSoftInputFromWindow(reportingEditText.getWindowToken(), 0); //TO Hide keyboard!
                immProfile = null;
                reportingRelativeLayout.setVisibility(View.GONE);
                headingTextView.setText("Kullanıcı Bilgileri");
                avatarRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeUsername.setVisibility(View.VISIBLE);
                vitalsRelativeLayout.setVisibility(View.VISIBLE);
                foodsRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeRating.setVisibility(View.VISIBLE);
                reviewsRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeIssue.setVisibility(View.VISIBLE);
                reportRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeExit.setVisibility(View.VISIBLE);
                messagesButton.setVisibility(View.VISIBLE);
                dividerBeforeStore.setVisibility(View.VISIBLE);
                privacyConditionsLayout.setVisibility(View.VISIBLE);
                dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.VISIBLE);
                logoTextView.setVisibility(View.VISIBLE);
                versionTextView.setVisibility(View.VISIBLE);

                DatabaseReference mReportIssueDatabaseRef = mReportDatabase.getReference().child("reportsDatabase/" + "issueReports/" + uid + "/");
                DatabaseReference mReportDatabaseReportRef = mReportIssueDatabaseRef.getRef().child("report");
                DatabaseReference mReportDatabaseReporterNameRef = mReportIssueDatabaseRef.getRef().child("reporterName");
                DatabaseReference mReportDatabaseReporterEmailRef = mReportIssueDatabaseRef.getRef().child("reporterEmail");
                DatabaseReference mReportDatabaseReportedDateRef = mReportIssueDatabaseRef.getRef().child("reportedDate");
                mReportDatabaseReportRef.setValue(report);
                mReportDatabaseReporterNameRef.setValue(reporterName);
                mReportDatabaseReporterEmailRef.setValue(reporterEmail);
                mReportDatabaseReportedDateRef.setValue(reportDate);
            } else {
                String profileOwnerLocation = userLocationTextView.getText().toString();
                reportingEditText.setText("");
                immProfile.hideSoftInputFromWindow(reportingEditText.getWindowToken(), 0); //TO Hide keyboard!
                immProfile = null;
                reportingRelativeLayout.setVisibility(View.GONE);
                headingTextView.setText("Kullanıcı Bilgileri");
                avatarRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeUsername.setVisibility(View.VISIBLE);
                vitalsRelativeLayout.setVisibility(View.VISIBLE);
                foodsRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeRating.setVisibility(View.VISIBLE);
                reviewsRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeIssue.setVisibility(View.VISIBLE);
                reportRelativeLayout.setVisibility(View.VISIBLE);
                dividerBeforeExit.setVisibility(View.VISIBLE);

                DatabaseReference mReportProfileDatabaseRef = mReportDatabase.getReference().child("reportsDatabase/" + "profileReports/" + uid + "/" + profileOwnerUid + "/");
                DatabaseReference mReportDatabaseReportRef = mReportProfileDatabaseRef.getRef().child("report");
                DatabaseReference mReportDatabaseReporterNameRef = mReportProfileDatabaseRef.getRef().child("reporterName");
                DatabaseReference mReportDatabaseReporterEmailRef = mReportProfileDatabaseRef.getRef().child("reporterEmail");
                DatabaseReference mReportDatabaseReportedDateRef = mReportProfileDatabaseRef.getRef().child("reportedDate");
                DatabaseReference mReportDatabaseLocationRef = mReportProfileDatabaseRef.getRef().child("profileLocation");
                mReportDatabaseReportRef.setValue(report);
                mReportDatabaseReporterNameRef.setValue(reporterName);
                mReportDatabaseReporterEmailRef.setValue(reporterEmail);
                mReportDatabaseReportedDateRef.setValue(reportDate);
                mReportDatabaseLocationRef.setValue(profileOwnerLocation);
            }
            mProfileAdView.setVisibility(View.VISIBLE);
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Talebiniz alındı! En kısa sürede değerlendirilerek tarafınıza bilgi verilecektir.\nTEŞEKKÜR EDERİZ!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        } else {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Şikayetinizi bir kaç cümleyle anlatın!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }

        if (profileOwnerUid.equals(uid)) {
            //???
        }
    }

    /*
    TO SIGN OUT !!!
     */
    public void signOut(View view) {

        if (user != null) {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            //builder1.setIcon(R.drawable.avatar);
            builder1.setTitle("Hesaptan çıkış yapmak istediğinize emin misiniz?");
            builder1.setMessage("\n(Mesaj alıp gönderemeyecek ve yemek ekleyemeyeceksiniz!)\n");
            builder1.setCancelable(true);
            builder1.setPositiveButton(Html.fromHtml("<font color='#D3D3D3'>Çıkış yap</font>"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            //IF USER IS SIGNED IN VIA GOOGLE!!!
                            if (acct != null) {
                                mGoogleSignInClient.signOut();
                                mGoogleSignInClient.revokeAccess();
                            }

                            mFirebaseAuth.signOut();
                            user = null;
                            uid = null;

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Hesaptan çıkış yapıldı!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();

                            dialog.cancel();
                            backToTheBlack();
                        }
                    });
            builder1.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }
    private void backToTheBlack() {
        Intent myIntent = new Intent(ProfileActivity.this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To kill next activity when user clicks the back button!!!
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //To kill next activity when user clicks the back button!!!
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //To kill next activity when user clicks the back button!!!
        ProfileActivity.this.startActivity(myIntent);
    }

    /*
    City/County/NbHood Spinner Selection!!!
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
        int id = parent.getId();
        switch (id) {
            case R.id.profile_city_spinner:
                profileCity = parent.getItemAtPosition(position).toString();
                setCountyLocation();
                break;
            case R.id.profile_county_spinner:
                profileCounty = parent.getItemAtPosition(position).toString();
                setNbhoodLocation();
                break;
            case R.id.profile_neighborhood_spinner:
                profileNeighborhood = parent.getItemAtPosition(position).toString();
                //To make sure the user selected all locayions (city,county,nbhood) accordingly.
                if (profileCity != null && profileCounty != null && profileNeighborhood != null && !profileCity.equals("İl seçin") && !profileCounty.equals("İlçe seçin") && !profileNeighborhood.equals("Mahalle seçin") && !profileNeighborhood.equals("Önce ilçe seçin")) {
                    updateUserLocation();
                } else {
                    //
                }
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
    /*
    To update user information according to the location selection!
     */
    private void updateUserLocation() {

        if (user != null) {
            //TO Update user location in firebase databse
            mLocationDatabaseCityRef.setValue(profileCity);
            mLocationDatabaseCountyRef.setValue(profileCounty);
            mLocationDatabaseNbhoodRef.setValue(profileNeighborhood);

            newLocationHeadingTextView.setVisibility(View.GONE);
            newLocationLayout.setVisibility(View.GONE);
            noTonewLocationButton.setVisibility(View.GONE);

            headingTextView.setText("Kullanıcı Bilgileri");
            avatarRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeUsername.setVisibility(View.VISIBLE);
            userNameImageView.setVisibility(View.VISIBLE);
            userNameTextView.setVisibility(View.VISIBLE);
            changeUserNameTextView.setVisibility(View.VISIBLE);

            userEmailImageView.setVisibility(View.VISIBLE);
            userEmailTextView.setVisibility(View.VISIBLE);
            changeUserEmailTextView.setVisibility(View.VISIBLE);

            userLocationImageView.setVisibility(View.VISIBLE);
            userLocationTextView.setVisibility(View.VISIBLE);
            changeUserLocationTextView.setVisibility(View.VISIBLE);

            userPasswordImageView.setVisibility(View.VISIBLE);
            userPasswordTextView.setVisibility(View.VISIBLE);
            changeUserPasswordTextView.setVisibility(View.VISIBLE);

            dividerBeforeStore.setVisibility(View.VISIBLE);
            foodsRelativeLayout.setVisibility(View.VISIBLE);
            dividerBeforeRating.setVisibility(View.VISIBLE);
            reviewsRelativeLayout.setVisibility(View.VISIBLE);

            privacyConditionsLayout.setVisibility(View.VISIBLE);
            dividerBeforePrivacyConditions.setVisibility(View.VISIBLE);

            dividerBeforeIssue.setVisibility(View.VISIBLE);
            reportRelativeLayout.setVisibility(View.VISIBLE);

            dividerBeforeExit.setVisibility(View.VISIBLE);
            messagesButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);

            logoTextView.setVisibility(View.VISIBLE);
            versionTextView.setVisibility(View.VISIBLE);
        } else {
            //Hesaptan çıkılmış, girişe yönlendir.
        }
        //TO RESET THE SPINNERS AFTER SELECTION!
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(ProfileActivity.this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileCitySpinner.setDropDownWidth(400);
        profileCitySpinner.setAdapter(cityAdapter);
    }
    /*
    County Spinner Setter (Opens profileCounty list according to the selected profileCity!)
     */
    private void setCountyLocation() {
        //County Location Setter
        profileCountySpinner = findViewById(R.id.profile_county_spinner);
        if (profileCity.equals("İl seçin")) {
            ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this,
                    R.array.county_array, android.R.layout.simple_spinner_item);
            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            profileCountySpinner.setDropDownWidth(400);
            profileCountySpinner.setAdapter(countyAdapter);
            profileCountySpinner.setOnItemSelectedListener(this);
        }
        else if (profileCity.equals("Adana")) {
            ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this,
                    R.array.adana_county_array, android.R.layout.simple_spinner_item);
            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            profileCountySpinner.setDropDownWidth(400);
            profileCountySpinner.setAdapter(countyAdapter);
            profileCountySpinner.setOnItemSelectedListener(this);
        }
        else if (profileCity.equals("Zonguldak")) {
            ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this,
                    R.array.zonguldak_county_array, android.R.layout.simple_spinner_item);
            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            profileCountySpinner.setDropDownWidth(400);
            profileCountySpinner.setAdapter(countyAdapter);
            profileCountySpinner.setOnItemSelectedListener(this);
        }
    }
    /*
    Neighborhood Spinner Setter (Opens neighborhood list according to the selected profileCity!)
     */
    private void setNbhoodLocation() {
        //Neighborhood Location Setter
        profileNbhoodSpinner = findViewById(R.id.profile_neighborhood_spinner);
        if (profileCounty.equals("İlçe seçin") || profileCounty.isEmpty()) {
            ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                    R.array.nbhood_array, android.R.layout.simple_spinner_item);
            setIt(nbhoodAdapter);
        }
        else if (profileCity.equals("Adana")) {
            if (profileCounty.equals("Aladağ")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_aladag_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Ceyhan")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_ceyhan_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Çukurova")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_cukurova_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Feke")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_feke_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("İmamoğlu")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_imamoglu_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Karaisalı")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_karaisali_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Karataş")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_karatas_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Kozan")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_kozan_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Pozantı")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_pozanti_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Saimbeyli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_saimbeyli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Sarıçam")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_saricam_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Seyhan")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_seyhan_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Tufanbeyli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_tufanbeyli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Yumurtalık")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_yumurtalik_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Yüreğir")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_yuregir_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
        }
        else if (profileCity.equals("Zonguldak")) {
            if (profileCounty.equals("Merkez")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_merkez_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Alaplı")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_alapli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Çaycuma")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_caycuma_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Devrek")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_devrek_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Ereğli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_eregli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Gökçebey")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_gokcebey_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Kilimli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_kilimli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (profileCounty.equals("Kozlu")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_kozlu_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
        }
    }
    private void setIt(ArrayAdapter nbhoodAdapter) {
        nbhoodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileNbhoodSpinner.setDropDownWidth(400);
        profileNbhoodSpinner.setAdapter(nbhoodAdapter);
        profileNbhoodSpinner.setOnItemSelectedListener(this);
    }

    /*
    TO GET USER TO THE TERMS&CONDITIONS AND PROVACY POLICY!
     */
    public void toConditions(View view) {
        Intent myIntent = new Intent(ProfileActivity.this, ConditionsActivity.class);
        ProfileActivity.this.startActivity(myIntent);
    }
    public void toPrivacy(View view) {
        Intent myIntent = new Intent(ProfileActivity.this, PrivacyActivity.class);
        ProfileActivity.this.startActivity(myIntent);
    }
}
