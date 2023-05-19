package com.party.partypal.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.party.partypal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private EditText mSendEditText;

    private Button mSendButton, mDeleteButton;

    private String currentUserID, matchId, chatId;

    DatabaseReference mDatabaseUser, mDatabaseChat, yepsId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        matchId = getIntent().getExtras().getString("matchId");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches").child(matchId).child("chatId");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");
        yepsId = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("yeps").child(matchId);

        /* mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot delSnapshot: snapshot.getChildren()){
                    delSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        mDatabaseChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot delSnapshot: snapshot.getChildren()){
                    delSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         */

        getChatId();


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setHasFixedSize(true);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(),ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);


        mSendEditText = findViewById(R.id.message);
        mSendButton = findViewById(R.id.send);
        getChatMessages();
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

            }
        });

        mDeleteButton = findViewById(R.id.delete);
        mDeleteButton.setOnClickListener(v -> deleteMatch());
    }

    private void sendMessage() {
        String sendMessageText = mSendEditText.getText().toString();

        if(!sendMessageText.isEmpty()){
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser",currentUserID);
            newMessage.put("text",sendMessageText);

            newMessageDb.setValue(newMessage);
        }
        mSendEditText.setText(null);
        mChatAdapter.notifyDataSetChanged();
    }

    private void getChatId(){
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);
                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    String message = null;
                    String createdByUser = null;

                    if(dataSnapshot.child("text").getValue()!=null){
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("createdByUser").getValue()!=null){
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
                    }

                    if(message!=null && createdByUser!=null){
                        Boolean currentUserBoolean = false;
                        if(createdByUser.equals(currentUserID)){
                            currentUserBoolean = true;
                        }
                        ChatObject newMessage = new ChatObject(message, currentUserBoolean);
                        resultsChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();
                    }
                }
                displayChatMessages();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void deleteMatch() {
        DatabaseReference deletingUserRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserID)
                .child("connections")
                .child("matches")
                .child(matchId);

        DatabaseReference deletingYeps = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserID)
                .child("connections")
                .child("yeps")
                .child(matchId);



        deletingUserRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Powodzenie usuniecia matcha");
                    } else {
                        System.out.println("Niepowodzenie usuniecia matcha");
                    }
                });

        deletingYeps.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Powodzenie usuniecia matcha");
            } else {
                System.out.println("Niepowodzenie usuniecia matcha");
            }
        });
    }


    private ArrayList<ChatObject> resultsChat = new ArrayList<ChatObject>();

    private List<ChatObject> getDataSetChat() {
        return resultsChat;
    }
    private void displayChatMessages() {
        for (ChatObject chatObject : resultsChat) {
            Log.d("ChatActivity", "Wiadomość: " + chatObject.getMessage() + ", Użytkownik: " + chatObject.getCurrentUser());
        }
        Log.d("ResultChatList"," Liczba: " + mChatAdapter.getItemCount());
    }

}