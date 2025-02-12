package com.example.personalwallet;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.personalwallet.model.Data;
import com.example.personalwallet.model.SavingsThreshold;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    Calendar calendar = Calendar.getInstance();
    String currentMonth = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());

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
        mExpenseDatabase = FirebaseDatabase.getInstance("https://personal-wallet-41219-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child("ExpenseData").child(uid);

        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn);
        fab_threshold_btn = myview.findViewById(R.id.threshold_ft_btn);
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text);
        fab_threshold_txt = myview.findViewById(R.id.threshold_ft_text);
        totalIncomeResult = myview.findViewById(R.id.income_set_result);

        fadeOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        fadeClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        fab_main_btn.setOnClickListener(v -> {
            addData();

            if (isOpen) {
                fab_income_btn.startAnimation(fadeClose);
                fab_expense_btn.startAnimation(fadeClose);
                fab_threshold_btn.startAnimation(fadeClose);
                fab_income_btn.setClickable(false);
                fab_expense_btn.setClickable(false);
                fab_threshold_btn.setClickable(false);
                fab_income_txt.startAnimation(fadeClose);
                fab_expense_txt.startAnimation(fadeClose);
                fab_threshold_txt.startAnimation(fadeClose);
                fab_income_txt.setClickable(false);
                fab_expense_txt.setClickable(false);
                fab_threshold_txt.setClickable(false);
                isOpen = false;
            } else {
                fab_income_btn.startAnimation(fadeOpen);
                fab_expense_btn.startAnimation(fadeOpen);
                fab_threshold_btn.startAnimation(fadeOpen);
                fab_income_btn.setClickable(true);
                fab_expense_btn.setClickable(true);
                fab_threshold_btn.setClickable(true);
                fab_income_txt.startAnimation(fadeOpen);
                fab_expense_txt.startAnimation(fadeOpen);
                fab_threshold_txt.startAnimation(fadeOpen);
                fab_income_txt.setClickable(false);
                fab_expense_txt.setClickable(false);
                fab_threshold_txt.setClickable(false);
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
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add the monthly boxes
        addMonthlyBoxes();

        // Fetch userId from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            view.post(() -> calculateAndFetchMonthlyStatus(userId, this::updateMonthlyBoxColors));
            fetchMonthlyExpenses(userId, this::displayMonthlyExpensesGraph);
        } else {
            Log.e("DashboardFragment", "User is not authenticated!");
        }
    }
    private void addData() {
        fab_income_btn.setOnClickListener(v -> incomeDataInsert());
        fab_expense_btn.setOnClickListener(v -> expenseDataInsert());
        fab_threshold_btn.setOnClickListener(v -> showSetThresholdDialog(currentMonth));
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
    public void expenseDataInsert() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.custom_layout_for_insert_data, null);
        myDialog.setView(myView);
        AlertDialog dialog = myDialog.create();

        EditText edtAmount = myView.findViewById(R.id.amount_edt);
        EditText edtType = myView.findViewById(R.id.type_edt);

        Button btnSave = myView.findViewById(R.id.btnSave);
        Button btnCancel = myView.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                String id = mExpenseDatabase.push().getKey();
                SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
                String mDate = sdf.format(new Date()).toLowerCase();

                Data data = new Data(ourAmountInt, type, id, mDate);
                mExpenseDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void showSetThresholdDialog(String month) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Set Savings Threshold for " + month);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter threshold amount");
        dialogBuilder.setView(input);

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String thresholdAmount = input.getText().toString().trim();
                if (!thresholdAmount.isEmpty()) {
                    try{
                        int threshold = Integer.parseInt(thresholdAmount);
                        // Sanitize the month string before saving
                        String sanitizedMonth = month.replaceAll("[.#$\\[\\]]", "");
                        saveThresholdToFirebase(sanitizedMonth, threshold);
                    } catch (NumberFormatException e) {
                        Log.e("DashboardFragment", "Invalid number format: " + thresholdAmount, e);
                        // Optionally, show an error message to the user.
                    }
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialogBuilder.create().show();
    }

    private void saveThresholdToFirebase(String month, int threshold) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid != null) {
            DatabaseReference thresholdRef = FirebaseDatabase.getInstance(
                            "https://personal-wallet-41219-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("UserThresholds")
                    .child(uid);

            thresholdRef.child(month).setValue(threshold).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("DashboardFragment", "Threshold updated for " + month + ": " + threshold);
                    } else {
                        Log.e("DashboardFragment", "Failed to update threshold for " + month, task.getException());
                    }
                }
            });
        }
    }
    private void calculateAndFetchMonthlyStatus(String userId, Callback<Map<String, Boolean>> callback) {
        DatabaseReference thresholdRef = FirebaseDatabase.getInstance("https://personal-wallet-41219-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("UserThresholds").child(userId);

        thresholdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot thresholdSnapshot) {
                SavingsThreshold thresholds = thresholdSnapshot.getValue(SavingsThreshold.class);
                if (thresholds == null) thresholds = new SavingsThreshold();

                Map<String, Boolean> monthStatus = new HashMap<>();
                String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

                DatabaseReference expenseRef = mExpenseDatabase;

                SavingsThreshold finalThresholds = thresholds;
                expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot expenseSnapshot) {
                        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
                        Map<String, Integer> monthlyExpenses = new HashMap<>();

                        // Sum up expenses per month
                        for (DataSnapshot snapshot : expenseSnapshot.getChildren()) {
                            Data data = snapshot.getValue(Data.class);
                            if (data == null || data.getDate() == null) continue;

                            Log.d("FirebaseExpense", "Expense Retrieved: " + data.getDate() + " | Amount: " + data.getAmount());

                            try {
                                Date date = sdf.parse(data.getDate().toLowerCase());
                                if (date == null) continue;

                                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
                                String monthName = monthFormat.format(date).toLowerCase();

                                monthlyExpenses.put(monthName,
                                        monthlyExpenses.getOrDefault(monthName, 0) + data.getAmount());
                                Log.d("ExpenseSum", "Month: " + monthName + " | Total Expense (Running): " + monthlyExpenses.get(monthName));
                            } catch (ParseException e) {
                                Log.e("DateParsing", "Failed to parse date: " + data.getDate(), e);
                            }
                        }

                        // Compare expenses to thresholds
                        for (String month : months) {
                            int monthlyThreshold = getThresholdForMonth(finalThresholds, month);
                            int totalExpense = monthlyExpenses.getOrDefault(month, 0);

                            boolean isWithinThreshold = monthlyThreshold == 0 || totalExpense <= monthlyThreshold;
                            monthStatus.put(month, isWithinThreshold);

                            Log.d("ThresholdCheck",
                                    "Month: " + month +
                                            " | Total Expense: " + totalExpense +
                                            " | Threshold: " + monthlyThreshold +
                                            " | Within Threshold: " + isWithinThreshold);
                        }

                        callback.onResult(monthStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("calculateMonthlyStatus", "Error: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("calculateMonthlyStatus", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchMonthlyExpenses(String userId, Callback<Map<String, Integer>> callback) {
        DatabaseReference expenseRef = mExpenseDatabase;

        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot expenseSnapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
                Map<String, Integer> monthlyExpenses = new HashMap<>();

                for (DataSnapshot snapshot : expenseSnapshot.getChildren()) {
                    Data data = snapshot.getValue(Data.class);
                    if (data == null || data.getDate() == null) continue;

                    try {
                        Date date = sdf.parse(data.getDate().toLowerCase());
                        if (date == null) continue;

                        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
                        String monthName = monthFormat.format(date).toLowerCase();

                        monthlyExpenses.put(monthName,
                                monthlyExpenses.getOrDefault(monthName, 0) + data.getAmount());
                    } catch (ParseException e) {
                        Log.e("DateParsing", "Failed to parse date: " + data.getDate(), e);
                    }
                }

                Log.d("MonthlyExpenses", "Monthly Expenses: " + monthlyExpenses.toString());
                callback.onResult(monthlyExpenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ExpenseFetch", "Error: " + databaseError.getMessage());
            }
        });
    }
    private void displayMonthlyExpensesGraph(Map<String, Integer> monthlyExpenses) {
        BarChart barChart = getView().findViewById(R.id.monthlyExpenseChart);

        // Define labels and their corresponding values
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < months.length; i++) {
            int expense = monthlyExpenses.getOrDefault(months[i], 0);
            entries.add(new BarEntry(i, expense));  // x = index, y = expense
        }

        // Create a BarDataSet
        BarDataSet barDataSet = new BarDataSet(entries, "Monthly Expenses");
        barDataSet.setColor(getResources().getColor(R.color.toolbar_color));

        // Create BarData and assign it to the chart
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.invalidate();

        // Customize the chart
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
    }

    // Callback Interface
    public interface Callback<T> {
        void onResult(T result);
    }
    private int getThresholdForMonth(SavingsThreshold thresholds, String month) {
        switch (month) {
            case "jan": return thresholds.getJan();
            case "feb": return thresholds.getFeb();
            case "mar": return thresholds.getMar();
            case "apr": return thresholds.getApr();
            case "may": return thresholds.getMay();
            case "jun": return thresholds.getJun();
            case "jul": return thresholds.getJul();
            case "aug": return thresholds.getAug();
            case "sep": return thresholds.getSep();
            case "oct": return thresholds.getOct();
            case "nov": return thresholds.getNov();
            case "dec": return thresholds.getDec();
            default: return 0;
        }
    }
    private void addMonthlyBoxes() {
        Log.d("DashboardFragment", "Adding monthly boxes");
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        LinearLayout container = getView().findViewById(R.id.monthly_boxes_container);

        if (container != null) {
            for (String month : months) {
                TextView box = new TextView(getContext());
                box.setText(month);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMarginEnd(8);
                box.setLayoutParams(layoutParams);
                box.setPadding(10, 10, 10, 10);
                box.setGravity(Gravity.CENTER);
                box.setBackgroundResource(R.drawable.month_box_background);

                container.addView(box);
                Log.d("DashboardFragment", "Added box for " + month);
            }
        }
    }
    private void updateMonthlyBoxColors(Map<String, Boolean> monthStatus) {
        LinearLayout container = getView().findViewById(R.id.monthly_boxes_container);

        // Get the current month index (January = 0, February = 1, etc.)
        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH);

        // Create a mapping from your month abbreviations to their indices.
        Map<String, Integer> monthIndices = new HashMap<>();
        monthIndices.put("jan", 0);
        monthIndices.put("feb", 1);
        monthIndices.put("mar", 2);
        monthIndices.put("apr", 3);
        monthIndices.put("may", 4);
        monthIndices.put("jun", 5);
        monthIndices.put("jul", 6);
        monthIndices.put("aug", 7);
        monthIndices.put("sep", 8);
        monthIndices.put("oct", 9);
        monthIndices.put("nov", 10);
        monthIndices.put("dec", 11);

        if (container != null) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof TextView) {
                    TextView box = (TextView) child;
                    String month = box.getText().toString().toLowerCase();  // e.g., "jan", "feb", etc.

                    Integer boxMonthIndex = monthIndices.get(month);
                    if (boxMonthIndex == null) {
                        Log.w("DashboardFragment", "Month not recognized: " + month);
                        continue;  // Skip if the month isn't recognized.
                    }

                    if (boxMonthIndex > currentMonthIndex) {
                        // This month is in the future: set grey background.
                        box.setBackgroundResource(R.drawable.month_box_future);
                    } else {
                        // For past or the current month, apply threshold logic.
                        boolean isWithinThreshold = Boolean.TRUE.equals(monthStatus.get(month));
                        if (isWithinThreshold) {
                            box.setBackgroundResource(R.drawable.month_box_success); // e.g., green.
                        } else {
                            box.setBackgroundResource(R.drawable.month_box_failure); // e.g., red.
                        }
                    }
                }
            }
        }
    }
}