package story.book.storyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import story.book.storyapp.databinding.RowCategoryBinding;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HoldCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    //View binding
    private RowCategoryBinding binding;

    //instance of our filter
    private FilterCategory filter;


    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HoldCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent, false);
        return new HoldCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HoldCategory holder, int position) {
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        String timestamp = model.getTimestamp();

        //set data
        holder.categoryTv.setText(category);

        //handle click, delete category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirm delete
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this category")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin deleting
                                Toast.makeText(context, "Deleting", Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }) .show();
            }
        });
    }

    private void deleteCategory(ModelCategory model, HoldCategory holder) {
        //get id of category to delete
        String id = model.getId();
        //Firebase Db > Categories  > categoryId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(String.valueOf(id))
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //delete success
                        Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }

    /*View holder class to hold UI views*/
    class HoldCategory extends RecyclerView.ViewHolder{

        //ui view of row.
        TextView categoryTv;
        ImageButton deleteBtn;

        public HoldCategory(@NonNull View itemView) {
            super(itemView);

            //init ui views
            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
