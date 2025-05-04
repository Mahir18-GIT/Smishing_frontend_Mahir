package com.example.smishingdetectionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smishingdetectionapp.news.Models.RSSFeedModel;
import com.example.smishingdetectionapp.news.NewsAdapter;
import com.example.smishingdetectionapp.news.NewsRequestManager;
import com.example.smishingdetectionapp.news.OnFetchDataListener;
import com.example.smishingdetectionapp.news.SelectListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class NewsActivity extends SharedActivity implements SelectListener {

    RecyclerView recyclerView;
    NewsAdapter adapter;
    NewsRequestManager manager;
    ProgressBar progressBar;
    TextView errorMessage, alertText;
    CardView alertBanner;
    Button refreshButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // UI Elements
        errorMessage = findViewById(R.id.errorTextView);
        recyclerView = findViewById(R.id.news_recycler_view);
        refreshButton = findViewById(R.id.refreshButton);
        progressBar = findViewById(R.id.progressBar);
        alertBanner = findViewById(R.id.alert_banner);
        alertText = findViewById(R.id.alert_text);

        // Bottom Navigation
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_news);
        nav.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_news) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // Fetch News Data
        loadData();

        // Refresh Button
        refreshButton.setOnClickListener(v -> {
            if (isNetworkConnected()) {
                loadData();
            } else {
                Toast.makeText(this, "You Have Lost Network Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Connectivity Check
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }
        return false;
    }

    // Load RSS Feed
    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        alertBanner.setVisibility(View.GONE);

        manager = new NewsRequestManager(this);
        manager.fetchRSSFeed(new OnFetchDataListener<RSSFeedModel.Feed>() {
            @Override
            public void onFetchData(List<RSSFeedModel.Article> list, String message) {
                showNews(list);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                errorMessage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            // Show news in RecyclerView and show alert if scam detected
            private void showNews(List<RSSFeedModel.Article> list) {
                adapter = new NewsAdapter(list, NewsActivity.this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(NewsActivity.this, 1));
                recyclerView.setAdapter(adapter);

                // 🚨 Dynamic Scam Detection Alert
                boolean hasScam = false;
                for (RSSFeedModel.Article article : list) {
                    String title = article.title.toLowerCase();
                    if (title.contains("scam") || title.contains("phishing") || title.contains("fraud")) {
                        hasScam = true;
                        break;
                    }
                }

                if (hasScam) {
                    alertBanner.setVisibility(View.VISIBLE);
                    alertText.setText("⚠️ Scam Alert: Check out the latest scam reports below.");

                    // Optional: auto-hide after 6 seconds
                    alertBanner.postDelayed(() -> alertBanner.setVisibility(View.GONE), 6000);
                }
            }
        });
    }

    // Handle News Click
    @Override
    public void OnNewsClicked(RSSFeedModel.Article article) {
        if (article != null && article.link != null && !article.link.isEmpty()) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.link));
                startActivity(browserIntent);
            } catch (Exception e) {
                Log.e("NewsActivity", "Error opening URL", e);
                Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No URL available", Toast.LENGTH_SHORT).show();
        }
    }
}
