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
package it.polimi.spf.app.fragments.personas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.app.fragments.personas.ProfileFieldCirclePickerItem.OnChangeListener;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.ProfileField;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PersonasCirclesFragment extends Fragment {

	private SPFPersona mPersona;

	private final ProfileField<?>[] mProfileFieldsToShow = {
			ProfileField.GENDER,
			ProfileField.BIRTHDAY,
			ProfileField.LOCATION,
			ProfileField.EMAILS,
			ProfileField.ABOUT_ME,
			ProfileField.STATUS,
			ProfileField.PHOTO,
			ProfileField.INTERESTS };

	public static ProfileField<?>[] TAG_FIELDS = { ProfileField.INTERESTS };

	private PersonasCirclesFragment() {
		// use new instance to generate a new object
	}

	public static PersonasCirclesFragment newInstance(SPFPersona persona) {
		Bundle b = new Bundle();
		b.putParcelable("persona", persona);
		PersonasCirclesFragment fr = new PersonasCirclesFragment();
		fr.setArguments(b);
		return fr;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle data = savedInstanceState == null ? getArguments()
				: savedInstanceState;
		mPersona = data.getParcelable("persona");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.personas_circle_fragment, container,
				false);
		LinearLayout layout = (LinearLayout) v
				.findViewById(R.id.personas_circle_fragment_layout);
		Collection<String> circles = SPF.get().getSecurityMonitor().getPersonRegistry().getCircles();
		Bundle bc = SPF.get().getProfileManager().getCirclesOf(mPersona);
		for (ProfileField<?> f : mProfileFieldsToShow) {
			ProfileFieldCirclePickerItem item = new ProfileFieldCirclePickerItem(
					getActivity());
			item.setProfileField(f);
			item.setOnChangeListener(listener);
			// FIXME circles are string.... but what about translation of the
			// default ones?
			List<String> selected = bc.getStringArrayList(f.getIdentifier());
			if (selected == null) {
				selected = new ArrayList<String>(0);
			}
			List<String> selectable = new ArrayList<String>(circles);
			selectable.removeAll(selected);
			item.setCircles(selected, selectable);
			layout.addView(item);
		}
		return v;

	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().getActionBar().setTitle(mPersona.getIdentifier());
	};

	ProfileFieldCirclePickerItem.OnChangeListener listener = new OnChangeListener() {

		@Override
		public void onRemove(ProfileField<?> f, String circle) {
			SPF.get().getProfileManager()
					.removeCircleFromField(f, circle, mPersona);
		}

		@Override
		public void onAdd(ProfileField<?> f, String circle) {
			SPF.get().getProfileManager().addCircleToField(f, circle, mPersona);
		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("persona", mPersona);
		super.onSaveInstanceState(outState);
	}

}
