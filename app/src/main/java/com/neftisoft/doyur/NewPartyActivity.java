package com.neftisoft.doyur;

import android.Manifest;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;

import static java.lang.StrictMath.abs;

public class NewPartyActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {

    ExtendedFloatingActionButton fabAddFoodButton;
    ExtendedFloatingActionButton fabAddStoreButton;
    FloatingActionButton fabChangeLayerButton;

    private Dialog dialogNewParty;
    private Dialog dialogProfile;
    private Boolean dialogProfileBool = false;
    private boolean locationChanged = false;

    //byte fabButtonSwitch = 0; // Floating Action Button Switch (OLD ONE)
    byte selectorButtonSwitch = 2; // Store(1), Daily(2) and Charity(3) Buttons Switch (NEW: TAB BUTTONS)
    byte selectorButtonSwitchLast = 2; // Daily(2) and Charity(3) TABS Last Value Before Leaving List View (TO Add food or store to right place when getting back from map view to the list view) (NEW: TAB BUTTONS)
    byte locationPermissionAsked = 0; // "1" if 'doyur' already asked for location permission once!
    //int locationPermissionGiven = 0; // "1" if 'user' already gave location permission once!
    byte mapModeSwitch = 0; // "1" if 'user' goes to map mode!

    private String initialUserLatStr;
    private String initialUserLonStr;

    ViewPager2 newPartyViewPager;
    TabLayout newPartyTabLayout;
    NewPartyViewPagerFragmentAdapter newPartyPagerAdapter;

    private static final byte REQUEST_TAKE_PHOTO = 99; // DEVELOPER ANDROID
    private static final byte RC_PHOTO_PICKER =  88;

    FirebaseUser user;

    //Firebase Instance Variables
    private StorageReference mFoodsDailyStorageRef;
    private FirebaseDatabase mFoodsDailyDatabase;
    private DatabaseReference mFoodsDailyDatabaseRef;

    private StorageReference mFoodsCharityStorageRef;
    private FirebaseDatabase mFoodsCharityDatabase;
    private DatabaseReference mFoodsCharityDatabaseRef;
    private FirebaseDatabase mFoodsStoreDatabase;

    //MAP REFS
    private DatabaseReference mMapFoodsDailyDatabaseRef;
    private DatabaseReference mMapFoodsCharityDatabaseRef;
    private DatabaseReference mMapFoodsStoreDatabaseRef;
    private ChildEventListener mMapFoodChildEventListener; //MAP LISTENER

    private DatabaseReference mUsersDatabaseRef;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseTokenRef;  //To store, update and get user TOKEN
    private DatabaseReference mUsersDatabaseDailyRef;  //To store, update and get user Daily Food (Image Uri)
    private DatabaseReference mUsersDatabaseCharityRef;  //To store, update and get user Charity Food (Image Uri)
    private DatabaseReference mUsersDatabaseLocationRef;  //To store, update and get user Location Information

    String imageFileName; //DEVELOPER ANDROID SOLUTION
    String currentPhotoPath; //DEVELOPER ANDROID SOLUTION

    //User information
    String username;
    String uid;
    String avatarUri;

    Spinner citySpinner;
    Spinner countySpinner;
    Spinner nbhoodSpinner;

    //Firebase references for user information
    String city;
    String refCity;  //Clean city name for database. (without ç,ş,ğ...)
    String county;
    String refCounty;  //Clean city name for database. (without ç,ş,ğ...)
    String neighborhood;
    String refNeighborhood;  //Clean city name for database. (without ç,ş,ğ...)
    byte addPhotoPermission = 0; //To check if all location spinners filled! (1 = Filled)

    //Initializing Layouts
    LinearLayout locationSelector;
    LinearLayout fixLocation;
    LinearLayout editLocationIcon;

    //View mapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    double lat;
    double lon;
    private MarkerOptions userPositionMarker = null;

    Window window;

    Handler mHandler3 = new Handler();
    Handler handlerForPermission = new Handler();

