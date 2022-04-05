package com.neftisoft.doyur;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class NewPartyViewPagerFragmentAdapter extends FragmentStateAdapter {
    private final int numOfTabs = 3;
    private String adress;
    private String uid;
    private String refCity;

    public NewPartyViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity, String newAdress, String newUid, String newRefCity) {
        super(fragmentActivity);
        adress = newAdress;
        uid = newUid;
        refCity = newRefCity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if (position == 0) { //DAILY
            return TabFragmentDaily.newInstance(position, adress, uid, refCity);
        } else if (position == 1) { //STORE
            return TabFragmentStore.newInstance(position, adress, uid);
        } else { //CHARITY
            return TabFragmentCharity.newInstance(position, adress, uid, refCity);
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
