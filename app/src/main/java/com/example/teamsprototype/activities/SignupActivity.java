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
import com.example.teamsprototype.services.User;
import com.example.teamsprototype.utilities.AppConstants;
import com.example.teamsprototype.utilities.Preferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity{
    FirebaseAuth auth;
    EditText username, emailTxt, passwordTxt, confirmPass;
    Button signUp;
    TextView login;
    ImageView showPass, showConPass;
    private Preferences preferences;
    boolean show_pass=false;
    boolean show_con_pass=false;

    String name, email, password, con_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        preferences = new Preferences(getApplicationContext());

        username = findViewById(R.id.username);
        emailTxt = findViewById(R.id.signupEmail);
        passwordTxt = findViewById(R.id.signupPass);
        confirmPass = findViewById(R.id.confirmPass);
        showPass = findViewById(R.id.showPass);
        showConPass = findViewById(R.id.showConPass);

        signUp = findViewById(R.id.signupBtn);
        login = findViewById(R.id.login);

        auth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(v -> {
            name = username.getText().toString();
            email = emailTxt.getText().toString();
            password = passwordTxt.getText().toString();
            con_pass = confirmPass.getText().toString();

            if(validateName(name) && validateEmail(email) && validatePassword(password, con_pass)){
                signUp.setVisibility(View.GONE);
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        signUp();
                    }else{
                        signUp.setVisibility(View.VISIBLE);
                        Toast.makeText(SignupActivity.this, "Signup Unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        login.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        showPass.setOnClickListener(v -> {
            show_pass = !show_pass;
            if(show_pass){
                passwordTxt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showPass.setImageResource(R.drawable.hide);
            } else {
                passwordTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showPass.setImageResource(R.drawable.show);
            }
        });

        showConPass.setOnClickListener(v -> {
            show_con_pass = !show_con_pass;
            if(show_con_pass) {
                confirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                showConPass.setImageResource(R.drawable.show);
            } else {
                confirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                showConPass.setImageResource(R.drawable.hide);
            }
        });
    }

    private boolean validateName(String name){
        boolean success = false;
        if(name.trim().isEmpty()){
            username.setError("Field can't be empty");
        } else {
            success = true;
        }
        return success;
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

    private boolean validatePassword(String password, String con_pass){
        boolean success = false;
        if(password.trim().isEmpty()){
            passwordTxt.setError("Field can't be empty");
        } else if (con_pass.trim().isEmpty()){
            confirmPass.setError("Field can't be empty");
        } else if (password.length()< 6 ){
            passwordTxt.setError("Password must be at least 6 characters");
        } else if(!password.equals(con_pass)){
            confirmPass.setError("Passwords don't match");
        } else {
            success = true;
        }
        return success;
    }

    private void signUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = auth.getUid();
        User user = new User(uid, name, email, password);

        assert uid != null;
        db.collection(AppConstants.KEY_COLLECTION).document(uid).set(user)
                .addOnSuccessListener(documentReference -> {
                    preferences.putBoolean(AppConstants.SIGNED_IN, true);
                    preferences.putString(AppConstants.NAME, name);
                    preferences.putString(AppConstants.EMAIL, email);
                    preferences.putString(AppConstants.PASSWORD, password);
                    preferences.putString(AppConstants.USER_ID, uid);

                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }



}