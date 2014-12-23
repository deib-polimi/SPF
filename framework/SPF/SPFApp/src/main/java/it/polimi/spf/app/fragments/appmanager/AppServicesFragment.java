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

import java.util.Arrays;
import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.AppAuth;
import it.polimi.spf.shared.model.SPFServiceDescriptor;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppServicesFragment extends Fragment {

	private AppAuth mAppAuth;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.appmanager_service_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView serviceList = (ListView) getView().findViewById(R.id.appmanager_service_list);

		if (savedInstanceState == null) {
			mAppAuth = getArguments().getParcelable(AppDetailActivity.APP_AUTH_KEY);
		} else {
			mAppAuth = savedInstanceState.getParcelable(AppDetailActivity.APP_AUTH_KEY);
		}

		if (mAppAuth == null) {
			throw new IllegalStateException("AppAuth not found");
		}

		SPFServiceDescriptor[] services = SPF.get().getServiceRegistry().getServicesOfApp(mAppAuth.getAppIdentifier());
		serviceList.setAdapter(new ServiceAdapter(getActivity(), Arrays.asList(services)));
		serviceList.setEmptyView(getView().findViewById(R.id.app_manager_services_list_emptyview));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(AppDetailActivity.APP_AUTH_KEY, mAppAuth);
	}

	private static class ServiceAdapter extends ArrayAdapter<SPFServiceDescriptor> {

		public ServiceAdapter(Context context, List<SPFServiceDescriptor> services) {
			super(context, android.R.layout.simple_list_item_2, services);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(R.layout.appmanager_service_entry, parent, false);
			ViewHolder holder = ViewHolder.from(v);
			SPFServiceDescriptor desc = getItem(position);
			holder.name.setText(desc.getServiceName());
			holder.desc.setText(desc.getDescription());
			return v;
		}
	}

	private static class ViewHolder {

		public static ViewHolder from(View v) {
			Object o = v.getTag();
			if (o != null && o instanceof ViewHolder) {
				return (ViewHolder) o;
			}

			ViewHolder holder = new ViewHolder(v);
			v.setTag(holder);
			return holder;
		}

		public final TextView desc;
		public final TextView name;

		private ViewHolder(View view) {
			this.name = (TextView) view.findViewById(R.id.appmanager_service_name);
			this.desc = (TextView) view.findViewById(R.id.appmanager_service_description);
		}
	}
}
