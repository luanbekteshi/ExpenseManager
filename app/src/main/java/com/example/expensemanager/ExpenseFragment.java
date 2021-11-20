package com.example.expensemanager;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;


public class ExpenseFragment extends Fragment {


    //firebase db
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    //Recyclerview
    private RecyclerView recyclerView;

    //TextView per total
    private TextView expenseTotalSum;

    //Edt data itemat
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    private Button btnUpdate;
    private Button btnDelete;

    //Data
    private String type;
    private String note;
    private int amount;
    private String post_key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview= inflater.inflate(R.layout.fragment_expense, container, false);
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid=mUser.getUid();

        mExpenseDatabase= FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        //per Total
        expenseTotalSum=myview.findViewById(R.id.expense_txt_result);

        recyclerView=myview.findViewById(R.id.recycle_id_expense);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        //method expense total
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalvalue=0;
                for(DataSnapshot mysnapshot:snapshot.getChildren()){


                    Data data=mysnapshot.getValue(Data.class);
                    //shfaqja e totalit

                    totalvalue+=data.getAmount();
                    //conver to string
                    String stTotalvalue=String.valueOf(totalvalue);

                    //vendosja te textview
                    expenseTotalSum.setText(stTotalvalue);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();

        //firebaserecycler options
        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDatabase, Data.class)
                        .build();

       //leximi i vlerave
        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
                holder.setAmount(model.getAmount());

                //per update
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key=getRef(holder.getAbsoluteAdapterPosition()).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();
                        updateDataItem();
                    }
                });

            }

            @NonNull
            @Override
            public ExpenseFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycle_data,parent,false);

                return new MyViewHolder(view);
            }
        };
        adapter.startListening();

        recyclerView.setAdapter(adapter);

    }

    //klasa MyViewHolder
    public static class  MyViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }

        private void setDate(String date){
            TextView mDate=mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }

        private void setType(String type){
            TextView mType=mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }
        private void setNote(String note){
            TextView mNote=mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);
        }
        private void setAmount(int amount){
            TextView mAmount=mView.findViewById(R.id.amount_txt_expense);
            String strAmount=String.valueOf(amount);
            mAmount.setText(strAmount);
        }
    }
    //metoda per update ne edittexta
    private void updateDataItem(){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myview=inflater.inflate(R.layout.update_data_item,null);
         mydialog.setView(myview);

        edtAmount=myview.findViewById(R.id.ammount_edt);
        edtNote=myview.findViewById(R.id.note_edt);
        edtType=myview.findViewById(R.id.type_edt);

        //mbushja e edittextave
        edtType.setText(type);
        edtType.setSelection(type.length());
        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        //butonat
        btnUpdate=myview.findViewById(R.id.btnUpdate);
        btnDelete=myview.findViewById(R.id.btnDelete);

        AlertDialog dialog=mydialog.create();

        //klikimi butonit update
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type=edtType.getText().toString().trim();
                note=edtNote.getText().toString().trim();

               String str_amount=String.valueOf(amount);
               str_amount=edtAmount.getText().toString().trim();
               int int_amount=Integer.parseInt(str_amount);
               String mDate= DateFormat.getDateInstance().format(new Date());
               //objekti
                Data data=new Data(int_amount,type,note,post_key,mDate);
                //update ne DB
                mExpenseDatabase.child(post_key).setValue(data);
                dialog.dismiss();

            }
        });
        //klikimi butonit delete
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //delete ne DB
                mExpenseDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });
        dialog.show();


    }


}