package com.vladrip.drgassistant.fr_fandom;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;

import com.vladrip.drgassistant.DrgBaseFragment;
import com.vladrip.drgassistant.MainActivity;
import com.vladrip.drgassistant.R;

import im.delight.android.webview.AdvancedWebView;

public class FandomFragment extends DrgBaseFragment {
    private FandomViewModel model;
    private AdvancedWebView fandom;
    private final static String HOME_URL = "https://deeprockgalactic.fandom.com/wiki/Deep_Rock_Galactic_Wiki";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //model = new ViewModelProvider(this).get(FandomViewModel.class);
        //binding = FragmentFandomBinding.inflate(inflater, container, false);

        //return binding.getRoot();
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fandom_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.fandom_home)
                    fandom.loadUrl(HOME_URL);
                else if (id == R.id.fandom_desktop) {
                    WebSettings settings = fandom.getSettings();
                    fandom.setDesktopMode(!settings.getLoadWithOverviewMode());
                    settings.setDisplayZoomControls(false);
                    fandom.reload();
                }
                else return false;
                return true;
            }
        });

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
        fandom.setListener(new Fragment(), new AdvancedWebView.Listener() {
            @Override
            public void onPageStarted(String url, Bitmap favicon) {
                if (fandom.getSettings().getLoadWithOverviewMode())
                    fandom.zoomBy(0.5f);
            }

            @Override
            public void onPageFinished(String url) {
                if (fandom.getSettings().getLoadWithOverviewMode())
                    fandom.zoomBy(0.5f);
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {

            }

            @Override
            public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

            }

            @Override
            public void onExternalPageRequest(String url) {

            }
        });

        fandom.loadUrl(HOME_URL, false);
        fandom.setSelected(true); //flag to check if initialized

        /* standard WebView config
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
    public void onDestroyView() {
        ((MainActivity)requireActivity()).setFandomView(rootView);
        super.onDestroyView();
    }
}