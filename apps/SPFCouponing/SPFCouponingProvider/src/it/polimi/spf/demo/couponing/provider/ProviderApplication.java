package it.polimi.spf.demo.couponing.provider;

import it.polimi.spf.lib.SPFPermissionManager;
import it.polimi.spf.shared.model.Permission;
import android.app.Application;

public class ProviderApplication extends Application{

	private static ProviderApplication instance;
	private CouponDatabase mDatabase;
	
	public static ProviderApplication get(){
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mDatabase = new CouponDatabase(this);
		
		SPFPermissionManager.get().requirePermission(
				Permission.READ_LOCAL_PROFILE,
				Permission.WRITE_LOCAL_PROFILE,
				Permission.EXECUTE_REMOTE_SERVICES,
				Permission.NOTIFICATION_SERVICES,
				Permission.SEARCH_SERVICE);
	}
	
	public CouponDatabase getCouponDatabase(){
		return mDatabase;
	}
}
