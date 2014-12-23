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

import it.polimi.spf.demo.chat.model.Conversation;
import it.polimi.spf.demo.chat.model.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Database helper to manage the messages and conversations stored in the
 * database.
 * 
 */
public interface ChatStorage {

	public List<Conversation> getAllConversations();

	public List<Message> getAllMessages(Conversation conversation);

	public long saveMessage(Message message, Conversation conversation);

	public Conversation findConversationById(long conversationId);

	public Conversation findConversationWith(String contactIdentifier);

	public long saveConversation(Conversation conversation);

	public void markAsRead(Conversation conversation);

	public void deleteConversation(Conversation conversation);

	public interface ConversationContract extends BaseColumns {
		public static final String TABLE_NAME = "Conversations";
		public static final String COLUMN_USER_IDENTIFIER = "user_id";
		public static final String COLUMN_USER_DISPLAY_NAME = "display_name";
		public static final String COLUMN_USER_PICTURE = "userPicture";
		public static final String COLUMN_NUMBER_OF_UNREAD_MSG = "num_of_unread_msg";
		public static final String COLUMN_UPDATED_AT = "updated_at";
		public static final String COLUMN_FIRST_MESSAGE = "first_message";
	}

	public interface MessageContract extends BaseColumns {
		public static final String TABLE_NAME = "Messages";
		public static final String COLUMN_CONVERSATION = "conversation_id";
		public static final String COLUMN_USER_IDENTIFIER = "user_id";
		public static final String COLUMN_MESSAGE = "message";
		public static final String COLUMN_READ = "read";
		public static final String COLUMN_CREATED_AT = "created_at";
		public static final String COLUMN_DISPLAY_NAME = "display_name";
	}

	public static class ChatStorageImpl extends SQLiteOpenHelper implements ChatStorage {
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "ChatDatabase.db";
		private static final String TEXT_TYPE = " TEXT";
		private static final String INTEGER_TYPE = " INTEGER";

		private static final String COMMA_SEP = ",";

		//@formatter:off
		private static final String CREATE_MESSAGE_SQL = "CREATE TABLE " + MessageContract.TABLE_NAME + " ("
				+ MessageContract._ID                    + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP
				+ MessageContract.COLUMN_CONVERSATION    + INTEGER_TYPE + COMMA_SEP
				+ MessageContract.COLUMN_MESSAGE         + TEXT_TYPE    + COMMA_SEP
				+ MessageContract.COLUMN_USER_IDENTIFIER + TEXT_TYPE    + COMMA_SEP
				+ MessageContract.COLUMN_DISPLAY_NAME    + TEXT_TYPE    + COMMA_SEP
				+ MessageContract.COLUMN_CREATED_AT      + INTEGER_TYPE + COMMA_SEP
				+ MessageContract.COLUMN_READ            + INTEGER_TYPE + ")";
		
		private static final String CREATE_CONVERSATION_SQL = "CREATE TABLE " + ConversationContract.TABLE_NAME + " ("
				+ ConversationContract._ID                         + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP
				+ ConversationContract.COLUMN_USER_IDENTIFIER      + INTEGER_TYPE + COMMA_SEP
				+ ConversationContract.COLUMN_USER_DISPLAY_NAME    + TEXT_TYPE    + COMMA_SEP
				+ ConversationContract.COLUMN_USER_PICTURE         + TEXT_TYPE    + COMMA_SEP
				+ ConversationContract.COLUMN_UPDATED_AT           + INTEGER_TYPE + COMMA_SEP
				+ ConversationContract.COLUMN_FIRST_MESSAGE        + TEXT_TYPE    + COMMA_SEP
				+ ConversationContract.COLUMN_NUMBER_OF_UNREAD_MSG + INTEGER_TYPE + ")";
		//@formatter:on

		/**
		 * Constructor for {@link ChatStorage}
		 * 
		 * @param ctx
		 *            - the Context in which the database should be created
		 */
		public ChatStorageImpl(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_CONVERSATION_SQL);
			db.execSQL(CREATE_MESSAGE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// No code here for the first version
		}

