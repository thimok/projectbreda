package nl.gemeente.breda.bredaapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TermsAndConditionsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terms_and_conditions);
		
		Button continueButton = (Button) findViewById(R.id.activityTermsAndConditions_bt_continue);
		
		continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				returnIntent.putExtra("accepted", true);
				setResult(Activity.RESULT_OK, returnIntent);
				finish();
			}
		});
	}
}
