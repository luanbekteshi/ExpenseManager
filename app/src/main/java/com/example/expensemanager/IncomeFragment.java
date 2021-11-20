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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

public class IncomeFragment extends Fragment {



    //Firebase database
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;

    //Recyclerview
    private RecyclerView recyclerView;

    //TextView per total
    private TextView incomeTotalSum;

    ///Update editText
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    //Buttoni per update,delete
    private Button btnUpdate;
    private Button btnDelete;

    //Data item value
    private String type;
    private String note;
    private int amount;
    private String post_key;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview= inflater.inflate(R.layout.fragment_income, container, false);

    mAuth=FirebaseAuth.getInstance();
    FirebaseUser mUser=mAuth.getCurrentUser();
    String uid=mAuth.getUid();

    mIncomeDatabase= FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);

    //per Total
        incomeTotalSum=myview.findViewById(R.id.income_txt_result);

    recyclerView=myview.findViewById(R.id.recycle_id_income);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //kalkukimi vleres total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
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
                    incomeTotalSum.setText(stTotalvalue);

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


        //firebase recycler options
        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();


       FirebaseRecyclerAdapter<Data,MyViewHolder>adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
           @Override
           protected void onBindViewHolder(@NonNull MyViewHolder holder,final int position, @NonNull Data model) {
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
           public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

             View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data,parent,false);

               return new  MyViewHolder(view);
           }
       };
        adapter.startListening();

        recyclerView.setAdapter(adapter);

    }
//klasa MyViewHolder per mbushje nga DB te listes
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MyViewHolder(View itemView){
            super(itemView);
            mView=itemView;
        }

        private void setType(String type){
            TextView mType=mView.findViewById(R.id.type_txt_income);
            mType.setText(type);
        }
        private void setNote(String note){
            TextView mNote=mView.findViewById(R.id.note_txt_income);
            mNote.setText(note);
        }
        private void setDate(String date){
            TextView mDate=mView.findViewById(R.id.date_txt_income);
            mDate.setText(date);
        }
        private void setAmount(int amount){
            TextView mAmount=mView.findViewById(R.id.amount_txt_income);
            String stamount=String.valueOf(amount);
            mAmount.setText(stamount);
        }
    }

    //metoda per update
    private void updateDataItem(){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myview=inflater.inflate(R.layout.update_data_item,null);
        mydialog.setView(myview);
        edtAmount=myview.findViewById(R.id.ammount_edt);
        edtType=myview.findViewById(R.id.type_edt);
        edtNote=myview.findViewById(R.id.note_edt);

        //Set data to edittexat
        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(type.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());


        //butonat
        btnUpdate=myview.findViewById(R.id.btnUpdate);
        btnDelete=myview.findViewById(R.id.btnDelete);

       AlertDialog dialog=mydialog.create();

       //butoni per Update
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type=edtType.getText().toString().trim();
                note=edtNote.getText().toString().trim();
                String str_amount=String.valueOf(amount);
                str_amount=edtAmount.getText().toString().trim();

                int myAmount=Integer.parseInt(str_amount);

                String mDate= DateFormat.getDateInstance().format(new Date());
                //objekti Data me tdhanat e reja per update
                Data data=new Data(myAmount,type,note,post_key,mDate);
                mIncomeDatabase.child(post_key).setValue(data);
                dialog.dismiss();


            }
        });
        //butoni per delete
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //per delete
                mIncomeDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}