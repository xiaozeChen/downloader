package io.demo.download;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * viewPager2 适配器
 */
public class ViewPager2Adapter<T extends Fragment> extends FragmentStateAdapter {
    private List<T> fragments;

    public ViewPager2Adapter(@NonNull FragmentActivity fragmentActivity, List<T> fragments) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    public ViewPager2Adapter(@NonNull Fragment fragment, List<T> fragments) {
        super(fragment);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments == null ? 0 : fragments.size();
    }
}
