package com.vladrip.drgassistant.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class DrgBaseFragment extends Fragment {
    protected View rootView = null;

    protected View getPersistentView(View root, LayoutInflater inflater, ViewGroup container, int layout) {
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
