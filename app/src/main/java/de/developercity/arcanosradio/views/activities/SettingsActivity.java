package de.developercity.arcanosradio.views.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.developercity.arcanosradio.ArcanosRadioApplication;
import de.developercity.arcanosradio.R;
import de.developercity.arcanosradio.models.UserPreferences;

public class SettingsActivity extends AppCompatActivity {
    private UserPreferences userPreferences;

    @BindView(R.id.layout_root)
    CoordinatorLayout layout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.check_keep_screen_on)
    CheckBox screenOnCheckBox;

    @BindView(R.id.check_mobile_data)
    CheckBox mobileDataCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLayout();
        fetchData();

        setListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpLayout() {
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setListeners() {
        screenOnCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setKeepScreenOnEnabled(isChecked);
                storeData();
            }
        });

        mobileDataCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userPreferences.setMobileDataStreamingEnabled(isChecked);
                storeData();
            }
        });
    }

    private void fetchData() {
        userPreferences = ArcanosRadioApplication.getStorage().getUserPreferences();

        mobileDataCheckBox.setChecked(userPreferences.getMobileDataStreamingEnabled());
        screenOnCheckBox.setChecked(userPreferences.getKeepScreenOnEnabled());
    }

    private void storeData() {
        if (ArcanosRadioApplication.getStorage().setUserPreferences(userPreferences) != null) {
            Snackbar.make(layout, getString(R.string.settings_msg_save), Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(layout, getString(R.string.generic_msg_error), Snackbar.LENGTH_LONG).show();
        }
    }
}