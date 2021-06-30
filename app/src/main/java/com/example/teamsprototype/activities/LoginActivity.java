package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    FirebaseAuth auth;
    EditText emailTxt, passwordTxt;
    Button login;
    TextView signUp;
    Preferences preferences;
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = new Preferences(getApplicationContext());
        if(preferences.getBoolean(AppConstants.SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            finish();
        }

        emailTxt = findViewById(R.id.loginEmail);
        passwordTxt = findViewById(R.id.loginPass);
        login = findViewById(R.id.loginBtn);
        signUp = findViewById(R.id.signUp);
        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(this);
        signUp.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginBtn:
                email = emailTxt.getText().toString();
                password = passwordTxt.getText().toString();
                if(email.trim().isEmpty() || password.trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
                }
                else{
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            login();
                        } else {
                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;

            case R.id.signUp:
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void login() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.KEY_COLLECTION)
                .whereEqualTo(AppConstants.EMAIL, emailTxt.getText().toString())
                .whereEqualTo(AppConstants.PASSWORD, passwordTxt.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferences.putBoolean(AppConstants.SIGNED_IN, true);
                        preferences.putString(AppConstants.USER_ID, documentSnapshot.getId());
                        preferences.putString(AppConstants.NAME, documentSnapshot.getString(AppConstants.NAME));
                        preferences.putString(AppConstants.EMAIL, documentSnapshot.getString(AppConstants.EMAIL));
                        Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "User Unavailable. Create Account", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}