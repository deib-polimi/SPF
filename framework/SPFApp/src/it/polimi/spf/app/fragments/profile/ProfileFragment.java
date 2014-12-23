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

import java.util.List;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import it.polimi.spf.app.R;
import it.polimi.spf.app.fragments.contacts.ContactConfirmDialogView;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.proximity.SPFRemoteInstance;
import it.polimi.spf.app.view.CircleImageView;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<ProfileFieldContainer>, OnItemSelectedListener, OnClickListener {

	/**
	 * Possible visualization modes of fields values.
	 * 
	 * @author darioarchetti
	 */
	public static enum Mode {
		/**
		 * Shows the profile of the local user
		 */
		SELF,

		/**
		 * Shows the profile of a remote user
		 */
		REMOTE,

		/**
		 * Shows the profile of the local user and allows modifications
		 */
		EDIT;
	}

	/**
	 * Creates a new instance of {@link ProfileFragment} to show the profile of
	 * a remote user. The fragment will allow only to view the fields and not to
	 * modify them.
	 * 
	 * @param personIdentifer
	 *            - the identifier of the person whose profile to show
	 * @return an instance of ProfileFragment
	 */
	public static ProfileFragment createRemoteProfileFragment(String personIdentifer) {
		Bundle b = new Bundle();
		b.putInt(EXTRA_VIEW_MODE, Mode.REMOTE.ordinal());
		b.putString(EXTRA_PERSON_IDENTIFIER, personIdentifer);
		ProfileFragment fragment = new ProfileFragment();
		fragment.setArguments(b);
		return fragment;
	}

	/**
	 * Creates a new instance of ProfileFragment to show the local profile. The
	 * fragment may also allow to modify the values depending on the given
	 * {@link Mode}.
	 * 
	 * @param mode
	 *            - the visualization {@link Mode}.
	 * @return an instance of ProfileFragment
	 */
	public static ProfileFragment createViewSelfProfileFragment() {
		Bundle b = new Bundle();
		b.putInt(EXTRA_VIEW_MODE, Mode.SELF.ordinal());
		ProfileFragment fragment = new ProfileFragment();
		fragment.setArguments(b);
		return fragment;
	}

	public static ProfileFragment createEditSelfProfileFragment(SPFPersona persona) {
		Bundle b = new Bundle();
		b.putInt(EXTRA_VIEW_MODE, Mode.EDIT.ordinal());
		b.putParcelable(EXTRA_CURRENT_PERSONA, persona);
		ProfileFragment fragment = new ProfileFragment();
		fragment.setArguments(b);
		return fragment;
	}

	private static final String EXTRA_PERSON_IDENTIFIER = "personIdentifier";
	private static final String EXTRA_PROFILE_CONTAINER = "profileContainer";
	private static final String EXTRA_CURRENT_PERSONA = "persona";
	private static final String EXTRA_VIEW_MODE = "viewMode";

	private static final int LOAD_PROFILE_LOADER_ID = 0;
	private static final int SAVE_PROFILE_LOADER_ID = 1;

	private static final int ACTIVITY_EDIT_PROFILE_CODE = 0;
	private static final int ACTIVITY_EDIT_PROFILE_PICTURE_CODE = 1;
	private static final String PHOTO_WIDTH = "50dp";
	private static final String PHOTO_HEIGHT = "50dp";
	private static final String TAG = "ProfileFragment";

	private String mPersonIdentifier;
	private SPFPersona mCurrentPersona;
	private Mode mMode;
	private ProfileFieldContainer mContainer;

	private ProfilePagerAdapter mPagerAdapter;
	private ViewPager mViewPager;
	private ProfileFieldViewFactory mFactory;
	private boolean mModifiedAtLeastOnce = false;

	// Lifecycle management methods

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_profile, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null) {
			mMode = Mode.values()[getArguments().getInt(EXTRA_VIEW_MODE)];
			switch (mMode) {
			case SELF:
				String callerApp = getActivity().getCallingPackage();
				if (callerApp == null) {
					mCurrentPersona = SPFPersona.DEFAULT;
				} else {
					mCurrentPersona = SPF.get().getSecurityMonitor().getPersonaOf(callerApp);
				}
				break;
			case REMOTE:
				mPersonIdentifier = getArguments().getString(EXTRA_PERSON_IDENTIFIER);
				break;
			case EDIT:
				mCurrentPersona = getArguments().getParcelable(EXTRA_CURRENT_PERSONA);
			}

			// Initialize the loader of profile information
			startLoader(LOAD_PROFILE_LOADER_ID);
		} else {
			mPersonIdentifier = savedInstanceState.getString(EXTRA_PERSON_IDENTIFIER);
			mCurrentPersona = savedInstanceState.getParcelable(EXTRA_CURRENT_PERSONA);
			mMode = Mode.values()[savedInstanceState.getInt(EXTRA_VIEW_MODE)];
			mContainer = savedInstanceState.getParcelable(EXTRA_PROFILE_CONTAINER);

			onProfileDataAvailable();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRA_PERSON_IDENTIFIER, mPersonIdentifier);
		outState.putParcelable(EXTRA_CURRENT_PERSONA, mCurrentPersona);
		outState.putInt(EXTRA_VIEW_MODE, mMode.ordinal());
		outState.putParcelable(EXTRA_PROFILE_CONTAINER, mContainer);
	}

	/*
	 * LOADERS - Used to load and save profile data.
	 */
	@Override
	public Loader<ProfileFieldContainer> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_PROFILE_LOADER_ID:
			return new AsyncTaskLoader<ProfileFieldContainer>(getActivity()) {

				@Override
				public ProfileFieldContainer loadInBackground() {
					if (mMode == Mode.SELF || mMode == Mode.EDIT) {
						return SPF.get().getProfileManager().getProfileFieldBulk(mCurrentPersona, ProfilePagerAdapter.DEFAULT_FIELDS);
					} else {
						SPFRemoteInstance instance = SPF.get().getPeopleManager().getPerson(mPersonIdentifier);
						if (instance == null) {
							throw new IllegalStateException("Person " + mPersonIdentifier + " not found in proximity");
						} else {
							String app = getActivity().getCallingPackage();
							app = app == null ? "it.polimi.spf.app" : app;
							return instance.getProfileBulk(ProfileField.toIdentifierList(ProfilePagerAdapter.DEFAULT_FIELDS), app);
						}
					}
				}
			};

		case SAVE_PROFILE_LOADER_ID:
			if (mMode != Mode.EDIT) {
				Log.e(TAG, "SAVE_PROFILE_LOADER initialized in mode " + mMode);
			}

			return new AsyncTaskLoader<ProfileFieldContainer>(getActivity()) {

				@Override
				public ProfileFieldContainer loadInBackground() {
					SPF.get().getProfileManager().setProfileFieldBulk(mContainer, mCurrentPersona);
					return null;
				}
			};

		default:
			throw new IllegalArgumentException("No loader for id " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<ProfileFieldContainer> loader, ProfileFieldContainer data) {
		switch (loader.getId()) {
		case LOAD_PROFILE_LOADER_ID:
			mContainer = data;
			onProfileDataAvailable();
			break;
		case SAVE_PROFILE_LOADER_ID:
			mContainer.clearModified();
			mModifiedAtLeastOnce = true;
			getActivity().finish();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<ProfileFieldContainer> loader) {
		// Do nothing
	}

	// Called when the profile data is available, thus we can set up the view
	private void onProfileDataAvailable() {
		Log.d(TAG, "onProfileDataAvailable");
		mFactory = new ProfileFieldViewFactory(getActivity(), mMode, mCurrentPersona, mContainer);

		// Populate field list
		mPagerAdapter = new ProfilePagerAdapter(getActivity(), getChildFragmentManager(), mMode);
		mViewPager = (ViewPager) getView().findViewById(R.id.profileedit_pager);

		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);

		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) getView().findViewById(R.id.profileedit_tabs);
		tabs.setViewPager(mViewPager);

		showPicture(mContainer.getFieldValue(ProfileField.PHOTO));

		// Refresh field fragments
		mPagerAdapter.onRefresh();
	}

	private void showPicture(Bitmap photo) {
		// Show picture
		CircleImageView profilePicView = (CircleImageView) getView().findViewById(R.id.profile_picture);
		if (mMode == Mode.EDIT) {
			profilePicView.setOnClickListener(this);
		}

		profilePicView.setBackground(photo);
		profilePicView.invalidate();
	}

	// Methods to be called from child ProfileFieldsFragment to obtain views and
	// to notify of
	// changes in values of circles

	/**
	 * Creates a view for the given profile field. Depending on the {@link Mode}
	 * of visualization, the view may allow the modification of the value. This
	 * method will not attach the view to the provided {@link ViewGroup}
	 * 
	 * @param field
	 *            - the field for which to create the view.
	 * @param container
	 *            - the container to which the view will be attached, needed by
	 *            {@link LayoutInflater#inflate(int, ViewGroup, boolean)} to
	 *            evaluate layout params.
	 * @return
	 */
	public <E> View createViewFor(ProfileField<E> field, ViewGroup container) {
		switch (mMode) {
		case SELF:
		case REMOTE:
			return mFactory.createViewForField(field, container, null);
		case EDIT:
			return mFactory.createViewForField(field, container, new ProfileFieldViewFactory.FieldValueListener<E>() {

				@Override
				public void onFieldValueChanged(ProfileField<E> field, E value) {
					mContainer.setFieldValue(field, value);
				}

				@Override
				public void onInvalidFieldValue(ProfileField<E> field, String fieldFriendlyName) {
					Toast.makeText(getActivity(), "Invalid value for field " + fieldFriendlyName, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onCircleAdded(ProfileField<E> field, String circle) {
					Log.d(TAG, "Circle " + circle + " added to field " + field + " of persona " + mCurrentPersona);
					SPF.get().getProfileManager().addCircleToField(field, circle, mCurrentPersona);
				}

				@Override
				public void onCircleRemoved(ProfileField<E> field, String circle) {
					Log.d(TAG, "Circle " + circle + " removed from field " + field + " of persona " + mCurrentPersona);
					SPF.get().getProfileManager().removeCircleFromField(field, circle, mCurrentPersona);
				}
			});
		default:
			return null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_EDIT_PROFILE_CODE:
			// Profile may have changed, reload it
			if (resultCode == Activity.RESULT_CANCELED) {
				Log.d(TAG, "Edit finished but no data was modified");
			}
			
			onProfileDataSaved();
			startLoader(LOAD_PROFILE_LOADER_ID);
			break;
		case ACTIVITY_EDIT_PROFILE_PICTURE_CODE:
			if (resultCode != Activity.RESULT_OK) {
				return;
			}

			if (data != null && data.getExtras() != null) {
				Bitmap photo = data.getExtras().getParcelable("data");
				mContainer.setFieldValue(ProfileField.PHOTO, photo);
				showPicture(photo);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * MENU
	 */

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (mMode) {
		case SELF:
			inflater.inflate(R.menu.menu_view_self_profile, menu);
			break;
		case REMOTE:
			if (SPF.get().getSecurityMonitor().getPersonRegistry().lookup(mPersonIdentifier) == null) {
				inflater.inflate(R.menu.menu_view_other_profile, menu);
			}
			break;
		case EDIT:
			inflater.inflate(R.menu.menu_edit_profile, menu);
			break;
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem personaSelector = menu.findItem(R.id.profileview_persona_selector);
		if (personaSelector == null) {
			return;
		}

		Spinner spinner = (Spinner) personaSelector.getActionView().findViewById(R.id.profileview_persona_spinner);
		if (spinner == null) {
			return;
		}

		List<SPFPersona> personas = SPF.get().getProfileManager().getAvailablePersonas();
		spinner.setAdapter(new ArrayAdapter<SPFPersona>(getActivity().getActionBar().getThemedContext(), android.R.layout.simple_list_item_1, personas));
		spinner.setSelection(personas.indexOf(mCurrentPersona), false);
		spinner.setOnItemSelectedListener(this);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.profileview_edit:
			Intent i = new Intent(getActivity(), ProfileEditActivity.class);
			i.putExtra(ProfileEditActivity.EXTRA_PERSONA, mCurrentPersona);
			startActivityForResult(i, ACTIVITY_EDIT_PROFILE_CODE);
			return true;
		case R.id.profileview_send_contact_request:
			final String displayName = mContainer.getFieldValue(ProfileField.DISPLAY_NAME);
			final Bitmap picture = mContainer.getFieldValue(ProfileField.PHOTO);

			final ContactConfirmDialogView view = new ContactConfirmDialogView(getActivity(), displayName, picture);
			new AlertDialog.Builder(getActivity()).setTitle("Send contact request?").setView(view).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SPF.get().getSecurityMonitor().getPersonRegistry().sendContactRequestTo(mPersonIdentifier, view.getPassphrase(), displayName, picture, view.getSelectedCircles());
				}
			}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();

			return true;
		case R.id.profileedit_save:
			if (mContainer.isModified()) {
				startLoader(SAVE_PROFILE_LOADER_ID);
			} else {
				onSaveNotNecessary();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Click on the profile picture in edit mode starts an activity to change
	 * the profile picture
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);
		intent.putExtra("outputX", PHOTO_WIDTH);
		intent.putExtra("outputY", PHOTO_HEIGHT);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, ACTIVITY_EDIT_PROFILE_PICTURE_CODE);
	}

	/*
	 * ItemSelectListener for Persona spinner in actionbar
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		SPFPersona persona = (SPFPersona) parent.getItemAtPosition(position);
		if (mCurrentPersona.equals(persona)) {
			return;
		}

		mCurrentPersona = persona;
		startLoader(LOAD_PROFILE_LOADER_ID);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing
	}

	private void startLoader(int id) {
		getLoaderManager().destroyLoader(id);
		getLoaderManager().initLoader(id, null, this).forceLoad();
	}

	private void onProfileDataSaved() {
		Toast.makeText(getActivity(), "Profile data saved", Toast.LENGTH_SHORT).show();
	}

	private void onSaveNotNecessary() {
		Toast.makeText(getActivity(), "No field modified", Toast.LENGTH_SHORT).show();
	}

	public boolean isContainerModified() {
		return mContainer.isModified();
	}

	public boolean isContainerModifiedAtLeastOnce() {
		return mModifiedAtLeastOnce;
	}
}