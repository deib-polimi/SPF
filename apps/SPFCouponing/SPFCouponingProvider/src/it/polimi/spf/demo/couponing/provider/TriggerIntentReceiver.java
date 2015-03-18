package it.polimi.spf.demo.couponing.provider;

import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.lib.search.SPFSearch;
import it.polimi.spf.lib.services.ServiceInvocationException;
import it.polimi.spf.shared.model.SPFActionIntent;
import it.polimi.spf.shared.model.SPFError;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class TriggerIntentReceiver extends BroadcastReceiver {

	protected static final String TAG = "TriggerIntentReceiver";

	protected static final int MESSAGE_SEARCH = 0;
	protected static final int MESSAGE_EXECUTE = 1;

	private Handler mHandler;

	private Handler.Callback mHandlerCallback = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			HandlerData data = (HandlerData) msg.obj;

			switch (msg.what) {
			case MESSAGE_SEARCH:
				SPFSearch search = data.spf.getComponent(SPF.SEARCH);
				String identifier = getTargetIdentifier(data.extras);
				SPFPerson target = search.lookup(identifier);
				if (target == null) {
					Log.e(TAG, "Person " + identifier + " not found after second search");
					return true;
				}

				data.person = target;
				mHandler.obtainMessage(MESSAGE_EXECUTE, data).sendToTarget();
				return true;
			case MESSAGE_EXECUTE:
				CouponDeliveryService svc = data.person.getServiceInterface(CouponDeliveryService.class, data.spf);
				CouponDatabase db = ProviderApplication.get().getCouponDatabase();
				long triggerId = getTriggerId(data.extras);
				Coupon coupon = db.getCouponByTriggerId(triggerId);
				
				if(coupon == null){
					Log.e(TAG, "No coupon found for trigger id " + triggerId);
					return true;
				}
				
				coupon.setTriggerId(-1);
				coupon.setId(-1);
				
				try{
					svc.deliverCoupon(coupon);
				} catch(ServiceInvocationException e){
					Log.e(TAG, "Could not deliver coupon " + coupon + " to " +data.person.getIdentifier(), e);
				}
				
				Log.d(TAG, "Coupon " + coupon + " delivered to " + data.person.getIdentifier());
				data.spf.disconnect();
				return true;
			default:
				return false;
			}
		}
	};

	public TriggerIntentReceiver() {
		HandlerThread th = new HandlerThread("TriggerIntentReceiver");
		th.start();
		mHandler = new Handler(th.getLooper(), mHandlerCallback);
	}

	@Override
	public void onReceive(Context context, final Intent intent) {
		Log.d(TAG, "Intent received");
		SPF.connect(ProviderApplication.get(), new SPF.ConnectionListener() {

			@Override
			public void onError(SPFError error) {
				Log.e(TAG, "Error in SPF: " + error);
			}

			@Override
			public void onDisconnected() {
				// Do nothing
			}

			@Override
			public void onConnected(SPF instance) {
				SPFSearch search = instance.getComponent(SPF.SEARCH);
				HandlerData data = new HandlerData();
				data.spf = instance;
				data.extras = intent.getExtras();

				SPFPerson target = search.lookup(getTargetIdentifier(data.extras));

				if (target == null) {
					// Try postponing the request
					Message message = mHandler.obtainMessage(MESSAGE_SEARCH, data);
					mHandler.sendMessageDelayed(message, 2000);
				} else {
					data.person = target;
					mHandler.obtainMessage(MESSAGE_EXECUTE, data).sendToTarget();
				}
			}
		});
	}

	private class HandlerData {
		public SPF spf;
		public Bundle extras;
		public SPFPerson person;
	}

	private long getTriggerId(Bundle data) {
		return data.getLong(SPFActionIntent.ARG_LONG_TRIGGER_ID);
	}

	private String getTargetIdentifier(Bundle data) {
		return data.getString(SPFActionIntent.ARG_STRING_TARGET);
	}

}