		@Override
		public List<Conversation> getAllConversations() {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(ConversationContract.TABLE_NAME, null,// columns
					null,// selection
					null,// selection args
					null,// groupby
					null,// having
					ConversationContract.COLUMN_UPDATED_AT + " DESC"// orderby
			);

			List<Conversation> result = new ArrayList<Conversation>();
			while (c.moveToNext()) {
				result.add(conversationFromCursor(c));
			}

			return result;
		}

		@Override
		public List<Message> getAllMessages(Conversation conversation) {
			String where = MessageContract.COLUMN_CONVERSATION + " = ?";
			String[] args = { Long.toString(conversation.getId()) };
			Cursor c = getReadableDatabase().query(MessageContract.TABLE_NAME, null, where, args, null, null, null);
			List<Message> result = new ArrayList<Message>();
			while (c.moveToNext()) {
				result.add(messageFromCursor(c));
			}

			return result;
		}

		@Override
		public long saveMessage(Message message, Conversation conversation) {
			SQLiteDatabase db = getWritableDatabase();
			if (!existsConversation(conversation.getId())) {
				return -1;
			}

            message.setCreationTime(new Date());

			ContentValues values = new ContentValues();
			values.put(MessageContract.COLUMN_CONVERSATION, conversation.getId());
			values.put(MessageContract.COLUMN_USER_IDENTIFIER, message.getSenderId());
			values.put(MessageContract.COLUMN_MESSAGE, message.getText());
			values.put(MessageContract.COLUMN_READ, message.isRead() ? 1 : 0);
			values.put(MessageContract.COLUMN_CREATED_AT, message.getCreationTime().getTime());

			long id = db.insert(MessageContract.TABLE_NAME, "NULL", values);
			if (id == -1) {
				return id;
			}

			updateConversation(conversation, message);
			return id;
		}

		@Override
		public Conversation findConversationById(long conversationId) {
			Cursor c = findConversation(conversationId);
			if (c.moveToFirst()) {
				return conversationFromCursor(c);
			} else {
				return null;
			}
		}

		@Override
		public Conversation findConversationWith(String contactIdentifier) {
			SQLiteDatabase db = getReadableDatabase();
			String selection = ConversationContract.COLUMN_USER_IDENTIFIER + " = ? ";
			String[] selectionArgs = new String[] { contactIdentifier };
			Cursor c = db.query(ConversationContract.TABLE_NAME, null, selection, selectionArgs, null/* groupBy */, null/* having */, null/* orderBy */, "1"/* limit */);
			// move to first return false if the result set is empty
			if (c.moveToFirst()) {
				return conversationFromCursor(c);
			} else {
				return null;
			}
		}

		@Override
		public long saveConversation(Conversation conversation) {
			SQLiteDatabase db = getWritableDatabase();
            conversation.setFirstMessage("");
            conversation.setUnreadMsgCount(0);
            conversation.setUpdateTime(new Date());

			ContentValues values = new ContentValues(5);
			values.put(ConversationContract.COLUMN_USER_IDENTIFIER, conversation.getContactIdentifier());
			values.put(ConversationContract.COLUMN_USER_DISPLAY_NAME, conversation.getContactDisplayName());
			values.put(ConversationContract.COLUMN_NUMBER_OF_UNREAD_MSG, conversation.getUnreadMsgCount());
			values.put(ConversationContract.COLUMN_FIRST_MESSAGE, conversation.getFirstMessage());
			values.put(ConversationContract.COLUMN_UPDATED_AT, conversation.getUpdateTime().getTime());
			long id = db.insert(ConversationContract.TABLE_NAME, null, values);
            conversation.setId(id);
            return id;
		}

		@Override
		public void markAsRead(Conversation conversation) {
			SQLiteDatabase db = getWritableDatabase();
			String[] selectionArgs = new String[] { Long.toString(conversation.getId()) };

			String updateConversationQuery = "UPDATE " + ConversationContract.TABLE_NAME + " SET " + ConversationContract.COLUMN_NUMBER_OF_UNREAD_MSG + " =  0  " + " WHERE " + ConversationContract._ID + " = ?";
			db.execSQL(updateConversationQuery, selectionArgs);

			String updateMessagesQuery = "UPDATE " + MessageContract.TABLE_NAME + " SET " + MessageContract.COLUMN_READ + " = 1 WHERE " + MessageContract.COLUMN_CONVERSATION + " = ?";
			db.execSQL(updateMessagesQuery, selectionArgs);
		}

