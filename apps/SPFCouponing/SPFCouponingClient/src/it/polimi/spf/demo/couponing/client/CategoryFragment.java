package it.polimi.spf.demo.couponing.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.polimi.spf.lib.notification.SPFNotification;
import it.polimi.spf.lib.profile.SPFLocalProfile;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFAction;
import it.polimi.spf.shared.model.SPFActionIntent;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFTrigger;
import it.polimi.spf.shared.model.SPFTrigger.IllegalTriggerException;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CategoryFragment extends Fragment {

	public static Fragment newInstance() {
		return new CategoryFragment();
	}

	private static final String TAG = "CategoryFragment";
	private static final String INTENT_ACTION = "it.polimi.spf.demo.couponing.client.TRIGGER_INTENT";
	private static final long SLEEP_PERIOD = 60 * 1000;
	private static final String PREF_FILE = "trigger_cache";

	private SPFLocalProfile mLocalProfile;
	private SPFNotification mNotificationService;
	private ProfileFieldContainer mContainer;

	private TextView mEmptyView;
	private ListView mList;
	private ArrayAdapter<String> mAdapter;
	private ActionMode mActionMode;

	private final SPFLocalProfile.Callback mProfileCallbacks = new SPFLocalProfile.Callback() {

		@Override
		public void onServiceReady(SPFLocalProfile service) {
			mLocalProfile = service;
			mContainer = mLocalProfile.getValueBulk(ProfileField.INTERESTS);
		}

		@Override
		public void onError(SPFError errorMsg) {
			Log.e(TAG, "Error in local profile: " + errorMsg);
			mLocalProfile = null;
		}

		@Override
		public void onDisconnect() {
			mLocalProfile = null;
		}
	};

	private final SPFNotification.Callback mNotificationCallbacks = new SPFNotification.Callback() {

		@Override
		public void onServiceReady(SPFNotification service) {
			mNotificationService = service;
		}

		@Override
		public void onError(SPFError errorMsg) {
			Log.e(TAG, "Error in notification service: " + errorMsg);
			mNotificationService = null;
		}

		@Override
		public void onDisconnect() {
			mNotificationService = null;
		}
	};

	private AbsListView.MultiChoiceModeListener mChoiceListener = new AbsListView.MultiChoiceModeListener() {

        private Set<Integer> mSelectedPositions;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if(checked){
                mSelectedPositions.add(position);
            } else {
                mSelectedPositions.remove(position);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_category_action_mode, menu);
            mActionMode = mode;
            mSelectedPositions = new HashSet<Integer>();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.action_category_delete){
                for(int i : mSelectedPositions){
                    deleteCategory((String) mList.getItemAtPosition(i));
                }
                loadCategoryList();
                mActionMode.finish();
            }

            return true;
        }

		@Override
        public void onDestroyActionMode(ActionMode mode) {
            mSelectedPositions = null;
            mActionMode = null;
        }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_category_list, container, false);

		mEmptyView = (TextView) root.findViewById(R.id.category_list_empty);
		mList = (ListView) root.findViewById(R.id.category_list);
		mList.setEmptyView(mEmptyView);
		
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1);
		mList.setAdapter(mAdapter);
		
		mList.setMultiChoiceModeListener(mChoiceListener);
		mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		loadCategoryList();
	}

	@Override
	public void onResume() {
		super.onResume();
		SPFLocalProfile.load(getActivity(), mProfileCallbacks);
		SPFNotification.load(getActivity(), mNotificationCallbacks);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocalProfile != null) {
			mLocalProfile.disconnect();
		}

		if (mNotificationService != null) {
			mNotificationService.disconnect();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_category, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_category_add) {
			onCategoryAdd();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		
		if(mActionMode != null && !isVisibleToUser){
			mActionMode.finish();
		}
	};

	private void onCategoryAdd() {
		List<String> categories = ClientApplication.get().getCouponDatabase().getCategories();
		Set<String> selected = getPreferences().getAll().keySet();
		categories.removeAll(selected);
		
		final CategoryDialogView view = new CategoryDialogView(getActivity(), categories);

		new AlertDialog.Builder(getActivity())
		.setTitle("Add category")
		.setView(view)
		.setPositiveButton("Add", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String category = view.getSelectedCategory();
				if (!saveCategory(category)) {
					return;
				}

				if (view.isProfileOptionChecked()) {
					addCategoryToProfile(category);
				}

				loadCategoryList();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	private boolean saveCategory(String category) {
		if (mNotificationService == null) {
			toast(R.string.error_notification_service_not_available);
			return false;
		}

		SPFQuery q = new SPFQuery.Builder()
			.setTag(category)
			.setAppIdentifier("it.polimi.spf.demo.couponing.provider")
			.build();

		SPFAction a = new SPFActionIntent(INTENT_ACTION);
		SPFTrigger t;
		try {
			t = new SPFTrigger("Category " + category + " trigger", q, a, SLEEP_PERIOD);
		} catch (IllegalTriggerException e) {
			toast(R.string.error_notification_service_not_available);
			return false;
		}

		if (!mNotificationService.saveTrigger(t)) {
			toast(R.string.error_cannot_save_trigger);
			return false;
		}

		getPreferences().edit()
			.putLong(category, t.getId())
			.apply();

		return true;
	}

    private void deleteCategory(String category) {
		if(mNotificationService == null){
			toast(R.string.error_notification_service_not_available);
			return;
		}
		
		SharedPreferences pf = getPreferences();
		long triggerId = pf.getLong(category, -1);
    	if(!mNotificationService.deleteTrigger(triggerId)){
    		toast(R.string.error_cannot_delete_trigger);
    		//return;
    	}
    	
    	getPreferences()
			.edit()
			.remove(category)
			.apply();
	}
	
	private void addCategoryToProfile(String category) {
		if (mContainer == null) {
			toast(R.string.error_profile_service_not_available);
			return;
		}

		String[] catArray = mContainer.getFieldValue(ProfileField.INTERESTS);
		if (catArray == null) {
			catArray = new String[0];
		}

		List<String> cat = new ArrayList<String>(Arrays.asList(catArray));
		if (cat.indexOf(category) == -1) {
			cat.add(category);
			mContainer.setFieldValue(ProfileField.INTERESTS, cat.toArray(new String[cat.size()]));
			mLocalProfile.setValueBulk(mContainer);
			mContainer.clearModified();
		}
	}

	private void loadCategoryList() {
		Set<String> categories = getPreferences()
				.getAll()
				.keySet();
		
		mAdapter.clear();
		mAdapter.addAll(categories);
	}

	private SharedPreferences getPreferences() {
		return getActivity()
				.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
	}

	private void toast(int resId) {
		Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
	}

	private class CategoryDialogView extends LinearLayout {

		private Spinner mCategorySpinner;
		private CheckBox mProfileCheckbox;

		public CategoryDialogView(Context context, List<String> categories) {
			super(context);
			inflate(getContext(), R.layout.view_category_dialog, this);

			mCategorySpinner = (Spinner) findViewById(R.id.category_dialog_spinner);
			mProfileCheckbox = (CheckBox) findViewById(R.id.category_dialog_profile_check);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, categories);
			mCategorySpinner.setAdapter(adapter);
		}

		public String getSelectedCategory() {
			return (String) mCategorySpinner.getSelectedItem();
		}

		public boolean isProfileOptionChecked() {
			return mProfileCheckbox.isChecked();
		}
	}
}
