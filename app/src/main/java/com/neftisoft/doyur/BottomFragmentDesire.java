package com.neftisoft.doyur;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import static androidx.core.content.ContextCompat.getSystemService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomFragmentDesire#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomFragmentDesire extends Fragment {

    private final Handler handlerForLocation = new Handler();
    private final Handler handlerForCharity = new Handler();
    private final Handler handlerForSearchFoodProgressCircle = new Handler();
    private int numberOfListItem = 0;
    private String uid1;
    private DatabaseReference mDesiredFoodsDatabaseRef1;
    FusedLocationProviderClient fusedLocationClient;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BottomFragmentDesire() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BottomFragmentDesire.
     */
    // TODO: Rename and change types and number of parameters
    public static BottomFragmentDesire newInstance(String param1, String param2) {
        BottomFragmentDesire fragment = new BottomFragmentDesire();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return inflater.inflate(R.layout.fragment_bottom_desire, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        numberOfListItem = 0;
        final List<FoodParty> foodPartyList = new ArrayList<>();
        FoodsAdapter mFoodsDailyAdapter = new FoodsAdapter(getContext(), R.layout.item_food, foodPartyList);
        ListView mFoodsListView = view.findViewById(R.id.food_search_result_list_view);

        EditText searchFoodEditText = view.findViewById(R.id.search_food_edittext);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user;
        user = mFirebaseAuth.getCurrentUser();
        String uid = null;
        String username = null;
        Uri photoUrl;
        String avatarUri = null;
        final String[] city = new String[1];
        final String[] county = new String[1];
        final String[] neighborhood = new String[1];
        final String[] refCity = new String[1];
        final String[] refCounty = new String[1];
        final String[] refNeighborhood = new String[1];
        final String[] nbHoodAddress = new String[1];;
        final String[] userLatStr = {null};
        final String[] userLonStr = {null};
        final String[] itemKey = new String[1];
        if (user != null) {
            uid = user.getUid();
            uid1 = uid;
            username = user.getDisplayName();
            photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                avatarUri = photoUrl.toString();
            } else {
                //Take default avatar!
            }
        }
        String finalUid = uid;
        String finalUsername = username;
        String finalAvatarUri = avatarUri;

        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();  //To store and update user information
        DatabaseReference mUsersDatabaseRef;  //To store, update and get user information
        DatabaseReference mUsersDatabaseLocationRef;  //To store, update and get user Location Information
        final DatabaseReference[] mDesiredFoodsDatabaseRef = new DatabaseReference[1];  //To store, update and get user Desired Food
        mUsersDatabaseRef = mDatabase.getReference().child("usersDatabase/" + uid + "/");  //To store and update user information
        mUsersDatabaseLocationRef = mUsersDatabaseRef.child("locationDatabase/");

        List<FoodDesired> foodDesiredList = new ArrayList<>();
        ListView mDesiredFoodsListView = view.findViewById(R.id.desired_foods_list_view);
        DesireAdapter mDesiredFoodsAdapter;
        mDesiredFoodsAdapter = new DesireAdapter(getContext(), R.layout.item_desired, foodDesiredList);
        mDesiredFoodsListView.setAdapter(mDesiredFoodsAdapter);

        /*
        TO TAKE THE USER LOCATION!!!
         */
        mUsersDatabaseLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userLocationSnapshot) {
                if (userLocationSnapshot.hasChild("city")) {
                    city[0] = userLocationSnapshot.child("city").getValue().toString();
                    county[0] = userLocationSnapshot.child("county").getValue().toString();
                    neighborhood[0] = userLocationSnapshot.child("nbhood").getValue().toString();
                    //Get refLocations to use them for listening the Database Reference.
                    refCity[0] = city[0].replace("ç", "c").replace("Ç", "C")
                            .replace("ğ", "g").replace("Ğ", "G")
                            .replace("ı", "i").replace("İ", "I")
                            .replace("ö", "o").replace("Ö", "O")
                            .replace("ü", "u").replace("Ü", "U")
                            .replace("ş", "s").replace("Ş", "S").replace(" ", "");
                    refCounty[0] = county[0].replace("ç","c").replace("Ç","C")
                            .replace("ğ","g").replace("Ğ","G")
                            .replace("ı","i").replace("İ","I")
                            .replace("ö","o").replace("Ö","O")
                            .replace("ü","u").replace("Ü","U")
                            .replace("ş","s").replace("Ş","S").replace(" ","");
                    refNeighborhood[0] = neighborhood[0].replace("ç","c").replace("Ç","C")
                            .replace("ğ","g").replace("Ğ","G")
                            .replace("ı","i").replace("İ","I")
                            .replace("ö","o").replace("Ö","O")
                            .replace("ü","u").replace("Ü","U")
                            .replace("ş","s").replace("Ş","S").replace(" ","");

                    mDesiredFoodsDatabaseRef[0] = mDatabase.getReference().child("desiredFoodPhotoDatabase/" + refCity[0] + "/");
                    mDesiredFoodsDatabaseRef1 = mDesiredFoodsDatabaseRef[0];

                    nbHoodAddress[0] = (refCity[0]+"/"+refCounty[0]+"/"+refNeighborhood[0]+"/");

                    if (mFoodsDailyAdapter.isEmpty()) {
                        listAllFoods(mFoodsListView, mFoodsDailyAdapter, mDatabase, refCity[0]);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        TextView desireFoodNoFoodFoundTextview = view.findViewById(R.id.desire_food_no_food_found_textview);
        TextView desireFoodNoFoodDesiredTextview = view.findViewById(R.id.desire_food_no_food_desired_textview);
        ProgressBar desireFoodProgressCircle = view.findViewById(R.id.desire_food_progress_circle);

        /*
        TO SEARCH FOOD
         */
        ImageButton searchFoodButton = view.findViewById(R.id.search_button);
        searchFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFoodsDailyAdapter.clear();
                //Progress circle and not found message!
                handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
                desireFoodProgressCircle.setVisibility(View.GONE);
                desireFoodNoFoodFoundTextview.setVisibility(View.GONE);
                //TAKE SEARCHED TERM, THEN SEARCH IT!
                String searchFoodString = searchFoodEditText.getText().toString().toLowerCase().trim();
                if (searchFoodString.length()>0) {
                    //TO HIDE SOFT KEYBOARD!
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getActivity().INPUT_METHOD_SERVICE); //TO Hide keyboard!
                    assert imm != null; //TO Hide keyboard!
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //TO Hide keyboard!
                    //START LOADING CIRCLE
                    desireFoodNoFoodFoundTextview.setVisibility(View.GONE);
                    desireFoodProgressCircle.setVisibility(View.VISIBLE);
                    handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
                    handlerForSearchFoodProgressCircle.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            desireFoodProgressCircle.setVisibility(View.GONE); //TO Hide progress bar
                            desireFoodNoFoodFoundTextview.setVisibility(View.VISIBLE);
                        }
                    },3500);
                    /*
                    TO SHOW FOODS THAT CONTAIN SEARCH KEY (AND ADS) ORDERLY !!!
                     */
                    mFoodsListView.setAdapter(mFoodsDailyAdapter);
                    DatabaseReference foodDailyDatabaseRef = mDatabase.getReference("mapDailyPhotoDatabase/" + refCity[0]);
                    foodDailyDatabaseRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.hasChild("tag")) {
                                String foodTag = snapshot.child("tag").getValue().toString();
                                if (foodTag.contains(searchFoodString)) {
                                    //Progress circle and not found message!
                                    handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
                                    desireFoodProgressCircle.setVisibility(View.GONE);
                                    desireFoodNoFoodFoundTextview.setVisibility(View.GONE);
                                    //Search and listing continues from here!
                                    FoodParty foodParty = snapshot.getValue(FoodParty.class);
                                    if (numberOfListItem % 10 == 2) { //10'a bölününce 2 kalırsa.. (FoodsAdapter.java NATIVE AD position= should also changed)
                                        mFoodsDailyAdapter.add(foodParty);
                                        //TO SHOW THIS ITEM AFTER AD
                                        numberOfListItem = +1;
                                        mFoodsDailyAdapter.add(foodParty);
                                    } else {
                                        mFoodsDailyAdapter.add(foodParty);
                                    }
                                    numberOfListItem = +1;
                                } else {}
                            } else {}
                        }
                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            //DO Something if someone removes the food from list!
                            //Log.e("onChildRemoved", " Sadece burası çalışıyor");
                        }
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    handlerForCharity.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseReference foodCharityDatabaseRef = mDatabase.getReference("mapCharityPhotoDatabase/" + refCity[0]);
                            foodCharityDatabaseRef.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    if (snapshot.hasChild("tag")) {
                                        String foodTag = snapshot.child("tag").getValue().toString();
                                        if (foodTag.contains(searchFoodString)) {
                                            //Progress circle and not found message!
                                            handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
                                            desireFoodProgressCircle.setVisibility(View.GONE);
                                            desireFoodNoFoodFoundTextview.setVisibility(View.GONE);
                                            //Search and listing continues from here!
                                            FoodParty foodParty = snapshot.getValue(FoodParty.class);
                                            if (numberOfListItem % 10 == 2) { //10'a bölününce 2 kalırsa.. (FoodsAdapter.java NATIVE AD position= should also changed)
                                                mFoodsDailyAdapter.add(foodParty);
                                                //TO SHOW THIS ITEM AFTER AD
                                                numberOfListItem = +1;
                                                mFoodsDailyAdapter.add(foodParty);
                                            } else {
                                                mFoodsDailyAdapter.add(foodParty);
                                            }
                                            numberOfListItem = +1;
                                        } else {}
                                    } else {}
                                }
                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                    //DO Something if someone removes the food from list!
                                    //Log.e("onChildRemoved", " Sadece burası çalışıyor");
                                }
                                @Override
                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                    },500);
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) view.findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Aradığınız yemeğin adını yazın!");
                    Toast toast = new Toast(getContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        /*
        TO GO CHAT ROOM (AND TO DELETE selected item(food picture of search result) from DATABASES and remove from adapter if it is uploaded by user!)
         */
        mFoodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                TextView otherUid = view.findViewById(R.id.uidTextView);
                String othersUid = otherUid.getText().toString();
                TextView otherUsername = view.findViewById(R.id.nameTextView);
                String othersUsername = otherUsername.getText().toString();
                TextView price = view.findViewById(R.id.priceTextView);
                String priceString = price.getText().toString();

                if (user != null) {
                    if (!finalUid.equals(othersUid) && !othersUid.equals("")) {
                        Intent myIntent = new Intent(getContext(), ChatActivity.class);
                        myIntent.putExtra("Other Uid", othersUid);
                        myIntent.putExtra("Other Username", othersUsername);
                        requireContext().startActivity(myIntent);
                    }
                    else if (othersUid.equals("")) {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) view.findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Bu yemek silinmiş! Başka yemekleri deneyin");
                        Toast toast = new Toast(getContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    } else {
                        AlertDialog.Builder builderParty = new AlertDialog.Builder(getContext());
                        //builder1.setIcon(R.drawable.avatar);
                        builderParty.setTitle("Yemeği sil");
                        builderParty.setMessage("\nYemeğinizi listeden kaldırmak istediğinize emin misiniz?\n");
                        builderParty.setCancelable(true);
                        //final int positionToRemove = position;

                        builderParty.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        foodPartyList.remove(position);
                                        if (!priceString.equals("Ücretsiz!")) { //If it is DAILY
                                            DatabaseReference mUsersDatabaseDailyRef = mDatabase.getReference().child("usersDatabase/" + finalUid + "/" + "dailyFoodsDatabase/");
                                            DatabaseReference mFoodsDailyDatabaseRef = mDatabase.getReference().child("foodDailyPhotoDatabase/" + nbHoodAddress[0]);
                                            StorageReference mFoodsDailyStorageRef = mStorage.getReference().child("foodDailyPhotoStorage/" + finalUid + "/");
                                            DatabaseReference mMapFoodsDailyDatabaseRef = mDatabase.getReference().child("mapDailyPhotoDatabase/" + refCity[0] + "/");
                                            mFoodsDailyAdapter.notifyDataSetChanged();
                                            mUsersDatabaseDailyRef.removeValue(); //To delete food in the USER'S DB
                                            mFoodsDailyDatabaseRef.child(finalUid).removeValue(); //To delete food in the NBHOOD DB
                                            mFoodsDailyStorageRef.child("_"+finalUid+"_").delete(); //To delete food in the STORAGE
                                            mFoodsDailyStorageRef.child("_"+finalUid+"_"+"_1024x1024").delete(); //To delete food in the STORAGE (Resized version!)
                                            mMapFoodsDailyDatabaseRef.child(finalUid).removeValue(); //To delete food in the MAP DB
                                            /*
                                            Query applesQuery = mFoodsDailyDatabaseRef.orderByChild("uid").equalTo(uid); //EĞER DB KEY RANDOM İSE BU YÖNTEM KULLANILABİLİR!!!
                                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot deletedFoodSnapshot: dataSnapshot.getChildren()) {
                                                        deletedFoodSnapshot.getRef().removeValue();
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                            Query mapDailyFoodQuery = mMapFoodsDailyDatabaseRef.orderByChild("uid").equalTo(uid);
                                            mapDailyFoodQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot deletedMapFoodSnapshot: dataSnapshot.getChildren()) {
                                                        deletedMapFoodSnapshot.getRef().removeValue();
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                             */
                                        } else { //If it is CHARITY
                                            DatabaseReference mUsersDatabaseCharityRef = mDatabase.getReference().child("usersDatabase/" + finalUid + "/" + "charityFoodsDatabase/");
                                            DatabaseReference mFoodsCharityDatabaseRef = mDatabase.getReference().child("foodCharityPhotoDatabase/" + nbHoodAddress[0]);
                                            StorageReference mFoodsCharityStorageRef = mStorage.getReference().child("foodCharityPhotoStorage/" + finalUid + "/");
                                            DatabaseReference mMapFoodsCharityDatabaseRef = mDatabase.getReference().child("mapCharityPhotoDatabase/" + refCity[0] + "/");
                                            mFoodsDailyAdapter.notifyDataSetChanged();
                                            mUsersDatabaseCharityRef.removeValue(); //To delete food in the USER'S DB
                                            mFoodsCharityDatabaseRef.child(finalUid).removeValue(); //To delete food in the NBHOOD DB
                                            mFoodsCharityStorageRef.child("_"+finalUid+"_").delete(); //To delete food in the STORAGE
                                            mFoodsCharityStorageRef.child("_"+finalUid+"_"+"_1024x1024").delete(); //To delete food in the STORAGE (Resized version!)
                                            mMapFoodsCharityDatabaseRef.child(finalUid).removeValue(); //To delete food in the MAP DB
                                        }

                                        //To show custom toast message!
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate(R.layout.custom_toast,
                                                (ViewGroup) view.findViewById(R.id.custom_toast_container));
                                        TextView text = layout.findViewById(R.id.toast_text);
                                        text.setText("Yemeğiniz silindi!");
                                        Toast toast = new Toast(getContext());
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.setDuration(Toast.LENGTH_SHORT);
                                        toast.setView(layout);
                                        toast.show();

                                        dialog.cancel();
                                    }
                                });
                        builderParty.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert1Party = builderParty.create();
                        alert1Party.show();
                    }
                }
                else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) view.findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Lütfen hesabınıza tekrar giriş yapın!");
                    Toast toast = new Toast(getContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });

        /*
        TO SWITCH BETWEEN SEARCH RESULTS MODE AND DESIRED RESULTS MODE in DESIRE BOTTOM FRAGMENT!
         */
        Switch modeSwitcher = view.findViewById(R.id.desire_map_switch);
        modeSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //TO HIDE SOFT KEYBOARD!
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getActivity().INPUT_METHOD_SERVICE); //TO Hide keyboard!
                    assert imm != null;
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //TO Hide keyboard!
                    //Progress circle and not found message!
                    handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
                    desireFoodProgressCircle.setVisibility(View.GONE);
                    desireFoodNoFoodFoundTextview.setVisibility(View.GONE);
                    //DatabaseReference mDesiredFoodDatabaseRef = mDesiredFoodsDatabaseRef[0];
                    view.findViewById(R.id.search_bar).setVisibility(View.GONE);
                    view.findViewById(R.id.food_search_result_list_view).setVisibility(View.GONE);
                    view.findViewById(R.id.desired_foods_list_view).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.desired_foods_cap_layout).setVisibility(View.VISIBLE);
                    desireFoodProgressCircle.setVisibility(View.VISIBLE);
                    handlerForSearchFoodProgressCircle.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            desireFoodProgressCircle.setVisibility(View.GONE); //TO Hide progress bar
                            desireFoodNoFoodDesiredTextview.setVisibility(View.VISIBLE);
                        }
                    },3500);
                    listDesiredFoods(mDesiredFoodsDatabaseRef[0], mDesiredFoodsAdapter);
                } else {
                    handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
                    desireFoodProgressCircle.setVisibility(View.GONE);
                    desireFoodNoFoodDesiredTextview.setVisibility(View.GONE);
                    view.findViewById(R.id.desired_foods_cap_layout).setVisibility(View.GONE);
                    view.findViewById(R.id.desired_foods_list_view).setVisibility(View.GONE);
                    view.findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.food_search_result_list_view).setVisibility(View.VISIBLE);
                    mDesiredFoodsAdapter.clear();
                }
            }
        });

        /*
        TO ADD DESIRED FOOD!
         */
        ExtendedFloatingActionButton addDesire = view.findViewById(R.id.fab_add_desire_food);
        addDesire.setOnClickListener(new View.OnClickListener() {
            final Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

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
                /*
                TO SHOW POPUP MESSAGE AND TAKE USER DESIRE!
                 */
                final EditText desiredFoodEditText = new EditText(getContext());
                desiredFoodEditText.setHint("İstediğin yemeği buraya yaz");
                desiredFoodEditText.setGravity(Gravity.CENTER);
                final boolean[] checked = new boolean[]{false};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Yemek İste!")
                        .setView(desiredFoodEditText)
                        .setPositiveButton(Html.fromHtml("<font color='#556B2F'>İstek Yap</font>"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int dialogIdPositive) {
                                String desiredFoodString = desiredFoodEditText.getText().toString().trim();
                                //DO IT HERE (Kim, nereden (şehir,enlem,boylam), hangi yemeği, hangi foto ile, hangi uid ile, hangi key ile istemiş)

                                if (checked[0]) {
                                    //Take fine location and keep dancing!
                                    //To ask for permission! (After a short delay to let user see (upper Toast message) why is permission for!)
                                    requestPermissions(new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,}, 1);
                                }
                                /*
                                TO TAKE APPROXIMATE LOCATION (LAT-LNG) OF USER!
                                 */
                                Geocoder coderUser = new Geocoder(getActivity());
                                String userCountyLocationStr;
                                if (county[0].equals("Merkez")) {
                                    //userCountyLocationStr = city[0] + ", " + city[0]; //OLD ONE!
                                    userCountyLocationStr = city[0];
                                } else {
                                    userCountyLocationStr = county[0] + ", " + city[0];
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
                                    userLatStr[0] = String.valueOf(randomUserLat);
                                    userLonStr[0] = String.valueOf(randomUserLon);
                                } catch (IOException e) {
                                    //e.printStackTrace();
                                }
                                /*
                                TO GET REVERSED TIME AND DATE TO LIST STORES UPSIDE DOWN (NEW TO OLD)
                                 */
                                Calendar currentTime = Calendar.getInstance();
                                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                                int minute = currentTime.get(Calendar.MINUTE);
                                int second = currentTime.get(Calendar.SECOND);
                                String[] ids = TimeZone.getAvailableIDs();
                                long milliDiff = currentTime.get(Calendar.ZONE_OFFSET);
                                for (String id : ids) {
                                    TimeZone tz = TimeZone.getTimeZone(id);
                                    if (tz.getRawOffset() == milliDiff) {  // Found a match, now check for daylight saving
                                        boolean inDs = tz.inDaylightTime(new Date());
                                        if (inDs) {
                                            hour += 1;
                                        }
                                        if (hour == 25) {
                                            hour = 1;
                                        }
                                        break;
                                    }
                                }
                                String minuteString;
                                if (minute < 10) {
                                    minuteString = "0" + minute;
                                } else {
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
                                int inverseItemDateInt = 99991231 - Integer.parseInt(itemDate);
                                int inverseItemTimeInt = 246060 - Integer.parseInt(itemTime);
                                if (hour > 14) {
                                    itemKey[0] = inverseItemDateInt + "_0" + inverseItemTimeInt;
                                } else {
                                    itemKey[0] = inverseItemDateInt + "_" + inverseItemTimeInt;
                                }

                                //TO SAVE DESIRED FOOD IN FIREBASE!
                                olsaDaYesek(finalUid, finalUsername, finalAvatarUri, desiredFoodString, itemKey[0], userLatStr[0], userLonStr[0]);
                            }
                        })
                        .setNeutralButton(Html.fromHtml("<font color='#FF6347'>İptal Et</font>"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int dialogIdNeutral) {
                                dialog.dismiss();
                            }
                        })
                        .setMultiChoiceItems(new String[]{"\nKonumuma erişim izni ver!\n"}, checked, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int i, boolean b) {
                                checked[i] = b;
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                /*
                TO CONTROL VISIBILITY OF POSITIVE BUTTON OF ALERT DIALOG TO MAKE SURE THAT THE USER TYPES SOMETHING IN THE EDITTEXT
                 */
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                //dialog.getListView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                dialog.getListView().setVisibility(View.INVISIBLE);
                desiredFoodEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.length() > 0) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                dialog.getListView().setVisibility(View.VISIBLE);
                            }
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                            dialog.getListView().setVisibility(View.INVISIBLE);
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            }

            private void olsaDaYesek(String uid, String username, String avatarUri, String desiredFoodString, String itemKey, String userLatStr, String userLonStr) {
                DatabaseReference mDesiredFoodsDatabaseRefItemKey = mDesiredFoodsDatabaseRef[0].child(uid + "/key");
                mDesiredFoodsDatabaseRefItemKey.setValue(itemKey);
                DatabaseReference mDesiredFoodsDatabaseRefLat = mDesiredFoodsDatabaseRef[0].child(uid + "/lat");
                mDesiredFoodsDatabaseRefLat.setValue(userLatStr);
                DatabaseReference mDesiredFoodsDatabaseRefLng = mDesiredFoodsDatabaseRef[0].child(uid + "/lng");
                mDesiredFoodsDatabaseRefLng.setValue(userLonStr);
                DatabaseReference mDesiredFoodsDatabaseRefName = mDesiredFoodsDatabaseRef[0].child(uid + "/name");
                mDesiredFoodsDatabaseRefName.setValue(username);
                DatabaseReference mDesiredFoodsDatabaseRefAvatarUrl = mDesiredFoodsDatabaseRef[0].child(uid + "/avatar");
                mDesiredFoodsDatabaseRefAvatarUrl.setValue(avatarUri);
                DatabaseReference mDesiredFoodsDatabaseRefUid = mDesiredFoodsDatabaseRef[0].child(uid + "/uid"); // Could be deleted after arranging the delete (onClick to food) action accordingly
                mDesiredFoodsDatabaseRefUid.setValue(uid);
                DatabaseReference mDesiredFoodsDatabaseRefDesire = mDesiredFoodsDatabaseRef[0].child(uid + "/desire");
                mDesiredFoodsDatabaseRefDesire.setValue(desiredFoodString);
                handlerForLocation.postDelayed(new Runnable() //TEMPORARY SOLUTION!! NEED TO BE REVISED!
                {
                    @Override
                    public void run() {
                        getUsersFineLocation();
                    }
                }, 5000);
            }
        });

        /*
        TO GO CHAT ROOM (OR TO DELETE clicked item from DATABASES and remove from adapter if it is User's desire!)
         */
        mDesiredFoodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                TextView otherUid = view.findViewById(R.id.desire_uid_textview);
                String othersUid = otherUid.getText().toString();
                TextView otherUsername = view.findViewById(R.id.desired_username_textview);
                String othersUsername = otherUsername.getText().toString();

                if (user != null) {
                    if (!finalUid.equals(othersUid) && !othersUid.equals("")) {
                        Intent myIntent = new Intent(getContext(), ChatActivity.class);
                        myIntent.putExtra("Other Uid", othersUid);
                        myIntent.putExtra("Other Username", othersUsername);
                        getContext().startActivity(myIntent);
                    } else if (othersUid.equals("")) {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) view.findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Bu yemek silinmiş! Başka yemekleri deneyin");
                        Toast toast = new Toast(getContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    } else {
                        AlertDialog.Builder builderParty = new AlertDialog.Builder(getContext());
                        //builder1.setIcon(R.drawable.avatar);
                        builderParty.setTitle("Yemek isteğini sil");
                        builderParty.setMessage("\nYemek isteğinizi listeden kaldırmak istediğinize emin misiniz?\n");
                        builderParty.setCancelable(true);
                        //final int positionToRemove = position;

                        builderParty.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        foodDesiredList.remove(position);
                                        mDesiredFoodsAdapter.notifyDataSetChanged();
                                        mDesiredFoodsDatabaseRef[0].child(finalUid).removeValue(); //To delete food in the MAP DB

                                        //To show custom toast message!
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate(R.layout.custom_toast,
                                                (ViewGroup) view.findViewById(R.id.custom_toast_container));
                                        TextView text = layout.findViewById(R.id.toast_text);
                                        text.setText("Yemek isteğiniz silindi!");
                                        Toast toast = new Toast(getContext());
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.setDuration(Toast.LENGTH_SHORT);
                                        toast.setView(layout);
                                        toast.show();

                                        dialog.cancel();
                                    }
                                });
                        builderParty.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert1Party = builderParty.create();
                        alert1Party.show();
                    }
                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) view.findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Lütfen hesabınıza tekrar giriş yapın!");
                    Toast toast = new Toast(getContext());
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });

    }

    /*
    TO INITIALLY SHOW ALL DAILY FOODS (AND ADS) IN THE WHOLE CITY TO ENCOURAGE INTERACTION !!!
     */
    private void listAllFoods(ListView mFoodsListView, FoodsAdapter mFoodsDailyAdapter, FirebaseDatabase mDatabase, String refCity) {
        mFoodsListView.setAdapter(mFoodsDailyAdapter);
        DatabaseReference foodDailyDatabaseRef = mDatabase.getReference("mapDailyPhotoDatabase/" + refCity);
        foodDailyDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                numberOfListItem = +1;
                FoodParty foodParty = snapshot.getValue(FoodParty.class);
                if (numberOfListItem % 10 == 2) { //10'a bölününce 2 kalırsa.. (FoodsAdapter.java NATIVE AD position= should also changed)
                    mFoodsDailyAdapter.add(foodParty);
                    //TO SHOW THIS ITEM AFTER AD
                    numberOfListItem = +1;
                    mFoodsDailyAdapter.add(foodParty);
                } else {
                    mFoodsDailyAdapter.add(foodParty);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listDesiredFoods(DatabaseReference mDesiredFoodDatabaseRef, DesireAdapter mDesiredFoodsAdapter) {
        //FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        //DatabaseReference mDesiredFoodDatabaseRef = mDatabase.getReference().child("desiredFoodPhotoDatabase/" + refCity + "/");
        /*
        TO LIST MESSAGES IN THE CORRECT ORDER (BY DATE) IN THE LOBBY!
         */
        mDesiredFoodDatabaseRef.orderByChild("/key").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot desiredFoodDataSnapshot, @Nullable String previousChildName) {
                requireView().findViewById(R.id.desire_food_progress_circle).setVisibility(View.GONE);
                handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);

                FoodDesired foodDesired = desiredFoodDataSnapshot.getValue(FoodDesired.class);
                mDesiredFoodsAdapter.add(foodDesired);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        /*
        if (mDesiredFoodsAdapter.isEmpty()) {
            mHandler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lobbyProgressBar.setVisibility(View.GONE); //TO Hide progress bar
                    lobbyNoMessageTextView.setVisibility(View.VISIBLE);
                }
            },3500);
        }
         */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_DENIED) {
            getUsersFineLocation();
        } else {
            //Give some advise to the USER :)
        }
    }

    private void getUsersFineLocation() {
        // TO GET the LIVE LOCATION and SET USER POSITION MARKER ON THE MAP!!
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation() //PERMISSION IS CHECKED JUST BEFORE THIS LINE OF CODE!!!
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location userLocation) {
                        String userLatStr = String.valueOf(userLocation.getLatitude());
                        String userLonStr = String.valueOf(userLocation.getLongitude());
                        DatabaseReference mDesiredFoodsDatabaseRefLat = mDesiredFoodsDatabaseRef1.child(uid1 + "/lat");
                        mDesiredFoodsDatabaseRefLat.setValue(userLatStr);
                        DatabaseReference mDesiredFoodsDatabaseRefLng = mDesiredFoodsDatabaseRef1.child(uid1 + "/lng");
                        mDesiredFoodsDatabaseRefLng.setValue(userLonStr);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        handlerForLocation.removeCallbacksAndMessages(null);
        handlerForCharity.removeCallbacksAndMessages(null);
        handlerForSearchFoodProgressCircle.removeCallbacksAndMessages(null);
    }
}
