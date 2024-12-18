package com.example.personalwallet;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.personalwallet.model.Data;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FloatingActionButton fab_main_btn, fab_income_btn, fab_expense_btn, fab_threshold_btn;
    private TextView fab_income_txt, fab_expense_txt, fab_threshold_txt;
    private boolean isOpen = false;
    private Animation fadeOpen, fadeClose;
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase, mExpenseDatabase;
    private TextView totalIncomeResult;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser != null ? mUser.getUid() : null;

        mIncomeDatabase = FirebaseDatabase.getInstance("https://personal-wallet-41219-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("IncomeData").child(uid);
        //mExpenseDatabase = FirebaseDatabase.getInstance("https://personal-wallet-41219-default-rtdb.europe-west1.firebasedatabase.app/")
                //.getReference().child("ExpenseData").child(uid);

        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_btn);
        //fab_expense_btn = myview.findViewById(R.id.expense_ft_btn);
        //fab_threshold_btn = myview.findViewById(R.id.threshold_ft_btn);
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        //fab_expense_txt = myview.findViewById(R.id.expense_ft_text);
        //fab_threshold_txt = myview.findViewById(R.id.threshold_ft_text);
        totalIncomeResult = myview.findViewById(R.id.income_set_result);

        fadeOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        fadeClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        fab_main_btn.setOnClickListener(v -> {
            addData();

            if (isOpen) {
                fab_income_btn.startAnimation(fadeClose);
                //fab_expense_btn.startAnimation(fadeClose);
                //fab_threshold_btn.startAnimation(fadeClose);
                fab_income_btn.setClickable(false);
                //fab_expense_btn.setClickable(false);
                //fab_threshold_btn.setClickable(false);
                fab_income_txt.startAnimation(fadeClose);
                //fab_expense_txt.startAnimation(fadeClose);
                //fab_threshold_txt.startAnimation(fadeClose);
                fab_income_txt.setClickable(false);
                //fab_expense_txt.setClickable(false);
                //fab_threshold_txt.setClickable(false);
                isOpen = false;
            } else {
                fab_income_btn.startAnimation(fadeOpen);
                //fab_expense_btn.startAnimation(fadeOpen);
                //fab_threshold_btn.startAnimation(fadeOpen);
                fab_income_btn.setClickable(true);
                //fab_expense_btn.setClickable(true);
                //fab_threshold_btn.setClickable(true);
                fab_income_txt.startAnimation(fadeOpen);
                //fab_expense_txt.startAnimation(fadeOpen);
                //fab_threshold_txt.startAnimation(fadeOpen);
                fab_income_txt.setClickable(false);
                //fab_expense_txt.setClickable(false);
                //fab_threshold_txt.setClickable(false);
                isOpen = true;
            }
        });

        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalsum = 0;
                for (DataSnapshot mySnapshot : snapshot.getChildren()) {
                    Data data = mySnapshot.getValue(Data.class);
                    if (data != null) {
                        totalsum += data.getAmount();
                    }
                }
                String stResult = String.valueOf(totalsum);
                totalIncomeResult.setText(stResult);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error here if needed
            }
        });

        return myview;
    }
    private void addData() {
        fab_income_btn.setOnClickListener(v -> incomeDataInsert());
    }
    private void incomeDataInsert() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myviewm = inflater.inflate(R.layout.custom_layout_for_insert_data, null);
        mydialog.setView(myviewm);
        AlertDialog dialog = mydialog.create();

        EditText edtAmount = myviewm.findViewById(R.id.amount_edt);
        EditText edtType = myviewm.findViewById(R.id.type_edt);
        Button btnSave = myviewm.findViewById(R.id.btnSave);
        Button btnCancel = myviewm.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String type = edtType.getText().toString().trim();
            String amount = edtAmount.getText().toString().trim();

            if (TextUtils.isEmpty(type)) {
                edtType.setError("Required field..");
                return;
            }
            if (TextUtils.isEmpty(amount)) {
                edtAmount.setError("Required field..");
                return;
            }
            int ourAmountInt = Integer.parseInt(amount);
            String id = mIncomeDatabase.push().getKey();
            String mDate = DateFormat.getDateInstance().format(new Date());
            Data data = new Data(ourAmountInt, type, id, mDate);
            mIncomeDatabase.child(id).setValue(data);
            Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}