/*
 * Adapted from OpenVPN for Android
 * Copyright (c) 2012-2013, Arne Schwabe
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * In addition, as a special exception, the copyright holders give
 * permission to link the code of portions of this program with the
 * OpenSSL library.
 */

package net.openconnect_vpn.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.google.android.material.tabs.TabLayout;
import net.openconnect_vpn.android.R;
import net.openconnect_vpn.android.core.AssetExtractor;
import net.openconnect_vpn.android.core.FaqHtml;
import net.openconnect_vpn.android.core.WebColors;

public class AboutFragment extends Fragment  {

	public static final String TAG = "OpenConnect";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Activity activity = getActivity();

    	View v = inflater.inflate(R.layout.about, container, false);

		try {
			PackageInfo packageinfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
			activity.setTitle(activity.getString(R.string.app) + " v" + packageinfo.versionName);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "can't retrieve package version");
		}

    	final WebView contents = (WebView)v.findViewById(R.id.about_contents);
    	contents.setBackgroundColor(0);
    	showPage(contents, 0);

    	TabLayout tabs = (TabLayout) v.findViewById(R.id.about_tabs);
    	tabs.addTab(tabs.newTab().setText(R.string.faq));
    	tabs.addTab(tabs.newTab().setText(R.string.credits));
    	tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    		@Override public void onTabSelected(TabLayout.Tab tab) { showPage(contents, tab.getPosition()); }
    		@Override public void onTabUnselected(TabLayout.Tab tab) { }
    		@Override public void onTabReselected(TabLayout.Tab tab) { }
    	});

    	return v;
    }

    private void showPage(WebView contents, int page) {
    	Activity activity = getActivity();
    	StringBuilder html = new StringBuilder();
    	html.append("<html><head><meta charset=\"UTF-8\">");
    	html.append(WebColors.style(activity));
    	html.append("<style>body{margin:16px}</style>");
    	html.append("</head><body>");
    	if (page == 0) {
    		html.append(FaqHtml.build(activity));
    	} else {
    		String about = AssetExtractor.readString(activity, "about.html");
    		int start = about.indexOf("<body>") + "<body>".length();
    		int end = about.lastIndexOf("</body>");
    		html.append(about.substring(start, end));
    	}
    	html.append("</body></html>");
    	contents.loadDataWithBaseURL("file:///android_asset/", html.toString(), null, null, null);
    }

}
