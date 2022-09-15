package com.example.fetchrewards.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fetchrewards.ApiInterface;
import com.example.fetchrewards.R;
import com.example.fetchrewards.adapter.RewardsAdapter;
import com.example.fetchrewards.model.Rewards;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DataActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RewardsAdapter rewardsAdapter;
    private List<Rewards> searchArrayList = new ArrayList<>();
    private List<Integer> groupListData = new ArrayList<>();
    private List<List<Rewards>> mainList = new ArrayList<>();
    Spinner mySpinner;
    String[] categories = {"Filter data", "Empty data", "Null data"};
    LinearLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        init();
    }

    public void init() {
        parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getRewards();
        initializeViews();
    }

    private void initializeViews() {

        mySpinner = findViewById(R.id.mySpinner);
        mySpinner.setAdapter(new ArrayAdapter<>(this, R.layout.simple_list_item, categories));

    }

    private void getRewards() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fetch-hiring.s3.amazonaws.com")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        // below line is to create an instance for our retrofit api class.
        ApiInterface retrofitAPI = retrofit.create(ApiInterface.class);
        Call<String> call = retrofitAPI.getRewards();

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {

                    if (response.body() != null) {
                        Log.e("Response Data", "" + response.body());

                        try {
                            JSONArray jsonArray = new JSONArray(response.body());

                            for (int i = 0; i < jsonArray.length(); i++) {
                                Rewards rewards = new Rewards();
                                JSONObject jsonObjectDataList = jsonArray.getJSONObject(i);

                                rewards.setId(jsonObjectDataList.getInt("id"));
                                rewards.setLid(jsonObjectDataList.getInt("listId"));
                                if (jsonObjectDataList.getString("name") != null) {
                                    rewards.setName(jsonObjectDataList.getString("name"));
                                }

                                searchArrayList.add(rewards);
                                Log.e("Rewards Data", "" + searchArrayList.toString());
                                Collections.sort(searchArrayList, new Comparator<Rewards>() {
                                    @Override
                                    public int compare(Rewards p1, Rewards p2) {
                                        return p2.getName().compareTo(p1.getName()); // Descending
                                    }
                                });

                                if (!groupListData.contains(rewards.getLid())) {
                                    groupListData.add(rewards.getLid());
                                    Collections.sort(groupListData);
                                }
                            }

                            for (int i = 0; i < groupListData.size(); i++) {

                                ArrayList<Rewards> data = new ArrayList<>();
                                for (int j = 0; j < searchArrayList.size(); j++) {

                                    if (searchArrayList.get(j).getLid() == groupListData.get(i) && !searchArrayList.get(j).getName().equals("")
                                            && !searchArrayList.get(j).getName().equals("null") && searchArrayList.get(j).getName() != null) {
                                        data.add(searchArrayList.get(j));
                                    }
                                }
                                mainList.add(data);

                            }

                            rewardsAdapter = new RewardsAdapter(mainList);
                            recyclerView.setAdapter(rewardsAdapter);
                            Log.e("uniqueListData", "getJsonFileFromLocally: " + groupListData.size());

                            rewardsAdapter.notifyDataSetChanged();

                            mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long itemID) {
                                    if (position >= 0 && position < categories.length) {
                                        getSelectedCategoryData(position);
                                        Log.e("ss", "onItemSelected: " + searchArrayList.get(position).getName());
                                    } else {
                                        Toast.makeText(DataActivity.this, "Selected Category Does not Exist!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // displaying an error message in toast
                Toast.makeText(DataActivity.this, "Fail to get the data..", Toast.LENGTH_SHORT).show();
            }
        });
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(parentLayout, "Please click BACK again to exit", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finishAffinity();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void getSelectedCategoryData(int categoryID) {
        Log.e("ss1", "getSelectedCategoryData: " + categoryID);
        List<List<Rewards>> FilterArrayList = new ArrayList<>();

        //filter out Items whose name does not have null or empty values
        if (categoryID == 0) {
            FilterArrayList.clear();
            for (int i = 0; i < groupListData.size(); i++) {
                ArrayList<Rewards> rewards = new ArrayList<>();
                for (int j = 0; j < searchArrayList.size(); j++) {
                    String str = searchArrayList.get(j).getName();
                    Log.e("data", "getSelectedCategoryData: " + str);
                    if (searchArrayList.get(j).getLid() == groupListData.get(i) && !searchArrayList.get(j).getName().equals("") && !searchArrayList.get(j).getName().equals("null") && searchArrayList.get(j).getName() != null) {
                        rewards.add(searchArrayList.get(j));
                    }
                }
                Log.e("ss", "size1: " + rewards.size());
                FilterArrayList.add(rewards);
            }

            rewardsAdapter.setdata(FilterArrayList);

            //filter out Id whose name has empty values only
        } else if (categoryID == 1) {
            FilterArrayList.clear();
            for (int i = 0; i < groupListData.size(); i++) {
                ArrayList<Rewards> rewards = new ArrayList<>();
                for (int j = 0; j < searchArrayList.size(); j++) {
                    if (searchArrayList.get(j).getLid() == groupListData.get(i) && searchArrayList.get(j).getName().equals("")) {
                        rewards.add(searchArrayList.get(j));
                    }
                }
                Log.e("ss", "size: " + rewards.size());
                FilterArrayList.add(rewards);
            }
            rewardsAdapter.setdata(FilterArrayList);

            //filter out Id whose name has null values only
        } else if (categoryID == 2) {
            FilterArrayList.clear();
            for (int i = 0; i < groupListData.size(); i++) {
                ArrayList<Rewards> rewards = new ArrayList<>();
                for (int j = 0; j < searchArrayList.size(); j++) {
                    if (searchArrayList.get(j).getLid() == groupListData.get(i) && (searchArrayList.get(j).getName() == null || searchArrayList.get(j).getName().equals("null"))) {
                        rewards.add(searchArrayList.get(j));
                    }
                }
                Log.e("ss", "size: " + rewards.size());
                FilterArrayList.add(rewards);
            }
            rewardsAdapter.setdata(FilterArrayList);
        }
    }
}