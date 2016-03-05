package com.ashwin.sadhanasms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginFragmentInteractionListener, MainActivityFragment.OnMainActivityFragmentInteractionListener {

    private static final String BUNDLE_KEY = "BUNDLE_KEY";
    DbHelper db;
    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        db = new DbHelper(this);

        if (savedInstanceState == null) {

            if (db.isNull()) {
                LoginFragment loginFragment = new LoginFragment();

                args = new Bundle();
                args.putBoolean(BUNDLE_KEY, true);

                loginFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, loginFragment).commit();
            } else {

                MainActivityFragment mainActivityFragment = new MainActivityFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mainActivityFragment).commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            LoginFragment loginFragment = new LoginFragment();

            args = new Bundle();
            args.putBoolean(BUNDLE_KEY, false);

            loginFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, loginFragment).commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginFragmentInteraction() {

        MainActivityFragment mainActivityFragment = new MainActivityFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mainActivityFragment).commit();

    }

    @Override
    public void onMainActivityFragmentInteraction() {
        LoginFragment loginFragment = new LoginFragment();

        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_KEY, true);

        loginFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, loginFragment).commit();
    }
}
