package de.vertedge.ssiwallet;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import de.vertedge.ssiwallet.data.SSI.SSI_Database;
import de.vertedge.ssiwallet.databinding.ActivityMainBinding;
import de.vertedge.ssiwallet.ui.Authorities.FragmentAuthorities;
import de.vertedge.ssiwallet.ui.VCs.FragmentVCs;
import de.vertedge.ssiwallet.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    String pin;

    private SSI_Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
        } catch (Exception e) {
            Log.e("MainActivity.onCreate inflation", "onCreateView", e);
            throw e;
        }
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, R.string.nope_new, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_ids, R.id.nav_attributes, R.id.nav_authorities)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // TODO PIN Eingabe öffnen
        // Intent intent = new Intent(this, LoginActivity.class);
        // startActivity(intent);

        // get data from database
        db = SSI_Database.getInstance(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                finish();
                return true;
            case R.id.nav_help:
                Log.d("AAAAAA", item.getTitle().toString());
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public SSI_Database getDb() {
        return db;
    }

}