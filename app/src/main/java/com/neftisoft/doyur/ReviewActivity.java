package com.neftisoft.doyur;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReviewActivity extends AppCompatActivity {

    FirebaseUser user;

    List<FoodReview> foodReviewList = new ArrayList<>();

    private ReviewAdapter mReviewAdapter;
    private AdView mReviewAdView;

    private FirebaseDatabase mReviewDatabase;
    private FirebaseDatabase mReportDatabase;

    private DatabaseReference mReportDatabaseRef;
    private DatabaseReference mReportDatabaseReportRef;
    private DatabaseReference mReportDatabaseReporterNameRef;
    private DatabaseReference mReportDatabaseReporterEmailRef;
    private DatabaseReference mReportDatabaseReviewRef;
    private DatabaseReference mReportDatabaseReportedDateRef;

    private DatabaseReference mUsersDatabaseRatingTotalPointRef;
    private DatabaseReference mUsersDatabaseRatingTotalPersonRef;

    private DatabaseReference mOthersReviewDatabaseRef;
    private ChildEventListener mOthersReviewChildEventListener;

    private DatabaseReference mMyReviewsDatabaseRef;
    private DatabaseReference mMyReviewDatabaseReviewRef;
    private DatabaseReference mMyReviewDatabasePointRef;
    private ValueEventListener mUsersDatabaseRatingTotalPersonValueEventListener;
    private ValueEventListener mUsersDatabaseRatingTotalPointValueEventListener;

    private FirebaseAuth mFirebaseAuth;
    private Uri avatarUrl;

    private String username;
    private String email;
    private String uid;
    private String otherUid;
    private String reviewedUid;
    private String myPoint;
    private String reviewToReport;

    private int myPointInt;
    private int reviewedBefore = 0; //To know if user reviewed before (0=no, 1=yes)
    private int ratingTotalPointInt;
    private int ratingTotalPersonInt;

    private TextView reviewHeadingTextView;
    private TextView finalRatingTextView;
    private TextView noReviewTextView;

    private EditText reviewEditText;
    private EditText reviewReportEditText;

    private ListView mReviewsListView;
    private ProgressBar progressCircle;
    private LinearLayout reviewItemLinearLayout;
    private FloatingActionButton fabRateReview;

    private ScrollView ratingReviewScrollView;
    private ScrollView reportReviewScrollView;

    private SeekBar ratingSeekBar;
    private Handler mHandler11 = new Handler();

    private InputMethodManager immReview;
    private InputMethodManager immReport;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // TO Change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) { //For grey status bar!
            Window window = ReviewActivity.this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //For grey status bar!
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //For grey status bar!
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(ReviewActivity.this,R.color.colorNavigationBar)); //For grey status bar!
        }

        //TO INITIALIZE ADMOB ADS!!!
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //TO SHOW ADMOD ADS!!!
        mReviewAdView = findViewById(R.id.review_ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mReviewAdView.loadAd(adRequest);
        //TO LISTEN ADMOB ADS!!!
        mReviewAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {}
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {}
            @Override
            public void onAdOpened() {}
            @Override
            public void onAdClicked() {}
            @Override
            public void onAdClosed() {}
        });

        // User comes from PartyActivity (by Clicking on any Store as a Guest)
        Intent profileReviewIntent = getIntent();
        otherUid = profileReviewIntent.getStringExtra("Profile Uid");

        mFirebaseAuth =FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        if (user != null) {
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = user.getUid();

            // Name, email address, and profile photo Url
            username = user.getDisplayName();
            email = user.getEmail();
            avatarUrl = user.getPhotoUrl();
        }

        //TO Initialize View Groups
        reviewHeadingTextView = findViewById(R.id.review_heading_textview);
        noReviewTextView = findViewById(R.id.review_no_rvw_textview);
        progressCircle = findViewById(R.id.review_progress_circle);
        reviewItemLinearLayout = findViewById(R.id.review_item_linear_layout);
        fabRateReview = findViewById(R.id.fab_rate_review);
        ratingReviewScrollView = findViewById(R.id.rating_review_scroll_view);
        reportReviewScrollView = findViewById(R.id.report_scroll_view);
        reviewReportEditText = findViewById(R.id.review_report_edittext);

        if (otherUid != null) {
            if (!otherUid.equals(uid)) {
                reviewedUid = otherUid;

                //TO Give permission to review if two people chat before !!!
                FirebaseDatabase mLobbyDatabase = FirebaseDatabase.getInstance();
                DatabaseReference mMyLobbyDatabaseRef = mLobbyDatabase.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/" + otherUid);
                mMyLobbyDatabaseRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            fabRateReview.setVisibility(View.VISIBLE); // IF AND ONLY IF USER HAS A CONTACT WITH THE PROFILE OWNER (LIKE IF CHAT BEFORE!!!) !!!
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
                });
                DatabaseReference mOtherLobbyDatabaseRef = mLobbyDatabase.getReference().child("usersDatabase/" + otherUid + "/" + "lobbyDatabase/" + uid); //In case user deletes the lobby item.
                mOtherLobbyDatabaseRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            fabRateReview.setVisibility(View.VISIBLE); // IF AND ONLY IF USER HAS A CONTACT WITH THE PROFILE OWNER (LIKE IF CHAT BEFORE!!!) !!!
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
                });

                if (reviewItemLinearLayout != null) {
                    reviewItemLinearLayout.setClickable(false);
                }
            } else {
                reviewedUid = uid;
            }
        } else {
            reviewedUid = uid;
        }
        // Initialize foods ListView and its adapter
        mReviewsListView = findViewById(R.id.reviewListView);
        mReviewAdapter = new ReviewAdapter(this, R.layout.item_review, foodReviewList);
        mReviewsListView.setAdapter(mReviewAdapter);

        /*
        TO LISTEN ANY CHANGE AND UPDATE REVIEW LIST
         */
        mReviewDatabase = FirebaseDatabase.getInstance();
        mOthersReviewDatabaseRef = mReviewDatabase.getReference().child("usersDatabase/" + reviewedUid + "/" + "ratingReviewDatabase/otherReviews/");
        mOthersReviewChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot reviewDataSnapshot, @Nullable String s) {
                FoodReview foodReview = reviewDataSnapshot.getValue(FoodReview.class);
                mReviewAdapter.add(foodReview);
                progressCircle.setVisibility(View.GONE); //TO Hide progress bar
                noReviewTextView.setVisibility(View.GONE);
                mHandler11.removeCallbacksAndMessages(null);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot newReviewDataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot removedReviewDataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        mOthersReviewDatabaseRef.orderByChild("/reviewKey").addChildEventListener(mOthersReviewChildEventListener);

        /*
        TO HIDE PROGRESS AND SHOW "NO REVIEW" MESSAGE IF THERE IS NONE !!!
         */
        if (mReviewAdapter.isEmpty()) {
            mHandler11.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressCircle.setVisibility(View.GONE); //TO Hide progress bar
                    noReviewTextView.setVisibility(View.VISIBLE);
                }
            },3500);
        }

        /*
        TO CALCULATE FINAL RATING OF REVIEWED USER !!!
         */
        DatabaseReference mUsersDatabaseRatingRef = mReviewDatabase.getReference().child("usersDatabase/" + reviewedUid + "/" + "ratingReviewDatabase/");
        mUsersDatabaseRatingTotalPointRef = mUsersDatabaseRatingRef.child("totalPoint/");
        mUsersDatabaseRatingTotalPersonRef = mUsersDatabaseRatingRef.child("totalPerson/");
        mUsersDatabaseRatingTotalPointValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingTotPointSnapshot) {
                try {
                    if (ratingTotPointSnapshot.getValue() != null) {
                        try {
                            String ratingTotalPoint = ratingTotPointSnapshot.getValue().toString();
                            ratingTotalPointInt = Integer.parseInt(ratingTotalPoint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //ratingTotalPointInt = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError ratingTotPointError) {
                //
            }
        };
        mUsersDatabaseRatingTotalPointRef.addValueEventListener(mUsersDatabaseRatingTotalPointValueEventListener);
        mUsersDatabaseRatingTotalPersonValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ratingTotPersonSnapshot) {
                try {
                    if (ratingTotPersonSnapshot.getValue() != null) {
                        try {
                            String ratingTotalPerson = ratingTotPersonSnapshot.getValue().toString();
                            ratingTotalPersonInt = Integer.parseInt(ratingTotalPerson);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //ratingTotalPersonInt = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //
            }
        };
        mUsersDatabaseRatingTotalPersonRef.addValueEventListener(mUsersDatabaseRatingTotalPersonValueEventListener);

        /*
        TO GET REVIEWER RATE (POINT) IF THERE IS ONE (to subtract it from total rating anytime) !!!
         */
        mMyReviewsDatabaseRef = mReviewDatabase.getReference().child("usersDatabase/" + uid + "/" + "ratingReviewDatabase/myReviews/" + reviewedUid + "/");
        mMyReviewDatabasePointRef = mMyReviewsDatabaseRef.child("point");
        mMyReviewDatabasePointRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot pointDataSnapshot) {
                try {
                    if (pointDataSnapshot.getValue() != null) {
                        try {
                            myPoint = pointDataSnapshot.getValue().toString();
                            myPointInt = Integer.parseInt(myPoint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //
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
        TO DELETE REVIEW OR SEND USER TO REVIEWER PROFILE !!!
         */
        mReviewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                TextView reviewerUidTextview = view.findViewById(R.id.review_uid_textview);
                final String reviewerUid = reviewerUidTextview.getText().toString(); //TO Get the UID that is reported!
                if (user != null) {
                    if (reviewerUid.equals(null)) {
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Bu yorum silinmiş!");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    } else if (reviewedUid.equals(uid)) {
                        //TO ASK USER IF HE/SHE WANTS TO REPORT AN INAPPROPRIATE REVIEW OR GO TO THE REVIEWERS PROFILE
                        AlertDialog.Builder builderReviewer = new AlertDialog.Builder(ReviewActivity.this);
                        builderReviewer.setTitle("Bu yorumla ne yapmak istiyorsunuz?\n");
                        builderReviewer.setCancelable(true);
                        builderReviewer.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Yorumu şikayet et</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        TextView reviewTextView = view.findViewById(R.id.review_textview);
                                        reviewToReport = reviewTextView.getText().toString(); //TO Get the review that is reported!
                                        mReportDatabase = FirebaseDatabase.getInstance();
                                        mReportDatabaseRef = mReportDatabase.getReference().child("reportsDatabase/" + "reviewReports/" + uid + "/" + reviewerUid + "/");
                                        mReviewsListView.setVisibility(View.GONE);
                                        mReviewAdView.setVisibility(View.GONE);
                                        reportReviewScrollView.setVisibility(View.VISIBLE);
                                        reviewHeadingTextView.setText("Uygunsuz yorum bildir");
                                        reviewReportEditText.requestFocus(); //TO Get focus on the editText programmatically!
                                        immReport = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
                                        if (immReport != null) {
                                            immReport.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
                                        }
                                        dialog.cancel();
                                    }
                                });
                        builderReviewer.setNegativeButton(Html.fromHtml("<font color='#556B2F'>Profili ziyaret et</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent myIntent = new Intent(ReviewActivity.this, ProfileActivity.class);
                                        myIntent.putExtra("Reviewer Uid", reviewerUid);
                                        ReviewActivity.this.startActivity(myIntent);
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertReviewer = builderReviewer.create();
                        alertReviewer.show();
                    } else if (reviewerUid.equals(uid)) {
                        AlertDialog.Builder builderReview = new AlertDialog.Builder(ReviewActivity.this);
                        builderReview.setTitle("Yorumu sil");
                        builderReview.setMessage("\nYorumunuzu listeden kaldırmak istediğinize emin misiniz?\n");
                        builderReview.setCancelable(true);

                        builderReview.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //TO Subtract deleted rate and a person from database & reListen to reset the total point and total person values !!!
                                        ratingTotalPointInt = ratingTotalPointInt - myPointInt;
                                        mUsersDatabaseRatingTotalPointRef.setValue(ratingTotalPointInt);
                                        ratingTotalPersonInt = ratingTotalPersonInt - 1;
                                        mUsersDatabaseRatingTotalPersonRef.setValue(ratingTotalPersonInt);

                                        //TO Delete reviews from all databases !!!
                                        foodReviewList.remove(position); //TO Remove review from list
                                        mReviewAdapter.notifyDataSetChanged();
                                        Query reviewsQuery = mOthersReviewDatabaseRef.orderByKey().equalTo(uid);
                                        reviewsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot deletedReviewSnapshot: dataSnapshot.getChildren()) {
                                                    deletedReviewSnapshot.getRef().removeValue(); //TO Remove review from database
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        DatabaseReference mUsersDatabaseRatingMyReviewsRef = mReviewDatabase.getReference().child("usersDatabase/" + uid + "/" + "ratingReviewDatabase/myReviews/");
                                        Query myReviewsQuery = mUsersDatabaseRatingMyReviewsRef.orderByKey().equalTo(reviewedUid);
                                        myReviewsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot deletedMyReviewSnapshot: dataSnapshot.getChildren()) {
                                                    deletedMyReviewSnapshot.getRef().removeValue(); //TO Remove review from database
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });

                                        myPoint = null; //TO Avoid remembering last deleted review and rate (point).
                                        reviewedBefore = 0; //TO Avoid remembering last deleted review and rate (point).
                                        //To show custom toast message!
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate(R.layout.custom_toast,
                                                (ViewGroup) findViewById(R.id.custom_toast_container));
                                        TextView text = layout.findViewById(R.id.toast_text);
                                        text.setText("Yorumunuz silindi!");
                                        Toast toast = new Toast(getApplicationContext());
                                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                                        toast.setDuration(Toast.LENGTH_SHORT);
                                        toast.setView(layout);
                                        toast.show();

                                        dialog.cancel();
                                    }
                                });
                        builderReview.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertReview = builderReview.create();
                        alertReview.show();
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
            }
        });
    }

    /*
    TO GET BACK !!!
     */
    public void getBack(View view) {
        if (immReview != null) {
            reviewEditText.setText("");
            reviewEditText.clearFocus();
            immReview.hideSoftInputFromWindow(reviewEditText.getWindowToken(),0);
        }
        if (immReport != null) {
            reviewReportEditText.setText("");
            reviewReportEditText.clearFocus();
            immReport.hideSoftInputFromWindow(reviewReportEditText.getWindowToken(),0);
        }
        onBackPressed();
    }

    /*
    TO ADD REVIEW
     */
    @SuppressLint("RestrictedApi")
    public void addReview(View view) {
        mReviewAdView.setVisibility(View.GONE);
        mReviewsListView.setVisibility(View.GONE);
        progressCircle.setVisibility(View.GONE); //TO Hide progress bar
        noReviewTextView.setVisibility(View.GONE);
        fabRateReview.setVisibility(View.GONE);
        mHandler11.removeCallbacksAndMessages(null);
        ratingReviewScrollView.setVisibility(View.VISIBLE);

        ratingSeekBar = findViewById(R.id.rating_seek_bar);
        finalRatingTextView = findViewById(R.id.final_rating_textview);
        reviewEditText = findViewById(R.id.review_edit_text);

        reviewEditText.requestFocus(); //TO Get focus on the editText programmatically!
        immReview = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //TO Show keyboard!
        if (immReview != null) {
            immReview.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //TO Show keyboard!
        }

        /*
        TO SET OLDER REVIEW IF THERE IS ONE !!!
         */
        mMyReviewsDatabaseRef = mReviewDatabase.getReference().child("usersDatabase/" + uid + "/" + "ratingReviewDatabase/myReviews/" + reviewedUid + "/");
        mMyReviewDatabaseReviewRef = mMyReviewsDatabaseRef.child("review");
        mMyReviewDatabaseReviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot reviewDataSnapshot) {
                try {
                    if (reviewDataSnapshot.getValue() != null && myPoint != null) {
                        try {
                            reviewEditText.setText("");
                            String myReview = reviewDataSnapshot.getValue().toString();
                            reviewEditText.setText(myReview);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        if (myPoint != null) {
            int myFinalPointInt = myPointInt - 1; //To calculate progress which is one less than the real point!
            finalRatingTextView.setText(myPoint);
            ratingSeekBar.setProgress(myFinalPointInt);
            reviewedBefore = 1;
        }

        /*
        TO GET THE VALUE OF SEEK BAR !!!
         */
        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int finalRating = progress + 1;
                String finalRatingString = String.valueOf(finalRating);
                finalRatingTextView.setText(finalRatingString);
                if (finalRating == 10) {
                    finalRatingTextView.setTextColor(Color.parseColor("#1B5E20"));
                }
                else if (finalRating == 9) {
                    finalRatingTextView.setTextColor(Color.parseColor("#66BB6A"));
                }
                else if (finalRating == 8) {
                    finalRatingTextView.setTextColor(Color.parseColor("#D4E157"));
                }
                else if (finalRating == 7) {
                    finalRatingTextView.setTextColor(Color.parseColor("#FFEE58"));
                }
                else if (finalRating == 6) {
                    finalRatingTextView.setTextColor(Color.parseColor("#FFA726"));
                }
                else if (finalRating == 5) {
                    finalRatingTextView.setTextColor(Color.parseColor("#FF7043"));
                }
                else if (5 > finalRating) {
                    finalRatingTextView.setTextColor(Color.parseColor("#BF360C"));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /*
    TO CANCEL NEW REVIEW
     */
    @SuppressLint("RestrictedApi")
    public void cancelNewReview(View view) {
        reviewEditText.clearFocus();
        immReview = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
        if (immReview != null) {
            immReview.hideSoftInputFromWindow(reviewEditText.getWindowToken(), 0); //TO Hide keyboard!
        }
        ratingSeekBar.setProgress(0);
        reviewEditText.setText("");
        ratingReviewScrollView.setVisibility(View.GONE);
        mReviewsListView.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.VISIBLE);
        fabRateReview.setVisibility(View.VISIBLE);
        mReviewAdView.setVisibility(View.VISIBLE);
        if (mReviewAdapter.isEmpty()) {
            mHandler11.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressCircle.setVisibility(View.GONE); //TO Hide progress bar
                    noReviewTextView.setVisibility(View.VISIBLE);
                }
            },3500);
        } else {
            progressCircle.setVisibility(View.GONE);
        }
    }

    /*
    TO SAVE NEW REVIEW
     */
    public void saveNewReview(View view) {
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

        String rating = finalRatingTextView.getText().toString().trim();
        String review = reviewEditText.getText().toString().trim();

        //BEFORE SAVING ASK TWICE IF USER RATED 1 !!! (Could make it accidently)
        checkGivenInformationAndSaveReview(rating, review, itemKey);
    }
    @SuppressLint("RestrictedApi")
    private void checkGivenInformationAndSaveReview(String rating, String review, String itemKey) {
        String avatarUri;
        avatarUri = avatarUrl.toString();

        // TO Get the date
        String dateOfToday;
        dateOfToday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());

        // TO Send info (avatar, username, point, review, day-hour, uid) to otherReviews (Ref=reviewedUid)
        DatabaseReference mOthersReviewDatabaseFinalRef = mOthersReviewDatabaseRef.child(uid);
        DatabaseReference mOthersReviewDatabaseFinalAvatarRef = mOthersReviewDatabaseFinalRef.child("reviewerAvatar");
        mOthersReviewDatabaseFinalAvatarRef.setValue(avatarUri);
        DatabaseReference mOthersReviewDatabaseFinalUsernameRef = mOthersReviewDatabaseFinalRef.child("reviewerName");
        mOthersReviewDatabaseFinalUsernameRef.setValue(username);
        DatabaseReference mOthersReviewDatabaseFinalPointRef = mOthersReviewDatabaseFinalRef.child("reviewerRate");
        mOthersReviewDatabaseFinalPointRef.setValue(rating);
        DatabaseReference mOthersReviewDatabaseFinalReviewRef = mOthersReviewDatabaseFinalRef.child("review");
        mOthersReviewDatabaseFinalReviewRef.setValue(review);
        DatabaseReference mOthersReviewDatabaseFinalDateRef = mOthersReviewDatabaseFinalRef.child("dateNow");
        mOthersReviewDatabaseFinalDateRef.setValue(dateOfToday);
        DatabaseReference mOthersReviewDatabaseFinalUidRef = mOthersReviewDatabaseFinalRef.child("reviewerUid");
        mOthersReviewDatabaseFinalUidRef.setValue(uid);
        DatabaseReference mOthersReviewDatabaseFinalKeyRef = mOthersReviewDatabaseFinalRef.child("reviewKey");
        mOthersReviewDatabaseFinalKeyRef.setValue(itemKey);
      
        refreshReviews();

        // TO Send info (point, review, reviewedUid) to myReviews (Ref=uid)
        mMyReviewDatabaseReviewRef.setValue(review);
        mMyReviewDatabasePointRef.setValue(rating);

        // TO Add 1 to totalPerson and 1 to totalPoint databases
        int ratingInt = Integer.parseInt(rating);
        if (reviewedBefore == 1) {
            ratingTotalPointInt = ratingTotalPointInt - myPointInt;
            ratingTotalPersonInt = ratingTotalPersonInt - 1;
        }
        ratingTotalPointInt = ratingTotalPointInt + ratingInt;
        mUsersDatabaseRatingTotalPointRef.setValue(ratingTotalPointInt);
        ratingTotalPersonInt = ratingTotalPersonInt + 1;
        mUsersDatabaseRatingTotalPersonRef.setValue(ratingTotalPersonInt);

        // TO Reset input values and return to listview
        reviewEditText.clearFocus();
        immReview = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
        if (immReview != null) {
            immReview.hideSoftInputFromWindow(reviewEditText.getWindowToken(), 0); //TO Hide keyboard!
        }
        ratingSeekBar.setProgress(0);
        reviewEditText.setText("");
        ratingReviewScrollView.setVisibility(View.GONE);
        mReviewsListView.setVisibility(View.VISIBLE);
        progressCircle.setVisibility(View.VISIBLE);
        fabRateReview.setVisibility(View.VISIBLE);
        mReviewAdView.setVisibility(View.VISIBLE);
        if (mReviewAdapter.isEmpty()) {
            mHandler11.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressCircle.setVisibility(View.GONE); //TO Hide progress bar
                    noReviewTextView.setVisibility(View.VISIBLE);
                }
            },3500);
        } else {
            progressCircle.setVisibility(View.GONE); //TO Hide progress bar
        }

        //To show custom toast message!
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText("Yorum ve puanınız kaydedildi!");
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    /*
    TO CANCEL SENDING REPORT
     */
    public void cancelReport(View view) {
        reviewReportEditText.setText("");
        reviewReportEditText.clearFocus();
        immReport = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
        if (immReport != null) {
            immReport.hideSoftInputFromWindow(reviewReportEditText.getWindowToken(), 0); //TO Hide keyboard!
        }

        reportReviewScrollView.setVisibility(View.GONE);
        mReviewsListView.setVisibility(View.VISIBLE);
        mReviewAdView.setVisibility(View.VISIBLE);
        reviewHeadingTextView.setText("Yorumlar");
    }
    /*
    TO SEND REPORT
     */
    public void sendReport(View view) {
        String report = reviewReportEditText.getText().toString().trim();
        String reportDate = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
        if (!report.equals("")) {
            mReportDatabaseReportRef = mReportDatabaseRef.getRef().child("report");
            mReportDatabaseReporterNameRef = mReportDatabaseRef.getRef().child("reporterName");
            mReportDatabaseReporterEmailRef = mReportDatabaseRef.getRef().child("reporterEmail");
            mReportDatabaseReviewRef = mReportDatabaseRef.getRef().child("reportedReview");
            mReportDatabaseReportedDateRef = mReportDatabaseRef.getRef().child("reportedDate");
            mReportDatabaseReportRef.setValue(report);
            mReportDatabaseReporterNameRef.setValue(username);
            mReportDatabaseReporterEmailRef.setValue(email);
            mReportDatabaseReviewRef.setValue(reviewToReport);
            mReportDatabaseReportedDateRef.setValue(reportDate);

            reviewReportEditText.setText("");
            reviewReportEditText.clearFocus();
            immReport = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); //TO Hide keyboard!
            if (immReport != null) {
                immReport.hideSoftInputFromWindow(reviewReportEditText.getWindowToken(), 0); //TO Hide keyboard!
            }

            reportReviewScrollView.setVisibility(View.GONE);
            mReviewsListView.setVisibility(View.VISIBLE);
            mReviewAdView.setVisibility(View.VISIBLE);
            reviewHeadingTextView.setText("Yorumlar");

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
    }

    /*
    REVIEW REFRESHER
     */
    private void refreshReviews() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mReviewAdapter.clear();
                mOthersReviewDatabaseRef.orderByChild("/reviewKey").addChildEventListener(mOthersReviewChildEventListener);
            }
        }, 500);
    }
}
