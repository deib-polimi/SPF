package it.polimi.spf.demo.couponing.provider;

import it.polimi.spf.lib.notification.SPFNotification;
import it.polimi.spf.shared.model.SPFAction;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFTrigger;
import it.polimi.spf.shared.model.SPFTrigger.IllegalTriggerException;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class WelcomeMessageFragment extends Fragment{

	public static Fragment newInstance() {
		return new WelcomeMessageFragment();
	}
	
    private static final String TAG = "CategoryFragment";

	private static final String PREF_WELCOME_MSG = "welcomeMsgPref";
	private static final String KEY_TRIGGER_ID = "wm_trigger_id";
	private static final String KEY_MESSAGE_TITLE = "wm_message_title";
	private static final String KEY_MESSAGE_TEXT = "wm_message_text";
    
    private SPFNotification mNotificationService;
    private EditText mTextInput, mTitleInput;
    private CheckBox mActive;

    private final SPFNotification.Callback mNotificationCallback = new SPFNotification.Callback() {

        @Override
        public void onServiceReady(SPFNotification service) {
            mNotificationService = service;
        }

        @Override
        public void onError(SPFError errorMsg) {
            Log.e(TAG, "Error in local profile: " + errorMsg);
            mNotificationService = null;
        }

        @Override
        public void onDisconnect() {
            mNotificationService = null;
        }
    };

	private final OnCheckedChangeListener mActiveToggleListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			SharedPreferences prefs = getSharedPreferences();
			long triggerId = prefs.getLong(KEY_TRIGGER_ID, -1);
			
			if(isChecked){
				String text = mTextInput.getText().toString();
				String title = mTitleInput.getText().toString();
				if(text.length() == 0 || title.length() == 0){
					toast(R.string.error_welcome_message_missing_detail);
					mActive.setChecked(false);
					return;
				}
				
				if(triggerId != -1){
					// Delete trigger
					mNotificationService.deleteTrigger(triggerId);
				}
				
				SPFQuery q = new SPFQuery.Builder()
					.setAppIdentifier("it.polimi.spf.demo.couponing.provider")
					.build();
				
				SPFAction a = new SPFActionSendNotification(title, text);
				SPFTrigger trigger; 
				
				try{
					trigger = new SPFTrigger("Welcome message", q, a);
				}catch(IllegalTriggerException e){
					toast(R.string.error_trigger_invalid);
					return;
				}
				
				if(!mNotificationService.saveTrigger(trigger)){
					toast(R.string.error_trigger_not_saved);
					return;
				}
				
				prefs.edit()
					.putLong(KEY_TRIGGER_ID, trigger.getId())
					.putString(KEY_MESSAGE_TITLE, title)
					.putString(KEY_MESSAGE_TEXT, text)
					.apply();
				
			} else if (triggerId > -1){
				// Delete trigger
				mNotificationService.deleteTrigger(triggerId);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_welcome_message, container, false);
		
		mTitleInput = (EditText) root.findViewById(R.id.welcome_message_title_input);
		mTextInput = (EditText) root.findViewById(R.id.welcome_message_text_input);
		mActive = (CheckBox) root.findViewById(R.id.welcome_message_active);
        
		SharedPreferences prefs = getSharedPreferences();
		if(prefs.getLong(KEY_TRIGGER_ID, -1) != -1){
			mActive.setChecked(true);
			mTitleInput.setText(prefs.getString(KEY_MESSAGE_TITLE, null));
			mTextInput.setText(prefs.getString(KEY_MESSAGE_TEXT, null));
		}
		
		mActive.setOnCheckedChangeListener(mActiveToggleListener );
		return root;
	}
	
    @Override
    public void onResume() {
        super.onResume();
        SPFNotification.load(getActivity(), mNotificationCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNotificationService != null) {
            mNotificationService.disconnect();
        }
    }
    
    private SharedPreferences getSharedPreferences(){
    	return getActivity()
    			.getSharedPreferences(PREF_WELCOME_MSG, Context.MODE_PRIVATE);
    }

	private void toast(int resId) {
		Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
	}
    
}
