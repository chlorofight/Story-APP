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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import story.book.storyapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //frirebase auth
    private FirebaseAuth firebaseAuth;

    //progress dia
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //start firebase instance
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);



        //handle click :0, to register screen
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //handle click
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private String email = "", password = "";


    private void validateData() {
        //get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //Email is not entered or does not follow pattern, IE: abc.com | correct --> abc@gmail.com
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(password)){
            //Password must not be empty
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        }
        else{
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Logging in");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login suc
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("checking");
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        //check if user is admin or user
        //check in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //get user type
                        String userType = ""+snapshot.child("userType").getValue();
                        //check usertype
                        if(userType.equals("user")){
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                        else if (userType.equals("admin")){
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}