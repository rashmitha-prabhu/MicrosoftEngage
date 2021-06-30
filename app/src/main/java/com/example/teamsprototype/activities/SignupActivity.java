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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{
    FirebaseAuth auth;
    EditText username, emailTxt, passwordTxt, confirmPass;
    Button signUp;
    TextView login;
    private Preferences preferences;

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

        signUp = findViewById(R.id.signupBtn);
        login = findViewById(R.id.login);

        auth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.signupBtn:
                name = username.getText().toString();
                email = emailTxt.getText().toString();
                password = passwordTxt.getText().toString();
                con_pass = confirmPass.getText().toString();

                if(name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || con_pass.trim().isEmpty()) {
                    Toast.makeText(SignupActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(password.equals(con_pass)){
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                signUp();
                                startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                                finish();
                            }else{
                                Toast.makeText(SignupActivity.this, "SignUp Unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(SignupActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.login:
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    private void signUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(AppConstants.NAME, name);
        user.put(AppConstants.EMAIL, email);
        user.put(AppConstants.PASSWORD, password);

        db.collection(AppConstants.KEY_COLLECTION)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferences.putBoolean(AppConstants.SIGNED_IN, true);
                    preferences.putString(AppConstants.USER_ID, documentReference.getId());
                    preferences.putString(AppConstants.NAME, name);
                    preferences.putString(AppConstants.EMAIL, email);
                    preferences.putString(AppConstants.PASSWORD, password);
                    preferences.putString(AppConstants.USER_ID, documentReference.toString());
                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}