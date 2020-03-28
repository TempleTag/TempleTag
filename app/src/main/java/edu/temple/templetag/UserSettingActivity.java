package edu.temple.templetag;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class UserSettingActivity extends AppCompatActivity {

    private String txt_username, txt_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        txt_username = getIntent().getExtras().getString("username");
        txt_email = getIntent().getExtras().getString("email");

        TextView textView = findViewById(R.id.username);
        textView.setText(txt_username);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.setting_container, new UserSettingActivity.SettingsFragment(txt_username, txt_email))
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private String txt_username, txt_email;

        public SettingsFragment(String txt_username, String txt_email){
            this.txt_email = txt_email;
            this.txt_username = txt_username;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference editTextPreference = findPreference(getString(R.string.email));
            editTextPreference.setText(txt_email);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}