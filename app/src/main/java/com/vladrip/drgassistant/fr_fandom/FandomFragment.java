package com.vladrip.drgassistant.fr_fandom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vladrip.drgassistant.MainActivity;
import com.vladrip.drgassistant.MyBaseFragment;
import com.vladrip.drgassistant.R;

import im.delight.android.webview.AdvancedWebView;

public class FandomFragment extends MyBaseFragment {
    private FandomViewModel model;
    private AdvancedWebView fandom;
    private final static String HOME_URL = "https://deeprockgalactic.fandom.com/wiki/Deep_Rock_Galactic_Wiki";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //model = new ViewModelProvider(this).get(FandomViewModel.class);
        //binding = FragmentFandomBinding.inflate(inflater, container, false);

        //return binding.getRoot();
        setHasOptionsMenu(true);
        return getPersistentView(((MainActivity)requireActivity()).getFandomView(),
                inflater, container, savedInstanceState, R.layout.fragment_fandom);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fandom = view.findViewById(R.id.fandom_webview);
        if (!fandom.isSelected())
            initFandom();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fandom.canGoBack())
                    fandom.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void initFandom() {
        fandom = rootView.findViewById(R.id.fandom_webview);
        fandom.setMixedContentAllowed(true);
        /*
        fandom.setWebChromeClient(new WebChromeClient());
        WebSettings settings = fandom.getSettings();
        settings.setDomStorageEnabled(true);
        fandom.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        */

        fandom.loadUrl(HOME_URL, false);
        fandom.setSelected(true); //flag to check if initialized

        /*
        File dir = requireActivity().getCacheDir();
        if (!dir.exists())
            dir.mkdirs();
        settings.setAppCachePath(dir.getPath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        fandom.loadUrl(requireActivity().getSharedPreferences("fandom_state", 0)
              .getString("url", "https://deeprockgalactic.fandom.com/wiki/Deep_Rock_Galactic_Wiki"));
         */
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fandom_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fandom_home)
            fandom.loadUrl(HOME_URL);
        else if (id == R.id.fandom_desktop) {
            fandom.setDesktopMode(!fandom.getSettings().getLoadWithOverviewMode());
            fandom.reload();
        }
        else return false;
        return true;
    }

    @Override
    public void onDestroyView() {
        //requireActivity().getSharedPreferences("fandom_state", 0).edit()
        //        .putString("url", fandom.getUrl()).apply();
        ((MainActivity)requireActivity()).setFandomView(rootView);
        super.onDestroyView();
    }
}