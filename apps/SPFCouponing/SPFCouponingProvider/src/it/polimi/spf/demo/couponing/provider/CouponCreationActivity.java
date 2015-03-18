package it.polimi.spf.demo.couponing.provider;

import it.polimi.spf.lib.notification.SPFNotification;
import it.polimi.spf.shared.model.SPFAction;
import it.polimi.spf.shared.model.SPFActionIntent;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFTrigger;
import it.polimi.spf.shared.model.SPFTrigger.IllegalTriggerException;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class CouponCreationActivity extends Activity {

	private static final String TAG = "CouponCreationActivity";
	private static final int CODE_EDIT_PHOTO = 1;
	private static final String PHOTO_WIDTH = "50dp";
	private static final String PHOTO_HEIGHT = "50dp";
	private static final String TRIGGER_INTENT_ACTION = "it.polimi.spf.demo.couponing.COUPON_TRIGGERED";
	private static final long SLEEP_PERIOD = 60 * 1000;

	public static Intent newIntent(Context context) {
		Intent i = new Intent(context, CouponCreationActivity.class);
		return i;
	}

	private ImageView mPhotoInput;
	private EditText mTitleInput, mTextInput;
	private Spinner mCategoryInput;
	
	private Bitmap mPhoto;
	private SPFNotification mNotificationService;

	private OnClickListener mPhotoClickListener = new OnClickListener() {

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
			startActivityForResult(intent, CODE_EDIT_PHOTO);
		}
	};
	
	private SPFNotification.Callback mNotificationServiceCallback = new SPFNotification.Callback() {
		
		@Override
		public void onServiceReady(SPFNotification componentInstance) {
			mNotificationService = componentInstance;
		}
		
		@Override
		public void onError(SPFError err) {
			mNotificationService = null;
			Log.e(TAG, "Error from notification service: " + err);
		}
		
		@Override
		public void onDisconnect() {
			mNotificationService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coupon_creation);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mPhotoInput = (ImageView) findViewById(R.id.coupon_photo);
		mTitleInput = (EditText) findViewById(R.id.coupon_title);
		mTextInput = (EditText) findViewById(R.id.coupon_text);
		mCategoryInput = (Spinner) findViewById(R.id.coupon_category);

		String[] categories = ProviderApplication.get().getCouponDatabase().getCategories();
		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
		mCategoryInput.setAdapter(categoryAdapter);

		mPhotoInput.setOnClickListener(mPhotoClickListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		SPFNotification.load(this, mNotificationServiceCallback );
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mNotificationService != null){
			mNotificationService.disconnect();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_coupon_creation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_coupon_save:
			onSave();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_EDIT_PHOTO) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			}

			if (data != null && data.getExtras() != null) {
				mPhoto = data.getExtras().getParcelable("data");
				mPhotoInput.setImageBitmap(mPhoto);
				mPhotoInput.invalidate();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void onSave() {
		try {
			checkInput();
		} catch (InputException e) {
			toast(e.getMessageResId());
			return;
		}

		Coupon coupon = new Coupon();
		coupon.setTitle(mTitleInput.getText().toString());
		coupon.setText(mTextInput.getText().toString());
		coupon.setCategory((String) mCategoryInput.getSelectedItem());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mPhoto.compress(CompressFormat.JPEG, 50, baos);
		coupon.setPhoto(baos.toByteArray());

		if(mNotificationService == null){
			toast(R.string.error_notification_service_unavailable);
			return;
		}
		
		SPFQuery query = new SPFQuery.Builder()
			.setTag(coupon.getCategory())
			.setAppIdentifier("it.polimi.spf.demo.couponing.client")
			.build();
		
		SPFAction action = new SPFActionIntent(TRIGGER_INTENT_ACTION);
		SPFTrigger trigger;
		try {
			trigger = new SPFTrigger("Coupon " + coupon.getTitle() + " trigger", query, action, SLEEP_PERIOD);
		} catch (IllegalTriggerException e) {
			toast(R.string.error_trigger_invalid);
			return;
		}
		
		if(!mNotificationService.saveTrigger(trigger)){
			toast(R.string.error_trigger_not_saved);
			return;
		}
		
		coupon.setTriggerId(trigger.getId());
		ProviderApplication.get().getCouponDatabase().saveCoupon(coupon);
		finish();
	}

	private void toast(int messageResId) {
		Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
	}

	private void checkInput() throws InputException {
		if(mPhoto == null){
			throw new InputException(R.string.error_coupon_photo_empty);
		}
		
		if (mTitleInput.getText().length() == 0) {
			throw new InputException(R.string.error_coupon_title_empty);
		}

		if (mTextInput.getText().length() == 0) {
			throw new InputException(R.string.error_coupon_text_empty);
		}

		if (mCategoryInput.getSelectedItem() == null) {
			throw new InputException(R.string.error_coupon_category_empty);
		}
	}

	private class InputException extends Exception {

		private static final long serialVersionUID = -3918089388542981197L;
		private int mMessageResId;

		public InputException(int messageResId) {
			super();
			mMessageResId = messageResId;
		}

		public int getMessageResId() {
			return mMessageResId;
		}
	}
}
