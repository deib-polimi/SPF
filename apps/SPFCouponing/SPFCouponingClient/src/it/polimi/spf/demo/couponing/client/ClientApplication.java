package it.polimi.spf.demo.couponing.client;

import it.polimi.spf.lib.SPFPermissionManager;
import it.polimi.spf.shared.model.Permission;
import android.app.Application;

public class ClientApplication extends Application{

	private static ClientApplication instance;
	private CouponDatabase mDatabase;
	
	public static ClientApplication get(){
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
				Permission.REGISTER_SERVICES,
				Permission.NOTIFICATION_SERVICES);

	}
	
	public CouponDatabase getCouponDatabase(){
		return mDatabase;
	}
}
