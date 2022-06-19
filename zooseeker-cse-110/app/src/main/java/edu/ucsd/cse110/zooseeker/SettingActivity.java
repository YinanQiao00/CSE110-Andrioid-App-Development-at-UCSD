package edu.ucsd.cse110.zooseeker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import edu.ucsd.cse110.zooseeker.util.Constants;
import edu.ucsd.cse110.zooseeker.util.SettingsUtil;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public String instructionType = Constants.DIRECTIONS_BRIEF;

    ImageButton btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        instructionType = SettingsUtil.get(this, Constants.INSTRUCTION_TYPE, Constants.DIRECTIONS_BRIEF);

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.spinner);
        dropdown.setOnItemSelectedListener(this);
        //create a list of items for the spinner.
        String[] items = new String[]{"Brief Directions", "Detailed Directions"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setSelection(instructionType.equals(Constants.DIRECTIONS_BRIEF) ? 0 : 1);

        btn_back = (ImageButton)findViewById(R.id.settings_back_button);
        btn_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        String sel = (String) adapterView.getItemAtPosition(pos);
        if(sel.equals("Brief Directions")) {
            instructionType = Constants.DIRECTIONS_BRIEF;
            Log.v("BRIEF DIRECTIONS =========", "yes");
        }
        else if (sel.equals("Detailed Directions")) {
            instructionType = Constants.DIRECTIONS_DETAILED;
            Log.v("DETAILED DIRECTIONS =========", "yes");
        }
        SettingsUtil.set(this, Constants.INSTRUCTION_TYPE, instructionType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}