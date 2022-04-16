package com.neftisoft.doyur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentDaily extends Fragment {

    private View rootView;

    private String nbHoodAddress;
    private String refCity;
    private String uid;
    private FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    private int listenedThatMuch = 0;
    private int numberOfDailyListItem = 0;
    private int numberOfPseudoListed = 0;
    private boolean childAddedOnce = false;
    private boolean pseudoAddedOnce = false;

    private DatabaseReference foodDailyDatabaseRef;
    private DatabaseReference foodPseudoDatabaseRef;
    private ChildEventListener dailyChildEventListener;
    private ChildEventListener pseudoChildEventListener;

    private final List<FoodParty> foodPartyList = new ArrayList<>();
    private ListView mFoodsListView;
    private FoodsAdapter mFoodsDailyAdapter;

    private ProgressBar tabDailyProgressCircle;
    private TextView tabDailyNoFoodTextview;
    private final Handler tabDailyProgressCircleHandler = new Handler();
    private final Handler dailyStopListenerHandler = new Handler();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    // TODO: Rename and change types of parameters
    private Integer positionOfTab;

    public TabFragmentDaily() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p>
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     *
     * @return A new instance of fragment PartyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabFragmentDaily newInstance(Integer position, String address, String uid, String refCity) {
        TabFragmentDaily fragment = new TabFragmentDaily();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        args.putString(ARG_PARAM2, address);
        args.putString(ARG_PARAM3, uid);
        args.putString(ARG_PARAM4, refCity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            positionOfTab = getArguments().getInt(ARG_PARAM1);
            nbHoodAddress = getArguments().getString(ARG_PARAM2);
            uid = getArguments().getString(ARG_PARAM3);
            refCity = getArguments().getString(ARG_PARAM4);
        }
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return rootView = inflater.inflate(R.layout.fragment_tab_daily, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!childAddedOnce && !pseudoAddedOnce) {
            tabDailyProgressCircleHandler.removeCallbacksAndMessages(null);
            tabDailyProgressCircle = rootView.findViewById(R.id.tab_daily_progress_circle);
            tabDailyProgressCircle.setVisibility(View.GONE);
            tabDailyNoFoodTextview = rootView.findViewById(R.id.tab_daily_no_food_textview);
            tabDailyProgressCircle.setVisibility(View.VISIBLE);
            tabDailyProgressCircleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabDailyProgressCircle.setVisibility(View.GONE);
                    tabDailyNoFoodTextview.setVisibility(View.VISIBLE);
                }
            }, 3500);
        }

        /*
        TO SHOW DAILY FOODS (AND PSEUDOS, AND ADS) ORDERLY !!!
         */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        foodDailyDatabaseRef = database.getReference("foodDailyPhotoDatabase/" + nbHoodAddress);
        foodPseudoDatabaseRef = database.getReference("pseudoDailyPhotoDatabase/");
        mFoodsDailyAdapter = new FoodsAdapter(getContext(), R.layout.item_food, foodPartyList);
        mFoodsListView = rootView.findViewById(R.id.daily_foods_listview);
        //TO LISTEN DATABASE FOR LISTING
        dailyChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChild("name") && snapshot.hasChild("uid")) {

                    mFoodsListView.setAdapter(mFoodsDailyAdapter);
                    childAddedOnce = true;

                    tabDailyProgressCircleHandler.removeCallbacksAndMessages(null);
                    tabDailyNoFoodTextview.setVisibility(View.GONE);
                    tabDailyProgressCircle.setVisibility(View.GONE);

                    numberOfDailyListItem = numberOfDailyListItem + 1;
                    FoodParty foodParty = snapshot.getValue(FoodParty.class);
                    if (numberOfDailyListItem % 10 == 2) { //OR A specific number to show ad only once.. (FoodsAdapter.java NATIVE AD position= should also be changed!)
                        mFoodsDailyAdapter.add(foodParty);

                        //TO SHOW THIS ITEM AFTER AD
                        numberOfDailyListItem = numberOfDailyListItem + 1;
                        mFoodsDailyAdapter.add(foodParty);
                    } else {
                        mFoodsDailyAdapter.add(foodParty);
                    }
                } else {//CRITIC POINT (ALWAYS BE CAREFUL) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    if (listenedThatMuch < 15) { //TO LIMIT A POSSIBLE INFINITIVE LOOP! (IT ALSO LIMITS SHOWING FOODS THAT ADDED BY THE SAME USER WITHOUT LEAVING THE ACTIVITY (WITH ~5))
                        handleAddingFood(); //TO HANDLE EARLY STAGE OF ADDING FOOD (WHEN THE CHILDREN ADDED ONE BY ONE SO THERE IS A POSSIBILITY OF MISSING CHILD (NAME and UID))
                        listenedThatMuch ++;
                    }
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
        };
        //TO PREVENT LISTENING AND LISTING EVERY TIME FRAGMENT RESUMES!
        if (!childAddedOnce) {
            foodDailyDatabaseRef.orderByChild("/key").addChildEventListener(dailyChildEventListener); //TO START LISTENING DATABASE FOR LISTING!
        }
        /*
        TO SHOW PSEUDO FOODS IF THERE IS NO REAL FOOD (OR LESS THAN 2 FOODS) IN THE NEIGHBORHOOD!!!
         */
        pseudoChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot pseudoDataSnapshot, @Nullable String previousChildName) {
                if (numberOfPseudoListed < 3) {
                    numberOfPseudoListed ++;

                    mFoodsListView.setAdapter(mFoodsDailyAdapter);
                    pseudoAddedOnce = true;

                    tabDailyProgressCircleHandler.removeCallbacksAndMessages(null);
                    tabDailyNoFoodTextview.setVisibility(View.GONE);
                    tabDailyProgressCircle.setVisibility(View.GONE);

                    FoodParty pseudoFoodParty = pseudoDataSnapshot.getValue(FoodParty.class);
                    if (numberOfDailyListItem % 10 == 2) { //OR A specific number to show ad only once
                        mFoodsDailyAdapter.add(pseudoFoodParty);

                        //TO SHOW SECOND ITEM
                        numberOfDailyListItem = numberOfDailyListItem + 1;
                        mFoodsDailyAdapter.add(pseudoFoodParty);
                    } else {
                        mFoodsDailyAdapter.add(pseudoFoodParty);
                    }
                    numberOfDailyListItem = numberOfDailyListItem + 1;
                } else if (numberOfPseudoListed == 0) {
                    pseudoAddedOnce = false;
                    foodPseudoDatabaseRef.removeEventListener(pseudoChildEventListener);
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
        };
        if (!pseudoAddedOnce) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (numberOfDailyListItem < 2) {
                        foodPseudoDatabaseRef.addChildEventListener(pseudoChildEventListener); //TO START LISTENING PSEUDO DATABASE FOR LISTING!
                    }
                }
            },300);
        }

        /*
        TO GO CHAT ROOM (AND TO DELETE selected item(food picture) from DATABASES and remove from adapter if it is uploaded by user!)
         */
        mFoodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                TextView otherUid = view.findViewById(R.id.uidTextView);
                String othersUid = otherUid.getText().toString();
                TextView otherUsername = view.findViewById(R.id.nameTextView);
                String othersUsername = otherUsername.getText().toString();

                if (user != null) {
                    if (!uid.equals(othersUid) && !othersUid.equals("")) {
                        Intent myIntent = new Intent(getContext(), ChatActivity.class);
                        myIntent.putExtra("Other Uid", othersUid);
                        myIntent.putExtra("Other Username", othersUsername);
                        requireContext().startActivity(myIntent);
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
                        builderParty.setTitle("Yemeği sil");
                        builderParty.setMessage("\nYemeğinizi listeden kaldırmak istediğinize emin misiniz?\n");
                        builderParty.setCancelable(true);

                        builderParty.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        foodPartyList.remove(position);
                                        DatabaseReference mUsersDatabaseDailyRef = database.getReference().child("usersDatabase/" + uid + "/" + "dailyFoodsDatabase/");
                                        DatabaseReference mFoodsDailyDatabaseRef = database.getReference().child("foodDailyPhotoDatabase/" + nbHoodAddress);
                                        StorageReference mFoodsDailyStorageRef = storage.getReference().child("foodDailyPhotoStorage/" + uid + "/");
                                        DatabaseReference mMapFoodsDailyDatabaseRef = database.getReference().child("mapDailyPhotoDatabase/" + refCity + "/");
                                        mFoodsDailyAdapter.notifyDataSetChanged();
                                        mUsersDatabaseDailyRef.removeValue(); //To delete food in the USER'S DB
                                        mFoodsDailyDatabaseRef.child(uid).removeValue(); //To delete food in the NBHOOD DB
                                        mFoodsDailyStorageRef.child("_" + uid + "_").delete(); //To delete food in the STORAGE
                                        mFoodsDailyStorageRef.child("_" + uid + "_" + "_1024x1024").delete(); //To delete food in the STORAGE (Resized version!)
                                        mMapFoodsDailyDatabaseRef.child(uid).removeValue(); //To delete food in the MAP DB

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

                                        //TO CLEAR EVERYTHING AND START LISTENING OVER!
                                        if (mFoodsDailyAdapter != null && mFoodsListView != null) {
                                            mFoodsDailyAdapter.clear();
                                            mFoodsListView.setAdapter(null);
                                        }
                                        numberOfDailyListItem = 0;
                                        childAddedOnce = false;
                                        foodDailyDatabaseRef.orderByChild("/key").addChildEventListener(dailyChildEventListener);
                                        numberOfPseudoListed = 0;
                                        if (pseudoChildEventListener != null) {
                                            //TO START LISTENING PSEUDO DATABASE FOR LISTING!
                                            pseudoAddedOnce = false;
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (numberOfDailyListItem < 2) {
                                                        foodPseudoDatabaseRef.addChildEventListener(pseudoChildEventListener); //TO START LISTENING PSEUDO DATABASE FOR LISTING!
                                                    }
                                                }
                                            },300);
                                        }

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

    private void handleAddingFood() {
        dailyStopListenerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //TO CLEAR EVERYTHING AND START LISTENING OVER!
                if (mFoodsDailyAdapter != null && mFoodsListView != null) {
                    mFoodsDailyAdapter.clear();
                    mFoodsListView.setAdapter(null);
                }
                numberOfDailyListItem = 0;
                childAddedOnce = false;
                foodDailyDatabaseRef.orderByChild("/key").addChildEventListener(dailyChildEventListener); //TO START OVER LISTENING DATABASE FOR LISTING!
                numberOfPseudoListed = 0;
                if (pseudoChildEventListener != null) {
                    //TO START LISTENING PSEUDO DATABASE FOR LISTING!
                    pseudoAddedOnce = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (numberOfDailyListItem < 2) {
                                foodPseudoDatabaseRef.addChildEventListener(pseudoChildEventListener); //TO START LISTENING PSEUDO DATABASE FOR LISTING!
                            }
                        }
                    },300);
                }

                dailyStopListenerHandler.removeCallbacksAndMessages(null);
            }
        },5000);
    }
}
