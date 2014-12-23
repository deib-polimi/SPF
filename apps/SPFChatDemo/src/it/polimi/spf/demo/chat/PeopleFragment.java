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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import it.polimi.spf.demo.chat.model.Conversation;
import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.lib.search.SPFSearch;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFSearchDescriptor;

/**
 * @author darioarchetti
 */

public class PeopleFragment extends Fragment {

    private static final String TAG = "PeopleFragment";
    private static final int SEARCH_TAG = 0xabc;
    private static final long SIGNAL_INTERVAL = 5 * 1000;
    private static final int SIGNAL_NUMBER = 3;
    private static final int QUERY_SET_CODE = 0;

    private PersonAdapter mAdapter;
    private TextView mMessageView;
    private SPF mSPF;
    private SPFQuery mQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_people, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new PersonAdapter();
        mMessageView = (TextView) getView().findViewById(R.id.people_list_empty);
        ListView lv = (ListView) getView().findViewById(R.id.people_list);
        lv.setEmptyView(mMessageView);
        lv.setAdapter(mAdapter);

        //Set default query
        mQuery = new SPFQuery.Builder()
                //.setAppIdentifier("it.polimi.spf.example.nex2")
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        SPF.connect(getActivity(), mSPFConnectioListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSPF != null) {
            mSPF.disconnect();
        }
    }

    // Actionbar Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_people, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.people_menu_refresh:
                onRefresh();
                return true;
            case R.id.people_menu_query:
                Intent i = new Intent(getActivity(), QuerySettingActivity.class);
                startActivityForResult(i, QUERY_SET_CODE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == QUERY_SET_CODE && resultCode == Activity.RESULT_OK){
            SPFQuery q = data.getParcelableExtra(QuerySettingActivity.EXTRA_QUERY);
            if(q == null){
                Log.e(TAG, "Query setting returned null result");
            } else {
                mQuery = q;
                Log.d(TAG, "Set new query");
            }
        }
    }

    private void onRefresh() {
        if (mSPF == null) {
            toast(R.string.people_connection_error);
            return;
        }

        SPFSearch search = mSPF.getComponent(SPF.SEARCH);
        SPFSearchDescriptor descriptor = new SPFSearchDescriptor(SIGNAL_INTERVAL, SIGNAL_NUMBER, mQuery);
        search.startSearch(SEARCH_TAG, descriptor, mSearchCallback);
    }

    private void toast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    private SPFSearch.SearchCallback mSearchCallback = new SPFSearch.SearchCallback() {
        @Override
        public void onPersonFound(SPFPerson spfPerson) {
            mAdapter.add(spfPerson);
        }

        @Override
        public void onPersonLost(SPFPerson spfPerson) {
            mAdapter.remove(spfPerson);
        }

        @Override
        public void onSearchStop() {
            // Set the empty message: it will be shown only if the adapter is actually empty
            mMessageView.setText(R.string.people_no_result);
        }

        @Override
        public void onSearchError() {
            mMessageView.setText(R.string.people_search_error);
            mAdapter.clear();
        }

        @Override
        public void onSearchStart() {
            mAdapter.clear();
            mMessageView.setText(R.string.people_search_performing);
        }
    };

    private final SPF.ConnectionListener mSPFConnectioListener = new SPF.ConnectionListener() {
        @Override
        public void onConnected(SPF spf) {
            mSPF = spf;
            Log.v(TAG, "Connected to spf");
        }

        @Override
        public void onError(SPFError spfError) {
            mSPF = null;
            Log.e(TAG, "Error in SPF: " + spfError);
        }

        @Override
        public void onDisconnected() {
            mSPF = null;
            Log.d(TAG, "Disconnected from SPF");
        }
    };

    private class PersonAdapter extends ArrayAdapter<SPFPerson> implements View.OnClickListener{

        private PersonAdapter() {
            super(getActivity(), android.R.layout.simple_list_item_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView :
                    LayoutInflater.from(getActivity()).inflate(R.layout.people_list_entry, parent, false);
            ViewHolder holder = ViewHolder.from(view);

            SPFPerson person = getItem(position);

            String name = person.getBaseInfo().getDisplayName();
            holder.personName.setText(name == null ? person.getIdentifier() : name);

            holder.profileBtn.setOnClickListener(this);
            holder.profileBtn.setTag(person);

            holder.pokeBtn.setOnClickListener(this);
            holder.pokeBtn.setTag(person);

            holder.messageBtn.setOnClickListener(this);
            holder.messageBtn.setTag(person);
            return view;
        }

        @Override
        public void onClick(View v) {
            SPFPerson person = (SPFPerson) v.getTag();
            switch(v.getId()){
                case R.id.people_action_profile:
                    showUserProfile(person);
                    break;
                case R.id.people_action_poke:
                    sendPoke(person);
                    break;
                case R.id.people_action_message:
                    showConversationWith(person);
                    break;
            }
        }
    }

    private void showUserProfile(SPFPerson person){
        Intent i = new Intent(getActivity(), UserProfileActivity.class);
        i.putExtra(UserProfileActivity.EXTRA_PERSON_IDENTIFIER, person.getIdentifier());
        startActivity(i);
    }

    private void showConversationWith(SPFPerson person){
        ChatStorage storage = ChatDemoApp.get().getChatStorage();
        long id;
        Conversation c = storage.findConversationWith(person.getIdentifier());
        if(c != null){
            id = c.getId();
        } else {
            c = new Conversation();
            c.setContactDisplayName(person.getBaseInfo().getDisplayName());
            c.setContactIdentifier(person.getIdentifier());
            id = storage.saveConversation(c);
        }

        Intent i = new Intent(getActivity(), ConversationActivity.class);
        i.putExtra(ConversationActivity.EXTRA_CONVERSATION_ID, id);
        startActivity(i);
    }

    private void sendPoke(SPFPerson person){
        if(mSPF == null){
            toast(R.string.people_connection_error);
            return;
        }

        SPFActivity poke = new SPFActivity(ProximityService.POKE_VERB);
        if(!person.sendActivity(mSPF, poke)){
            Toast.makeText(getActivity(), "Error sending poke", Toast.LENGTH_LONG).show();
        }
    }

    private static class ViewHolder {
        public TextView personName;
        public ImageButton profileBtn, pokeBtn, messageBtn;

        public static ViewHolder from (View view){
            Object t = view.getTag();
            if(t != null && (t instanceof ViewHolder)){
                return (ViewHolder) t;
            }

            ViewHolder h = new ViewHolder();
            view.setTag(h);

            h.personName = (TextView) view.findViewById(R.id.people_entry_name);
            h.profileBtn = (ImageButton) view.findViewById(R.id.people_action_profile);
            h.pokeBtn = (ImageButton) view.findViewById(R.id.people_action_poke);
            h.messageBtn = (ImageButton) view.findViewById(R.id.people_action_message);

            return h;
        }
    }
}