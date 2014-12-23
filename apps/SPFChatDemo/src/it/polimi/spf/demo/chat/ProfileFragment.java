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
package it.polimi.spf.demo.chat;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.lib.profile.SPFLocalProfile;
import it.polimi.spf.lib.search.SPFSearch;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFError;

/**
 * Fragment to show the detail retrieved from a profile, either local or remote
 */
public class ProfileFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<ProfileFieldContainer>, View.OnClickListener {

    private static final String SELF_IDENTIFIER = "self";
    private static final String EXTRA_IDENTIFIER  = "personIdentifier";
    private static final String TAG = "ProfileFragment";
    private static final int PROFILE_LOADER_ID = 0;

    private final static ProfileField<?>[] FIELDS = {
            ProfileField.DISPLAY_NAME,
            ProfileField.IDENTIFIER,
            ProfileField.PHOTO
    };

    public static ProfileFragment forSelfProfile() {
        return forRemoteProfile(SELF_IDENTIFIER);
    }

    public static ProfileFragment forRemoteProfile(String identifier) {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        b.putString(EXTRA_IDENTIFIER, identifier);
        f.setArguments(b);
        return f;
    }

    private String mProfileIdentifier;
    private SPFLocalProfile mLocalProfile; // For the local profile
    private SPF mSpf; // For remote profiles
    private Handler mUIHandler = new Handler(Looper.getMainLooper());


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle source = savedInstanceState == null ? getArguments() : savedInstanceState;
        if (source == null || !source.containsKey(EXTRA_IDENTIFIER)) {
            throw new IllegalStateException("Missing person identifier");
        }

        mProfileIdentifier = source.getString(EXTRA_IDENTIFIER);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SELF_IDENTIFIER.equals(mProfileIdentifier)) {
            showLocalProfile();
        } else {
            showRemoteProfile();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_IDENTIFIER, mProfileIdentifier);
    }

    private void showLocalProfile() {
        SPFLocalProfile.load(getActivity(), new SPFLocalProfile.Callback() {

            @Override
            public void onServiceReady(SPFLocalProfile spfLocalProfile) {
                Log.v(TAG, "Local profile loaded");
                mLocalProfile = spfLocalProfile;
                getLoaderManager().initLoader(PROFILE_LOADER_ID, null, ProfileFragment.this).forceLoad();
            }

            @Override
            public void onError(SPFError spfError) {
                Log.e(TAG, "Error loading local profile: " + spfError);
                mLocalProfile = null;
            }

            @Override
            public void onDisconnect() {
                Log.d(TAG, "Disconnected from Local profile");
                mLocalProfile = null;
            }
        });
    }

    private void showRemoteProfile() {
        SPF.connect(getActivity(), new SPF.ConnectionListener() {
            @Override
            public void onConnected(SPF spf) {
                Log.v(TAG, "SPF loaded");
                mSpf = spf;
                getLoaderManager().initLoader(PROFILE_LOADER_ID, null, ProfileFragment.this).forceLoad();
            }

            @Override
            public void onError(SPFError spfError) {
                Log.e(TAG, "Error loading SPF: " + spfError);
                mSpf = null;
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Disconnected from SPF");
                mSpf = null;
            }
        });
    }

    @Override
    public Loader<ProfileFieldContainer> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<ProfileFieldContainer>(getActivity()) {
            @Override
            public ProfileFieldContainer loadInBackground() {
                ProfileFieldContainer result;
                if (SELF_IDENTIFIER.equals(mProfileIdentifier)) {
                    result = mLocalProfile.getValueBulk(FIELDS);
                    mLocalProfile.disconnect();
                } else {
                    SPFSearch search = mSpf.getComponent(SPF.SEARCH);
                    SPFPerson person = search.lookup(mProfileIdentifier);
                    if (person == null) {
                        Log.w(TAG, "Person " + mProfileIdentifier + " is not available");
                        return null;
                    }

                    result = person.getProfile(mSpf).getProfileBulk(FIELDS);
                }

                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ProfileFieldContainer> loader, final ProfileFieldContainer data) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                onProfileDataAvailable(data);
            }
        });

        if(mSpf != null){
            //mSpf.disconnect();
        }

        if(mLocalProfile != null){
            //mLocalProfile.disconnect();
        }
    }

    @Override
    public void onLoaderReset(Loader<ProfileFieldContainer> loader) {
        // Do nothing
    }

    private void onProfileDataAvailable(ProfileFieldContainer data) {
        if(data == null){
            Toast.makeText(getActivity(), R.string.people_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        String name = data.getFieldValue(ProfileField.DISPLAY_NAME);
        String identifier = data.getFieldValue(ProfileField.IDENTIFIER);

        ((TextView) getView().findViewById(R.id.profile_detail_name)).setText(name != null ? name : "-");
        ((TextView) getView().findViewById(R.id.profile_detil_identifier)).setText(identifier);

        if(!SELF_IDENTIFIER.equals(mProfileIdentifier)){
            getActivity().setTitle(name != null ? name : identifier);
        }

        Bitmap pic = data.getFieldValue(ProfileField.PHOTO);
        if(pic != null){
            ((ImageView) getView().findViewById(R.id.profile_detail_pic)).setImageBitmap(pic);
        }

        getView().findViewById(R.id.profile_full).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       Intent i = new Intent("it.polimi.spf.app.ShowProfile");
        if(!SELF_IDENTIFIER.equals(mProfileIdentifier)){
            i.putExtra(EXTRA_IDENTIFIER, mProfileIdentifier);
        }

       startActivity(i);
    }
}
