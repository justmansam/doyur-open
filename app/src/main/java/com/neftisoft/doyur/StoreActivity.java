package com.neftisoft.doyur;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class StoreActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 22222;
    private static final int REQUEST_TAKE_PHOTO = 11111;

    public int storeImageNumber = 0; //To control which image is the subject of change! (0 for none, 1 for main store image, 2-3-4-5 are for additional store images respectively)

    private ValueEventListener mLocationChildEventListener;

    private FusedLocationProviderClient fusedStoreLocationClient;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase mUsersDatabase;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseStoreRef;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseRef;
    private DatabaseReference mUsersDatabaseRatingRef;
    private DatabaseReference mUsersDatabaseRatingTotalPointRef;
    private DatabaseReference mUsersDatabaseRatingTotalPersonRef;
    private DatabaseReference mUsersDatabaseLocationRef;
    private DatabaseReference mUsersDatabaseLocationCityRef;
    private DatabaseReference mUsersDatabaseLocationCountyRef;
    private DatabaseReference mUsersDatabaseLocationNbhoodRef;

    private DatabaseReference mUsersDatabaseStoreMainImageRef;
    private DatabaseReference mUsersDatabaseStoreAddImage2Ref;
    private DatabaseReference mUsersDatabaseStoreAddImage3Ref;
    private DatabaseReference mUsersDatabaseStoreAddImage4Ref;
    private DatabaseReference mUsersDatabaseStoreAddImage5Ref;
    private DatabaseReference mUsersDatabaseStoreInfoRef;
    private DatabaseReference mUsersDatabaseStoreNameRef;
    private DatabaseReference mUsersDatabaseStoreOwnerAvatarRef;
    private DatabaseReference mUsersDatabaseStoreOwnerNameRef;

    private FirebaseStorage mFoodsStoreStorage;
    private StorageReference mFoodsStoreStorageRef;
    private FirebaseDatabase mFoodsStoreDatabase;
    private DatabaseReference mFoodsStoreDatabaseRef;
    private DatabaseReference mMapStoreDatabaseRef;
    private DatabaseReference mMapStoreDatabaseRefToDelete;

    private String username;
    private String email;
    private String uid;
    private String otherUid;
    private String storeOwnerName;
    private String ratingTotalPoint;
    private String ratingTotalPerson;
    private String storeOwnerUid;

    private String initialStoreLatStr;
    private String initialStoreLonStr;

    private String temporaryStoreImageUri;
    private String mainStoreImageUri;
    private String additionalStoreImageUri2;
    private String additionalStoreImageUri3;
    private String additionalStoreImageUri4;
    private String additionalStoreImageUri5;

    private String mainStoreImageString;
    private String storeInformationString;
    private String storeNameString;
    private String additionalStoreImage2String;
    private String additionalStoreImage3String;
    private String additionalStoreImage4String;
    private String additionalStoreImage5String;

    private String refCity;
    private String refCounty;
    private String refNeighborhood;

    private String imageFileName;
    private String currentPhotoPath;

    private float ratingTotalPointFloat;
    private float ratingTotalPersonFloat;

    private Dialog dialogStore;

    TemplateView storeNativeAdTemplate;

    ImageView backButtonImageView;
    ImageView mainStoreImageView;
    ImageView additionalStoreImageView2;
    ImageView additionalStoreImageView3;
    ImageView additionalStoreImageView4;
    ImageView additionalStoreImageView5;
    ImageView storeOwnerAvatarImageView;

    ImageView editMainStoreImageButton;
    ImageView editStoreNameButton;
    ImageView editStoreInfoButton;
    ImageView addMainStoreImageImageView;

    TextView addMainStoreImageTextView;
    TextView storeNameEditText;
    TextView storeInfoEditText;
    TextView storeNameTextView;
    TextView storeInfoTextView;
    TextView storeNameCaptionTextView;
    TextView storeInfoCaptionTextView;
    TextView storeAddImagesCaptionTextView;
    TextView storeOwnerNameTextView;
    TextView storeOwnerRatingTextView;
    TextView storeOwnerRateCountTextView;

    Button deleteStoreButton;
    Button cancelStoreButton;
    Button saveStoreButton;
    Button cancelEditingStoreButton;
    Button saveEdittedStoreButton;
    FloatingActionButton fabGiveAnOrder;

    LinearLayout storeNameLL;
    LinearLayout storeInfoLL;
    LinearLayout storeAddImagesLL;
    LinearLayout storeOwnerLL;

    View dividerBeforeStoreInfo;
    View dividerAfterStoreInfo;
    View dividerBeforeStoreOwner;
    View dividerAfterStoreOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // To change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = StoreActivity.this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(StoreActivity.this,R.color.colorNavigationBar));
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //TO Stop OnScreen Keyboard Auto Popup!

        // User comes from PartyActivity (by Clicking on any Store as a Guest)
        Intent partyStoreIntent = getIntent();
        otherUid = partyStoreIntent.getStringExtra("Store Other Uid");

        // To access user information!!
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            username = user.getDisplayName();
            email = user.getEmail();

            // Check if user's email is verified
            //boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = user.getUid();
        }

        if (otherUid != null) {
            if (otherUid.equals(uid)) {
                // I am the owner
                storeOwnerUid = uid;
            } else {
                // The owner is someone else
                storeOwnerUid = otherUid;
            }
        } else {
            // It seems I am the owner
            storeOwnerUid = uid;
        }

        //STORE DATABASE & STORAGE
        mFoodsStoreStorage = FirebaseStorage.getInstance();
        mFoodsStoreStorageRef = mFoodsStoreStorage.getReference().child("foodStorePhotoStorage/" + uid + "/");   // + a spesific range of the current location (City or town)

        mUsersDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mUsersDatabaseStoreRef = mUsersDatabase.getReference().child("usersDatabase/" + storeOwnerUid + "/" + "storeFoodsDatabase/");  //To save and update user's store information
        mUsersDatabaseRef = mUsersDatabase.getReference().child("usersDatabase/" + storeOwnerUid + "/" + "vitalsDatabase/");
        mUsersDatabaseRatingRef = mUsersDatabase.getReference().child("usersDatabase/" + storeOwnerUid + "/" + "ratingReviewDatabase/");

        mUsersDatabaseLocationRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "locationDatabase/");
        mUsersDatabaseLocationCityRef = mUsersDatabaseLocationRef.getRef().child("city");
        mUsersDatabaseLocationCountyRef = mUsersDatabaseLocationRef.getRef().child("county");
        mUsersDatabaseLocationNbhoodRef = mUsersDatabaseLocationRef.getRef().child("nbhood");

        //TO INITIALIZE ADMOB ADS!!!
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //TO SHOW AD!
        if (!storeOwnerUid.equals(uid)) {
            storeNativeAdTemplate = findViewById(R.id.store_native_ad_template);
            AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-1548831853802422/7009498735")
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            storeNativeAdTemplate.setNativeAd(nativeAd);
                            storeNativeAdTemplate.setVisibility(View.VISIBLE);

                            if (isDestroyed()) {
                                nativeAd.destroy();
                            }

                        }
                    })
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }

        /*
        TO TAKE THE USER LOCATION !!!
        */
        mUsersDatabaseLocationCityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                try {
                    if (dataSnapshot1.getValue() != null) {
                        try {
                            //Log.e("TAG", "" + dataSnapshot1.getValue());
                            String city;
                            city = dataSnapshot1.getValue().toString();
                            refCity = city.replace("ç","c").replace("Ç","C")
                                    .replace("ğ","g").replace("Ğ","G")
                                    .replace("ı","i").replace("İ","I")
                                    .replace("ö","o").replace("Ö","O")
                                    .replace("ü","u").replace("Ü","U")
                                    .replace("ş","s").replace("Ş","S").replace(" ","");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " city is null.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mUsersDatabaseLocationCountyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                try {
                    if (dataSnapshot2.getValue() != null) {
                        try {
                            //Log.e("TAG", "" + dataSnapshot2.getValue());
                            String county;
                            county = dataSnapshot2.getValue().toString();
                            refCounty = county.replace("ç","c").replace("Ç","C")
                                    .replace("ğ","g").replace("Ğ","G")
                                    .replace("ı","i").replace("İ","I")
                                    .replace("ö","o").replace("Ö","O")
                                    .replace("ü","u").replace("Ü","U")
                                    .replace("ş","s").replace("Ş","S").replace(" ","");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " county is null.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mUsersDatabaseLocationNbhoodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                try {
                    if (dataSnapshot3.getValue() != null) {
                        try {
                            //Log.e("TAG", "" + dataSnapshot3.getValue());
                            String neighborhood;
                            neighborhood = dataSnapshot3.getValue().toString();
                            refNeighborhood = neighborhood.replace("ç","c").replace("Ç","C")
                                    .replace("ğ","g").replace("Ğ","G")
                                    .replace("ı","i").replace("İ","I")
                                    .replace("ö","o").replace("Ö","O")
                                    .replace("ü","u").replace("Ü","U")
                                    .replace("ş","s").replace("Ş","S").replace(" ","");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " neigborhood is null.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //IMAGEVIEWS, TEXTVIEWS and BUTTONS
        backButtonImageView = findViewById(R.id.back_button_imageView);
        addMainStoreImageImageView = findViewById(R.id.add_main_store_image_imageview);
        mainStoreImageView = findViewById(R.id.main_store_imageview);
        additionalStoreImageView2 = findViewById(R.id.additional_store_image_2);
        additionalStoreImageView3 = findViewById(R.id.additional_store_image_3);
        additionalStoreImageView4 = findViewById(R.id.additional_store_image_4);
        additionalStoreImageView5 = findViewById(R.id.additional_store_image_5);
        storeOwnerAvatarImageView = findViewById(R.id.store_avatar_imageview);

        editMainStoreImageButton = findViewById(R.id.edit_main_image_button);
        editStoreNameButton = findViewById(R.id.edit_store_name_button);
        editStoreInfoButton = findViewById(R.id.edit_store_info_button);

        addMainStoreImageTextView =findViewById(R.id.add_main_store_image_textview);
        storeNameEditText = findViewById(R.id.store_name_edittext);
        storeInfoEditText = findViewById(R.id.store_information_edittext);
        storeNameTextView = findViewById(R.id.store_name_textview);
        storeInfoTextView = findViewById(R.id.store_information_textview);
        storeNameCaptionTextView = findViewById(R.id.store_name_caption);
        storeInfoCaptionTextView = findViewById(R.id.store_information_caption);
        storeAddImagesCaptionTextView = findViewById(R.id.store_additional_images_caption);
        storeOwnerNameTextView = findViewById(R.id.store_ownername_textview);
        storeOwnerRatingTextView = findViewById(R.id.store_rating_textview);
        storeOwnerRateCountTextView = findViewById(R.id.store_rate_count_textview);

        deleteStoreButton = findViewById(R.id.delete_store_button);
        cancelStoreButton = findViewById(R.id.cancel_store_button);
        saveStoreButton = findViewById(R.id.save_store_button);
        cancelEditingStoreButton = findViewById(R.id.cancel_editing_store_button);
        saveEdittedStoreButton = findViewById(R.id.save_editted_store_button);
        fabGiveAnOrder = findViewById(R.id.fab_give_order);

        storeNameLL = findViewById(R.id.store_name_linear_layout);
        storeInfoLL = findViewById(R.id.store_information_linear_layout);
        storeAddImagesLL = findViewById(R.id.store_additional_images_linear_layout);
        storeOwnerLL = findViewById(R.id.store_owner_linear_layout);

        dividerBeforeStoreInfo = findViewById(R.id.divider_before_store_information);
        dividerAfterStoreInfo = findViewById(R.id.divider_after_store_information);
        dividerBeforeStoreOwner = findViewById(R.id.divider_before_store_owner);
        dividerAfterStoreOwner = findViewById(R.id.divider_after_store_owner);

        setStoreImagesAndInfo();
    }

    /*
    TO GET BACK !!!
     */
    public void getBack(View view) {
        onBackPressed();
    }

    /*
    TO SET ALREADY UPLOADED IMAGES & INFORMATION OF THE STORE
     */
    @SuppressLint("RestrictedApi")
    private void setStoreImagesAndInfo() {

        if (!storeOwnerUid.equals(uid)) {
            storeNameLL.setBackground(null);
            storeInfoLL.setBackground(null);
            storeAddImagesLL.setBackground(null);
            addMainStoreImageImageView.setVisibility(View.GONE);
            addMainStoreImageTextView.setVisibility(View.GONE);
            storeNameCaptionTextView.setVisibility(View.GONE);
            storeInfoCaptionTextView.setVisibility(View.GONE);
            storeInfoEditText.setVisibility(View.GONE);
            storeNameEditText.setVisibility(View.GONE);
            storeAddImagesCaptionTextView.setVisibility(View.GONE);
            deleteStoreButton.setVisibility(View.GONE);
            cancelStoreButton.setVisibility(View.GONE);
            saveStoreButton.setVisibility(View.GONE);
            backButtonImageView.setVisibility(View.VISIBLE);
            fabGiveAnOrder.setVisibility(View.VISIBLE); //@SuppressLint("RestrictedApi") is for THIS !!
            storeOwnerLL.setVisibility(View.VISIBLE);
            dividerBeforeStoreInfo.setVisibility(View.VISIBLE);
            dividerBeforeStoreOwner.setVisibility(View.VISIBLE);
            dividerAfterStoreOwner.setVisibility(View.VISIBLE);
            //mainStoreImageView.setClickable(false); //Canceled to let user see full image!
            additionalStoreImageView2.setVisibility(View.GONE);
        }

        mUsersDatabaseStoreMainImageRef = mUsersDatabaseStoreRef.getRef().child("mainStoreImageUri/");
        mUsersDatabaseStoreAddImage2Ref = mUsersDatabaseStoreRef.getRef().child("additionalStoreImageUri2/");
        mUsersDatabaseStoreAddImage3Ref = mUsersDatabaseStoreRef.getRef().child("additionalStoreImageUri3/");
        mUsersDatabaseStoreAddImage4Ref = mUsersDatabaseStoreRef.getRef().child("additionalStoreImageUri4/");
        mUsersDatabaseStoreAddImage5Ref = mUsersDatabaseStoreRef.getRef().child("additionalStoreImageUri5/");
        mUsersDatabaseStoreInfoRef = mUsersDatabaseStoreRef.getRef().child("storeInfo/");
        mUsersDatabaseStoreNameRef = mUsersDatabaseStoreRef.getRef().child("storeName/");
        mUsersDatabaseStoreOwnerAvatarRef = mUsersDatabaseRef.getRef().child("avatar/");
        mUsersDatabaseStoreOwnerNameRef = mUsersDatabaseRef.getRef().child("username/");
        mUsersDatabaseRatingTotalPointRef = mUsersDatabaseRatingRef.getRef().child("totalPoint/");
        mUsersDatabaseRatingTotalPersonRef = mUsersDatabaseRatingRef.getRef().child("totalPerson/");

        //TO Load MAIN IMAGE of the Store !!!
        mUsersDatabaseStoreMainImageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataImage1Snapshot) {
                try {
                    if (dataImage1Snapshot.getValue() != null) {
                        try {
                            mainStoreImageString = dataImage1Snapshot.getValue().toString();
                            Uri mainStoreImageUrl = Uri.parse(mainStoreImageString);
                            Glide.with(mainStoreImageView.getContext())
                                    .load(mainStoreImageUrl)
                                    .centerCrop()
                                    .into(mainStoreImageView);
                            if (storeOwnerUid.equals(uid)) { //TO Check if Store belongs to user or not!!!
                                addMainStoreImageImageView.setVisibility(View.GONE);
                                addMainStoreImageTextView.setVisibility(View.GONE);
                                backButtonImageView.setVisibility(View.VISIBLE);
                                editMainStoreImageButton.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Photo 1 is null.");
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
        //TO Load ADDITIONAL IMAGE (2) of the store !!!
        mUsersDatabaseStoreAddImage2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataImage2Snapshot) {
                try {
                    if (dataImage2Snapshot.getValue() != null) {
                        try {
                            additionalStoreImageView2.setVisibility(View.VISIBLE);
                            dividerAfterStoreInfo.setVisibility(View.VISIBLE);
                            additionalStoreImage2String = dataImage2Snapshot.getValue().toString();
                            Uri additionalStoreImage2Url = Uri.parse(additionalStoreImage2String);
                            //TO Widen image size
                            additionalStoreImageView2.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                            additionalStoreImageView2.getLayoutParams().height = 500;
                            additionalStoreImageView2.setPadding(0,0,0,0);
                            Glide.with(additionalStoreImageView2.getContext())
                                    .load(additionalStoreImage2Url)
                                    .centerCrop()
                                    .into(additionalStoreImageView2);
                            if (storeOwnerUid.equals(uid)) { //TO Check if Store belongs to user or not!!!
                                additionalStoreImageView3.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Photo 2 is null.");
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
        //TO Load ADDITIONAL IMAGE (3) of the store !!!
        mUsersDatabaseStoreAddImage3Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataImage3Snapshot) {
                try {
                    if (dataImage3Snapshot.getValue() != null) {
                        try {
                            additionalStoreImageView3.setVisibility(View.VISIBLE);
                            additionalStoreImage3String = dataImage3Snapshot.getValue().toString();
                            Uri additionalStoreImage3Url = Uri.parse(additionalStoreImage3String);
                            //TO Widen image size
                            additionalStoreImageView3.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                            additionalStoreImageView3.getLayoutParams().height = 500;
                            additionalStoreImageView3.setPadding(0,0,0,0);
                            Glide.with(additionalStoreImageView3.getContext())
                                    .load(additionalStoreImage3Url)
                                    .centerCrop()
                                    .into(additionalStoreImageView3);
                            if (storeOwnerUid.equals(uid)) { //TO Check if Store belongs to user or not!!!
                                additionalStoreImageView4.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Photo 3 is null.");
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
        //TO Load ADDITIONAL IMAGE (4) of the store !!!
        mUsersDatabaseStoreAddImage4Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataImage4Snapshot) {
                try {
                    if (dataImage4Snapshot.getValue() != null) {
                        try {
                            additionalStoreImageView4.setVisibility(View.VISIBLE);
                            additionalStoreImage4String = dataImage4Snapshot.getValue().toString();
                            Uri additionalStoreImage4Url = Uri.parse(additionalStoreImage4String);
                            //TO Widen image size
                            additionalStoreImageView4.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                            additionalStoreImageView4.getLayoutParams().height = 500;
                            additionalStoreImageView4.setPadding(0,0,0,0);
                            Glide.with(additionalStoreImageView4.getContext())
                                    .load(additionalStoreImage4Url)
                                    .centerCrop()
                                    .into(additionalStoreImageView4);
                            if (storeOwnerUid.equals(uid)) { //TO Check if Store belongs to user or not!!!
                                additionalStoreImageView5.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Photo 4 is null.");
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
        //TO Load ADDITIONAL IMAGE (5) of the store !!!
        mUsersDatabaseStoreAddImage5Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataImage5Snapshot) {
                try {
                    if (dataImage5Snapshot.getValue() != null) {
                        try {
                            additionalStoreImageView5.setVisibility(View.VISIBLE);
                            additionalStoreImage5String = dataImage5Snapshot.getValue().toString();
                            Uri additionalStoreImage5Url = Uri.parse(additionalStoreImage5String);
                            //TO Widen image size
                            additionalStoreImageView5.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                            additionalStoreImageView5.requestLayout();
                            additionalStoreImageView5.getLayoutParams().height = 500;
                            additionalStoreImageView5.requestLayout();
                            additionalStoreImageView5.setPadding(0,0,0,0);
                            Glide.with(additionalStoreImageView5.getContext())
                                    .load(additionalStoreImage5Url)
                                    .centerCrop()
                                    .into(additionalStoreImageView5);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Photo 5 is null.");
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
        //TO Load INFORMATION about the store !!!
        mUsersDatabaseStoreInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataInfoSnapshot) {
                try {
                    if (dataInfoSnapshot.getValue() != null) {
                        try {
                            storeInfoEditText.setVisibility(View.GONE);
                            storeInfoTextView.setVisibility(View.VISIBLE);
                            if (storeOwnerUid.equals(uid)) { //TO Check if Store belongs to user or not!!!
                                editStoreInfoButton.setVisibility(View.VISIBLE);
                            }
                            storeInformationString = dataInfoSnapshot.getValue().toString();
                            storeInfoTextView.setText(storeInformationString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Information is null.");
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
        //TO Load NAME of the store !!! ()
        mUsersDatabaseStoreNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataNameSnapshot) {
                try {
                    if (dataNameSnapshot.getValue() != null) {
                        try {
                            storeNameEditText.setVisibility(View.GONE);
                            storeNameTextView.setVisibility(View.VISIBLE);
                            if (storeOwnerUid.equals(uid)) { //TO Check if Store belongs to user or not!!!
                                editStoreNameButton.setVisibility(View.VISIBLE);
                                deleteStoreButton.setVisibility(View.VISIBLE);
                            }
                            storeNameString = dataNameSnapshot.getValue().toString();
                            storeNameTextView.setText(storeNameString);

                            //TO GET RID OF THE BACKGROUNDS
                            storeNameLL.setBackground(null);
                            storeInfoLL.setBackground(null);
                            storeAddImagesLL.setBackground(null);
                            storeNameCaptionTextView.setVisibility(View.GONE);
                            storeInfoCaptionTextView.setVisibility(View.GONE);
                            storeAddImagesCaptionTextView.setVisibility(View.GONE);
                            cancelStoreButton.setVisibility(View.GONE);
                            saveStoreButton.setVisibility(View.GONE);
                            dividerBeforeStoreInfo.setVisibility(View.VISIBLE);
                            dividerBeforeStoreOwner.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Name is null.");
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
        //TO Load AVATAR of the store owner !!!
        mUsersDatabaseStoreOwnerAvatarRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataAvatarSnapshot) {
                try {
                    if (dataAvatarSnapshot.getValue() != null) {
                        try {
                            if (!storeOwnerUid.equals(uid)) {
                                String storeOwnerAvatarString = dataAvatarSnapshot.getValue().toString();
                                Uri storeOwnerAvatarUrl = Uri.parse(storeOwnerAvatarString);
                                Glide.with(storeOwnerAvatarImageView.getContext())
                                        .load(storeOwnerAvatarUrl)
                                        .centerCrop()
                                        .into(storeOwnerAvatarImageView);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Photo 1 is null.");
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
        //TO Load NAME of the store owner !!!
        mUsersDatabaseStoreOwnerNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataStoreOwnerNameSnapshot) {
                try {
                    if (dataStoreOwnerNameSnapshot.getValue() != null) {
                        try {
                            if (!storeOwnerUid.equals(uid)) {
                                storeOwnerName = dataStoreOwnerNameSnapshot.getValue().toString();
                                storeOwnerNameTextView.setText(storeOwnerName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Name is null.");
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
        //TO Load RATING (TOTAL POINT) of the store owner !!!
        mUsersDatabaseRatingTotalPointRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingTotPointSnapshot) {
                try {
                    if (ratingTotPointSnapshot.getValue() != null) {
                        try {
                            if (!storeOwnerUid.equals(uid)) {
                                ratingTotalPoint = ratingTotPointSnapshot.getValue().toString();
                                ratingTotalPointFloat = Float.parseFloat(ratingTotalPoint);
                                calculateRating();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Name is null.");
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
        //TO Load RATING (TOTAL PERSON) of the store owner !!!
        mUsersDatabaseRatingTotalPersonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingTotPersonSnapshot) {
                try {
                    if (ratingTotPersonSnapshot.getValue() != null) {
                        try {
                            if (!storeOwnerUid.equals(uid)) {
                                ratingTotalPerson = ratingTotPersonSnapshot.getValue().toString();
                                ratingTotalPersonFloat = Float.parseFloat(ratingTotalPerson);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Log.e("TAG", " Name is null.");
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

        /*
        TO DELETE ADDITIONAL PHOTOS !!! (İLERİDE DÜZENLENİP UYGULANABİLİR! BU HALİYLE KOD ÇALIŞIYOR..)
         */
        /*
        additionalStoreImageView2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mFoodsStoreDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference mFoodsStoreAdd2DatabaseRef = mFoodsStoreDatabase.getReference().child("foodStorePhotoDatabase/" + refCity + "/" + refCounty + "/" + refNeighborhood + "/" + uid + "/additionalStoreImageUri2");
                if (storeOwnerUid.equals(uid)) {
                    AlertDialog.Builder builderStoreDel = new AlertDialog.Builder(StoreActivity.this);
                    builderStoreDel.setTitle("Fotoğrafı sil");
                    builderStoreDel.setMessage("\nBu fotoğrafı kaldırmak istediğinize emin misiniz?\n");
                    builderStoreDel.setCancelable(true);

                    builderStoreDel.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //TO Delete photo from user and store Databases !!!
                                    mUsersDatabaseStoreAddImage2Ref.removeValue();
                                    mFoodsStoreAdd2DatabaseRef.removeValue();

                                    //DON'T FORGET TO UPDATE STORE IMMEDIATELY !!!


                                    Toast.makeText(StoreActivity.this, "Fotoğraf silindi!",
                                            Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                            });
                    builderStoreDel.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertStoreDel = builderStoreDel.create();
                    alertStoreDel.show();
                }
                return true;
            }
        });
        */
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
                    storeOwnerRatingTextView.setText(finalRatingString);
                    storeOwnerRateCountTextView.setText("(" + ratingTotalPerson + " oy)");
                    if (calculatedRating >= 9) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#1B5E20"));
                    }
                    else if (9 > calculatedRating && calculatedRating >= 8) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#66BB6A"));
                    }
                    else if (8 > calculatedRating && calculatedRating >= 7) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#D4E157"));
                    }
                    else if (7 > calculatedRating && calculatedRating >= 6) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#FFEE58"));
                    }
                    else if (6 > calculatedRating && calculatedRating >= 5) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#FFA726"));
                    }
                    else if (5 > calculatedRating && calculatedRating >= 4) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#FF7043"));
                    }
                    else if (4 > calculatedRating && calculatedRating > 0) {
                        storeOwnerRatingTextView.setTextColor(Color.parseColor("#BF360C"));
                    }
                } else {
                    storeOwnerRatingTextView.setText("0.0");
                    storeOwnerRateCountTextView.setText("(Henüz oy verilmemiş!)");
                }
            }
        }, 500);
    }

    /*
    TO ADD or CHANGE MAIN STORE IMAGE OR TO SEE IMAGE IN FULLSCREEN !!!
     */
    public void addMainStoreImage(View view) {
        if (!storeOwnerUid.equals(uid)) {
            //TO SEE IMAGE FULLSCREEN
            Intent myIntent = new Intent(StoreActivity.this, FullscreenImageActivity.class);
            myIntent.putExtra("Image Uri", mainStoreImageString);
            StoreActivity.this.startActivity(myIntent);
        } else {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            builder3.setTitle("Mutfak resmi ekle...");
            builder3.setMessage("\n");
            builder3.setCancelable(true);
            builder3.setPositiveButton(
                    "Fotoğraf çek",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                                storeImageNumber = 1;
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile(); //Take image file name for the avatar.
                                } catch (IOException ex) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(StoreActivity.this,
                                            "com.neftisoft.android.fileprovider",
                                            photoFile);
                                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            }
                            dialog.cancel();
                        }
                    });
            builder3.setNegativeButton(
                    "Galeriden seç",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            storeImageNumber = 1;
                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            pictureIntent.setType("image/jpeg");
                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                            dialog.cancel();
                        }
                    });
            builder3.setNeutralButton("İptal",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert123 = builder3.create();
            alert123.show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent pictureIntent) { //ON ACTIVITY RESULT ACTION
        super.onActivityResult(requestCode, resultCode, pictureIntent);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (deleteStoreButton.getVisibility() == View.VISIBLE) {
                deleteStoreButton.setVisibility(View.GONE);
                cancelEditingStoreButton.setVisibility(View.VISIBLE);
                saveEdittedStoreButton.setVisibility(View.VISIBLE);
            }
            addPhotoShoot();
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            if (deleteStoreButton.getVisibility() == View.VISIBLE) {
                deleteStoreButton.setVisibility(View.GONE);
                cancelEditingStoreButton.setVisibility(View.VISIBLE);
                saveEdittedStoreButton.setVisibility(View.VISIBLE);
            }
            Uri selectedImageUri = pictureIntent.getData();
            addPhotoPicked(selectedImageUri);
        }
    }
    /*
    TO CREATE IMAGE FILE
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        imageFileName = storeImageNumber + "_" + storeOwnerUid + "_";
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

        dialogStore = ProgressDialog.show(StoreActivity.this, "",
                "Mutfak resminiz güncelleniyor...\nLütfen bekleyin!", true);

        final Uri file = Uri.fromFile(new File(currentPhotoPath));
        final StorageReference mstoreImageStorageRef = mFoodsStoreStorageRef.child(imageFileName);

        mstoreImageStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        getDownloadUrl(mstoreImageStorageRef, file);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialogStore.cancel();

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
    TO GET DOWNLOAD URL
     */
    private void getDownloadUrl(final StorageReference mStoreImageStorageRef, Uri file) {

        UploadTask uploadTask = mStoreImageStorageRef.putFile(file);

        final Task<Uri> photoUrl = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                final StorageReference[] mStorePhotoStorageDownloadRef = {null};
                final Task<Uri>[] downloadStorePhotoUri = new Task[1];
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
                    String mStorePhotoStorageUploadChildRefString = mStoreImageStorageRef.getName();
                    mStorePhotoStorageUploadChildRefString = mStorePhotoStorageUploadChildRefString + "_1024x1024";
                    StorageReference mStorePhotoStorageUploadRootRef = mStoreImageStorageRef.getParent();
                    Thread.sleep(2100); //WAIT FOR THE FIREBASE FUNCTION TO RESIZE THE IMAGE!!!
                    mStorePhotoStorageDownloadRef[0] = mStorePhotoStorageUploadRootRef.child(mStorePhotoStorageUploadChildRefString);
                    downloadStorePhotoUri[0] = mStorePhotoStorageDownloadRef[0].getDownloadUrl();
                    return downloadStorePhotoUri[0];
                }
                // Continue with the task to get the download URL
                //return mstoreImageStorageRef.getDownloadUrl(); //OLD ONE
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadStoreImageUri = task.getResult();

                    try {
                        getUrlandSetStoreImage(downloadStoreImageUri);
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
    private void getUrlandSetStoreImage(Uri downloadStoreImageUri) throws FileNotFoundException {
        temporaryStoreImageUri = downloadStoreImageUri.toString();
        if (storeImageNumber == 1) {
            Glide.with(mainStoreImageView.getContext())
                    .load(downloadStoreImageUri)
                    .centerCrop()
                    .into(mainStoreImageView);
            addMainStoreImageImageView.setVisibility(View.GONE);
            addMainStoreImageTextView.setVisibility(View.GONE);
            backButtonImageView.setVisibility(View.VISIBLE);
            editMainStoreImageButton.setVisibility(View.VISIBLE);
            mainStoreImageUri = temporaryStoreImageUri; //TO Save image download URI accordingly
        } else if (storeImageNumber == 2) {
            additionalStoreImageView2.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            additionalStoreImageView2.getLayoutParams().height = 500;
            additionalStoreImageView2.setPadding(0,0,0,0);
            Glide.with(additionalStoreImageView2.getContext())
                    .load(downloadStoreImageUri)
                    .centerCrop()
                    .into(additionalStoreImageView2);
            additionalStoreImageView3.setVisibility(View.VISIBLE);
            additionalStoreImageUri2 = temporaryStoreImageUri; //TO Save image download URI accordingly
        } else if (storeImageNumber == 3) {
            additionalStoreImageView3.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            additionalStoreImageView3.getLayoutParams().height = 500;
            additionalStoreImageView3.setPadding(0,0,0,0);
            Glide.with(additionalStoreImageView3.getContext())
                    .load(downloadStoreImageUri)
                    .centerCrop()
                    .into(additionalStoreImageView3);
            additionalStoreImageView4.setVisibility(View.VISIBLE);
            additionalStoreImageUri3 = temporaryStoreImageUri; //TO Save image download URI accordingly
        } else if (storeImageNumber == 4) {
            additionalStoreImageView4.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            additionalStoreImageView4.getLayoutParams().height = 500;
            additionalStoreImageView4.setPadding(0,0,0,0);
            Glide.with(additionalStoreImageView4.getContext())
                    .load(downloadStoreImageUri)
                    .centerCrop()
                    .into(additionalStoreImageView4);
            additionalStoreImageView5.setVisibility(View.VISIBLE);
            additionalStoreImageUri4 = temporaryStoreImageUri; //TO Save image download URI accordingly
        } else if (storeImageNumber == 5) {
            additionalStoreImageView5.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            additionalStoreImageView5.requestLayout();
            additionalStoreImageView5.getLayoutParams().height = 500;
            additionalStoreImageView5.requestLayout();
            additionalStoreImageView5.setPadding(0,0,0,0);
            Glide.with(additionalStoreImageView5.getContext())
                    .load(downloadStoreImageUri)
                    .centerCrop()
                    .into(additionalStoreImageView5);
            additionalStoreImageUri5 = temporaryStoreImageUri; //TO Save image download URI accordingly
        }
        storeImageNumber = 0;

        dialogStore.cancel();

        //To show custom toast message!
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText("Mutfak resminiz başarıyla güncellendi!");
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();

        /*
        TO TAKE INITIAL LOCATION (LAT-LNG) OF USER!
         */
        Geocoder coderUser = new Geocoder(this);
        String userCountyLocationStr;
        if (refCounty.equals("Merkez")) {
            userCountyLocationStr = refCity + ", " + refCity;
        } else {
            userCountyLocationStr = refCounty + ", " + refCity;
        }
        List<Address> userAddress;
        try {
            userAddress = coderUser.getFromLocationName(userCountyLocationStr, 1);
            assert userAddress != null;
            Address userLocation = userAddress.get(0);
            double initUserLat = userLocation.getLatitude();
            double initUserLon = userLocation.getLongitude();
            //TO SET A RANDOM LOCATION
            final double maxLat = initUserLat + 0.0150;
            final double minLat = initUserLat - 0.0150;
            final double maxLon = initUserLon + 0.0150;
            final double minLon = initUserLon - 0.0150;
            Random random = new Random();
            final double randomUserLat = minLat + (maxLat - minLat) * random.nextDouble();
            final double randomUserLon = minLon + (maxLon - minLon) * random.nextDouble();
            initialStoreLatStr = String.valueOf(randomUserLat);
            initialStoreLonStr = String.valueOf(randomUserLon);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
    /*
    TO ADD PHOTO PICKED TO FIREBASE STORAGE
     */
    private void addPhotoPicked(Uri selectedImageUri) {

        dialogStore = ProgressDialog.show(StoreActivity.this, "",
                "Mutfak resminiz güncelleniyor...\nLütfen bekleyin!", true);

        final Uri file = selectedImageUri; //selected image uri
        final StorageReference mstoreImageStorageRef = mFoodsStoreStorageRef.child(storeImageNumber + "_" + storeOwnerUid + "_");

        mstoreImageStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Show uploaded photo in the main screen

                        // Get a URL to the uploaded content
                        getDownloadUrl(mstoreImageStorageRef, file);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialogStore.cancel();

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

    public void addNewStorePhoto(View view) {
        // SAME WITH addMainStoreImage(View view) !!!!!!!!!!!!!!!!!!!
        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setTitle("Mutfak resmini güncelle...");
        builder3.setMessage("\n");
        builder3.setCancelable(true);
        builder3.setPositiveButton(
                "Fotoğraf çek",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                            storeImageNumber = 1;
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile(); //Take image file name for the avatar.
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(StoreActivity.this,
                                        "com.neftisoft.android.fileprovider",
                                        photoFile);
                                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                            }
                        }
                        dialog.cancel();
                    }
                });
        builder3.setNegativeButton(
                "Galeriden seç",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        storeImageNumber = 1;
                        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        pictureIntent.setType("image/jpeg");
                        pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                        dialog.cancel();
                    }
                });
        builder3.setNeutralButton("İptal",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert123 = builder3.create();
        alert123.show();
    }

    public void editStoreName(View view) {
        deleteStoreButton.setVisibility(View.GONE);
        editStoreNameButton.setVisibility(View.GONE);
        storeNameTextView.setVisibility(View.GONE);
        storeNameEditText.setVisibility(View.VISIBLE);
        cancelEditingStoreButton.setVisibility(View.VISIBLE);
        saveEdittedStoreButton.setVisibility(View.VISIBLE);
        // MAKE 'SAVE BUTTON' VISIBLE (It can be for only this TextView)


        storeNameLL.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_settings_frame));
        storeNameCaptionTextView.setVisibility(View.VISIBLE);
        dividerBeforeStoreInfo.setVisibility(View.GONE);

        storeNameEditText.setText(storeNameString);
    }

    public void editStoreInformation(View view) {
        deleteStoreButton.setVisibility(View.GONE);
        editStoreInfoButton.setVisibility(View.GONE);
        storeInfoTextView.setVisibility(View.GONE);
        storeInfoEditText.setVisibility(View.VISIBLE);
        cancelEditingStoreButton.setVisibility(View.VISIBLE);
        saveEdittedStoreButton.setVisibility(View.VISIBLE);
        // MAKE 'SAVE BUTTON' VISIBLE (It can be for only this TextView)


        storeInfoLL.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_settings_frame));
        storeInfoCaptionTextView.setVisibility(View.VISIBLE);
        dividerAfterStoreInfo.setVisibility(View.GONE);

        storeInfoEditText.setText(storeInformationString);
    }

    /*
    TO ADD or CHANGE MORE STORE IMAGES OR TO SEE IMAGES IN FULLSCREEN !!!
     */
    public void addMoreStoreImage2(View view) {
        if (storeOwnerUid.equals(uid)) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            builder3.setTitle("Mutfak resmini güncelle...");
            builder3.setMessage("\n");
            builder3.setCancelable(true);
            builder3.setPositiveButton(
                    "Fotoğraf çek",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                                storeImageNumber = 2;
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile(); //Take image file name for the avatar.
                                } catch (IOException ex) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(StoreActivity.this,
                                            "com.neftisoft.android.fileprovider",
                                            photoFile);
                                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            }
                            dialog.cancel();
                        }
                    });
            builder3.setNegativeButton(
                    "Galeriden seç",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            storeImageNumber = 2;
                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            pictureIntent.setType("image/jpeg");
                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                            dialog.cancel();
                        }
                    });
            builder3.setNeutralButton("İptal",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert12 = builder3.create();
            alert12.show();
        } else {
            //TO SEE IMAGE FULLSCREEN
            Intent myIntent = new Intent(StoreActivity.this, FullscreenImageActivity.class);
            myIntent.putExtra("Image Uri", additionalStoreImage2String);
            StoreActivity.this.startActivity(myIntent);
        }
    }
    public void addMoreStoreImage3(View view) {
        if (storeOwnerUid.equals(uid)) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            builder3.setTitle("Mutfak resmini güncelle...");
            builder3.setMessage("\n");
            builder3.setCancelable(true);
            builder3.setPositiveButton(
                    "Fotoğraf çek",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                                storeImageNumber = 3;
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile(); //Take image file name for the avatar.
                                } catch (IOException ex) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(StoreActivity.this,
                                            "com.neftisoft.android.fileprovider",
                                            photoFile);
                                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            }
                            dialog.cancel();
                        }
                    });
            builder3.setNegativeButton(
                    "Galeriden seç",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            storeImageNumber = 3;
                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            pictureIntent.setType("image/jpeg");
                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                            dialog.cancel();
                        }
                    });
            builder3.setNeutralButton("İptal",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert12 = builder3.create();
            alert12.show();
        } else {
            //TO SEE IMAGE FULLSCREEN
            Intent myIntent = new Intent(StoreActivity.this, FullscreenImageActivity.class);
            myIntent.putExtra("Image Uri", additionalStoreImage3String);
            StoreActivity.this.startActivity(myIntent);
        }
    }
    public void addMoreStoreImage4(View view) {
        if (storeOwnerUid.equals(uid)) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            builder3.setTitle("Mutfak resmini güncelle...");
            builder3.setMessage("\n");
            builder3.setCancelable(true);
            builder3.setPositiveButton(
                    "Fotoğraf çek",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                                storeImageNumber = 4;
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile(); //Take image file name for the avatar.
                                } catch (IOException ex) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(StoreActivity.this,
                                            "com.neftisoft.android.fileprovider",
                                            photoFile);
                                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            }
                            dialog.cancel();
                        }
                    });
            builder3.setNegativeButton(
                    "Galeriden seç",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            storeImageNumber = 4;
                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            pictureIntent.setType("image/jpeg");
                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                            dialog.cancel();
                        }
                    });
            builder3.setNeutralButton("İptal",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert12 = builder3.create();
            alert12.show();
        } else {
            //TO SEE IMAGE FULLSCREEN
            Intent myIntent = new Intent(StoreActivity.this, FullscreenImageActivity.class);
            myIntent.putExtra("Image Uri", additionalStoreImage4String);
            StoreActivity.this.startActivity(myIntent);
        }
    }
    public void addMoreStoreImage5(View view) {
        if (storeOwnerUid.equals(uid)) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            builder3.setTitle("Mutfak resmini güncelle...");
            builder3.setMessage("\n");
            builder3.setCancelable(true);
            builder3.setPositiveButton(
                    "Fotoğraf çek",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                                storeImageNumber = 5;
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile(); //Take image file name for the avatar.
                                } catch (IOException ex) {
                                    // Error occurred while creating the File
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(StoreActivity.this,
                                            "com.neftisoft.android.fileprovider",
                                            photoFile);
                                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            }
                            dialog.cancel();
                        }
                    });
            builder3.setNegativeButton(
                    "Galeriden seç",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            storeImageNumber = 5;
                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            pictureIntent.setType("image/jpeg");
                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                            dialog.cancel();
                        }
                    });
            builder3.setNeutralButton("İptal",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert12 = builder3.create();
            alert12.show();
        } else {
            //TO SEE IMAGE FULLSCREEN
            Intent myIntent = new Intent(StoreActivity.this, FullscreenImageActivity.class);
            myIntent.putExtra("Image Uri", additionalStoreImage5String);
            StoreActivity.this.startActivity(myIntent);
        }
    }

    public void saveStore(View view) {
        mFoodsStoreDatabase = FirebaseDatabase.getInstance();
        mFoodsStoreDatabaseRef = mFoodsStoreDatabase.getReference().child("foodStorePhotoDatabase/" + refCity + "/" + refCounty + "/" + refNeighborhood + "/");
        mMapStoreDatabaseRef = mFoodsStoreDatabase.getReference().child("mapStorePhotoDatabase/" + refCity + "/");

        /*
        TO GET REVERSED TIME AND DATE TO LIST STORES UPSIDE DOWN (NEW TO OLD)
         */
        Calendar currentTime = Calendar.getInstance();
        int hour                = currentTime.get(Calendar.HOUR_OF_DAY) ;
        int minute              = currentTime.get(Calendar.MINUTE)      ;
        int second              = currentTime.get(Calendar.SECOND)      ;
        String [] ids           = TimeZone.getAvailableIDs()            ;
        long milliDiff          = currentTime.get(Calendar.ZONE_OFFSET) ;
        for (String id : ids)
        {
            TimeZone tz = TimeZone.getTimeZone(id)                   ;
            if (tz.getRawOffset() == milliDiff)
            {  // Found a match, now check for daylight saving
                boolean inDs    = tz.inDaylightTime(new Date())   ;
                if (inDs)       { hour += 1 ; }
                if (hour == 25) { hour  = 1 ; }
                break                                             ;
            }
        }
        String minuteString;
        if (minute < 10) {
            minuteString = "0" + minute;
        }
        else {
            minuteString = String.valueOf(minute);
        }
        String secondString;
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = String.valueOf(second);
        }
        //Magic is for listing upside down!!!
        String itemDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String itemTime = hour + minuteString + secondString;
        String itemKey;
        int inverseItemDateInt = 99991231 - Integer.parseInt(itemDate);
        int inverseItemTimeInt = 246060 - Integer.parseInt(itemTime);
        if (hour > 14) {
            itemKey = inverseItemDateInt + "_0" + inverseItemTimeInt;
        } else {
            itemKey = inverseItemDateInt + "_" + inverseItemTimeInt;
        }

        String storeName = storeNameEditText.getText().toString().trim();
        String storeInfo = storeInfoEditText.getText().toString().trim();

        checkGivenInformationAndSaveStore(storeName, storeInfo, itemKey);
    }
    private void checkGivenInformationAndSaveStore(String storeName, String storeInfo, String itemKey) {
        if (editMainStoreImageButton.getVisibility() != View.VISIBLE) { // OR IT CAN BE DETERMINED ONLY AS: if(!mainStoreImageUri.equals(null)){}
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen mutfağınız için bir resim ekleyin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else if (storeName.length() < 1) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen mutfağınıza bir isim verin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else if (storeInfo.length() < 5) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen mutfağınız hakkında bilgi verin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else {
            //TO Save Store in the FoodsStoreDatabase
            DatabaseReference mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/mainStoreImageUri");
            mFoodsDatabaseRefStore.setValue(mainStoreImageUri);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri2");
            mFoodsDatabaseRefStore.setValue(additionalStoreImageUri2);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri3");
            mFoodsDatabaseRefStore.setValue(additionalStoreImageUri3);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri4");
            mFoodsDatabaseRefStore.setValue(additionalStoreImageUri4);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri5");
            mFoodsDatabaseRefStore.setValue(additionalStoreImageUri5);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeName");
            mFoodsDatabaseRefStore.setValue(storeName);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeInfo");
            mFoodsDatabaseRefStore.setValue(storeInfo);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/userName");
            mFoodsDatabaseRefStore.setValue(username);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeKey");
            mFoodsDatabaseRefStore.setValue(itemKey);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeUid");
            mFoodsDatabaseRefStore.setValue(uid);
            //TO Save Store in the UserDatabase
            DatabaseReference mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("mainStoreImageUri");
            mUsersDatabaseRefStore.setValue(mainStoreImageUri);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri2");
            mUsersDatabaseRefStore.setValue(additionalStoreImageUri2);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri3");
            mUsersDatabaseRefStore.setValue(additionalStoreImageUri3);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri4");
            mUsersDatabaseRefStore.setValue(additionalStoreImageUri4);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri5");
            mUsersDatabaseRefStore.setValue(additionalStoreImageUri5);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("storeName");
            mUsersDatabaseRefStore.setValue(storeName);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("storeInfo");
            mUsersDatabaseRefStore.setValue(storeInfo);
            //TO Save Store in the MapStoreDatabase
            DatabaseReference mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/mainStoreImageUri");
            mMapDatabaseRefStore.setValue(mainStoreImageUri);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/storeName");
            mMapDatabaseRefStore.setValue(storeName);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/storeInfo");
            mMapDatabaseRefStore.setValue(storeInfo);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/storeUid");
            mMapDatabaseRefStore.setValue(uid);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/lat");
            mMapDatabaseRefStore.setValue(initialStoreLatStr);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/lng");
            mMapDatabaseRefStore.setValue(initialStoreLonStr);

            setLatLngToStore();
        }
    }

    /*
    TO FIND LATITUDE-LONGITUDE VALUE OF USER NBHOOD LOCATION TO ADD STORE ON MAP ACCORDINGLY!!!
     */
    private void setLatLngToStore() {
        fusedStoreLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(StoreActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (initialStoreLatStr == null || initialStoreLonStr == null) {
                Geocoder coderFood = new Geocoder(this);
                String userStoreCountyLocationStr;
                if (refCounty.equals("Merkez")) {
                    userStoreCountyLocationStr = refCity;
                } else {
                    userStoreCountyLocationStr = refCounty + ", " + refCity;
                }
                List<Address> storeAddress;
                try {
                    storeAddress = coderFood.getFromLocationName(userStoreCountyLocationStr, 1);
                    assert storeAddress != null;
                    Address storeLocation = storeAddress.get(0);
                    double storeLat = storeLocation.getLatitude();
                    double storeLon = storeLocation.getLongitude();

                    //TO SET A RANDOM LOCATION
                    final double maxLat = storeLat + 0.0150;
                    final double minLat = storeLat - 0.0150;
                    final double maxLon = storeLon + 0.0150;
                    final double minLon = storeLon - 0.0150;
                    Random random = new Random();
                    final double randomStoreLat = minLat + (maxLat - minLat) * random.nextDouble();
                    final double randomStoreLon = minLon + (maxLon - minLon) * random.nextDouble();

                    String latStoreStr = String.valueOf(randomStoreLat);
                    String lonStoreStr = String.valueOf(randomStoreLon);
                    //TO ADD LAT-LNG ON MAP REF!!!
                    DatabaseReference mMapStoreDatabaseRefLat = mMapStoreDatabaseRef.child(uid + "/lat");
                    mMapStoreDatabaseRefLat.setValue(latStoreStr);
                    DatabaseReference mMapStoreDatabaseRefLng = mMapStoreDatabaseRef.child(uid + "/lng");
                    mMapStoreDatabaseRefLng.setValue(lonStoreStr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //TO ASK FOR LOCATION PERMISSION
            AlertDialog.Builder builderStoreLocation = new AlertDialog.Builder(StoreActivity.this);
            //builder1.setIcon(R.drawable.avatar);
            builderStoreLocation.setTitle("Konum ekle");
            builderStoreLocation.setMessage("\nMutfağınız oluşturuldu!" + "\n\nMutfağınızın haritada doğru yerde görünmesi için konum izni verin!\n");
            builderStoreLocation.setCancelable(false);
            builderStoreLocation.setPositiveButton(Html.fromHtml("<font color='#556B2F'>Anladım</font>"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            final AlertDialog alertStoreLocation = builderStoreLocation.create();
            alertStoreLocation.show();
            alertStoreLocation.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertStoreLocation.dismiss();
                    ActivityCompat.requestPermissions(StoreActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,}, 11);
                }
            });
        } else {
            // TO GET the LIVE LOCATION and SET STORE LAT-LNG VALUE TO DATABASE !!
            fusedStoreLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location userLocation) {
                            if (userLocation != null) {
                                double latMapStore = userLocation.getLatitude();
                                double lonMapStore = userLocation.getLongitude();
                                String latMapStoreStr = String.valueOf(latMapStore);
                                String lonMapStoreStr = String.valueOf(lonMapStore);
                                //TO ADD LAT-LNG ON MAP REF!!!
                                DatabaseReference mMapStoreDatabaseRefLat = mMapStoreDatabaseRef.child(uid + "/lat");
                                mMapStoreDatabaseRefLat.setValue(latMapStoreStr);
                                DatabaseReference mMapStoreDatabaseRefLng = mMapStoreDatabaseRef.child(uid + "/lng");
                                mMapStoreDatabaseRefLng.setValue(lonMapStoreStr);

                                // TO Clear input boxes
                                storeNameEditText.setText("");
                                storeInfoEditText.setText("");

                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Mutfağınız oluşturuldu!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();

                                // TO Go back to party activity
                                onBackPressed();
                            } else {
                                setLatLngToStore();
                            }
                        }
                    });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestStoreCode, @NonNull String[] permissions, @NonNull int[] grantStoreResults) {
        if (requestStoreCode == 11 && grantStoreResults[0] == PackageManager.PERMISSION_GRANTED) {
            // TO GET the LIVE LOCATION and SET STORE LAT-LNG VALUE TO DATABASE !!
            fusedStoreLocationClient.getLastLocation() //PERMISSION IS ASKED JUST BEFORE THIS LINE OF CODE!!!
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location userLocation) {
                            if (userLocation != null) {
                                double latMapStore = userLocation.getLatitude();
                                double lonMapStore = userLocation.getLongitude();
                                String latMapStoreStr = String.valueOf(latMapStore);
                                String lonMapStoreStr = String.valueOf(lonMapStore);
                                //TO ADD LAT-LNG ON MAP REF!!!
                                DatabaseReference mMapStoreDatabaseRefLat = mMapStoreDatabaseRef.child(uid + "/lat");
                                mMapStoreDatabaseRefLat.setValue(latMapStoreStr);
                                DatabaseReference mMapStoreDatabaseRefLng = mMapStoreDatabaseRef.child(uid + "/lng");
                                mMapStoreDatabaseRefLng.setValue(lonMapStoreStr);

                                // TO Clear input boxes
                                storeNameEditText.setText("");
                                storeInfoEditText.setText("");

                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Konumunuz eklendi!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();

                                // TO Go back to party activity
                                onBackPressed();
                            } else {
                                setLatLngToStore();
                            }
                        }
                    });
        } else {
            // TO Clear input boxes
            storeNameEditText.setText("");
            storeInfoEditText.setText("");

            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Mutfağınız oluşturuldu!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();

            // TO Go back to party activity
            onBackPressed();
        }
    }

    public void cancelStore(View view) {
        mFoodsStoreStorageRef.child("1_"+uid+"_").delete();
        mFoodsStoreStorageRef.child("2_"+uid+"_").delete();
        mFoodsStoreStorageRef.child("3_"+uid+"_").delete();
        mFoodsStoreStorageRef.child("4_"+uid+"_").delete();
        mFoodsStoreStorageRef.child("5_"+uid+"_").delete();
        mFoodsStoreStorageRef.child("1_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
        mFoodsStoreStorageRef.child("2_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
        mFoodsStoreStorageRef.child("3_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
        mFoodsStoreStorageRef.child("4_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
        mFoodsStoreStorageRef.child("5_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
        onBackPressed();
    }

    public void deleteStore(View view) {
        mFoodsStoreDatabase = FirebaseDatabase.getInstance();
        mFoodsStoreDatabaseRef = mFoodsStoreDatabase.getReference().child("foodStorePhotoDatabase/" + refCity + "/" + refCounty + "/" + refNeighborhood + "/" + uid + "/");
        mMapStoreDatabaseRefToDelete = mFoodsStoreDatabase.getReference().child("mapStorePhotoDatabase/" + refCity + "/" + uid + "/");

        AlertDialog.Builder builderStore = new AlertDialog.Builder(StoreActivity.this);
        builderStore.setTitle("Mutfağı sil");
        builderStore.setMessage("\nMutfağınızı silmek istediğinize emin misiniz?\n");
        builderStore.setCancelable(true);
        builderStore.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (storeOwnerUid.equals(uid)) {
                            mFoodsStoreDatabaseRef.removeValue();
                            mUsersDatabaseStoreRef.removeValue();
                            mMapStoreDatabaseRefToDelete.removeValue();
                            mFoodsStoreStorageRef.child("1_"+uid+"_").delete();
                            mFoodsStoreStorageRef.child("2_"+uid+"_").delete();
                            mFoodsStoreStorageRef.child("3_"+uid+"_").delete();
                            mFoodsStoreStorageRef.child("4_"+uid+"_").delete();
                            mFoodsStoreStorageRef.child("5_"+uid+"_").delete();
                            mFoodsStoreStorageRef.child("1_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
                            mFoodsStoreStorageRef.child("2_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
                            mFoodsStoreStorageRef.child("3_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
                            mFoodsStoreStorageRef.child("4_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
                            mFoodsStoreStorageRef.child("5_"+uid+"_"+"_1024x1024").delete(); //(Resized version!)
                            onBackPressed();
                        }
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Mutfağınız silindi!");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();

                        dialog.cancel();
                    }
                });
        builderStore.setNegativeButton(Html.fromHtml("<font color='#556B2F'>Vazgeç</font>"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert1Store = builderStore.create();
        alert1Store.show();
    }

    public void cancelEditingStore(View view) {
        storeNameEditText.setText("");
        storeInfoEditText.setText("");
        cancelEditingStoreButton.setVisibility(View.GONE);
        saveEdittedStoreButton.setVisibility(View.GONE);
        storeNameEditText.setVisibility(View.GONE);
        storeInfoEditText.setVisibility(View.GONE);
        storeNameCaptionTextView.setVisibility(View.GONE);
        storeInfoCaptionTextView.setVisibility(View.GONE);
        storeAddImagesCaptionTextView.setVisibility(View.GONE);
        storeNameLL.setBackground(null);
        storeInfoLL.setBackground(null);
        storeAddImagesLL.setBackground(null);
        deleteStoreButton.setVisibility(View.VISIBLE);
        editStoreNameButton.setVisibility(View.VISIBLE);
        storeNameTextView.setVisibility(View.VISIBLE);
        editStoreInfoButton.setVisibility(View.VISIBLE);
        storeInfoTextView.setVisibility(View.VISIBLE);
        dividerBeforeStoreInfo.setVisibility(View.VISIBLE);
        dividerAfterStoreInfo.setVisibility(View.VISIBLE);
        dividerBeforeStoreOwner.setVisibility(View.VISIBLE);
    }

    public void saveEditedStore(View view) {
        mFoodsStoreDatabase = FirebaseDatabase.getInstance();
        mFoodsStoreDatabaseRef = mFoodsStoreDatabase.getReference().child("foodStorePhotoDatabase/" + refCity + "/" + refCounty + "/" + refNeighborhood + "/");
        mMapStoreDatabaseRef = mFoodsStoreDatabase.getReference().child("mapStorePhotoDatabase/" + refCity + "/");

        /*
        TO GET REVERSED TIME AND DATE TO LIST STORES UPSIDE DOWN (NEW TO OLD)
         */
        Calendar currentTime = Calendar.getInstance();
        int hour                = currentTime.get(Calendar.HOUR_OF_DAY) ;
        int minute              = currentTime.get(Calendar.MINUTE)      ;
        int second              = currentTime.get(Calendar.SECOND)      ;
        String [] ids           = TimeZone.getAvailableIDs()            ;
        long milliDiff          = currentTime.get(Calendar.ZONE_OFFSET) ;
        for (String id : ids)
        {
            TimeZone tz = TimeZone.getTimeZone(id)                   ;
            if (tz.getRawOffset() == milliDiff)
            {  // Found a match, now check for daylight saving
                boolean inDs    = tz.inDaylightTime(new Date())   ;
                if (inDs)       { hour += 1 ; }
                if (hour == 25) { hour  = 1 ; }
                break                                             ;
            }
        }
        String minuteString;
        if (minute < 10) {
            minuteString = "0" + minute;
        }
        else {
            minuteString = String.valueOf(minute);
        }
        String secondString;
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = String.valueOf(second);
        }

        //Magic is for listing upside down!!!
        String itemDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String itemTime = hour + minuteString + secondString;
        String itemKey;
        int inverseItemDateInt = 99991231 - Integer.parseInt(itemDate);
        int inverseItemTimeInt = 246060 - Integer.parseInt(itemTime);
        if (hour > 14) {
            itemKey = inverseItemDateInt + "_0" + inverseItemTimeInt;
        } else {
            itemKey = inverseItemDateInt + "_" + inverseItemTimeInt;
        }

        String storeName;
        String storeInfo;
        storeName = storeNameEditText.getText().toString();
        storeInfo = storeInfoEditText.getText().toString();
        if (!storeName.equals("")) {
            storeNameString = storeName;
        }
        if (!storeInfo.equals("")) {
            storeInformationString = storeInfo;
        }
        checkGivenInformationAndUpdateStore(storeNameString, storeInformationString, itemKey);
    }
    private void checkGivenInformationAndUpdateStore(String storeName, String storeInfo, String itemKey) {
        if (editMainStoreImageButton.getVisibility() != View.VISIBLE) { // OR IT CAN BE DETERMINED ONLY AS: if(!mainStoreImageUri.equals(null)){}
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen mutfağınız için bir resim ekleyin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else if (storeName.length() < 1) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen mutfağınıza bir isim verin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else if (storeInfo.length() < 5) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Lütfen mutfağınız hakkında bir kaç kelime bilgi verin!");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
        else {
            //TO Update Store in the FoodsStoreDatabase
            DatabaseReference mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/mainStoreImageUri");
            if (mainStoreImageUri != null) {
                mFoodsDatabaseRefStore.setValue(mainStoreImageUri);
            }
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri2");
            if (additionalStoreImageUri2 != null) {
                mFoodsDatabaseRefStore.setValue(additionalStoreImageUri2);
            }
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri3");
            if (additionalStoreImageUri3 != null) {
                mFoodsDatabaseRefStore.setValue(additionalStoreImageUri3);
            }
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri4");
            if (additionalStoreImageUri4 != null) {
                mFoodsDatabaseRefStore.setValue(additionalStoreImageUri4);
            }
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/additionalStoreImageUri5");
            if (additionalStoreImageUri5 != null) {
                mFoodsDatabaseRefStore.setValue(additionalStoreImageUri5);
            }
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeName");
            mFoodsDatabaseRefStore.setValue(storeName);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeInfo");
            mFoodsDatabaseRefStore.setValue(storeInfo);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeUid");
            mFoodsDatabaseRefStore.setValue(uid);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/userName");
            mFoodsDatabaseRefStore.setValue(username);
            mFoodsDatabaseRefStore = mFoodsStoreDatabaseRef.child(uid + "/storeKey");
            mFoodsDatabaseRefStore.setValue(itemKey);

            //TO Update Store in the UserDatabase
            DatabaseReference mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("mainStoreImageUri");
            if (mainStoreImageUri != null) {
                mUsersDatabaseRefStore.setValue(mainStoreImageUri);
            }
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri2");
            if (additionalStoreImageUri2 != null) {
                mUsersDatabaseRefStore.setValue(additionalStoreImageUri2);
            }
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri3");
            if (additionalStoreImageUri3 != null) {
                mUsersDatabaseRefStore.setValue(additionalStoreImageUri3);
            }
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri4");
            if (additionalStoreImageUri4 != null) {
                mUsersDatabaseRefStore.setValue(additionalStoreImageUri4);
            }
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("additionalStoreImageUri5");
            if (additionalStoreImageUri5 != null) {
                mUsersDatabaseRefStore.setValue(additionalStoreImageUri5);
            }
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("storeName");
            mUsersDatabaseRefStore.setValue(storeName);
            mUsersDatabaseRefStore = mUsersDatabaseStoreRef.child("storeInfo");
            mUsersDatabaseRefStore.setValue(storeInfo);

            //TO Save Store in the MapStoreDatabase
            DatabaseReference mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/mainStoreImageUri");
            if (mainStoreImageUri != null) {
                mMapDatabaseRefStore.setValue(mainStoreImageUri);
            }
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/storeName");
            mMapDatabaseRefStore.setValue(storeName);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/storeInfo");
            mMapDatabaseRefStore.setValue(storeInfo);
            mMapDatabaseRefStore = mMapStoreDatabaseRef.child(uid + "/storeUid");
            mMapDatabaseRefStore.setValue(uid);

            setLatLngToStore();
        }
    }

    public void giveAnOrder(View view) {
        Intent myIntent = new Intent(StoreActivity.this, ChatActivity.class);
        myIntent.putExtra("Other Uid", otherUid);
        myIntent.putExtra("Other Username", storeOwnerName);
        StoreActivity.this.startActivity(myIntent);
    }

    public void goToProfile(View view) {
        Intent myIntent = new Intent(StoreActivity.this, ProfileActivity.class);
        myIntent.putExtra("Other Uid", otherUid);
        StoreActivity.this.startActivity(myIntent);
    }
}
