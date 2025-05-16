package uk.ac.hope.mcse.android.coursework;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import uk.ac.hope.mcse.android.coursework.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        
        // Load and apply saved theme
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Add destination changed listener to handle toolbar titles
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.FirstFragment) {
                binding.toolbar.setTitle("Home 🏠");
            } else if (destination.getId() == R.id.SecondFragment) {
                binding.toolbar.setTitle("Create Event \uD83D\uDDD3\uFE0F");
            } else if (destination.getId() == R.id.SettingsFragment) {
                binding.toolbar.setTitle("Settings ⚙️");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if theme needs to be updated
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        
        if (currentMode != newMode) {
            AppCompatDelegate.setDefaultNightMode(newMode);
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            navController.navigate(R.id.SettingsFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}