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
package it.polimi.spf.app.fragments.profile;

import it.polimi.spf.app.R;
import it.polimi.spf.shared.model.ProfileField;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentStatePagerAdapter;

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {

	private static final int PAGE_COUNT = 3;

	public static final ProfileField<?>[] DEFAULT_FIELDS = { ProfileField.IDENTIFIER, ProfileField.DISPLAY_NAME, ProfileField.GENDER, ProfileField.BIRTHDAY, ProfileField.LOCATION, ProfileField.EMAILS, ProfileField.ABOUT_ME, ProfileField.STATUS, ProfileField.PHOTO, ProfileField.INTERESTS };
	public static final ProfileField<?>[] PERSONAL_FIELDS = { ProfileField.IDENTIFIER, ProfileField.DISPLAY_NAME, ProfileField.GENDER, ProfileField.BIRTHDAY, ProfileField.LOCATION, ProfileField.EMAILS };
	public static final ProfileField<?>[] EDITABLE_PERSONAL_FIELDS = { ProfileField.DISPLAY_NAME, ProfileField.GENDER, ProfileField.BIRTHDAY, ProfileField.LOCATION, ProfileField.EMAILS };
	public static ProfileField<?>[] ABOUT_ME_FIELDS = { ProfileField.ABOUT_ME, ProfileField.STATUS };
	public static ProfileField<?>[] TAG_FIELDS = { ProfileField.INTERESTS };

	private final String[] mPageTitles;
	private ProfileFieldsFragment[] mCurrentFragments = new ProfileFieldsFragment[PAGE_COUNT];
	private ProfileFragment.Mode mMode;

	public ProfilePagerAdapter(Context context, FragmentManager fm, ProfileFragment.Mode mode) {
		super(fm);
		mPageTitles = context.getResources().getStringArray(R.array.profileedit_fragments_titles);
		mMode = mode;
	}

	@Override
	public Fragment getItem(int arg0) {
		ProfileField<?>[] fields;

		switch (arg0) {
		case 0:
			fields = mMode == ProfileFragment.Mode.EDIT ? EDITABLE_PERSONAL_FIELDS : PERSONAL_FIELDS;
			break;
		case 1:
			fields = TAG_FIELDS;
			break;
		case 2:
			fields = ABOUT_ME_FIELDS;
			break;
		default:
			throw new IndexOutOfBoundsException("Page " + arg0 + "/" + PAGE_COUNT);
		}

		ProfileFieldsFragment fragment = ProfileFieldsFragment.newInstance(fields);
		mCurrentFragments[arg0] = fragment;
		return fragment;
	}

	public void onRefresh() {
		for (ProfileFieldsFragment fragment : mCurrentFragments) {
			if (fragment != null) {
				fragment.onRefresh();
			}
		}
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mPageTitles[position];
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}
}