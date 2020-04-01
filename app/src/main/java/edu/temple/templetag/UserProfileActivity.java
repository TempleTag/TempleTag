package edu.temple.templetag;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import edu.temple.templetag.fragments.TagRecyclerViewFragment;

public class UserProfileActivity extends AppCompatActivity {

    private String txt_username, txt_email;
    private TextView change_username_textview, username_textview, email_textview, tagcount_textview;
    private String dialogText = "nothing";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        displayInfo();

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                finish();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //Change Username
        change_username_textview = findViewById(R.id.change_username);
        change_username_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                builder.setTitle("New Username");

                // Set up the input
                final EditText input = new EditText(UserProfileActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogText = input.getText().toString();
                        changeUsername();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        TagRecyclerViewFragment fragment = (TagRecyclerViewFragment) getSupportFragmentManager().findFragmentByTag("taglist");
        if (fragment != null){
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .add(R.id.tagListContainer, TagRecyclerViewFragment.newInstance("Hello", "From UserSetting"), "taglist")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tagListContainer, TagRecyclerViewFragment.newInstance("Hello", "From UserSetting"), "taglist")
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeUsername(){
        //Go through firebase and change username
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        firestore.collection("Users")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean canChangeUsername = true;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if ((!document.getData().get("id").equals(firebaseUser.getUid())) && document.getData().get("username").equals(dialogText)) {
                            canChangeUsername = false;
                        }
                    }
                    if (canChangeUsername) {
                        DocumentReference reference = firestore.collection("Users").document(firebaseUser.getUid());
                        reference.update("username", dialogText).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                username_textview.setText(dialogText);
                            }
                        });
                    } else {
                        Toast.makeText(UserProfileActivity.this, "You cannot use this username", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void displayInfo(){
        txt_username = getIntent().getExtras().getString("username");
        txt_email = getIntent().getExtras().getString("email");
        username_textview = findViewById(R.id.username);
        username_textview.setText("Hi, " + txt_username);
        email_textview = findViewById(R.id.emailTextView);
        email_textview.setText(txt_email);
        tagcount_textview = findViewById(R.id.tagCount);

        tagcount_textview.setText(String.valueOf(getTagCount()) + " active tags");
    }

    private int getTagCount(){
        return 0;
    }
}