		@Override
		public void deleteConversation(Conversation conversation) {
			SQLiteDatabase db = getWritableDatabase();
			final String conversationWhereClause = ConversationContract._ID + " = ?";
			final String[] conversationIdWhereArgs = new String[] { Long.toString(conversation.getId()) };
			if (db.delete(ConversationContract.TABLE_NAME, conversationWhereClause, conversationIdWhereArgs) > 0) {
				final String messagesWhereClause = MessageContract.COLUMN_CONVERSATION + "= ?";
				db.delete(MessageContract.TABLE_NAME, messagesWhereClause, conversationIdWhereArgs);
			}
		}

		private Conversation conversationFromCursor(Cursor c) {
			Conversation conv = new Conversation();
			conv.setId(c.getLong(c.getColumnIndexOrThrow(ConversationContract._ID)));
			conv.setContactIdentifier(c.getString(c.getColumnIndexOrThrow(ConversationContract.COLUMN_USER_IDENTIFIER)));
			conv.setContactDisplayName(c.getString(c.getColumnIndexOrThrow(ConversationContract.COLUMN_USER_DISPLAY_NAME)));
			conv.setFirstMessage(c.getString(c.getColumnIndexOrThrow(ConversationContract.COLUMN_FIRST_MESSAGE)));
			conv.setUnreadMsgCount(c.getInt(c.getColumnIndexOrThrow(ConversationContract.COLUMN_NUMBER_OF_UNREAD_MSG)));
			conv.setUpdateTime(new Date(c.getLong(c.getColumnIndexOrThrow(ConversationContract.COLUMN_UPDATED_AT))));
			return conv;
		}

		private Message messageFromCursor(Cursor c) {
			Message m = new Message();
			m.setText(c.getString(c.getColumnIndexOrThrow(MessageContract.COLUMN_MESSAGE)));
			m.setRead(c.getInt(c.getColumnIndexOrThrow(MessageContract.COLUMN_READ)) == 1);
			m.setCreationTime(new Date(c.getLong(c.getColumnIndexOrThrow(MessageContract.COLUMN_CREATED_AT))));
			m.setSenderId(c.getString(c.getColumnIndexOrThrow(MessageContract.COLUMN_USER_IDENTIFIER)));
			return m;
		}

		private boolean existsConversation(long conversationId) {
			return findConversation(conversationId).moveToFirst();
		}

		private Cursor findConversation(long id) {
			String selection = ConversationContract._ID + " = ? ";
			String[] selectionArgs = new String[] { Long.toString(id) };
			return getReadableDatabase().query(ConversationContract.TABLE_NAME, null, selection, selectionArgs, null/* groupBy */, null/* having */, null/* orderBy */, "1"/* limit */);
		}

		private void updateConversation(Conversation conversation, Message message) {
            StringBuilder query = new StringBuilder();
            query.append("UPDATE ");
            query.append(ConversationContract.TABLE_NAME);
            query.append(" SET ");
            if(!message.isRead()){
                query.append(ConversationContract.COLUMN_NUMBER_OF_UNREAD_MSG);
                query.append(" = ");
                query.append(ConversationContract.COLUMN_NUMBER_OF_UNREAD_MSG);
                query.append(" + 1 ,");
            }

            query.append(ConversationContract.COLUMN_FIRST_MESSAGE);
            query.append(" = ? , ");
            query.append(ConversationContract.COLUMN_UPDATED_AT);
            query.append(" = ?  WHERE ");
            query.append(ConversationContract._ID);
            query.append(" = ?");

			String[] whereArgs = new String[] { message.getText(), Long.toString(System.currentTimeMillis()), Long.toString(conversation.getId()) };
			getReadableDatabase().execSQL(query.toString(), whereArgs);
			// problem if it fails out of synch 4ever: maybe is better an TODO
			// update with nested query...
		}
	}
}