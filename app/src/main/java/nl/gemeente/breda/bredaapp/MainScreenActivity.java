package nl.gemeente.breda.bredaapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.floatingactionmenu.FloatingActionButton;
import com.flask.floatingactionmenu.FloatingActionMenu;
import com.flask.floatingactionmenu.FloatingActionToggleButton;
import com.flask.floatingactionmenu.OnFloatingActionMenuSelectedListener;

import nl.gemeente.breda.bredaapp.adapter.MainScreenSectionsPagerAdapter;
import nl.gemeente.breda.bredaapp.adapter.ServiceAdapter;
import nl.gemeente.breda.bredaapp.api.ApiHomeScreen;
import nl.gemeente.breda.bredaapp.api.LocationApi;
import nl.gemeente.breda.bredaapp.businesslogic.ReportManager;
import nl.gemeente.breda.bredaapp.businesslogic.ServiceManager;
import nl.gemeente.breda.bredaapp.domain.Report;
import nl.gemeente.breda.bredaapp.domain.Service;
import nl.gemeente.breda.bredaapp.util.AlertCreator;

import static nl.gemeente.breda.bredaapp.UserSettingsActivity.PREFS_NAME;

public class MainScreenActivity extends AppBaseActivity implements ApiHomeScreen.Listener, ApiHomeScreen.NumberOfReports, AdapterView.OnItemSelectedListener, LocationApi.LocationListener {
	
	//================================================================================
	// Properties
	//================================================================================
	
	protected int reportRadius;
	private MainScreenSectionsPagerAdapter sectionsPagerAdapter;
	private ViewPager viewPager;
	private ReportManager reportManager;
	//private Button newReportActivityBtn;
	private ServiceAdapter spinnerAdapter;
	private Spinner homescreenDropdown;
	private int numberOfReports;
	private TextView loading;
	private ImageView overlay;
	private double latitude;
	private double longtitude;
	private Context context;
	private String serviceCode;
	private TabLayout tabs;
	
	private int backPressAmount = 0;
	
	private FloatingActionMenu floatingActionMenu;
	
	//================================================================================
	// Accessors
	//================================================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		Bundle bundle = new Bundle();
		bundle.putInt("menuID", R.id.nav_reports);
		super.setMenuSelected(bundle);

//		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//		setSupportActionBar(toolbar);
		sectionsPagerAdapter = new MainScreenSectionsPagerAdapter(getSupportFragmentManager(), getApplicationContext());
		
		latitude = 0;
		longtitude = 0;
		serviceCode = "0";
		
		viewPager = (ViewPager) findViewById(R.id.container);
		viewPager.setAdapter(sectionsPagerAdapter);
		
		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		
		//newReportActivityBtn = (Button) findViewById(R.id.mainScreenActivity_Btn_MakeReport);

//		newReportActivityBtn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent i = new Intent(getApplicationContext(), CreateNewReportActivity.class);
//				startActivity(i);
//			}
//		});
		
		context = getApplicationContext();
		
		getReports("0", 60.1892477, 24.9707467, 10000);
		getLocation();
		
		numberOfReports = -1;
		
		loading = (TextView) findViewById(R.id.activityMainscreen_tv_loading);
		overlay = (ImageView) findViewById(R.id.activityMainscreen_overlay_image);
		overlay.setVisibility(View.INVISIBLE);
		loading.setText(R.string.spinner_loading);
		
		homescreenDropdown = (Spinner) findViewById(R.id.homescreen_dropdown);
		
		spinnerAdapter = new ServiceAdapter(getApplicationContext(), ServiceManager.getServices(), R.layout.spinner_layout_adapter);
		homescreenDropdown.setAdapter(spinnerAdapter);
		homescreenDropdown.setOnItemSelectedListener(this);
		homescreenDropdown.setPrompt(getResources().getString(R.string.spinner_loading));
		
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		reportRadius = preferences.getInt("ReportRadius", 500);
		
		spinnerAdapter.notifyDataSetChanged();
		
		floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fam);
		floatingActionMenu.setOnFloatingActionMenuSelectedListener(new OnFloatingActionMenuSelectedListener() {
			@Override
			public void onFloatingActionMenuSelected(FloatingActionButton floatingActionButton) {
				if (floatingActionButton instanceof FloatingActionToggleButton) {
					FloatingActionToggleButton fatb = (FloatingActionToggleButton) floatingActionButton;
				} else if (floatingActionButton instanceof FloatingActionButton) {
					FloatingActionButton fab = (FloatingActionButton) floatingActionButton;
					String label = fab.getLabelText();
					if(label.equals(getResources().getString(R.string.fab_other))){
						MainScreenActivity.super.onMenuClick(CreateNewReportDifferentLocationActivity.class, -1, false);
					} else if(label.equals(getResources().getString(R.string.fab_location))){
						MainScreenActivity.super.onMenuClick(CreateNewReportActivity.class, -1, false);
					}
				}
			}
		});
	}
	
	public void getReports(String serviceCode, double latitude, double longtitude, int radius) {
		ReportManager.emptyArray();
		ApiHomeScreen apiHomeScreen = new ApiHomeScreen(this, this);
		String[] urls = new String[]{"https://asiointi.hel.fi/palautews/rest/v1/requests.json?status=open&service_code=" + serviceCode + "&lat=" + latitude + "&long=" + longtitude + "&radius=" + radius};
		apiHomeScreen.execute(urls);
	}
	
	public void getLocation() {
		LocationApi locationApi = new LocationApi(this);
		locationApi.setContext(context, this);
		locationApi.search();
	}
	
	@Override
	public void onReportAvailable(Report report) {
		//Log.i("Report", report.getDescription());
		ReportManager.addReport(report);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		ReportManager.emptyArray();
		sectionsPagerAdapter.removeMarkers();
		Service service = ServiceManager.getServices().get(position);
		serviceCode = service.getServiceCode();
		
		if (latitude == 0 || longtitude == 0) {
			getReports(serviceCode, 60.1892477, 24.9707467, 10000);
		} else {
			getReports(serviceCode, latitude, longtitude, reportRadius);
		}
		
		loading.setText(R.string.spinner_loading);
		overlay.setVisibility(View.VISIBLE);
		if (sectionsPagerAdapter.getMap() != null) {
			sectionsPagerAdapter.getMap().getUiSettings().setScrollGesturesEnabled(false);
		}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	@Override
	public void onNumberOfReportsAvailable(int number) {
		this.numberOfReports = number;
		if (number > 0) {
			if (sectionsPagerAdapter.getMap() != null) {
				sectionsPagerAdapter.getMap().getUiSettings().setScrollGesturesEnabled(true);
			}
			loading.setText("");
			overlay.setVisibility(View.INVISIBLE);
		} else if (number == 0) {
			if (sectionsPagerAdapter.getMap() != null) {
				sectionsPagerAdapter.getMap().getUiSettings().setScrollGesturesEnabled(false);
			}
			loading.setText(R.string.no_reports_found);
			overlay.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void noConnectionAvailable() {
		Toast toast = Toast.makeText(this, "No connection available.", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					finish();
					startActivity(getIntent());
				} else {
					AlertCreator alertCreator = new AlertCreator(MainScreenActivity.this);
					
					alertCreator.setTitle(R.string.no_location_permission_title);
					alertCreator.setMessage(R.string.no_location_permission_description);
					alertCreator.setNegativeButton(R.string.no_location_permission_negative, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
					alertCreator.setPositiveButton(R.string.no_location_permission_positive, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(MainScreenActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
						}
					});
					alertCreator.show();
				}
				return;
			}
		}
	}
	
	@Override
	public void onLocationAvailable(double latitude, double longtitude) {
		Log.i("LOCATION", latitude + ":" + longtitude);
		this.latitude = latitude;
		this.longtitude = longtitude;
		getReports(serviceCode, latitude, longtitude, reportRadius);
	}
	
	@Override
	public void onBackPressed() {
//		AlertCreator exit = new AlertCreator(MainScreenActivity.this);
//		exit.setIcon(R.mipmap.ic_launcher);
//		exit.setTitle(getResources().getString(R.string.appClose));
//		exit.setMessage(getResources().getString(R.string.appCloseMessage));
//		exit.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				System.exit(0);
//			}
//		});
//		exit.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				
//			}
//		});
//		exit.show();
		
		if (backPressAmount == 0) {
			Toast toast = Toast.makeText(MainScreenActivity.this, "Press again to exit.", Toast.LENGTH_LONG);
			toast.show();
			backPressAmount = 1;
			
			new CountDownTimer(2000, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					
				}
				
				@Override
				public void onFinish() {
					backPressAmount = 0;
				}
			}.start();
		} else if (backPressAmount == 1) {
			System.exit(0);
		}
	}
}