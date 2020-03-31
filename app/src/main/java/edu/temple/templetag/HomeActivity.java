package edu.temple.templetag;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.temple.templetag.fragments.MapFragment;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private FloatingActionButton createTagBtn;
    private Intent createTagIntent;
    private String txt_username, txt_email;

    private static final String TAG = "HomeActivity";

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null){
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        createTagBtn = findViewById(R.id.createTagBtn);

        createTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateTagActivity();
            }
        });

        // Attach mapFragment
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragment");
        if (null != mapFragment){
            getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .add(R.id.mapContainer, MapFragment.newInstance(), "mapfragment")
                    .commit();
        } else {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment,"mapfragment")
                    .commit();
        }

        //Profile On CLick
        CircleImageView profile = findViewById(R.id.userProfile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("username", txt_username);
                bundle.putString("email", txt_email);
                Intent intent = new Intent(HomeActivity.this, UserSettingActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void displayUserInfo(){
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("id").equals(firebaseUser.getUid())) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    txt_username = (String) document.getData().get("username");
                                    txt_email = (String) document.getData().get("email");
                                    getSupportActionBar().setTitle("Welcome, " + txt_username);
                                    break;
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(HomeActivity.this, "Error getting user from Firestore to display in action bar..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startCreateTagActivity() {
        createTagIntent = new Intent(HomeActivity.this, CreateTagActivity.class);
        startActivity(createTagIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null){
            displayUserInfo();
        }
    }
}
