package edu.temple.templetag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    MaterialEditText username, email, password;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private Button registerBtn;
    private String userId;
    private Intent homeIntent;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                // Check conditions of required fields
                if (txt_email.equals("") || txt_password.equals("") || txt_username.equals("")){
                    Toast.makeText(SignUpActivity.this, "Please fill out all required fields!", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6){
                    Toast.makeText(SignUpActivity.this, "Password has to contain more than 5 characters!", Toast.LENGTH_SHORT).show();
                } else {
                    signUpAndIn(txt_username, txt_email, txt_password);
                }
            }
        });
    }

    private void signUpAndIn(final String username, final String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    userId = firebaseUser.getUid();

                    firestore = FirebaseFirestore.getInstance();
                    HashMap<String, String> newUser = new HashMap<>();
                    newUser.put("id", userId);
                    newUser.put("username", username);
                    newUser.put("email", email);

                    // Set FirebaseAuth user's display name to use when user creates a new tag
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();
                    firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User profile updated.");
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Something went wrong changing FirebaseAuth user's display name: "+e);
                                }
                            });

                    firestore.collection("Users").document(firebaseUser.getUid())
                            .set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startHomeActivity();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpActivity.this, "Something went wrong! Please try again.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startHomeActivity() {
        homeIntent = new Intent(SignUpActivity.this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }
}
