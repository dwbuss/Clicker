package com.example.clicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      switch(item.getItemId()) {
          case R.id.settings:
              Intent settings = new Intent(this, SettingsActivity.class);
              startActivity(settings);
              return true;
          case R.id.about:
              Intent about = new Intent(this, AboutActivity.class);
              startActivity(about);
              return true;
          default:
              return super.onOptionsItemSelected(item);
      }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clicker_menu, menu);
        return true;
    }

    public void addCount(View view){
        bump(R.id.total);
        bump(R.id.weekly);
        bump(R.id.daily);
    }

    public void bump(int id){
        TextView totalView = (TextView) findViewById(id);
        Integer total = Integer.parseInt(totalView.getText().toString());
        total++;
        totalView.setText(total.toString());
    }
}
