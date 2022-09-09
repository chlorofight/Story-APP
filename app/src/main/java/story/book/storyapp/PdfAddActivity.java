package story.book.storyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import story.book.storyapp.databinding.ActivityPdfAddBinding;

public class PdfAddActivity extends AppCompatActivity {

    //binding
    private ActivityPdfAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //array to hold the categories
    private ArrayList<ModelCategory> categoryArrayList;

    //uri of picked pdf
    private Uri pdfUri = null;

    private static final int PDF_PICK_CODE = 1000;

    //TAG for debugging?
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //handle click
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, attach PDF
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        //handle pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        //handle upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                validateData();
            }
        });

    }

    private String title = "", description = "", category = "";

    private void validateData() {
        Log.d(TAG, "validateData: validating data");
        
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        category = binding.categoryTv.getText().toString().trim();

        //validate
        if(TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri == null) {
            Toast.makeText(this, "Pick a pdf", Toast.LENGTH_SHORT).show();
        }
        else{
            //data is valid
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading data");

        //show progress
        progressDialog.setMessage("Uploading PDF");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //path of pdf
        String filePathAndName = "Books/" +timestamp;
        //storage ref
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDf uploaded");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedPdfUrl= ""+uriTask.getResult();

                        //upload to firebase db
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF upload Failed due" +e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading file to firebase Db");
        progressDialog.setMessage("Uploading pdf info");
        String uid = firebaseAuth.getUid();

        //setup data  to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("category",""+category);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",""+timestamp);

        //db reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Success");
                        Toast.makeText(PdfAddActivity.this, "Sucessfully uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories");
        categoryArrayList = new ArrayList<>();

        //db ref
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear(); //clear before adding data
                for(DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to arrayList
                    categoryArrayList.add(model);

                    Log.d(TAG, "onDataChange: "+model.getCategory());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category");

        //get array
        String[] categoriesArray = new String[categoryArrayList.size()];
        for (int i=0; i<categoryArrayList.size(); i++){
            categoriesArray[i] = categoryArrayList.get(i).getCategory();
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item click
                        //get clicked item
                        String category = categoriesArray[i];
                        //set cat to text view
                        binding.categoryTv.setText(category);

                        Log.d(TAG, "onClick: Selected category:"+category);

                    }
                })
                .show();

    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"),PDF_PICK_CODE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(resultCode ==  PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI:"+pdfUri);
            }
        }
        else{
            Log.d(TAG, "onActivityResult: cancelled");
            Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}

