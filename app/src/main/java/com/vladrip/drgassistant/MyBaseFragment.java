package com.vladrip.drgassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class MyBaseFragment extends Fragment {
    protected View rootView = null;

    protected View getPersistentView(View root, LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int layout) {
        rootView = root;
        if (rootView == null) {
            rootView = inflater.inflate(layout, container, false);
        } else {
            ViewGroup vg = (ViewGroup) rootView.getParent();
            if (vg != null) vg.removeView(rootView);
        }
        return rootView;
    }
}
