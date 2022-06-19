package edu.ucsd.cse110.zooseeker.data.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import edu.ucsd.cse110.zooseeker.R;
import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.graph.ZooNavigation;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;
import edu.ucsd.cse110.zooseeker.util.Constants;

public class EmptyActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty);
	}
}