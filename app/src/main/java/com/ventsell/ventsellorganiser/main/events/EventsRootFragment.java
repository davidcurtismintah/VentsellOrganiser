package com.ventsell.ventsellorganiser.main.events;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsell.ventsellorganiser.R;

import java.util.List;

public class EventsRootFragment extends Fragment {

	private static final String TAG = EventsRootFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_events_root, container, false);

		FragmentManager childFragmentManager = getChildFragmentManager();
		Fragment fragment = childFragmentManager.findFragmentById(R.id.events_root_frame);
		if (fragment == null) {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.events_root_frame, new EventsListFragment());
            transaction.commit();
        }
		return view;
	}

	public boolean onBackPressed(){
        boolean handled = false;
        List<Fragment> frags = getChildFragmentManager().getFragments();
        for (Fragment frag : frags) {
            if (frag instanceof EventInsightsFragment) {
                handled = ((EventInsightsFragment) frag).onBackPressed();
            }

            if (handled) {
                break;
            }
        }

        if (!handled && getChildFragmentManager().getBackStackEntryCount() != 0) {
            getChildFragmentManager().popBackStackImmediate("InsightsFragmentBackStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            handled = true;
        }

        return handled;
    }
}
