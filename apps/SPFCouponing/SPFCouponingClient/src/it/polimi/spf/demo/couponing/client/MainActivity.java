package it.polimi.spf.demo.couponing.client;

import it.polimi.spf.lib.services.SPFServiceRegistry;
import it.polimi.spf.shared.model.SPFError;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class MainActivity extends Activity {

	protected static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
		pager.setAdapter(new PagerConfigurator(this, getFragmentManager()));
		registerService();
		
		PagerTabStrip tabs = (PagerTabStrip) findViewById(R.id.main_tabs);
		tabs.setTabIndicatorColorResource(R.color.selection);
	}

	private void registerService(){
		SPFServiceRegistry.load(this, new SPFServiceRegistry.Callback() {
			@Override
			public void onServiceReady(SPFServiceRegistry localServiceRegistry) {
				localServiceRegistry.registerService(CouponDeliveryService.class, CouponDeliveryServiceImpl.class);
				localServiceRegistry.disconnect();
			}

			@Override
			public void onError(SPFError spfError) {
				Log.e(TAG, "Could not register service: " + spfError);
			}

			@Override
			public void onDisconnect() {
			}
		});
	}
	
	private static class PagerConfigurator extends FragmentPagerAdapter {

		private final static int PAGE_COUNT = 2;

		private final String[] mPageTitles;

		private PagerConfigurator(Context c, FragmentManager fm) {
			super(fm);
			this.mPageTitles = c.getResources().getStringArray(R.array.main_tabs_titles);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return CouponManagerFragment.newInstance();
			case 1:
				return CategoryFragment.newInstance();

			default:
				throw new IndexOutOfBoundsException("Requested page " + i + ", total " + PAGE_COUNT);
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
	
}
