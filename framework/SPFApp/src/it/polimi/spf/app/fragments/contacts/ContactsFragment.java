/* 
 * Copyright 2014 Jacopo Aliprandi, Dario Archetti
 * 
 * This file is part of SPF.
 * 
 * SPF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * SPF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SPF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package it.polimi.spf.app.fragments.contacts;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import it.polimi.spf.app.R;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContactsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_contacts, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ContactsPagerAdapter pagerAdapter = new ContactsPagerAdapter(getChildFragmentManager(), getActivity());
		ViewPager viewPager = (ViewPager) getView().findViewById(R.id.contacts_pager);

		viewPager.setAdapter(pagerAdapter);
		viewPager.setOffscreenPageLimit(2);

		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getView().findViewById(R.id.contacts_tabs);
		tabs.setViewPager(viewPager);
	}

	private static class ContactsPagerAdapter extends FragmentStatePagerAdapter {

		private final static int PAGE_COUNT = 2;
		private final String[] mPageTitles;

		public ContactsPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			mPageTitles = context.getResources().getStringArray(R.array.contacts_fragments_titles);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new PeopleFragment();
			case 1:
				return new CircleFragment();
			default:
				throw new IllegalArgumentException("Requested page outside boundaries");
			}
		}

		@Override
		public int getCount() {
			return PAGE_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mPageTitles[position];
		}
	}

}