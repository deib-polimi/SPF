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
package it.polimi.spf.app.fragments.appmanager;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.security.AppAuth;
import it.polimi.spf.shared.model.Permission;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AppPermissionsFragment extends Fragment {

	private AppAuth mAppAuth;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.appmanager_permission_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState == null) {
			mAppAuth = getArguments().getParcelable(AppDetailActivity.APP_AUTH_KEY);
		} else {
			mAppAuth = savedInstanceState.getParcelable(AppDetailActivity.APP_AUTH_KEY);
		}

		ListView list = (ListView) getView().findViewById(R.id.appmanager_permission_list);
		Permission[] permissionList = mAppAuth.getPermissions();
		list.setAdapter(new PermissionArrayAdapter(getActivity(), permissionList));
		list.setEmptyView(getView().findViewById(R.id.app_manager_permission_list_emptyview));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(AppDetailActivity.APP_AUTH_KEY, mAppAuth);
	}
}
