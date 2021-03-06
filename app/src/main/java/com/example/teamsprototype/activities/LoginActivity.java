package com.example.teamsprototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teamsprototype.R;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity{
    FirebaseAuth auth;
    EditText emailTxt, passwordTxt;
    Button login;
    TextView signUp;
    Preferences preferences;
    String email, password;
    ImageView showPass;
    boolean show = false;

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

        auth = FirebaseAuth.getInstance();
        emailTxt = findViewById(R.id.loginEmail);
        passwordTxt = findViewById(R.id.loginPass);
        login = findViewById(R.id.loginBtn);
        signUp = findViewById(R.id.signUp);
        showPass = findViewById(R.id.showLogPass);

//        Login with valid credentials
        login.setOnClickListener(v -> {
            email = emailTxt.getText().toString();
            password = passwordTxt.getText().toString();
            if(validateEmail(email)){
                if(validatePassword(password)){
                    login.setVisibility(View.GONE);
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            login();
                        } else {
                            login.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

//        Switch to create new account
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        showPass.setOnClickListener(v -> {
            show = !show;
            if(show){
                passwordTxt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showPass.setImageResource(R.drawable.show);
            } else {
                passwordTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showPass.setImageResource(R.drawable.hide);
            }
        });
    }

//    Update Shared Preferences upon successful login
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
                        preferences.putString(AppConstants.USER_ID, documentSnapshot.getString(AppConstants.USER_ID));
                        preferences.putString(AppConstants.NAME, documentSnapshot.getString(AppConstants.NAME));
                        preferences.putString(AppConstants.EMAIL, documentSnapshot.getString(AppConstants.EMAIL));

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

    private boolean validateEmail(String email){
        boolean success = false;
        if(email.trim().isEmpty()){
            emailTxt.setError("Field can't be empty");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailTxt.setError("Enter valid email");
        } else {
            success = true;
        }
        return success;
    }

    private boolean validatePassword(String password){
        boolean success = false;
        if(password.trim().isEmpty()) {
            passwordTxt.setError("Field can't be empty");
        } else {
            success = true;
        }
        return success;
    }

}