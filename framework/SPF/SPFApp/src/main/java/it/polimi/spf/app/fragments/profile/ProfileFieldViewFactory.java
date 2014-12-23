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
package it.polimi.spf.app.fragments.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import it.polimi.spf.app.R;
import it.polimi.spf.app.fragments.profile.Helper.InvalidValueException;
import it.polimi.spf.app.fragments.profile.ProfileFragment.Mode;
import it.polimi.spf.app.view.CircleSelectSpinner;
import it.polimi.spf.app.view.CircleSelectSpinner.OnSelectionChangedListener;
import it.polimi.spf.app.view.TagsPicker;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileField.DateProfileField;
import it.polimi.spf.shared.model.ProfileField.MultipleChoicheProfileField;
import it.polimi.spf.shared.model.ProfileField.TagProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Factory class that creates the most appropriate {@link View}s to display
 * and/or edit the values and clearance of {@link ProfileField}. Views are
 * created accordingly to:
 * <ul>
 * <li>The nature of the profile field</li>
 * <li>The {@link Mode} of visualization of the field.
 * </ul>
 * 
 * @author darioarchetti
 * 
 */
public final class ProfileFieldViewFactory {

	private final Context mContext;
	private final LayoutInflater mInflater;
	private final ProfileFragment.Mode mMode;
	private final Helper mHelper;

	private ProfileFieldContainer mContainer;
	private SPFPersona mPersona;

	/**
	 * Creates a new instance of {@link ProfileFieldViewFactory}.
	 * 
	 * @param context
	 *            - the context that will be used to inflate views
	 * @param mode
	 *            - the visualization mode for fields
	 * @param persona
	 *            - the persona used to retrieve field values. Required for
	 *            modes {@link Mode#SELF} and {@link Mode#EDIT}
	 * @param container
	 *            - the container to be used as source for current values of
	 *            profile fields.
	 */
	public ProfileFieldViewFactory(Context context, ProfileFragment.Mode mode, SPFPersona persona, ProfileFieldContainer container) {
		this.mContext = context;
		this.mMode = mode;
		this.mHelper = new Helper(context);
		this.mInflater = LayoutInflater.from(context);
		this.mContainer = container;
		this.mPersona = persona;
	}

	/**
	 * Creates a new view to visualize the value and the clearance of the given
	 * profile field. If the {@link Mode} of visualization is {@link Mode#EDIT},
	 * the view will support modifications. Such modifications will trigger the
	 * appropriate call to a {@link FieldValueListener}, if provided.
	 * 
	 * @param field
	 *            - the field to visualize
	 * @param container
	 *            - the {@link ViewGroup} to which the view is intended to be
	 *            attached, as in {@link LayoutInflater#inflate(int, ViewGroup)}
	 * @param listener
	 *            - the listener that will be notified of value or clearance
	 *            changes in {@link Mode#EDIT} mode.
	 * @return - the view that visualizes the profile field. Please note that
	 *         the view is <strong>NOT</strong> attached to the container;
	 */
	@SuppressWarnings("unchecked")
	public <E> View createViewForField(ProfileField<E> field, ViewGroup container, FieldValueListener<E> listener) {
		E currentValue = mContainer.getFieldValue(field);

		if (field instanceof TagProfileField) {
			return createTagView((TagProfileField) field, (String[]) currentValue, listener, container, mMode == Mode.EDIT);
		}

		switch (mMode) {
		case SELF:
		case REMOTE:
			return createStandardDisplayView(field, currentValue, container);
		case EDIT:
			if (field instanceof MultipleChoicheProfileField) {
				return createSpinner((MultipleChoicheProfileField<E>) field, currentValue, listener, container);
			} else if (field instanceof DateProfileField) {
				return createDateView((DateProfileField) field, (Date) currentValue, (FieldValueListener<Date>) listener, container);
			} else {
				return createStandardEditView(field, currentValue, listener, container);
			}

		default:
			return null;
		}
	}

	// Standard display view for all profile fields except tags
	private <E> View createStandardDisplayView(ProfileField<E> field, E currentValue, ViewGroup viewContainer) {
		View result = mInflater.inflate(R.layout.profileview_field_listelement, viewContainer, false);

		String friendlyFieldName = mHelper.getFriendlyNameOfField(field);
		((TextView) result.findViewById(R.id.profile_field_key)).setText(friendlyFieldName);

		String fieldValue = mHelper.convertToFriendlyString(field, currentValue);
		((TextView) result.findViewById(R.id.profile_field_value)).setText(fieldValue);

		setUpCircleView(result, field, null);
		return result;
	}

