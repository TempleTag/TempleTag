package edu.temple.templetag;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import edu.temple.templetag.fragments.TagListFragment;

public class UserSettingActivity extends AppCompatActivity {

    private String txt_username, txt_email;
    private TextView change_username_textview, username_textview, email_textview, tagcount_textview;
    private String dialogText = "nothing";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        displayInfo();

        findViewById(R.id.btn_lougout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserSettingActivity.this, LoginActivity.class));
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
                AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
                builder.setTitle("New Username");

                // Set up the input
                final EditText input = new EditText(UserSettingActivity.this);
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

        TagListFragment fragment = (TagListFragment) getSupportFragmentManager().findFragmentByTag("taglist");
        if (fragment != null){
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .add(R.id.tagListContainer, TagListFragment.newInstance("Hello", "From UserSetting"), "taglist")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tagListContainer, TagListFragment.newInstance("Hello", "From UserSetting"), "taglist")
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
                        Toast.makeText(UserSettingActivity.this, "You cannot use this username", Toast.LENGTH_SHORT).show();
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