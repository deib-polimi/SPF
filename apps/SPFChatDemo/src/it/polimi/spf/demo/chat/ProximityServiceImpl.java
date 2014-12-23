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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import it.polimi.spf.demo.chat.model.Conversation;
import it.polimi.spf.demo.chat.model.Message;
import it.polimi.spf.lib.services.ActivityConsumer;
import it.polimi.spf.lib.services.SPFServiceEndpoint;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Implementation of ProximityService to send and receive messages and pokes
 *
 * @author darioarchetti
 */
public class ProximityServiceImpl extends SPFServiceEndpoint implements ProximityService {

    public EventListener getListener() {
        synchronized (sLock) {
            return sListener != null ? sListener : sDefaultListener;
        }
    }

    public interface EventListener {
        void onPokeReceived(SPFActivity poke);

        void onMessageReceived(SPFActivity message);
    }

    private static EventListener sListener, sDefaultListener;
    private final static Object sLock = new Object();

    public static void setEventListener(EventListener listener) {
        if (listener == null) {
            throw new NullPointerException("Listener must not be null");
        }

        synchronized (sLock) {
            sListener = listener;
        }
    }

    public static void removeEventListener(EventListener mProximityEventListener) {
        synchronized (sLock) {
            if (sListener == mProximityEventListener) {
                sListener = null;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sDefaultListener = new DefaultEventListener(this);
    }

    @ActivityConsumer(verb = POKE_VERB)
    public void onPokeReceived(SPFActivity poke) {
        getListener().onPokeReceived(poke);
    }

    @ActivityConsumer(verb = MESSAGE_VERB)
    public void onMessageReceived(SPFActivity message) {
        ChatStorage storage = ChatDemoApp.get().getChatStorage();
        Conversation c = storage.findConversationWith(message.get(SPFActivity.SENDER_IDENTIFIER));
        if (c == null) {
            c = new Conversation();
            c.setContactIdentifier(message.get(SPFActivity.SENDER_IDENTIFIER));
            c.setContactDisplayName(message.get(SPFActivity.SENDER_DISPLAY_NAME));
            storage.saveConversation(c);
        }

        Message m = new Message();
        m.setSenderId(c.getContactIdentifier());
        m.setText(message.get(ProximityService.MESSAGE_TEXT));
        m.setRead(false);
        storage.saveMessage(m, c);

        getListener().onMessageReceived(message);
    }

    private class DefaultEventListener implements EventListener {

        private final Context mContext;

        private DefaultEventListener(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void onPokeReceived(SPFActivity poke) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(poke.get(SPFActivity.SENDER_DISPLAY_NAME) + " poked you!")
                    .setAutoCancel(true);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);

            Intent showActivity = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, showActivity, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
            int mNotificationId = 0xabc1;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(mNotificationId, mBuilder.build());
        }

        @Override
        public void onMessageReceived(SPFActivity message) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("New message")
                    .setContentText(message.get(SPFActivity.SENDER_DISPLAY_NAME) + " : " + message.get(ProximityService.MESSAGE_TEXT))
                    .setAutoCancel(true);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            long convId = ChatDemoApp.get().getChatStorage().findConversationWith(message.get(SPFActivity.SENDER_IDENTIFIER)).getId();
            Intent showActivity = new Intent(mContext, ConversationActivity.class);
            showActivity.putExtra(ConversationActivity.EXTRA_CONVERSATION_ID, convId);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, showActivity, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            int mNotificationId = 0xabc2;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(mNotificationId, mBuilder.build());
        }
    }
}
