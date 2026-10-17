/*
 * Adapted from OpenVPN for Android
 * Copyright (c) 2012-2013, Arne Schwabe
 * Copyright (c) 2013, Kevin Cernekee
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

package net.openconnect_vpn.android;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import net.openconnect_vpn.android.core.OpenConnectManagementThread;
import net.openconnect_vpn.android.core.OpenVpnService;
import net.openconnect_vpn.android.core.VPNConnector;
import net.openconnect_vpn.android.fragments.*;

public class MainActivity extends AppCompatActivity {

	public static final String TAG = "OpenConnect";

	private TabLayout mTabs;

	private ArrayList<TabContainer> mTabList = new ArrayList<TabContainer>();

	private TabContainer mConnectionTab;
	private int mLastTab;
	private boolean mTabsActive;

	private int mConnectionState = OpenConnectManagementThread.STATE_DISCONNECTED;
	private VPNConnector mConn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mTabsActive = false;
		if (savedInstanceState != null) {
			mLastTab = savedInstanceState.getInt("active_tab");
			if (mLastTab >= 2) {
				// the FAQ used to be a tab; it now lives in the overflow menu
				mLastTab = 0;
			}
		}

		setContentView(R.layout.activity_main);
		mTabs = findViewById(R.id.tabs);
		mTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab t) {
				((TabContainer)t.getTag()).onTabSelected(t);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab t) {
				((TabContainer)t.getTag()).onTabUnselected(t);
			}

			@Override
			public void onTabReselected(TabLayout.Tab t) {
			}
		});

		mTabList.add(new TabContainer(0, R.string.vpn_list_title, new VPNProfileList()));
		mTabList.add(new TabContainer(1, R.string.log, new LogFragment()));

		mConnectionTab = mTabList.get(0);

		FeedbackFragment.recordUse(this, false);
	}

	@Override
	protected void onSaveInstanceState(Bundle b) {
		super.onSaveInstanceState(b);
		b.putInt("active_tab", mLastTab);
	}

	private void updateUI(OpenVpnService service) {
		int newState = service.getConnectionState();

		service.startActiveDialog(this);

		if (mConnectionState != newState) {
			if (newState == OpenConnectManagementThread.STATE_DISCONNECTED) {
				mConnectionTab.replace(R.string.vpn_list_title, new VPNProfileList());
			} else if (mConnectionState == OpenConnectManagementThread.STATE_DISCONNECTED) {
				mConnectionTab.replace(R.string.status, new StatusFragment());
			}
			mConnectionState = newState;
		}

		if (!mTabsActive) {
			// NOTE: addTab may cause mLastTab to change, so cache the value here
			int lastTab = mLastTab;
			for (TabContainer tc : mTabList) {
				mTabs.addTab(tc.tab, tc.idx == lastTab);
			}
			mTabsActive = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mConn = new VPNConnector(this, true) {
			@Override
			public void onUpdate(OpenVpnService service) {
				updateUI(service);
			}
		};
	}

	@Override
	protected void onPause() {
		mConn.stopActiveDialog();
		mConn.unbind();
		super.onPause();
	}

	protected class TabContainer implements TabLayout.OnTabSelectedListener {
		private Fragment mFragment;
		private boolean mActive;
		public TabLayout.Tab tab;
		public int idx;

		public void replace(int titleResId, Fragment frag) {
			if (mActive) {
				getFragmentManager().beginTransaction().remove(mFragment).commit();
			}

			mFragment = frag;
			tab.setText(titleResId);

			if (idx == mLastTab) {
				getFragmentManager().beginTransaction()
					.setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
					.replace(R.id.fragment_container, mFragment)
					.commit();
				mActive = true;
			} else {
				mActive = false;
			}
		}

		public TabContainer(int idx, int titleResId, Fragment frag) {
			this.idx = idx;
			this.mFragment = frag;
			tab = mTabs.newTab().setText(titleResId).setTag(this);
		}

		@Override
		public void onTabSelected(TabLayout.Tab t) {
			android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (mTabsActive) {
				if (idx < mLastTab) {
					ft.setCustomAnimations(R.animator.fragment_slide_right_enter,
							R.animator.fragment_slide_right_exit);
				} else if (idx > mLastTab) {
					ft.setCustomAnimations(R.animator.fragment_slide_left_enter,
							R.animator.fragment_slide_left_exit);
				}
			}

			mLastTab = idx;
			ft.replace(R.id.fragment_container, mFragment).commit();
			mActive = true;
		}

		@Override
		public void onTabUnselected(TabLayout.Tab t) {
			// the newly selected tab replaces this fragment
			mActive = false;
		}

		@Override
		public void onTabReselected(TabLayout.Tab t) {
		}
	}
}
