package nl.gemeente.breda.bredaapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import nl.gemeente.breda.bredaapp.adapter.FavoriteReportsAdapter;
import nl.gemeente.breda.bredaapp.api.ApiHomeScreen;
import nl.gemeente.breda.bredaapp.businesslogic.FavoriteReportManager;
import nl.gemeente.breda.bredaapp.domain.Report;
import nl.gemeente.breda.bredaapp.util.DatabaseHandler;

import static nl.gemeente.breda.bredaapp.fragment.MainScreenListFragment.EXTRA_REPORT;

public class FavoriteReportsActivity extends AppBaseActivity implements ApiHomeScreen.Listener, ApiHomeScreen.NumberOfReports, AdapterView.OnItemClickListener {
	
	public static final String TAG = "FavoriteReportsActivity";
	private ListView favoriteReportsListView;
	private FavoriteReportsAdapter favoriteReportsAdapter;
	private ArrayList<Report> favoritereports = new ArrayList<Report>();
	private String serviceRequestId;
	private int numberOfReports;
	private Report report;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_reports);
		super.setMenuSelected(getIntent().getExtras());
		
		final DatabaseHandler dbh = new DatabaseHandler(getApplicationContext(), null, null, 1);
		numberOfReports = -1;
		favoriteReportsListView = (ListView) findViewById(R.id.favoritescreen_lv);
		
		favoritereports = (ArrayList<Report>) dbh.getAllReports();
		
		Log.i(TAG, "We hebben " + favoritereports.size() + " favorites");
		
		for (Report report : favoritereports) {
			serviceRequestId = report.getServiceRequestId();
			Log.i(TAG, "ID = " + report.getServiceRequestId());
			
//			favoriteReportsAdapter = new FavoriteReportsAdapter(getApplicationContext(), FavoriteReportManager.getFavoriteReports());
			favoriteReportsAdapter = new FavoriteReportsAdapter(getApplicationContext(), FavoriteReportManager.getFavoriteReports());
			favoriteReportsListView.setAdapter(favoriteReportsAdapter);
			
			getFavoriteReports(serviceRequestId);
			favoriteReportsListView.setOnItemClickListener(this);
		}
	}
	
	public void getFavoriteReports(String serviceRequestId) {
		FavoriteReportManager.emptyArray();
		ApiHomeScreen apiHomeScreen = new ApiHomeScreen(this, this);
		String[] urls = new String[]{getString(R.string.api_address) + "requests.format?service_request_id=" + serviceRequestId};
		apiHomeScreen.execute(urls);
	}
		
	@Override
	public void onReportAvailable(Report report) {
		FavoriteReportManager.addReport(report);
		favoriteReportsAdapter.notifyDataSetChanged();
	}

	@Override
	public void noConnectionAvailable() {
		Toast toast = Toast.makeText(this, "No connection available.", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	@Override
	public void onNumberOfReportsAvailable(int number) {
		this.numberOfReports = number;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.i(TAG, "Favorite report " + position + " is geselecteerd");

		Report r = FavoriteReportManager.getFavoriteReports().get(position);
		Intent detailedReportIntent = new Intent(getApplicationContext(), DetailedReportActivity.class);
		detailedReportIntent.putExtra("MediaUrl", r.getMediaUrl());
		detailedReportIntent.putExtra(EXTRA_REPORT, r);
		startActivity(detailedReportIntent);
	}
}
