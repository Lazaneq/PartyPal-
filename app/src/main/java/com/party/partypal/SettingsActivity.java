package com.party.partypal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField,mPhoneField;

    private Button mBack,mConfirm;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    private String userId, name, phone, profileImageUrl, userSex;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mNameField=(EditText) findViewById(R.id.name);
        mPhoneField=(EditText) findViewById(R.id.phone);

        mProfileImage=(ImageView) findViewById(R.id.profileImage);

        mBack=(Button) findViewById(R.id.back);
        mConfirm=(Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);


        getUserInfo();

        mProfileImage.setOnClickListener(view -> {
            Intent intent=new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);


        });


        mConfirm.setOnClickListener(view -> saveUserInformation());
    mBack.setOnClickListener(view -> {
        finish();
        return;
    });

    }

    private void getUserInfo()
    {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("sex")!=null){
                        userSex = map.get("sex").toString();
                    }
                if(map.get("profileImageUrl")!=null)
                {
                    profileImageUrl = map.get("profileImageUrl").toString();
                    switch (profileImageUrl) {
                        case "default":
                            Glide.with(getApplication()).load(R.mipmap.icon).into(mProfileImage);
                            break;
                        default:
                            Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                            break;
                    }

                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {

        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        mUserDatabase.updateChildren(userInfo);
        if(resultUri != null)
        {
            StorageReference filepath= FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data= baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(e -> finish());

            filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Map userInfo1 = new HashMap();
                userInfo1.put("profileImageUrl", downloadUrl.toString());
                mUserDatabase.updateChildren(userInfo1);
                finish();
                return;

           });


        }
        else
        {
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}