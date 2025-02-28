package com.example.attendance;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LeavePagerAdapter extends FragmentStateAdapter {
    public LeavePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new LeaveRequestFragment() : new LeaveStatusFragment();
    }

    @Override
    public int getItemCount() {
        return 2;  // Two tabs: Request Leave & Request Status
}
}