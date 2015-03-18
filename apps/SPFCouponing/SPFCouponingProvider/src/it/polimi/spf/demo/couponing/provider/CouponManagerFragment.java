package it.polimi.spf.demo.couponing.provider;

import java.util.List;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CouponManagerFragment extends Fragment {

	private static final int LOADER_COUPON_ID = 0;

	public static Fragment newInstance() {
		return new CouponManagerFragment();
	}

	private LoaderCallbacks<List<Coupon>> mLoaderCallbacks = new LoaderCallbacks<List<Coupon>>() {

		@Override
		public void onLoaderReset(Loader<List<Coupon>> arg0) {
			// Do nothing
		}

		@Override
		public void onLoadFinished(Loader<List<Coupon>> arg0, List<Coupon> coupons) {
			mAdapter.clear();
			if (coupons.size() == 0) {
				mEmpty.setText(R.string.coupon_list_empty);
			} else {
				mAdapter.addAll(coupons);
			}
		}

		@Override
		public Loader<List<Coupon>> onCreateLoader(int id, Bundle args) {
			return new AsyncTaskLoader<List<Coupon>>(getActivity()) {

				@Override
				public List<Coupon> loadInBackground() {
					return ProviderApplication.get().getCouponDatabase().getAllCoupons();
				}
			};
		}
	};

	private ListView mCouponList;
	private ArrayAdapter<Coupon> mAdapter;
	private TextView mEmpty;

	private final OnItemClickListener mCouponClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
			Coupon c = (Coupon) parent.getItemAtPosition(position);
			Intent i = CouponDetailActivity.newIntent(getActivity(), c.getId());
			startActivity(i);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_coupon_list, container, false);
		mCouponList = (ListView) root.findViewById(R.id.coupon_list);
		mEmpty = (TextView) root.findViewById(R.id.coupon_list_empty);
		mCouponList.setEmptyView(mEmpty);
		mCouponList.setOnItemClickListener(mCouponClickListener );

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new CouponAdapter(getActivity());
		mCouponList.setAdapter(mAdapter);
		
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(LOADER_COUPON_ID, null, mLoaderCallbacks).forceLoad();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_coupon_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_coupon_add) {
			Intent i = CouponCreationActivity.newIntent(getActivity());
			startActivity(i);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class CouponAdapter extends ArrayAdapter<Coupon> {
		public CouponAdapter(Context c) {
			super(c, android.R.layout.simple_list_item_1);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(R.layout.coupon_list_entry, parent, false);
			ViewHolder holder = ViewHolder.from(view);
			Coupon coupon = getItem(position);

			holder.title.setText(coupon.getTitle());
			holder.category.setText(coupon.getCategory());
			holder.photo.setImageBitmap(BitmapFactory.decodeByteArray(coupon.getPhoto(), 0, coupon.getPhoto().length));

			return view;
		}
	}

	private static class ViewHolder {

		public static ViewHolder from(View view) {
			Object o = view.getTag();
			if (o != null && (o instanceof ViewHolder)) {
				return (ViewHolder) o;
			}

			//@formatter:off
			ViewHolder holder = new ViewHolder(
					(ImageView) view.findViewById(R.id.coupon_entry_photo),
					(TextView) view.findViewById(R.id.coupon_entry_title),
					(TextView) view.findViewById(R.id.coupon_entry_category));
			//@formatter:on

			view.setTag(holder);
			return holder;
		}

		public final ImageView photo;
		public final TextView title, category;

		private ViewHolder(ImageView photo, TextView title, TextView category) {
			this.photo = photo;
			this.title = title;
			this.category = category;
		}
	}
}
