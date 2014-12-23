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
package it.polimi.spf.demo.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.SPFQuery;


public class QuerySettingActivity extends Activity {

    private SPFQuery.Builder mQueryBuilder;
    private ArrayAdapter<String> mAdapter;
    private QueryCreationDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_setting);

        mQueryBuilder = new SPFQuery.Builder();

        mDialog = new QueryCreationDialog();
        mDialog.setListener(mDialogListener);

        ListView lv = (ListView) findViewById(R.id.query_parameter_list);
        lv.setEmptyView(findViewById(R.id.query_param_list_empty));
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_query, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(false);
                return true;
            case R.id.query_menu_add:
                mDialog.show(getFragmentManager(), "QueryDialog");
                return true;
            case R.id.query_menu_save:
                finish(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private QueryCreationDialog.OnParameterAddedListener mDialogListener = new QueryCreationDialog.OnParameterAddedListener() {
        @Override
        public void onTagParameterAdded(String tag) {
            mQueryBuilder.setTag(tag);
            mAdapter.add("[TAG] "+ tag);
        }

        @Override
        public void onPropertyParameterAdded(ProfileField<String> field, String fieldValue) {
            mQueryBuilder.setProfileField(field, fieldValue);
            mAdapter.add("[FIELD] "+ field.getIdentifier() + " = " + fieldValue);
        }
    };

    private void finish(boolean save) {
        if (save) {
            Intent result = new Intent();
            result.putExtra(EXTRA_QUERY, mQueryBuilder.build());
            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    public static final String EXTRA_QUERY = "query";

    public static class QueryCreationDialog extends DialogFragment implements RadioGroup.OnCheckedChangeListener {

        public interface OnParameterAddedListener {
            public void onTagParameterAdded(String tag);

            public void onPropertyParameterAdded(ProfileField<String> field, String value);
        }

        private final static String EMPTY_FIELD = "The field %s cannot be empty";

        private OnParameterAddedListener mListener;
        private EditText mTagView, mFieldValueView;
        private Spinner mFieldKeyView;
        private RadioGroup mQueryTypeRadioGroup;

        public void setListener(OnParameterAddedListener listener){
            mListener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.query_dialog_view, null);

            mTagView = (EditText) view.findViewById(R.id.tag_input);
            mFieldKeyView = (Spinner) view.findViewById(R.id.prop_key_input);
            mFieldValueView = (EditText) view.findViewById(R.id.prop_value_input);
            mQueryTypeRadioGroup = (RadioGroup) view.findViewById(R.id.query_type_radiogroup);
            mQueryTypeRadioGroup.setOnCheckedChangeListener(this);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
            mFieldKeyView.setAdapter(adapter);
            ProfileField<?>[] available = {
                    ProfileField.DISPLAY_NAME,
                    ProfileField.GENDER,
                    ProfileField.LOCATION
            };

            for (ProfileField<?> f : available) {
                adapter.addAll(f.getIdentifier());
            }

            return builder.setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handleSubmission();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getDialog().dismiss();
                        }
                    }).create();
        }

        @SuppressWarnings("unchecked")
		private void handleSubmission() {
            switch (mQueryTypeRadioGroup.getCheckedRadioButtonId()) {
                case R.id.query_tag_radio: {
                    String tag = mTagView.getText().toString();
                    if (isEmptyString(tag)) {
                        notifyError(String.format(EMPTY_FIELD, "tag"));
                        return;
                    }

                    mListener.onTagParameterAdded(tag);
                    break;
                }
                case R.id.query_prop_radio:
                    String fieldKey = (String) mFieldKeyView.getSelectedItem();
                    if (isEmptyString(fieldKey)) {
                        notifyError(String.format(EMPTY_FIELD, "key"));
                        return;
                    }

                    String fieldValue = mFieldValueView.getText().toString();
                    if (isEmptyString(fieldValue)) {
                        notifyError(String.format(EMPTY_FIELD, "value"));
                        return;
                    }

                    mListener.onPropertyParameterAdded((ProfileField<String>) ProfileField.lookup(fieldKey), fieldValue);
                    break;
            }
        }

        private boolean isEmptyString(String tag) {
            return tag == null || tag.length() == 0;
        }

        private void notifyError(String msg) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            mTagView.setEnabled(checkedId == R.id.query_tag_radio);
            mFieldKeyView.setEnabled(checkedId == R.id.query_prop_radio);
            mFieldValueView.setEnabled(checkedId == R.id.query_prop_radio);
        }

    }

}
