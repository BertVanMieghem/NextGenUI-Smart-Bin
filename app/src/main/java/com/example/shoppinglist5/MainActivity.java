package com.example.shoppinglist5;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String item;
    EditText productInput;
    Button addButton;
    Toolbar tlb;

    // Initializing a new String Array
    String[] items = new String[] {
            "Beer",
            "Milk"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference of widgets from XML layout
        productInput = (EditText) findViewById(R.id.editText);
        addButton = (Button) findViewById(R.id.button);
        ListView listView = (ListView) findViewById(R.id.shopping_list);


        // Create a List from String Array elements
        final List<String> shoppingList = new ArrayList<>(Arrays.asList(items));

        // Create an ArrayAdapter from List
        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingList);
        listView.setAdapter(adapter);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = productInput.getText().toString();
                System.out.println("item: " + item);
                shoppingList.add(item);
                adapter.notifyDataSetChanged();
                productInput.setText("");
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.actionbar1, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menuClearList:
                System.out.println("Clicked 'Clear List'");
                break;
            case R.id.menuShareList:
                System.out.println("Clicked 'Share List'");
                break;
        }
        return true;
    }

}
