package story.book.storyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import story.book.storyapp.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {


    //view binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //start firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });


    }
    private String name = "", email = "", password = "";
    private void validateData() {
        //before acc is made.

        //get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.cPasswordEt.getText().toString().trim();

        //validate the data, make sure that the fields arnt empty
        if (TextUtils.isEmpty(name)){
            //name must not be empty
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //Email is not entered or does not follow pattern, IE: abc.com | correct --> abc@gmail.com
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(password)){
            //Password must not be empty
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(cPassword)){
            //Confirm password must not be empty
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cPassword)){
            //password and confirm password do not match.
            Toast.makeText(this, "Confirm password and password must match", Toast.LENGTH_SHORT).show();
        }
        else{
            //all data as be checked and is valid.
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progress
        progressDialog.setMessage("Creating account");
        progressDialog.show();

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account creation
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //account creating failed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid
        String uid = firebaseAuth.getUid();

        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); //temp NULL
        hashMap.put("userType", "user"); //poss val
        hashMap.put("timestamp", timestamp);

        // set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data added
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}