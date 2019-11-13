package com.example.shoppinglist5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.*;
import org.json.JSONException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String currentlySelectedList;
    String item;
    EditText productInput;
    Button addButton;
    HashMap<String, List<String>> items = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items.put("Shopping list 1", new ArrayList<String>());
        currentlySelectedList = getFirstListName();

        showList();
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
            case R.id.menuRemoveList:
                System.out.println("Clicked 'Remove List'");
                removeList();
                break;
            case R.id.menuShareList:
                System.out.println("Clicked 'Share List'");
                break;
            case R.id.menuNewList:
                System.out.println("Clicked 'New list'");
                createNewList("Give your new list a name");
                break;
            case R.id.menuRename:
                System.out.println("Clicked 'Rename list'");
                renameShoppingList();
                break;
            case R.id.menuViewLists:
                System.out.println("Clicked 'View my lists'");
                selectList();
                break;

        }
        return true;
    }

    private void createNewList(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newListName = input.getText().toString();
                items.put(newListName, new ArrayList<String>());
                currentlySelectedList = newListName;
                System.out.println("createNewList [][][] items: " + items.toString());
                showList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void removeList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + currentlySelectedList + "?");
        builder.setMessage("entries in this list: " + items.get(currentlySelectedList).size());

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items.size() > 1) {
                    items.remove(currentlySelectedList);
                    currentlySelectedList = getFirstListName();
                    showList();
                } else {
//                    Toast.makeText(getApplicationContext(), "You only have one list! You can't remove your only list", Toast.LENGTH_LONG).show();
                    items.remove(currentlySelectedList);
                    createNewList("This was your only list, create a new one?");
                }
                System.out.println("removeList [][][] items: " + items.toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void renameShoppingList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a new name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                items.put(newName, items.get(currentlySelectedList));
                items.remove(currentlySelectedList);
                currentlySelectedList = newName;
                setTitle(newName);
                System.out.println("renameShoppingList [][][] items: " + items.toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void selectList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a shopping list");
        CharSequence[] listNamesCharSeq = items.keySet().toArray(new CharSequence[items.size()]);
        builder.setItems(listNamesCharSeq, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentlySelectedList = (String) items.keySet().toArray()[which];
                setTitle(currentlySelectedList);
                showList();
                System.out.println("selectList [][][] items: " + items.toString());
            }
        });
        builder.show();
    }

    private void showList() {
        // Get reference of widgets from XML layout
        productInput = findViewById(R.id.editText);
        addButton = findViewById(R.id.button);
        ListView listView = findViewById(R.id.shopping_list);
        setTitle(currentlySelectedList);


        // Create an ArrayAdapter from List
        List<String> ls = (items.get(currentlySelectedList) == null) ? new ArrayList<String>() : items.get(currentlySelectedList);
        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ls);
        listView.setAdapter(adapter);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = productInput.getText().toString();
                List<String> ls = (items.get(currentlySelectedList) == null) ? new ArrayList<String>() : items.get(currentlySelectedList);
                ls.add(item);
                items.put(currentlySelectedList, ls);
                adapter.notifyDataSetChanged();
                productInput.setText("");
                showList();
                System.out.println("showList [][][] items: " + items.toString());
            }
        });
    }

    private String getFirstListName() {
        return (String) items.keySet().toArray()[0];
    }
}
