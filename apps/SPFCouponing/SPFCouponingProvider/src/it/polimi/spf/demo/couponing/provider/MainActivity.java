package it.polimi.spf.demo.couponing.provider;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
		pager.setAdapter(new PagerConfigurator(this, getFragmentManager()));
	}

	private static class PagerConfigurator extends FragmentPagerAdapter {

		private final static int PAGE_COUNT = 3;

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
			case 2:
				return WelcomeMessageFragment.newInstance();
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
