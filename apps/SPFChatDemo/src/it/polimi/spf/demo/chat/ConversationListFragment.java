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
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import it.polimi.spf.demo.chat.model.Conversation;

/**
 * @author darioarchetti
 */
public class ConversationListFragment extends Fragment
    implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<Conversation>> {

    private static final int CONVERSATION_LOADER = 0;

    private ConversationListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up list view
        ListView lv = (ListView) getView().findViewById(R.id.conversation_list);
        lv.setEmptyView(getView().findViewById(R.id.conversation_list_empty));
        lv.setOnItemClickListener(this);
        mAdapter = new ConversationListAdapter(getActivity());
        lv.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).registerMessageObserver(mMessageObserver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((MainActivity) getActivity()).unregisterMessageObserver(mMessageObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(CONVERSATION_LOADER, null, this).forceLoad();
    }

    // Called when a conversation is tapped
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Conversation c = (Conversation) view.getTag();

        Intent i = new Intent(getActivity(), ConversationActivity.class);
        i.putExtra(ConversationActivity.EXTRA_CONVERSATION_ID, c.getId());
        startActivity(i);
    }

    @Override
    public Loader<List<Conversation>> onCreateLoader(int id, Bundle args) {
        switch(id){
            case CONVERSATION_LOADER:
                return new AsyncTaskLoader<List<Conversation>>(getActivity()) {
                    @Override
                    public List<Conversation> loadInBackground() {
                        return ChatDemoApp.get().getChatStorage().getAllConversations();
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Conversation>> loader, List<Conversation> data) {
        switch(loader.getId()){
            case CONVERSATION_LOADER:
                mAdapter.clear();
                if(data != null){
                    mAdapter.addAll(data);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Conversation>> loader) {
        // Do nothing
    }

    private final Observer mMessageObserver = new Observer() {
        @Override
        public void update(Observable observable, Object data) {
            getLoaderManager().initLoader(CONVERSATION_LOADER, null, ConversationListFragment.this).forceLoad();
        }
    };

    private static class ConversationListAdapter extends ArrayAdapter<Conversation> {

        public ConversationListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView :
                    LayoutInflater.from(getContext()).inflate(R.layout.conversation_list_element, parent, false);
            ViewHolder holder = ViewHolder.from(view);
            Conversation conversation = getItem(position);

            holder.senderDisplayNameView.setText(conversation.getContactDisplayName());
            holder.lastMessageView.setText(conversation.getFirstMessage());
            holder.root.setTag(conversation);

            int unreadCount = conversation.getUnreadMsgCount();
            if (unreadCount == 0) {
                holder.unreadCountView.setVisibility(View.GONE);
            } else {
                holder.unreadCountView.setVisibility(View.VISIBLE);
                holder.unreadCountView.setText(String.valueOf(unreadCount));
            }

            return view;
        }
    }

    private static class ViewHolder {

        public static ViewHolder from(View v) {
            Object o = v.getTag();
            if (o != null && o instanceof ViewHolder) {
                return (ViewHolder) o;
            }

            ViewHolder holder = new ViewHolder();
            v.setTag(holder);
            holder.root = v;
            holder.senderDisplayNameView = (TextView) v.findViewById(R.id.sender_display_name);
            holder.lastMessageView = (TextView) v.findViewById(R.id.chat_last_message);
            holder.unreadCountView = (TextView) v.findViewById(R.id.unread_count);
            return holder;

        }

        public View root;
        public TextView senderDisplayNameView;
        public TextView lastMessageView;
        public TextView unreadCountView;
    }
}