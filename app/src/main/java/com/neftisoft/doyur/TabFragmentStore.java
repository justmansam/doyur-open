package com.neftisoft.doyur;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.List;

public class TabFragmentStore extends Fragment {

    private View rootView;

    private String nbHoodAddress;
    private String uid;
    private FirebaseUser user;
    private FirebaseAuth mFirebaseAuth;
    private int listenedThatMuch = 0;
    private int numberOfStoreListItem = 0;
    private boolean childAddedOnce = false;

    private DatabaseReference foodStoreDatabaseRef;
    private ChildEventListener storeChildEventListener;

    private StoreAdapter mFoodsStoreAdapter;
    private ListView mStoreListView;
    private final List<FoodStore> foodStoreList = new ArrayList<>();

    private ProgressBar tabStoreProgressCircle;
    private TextView tabStoreNoFoodTextview;
    private final Handler tabStoreProgressCircleHandler = new Handler();
    private final Handler storeStopListenerHandler = new Handler();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private Integer positionOfTab;

    public TabFragmentStore() {
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
    public static TabFragmentStore newInstance(Integer position, String address, String uid) {
        TabFragmentStore fragment = new TabFragmentStore();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        args.putString(ARG_PARAM2, address);
        args.putString(ARG_PARAM3, uid);
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
        }
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return rootView = inflater.inflate(R.layout.fragment_tab_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!childAddedOnce) {
            tabStoreProgressCircleHandler.removeCallbacksAndMessages(null);
            tabStoreNoFoodTextview = rootView.findViewById(R.id.tab_store_no_store_textview);
            tabStoreNoFoodTextview.setVisibility(View.GONE);
            tabStoreProgressCircle = rootView.findViewById(R.id.tab_store_progress_circle);
            tabStoreProgressCircle.setVisibility(View.VISIBLE);
            tabStoreProgressCircleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabStoreProgressCircle.setVisibility(View.GONE);
                    tabStoreNoFoodTextview.setVisibility(View.VISIBLE);
                }
            }, 3500);
        }

        /*
        TO SHOW STORES (AND ADS) ORDERLY !!!
         */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        foodStoreDatabaseRef = database.getReference("foodStorePhotoDatabase/" + nbHoodAddress);
        mStoreListView = rootView.findViewById(R.id.store_foods_listview);
        mFoodsStoreAdapter = new StoreAdapter(getContext(), R.layout.item_store, foodStoreList);
        //TO LISTEN DATABASE FOR LISTING
        storeChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChild("storeUid")) {
                    childAddedOnce = true;
                    mStoreListView.setAdapter(mFoodsStoreAdapter);

                    tabStoreProgressCircleHandler.removeCallbacksAndMessages(null);
                    tabStoreNoFoodTextview.setVisibility(View.GONE);
                    tabStoreProgressCircle.setVisibility(View.GONE);

                    numberOfStoreListItem = numberOfStoreListItem + 1;
                    FoodStore foodStore = snapshot.getValue(FoodStore.class);
                    if (numberOfStoreListItem == 3) { ////OR A specific number to show ad only once.. (StoreAdapter.java NATIVE AD position= should also be changed!)
                        mFoodsStoreAdapter.add(foodStore);

                        //TO SHOW THIS ITEM AFTER AD
                        numberOfStoreListItem = numberOfStoreListItem + 1;
                        mFoodsStoreAdapter.add(foodStore);
                    } else {
                        mFoodsStoreAdapter.add(foodStore);
                    }
                } else { //CRITIC POINT (ALWAYS BE CAREFUL) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    if (listenedThatMuch < 11) { //TO LIMIT A POSSIBLE INFINITIVE LOOP! (IT ALSO LIMITS SHOWING STORES THAT ADDED BY THE SAME USER WITHOUT LEAVING THE ACTIVITY (WITH ~5))
                        handleAddingStore(); //TO HANDLE EARLY STAGE OF ADDING FOOD (WHEN THE CHILDREN ADDED ONE BY ONE SO THERE IS A POSSIBILITY OF MISSING CHILD (NAME and UID))
                        listenedThatMuch ++;
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //TO CLEAR EVERYTHING AND START LISTENING OVER!
                if (mFoodsStoreAdapter != null && mStoreListView != null) {
                    mFoodsStoreAdapter.clear();
                    mStoreListView.setAdapter(null);
                }
                numberOfStoreListItem = 0;
                childAddedOnce = false;
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        if (!childAddedOnce) {
            foodStoreDatabaseRef.orderByChild("/storeKey").addChildEventListener(storeChildEventListener); //TO START LISTENING DATABASE FOR LISTING!
        }

        /*
        TO GO STORE
         */
        AdapterView.OnItemClickListener storeItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                TextView onClickUid = view.findViewById(R.id.storeUidTextView);
                String onClickedUid = onClickUid.getText().toString();

                if (user != null) {
                    if (!uid.equals(onClickedUid) && !onClickedUid.equals("")) {
                        Intent partyStoreIntent = new Intent(getContext(), StoreActivity.class);
                        partyStoreIntent.putExtra("Store Other Uid", onClickedUid);
                        requireContext().startActivity(partyStoreIntent);
                    } else if (onClickedUid.equals("")) {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) view.findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Bu mutfak silinmiş! Başka mutfakları deneyin");
                        Toast toast = new Toast(getContext());
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    } else {
                        Intent partyStoreIntent = new Intent(getContext(), StoreActivity.class);
                        requireContext().startActivity(partyStoreIntent);
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
        mStoreListView.setOnItemClickListener(storeItemClickListener); //TO START LISTENING CLICK ON LIST ITEMS
    }

    //TO HANDLE EARLY STAGE OF ADDING STORE (WHERE THE CHILDREN ADDED ONE-BY-ONE, SO THERE IS A POSSIBILITY OF MISSING CHILD (UID) FOR A MOMENT)
    private void handleAddingStore() {
        storeStopListenerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //TO CLEAR EVERYTHING AND START LISTENING OVER!
                if (mFoodsStoreAdapter != null && mStoreListView != null) {
                    mFoodsStoreAdapter.clear();
                    mStoreListView.setAdapter(null);
                }
                numberOfStoreListItem = 0;
                childAddedOnce = false;
                foodStoreDatabaseRef.orderByChild("/storeKey").addChildEventListener(storeChildEventListener); //TO START OVER LISTENING DATABASE FOR LISTING!
                storeStopListenerHandler.removeCallbacksAndMessages(null);
            }
        },5000);
    }
}
