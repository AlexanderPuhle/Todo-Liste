package com.example.todo_liste;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private final String TAG ="LoginActivity";
    private TextView ergebnisLogin;
    private FirebaseAuth mAuth;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView register = findViewById(R.id.textRegister);
        subscribeTopics();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigieren Sie zur registerActivity, wenn der TextView geklickt wird
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        Button btnLogin = findViewById(R.id.btnLogIn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ergebnisLogin = findViewById(R.id.textViewLoginErg);
                mAuth = FirebaseAuth.getInstance();
                EditText eMailEdit = (EditText) findViewById(R.id.editTextLoginEmail);
                EditText pwEdit = (EditText) findViewById(R.id.editTextLoginPassword);
                String emailLogin = eMailEdit.getText().toString();
                String pwLogin = pwEdit.getText().toString();
                Log.d(TAG, "Login leer");
                if(TextUtils.isEmpty(emailLogin)
                        || TextUtils.isEmpty(pwLogin)){
                    ergebnisLogin.setText("Bitte geben Sie die E-Mail und Passwort ein");
                    return;
                }

                mAuth.signInWithEmailAndPassword(emailLogin, pwLogin)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Erfolgreich angemeldet",
                                            Toast.LENGTH_SHORT).show();

                                    Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(MainIntent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Login", "Nicht erfolgreich: ", task.getException());
                                    ergebnisLogin.setText("Login fehlerhaft: Entweder Passwort falsch " +
                                            "oder Sie sind nicht registriert");
                                }
                            }
                        });
            }
        });
    }
    private void subscribeTopics() {
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic("BoardGamer")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                    }
                });
    }
}