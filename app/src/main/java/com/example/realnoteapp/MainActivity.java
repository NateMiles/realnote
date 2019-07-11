package com.example.realnoteapp;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private EditText emailTV, passwordTV;
    private Button loginBtn;
    private Button signinBtn;
    String email, password;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        initializeUI();
        setTitle("Welcome");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpAcc();
            }
        });
        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    private void signUpAcc() {
        email = emailTV.getText().toString().toLowerCase();
        password = passwordTV.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(getApplicationContext(), "Sign up success, please login", Toast.LENGTH_LONG).show();
                            System.out.println("SUCCESS");
                            dbCreate();
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Sign up failed, please retry", Toast.LENGTH_LONG).show();
                            System.out.println("FAILURE");
                        }
                    }
                });
    }
     private void signIn(){
         email = emailTV.getText().toString();
         password = passwordTV.getText().toString();


         mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            Intent chat= new Intent(getApplicationContext(),UserPage.class);
                            chat.putExtra("email", email);
                            startActivity(chat);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void dbCreate(){
            // Create a new user with email and username defaulted to their email address
            Map<String, Object> user = new HashMap<>();
            user.put("name", email);
            user.put("email", email);

            // Add a new document with email as doc ID
            db.collection("users").document(email).set(user);
    }

       private void initializeUI() {
           emailTV = findViewById(R.id.email);
           passwordTV = findViewById(R.id.password);
           loginBtn = findViewById(R.id.login);
           signinBtn = findViewById(R.id.signIn);

       }


}
