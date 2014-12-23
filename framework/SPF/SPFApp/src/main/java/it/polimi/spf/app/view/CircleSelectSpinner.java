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

package it.polimi.spf.app.view;

import it.polimi.spf.app.fragments.profile.Helper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * A Spinner view that does not dismiss the dialog displayed when the control is
 * "dropped down" and the user presses it. This allows for the selection of more
 * than one option.
 */
public class CircleSelectSpinner extends Spinner implements OnMultiChoiceClickListener {
	private String[] mItems = null;
	private boolean[] mSelection = null;
	private OnSelectionChangedListener mListener;
	private Helper mHelper;
	private ArrayAdapter<String> mProxyAdapter;

	public static interface OnSelectionChangedListener {
		void onItemAdded(String item);

		void onItemRemoved(String item);
	}

	/**
	 * Constructor for use when instantiating directly.
	 * 
	 * @param context
	 */
	public CircleSelectSpinner(Context context) {
		super(context);

		mHelper = new Helper(context);
		mProxyAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
		super.setAdapter(mProxyAdapter);
	}

	/**
	 * Constructor used by the layout inflater.
	 * 
	 * @param context
	 * @param attrs
	 */
	public CircleSelectSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);

		mHelper = new Helper(context);
		mProxyAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
		super.setAdapter(mProxyAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (mSelection != null && which < mSelection.length) {
			mSelection[which] = isChecked;

			List<String> selected = getSelectedStrings();

			mProxyAdapter.clear();
			mProxyAdapter.add(mHelper.sumUpCircles(selected));
			setSelection(0);

			if (mListener != null) {
				String item = mItems[which];
				if (isChecked) {
					mListener.onItemAdded(item);
				} else {
					mListener.onItemRemoved(item);
				}
			}

		} else {
			throw new IllegalArgumentException("Argument 'which' is out of bounds.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean performClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMultiChoiceItems(mItems, mSelection, this);
		builder.show();
		return true;
	}

	/**
	 * MultiSelectSpinner does not support setting an adapter. This will throw
	 * an exception.
	 * 
	 * @param adapter
	 */
	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		throw new RuntimeException("setAdapter is not supported by MultiSelectSpinner.");
	}

	/**
	 * Sets the options for this spinner.
	 * 
	 * @param items
	 */
	public void setItems(String[] items) {
		mItems = items;
		mSelection = new boolean[mItems.length];

		Arrays.fill(mSelection, false);
		refreshDisplayValue();
	}

	/**
	 * Sets the options for this spinner.
	 * 
	 * @param items
	 */
	public void setItems(List<String> items) {
		mItems = items.toArray(new String[items.size()]);
		mSelection = new boolean[mItems.length];

		Arrays.fill(mSelection, false);
		refreshDisplayValue();
	}

	/**
	 * Sets the selected options based on an array of string.
	 * 
	 * @param selection
	 */
	public void setSelection(String[] selection) {
		for (String sel : selection) {
			for (int j = 0; j < mItems.length; ++j) {
				if (mItems[j].equals(sel)) {
					mSelection[j] = true;
				}
			}
		}

		refreshDisplayValue();
	}

	/**
	 * Sets the selected options based on a list of string.
	 * 
	 * @param selection
	 */
	public void setSelection(List<String> selection) {
		for (String sel : selection) {
			for (int j = 0; j < mItems.length; ++j) {
				if (mItems[j].equals(sel)) {
					mSelection[j] = true;
				}
			}
		}

		refreshDisplayValue();
	}

	/**
	 * Sets the selected options based on an array of positions.
	 * 
	 * @param selectedIndicies
	 */
	public void setSelection(int[] selectedIndicies) {
		for (int index : selectedIndicies) {
			if (index >= 0 && index < mSelection.length) {
				mSelection[index] = true;
			} else {
				throw new IllegalArgumentException("Index " + index + " is out of bounds.");
			}
		}

		refreshDisplayValue();
	}

	/**
	 * Sets a listener to be notified when the selection changes
	 * 
	 * @param listener
	 */
	public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
		this.mListener = listener;
	}

	/**
	 * Returns a list of strings, one for each selected item.
	 * 
	 * @return
	 */
	public List<String> getSelectedStrings() {
		List<String> selection = new LinkedList<String>();
		for (int i = 0; i < mItems.length; ++i) {
			if (mSelection[i]) {
				selection.add(mItems[i]);
			}
		}
		return selection;
	}

	/**
	 * Returns a list of positions, one for each selected item.
	 * 
	 * @return
	 */
	public List<Integer> getSelectedIndicies() {
		List<Integer> selection = new LinkedList<Integer>();
		for (int i = 0; i < mItems.length; ++i) {
			if (mSelection[i]) {
				selection.add(i);
			}
		}
		return selection;
	}

	private void refreshDisplayValue() {
		mProxyAdapter.clear();
		mProxyAdapter.add(mHelper.sumUpCircles(getSelectedStrings()));
		setSelection(0);
	}

}