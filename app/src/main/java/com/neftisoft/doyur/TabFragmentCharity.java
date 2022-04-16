package com.neftisoft.doyur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentCharity extends Fragment {

    private View rootView;

    private String nbHoodAddress;
    private String refCity;
    private String uid;
    private FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    private int listenedThatMuch = 0;
    private int numberOfCharityListItem = 0;
    private boolean childAddedOnce = false;

    private DatabaseReference foodCharityDatabaseRef;
    private ChildEventListener charityChildEventListener;

    private final List<FoodParty> foodPartyList = new ArrayList<>();
    private ListView mFoodsListView;
    private FoodsAdapter mFoodsCharityAdapter;

    private ProgressBar tabCharityProgressCircle;
    private TextView tabCharityNoFoodTextview;
    private final Handler tabCharityProgressCircleHandler = new Handler();
    private final Handler charityStopListenerHandler = new Handler();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    // TODO: Rename and change types of parameters
    private Integer positionOfTab;

    public TabFragmentCharity() {
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
    public static TabFragmentCharity newInstance(Integer position, String address, String uid, String refCity) {
        TabFragmentCharity fragment = new TabFragmentCharity();
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
        return rootView = inflater.inflate(R.layout.fragment_tab_charity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() { //NOT onViewCreated BECAUSE Charity OR Store Creates even before it's layout is visible. (eg. Charity creates when Store is created or vice versa)
        super.onResume();
        if (!childAddedOnce) {
            tabCharityProgressCircleHandler.removeCallbacksAndMessages(null);
            tabCharityNoFoodTextview = rootView.findViewById(R.id.tab_charity_no_food_textview);
            tabCharityNoFoodTextview.setVisibility(View.GONE);
            tabCharityProgressCircle = rootView.findViewById(R.id.tab_charity_progress_circle);
            tabCharityProgressCircle.setVisibility(View.VISIBLE);
            tabCharityProgressCircleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabCharityProgressCircle.setVisibility(View.GONE);
                    tabCharityNoFoodTextview.setVisibility(View.VISIBLE);
                }
            }, 3500);
        }

        /*
        TO SHOW CHARITY FOODS (AND ADS) ORDERLY !!!
         */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        foodCharityDatabaseRef = database.getReference("foodCharityPhotoDatabase/" + nbHoodAddress);
        mFoodsListView = rootView.findViewById(R.id.charity_foods_listview);
        mFoodsCharityAdapter = new FoodsAdapter(getContext(), R.layout.item_food, foodPartyList);
        //TO LISTEN DATABASE FOR LISTING
        charityChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChild("name") && snapshot.hasChild("uid")) {
                    childAddedOnce = true;
                    mFoodsListView.setAdapter(mFoodsCharityAdapter);

                    tabCharityProgressCircleHandler.removeCallbacksAndMessages(null);
                    tabCharityNoFoodTextview.setVisibility(View.GONE);
                    tabCharityProgressCircle.setVisibility(View.GONE);

                    numberOfCharityListItem = numberOfCharityListItem + 1;
                    FoodParty foodParty = snapshot.getValue(FoodParty.class);
                    if (numberOfCharityListItem % 10 == 2) { ////OR A specific number to show ad only once.. (FoodsAdapter.java NATIVE AD position= should also be changed!)
                        mFoodsCharityAdapter.add(foodParty);

                        //TO SHOW THIS ITEM AFTER AD
                        numberOfCharityListItem = numberOfCharityListItem + 1;
                        mFoodsCharityAdapter.add(foodParty);
                    } else {
                        mFoodsCharityAdapter.add(foodParty);
                    }
                } else { //CRITIC POINT (ALWAYS BE CAREFUL) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
            foodCharityDatabaseRef.orderByChild("/key").addChildEventListener(charityChildEventListener); //TO START LISTENING DATABASE FOR LISTING!
        }

        /*
        TO GO CHAT ROOM (AND TO DELETE selected item(food picture) from DATABASES and remove from adapter if it is uploaded by user!)
         */
        AdapterView.OnItemClickListener charityItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
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
                                        DatabaseReference mUsersDatabaseCharityRef = database.getReference().child("usersDatabase/" + uid + "/" + "charityFoodsDatabase/");
                                        DatabaseReference mFoodsCharityDatabaseRef = database.getReference().child("foodCharityPhotoDatabase/" + nbHoodAddress);
                                        StorageReference mFoodsCharityStorageRef = storage.getReference().child("foodCharityPhotoStorage/" + uid + "/");
                                        DatabaseReference mMapFoodsCharityDatabaseRef = database.getReference().child("mapCharityPhotoDatabase/" + refCity + "/");
                                        mFoodsCharityAdapter.notifyDataSetChanged();
                                        mUsersDatabaseCharityRef.removeValue(); //To delete food in the USER'S DB
                                        mFoodsCharityDatabaseRef.child(uid).removeValue(); //To delete food in the NBHOOD DB
                                        mFoodsCharityStorageRef.child("_" + uid + "_").delete(); //To delete food in the STORAGE
                                        mFoodsCharityStorageRef.child("_" + uid + "_" + "_1024x1024").delete(); //To delete food in the STORAGE (Resized version!)
                                        mMapFoodsCharityDatabaseRef.child(uid).removeValue(); //To delete food in the MAP DB

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
                                        if (mFoodsCharityAdapter != null && mFoodsListView != null) {
                                            mFoodsCharityAdapter.clear();
                                            mFoodsListView.setAdapter(null);
                                        }
                                        numberOfCharityListItem = 0;
                                        childAddedOnce = false;
                                        foodCharityDatabaseRef.orderByChild("/key").addChildEventListener(charityChildEventListener);

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
        };
        mFoodsListView.setOnItemClickListener(charityItemClickListener); //TO START LISTENING ON CLICK ON A LIST ITEMS
    }

    //TO HANDLE EARLY STAGE OF ADDING FOOD (WHERE THE CHILDREN ADDED ONE-BY-ONE, SO THERE IS A POSSIBILITY OF MISSING CHILD (NAME and UID) FOR A MOMENT)
    private void handleAddingFood() {
        charityStopListenerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //TO CLEAR EVERYTHING AND START LISTENING OVER!
                if (mFoodsCharityAdapter != null && mFoodsListView != null) {
                    mFoodsCharityAdapter.clear();
                    mFoodsListView.setAdapter(null);
                }
                numberOfCharityListItem = 0;
                childAddedOnce = false;
                foodCharityDatabaseRef.orderByChild("/key").addChildEventListener(charityChildEventListener); //TO START OVER LISTENING DATABASE FOR LISTING!
                charityStopListenerHandler.removeCallbacksAndMessages(null);
            }
        },5000);
    }
}
