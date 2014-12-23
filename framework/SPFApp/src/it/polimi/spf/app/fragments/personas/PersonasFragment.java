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

import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFProfileManager;
import it.polimi.spf.framework.profile.SPFPersona;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class PersonasFragment extends Fragment implements PersonasArrayAdapter.OnPersonaDeletedListener, OnClickListener, LoaderManager.LoaderCallbacks<List<SPFPersona>> {

	private static final int CREATE_PERSONA_LOADER = 0;
	private static final int LOAD_PERSONAS_LOADER = 1;
	private static final int DELETE_PERSONA_LOADER = 2;
	protected static final String EXTRA_PERSONA = "persona";

	private PersonasArrayAdapter mAdapter;
	private EditText mNewPersonaName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_personas, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new PersonasArrayAdapter(getActivity(), this);
		ListView list = (ListView) getView().findViewById(R.id.personas_container);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(itemClickListener);
		mNewPersonaName = (EditText) getView().findViewById(R.id.personas_new_name);
		ImageButton addButton = (ImageButton) getView().findViewById(R.id.personas_new_add);
		addButton.setOnClickListener(this);
		getLoaderManager().destroyLoader(LOAD_PERSONAS_LOADER);
		getLoaderManager().initLoader(LOAD_PERSONAS_LOADER, null, this).forceLoad();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			SPFPersona p = mAdapter.getItem(position);
			PersonasCirclesActivity.start(getActivity(), p);
		}
	};

	@Override
	public void onPersonaDeleted(SPFPersona persona) {
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_PERSONA, persona);
		getLoaderManager().destroyLoader(DELETE_PERSONA_LOADER);
		getLoaderManager().initLoader(DELETE_PERSONA_LOADER, args, this).forceLoad();
	}

	@Override
	public void onClick(View v) {
		String name = mNewPersonaName.getText().toString();
		if (name.length() == 0) {
			Toast.makeText(getActivity(), "Persona name cannot be empty", Toast.LENGTH_LONG).show();
			return;
		}

		SPFPersona persona = new SPFPersona(name);
		mNewPersonaName.setText("");

		Bundle args = new Bundle();
		args.putParcelable(EXTRA_PERSONA, persona);
		getLoaderManager().destroyLoader(CREATE_PERSONA_LOADER);
		getLoaderManager().initLoader(CREATE_PERSONA_LOADER, args, this).forceLoad();
	}

	@Override
	public Loader<List<SPFPersona>> onCreateLoader(final int id, final Bundle args) {
		final SPFProfileManager profile = SPF.get().getProfileManager();

		switch (id) {
		case CREATE_PERSONA_LOADER:
			return new AsyncTaskLoader<List<SPFPersona>>(getActivity()) {

				@Override
				public List<SPFPersona> loadInBackground() {
					SPFPersona persona = args.getParcelable(EXTRA_PERSONA);
					profile.addPersona(persona);
					return profile.getAvailablePersonas();
				}
			};

		case DELETE_PERSONA_LOADER:
			return new AsyncTaskLoader<List<SPFPersona>>(getActivity()) {

				@Override
				public List<SPFPersona> loadInBackground() {
					SPFPersona persona = args.getParcelable(EXTRA_PERSONA);
					profile.removePersona(persona);
					return profile.getAvailablePersonas();
				}
			};
		case LOAD_PERSONAS_LOADER:
			return new AsyncTaskLoader<List<SPFPersona>>(getActivity()) {

				@Override
				public List<SPFPersona> loadInBackground() {
					return profile.getAvailablePersonas();
				}
			};
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<List<SPFPersona>> loader, List<SPFPersona> data) {
		mAdapter.clear();
		mAdapter.addAll(data);
	}

	@Override
	public void onLoaderReset(Loader<List<SPFPersona>> loader) {
		// Do nothing
	}

}
