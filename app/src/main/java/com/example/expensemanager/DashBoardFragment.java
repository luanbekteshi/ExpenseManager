package com.example.expensemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Inflater;


public class DashBoardFragment extends Fragment {

    //Floating buttoni
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;

    //floation button textview
    private TextView fab_income_txt;
    private TextView fab_expense_txt;

    //
    private boolean isOpen=false;

    //Animation objects
    private Animation FadeOpen;
    private Animation FadeClose;

    //Dashboard income dhe expense tOTAL result..
    private TextView totalIncomeResult;
    private TextView totalExpenseResult;

    //Per FIREBASE DB
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    //RecyclerView
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView= inflater.inflate(R.layout.fragment_dash_board, container, false);

       //Firebase inicializim
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUSer=mAuth.getCurrentUser();
        String uid=mUSer.getUid();

        mIncomeDatabase= FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase=FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        //
        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);


        //Connectimi recyclerview
        
        mRecyclerIncome=myView.findViewById(R.id.recycler_income);
        mRecyclerExpense=myView.findViewById(R.id.recycler_expense);



        //Connectimi floationg butonit me layoutin

        fab_main_btn=myView.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn=myView.findViewById(R.id.income_ft_btn);
        fab_expense_btn=myView.findViewById(R.id.expense_ft_btn);

        //Connectimi i floation textview me layoutin
        fab_income_txt=myView.findViewById(R.id.income_ft_text);
        fab_expense_txt=myView.findViewById(R.id.expense_ft_text);

        //Total income dhe expense
        totalIncomeResult=myView.findViewById(R.id.income_set_result);
        totalExpenseResult=myView.findViewById(R.id.expense_set_result);


        //Animation conection me layout
        FadeOpen= AnimationUtils.loadAnimation(getActivity(),R.anim.fade_open);
        FadeClose=AnimationUtils.loadAnimation(getActivity(),R.anim.fade_close);

         //when clicked
        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addData();
                ftAnimation();


            }
        });

        //kalkulimi i TOTAL income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalsum=0;
                for(DataSnapshot mysnap:snapshot.getChildren()){
                    Data data=mysnap.getValue(Data.class);
                    totalsum+=data.getAmount();

                    String str_result=String.valueOf(totalsum);
                    totalIncomeResult.setText(str_result);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //kalkulimi i TOTAL expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalsum=0;
                for(DataSnapshot mysnap:snapshot.getChildren()){
                    Data data=mysnap.getValue(Data.class);
                    totalsum+=data.getAmount();

                    String str_result=String.valueOf(totalsum);
                    totalExpenseResult.setText(str_result);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Recycler
            //per income
        LinearLayoutManager layoutManagerIncome=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

             //per expense
        LinearLayoutManager layoutManagerExpense=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerExpense.setReverseLayout(true);
        layoutManagerExpense.setStackFromEnd(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);


        return myView;
    }

    //method for animation

    private void ftAnimation(){
        if(isOpen){
            fab_income_btn.startAnimation(FadeClose);
            fab_expense_btn.startAnimation(FadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadeClose);
            fab_expense_txt.startAnimation(FadeClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);
            isOpen=false;
        }
        else{
            fab_income_btn.startAnimation(FadeOpen);
            fab_expense_btn.startAnimation(FadeOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadeOpen);
            fab_expense_txt.startAnimation(FadeOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);
            isOpen=true;
        }
    }

    private void addData(){
        //Fab Button income

        //call of methods for insert
        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incomeDataInsert();
            }
        });
        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                      expenseDataInsert();
            }
        });
    }

    //metoda per incomeDataInsert
    public void incomeDataInsert(){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());

        View myviewm=inflater.inflate(R.layout.custom_layout_for_insertdata,null);
        mydialog.setView(myviewm);
        AlertDialog dialog=mydialog.create();

        dialog.setCancelable(false);

        EditText edtAmount=myviewm.findViewById(R.id.ammount_edt);
        EditText edtType=myviewm.findViewById(R.id.type_edt);
        EditText edtNote=myviewm.findViewById(R.id.note_edt);

        Button btnSave=myviewm.findViewById(R.id.btnSave);
        Button btnCancel=myviewm.findViewById(R.id.btnCancel);


        //save butoni
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type=edtType.getText().toString().trim();
                String amount=edtAmount.getText().toString().trim();
                String note=edtNote.getText().toString().trim();

                if(TextUtils.isEmpty(type)){
                    edtType.setError("Required field..");
                    return;
                }
            if(TextUtils.isEmpty((amount))){
                edtAmount.setError("Required field..");
                return;
            }

            int ouramountint=Integer.parseInt(amount);

                if(TextUtils.isEmpty((note))){
                    edtNote.setError("Required field..");
                    return;
                }

                //per DB
                //random Id
                String id=mIncomeDatabase.push().getKey();

                String mDate= DateFormat.getDateInstance().format(new Date());
               //objekti
                Data data=new Data(ouramountint,type,note,id,mDate);
                //insertimi ne DB
                mIncomeDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(),"Data ADDED",Toast.LENGTH_SHORT).show();

                ftAnimation();


                //me hek dialogun
                dialog.dismiss();


            }




    });


        //cancel butooni
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ftAnimation();

                dialog.dismiss();
            }
        });

        dialog.show();


    }
    //metoda per expenseDataInsert
    public void expenseDataInsert(){
        AlertDialog.Builder myDialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());

        View myview=inflater.inflate(R.layout.custom_layout_for_insertdata,null);
        myDialog.setView(myview);

        final   AlertDialog dialog=myDialog.create();

        //per user me bo cancel veq ne buton
        dialog.setCancelable(false);


        EditText amount=myview.findViewById(R.id.ammount_edt);
        EditText type=myview.findViewById(R.id.type_edt);
        EditText note=myview.findViewById(R.id.note_edt);

        Button btnSave=myview.findViewById(R.id.btnSave);
        Button btnCancel=myview.findViewById(R.id.btnCancel);
        // butoni save
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmAmount=amount.getText().toString().trim();
                String tmType=type.getText().toString().trim();
                String tmNote=note.getText().toString().trim();

                if(TextUtils.isEmpty(tmAmount)){
                    amount.setError("Required Field...");
                    return;
                }
                //convert to Int
                int inamount=Integer.parseInt(tmAmount);

                if(TextUtils.isEmpty(tmType)){
                    type.setError("Required field...");
                    return;
                }
                if(TextUtils.isEmpty(tmNote)){
                    note.setError("Required field..");
                    return;
                }

                //per insert
                String id=mExpenseDatabase.push().getKey();
                String mDate=DateFormat.getDateInstance().format(new Date());
                Data data=new Data(inamount,tmType,tmNote,id,mDate);
                //insertimi ne DB i objektit data
                mExpenseDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(),"Data ADDED",Toast.LENGTH_SHORT).show();

                ftAnimation();
                dialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ftAnimation();


                //mbyllet dialogu
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
         //options for IncomeDB
        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();

        //options for ExpenseDB
        FirebaseRecyclerOptions<Data> options_exp =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDatabase, Data.class)
                        .build();

        //firebaserecycler  for income
        FirebaseRecyclerAdapter<Data,IncomeViewHolder>incomeAdapter=new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {

                holder.setIncomeType(model.getType());
                holder.setIncomeAmount(model.getAmount());
                holder.setIncomeDate(model.getDate());

            }

            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income,parent,false);

                return new DashBoardFragment.IncomeViewHolder(view);
            }
        };
        incomeAdapter.startListening();

        mRecyclerIncome.setAdapter(incomeAdapter);

        //firebaserecycler  for expense
        FirebaseRecyclerAdapter<Data,ExpenseViewHolder>expenseAdapter=new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(options_exp) {
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {

                holder.setExpenseType(model.getType());
                holder.setExpenseAmount(model.getAmount());
                holder.setExpenseDate(model.getDate());

            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense,parent,false);

                return new DashBoardFragment.ExpenseViewHolder(view);
            }
        };
        expenseAdapter.startListening();

        mRecyclerExpense.setAdapter(expenseAdapter);

    }

    //Class for income data
    public static class IncomeViewHolder extends RecyclerView.ViewHolder{


        View mIncomeView;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mIncomeView=itemView;
        }

        public void setIncomeType(String type){
            TextView mtype=mIncomeView.findViewById(R.id.type_income_ds);
            mtype.setText(type);
        }
        public void setIncomeAmount(int amount){
            TextView mamount=mIncomeView.findViewById(R.id.amount_income_ds);
              String str_amount=String.valueOf(amount);
            mamount.setText(str_amount);
        }
        public void setIncomeDate(String date){
            TextView mDate=mIncomeView.findViewById(R.id.date_income_ds);
            mDate.setText(date);
        }
    }

    //Class for expense Data
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{

        View mExpenseView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mExpenseView=itemView;
        }
        public void setExpenseType(String type){
            TextView mtype=mExpenseView.findViewById(R.id.type_expense_ds);
            mtype.setText(type);
        }
        public void setExpenseAmount(int amount){
            TextView mAmount=mExpenseView.findViewById(R.id.amount_expense_ds);
            String str_amount=String.valueOf(amount);
            mAmount.setText(str_amount);
        }
        public void setExpenseDate(String date){
            TextView mDate=mExpenseView.findViewById(R.id.date_expense_ds);
            mDate.setText(date);
        }
    }
}