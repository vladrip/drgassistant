/*
The MIT License (MIT)

Copyright (c) delight.im (https://www.delight.im/)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
 */

package com.vladrip.drgassistant.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vladrip.drgassistant.MainActivity;
import com.vladrip.drgassistant.R;

import im.delight.android.webview.AdvancedWebView;

public class WikiFragment extends DrgBaseFragment {
    private String selectedWiki;
    private AdvancedWebView wiki;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) requireActivity();
        selectedWiki = mainActivity.getSharedPreferences("wiki", 0)
                .getString("selected_wiki", getString(R.string.wiki_gg_url));

        requireActivity().addMenuProvider(new MenuProvider() {
            private Menu menu;

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.wiki_menu, menu);
                this.menu = menu;
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                menu.findItem(R.id.wiki_select).setIcon(mainActivity.getSelectedWikiIconId(selectedWiki));
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.wiki_home)
                    wiki.loadUrl(selectedWiki);
                else if (menuItem.getGroupId() == R.id.select_wiki_group) {
                    menuItem.setChecked(true);
                    selectedWiki = menuItem.getTitleCondensed().toString();
                    setPermittedHostnameFromUrl(selectedWiki);
                    wiki.loadUrl(selectedWiki);

                    int iconId = mainActivity.getSelectedWikiIconId(selectedWiki);
                    menu.findItem(R.id.wiki_select).setIcon(iconId);
                    BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
                    navView.getMenu().findItem(R.id.navigation_wiki).setIcon(iconId);
                    navView.setItemIconTintList(null);
                } else return false;
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return getPersistentView(((MainActivity) requireActivity()).getWikiView(),
                inflater, container, R.layout.fragment_wiki);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wiki = rootView.findViewById(R.id.wiki_webview);
        if (!wiki.isSelected())
            initFandom();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (wiki.canGoBack())
                    wiki.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void initFandom() {
        wiki = rootView.findViewById(R.id.wiki_webview);
        wiki.setMixedContentAllowed(true);
        wiki.setThirdPartyCookiesEnabled(true);

        //sick of wiki.gg spamming "allow cookies pls" popup
        CookieManager.getInstance().setCookie(selectedWiki, "commons_encookiewarning_dismissed=1");
        setPermittedHostnameFromUrl(selectedWiki);
        wiki.loadUrl(requireActivity().getSharedPreferences("wiki", 0)
                .getString("current_url", selectedWiki), false);
        wiki.setSelected(true); //flag to check if initialized
    }

    private void setPermittedHostnameFromUrl(String url) {
        wiki.clearPermittedHostnames();
        String hostname = url.replaceAll("https://", "");
        hostname = hostname.substring(0, hostname.indexOf("/"));
        wiki.addPermittedHostname(hostname);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().getSharedPreferences("wiki", 0).edit()
                .putString("current_url", wiki.getUrl())
                .putString("selected_wiki", selectedWiki)
                .apply();
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) requireActivity()).setWikiView(rootView);
        super.onDestroyView();
    }
}