    NavigationBarView nbv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_party);

        dialogNewParty = ProgressDialog.show(NewPartyActivity.this, "",
                "Giriş yapılıyor...", true);

        // To change the status bar color!
        window = NewPartyActivity.this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= 23) {
            // change the background color of status bar
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            // finally change the background color of status bar
            window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBarFor23));
        } else {
            // finally change the background color of status bar
            window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBar));
        }

        //INIT Floating Action Buttons
        fabAddFoodButton = findViewById(R.id.fab_add_food);
        fabAddStoreButton = findViewById(R.id.fab_add_store);
        fabChangeLayerButton = findViewById(R.id.fab_change_layer);

        //To access user information!!
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        FirebaseMessaging mToken = FirebaseMessaging.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (user != null) {
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = user.getUid();
            // Name, email address, and profile photo Url
            username = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                avatarUri = photoUrl.toString();
                setAvatar();
            } else {
                ImageView avatarFrameImage = findViewById(R.id.user_avatar_frame_image);
                avatarFrameImage.setVisibility(View.GONE);
            }

            // TO Get TOKEN
            mToken.getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    setToken(s);
                }
            });
        }

        //City Location Setter
        citySpinner = findViewById(R.id.city_spinner);
        final ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(NewPartyActivity.this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setDropDownWidth(400);
        citySpinner.setAdapter(cityAdapter);
        citySpinner.setOnItemSelectedListener(NewPartyActivity.this);

        //Initializing Layouts
        locationSelector = findViewById(R.id.location_selector_layout);
        fixLocation = findViewById(R.id.fix_location_layout);
        editLocationIcon = findViewById(R.id.edit_location_icon_layout);

        /*
        TO REMEMBER THE USER LOCATION PREFERENCES IF APPLICABLE (AND UPDATE TOKEN) !!!
         */
        FirebaseDatabase mUsersDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        mUsersDatabaseRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/");  //To store and update user information
        mUsersDatabaseTokenRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "notificationDatabase" + "/" + "Token");  //To store and update user TOKEN
        mUsersDatabaseDailyRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "dailyFoodsDatabase/");  //To store, update and get user Daily Food (Image Uri)
        mUsersDatabaseCharityRef = mUsersDatabase.getReference().child("usersDatabase/" + uid + "/" + "charityFoodsDatabase/");  //To store, update and get user Charity Food (Image Uri)
        mUsersDatabaseLocationRef = mUsersDatabaseRef.child("locationDatabase/");

        //DAILY DATABASE & STORAGE
        FirebaseStorage mFoodsDailyStorage = FirebaseStorage.getInstance();
        mFoodsDailyDatabase = FirebaseDatabase.getInstance();
        mFoodsDailyStorageRef = mFoodsDailyStorage.getReference().child("foodDailyPhotoStorage/" + uid + "/");   // + a spesific range of the current location (City or town)
        
        //CHARITY DATABASE & STORAGE
        FirebaseStorage mFoodsCharityStorage = FirebaseStorage.getInstance();
        mFoodsCharityDatabase = FirebaseDatabase.getInstance();
        mFoodsCharityStorageRef = mFoodsCharityStorage.getReference().child("foodCharityPhotoStorage/" + uid + "/");   // + a spesific range of the current location (City or town)
        
        //STORE DATABASE & STORAGE
        mFoodsStoreDatabase = FirebaseDatabase.getInstance();

        /*
        TO REMEMBER THE USER LOCATION PREFERENCES IF APPLICABLE OR SHOW A WELCOME MESSAGE (AND HIGHLIGHT THE LOCATION SELECTOR!) TO NEW USER FOR ONCE !!!
         */
        mUsersDatabaseLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userLocationSnapshot) {
                if (userLocationSnapshot.hasChild("city") && userLocationSnapshot.hasChild("county") && userLocationSnapshot.hasChild("nbhood")) {
                    city = userLocationSnapshot.child("city").getValue().toString();
                    county = userLocationSnapshot.child("county").getValue().toString();
                    neighborhood = userLocationSnapshot.child("nbhood").getValue().toString();

                    //Get refLocations to use them for listening the Database Reference.
                    refCity = city.replace("ç","c").replace("Ç","C")
                            .replace("ğ","g").replace("Ğ","G")
                            .replace("ı","i").replace("İ","I")
                            .replace("ö","o").replace("Ö","O")
                            .replace("ü","u").replace("Ü","U")
                            .replace("ş","s").replace("Ş","S").replace(" ","");
                    refCounty = county.replace("ç","c").replace("Ç","C")
                            .replace("ğ","g").replace("Ğ","G")
                            .replace("ı","i").replace("İ","I")
                            .replace("ö","o").replace("Ö","O")
                            .replace("ü","u").replace("Ü","U")
                            .replace("ş","s").replace("Ş","S").replace(" ","");
                    refNeighborhood = neighborhood.replace("ç","c").replace("Ç","C")
                            .replace("ğ","g").replace("Ğ","G")
                            .replace("ı","i").replace("İ","I")
                            .replace("ö","o").replace("Ö","O")
                            .replace("ü","u").replace("Ü","U")
                            .replace("ş","s").replace("Ş","S").replace(" ","");

                    setLocationsAndTabs(city, county, neighborhood, refCity, refCounty, refNeighborhood, (byte) 0); //Set location info to the related TextViews!
                } else {
                    dialogNewParty.cancel();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        int foregroundColor = R.color.foregroundColor;
                        findViewById(R.id.new_party_tab_layout).setForeground(new ColorDrawable(ContextCompat.getColor(NewPartyActivity.this, foregroundColor)));
                        findViewById(R.id.top_layout).setForeground(new ColorDrawable(ContextCompat.getColor(NewPartyActivity.this, foregroundColor)));
                        findViewById(R.id.fragment).setForeground(new ColorDrawable(ContextCompat.getColor(NewPartyActivity.this, foregroundColor)));
                        findViewById(R.id.bottom_navigation_view).setForeground(new ColorDrawable(ContextCompat.getColor(NewPartyActivity.this, foregroundColor)));
                    }

                    AlertDialog.Builder builderWelcome = new AlertDialog.Builder(NewPartyActivity.this);
                    builderWelcome.setTitle("HOŞGELDİNİZ!");
                    builderWelcome.setIcon(R.drawable.doyur_logo_original);
                    builderWelcome.setMessage("\nKonumunuzu seçerek doyur'u kullanmaya başlayabilirsiniz!" +
                            "\n\nGÜNLÜK: Sıcak yemeğinizi ücretli olarak sunmak veya ev yemeği satın almak için..." +
                            "\n\nSİPARİŞ: Mutfak açarak sürekli sipariş kabul etmek veya açılmış mutfakları görmek için..." +
                            "\n\nASKIDA: Yemeğinizi ücretsiz sunmak veya ev yemeği ile karnınızı ücretsiz doyurmak için..." +
                            "\n\n'Yemek' eklemek veya 'Mutfak' oluşturmak için ilgili sekmedeyken alttaki simgeleri tıklayabilirsiniz!" +
                            "\n\ndoyur'u keyifle kullanmanız dileğiyle...");
                    builderWelcome.setCancelable(false);
                    builderWelcome.setPositiveButton(Html.fromHtml("<font color='#556B2F'>TAMAM</font>"),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert1Welcome = builderWelcome.create();
                    alert1Welcome.show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        
        /*
        FLOATING ACTION BUTTONS!!!
         */
        // TO ADD NEW FOOD...
        fabAddFoodButton.setOnClickListener(new View.OnClickListener() {
            final Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            @Override
            public void onClick(View view) {
                /*
                TO VIBRATE
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(75);
                }
                if (user != null) {
                    if (addPhotoPermission == 1 || fixLocation.getVisibility() == View.VISIBLE) {
                        if (selectorButtonSwitch == 2) {
                            selectorButtonSwitchLast = 2;
                            /*
                            TO SHOW POPUP MESSAGE AND DIRECT USER ACCORDINGLY
                             */
                            AlertDialog.Builder builder = new AlertDialog.Builder(NewPartyActivity.this);
                            builder.setMessage("\nResim eklemek için bir yöntem seçin...\n")
                                    .setTitle("Yemeğini Sat!\n")
                                    .setPositiveButton(Html.fromHtml("<font color='#556B2F'>Fotoğraf ÇEK</font>"), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dispatchTakePictureIntent();
                                        }
                                    })
                                    .setNeutralButton(Html.fromHtml("<font color='#FF6347'>Galeriden SEÇ</font>"), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                            pictureIntent.setType("image/jpeg");
                                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else if (selectorButtonSwitch == 3) {
                            selectorButtonSwitchLast = 3;
                                /*
                                TO SHOW POPUP MESSAGE AND DIRECT USER ACCORDINGLY
                                 */
                            AlertDialog.Builder builder = new AlertDialog.Builder(NewPartyActivity.this);
                            builder.setMessage("\nResim eklemek için bir yöntem seçin...\n")
                                    .setTitle("Yemeğini Ücretsiz Ver!\n")
                                    .setPositiveButton(Html.fromHtml("<font color='#556B2F'>Fotoğraf ÇEK</font>"), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dispatchTakePictureIntent();
                                        }
                                    })
                                    .setNeutralButton(Html.fromHtml("<font color='#FF6347'>Galeriden SEÇ</font>"), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                            pictureIntent.setType("image/jpeg");
                                            pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                            startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        /*
                        TO TAKE INITIAL LOCATION (LAT-LNG) OF USER!
                         */
                        Geocoder coderUser = new Geocoder(NewPartyActivity.this);
                        String userCountyLocationStr;
                        if (county.equals("Merkez")) {
                            userCountyLocationStr = city;
                        } else {
                            userCountyLocationStr = county + ", " + city;
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
                            initialUserLatStr = String.valueOf(randomUserLat);
                            initialUserLonStr = String.valueOf(randomUserLon);
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    } else {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("\nDevam etmek için önce KONUM seçin!\n");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("\nLütfen tekrar GİRİŞ yapın!\n");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        // TO ADD NEW STORE...
        fabAddStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    if (addPhotoPermission == 1 && fixLocation.getVisibility() == View.VISIBLE) {
                        Intent myIntent = new Intent(NewPartyActivity.this, StoreActivity.class);
                        NewPartyActivity.this.startActivity(myIntent);
                    } else {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("\nDevam etmek için önce KONUM seçin!\n");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("\nLütfen tekrar GİRİŞ yapın!\n");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        // TO CHANGE LAYER IN MAP...
        fabChangeLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (user != null) {
                    if (addPhotoPermission == 1) {
                        /*
                        TO VIBRATE
                         */
                        // Vibrate for 500 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v1.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            v1.vibrate(75);
                        }
                        showMapLayerModalBottomDialog();
                    } else {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("\nDevam etmek için önce KONUM seçin!\n");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("\nLütfen tekrar GİRİŞ yapın!\n");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });

        /*
        BOTTOM NAVIGATION !!!
         */
        View newPartyViewPagerLayout = findViewById(R.id.new_party_view_pager_layout);
        View appBarLayout = findViewById(R.id.app_bar_layout);
        View newPartyMapLayout = findViewById(R.id.new_party_map_layout);
        View newPartyMapView = findViewById(R.id.new_party_map_view);
        nbv = findViewById(R.id.bottom_navigation_view);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new BottomFragmentHome()).commit();
        nbv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (addPhotoPermission == 1) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.bottomFragmentHome:
                            selectedFragment = new BottomFragmentHome();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, selectedFragment).commit();
                            mapModeSwitch = 0;
                            selectorButtonSwitch = selectorButtonSwitchLast;
                            handlerForPermission.removeCallbacksAndMessages(null);
                            newPartyMapLayout.setVisibility(View.GONE);
                            newPartyMapView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                            // TO Restore the System UI Flags (Status and Navigation Bars) after Map Fragment!
                            if (Build.VERSION.SDK_INT >= 23) {
                                // TO Change text and background colors of status bar
                                window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBarFor23));
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); //TO Make MAP limitless (Works with Transparency)
                            } else {
                                window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBar)); //TO Change the background color of status bar (Transparent)
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE); //TO Make MAP limitless (Works with Transparency)
                            }
                            newPartyViewPagerLayout.setVisibility(View.VISIBLE);
                            appBarLayout.setVisibility(View.VISIBLE);
                            break;

                        case R.id.bottomFragmentMap:
                            selectedFragment = new BottomFragmentMap();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, selectedFragment).commit();
                            mapModeSwitch = 1;
                            selectorButtonSwitchLast = selectorButtonSwitch;
                            selectorButtonSwitch = 2; //TO SHOW ALWAYS DAILY FOODS FIRST (MORE CROWDED)!
                            handlerForPermission.removeCallbacksAndMessages(null);
                            newPartyViewPagerLayout.setVisibility(View.GONE);
                            appBarLayout.setVisibility(View.GONE);
                            newPartyMapLayout.setVisibility(View.VISIBLE);
                            newPartyMapView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            //TO Make MAP limitless (Works with Transparency)
                            window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.transparentColor)); //TO Change the background color of status bar (Transparent)
                            if (Build.VERSION.SDK_INT >= 23) {
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            } else {
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                            }

                            checkLocationPermission();
                            startGoogleMaps();
                            /*
                            TO SHOW FOODS AND STORES IN THE MAP MODE!!!
                             */
                            mMapFoodChildEventListener = new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataMapSnapshot, @Nullable String s) {
                                    if (dataMapSnapshot.hasChild("lat") && dataMapSnapshot.hasChild("lng")) {
                                        String latSnapStr = dataMapSnapshot.child("lat").getValue().toString();
                                        double latSnapDbl = Double.parseDouble(latSnapStr);
                                        String lngSnapStr = dataMapSnapshot.child("lng").getValue().toString();
                                        double lngSnapDbl = Double.parseDouble(lngSnapStr);
                                        if (selectorButtonSwitch == 1) {
                                            if (dataMapSnapshot.hasChild("storeName")
                                                    && dataMapSnapshot.hasChild("storeInfo")
                                                    && dataMapSnapshot.hasChild("mainStoreImageUri")
                                                    && dataMapSnapshot.hasChild("storeUid")) {
                                                String mapStoreName = dataMapSnapshot.child("storeName").getValue().toString();
                                                String mapStoreInfo = dataMapSnapshot.child("storeInfo").getValue().toString();
                                                String mapStoreMainPhotoUrl = dataMapSnapshot.child("mainStoreImageUri").getValue().toString();
                                                String mapStoreOwnerUid = dataMapSnapshot.child("storeUid").getValue().toString();
                                                setStoreInMap(latSnapDbl, lngSnapDbl, mapStoreName, mapStoreInfo, mapStoreMainPhotoUrl, mapStoreOwnerUid);
                                            }
                                        } else {
                                            if (dataMapSnapshot.hasChild("name")
                                                    && dataMapSnapshot.hasChild("price")
                                                    && dataMapSnapshot.hasChild("photoUrl")
                                                    && dataMapSnapshot.hasChild("uid")) {
                                                String mapOwnerName = dataMapSnapshot.child("name").getValue().toString();
                                                String mapFoodPriceTag = dataMapSnapshot.child("price").getValue().toString();
                                                String mapFoodPhotoUrl = dataMapSnapshot.child("photoUrl").getValue().toString();
                                                String mapOwnerUid = dataMapSnapshot.child("uid").getValue().toString();
                                                setFoodInMap(latSnapDbl, lngSnapDbl, mapOwnerName, mapFoodPriceTag, mapFoodPhotoUrl, mapOwnerUid);
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            };
                            break;

                        case R.id.bottomFragmentDesire:
                            mapModeSwitch = 0;
                            selectorButtonSwitchLast = selectorButtonSwitch;
                            handlerForPermission.removeCallbacksAndMessages(null);
                            newPartyViewPagerLayout.setVisibility(View.GONE);
                            appBarLayout.setVisibility(View.GONE);
                            newPartyMapLayout.setVisibility(View.GONE);
                            newPartyMapView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                            // TO Restore the System UI Flags (Status and Navigation Bars) after Map Fragment!
                            if (Build.VERSION.SDK_INT >= 23) {
                                // TO Change text and background colors of status bar
                                window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBarFor23));
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); //TO Make MAP limitless (Works with Transparency)
                            } else {
                                window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBar)); //TO Change the background color of status bar (Transparent)
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE); //TO Make MAP limitless (Works with Transparency)
                            }
                            selectedFragment = new BottomFragmentDesire();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, selectedFragment).commit();
                            break;

                        case R.id.bottomFragmentLobby:
                            mapModeSwitch = 0;
                            selectorButtonSwitchLast = selectorButtonSwitch;
                            handlerForPermission.removeCallbacksAndMessages(null);
                            newPartyViewPagerLayout.setVisibility(View.GONE);
                            appBarLayout.setVisibility(View.GONE);
                            newPartyMapLayout.setVisibility(View.GONE);
                            newPartyMapView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
                            // TO Restore the System UI Flags (Status and Navigation Bars) after Map Fragment!
                            if (Build.VERSION.SDK_INT >= 23) {
                                // TO Change text and background colors of status bar
                                window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBarFor23));
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); //TO Make MAP limitless (Works with Transparency)
                            } else {
                                window.setStatusBarColor(ContextCompat.getColor(NewPartyActivity.this,R.color.colorStatusBar)); //TO Change the background color of status bar (Transparent)
                                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE); //TO Make MAP limitless (Works with Transparency)
                            }
                            selectedFragment = new BottomFragmentLobby();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, selectedFragment).commit();
                            break;
                    }
                    return true;
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("\nDevam etmek için önce KONUM seçin!\n");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    return false;
                }
            }
        });
    }
    /*
    TO SEND USER TO PROFILE!
     */
    public void goToUserProfile(View view) {
        if (addPhotoPermission == 0 || findViewById(R.id.fix_location_layout).getVisibility() == View.GONE) {
            //To show custom toast message!
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("\nDevam etmek için önce bir KONUM seçin!\n");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialogProfile = ProgressDialog.show(NewPartyActivity.this, "",
                        "Yönlendiriliyor...", true);
                dialogProfile.create();
                dialogProfileBool = true;
            }
            Intent myIntent = new Intent(NewPartyActivity.this, ProfileActivity.class);
            NewPartyActivity.this.startActivity(myIntent);
        }
    }
    /*
    Location Changer Button
     */
    public void changeLocation(View view) {
        fixLocation.setVisibility(View.GONE);
        editLocationIcon.setVisibility(View.GONE);
        locationSelector.setVisibility(View.VISIBLE);
        addPhotoPermission = 0;
    }
    /*
    City/County/NbHood Spinner Selection!!!
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
        int id = parent.getId();
        switch (id) {

            case R.id.city_spinner:
                city = parent.getItemAtPosition(position).toString();

                refCity = city.replace("ç","c").replace("Ç","C")
                        .replace("ğ","g").replace("Ğ","G")
                        .replace("ı","i").replace("İ","I")
                        .replace("ö","o").replace("Ö","O")
                        .replace("ü","u").replace("Ü","U")
                        .replace("ş","s").replace("Ş","S").replace(" ","");
                setCountyLocation();
                break;

            case R.id.county_spinner:
                county = parent.getItemAtPosition(position).toString();

                refCounty = county.replace("ç","c").replace("Ç","C")
                        .replace("ğ","g").replace("Ğ","G")
                        .replace("ı","i").replace("İ","I")
                        .replace("ö","o").replace("Ö","O")
                        .replace("ü","u").replace("Ü","U")
                        .replace("ş","s").replace("Ş","S").replace(" ","");

                setNbhoodLocation();
                break;

            case R.id.neighborhood_spinner:
                neighborhood = parent.getItemAtPosition(position).toString();

                refNeighborhood = neighborhood.replace("ç","c").replace("Ç","C")
                        .replace("ğ","g").replace("Ğ","G")
                        .replace("ı","i").replace("İ","I")
                        .replace("ö","o").replace("Ö","O")
                        .replace("ü","u").replace("Ü","U")
                        .replace("ş","s").replace("Ş","S").replace(" ","");

                //To make sure the user listen correct neighborhood reference and add foods.
                if (!refCity.isEmpty() && !refCounty.isEmpty() && !refNeighborhood.isEmpty() && !county.equals("İlçe seçin") && !county.equals("Önce il seçin") && !neighborhood.equals("Mahalle seçin") && !neighborhood.equals("Önce ilçe seçin")) {

                    setLocationsAndTabs(city, county, neighborhood, refCity, refCounty, refNeighborhood, (byte) 1);
                    updateUserLocation();

                } else {
                    addPhotoPermission = 0;
                }
                break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
    /*
    AVATAR SETTER
     */
    private void setAvatar() {
        findViewById(R.id.user_avatar_frame_image).setVisibility(View.VISIBLE);
        ImageView avatarImageView = findViewById(R.id.user_avatar_image);
        Glide.with(avatarImageView.getContext())
                .load(avatarUri)
                .centerCrop()
                .into(avatarImageView);
    }
    /*
    TOKEN SETTER
     */
    private void setToken(String token) {
        //Delete old Token if there is a new one! (Instead delete it anyway!)
        if (token != null) {
            DatabaseReference mUsersDatabaseRefToken = mUsersDatabaseTokenRef.child(token);
            mUsersDatabaseRefToken.setValue(true);
        }
    }
    /*
    To update user information according to the location selection!
     */
    private void updateUserLocation() {
        if (user != null) {
            //Add user avatar.
            mUsersDatabaseLocationRef.child("city").setValue(city);
            mUsersDatabaseLocationRef.child("county").setValue(county);
            mUsersDatabaseLocationRef.child("nbhood").setValue(neighborhood);

            addPhotoPermission = 1;

            //TO RESET THE SPINNERS AFTER SELECTION!
            ArrayAdapter<CharSequence> cityAdapterToReset = ArrayAdapter.createFromResource(NewPartyActivity.this,
                    R.array.city_array, android.R.layout.simple_spinner_item);
            cityAdapterToReset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setDropDownWidth(400);
            citySpinner.setAdapter(cityAdapterToReset);
        }
    }
    /*
    TO SET LOCATION TEXTVIEW AND TO GET FIREBASE DATABASE REFS !!! (TO SET TAB NAMES AND TO LISTEN SELECTED TABS!)
     */
    private void setLocationsAndTabs(final String city, final String county, final String neighborhood, final String refCity, final String refCounty, final String refNeighborhood, final byte locationChangedByte) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //To STOP highlighting the location selector!
            findViewById(R.id.new_party_tab_layout).setForeground(null);
            findViewById(R.id.top_layout).setForeground(null);
            findViewById(R.id.fragment).setForeground(null);
            findViewById(R.id.bottom_navigation_view).setForeground(null);
        }

        dialogNewParty.cancel();
        /*
        TO SET NBHOOD ADRESS FOR NEW PARTY FRAGMENT
         */
        String nbHoodAddress = (refCity+"/"+refCounty+"/"+refNeighborhood+"/");
        newPartyPagerAdapter = new NewPartyViewPagerFragmentAdapter(this, nbHoodAddress, uid, refCity);
        newPartyTabLayout = findViewById(R.id.new_party_tab_layout);
        newPartyViewPager = findViewById(R.id.new_party_view_pager);
        newPartyViewPager.setPageTransformer(new ZoomOutPageTransformer());
        newPartyViewPager.setAdapter(newPartyPagerAdapter);
        new TabLayoutMediator(newPartyTabLayout, newPartyViewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("G Ü N L Ü K");
                            return;
                        case 1:
                            tab.setText("S İ P A R İ Ş");
                            return;
                        case 2:
                            tab.setText("A S K I D A");
                    }
                }).attach();

        /*
        TO LISTEN SELECTED TAB !!! (TO SET FRAGMENT ACCORDINGLY AND TO ANIMATE THE TRANSITION OF FLOATING ACTION BUTTONS)
         */
        if (locationChangedByte == 1) {
            locationChanged = true;
        }
        Handler fabAnimationHandler = new Handler();
        newPartyTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (locationChanged) { // IF LOCATION CHANGED OR USER OPENS THE APP FOR THE FIRST TIME (ROTATE ANIMATION WILL BROKE SO SIMPLIFIED IT)
                    if (tab.getPosition()==1) { // If it is STORE
                        selectorButtonSwitch = 1;
                        fabAnimationHandler.removeCallbacksAndMessages(null);
                        fabAddFoodButton.shrink();
                        fabAddStoreButton.shrink();
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddFoodButton.setVisibility(View.GONE);
                            fabAddStoreButton.setVisibility(View.VISIBLE);
                        }, 200);
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddStoreButton.extend();
                        }, 400);
                    } else if (tab.getPosition()==0) { // If it is DAILY
                        selectorButtonSwitch = 2;
                        fabAnimationHandler.removeCallbacksAndMessages(null);
                        fabAddFoodButton.shrink();
                        fabAddStoreButton.shrink();
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddFoodButton.setVisibility(View.GONE);
                            fabAddStoreButton.setVisibility(View.GONE);
                            fabAddFoodButton.setIconResource(R.drawable.fab_daily_ic);
                            fabAddFoodButton.setVisibility(View.VISIBLE);
                            fabAddFoodButton.setText("Yemeğini Sat");
                        }, 200);
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddFoodButton.extend();
                        }, 400);
                    } else if (tab.getPosition()==2){ // If it is CHARITY
                        selectorButtonSwitch = 3;
                        fabAnimationHandler.removeCallbacksAndMessages(null);
                        fabAddFoodButton.shrink();
                        fabAddStoreButton.shrink();
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddFoodButton.setVisibility(View.GONE);
                            fabAddStoreButton.setVisibility(View.GONE);
                            fabAddFoodButton.setIconResource(R.drawable.fab_soup_icon);
                            fabAddFoodButton.setVisibility(View.VISIBLE);
                            fabAddFoodButton.setText("Ücretsiz Ver");
                        }, 200);
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddFoodButton.extend();
                        }, 400);
                    }
                } else { //IF USER OPEN THE APP (NOT FOR THE FIRST TIME!)
                    if (tab.getPosition()==1) { // If it is STORE
                        fabAnimationHandler.removeCallbacksAndMessages(null);
                        fabAddFoodButton.shrink();
                        fabAddStoreButton.shrink();
                        if (selectorButtonSwitch == 2) { //If user goes from daily to store
                            selectorButtonSwitch = 1;
                            fabAnimationHandler.postDelayed(() -> {
                                fabAddFoodButton.startAnimation(AnimationUtils.loadAnimation(NewPartyActivity.this, R.anim.rotate));
                                fabAnimationHandler.postDelayed(() -> {
                                    fabAddFoodButton.setVisibility(View.GONE);
                                    fabAddStoreButton.setVisibility(View.VISIBLE);
                                    fabAddStoreButton.startAnimation(AnimationUtils.loadAnimation(NewPartyActivity.this, R.anim.rotate_reverse));
                                }, 200);
                                fabAnimationHandler.postDelayed(() -> {
                                    fabAddStoreButton.extend();
                                }, 600);
                            }, 200);
                        } else { //Else if user goes from charity to store
                            selectorButtonSwitch = 1;
                            fabAddFoodButton.setVisibility(View.GONE);
                            fabAddStoreButton.setVisibility(View.VISIBLE);
                            fabAnimationHandler.postDelayed(() -> {
                                fabAddStoreButton.extend();
                            }, 200);
                        }
                    } else if (tab.getPosition()==0) { // If it is DAILY
                        fabAnimationHandler.removeCallbacksAndMessages(null);
                        selectorButtonSwitch = 2;
                        fabAddFoodButton.shrink();
                        fabAddStoreButton.shrink();
                        fabAddFoodButton.setVisibility(View.GONE);
                        fabAddStoreButton.setVisibility(View.GONE);
                        fabAddFoodButton.setVisibility(View.VISIBLE);
                        fabAddFoodButton.setIconResource(R.drawable.fab_daily_ic);
                        fabAddFoodButton.setText("Yemeğini Sat");
                        fabAnimationHandler.postDelayed(() -> {
                            fabAddFoodButton.extend();
                        }, 200);
                    } else if (tab.getPosition()==2){ // If it is CHARITY
                        fabAnimationHandler.removeCallbacksAndMessages(null);
                        fabAddFoodButton.shrink();
                        fabAddStoreButton.shrink();
                        if (selectorButtonSwitch == 1) { //If user goes from store to charity
                            selectorButtonSwitch = 3;
                            fabAnimationHandler.postDelayed(() -> {
                                fabAddStoreButton.startAnimation(AnimationUtils.loadAnimation(NewPartyActivity.this, R.anim.rotate));
                                fabAnimationHandler.postDelayed(() -> {
                                    fabAddStoreButton.setVisibility(View.GONE);
                                    fabAddFoodButton.setIconResource(R.drawable.fab_soup_icon);
                                    fabAddFoodButton.setVisibility(View.VISIBLE);
                                    fabAddFoodButton.setText("Ücretsiz Ver");
                                    fabAddFoodButton.startAnimation(AnimationUtils.loadAnimation(NewPartyActivity.this, R.anim.rotate_reverse));
                                }, 200);
                                fabAnimationHandler.postDelayed(() -> {
                                    fabAddFoodButton.extend();
                                }, 600);
                            }, 200);
                        } else { //Else if user goes from daily to charity
                            selectorButtonSwitch = 3;
                            fabAddStoreButton.setVisibility(View.GONE);
                            fabAnimationHandler.postDelayed(() -> {
                                fabAddFoodButton.startAnimation(AnimationUtils.loadAnimation(NewPartyActivity.this, R.anim.rotate));
                                fabAnimationHandler.postDelayed(() -> {
                                    fabAddFoodButton.setText("Ücretsiz Ver");
                                    fabAddFoodButton.setIconResource(R.drawable.fab_soup_icon);
                                    fabAddFoodButton.startAnimation(AnimationUtils.loadAnimation(NewPartyActivity.this, R.anim.rotate_reverse));
                                }, 200);
                                fabAnimationHandler.postDelayed(() -> {
                                    fabAddFoodButton.extend();
                                }, 600);
                            }, 200);
                        }
                    }
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        locationSelector.setVisibility(View.GONE);
        fixLocation.setVisibility(View.VISIBLE);
        editLocationIcon.setVisibility(View.VISIBLE);

        TextView cityTextView = findViewById(R.id.location_city_textview);
        TextView countyTextView = findViewById(R.id.location_county_textview);
        TextView nbhoodTextView = findViewById(R.id.location_nbhood_textview);

        cityTextView.setText(city+",");
        countyTextView.setText(county+",");
        nbhoodTextView.setText(neighborhood);

        mFoodsDailyDatabaseRef = mFoodsDailyDatabase.getReference().child("foodDailyPhotoDatabase/" + nbHoodAddress);
        mFoodsCharityDatabaseRef = mFoodsCharityDatabase.getReference().child("foodCharityPhotoDatabase/" + nbHoodAddress);

        //TO SET DB REF FOR MAP FOODS AND STORES!!!
        mMapFoodsDailyDatabaseRef = mFoodsDailyDatabase.getReference().child("mapDailyPhotoDatabase/" + refCity + "/");
        mMapFoodsCharityDatabaseRef = mFoodsCharityDatabase.getReference().child("mapCharityPhotoDatabase/" + refCity + "/");
        mMapFoodsStoreDatabaseRef = mFoodsStoreDatabase.getReference().child("mapStorePhotoDatabase/" + refCity + "/");

        addPhotoPermission = 1;
    }
    /*
    ACTIONS ON FINISH PHOTO SHOOT or PICK
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent pictureIntent) { //DEVELOPER ANDROID

        super.onActivityResult(requestCode, resultCode, pictureIntent);
        switch(requestCode) {
            case 99:
                if(resultCode == RESULT_OK){
                    addPhotoShoot();
                }
                break;
            case 88:
                if(resultCode == RESULT_OK){
                    Uri selectedImageUri = pictureIntent.getData();
                    addPhotoPicked(selectedImageUri);
                }
                break;
        }
    }
    /*
    TAKE PHOTO INTENT
     */
    private void dispatchTakePictureIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(); //Take image file name for the photo.
            } catch (IOException ex) {
                // Error occurred while creating the File
                //To show custom toast message!
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast,
                        (ViewGroup) findViewById(R.id.custom_toast_container));
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText("Bir sorun oluştu! Lütfen tekrar deneyin."+"\n\nHata Kodu: "+ex);
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.neftisoft.android.fileprovider",
                        photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    /*
    TO CREATE IMAGE FILE
     */
    private File createImageFile() throws IOException { //DEVELOPER ANDROID SOLUTION
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
        final Dialog dialogFood = ProgressDialog.show(NewPartyActivity.this, "",
                "Yemeğiniz yükleniyor...\n\n(Biraz zaman alabilir, lütfen bekleyin!)", true);

        final Uri file = Uri.fromFile(new File(currentPhotoPath));
        if (selectorButtonSwitchLast == 2) {
            final StorageReference mFoodsPhotoStorageRef = mFoodsDailyStorageRef.child(imageFileName);   // Uid/ + Current location (before the name)
            mFoodsPhotoStorageRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            getDownloadUrl(mFoodsPhotoStorageRef, file, dialogFood);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            dialogFood.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Yükleme başarısız oldu lütfen tekrar deneyin!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                            // ...
                        }
                    });
        } else if (selectorButtonSwitchLast == 3) {
            final StorageReference mFoodsPhotoStorageRef = mFoodsCharityStorageRef.child(imageFileName);   // Uid/ + Current location (before the name)
            mFoodsPhotoStorageRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            getDownloadUrl(mFoodsPhotoStorageRef, file, dialogFood);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            dialogFood.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Yükleme başarısız oldu lütfen tekrar deneyin!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                            // ...
                        }
                    });
        }
    }
    /*
    TO ADD PHOTO PICKED TO FIREBASE STORAGE
     */
    private void addPhotoPicked(Uri selectedImageUri) {
        final Dialog dialogFood = ProgressDialog.show(NewPartyActivity.this, "",
                "Yemeğiniz yükleniyor...\n\n(Biraz zaman alabilir, lütfen bekleyin!)", true);

        final Uri file = selectedImageUri; //selected image uri
        // TO Check which button is active!
        if (selectorButtonSwitchLast == 2) {
            final StorageReference mFoodsPhotoStorageRef = mFoodsDailyStorageRef.child("_" + uid + "_");   // Uid/ + Current location (before the name)

            mFoodsPhotoStorageRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            getDownloadUrl(mFoodsPhotoStorageRef, file, dialogFood);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            dialogFood.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Yükleme başarısız oldu lütfen tekrar deneyin!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                            // ...
                        }
                    });
        } else if (selectorButtonSwitchLast == 3) {
            final StorageReference mFoodsPhotoStorageRef = mFoodsCharityStorageRef.child("_" + uid + "_");   // Uid/ + Current location (before the name)

            mFoodsPhotoStorageRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            getDownloadUrl(mFoodsPhotoStorageRef, file, dialogFood);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            dialogFood.cancel();

                            //To show custom toast message!
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));
                            TextView text = layout.findViewById(R.id.toast_text);
                            text.setText("Yükleme başarısız oldu lütfen tekrar deneyin!");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                            // ...
                        }
                    });
        }
    }
    /*
    TO GET DOWNLOAD URL
     */
    private void getDownloadUrl(final StorageReference mFoodsPhotoStorageRef, Uri file, final Dialog dialogFood) {

        UploadTask uploadTask = mFoodsPhotoStorageRef.putFile(file);

        final Task<Uri> photoUrl = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                final StorageReference[] mFoodsPhotoStorageDownloadRef = {null};
                final Task<Uri>[] downloadUri = new Task[1];
                if (!task.isSuccessful()) {
                    dialogFood.cancel();
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Beklenmedik bir hata oluştu! Lütfen tekrar deneyin.");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    throw Objects.requireNonNull(task.getException());
                } else {
                    String mFoodsPhotoStorageUploadChildRefString = mFoodsPhotoStorageRef.getName();
                    mFoodsPhotoStorageUploadChildRefString = mFoodsPhotoStorageUploadChildRefString + "_1024x1024";
                    StorageReference mFoodsPhotoStorageUploadRootRef = mFoodsPhotoStorageRef.getParent();
                    Thread.sleep(2100); //WAIT FOR THE FIREBASE FUNCTION TO RESIZE THE IMAGE!!!
                    mFoodsPhotoStorageDownloadRef[0] = mFoodsPhotoStorageUploadRootRef.child(mFoodsPhotoStorageUploadChildRefString);
                    downloadUri[0] = mFoodsPhotoStorageDownloadRef[0].getDownloadUrl();
                    return downloadUri[0];
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    getUrlAndSendIt(downloadUri, dialogFood);

                } else {
                    dialogFood.cancel();
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Beklenmedik bir hata oluştu! Lütfen tekrar deneyin.");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
    }
    private void getUrlAndSendIt(Uri downloadUri, Dialog dialogFood) {
        username = user.getDisplayName();
        uid = user.getUid();

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
        //Magic for listing upside down!!!
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

        //FoodParty foodParty = new FoodParty(username,downloadUri.toString(),uid);  //OLD ONE!!!
        if (selectorButtonSwitchLast == 2) {
            DatabaseReference mFoodsDatabaseRefDailyName = mFoodsDailyDatabaseRef.child(uid + "/name");
            mFoodsDatabaseRefDailyName.setValue(username);
            DatabaseReference mFoodsDatabaseRefDailyPhotoUrl = mFoodsDailyDatabaseRef.child(uid + "/photoUrl");
            mFoodsDatabaseRefDailyPhotoUrl.setValue(downloadUri.toString());
            DatabaseReference mFoodsDatabaseRefDailyUid = mFoodsDailyDatabaseRef.child(uid + "/uid"); // Could be deleted after arranging the delete (onClick to food) action accordingly
            mFoodsDatabaseRefDailyUid.setValue(uid);
            DatabaseReference mFoodsDatabaseRefDailyKey = mFoodsDailyDatabaseRef.child(uid + "/key");
            mFoodsDatabaseRefDailyKey.setValue(itemKey);
            DatabaseReference mFoodsDatabaseRefDailyPrice = mFoodsDailyDatabaseRef.child(uid + "/price");
            mFoodsDatabaseRefDailyPrice.setValue("Pazarlık edilebilir!");
            //TO SEE FOOD PHOTO IN THE USERS PROFILE
            DatabaseReference mUsersDatabaseRefDaily = mUsersDatabaseDailyRef.child("downloadUrlDaily");
            mUsersDatabaseRefDaily.setValue(downloadUri.toString());

            //TO ADD FOOD ON MAP!!!
            DatabaseReference mMapFoodsDatabaseRefDailyLat = mMapFoodsDailyDatabaseRef.child(uid + "/lat");
            mMapFoodsDatabaseRefDailyLat.setValue(initialUserLatStr);
            DatabaseReference mMapFoodsDatabaseRefDailyLng = mMapFoodsDailyDatabaseRef.child(uid + "/lng");
            mMapFoodsDatabaseRefDailyLng.setValue(initialUserLonStr);
            DatabaseReference mMapFoodsDatabaseRefDailyName = mMapFoodsDailyDatabaseRef.child(uid + "/name");
            mMapFoodsDatabaseRefDailyName.setValue(username);
            DatabaseReference mMapFoodsDatabaseRefDailyPhotoUrl = mMapFoodsDailyDatabaseRef.child(uid + "/photoUrl");
            mMapFoodsDatabaseRefDailyPhotoUrl.setValue(downloadUri.toString());
            DatabaseReference mMapFoodsDatabaseRefDailyUid = mMapFoodsDailyDatabaseRef.child(uid + "/uid"); // Could be deleted after arranging the delete (onClick to food) action accordingly
            mMapFoodsDatabaseRefDailyUid.setValue(uid);
            DatabaseReference mMapFoodsDatabaseRefDailyPrice = mMapFoodsDailyDatabaseRef.child(uid + "/price");
            mMapFoodsDatabaseRefDailyPrice.setValue("Pazarlık edilebilir!");

            dialogFood.cancel();

            //TO ADD TAG (NAME & PRICE) ON FOOD !
            showAddFoodModalBottomDialog();

        } else if (selectorButtonSwitchLast == 3) {
            DatabaseReference mFoodsDatabaseRefCharityName = mFoodsCharityDatabaseRef.child(uid + "/name");
            mFoodsDatabaseRefCharityName.setValue(username);
            DatabaseReference mFoodsDatabaseRefCharityPhotoUrl = mFoodsCharityDatabaseRef.child(uid + "/photoUrl");
            mFoodsDatabaseRefCharityPhotoUrl.setValue(downloadUri.toString());
            DatabaseReference mFoodsDatabaseRefCharityUid = mFoodsCharityDatabaseRef.child(uid + "/uid"); // Could be deleted after arranging the delete (onClick to food) action accordingly
            mFoodsDatabaseRefCharityUid.setValue(uid);
            DatabaseReference mFoodsDatabaseRefCharityKey = mFoodsCharityDatabaseRef.child(uid + "/key");
            mFoodsDatabaseRefCharityKey.setValue(itemKey);
            DatabaseReference mFoodsDatabaseRefCharityPrice = mFoodsCharityDatabaseRef.child(uid + "/price");
            mFoodsDatabaseRefCharityPrice.setValue("Ücretsiz!");
            //TO SEE FOOD PHOTO IN THE USERS PROFILE
            DatabaseReference mUsersDatabaseRefCharity = mUsersDatabaseCharityRef.child("downloadUrlCharity");
            mUsersDatabaseRefCharity.setValue(downloadUri.toString());

            //TO ADD FOOD ON MAP!!!
            DatabaseReference mMapFoodsDatabaseRefCharityLat = mMapFoodsCharityDatabaseRef.child(uid + "/lat");
            mMapFoodsDatabaseRefCharityLat.setValue(initialUserLatStr);
            DatabaseReference mMapFoodsDatabaseRefCharityLng = mMapFoodsCharityDatabaseRef.child(uid + "/lng");
            mMapFoodsDatabaseRefCharityLng.setValue(initialUserLonStr);
            DatabaseReference mMapFoodsDatabaseRefCharityName = mMapFoodsCharityDatabaseRef.child(uid + "/name");
            mMapFoodsDatabaseRefCharityName.setValue(username);
            DatabaseReference mMapFoodsDatabaseRefCharityPhotoUrl = mMapFoodsCharityDatabaseRef.child(uid + "/photoUrl");
            mMapFoodsDatabaseRefCharityPhotoUrl.setValue(downloadUri.toString());
            DatabaseReference mMapFoodsDatabaseRefCharityUid = mMapFoodsCharityDatabaseRef.child(uid + "/uid"); // Could be deleted after arranging the delete (onClick to food) action accordingly
            mMapFoodsDatabaseRefCharityUid.setValue(uid);
            DatabaseReference mMapFoodsDatabaseRefCharityPrice = mMapFoodsCharityDatabaseRef.child(uid + "/price");
            mMapFoodsDatabaseRefCharityPrice.setValue("Ücretsiz!");

            dialogFood.cancel();

            //TO ADD TAG (NAME & PRICE) ON FOOD !
            showAddFoodModalBottomDialog();
        }
    }
    /*
    TO FIND LATITUDE-LONGITUDE VALUE OF USER NBHOOD LOCATION TO ADD FOOD ON MAP ACCORDINGLY!!!
     */
    private void setLatLngToFoods() {
        if (ActivityCompat.checkSelfPermission(NewPartyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (initialUserLatStr == null || initialUserLonStr == null) {
                Geocoder coderFood = new Geocoder(this);
                String userFoodCountyLocationStr;
                if (county.equals("Merkez")) {
                    userFoodCountyLocationStr = city;
                } else {
                    userFoodCountyLocationStr = county + ", " + city;
                }
                List<Address> foodAddress;
                try {
                    foodAddress = coderFood.getFromLocationName(userFoodCountyLocationStr, 1);
                    assert foodAddress != null;
                    Address foodLocation = foodAddress.get(0);
                    double foodLat = foodLocation.getLatitude();
                    double foodLon = foodLocation.getLongitude();

                    //TO SET A RANDOM LOCATION
                    final double maxLat = foodLat + 0.0150;
                    final double minLat = foodLat - 0.0150;
                    final double maxLon = foodLon + 0.0150;
                    final double minLon = foodLon - 0.0150;
                    Random random = new Random();
                    final double randomFoodLat = minLat + (maxLat - minLat) * random.nextDouble();
                    final double randomFoodLon = minLon + (maxLon - minLon) * random.nextDouble();

                    String latFoodStr = String.valueOf(randomFoodLat);
                    String lonFoodStr = String.valueOf(randomFoodLon);
                    //TO ADD LAT-LNG ON MAP DB REF!!!
                    if (selectorButtonSwitchLast == 2) {
                        DatabaseReference mMapFoodsDatabaseRefDailyLat = mMapFoodsDailyDatabaseRef.child(uid + "/lat");
                        mMapFoodsDatabaseRefDailyLat.setValue(latFoodStr);
                        DatabaseReference mMapFoodsDatabaseRefDailyLng = mMapFoodsDailyDatabaseRef.child(uid + "/lng");
                        mMapFoodsDatabaseRefDailyLng.setValue(lonFoodStr);
                    } else if (selectorButtonSwitchLast == 3) {
                        DatabaseReference mMapFoodsDatabaseRefCharityLat = mMapFoodsCharityDatabaseRef.child(uid + "/lat");
                        mMapFoodsDatabaseRefCharityLat.setValue(latFoodStr);
                        DatabaseReference mMapFoodsDatabaseRefCharityLng = mMapFoodsCharityDatabaseRef.child(uid + "/lng");
                        mMapFoodsDatabaseRefCharityLng.setValue(lonFoodStr);
                    }
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Yemeğiniz başarıyla yüklendi!");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //TO ASK FOR LOCATION PERMISSION
            AlertDialog.Builder builderFoodsLocation = new AlertDialog.Builder(NewPartyActivity.this);
            builderFoodsLocation.setTitle("Konum ekle");
            builderFoodsLocation.setMessage("\nYemeğiniz yüklendi!" + "\n\nYemeğinizin haritada doğru yerde görünmesi için konum izni verin!\n");
            builderFoodsLocation.setCancelable(false);
            builderFoodsLocation.setPositiveButton(Html.fromHtml("<font color='#556B2F'>Anladım</font>"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            final AlertDialog alertFoodsLocation = builderFoodsLocation.create();
            alertFoodsLocation.show();
            alertFoodsLocation.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertFoodsLocation.dismiss();
                    ActivityCompat.requestPermissions(NewPartyActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,}, 2);
                }
            });
        } else {
            // TO GET the LIVE LOCATION and SET FOOD LAT-LNG VALUE TO DATABASE !!
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location userLocation) {
                            if (userLocation != null) {
                                double latMapFood = userLocation.getLatitude();
                                double lonMapFood = userLocation.getLongitude();
                                String latMapFoodStr = String.valueOf(latMapFood);
                                String lonMapFoodStr = String.valueOf(lonMapFood);
                                //TO ADD LAT-LNG ON MAP REF!!!
                                if (selectorButtonSwitchLast == 2) {
                                    DatabaseReference mMapFoodsDatabaseRefDailyLat = mMapFoodsDailyDatabaseRef.child(uid + "/lat");
                                    mMapFoodsDatabaseRefDailyLat.setValue(latMapFoodStr);
                                    DatabaseReference mMapFoodsDatabaseRefDailyLng = mMapFoodsDailyDatabaseRef.child(uid + "/lng");
                                    mMapFoodsDatabaseRefDailyLng.setValue(lonMapFoodStr);
                                } else if (selectorButtonSwitchLast == 3) {
                                    DatabaseReference mMapFoodsDatabaseRefCharityLat = mMapFoodsCharityDatabaseRef.child(uid + "/lat");
                                    mMapFoodsDatabaseRefCharityLat.setValue(latMapFoodStr);
                                    DatabaseReference mMapFoodsDatabaseRefCharityLng = mMapFoodsCharityDatabaseRef.child(uid + "/lng");
                                    mMapFoodsDatabaseRefCharityLng.setValue(lonMapFoodStr);
                                }
                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Yemeğiniz başarıyla yüklendi!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            } else {
                                setLatLngToFoods();
                            }
                        }
                    });
        }
    }
    /*
    County Spinner Setter (Opens county list according to the selected city!)
     */
    private void setCountyLocation() {
        //County Location Setter
        countySpinner = findViewById(R.id.county_spinner);
        if (city.equals("İl seçin")) {
            ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this, //If condition to this.!
                    R.array.county_array, android.R.layout.simple_spinner_item);
            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            countySpinner.setDropDownWidth(400);
            countySpinner.setAdapter(countyAdapter);
            countySpinner.setOnItemSelectedListener(this);
        }
        else if (city.equals("Adana")) {
            ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this, //If condition to this.!
                    R.array.adana_county_array, android.R.layout.simple_spinner_item);
            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            countySpinner.setDropDownWidth(400);
            countySpinner.setAdapter(countyAdapter);
            countySpinner.setOnItemSelectedListener(this);
        }
        else if (city.equals("Zonguldak")) {
            ArrayAdapter<CharSequence> countyAdapter = ArrayAdapter.createFromResource(this, //If condition to this.!
                    R.array.zonguldak_county_array, android.R.layout.simple_spinner_item);
            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            countySpinner.setDropDownWidth(400);
            countySpinner.setAdapter(countyAdapter);
            countySpinner.setOnItemSelectedListener(this);
        }
    }
    /*
    Neighborhood Spinner Setter (Opens neighborhood list according to the selected city!)
     */
    private void setNbhoodLocation() {
        //Neighborhood Location Setter
        nbhoodSpinner = findViewById(R.id.neighborhood_spinner);
        if (county.equals("İlçe seçin") || county.isEmpty()) {
            ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                    R.array.nbhood_array, android.R.layout.simple_spinner_item);
            setIt(nbhoodAdapter);
        }
        else if (city.equals("Adana")) {
            if (county.equals("Aladağ")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_aladag_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Ceyhan")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_ceyhan_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Çukurova")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_cukurova_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Feke")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_feke_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("İmamoğlu")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_imamoglu_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Karaisalı")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_karaisali_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Karataş")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_karatas_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Kozan")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_kozan_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Pozantı")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_pozanti_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Saimbeyli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_saimbeyli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Sarıçam")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_saricam_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Seyhan")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_seyhan_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Tufanbeyli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_tufanbeyli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Yumurtalık")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_yumurtalik_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Yüreğir")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.adana_yuregir_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
        }
        else if (city.equals("Zonguldak")) {
            if (county.equals("Merkez")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_merkez_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Alaplı")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_alapli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Çaycuma")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_caycuma_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Devrek")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_devrek_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Ereğli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_eregli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Gökçebey")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_gokcebey_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Kilimli")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_kilimli_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
            else if (county.equals("Kozlu")) {
                ArrayAdapter<CharSequence> nbhoodAdapter = ArrayAdapter.createFromResource(this,
                        R.array.zonguldak_kozlu_nbhood_array, android.R.layout.simple_spinner_item);
                setIt(nbhoodAdapter);
            }
        }
    }
    private void setIt(ArrayAdapter nbhoodAdapter) {
        nbhoodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbhoodSpinner.setDropDownWidth(400);
        nbhoodSpinner.setAdapter(nbhoodAdapter);
        nbhoodSpinner.setOnItemSelectedListener(this);
    }

    /*
    MAP LOGIC
     */
    private void startGoogleMaps() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.new_party_map_fragment);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Call this when it really needed!
        mMap = googleMap;
        mMap.clear();

        /*
        MARKER CLICK LISTENER
         */
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getSnippet().equals(uid)) { //IF IT IS NOT THE USER LOCATION MARKER!
                    if (selectorButtonSwitch == 1) {
                        mMap.setInfoWindowAdapter(new StoreMapInfoWindowAdapter(NewPartyActivity.this));
                    } else {
                        mMap.setInfoWindowAdapter(new FoodsMapInfoWindowAdapter(NewPartyActivity.this));
                    }
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker((float) 120));
                } else { //ELSE IF IT IS THE USER LOCATION MARKER!
                    mMap.setInfoWindowAdapter(new UserMapInfoWindowAdapter(NewPartyActivity.this));
                }
                return false;
            }
        });

        /*
        INFO WINDOW CLICK LISTENER
         */
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                if (!marker.getSnippet().equals(uid)) { //IF IT IS NOT THE USER LOCATION MARKER!
                    if (selectorButtonSwitch == 1) { //If it is Store!
                        String mapInfoSnippet = marker.getSnippet();

                        StringTokenizer tokens = new StringTokenizer(mapInfoSnippet, "#");

                        String mapStoreOwnerUid = tokens.nextToken();
                        String mapStoreMainPhotoUrl = tokens.nextToken();
                        String mapStoreInfo = tokens.nextToken();

                        if (user != null) {
                            if (!uid.equals(mapStoreOwnerUid) && !mapStoreOwnerUid.equals("")) {
                                Intent partyStoreIntent = new Intent(NewPartyActivity.this, StoreActivity.class);
                                partyStoreIntent.putExtra("Store Other Uid", mapStoreOwnerUid);
                                NewPartyActivity.this.startActivity(partyStoreIntent);
                            }
                            else if (mapStoreOwnerUid.equals("")) {
                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Bu mutfak silinmiş! Başka mutfakları deneyin");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            } else {
                                Intent partyStoreIntent = new Intent(NewPartyActivity.this, StoreActivity.class);
                                NewPartyActivity.this.startActivity(partyStoreIntent);
                            }
                        } else {
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
                    } else { //Else if it is not Store (It is Daily or Charity)!
                        String mapOwnerName = marker.getTitle();
                        String mapInfoSnippet = marker.getSnippet();

                        StringTokenizer tokens = new StringTokenizer(mapInfoSnippet, ",");
                        String mapFoodPrice = tokens.nextToken();
                        String mapFoodPhotoUrl = tokens.nextToken();
                        String mapFoodOwnerUid = tokens.nextToken();

                        if (user != null) {
                            if (!uid.equals(mapFoodOwnerUid) && !mapFoodOwnerUid.equals("")) {
                                Intent myIntent = new Intent(NewPartyActivity.this, ChatActivity.class);
                                myIntent.putExtra("Other Uid", mapFoodOwnerUid);
                                myIntent.putExtra("Other Username", mapOwnerName);
                                NewPartyActivity.this.startActivity(myIntent);
                            }
                            else if (mapFoodOwnerUid.equals("")) {
                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Bu yemek silinmiş! Başka yemekleri deneyin");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            } else {
                                AlertDialog.Builder builderMapParty = new AlertDialog.Builder(NewPartyActivity.this);
                                builderMapParty.setTitle("Yemeği sil");
                                builderMapParty.setMessage("\nYemeğinizi silmek istediğinize emin misiniz?\n");
                                builderMapParty.setCancelable(true);

                                builderMapParty.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                if (selectorButtonSwitch == 2) {
                                                    mUsersDatabaseDailyRef.removeValue(); //To delete food in the USER'S DB
                                                    mFoodsDailyDatabaseRef.child(uid).removeValue(); //To delete food in the NBHOOD DB
                                                    mFoodsDailyStorageRef.child("_"+uid+"_").delete(); //To delete food picture in the STORAGE
                                                    mFoodsDailyStorageRef.child("_"+uid+"_"+"_1024x1024").delete(); //To delete food picture in the STORAGE
                                                    mMapFoodsDailyDatabaseRef.child(uid).removeValue(); //To delete food in the MAP DB
                                                    marker.remove();

                                                } else if (selectorButtonSwitch == 3) {
                                                    mUsersDatabaseCharityRef.removeValue(); //To delete food in the USER'S DB
                                                    mFoodsCharityDatabaseRef.child(uid).removeValue(); //To delete food in the NBHOOD DB
                                                    mFoodsCharityStorageRef.child("_"+uid+"_").delete(); //To delete food picture in the STORAGE
                                                    mFoodsCharityStorageRef.child("_"+uid+"_"+"_1024x1024").delete(); //To delete food picture in the STORAGE
                                                    mMapFoodsCharityDatabaseRef.child(uid).removeValue(); //To delete food in the MAP DB
                                                    marker.remove();
                                                }

                                                //To show custom toast message!
                                                LayoutInflater inflater = getLayoutInflater();
                                                View layout = inflater.inflate(R.layout.custom_toast,
                                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                                TextView text = layout.findViewById(R.id.toast_text);
                                                text.setText("Yemeğiniz silindi!");
                                                Toast toast = new Toast(getApplicationContext());
                                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                                toast.setDuration(Toast.LENGTH_SHORT);
                                                toast.setView(layout);
                                                toast.show();

                                                dialog.cancel();
                                            }
                                        });
                                builderMapParty.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alertMapParty = builderMapParty.create();
                                alertMapParty.show();
                            }
                        }
                        else {
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
                            //Send user to LOGIN Activity!
                        }
                    }
                } else { //ELSE IF IT IS THE USER LOCATION MARKER!
                    //GOTO PROFILE!!!
                    Intent myIntent = new Intent(NewPartyActivity.this, ProfileActivity.class);
                    NewPartyActivity.this.startActivity(myIntent);
                }
            }
        });
    }

    /*
    PERMISSION LOGIC
     */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(NewPartyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (locationPermissionAsked == 0) {
                /*
                TO ASK LOCATION PERMISSION!
                 */
                //To show custom toast message!
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast,
                        (ViewGroup) findViewById(R.id.custom_toast_container));
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText("\nYakındaki yemekleri görmek için 'konum' izni verin!\n");
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();

                //To ask for permission! (After a short delay to let user see (upper Toast message) why is permission for!)
                handlerForPermission.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActivityCompat.requestPermissions(NewPartyActivity.this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,}, 1);

                        locationPermissionAsked = 1;
                    }
                }, 1500);
            } else {
                getUserNbhoodLocation();
            }
        } else {
            getUserLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestPartyCode, @NonNull String[] partyPermissions, @NonNull int[] grantPartyResults) {
        if (requestPartyCode == 1) { //IF USER SWITCHES TO MAP MOD!!!
            if (grantPartyResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                getUserNbhoodLocation();
            }
        } else if (requestPartyCode == 2) { //IF USER ADDS FOOD!!!
            if (grantPartyResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TO GET the LIVE LOCATION and SET STORE LAT-LNG VALUE TO DATABASE !!
                fusedLocationClient.getLastLocation() //PERMISSION IS ASKED JUST BEFORE THIS LINE OF CODE!!!
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location userLocation) {
                                if (userLocation != null) {
                                    double latMapFood = userLocation.getLatitude();
                                    double lonMapFood = userLocation.getLongitude();
                                    String latMapFoodStr = String.valueOf(latMapFood);
                                    String lonMapFoodStr = String.valueOf(lonMapFood);
                                    //TO ADD LAT-LNG ON MAP REF!!!
                                    if (selectorButtonSwitchLast == 2) {
                                        DatabaseReference mMapFoodsDatabaseRefDailyLat = mMapFoodsDailyDatabaseRef.child(uid + "/lat");
                                        mMapFoodsDatabaseRefDailyLat.setValue(latMapFoodStr);
                                        DatabaseReference mMapFoodsDatabaseRefDailyLng = mMapFoodsDailyDatabaseRef.child(uid + "/lng");
                                        mMapFoodsDatabaseRefDailyLng.setValue(lonMapFoodStr);
                                    } else if (selectorButtonSwitchLast == 3) {
                                        DatabaseReference mMapFoodsDatabaseRefCharityLat = mMapFoodsCharityDatabaseRef.child(uid + "/lat");
                                        mMapFoodsDatabaseRefCharityLat.setValue(latMapFoodStr);
                                        DatabaseReference mMapFoodsDatabaseRefCharityLng = mMapFoodsCharityDatabaseRef.child(uid + "/lng");
                                        mMapFoodsDatabaseRefCharityLng.setValue(lonMapFoodStr);
                                    }

                                    //To show custom toast message!
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.custom_toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));
                                    TextView text = layout.findViewById(R.id.toast_text);
                                    text.setText("Konumunuz eklendi!");
                                    Toast toast = new Toast(getApplicationContext());
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.setDuration(Toast.LENGTH_SHORT);
                                    toast.setView(layout);
                                    toast.show();
                                } else {
                                    setLatLngToFoods();
                                }
                            }
                        });
            } else {
                //To show custom toast message!
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast,
                        (ViewGroup) findViewById(R.id.custom_toast_container));
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText("Yemeğiniz eklendi!");
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        }
    }
    private void getUserLocation() {
        // TO GET the LIVE LOCATION and SET USER POSITION MARKER ON THE MAP!!
        fusedLocationClient.getLastLocation() //PERMISSION IS CHECKED JUST BEFORE THIS LINE OF CODE!!!
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location userLocation) {
                        if (userLocation != null) {
                            lat = userLocation.getLatitude();
                            lon = userLocation.getLongitude();
                            LatLng userNEBound = new LatLng(lat+0.0800,lon+0.0800);
                            LatLng userSWBound = new LatLng(lat-0.0800,lon-0.0800);
                            LatLngBounds userMapBound = new LatLngBounds(userSWBound,userNEBound);
                            if (mapModeSwitch == 1) {
                                LatLng userLatLng = new LatLng(lat, lon);
                                userPositionMarker = new MarkerOptions()
                                        .position(new LatLng(lat, lon))
                                        .title(avatarUri)
                                        .snippet(uid)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location_icon));
                                mMap.setMaxZoomPreference(20);
                                mMap.setMinZoomPreference(13);
                                mMap.setLatLngBoundsForCameraTarget(userMapBound);
                                mMap.addMarker(userPositionMarker);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                final Handler handlerForZoomAnimation = new Handler();
                                handlerForZoomAnimation.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16),2000,null);
                                    }
                                }, 750);

                                //SHOW ALL FOODS OR STORES NEAR THE USER LOCATION!!!
                                if (selectorButtonSwitch == 2) {
                                    mMapFoodsDailyDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
                                } else if (selectorButtonSwitch == 3) {
                                    mMapFoodsCharityDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
                                } else {
                                    mMapFoodsStoreDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
                                }
                            }
                        } else {
                            getUserLocation();
                        }
                    }
                });
    }
    private void getUserNbhoodLocation() {
        // TO Get the NBHOOD LOCATION
        //To find latitude-longitude values from address and move camera to that location!
        Geocoder coder = new Geocoder(this);
        String userCountyLocationStr;
        if (county.equals("Merkez")) {
            userCountyLocationStr = city;
        } else {
            userCountyLocationStr = county + ", " + city;
        }
        List<Address> address;
        LatLng userCountyLocation;
        try {
            address = coder.getFromLocationName(userCountyLocationStr, 1);
            assert address != null;
            Address location = address.get(0);
            lat = location.getLatitude();
            lon = location.getLongitude();
            LatLng userNEBound = new LatLng(lat+0.0800,lon+0.0800);
            LatLng userSWBound = new LatLng(lat-0.0800,lon-0.0800);
            LatLngBounds userNbhoodMapBound = new LatLngBounds(userSWBound,userNEBound);

            userCountyLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.setMaxZoomPreference(20);
            mMap.setMinZoomPreference(11);
            mMap.setLatLngBoundsForCameraTarget(userNbhoodMapBound);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userCountyLocation));
            final Handler handlerForZoomAnimationToNonPermissionUser = new Handler();
            handlerForZoomAnimationToNonPermissionUser.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13),2000,null);
                }
            }, 750);

            //SHOW ALL FOODS OR STORES NEAR THE USER LOCATION!!!
            if (selectorButtonSwitch == 2) {
                mMapFoodsDailyDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
            } else if (selectorButtonSwitch == 3) {
                mMapFoodsCharityDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
            } else {
                mMapFoodsStoreDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    TO SHOW FOODS IN THE MAP!
     */
    private void setFoodInMap(double latSnapDbl, double lngSnapDbl, String mapOwnerName, String mapFoodPriceTag, String mapFoodPhotoUrl, String mapOwnerUid) {
        if (latSnapDbl > 99.000000 && lngSnapDbl > 99.000000) { // IF IT IS PSEUDO FOODS
          
            double latDistance = latSnapDbl - 100.000000;
            double lonDistance = lngSnapDbl - 100.000000;
            double pseudoLat = lat + latDistance;
            double pseudoLon = lon + lonDistance;

            LatLng pseudoFoodLatLng = new LatLng(pseudoLat, pseudoLon);
            MarkerOptions foodMarker = new MarkerOptions()
                    .position(pseudoFoodLatLng)
                    .title(mapOwnerName)
                    .snippet(mapFoodPriceTag + "," + mapFoodPhotoUrl + "," + mapOwnerUid)
                    .icon(BitmapDescriptorFactory.defaultMarker((float) 9.1413)); //HUE ANGLE OF TOMATO RED (FF6347)!!!

            mMap.addMarker(foodMarker);
        } else {
            if (abs(latSnapDbl - lat) < 0.0600 && abs(lngSnapDbl - lon) < 0.0600) { //TO SHOW THE FOODS THAT ARE NEAR THE USER! Kullanıcının erişim alanında olanları göstermek için!

                LatLng foodLatLng = new LatLng(latSnapDbl, lngSnapDbl);
                MarkerOptions foodMarker = new MarkerOptions()
                        .position(foodLatLng)
                        .title(mapOwnerName)
                        .snippet(mapFoodPriceTag + "," + mapFoodPhotoUrl + "," + mapOwnerUid)
                        .icon(BitmapDescriptorFactory.defaultMarker((float) 9.1413)); //HUE ANGLE OF TOMATO RED (FF6347)!!!

                mMap.addMarker(foodMarker);
            }
        }
    }
    /*
    TO SHOW STORES IN THE MAP!
     */
    private void setStoreInMap(double latSnapDbl, double lngSnapDbl, String mapStoreName, String mapStoreInfo, String mapStoreMainPhotoUrl, String mapStoreOwnerUid) {
        if (abs(latSnapDbl - lat) < 0.0600 && abs(lngSnapDbl - lon) < 0.0600) { ////TO SHOW THE STORES THAT ARE NEAR THE USER! (Kullanıcının erişim alanında olanları göstermek için!)
            LatLng storeLatLng = new LatLng(latSnapDbl, lngSnapDbl);
            MarkerOptions storeMarker = new MarkerOptions()
                    .position(storeLatLng)
                    .title(mapStoreName)
                    .snippet(mapStoreOwnerUid + "#" + mapStoreMainPhotoUrl + "#" + mapStoreInfo) //THIS IS TRICKY BECAUSE OF THE HASHTAG! (ESPECIALLY IF USER TYPES # IN THE STORE INFO!)
                    .icon(BitmapDescriptorFactory.defaultMarker((float) 9.1413)); //HUE ANGLE OF TOMATO RED (FF6347)!!!

            mMap.addMarker(storeMarker);
        }
    }

    /*
    TO SHOW MODAL BOTTOM DIALOG AND CHANGE LAYER OF THE MAP!
     */
    private void showMapLayerModalBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.modal_map_layers_layout);

        FrameLayout mapLayerDailyLayout = dialog.findViewById(R.id.modal_daily_frame);
        FrameLayout mapLayerStoreLayout = dialog.findViewById(R.id.modal_store_frame);
        FrameLayout mapLayerCharityLayout = dialog.findViewById(R.id.modal_charity_frame);

        //DON'T FORGET TO ADD USER PIN ON THE MAP IF HE/SHE GIVES FINE LOCATION PERMISSION!!!
        mapLayerDailyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorButtonSwitch = 2;
                mMap.clear();
                mMap.setInfoWindowAdapter(new FoodsMapInfoWindowAdapter(NewPartyActivity.this));
                if (userPositionMarker != null) {
                    mMap.addMarker(userPositionMarker);
                }
                mMapFoodsDailyDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
                dialog.dismiss();
            }
        });
        mapLayerStoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorButtonSwitch = 1;
                mMap.clear();
                mMap.setInfoWindowAdapter(new StoreMapInfoWindowAdapter(NewPartyActivity.this));
                if (userPositionMarker != null) {
                    mMap.addMarker(userPositionMarker);
                }
                mMapFoodsStoreDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
                dialog.dismiss();
            }
        });
        mapLayerCharityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorButtonSwitch = 3;
                mMap.clear();
                mMap.setInfoWindowAdapter(new FoodsMapInfoWindowAdapter(NewPartyActivity.this));
                if (userPositionMarker != null) {
                    mMap.addMarker(userPositionMarker);
                }
                mMapFoodsCharityDatabaseRef.addChildEventListener(mMapFoodChildEventListener);
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    /*
    TO SHOW MODAL BOTTOM DIALOG AND ADD FOOD TAG (NAME&PRICE)!
     */
    private void showAddFoodModalBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.modal_add_food_tag_layout);
        dialog.setCancelable(false);

        if (selectorButtonSwitchLast == 2) { //IF IT IS DAILY
            EditText modalFoodPriceEdittext = dialog.findViewById(R.id.food_price_edittext);
            EditText modalFoodNameEdittext = dialog.findViewById(R.id.food_name_edittext);
            ExtendedFloatingActionButton addNoFoodTagFab = dialog.findViewById(R.id.add_no_tag_fab);
            ExtendedFloatingActionButton addFoodTagFab = dialog.findViewById(R.id.add_tag_fab);

            addNoFoodTagFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    //TO GET AND SET LATITUDE-LONGITUDE VALUE OF USER!
                    setLatLngToFoods();
                }
            });
            addFoodTagFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String modalFoodPriceString = modalFoodPriceEdittext.getText().toString().trim();
                    String modalFoodNameString = modalFoodNameEdittext.getText().toString().toLowerCase().trim();

                    if (modalFoodPriceString.length()>0 && modalFoodNameString.length()>0) {
                        //FOOD PRICE
                        DatabaseReference mFoodsDatabaseRefDailyFoodPrice = mFoodsDailyDatabaseRef.child(uid + "/price");
                        mFoodsDatabaseRefDailyFoodPrice.setValue(modalFoodPriceString + " TL");
                        //On map!
                        DatabaseReference mMapFoodsDatabaseRefDailyFoodPrice = mMapFoodsDailyDatabaseRef.child(uid + "/price");
                        mMapFoodsDatabaseRefDailyFoodPrice.setValue(modalFoodPriceString + " TL");

                        //FOOD NAME
                        DatabaseReference mFoodsDatabaseRefDailyFoodName = mFoodsDailyDatabaseRef.child(uid + "/tag");
                        mFoodsDatabaseRefDailyFoodName.setValue(modalFoodNameString);
                        //On map!
                        DatabaseReference mMapFoodsDatabaseRefDailyFoodName = mMapFoodsDailyDatabaseRef.child(uid + "/tag");
                        mMapFoodsDatabaseRefDailyFoodName.setValue(modalFoodNameString);

                        dialog.dismiss();

                        //TO GET AND SET LATITUDE-LONGITUDE VALUE OF USER!
                        setLatLngToFoods();
                    } else {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("\nLütfen yemeğin adını ve fiyatını girin!\n");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            });
        } else {  //IF IT IS CHARITY
            TextView modalInfoDailyTextview = dialog.findViewById(R.id.modal_info_daily_textview);
            LinearLayout modalFoodPriceLayout = dialog.findViewById(R.id.modal_food_price_layout);
            TextView modalInfoCharityTextview = dialog.findViewById(R.id.modal_info_charity_textview);
            modalInfoDailyTextview.setVisibility(View.GONE);
            modalFoodPriceLayout.setVisibility(View.GONE);
            modalInfoCharityTextview.setVisibility(View.VISIBLE);
            EditText modalFoodNameEdittext = dialog.findViewById(R.id.food_name_edittext);
            ExtendedFloatingActionButton addNoFoodTagFab = dialog.findViewById(R.id.add_no_tag_fab);
            ExtendedFloatingActionButton addFoodTagFab = dialog.findViewById(R.id.add_tag_fab);

            addNoFoodTagFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    //TO GET AND SET LATITUDE-LONGITUDE VALUE OF USER!
                    setLatLngToFoods();
                }
            });
            addFoodTagFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String modalFoodNameString = modalFoodNameEdittext.getText().toString().toLowerCase().trim();

                    if (modalFoodNameString.length()>0) {
                        //FOOD NAME
                        DatabaseReference mFoodsDatabaseRefCharityFoodName = mFoodsCharityDatabaseRef.child(uid + "/tag");
                        mFoodsDatabaseRefCharityFoodName.setValue(modalFoodNameString);
                        //On map!
                        DatabaseReference mMapFoodsDatabaseRefCharityFoodName = mMapFoodsCharityDatabaseRef.child(uid + "/tag");
                        mMapFoodsDatabaseRefCharityFoodName.setValue(modalFoodNameString);

                        dialog.dismiss();

                        //TO GET AND SET LATITUDE-LONGITUDE VALUE OF USER!
                        setLatLngToFoods();
                    } else {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("\nLütfen yemeğin adını girin!\n");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            });
        }
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialogProfileBool) {
            dialogProfile.cancel();
            dialogProfileBool = false;
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        /*
        TO REFRESH IF LOCATION CHANGED BY USER (IN PROFILE ACTIVITY)
         */
        TextView cityFixedLocationTextview = findViewById(R.id.location_city_textview);
        TextView countyFixedLocationTextview = findViewById(R.id.location_county_textview);
        TextView nbhoodFixedLocationTextview = findViewById(R.id.location_nbhood_textview);
        String nbHoodAddressWithComma = cityFixedLocationTextview.getText().toString()+countyFixedLocationTextview.getText().toString()+nbhoodFixedLocationTextview.getText().toString();
        mUsersDatabaseLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userLocationSnapshot) {
                if (userLocationSnapshot.hasChild("city") && userLocationSnapshot.hasChild("county") && userLocationSnapshot.hasChild("nbhood")) {
                    city = userLocationSnapshot.child("city").getValue().toString();
                    county = userLocationSnapshot.child("county").getValue().toString();
                    neighborhood = userLocationSnapshot.child("nbhood").getValue().toString();
                    String nbHoodAddressToCompare = city + "," + county + "," + neighborhood;
                    if (!nbHoodAddressToCompare.equals(nbHoodAddressWithComma)) {
                        //Get refLocations to use them for listening the Database Reference.
                        refCity = city.replace("ç","c").replace("Ç","C")
                                .replace("ğ","g").replace("Ğ","G")
                                .replace("ı","i").replace("İ","I")
                                .replace("ö","o").replace("Ö","O")
                                .replace("ü","u").replace("Ü","U")
                                .replace("ş","s").replace("Ş","S").replace(" ","");
                        refCounty = county.replace("ç","c").replace("Ç","C")
                                .replace("ğ","g").replace("Ğ","G")
                                .replace("ı","i").replace("İ","I")
                                .replace("ö","o").replace("Ö","O")
                                .replace("ü","u").replace("Ü","U")
                                .replace("ş","s").replace("Ş","S").replace(" ","");
                        refNeighborhood = neighborhood.replace("ç","c").replace("Ç","C")
                                .replace("ğ","g").replace("Ğ","G")
                                .replace("ı","i").replace("İ","I")
                                .replace("ö","o").replace("Ö","O")
                                .replace("ü","u").replace("Ü","U")
                                .replace("ş","s").replace("Ş","S").replace(" ","");

                        setLocationsAndTabs(city, county, neighborhood, refCity, refCounty, refNeighborhood, (byte) 1); //Set location info to the related TextViews!
                    }
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("\nLütfen konumunuzu tekrar seçin!\n");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
