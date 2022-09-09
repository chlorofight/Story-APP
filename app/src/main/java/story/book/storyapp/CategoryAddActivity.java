package story.book.storyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import story.book.storyapp.databinding.ActivityCategoryAddBinding;

public class CategoryAddActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoryAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle back click
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private String category ="";

    private void validateData() {
        /*must validate data */

        //get data
        category = binding.categoryEt.getText().toString().trim();
        //validate if not empty
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Please enter a category", Toast.LENGTH_SHORT).show();
        }
        else{
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        //show progress
        progressDialog.setMessage("Adding Category");
        progressDialog.show();

        //get timestamp
        String timestamp = Long.toString(System.currentTimeMillis());



        //setup info add in firebase
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+ firebaseAuth.getUid());

        /*
        add to firebase db
        location: Database Roo > Categories > categoryId > category info
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //addon success
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "categories addon success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //addon failed
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}