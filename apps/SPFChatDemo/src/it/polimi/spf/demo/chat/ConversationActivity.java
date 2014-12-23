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
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import it.polimi.spf.demo.chat.model.Conversation;
import it.polimi.spf.demo.chat.model.Message;
import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.lib.search.SPFSearch;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFError;

/**
 * @author darioarchetti
 */
public class ConversationActivity extends Activity implements View.OnClickListener, LoaderManager.LoaderCallbacks<List<Message>>{

    public static final String EXTRA_CONVERSATION_ID = "conversationId";
    private static final String TAG = "ConversationActivity";
    private static final int MESSAGE_LOADER_ID = 0;

    private ConversationAdapter mAdapter;
    private EditText mTextInput;
    private Conversation mConversation;
    private SPF mSpf;
    private ImageButton mSendButton;
    private ChatStorage mStorage = ChatDemoApp.get().getChatStorage();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Bundle source = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if(!source.containsKey(EXTRA_CONVERSATION_ID)){
            throw new IllegalStateException("No conversation id found");
        }

        long id = source.getLong(EXTRA_CONVERSATION_ID);
        mConversation = mStorage.findConversationById(id);

        ListView lv = (ListView) findViewById(R.id.message_list_view);
        lv.setEmptyView(findViewById(R.id.message_list_emptymessage));
        mAdapter = new ConversationAdapter(this, mConversation.getContactIdentifier());
        lv.setAdapter(mAdapter);

        mSendButton = (ImageButton) findViewById(R.id.message_send_button);
        mSendButton.setOnClickListener(this);
        mTextInput = (EditText) findViewById(R.id.message_input);

        setTitle(mConversation.getContactDisplayName());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setInputEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SPF.connect(this, mConnectionListener);
        ProximityServiceImpl.setEventListener(mProximityEventListener);
        getLoaderManager().initLoader(MESSAGE_LOADER_ID, null, this).forceLoad();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSpf != null){
            mSpf.disconnect();
        }
        ProximityServiceImpl.removeEventListener(mProximityEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        String text = mTextInput.getText().toString();
        if(text.length() == 0){
            toast(R.string.message_empty);
            return;
        }

        if(mSpf == null){
            Log.e(TAG, "Not connected to SPF when send was tapped");
            return;
        }

        SPFActivity message = new SPFActivity(ProximityService.MESSAGE_VERB);
        message.put(ProximityService.MESSAGE_TEXT, text);

        SPFPerson p = findPerson(mConversation.getContactIdentifier());
        if(p == null){
            toast(R.string.message_person_lost);
            setInputEnabled(false);
            return;
        }

        if(!p.sendActivity(mSpf, message)) {
            toast(R.string.message_send_error);
            return;
        }

        Message m = new Message();
        m.setSenderId(message.get(SPFActivity.SENDER_IDENTIFIER));
        m.setSenderDisplayName(message.get(SPFActivity.SENDER_DISPLAY_NAME));
        m.setText(text);
        m.setRead(true);
        mStorage.saveMessage(m, mConversation);
        mAdapter.add(m);

        mTextInput.setText("");
    }

    private void setInputEnabled(boolean enabled) {
        mTextInput.setEnabled(enabled);
        mSendButton.setEnabled(enabled);
    }

    private SPFPerson findPerson(String contactIdentifier) {
        SPFSearch search = mSpf.getComponent(SPF.SEARCH);
        return search.lookup(contactIdentifier);
    }

    private final SPF.ConnectionListener mConnectionListener = new SPF.ConnectionListener() {
        @Override
        public void onConnected(SPF instance) {
            mSpf = instance;
            setInputEnabled(true);
        }

        @Override
        public void onError(SPFError errorMsg) {
            Log.e(TAG, "Error connecting to SPF: " + errorMsg);
            mSpf = null;
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "Disconnected from SPF");
            mSpf = null;
        }
    };

    private ProximityServiceImpl.EventListener mProximityEventListener = new ProximityServiceImpl.EventListener() {
        @Override
        public void onPokeReceived(SPFActivity poke) {
            Toast.makeText(ConversationActivity.this, poke.get(SPFActivity.SENDER_DISPLAY_NAME) + " poked you!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMessageReceived(final SPFActivity message) {
            new Handler(Looper.getMainLooper()).post(new Runnable(){

                @Override
                public void run() {
                    if(mConversation.getContactIdentifier().equals(message.get(SPFActivity.SENDER_IDENTIFIER))){
                        getLoaderManager().initLoader(MESSAGE_LOADER_ID, null, ConversationActivity.this).forceLoad();
                    }
                }
            });
        }
    };

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<List<Message>> onCreateLoader(int id, Bundle args) {
        switch (id){
            case MESSAGE_LOADER_ID:
                return new AsyncTaskLoader<List<Message>>(this) {
                    @Override
                    public List<Message> loadInBackground() {
                        mStorage.markAsRead(mConversation);
                        return mStorage.getAllMessages(mConversation);
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Message>> loader, List<Message> data) {
        mAdapter.clear();
        if(data != null){
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Message>> loader) {
        // Do nothing
    }

    public class ConversationAdapter extends ArrayAdapter<Message> {

        private String mOtherPersonId;
        private DateFormat mDateFormat;
        private DateFormat mTimeFormat;
        private Drawable mSelfMessageBg, mOtherMessageBg;
        private int mPadding;

        public ConversationAdapter(Context context, String otherPersonIdentifier) {
            super(context, android.R.layout.simple_list_item_1);

            mOtherPersonId = otherPersonIdentifier;
            mDateFormat = android.text.format.DateFormat.getDateFormat(context);
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);

            Resources r = context.getResources();
            mSelfMessageBg = r.getDrawable(R.drawable.bubble_self);
            mOtherMessageBg = r.getDrawable(R.drawable.bubble_other);
            mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView :
                    LayoutInflater.from(getContext()).inflate(R.layout.message_list_element, parent, false);
            ViewHolder holder = ViewHolder.from(view);

            Message message = getItem(position);
            Date d = message.getCreationTime();
            holder.messageSentTimeView.setText(mTimeFormat.format(d) + " " + mDateFormat.format(d));
            holder.messageTextView.setText(message.getText());

            String senderId = message.getSenderId();
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.background.getLayoutParams();

            if (senderId.equals(mOtherPersonId)) {
                holder.background.setBackground(mOtherMessageBg);
                lp.rightMargin = mPadding;
                lp.leftMargin = 0;
            } else {
                holder.background.setBackground(mSelfMessageBg);
                lp.leftMargin = mPadding;
                lp.rightMargin = 0;
            }

            //holder.background.setLayoutParams(lp);
            return view;
        }
    }

    private static class ViewHolder {

        public static ViewHolder from(View v) {
            Object tag = v.getTag();
            if (tag != null && tag instanceof ViewHolder) {
                return (ViewHolder) tag;
            }

            ViewHolder vh = new ViewHolder();
            v.setTag(vh);
            vh.messageSentTimeView = (TextView) v.findViewById(R.id.message_sent_time);
            vh.messageTextView = (TextView) v.findViewById(R.id.message_text);
            vh.background = v.findViewById(R.id.background);

            return vh;

        }

        public TextView messageSentTimeView;
        public TextView messageTextView;
        public View background;
    }
}
