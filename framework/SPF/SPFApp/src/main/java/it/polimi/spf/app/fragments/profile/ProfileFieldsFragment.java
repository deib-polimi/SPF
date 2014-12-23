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
import it.polimi.spf.app.fragments.profile.ProfileFragment.Mode;
import it.polimi.spf.shared.model.ProfileField;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Fragment that displays a list of {@link ProfileField} together with their
 * values. The {@link View}s used to display the values are obtained by the
 * parent fragment, intended to be {@link ProfileFragment}, with a call to
 * {@link ProfileFragment#createViewFor(ProfileField, ViewGroup)}. The
 * possibility of editing the values of fields depend on the {@link Mode} of the
 * parent.
 * 
 * Instances of {@link ProfileFieldsFragment} can be created using {@link
 * ProfileFieldsFragment#new}
 * 
 * @author darioarchetti
 * 
 */
public class ProfileFieldsFragment extends Fragment {

	/**
	 * Key for the list of fields to show that should be put in the arguments
	 * passed to this fragment.
	 */
	private static final String EXTRA_FIELDS_TO_SHOW = "fields";

	/**
	 * Creates a new instance of {@link ProfileFieldsFragment} to show the given
	 * list of profile fields.
	 * 
	 * @param fieldsToShow
	 *            - the fields to show;
	 * @return an instance of {@link ProfileFieldsFragment};
	 */
	public static ProfileFieldsFragment newInstance(ProfileField<?>[] fieldsToShow) {
		if (fieldsToShow == null) {
			throw new NullPointerException();
		}

		Bundle b = new Bundle();
		b.putStringArray(EXTRA_FIELDS_TO_SHOW, ProfileField.toIdentifierList(fieldsToShow));
		ProfileFieldsFragment instance = new ProfileFieldsFragment();
		instance.setArguments(b);
		return instance;
	}

	private ProfileField<?>[] mFieldsToShow;
	private LinearLayout mViewContainer;
	private ProfileFragment mParent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.profileedit_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle b = savedInstanceState == null ? getArguments() : savedInstanceState;

		if (b == null) {
			throw new IllegalArgumentException("No bundle to read data from");
		}

		mFieldsToShow = ProfileField.fromIdentifierList(b.getStringArray(EXTRA_FIELDS_TO_SHOW));
		mViewContainer = (LinearLayout) getView().findViewById(R.id.profileedit_field_container);
		mParent = (ProfileFragment) getParentFragment();
		onRefresh();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray(EXTRA_FIELDS_TO_SHOW, ProfileField.toIdentifierList(mFieldsToShow));
	}

	/**
	 * Refreshes the values of displayed fields.
	 */
	public void onRefresh() {
		mViewContainer.removeAllViews();
		for (ProfileField<?> field : mFieldsToShow) {
			View child = mParent.createViewFor(field, mViewContainer);
			mViewContainer.addView(child);
		}
	}
}