	// Edit view for multiple choiche fields
	private <E> View createSpinner(MultipleChoicheProfileField<E> field, E currentValue, FieldValueListener<E> listener, ViewGroup container) {
		View result = mInflater.inflate(R.layout.profileedit_field_multiplechoiche, container, false);

		String friendlyName = mHelper.getFriendlyNameOfField(field);
		((TextView) result.findViewById(R.id.profileedit_field_identifier)).setText(friendlyName);

		Spinner spinner = (Spinner) result.findViewById(R.id.profileedit_field_multiple_value);
		ArrayAdapter<E> adapter = new ArrayAdapter<E>(mContext, android.R.layout.simple_list_item_1, field.getChoiches());
		spinner.setAdapter(adapter);

		int index = indexOf(field.getChoiches(), currentValue);
		if (index >= 0) {
			spinner.setSelection(index, false);
		}

		spinner.setOnItemSelectedListener(new OnItemSelectedAdapter<E>(field, listener, adapter));
		setUpCircleView(result, field, listener);
		return result;
	}

	// Edit view for date fields
	@SuppressWarnings("unchecked")
	private <E> View createDateView(final DateProfileField field, Date currentValue, final FieldValueListener<Date> listener, ViewGroup container) {
		View result = mInflater.inflate(R.layout.profileedit_field_date, container, false);
		String friendlyName = mHelper.getFriendlyNameOfField(field);
		((TextView) result.findViewById(R.id.profileedit_field_identifier)).setText(friendlyName);

		String dateToShow;
		if (currentValue == null) {
			dateToShow = "Click to edit";
		} else {
			dateToShow = mHelper.convertToFriendlyString(field, currentValue);
		}
		final TextView dateTextView = (TextView) result.findViewById(R.id.profileedit_field_date_text);
		dateTextView.setText(dateToShow);
		dateTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar today = GregorianCalendar.getInstance();
				int year = today.get(Calendar.YEAR);
				int monthOfYear = today.get(Calendar.MONTH);
				int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
				OnDateSetListener callBack = new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						GregorianCalendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
						Date newDate = new Date(cal.getTimeInMillis());
						String newDateString = mHelper.convertToFriendlyString(field, newDate);
						dateTextView.setText(newDateString);
						listener.onFieldValueChanged(field, newDate);
					}
				};
				DatePickerDialog dialog = new DatePickerDialog(mContext, callBack, year, monthOfYear, dayOfMonth);
				dialog.show();
			}
		});

		setUpCircleView(result, (ProfileField<E>) field, (FieldValueListener<E>) listener);

		return result;
	}

	// Edit and display view for tags
	@SuppressWarnings("unchecked")
	private <E> View createTagView(TagProfileField field, String[] currentValue, FieldValueListener<E> listener, ViewGroup container, boolean editable) {
		View result = mInflater.inflate(editable ? R.layout.profileedit_field_tag : R.layout.profileview_tag_field, container, false);

		String friendlyName = mHelper.getFriendlyNameOfField(field);
		((TextView) result.findViewById(R.id.profileedit_field_identifier)).setText(friendlyName);

		TagsPicker picker = (TagsPicker) result.findViewById(R.id.profileedit_tags_picker);
		picker.setEditable(editable);

		if (currentValue != null) {
			picker.setInitialTags(Arrays.asList(currentValue));
		}
		if (editable) {
			picker.setChangeListener(new OnChangeListenerAdapter(field, (FieldValueListener<String[]>) listener));
		}

		setUpCircleView(result, (ProfileField<E>) field, listener);

		return result;
	}

	// Edit view for all other fields
	private <E> View createStandardEditView(ProfileField<E> field, E currentValue, FieldValueListener<E> listener, ViewGroup container) {
		View result = mInflater.inflate(R.layout.profileedit_field_standard, container, false);

		String friendlyName = mHelper.getFriendlyNameOfField(field);
		((TextView) result.findViewById(R.id.profileedit_field_identifier)).setText(friendlyName);

		EditText editText = (EditText) result.findViewById(R.id.profileedit_field_value);
		editText.setOnEditorActionListener(new OnEditorActionAdapter<E>(listener, field));
		if (currentValue != null) {
			editText.setText(mHelper.convertToFriendlyString(field, currentValue));
		}

		setUpCircleView(result, field, listener);

		return result;
	}

	private <E> int indexOf(E[] array, E value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value || array[i].equals(value)) {
				return i;
			}
		}

		return -1;
	}

	private <E> void setUpCircleView(View root, ProfileField<E> field, FieldValueListener<E> callback) {
		switch (mMode) {
		case SELF:
			List<String> selCircles = SPF.get().getProfileManager().getCirclesOf(mPersona).getStringArrayList(field.getIdentifier());
			((TextView) root.findViewById(R.id.profile_field_clearance)).setText(mHelper.sumUpCircles(selCircles));
			break;

		case REMOTE:
			root.findViewById(R.id.profile_field_clearance).setVisibility(View.GONE);
			break;
		case EDIT:
			List<String> circles = new ArrayList<String>(SPF.get().getSecurityMonitor().getPersonRegistry().getCircles());
			List<String> selectedCircles = SPF.get().getProfileManager().getCirclesOf(mPersona).getStringArrayList(field.getIdentifier());
			CircleSelectSpinner spinner = (CircleSelectSpinner) root.findViewById(R.id.profile_field_clearance);
			spinner.setItems(circles);
			if (selectedCircles != null) {
				spinner.setSelection(selectedCircles);
			}
			spinner.setOnSelectionChangedListener(new CircleChangedListener<E>(field, callback));
		}
	}

	public class CircleChangedListener<E> implements OnSelectionChangedListener {

		private final ProfileField<E> field;
		private final FieldValueListener<E> listener;

		public CircleChangedListener(ProfileField<E> field, FieldValueListener<E> listener) {
			this.field = field;
			this.listener = listener;
		}

		@Override
		public void onItemAdded(String item) {
			listener.onCircleAdded(field, item);
		}

		@Override
		public void onItemRemoved(String item) {
			listener.onCircleRemoved(field, item);
		}

	}

	/**
	 * Interface for components that want to listen for changes of values in
	 * profiles edit view.
	 * 
	 * @author darioarchetti
	 * 
	 * @param <E>
	 */
	public static interface FieldValueListener<E> {

		/**
		 * Notifies that the user has changed the value of a field in a child
		 * fragment. If the current {@link Mode} is {@link Mode#VIEW} the call
		 * does nothing.
		 * 
		 * @param field
		 *            - the field whose value has changed
		 * @param value
		 *            - the new field value
		 */
		public void onFieldValueChanged(ProfileField<E> field, E value);

		/**
		 * Notifies that the user has entered an invalid value in a field
		 * 
		 * @param field
		 *            - the field whose value is invalid
		 * @param fieldFriendlyName
		 *            - the friendly name of the field
		 */
		public void onInvalidFieldValue(ProfileField<E> field, String friendlyFieldName);

		/**
		 * Called the user adds a circle to a field
		 * 
		 * @param field
		 *            - the field to which the circle was added
		 * @param newCircles
		 *            - the circle that was added to the field
		 */
		public void onCircleAdded(ProfileField<E> field, String circle);

		/**
		 * Called when the user removes a circle from a field
		 * 
		 * @param field
		 *            - the field from which to remove the circle
		 * @param circle
		 *            - the circle to remove from the field
		 */
		public void onCircleRemoved(ProfileField<E> field, String circle);
	}

	private class BaseAdapter<T> {
		private final FieldValueListener<T> mOriginalListener;
		private final ProfileField<T> mField;

		public BaseAdapter(FieldValueListener<T> listener, ProfileField<T> field) {
			this.mOriginalListener = listener;
			this.mField = field;
		}

		protected FieldValueListener<T> getOriginalListener() {
			return mOriginalListener;
		}

		protected ProfileField<T> getField() {
			return mField;
		}
	}

	private class OnEditorActionAdapter<T> extends BaseAdapter<T> implements OnEditorActionListener {

		public OnEditorActionAdapter(FieldValueListener<T> listener, ProfileField<T> field) {
			super(listener, field);
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			T value;
			try {
				value = mHelper.convertFromFriendlyString(getField(), v.getText().toString());
				getOriginalListener().onFieldValueChanged(getField(), value);
			} catch (InvalidValueException e) {
				getOriginalListener().onInvalidFieldValue(getField(), mHelper.getFriendlyNameOfField(getField()));
			}
			return false;
		}
	}

	private class OnItemSelectedAdapter<T> extends BaseAdapter<T> implements OnItemSelectedListener {

		private ArrayAdapter<T> mAdapter;

		public OnItemSelectedAdapter(ProfileField<T> field, FieldValueListener<T> listener, ArrayAdapter<T> adapter) {
			super(listener, field);
			this.mAdapter = adapter;
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			T choice = mAdapter.getItem(position);
			getOriginalListener().onFieldValueChanged(getField(), choice);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			getOriginalListener().onInvalidFieldValue(getField(), mHelper.getFriendlyNameOfField(getField()));
		}

	}

	private class OnChangeListenerAdapter extends BaseAdapter<String[]> implements TagsPicker.OnChangeListener {

		public OnChangeListenerAdapter(TagProfileField field, FieldValueListener<String[]> listener) {
			super(listener, field);
		}

		@Override
		public void onChange(List<String> tags) {
			getOriginalListener().onFieldValueChanged(getField(), tags.toArray(new String[0]));
		}

	}
}
