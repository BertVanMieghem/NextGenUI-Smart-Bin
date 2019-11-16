package com.example.shoppinglist5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.*;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String currentlySelectedList;
    String item;
    EditText productInput;
    Button addButton;
    ListView listView;
    HashMap<String, List<String>> items = new HashMap<>();
    HashMap<String, JSONObject> itemDetails = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference of widgets from XML layout
        productInput = findViewById(R.id.editText);
        addButton = findViewById(R.id.button);
        listView = findViewById(R.id.shopping_list);
        items.put("Shopping list 1", new ArrayList<String>());
        currentlySelectedList = getFirstListName();
        setTitle(currentlySelectedList);

        updateListView();
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
            case R.id.menuFetch:
                System.out.println("Clicked 'Fetch data'");
                fetchFromServer();

        }
        return true;
    }

    private void updateListView() {
        // Create an ArrayAdapter from List
        List<String> ls = (items.get(currentlySelectedList) == null) ? new ArrayList<String>() : new ArrayList<>(items.get(currentlySelectedList));
        System.out.println("~!~ ls: " + ls);
        System.out.println("~!~ items: " + items);
        for (int i = 0; i < ls.size(); i++) {
            try {
                Integer q = (Integer) itemDetails.get(ls.get(i)).get("size");
                if (q > 1) ls.set(i, q.toString() + " " + ls.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ls);
        listView.setAdapter(adapter);
        setTitle(currentlySelectedList);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = productInput.getText().toString();
                addItemToList(item, "Manual entry");
                adapter.notifyDataSetChanged();
                productInput.setText("");
                System.out.println("updateListView [][][] items: " + items.toString());
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                return showPopup(view, position);
            }
        });
    }

    private boolean showPopup(View view, final int position) {
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.details:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        String product = items.get(currentlySelectedList).get(position);
                        builder.setTitle(product);
                        try {
                            builder.setMessage(buildItemDetailsString(product));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                        break;
                    case R.id.delete:
                        List<String> ls = items.get(currentlySelectedList);
                        String toRemove = ls.get(position);
                        ls.remove(toRemove);
                        items.put(currentlySelectedList, ls);
                        updateListView();
                        break;
                    case R.id.quantity:
                        showNumberPicker(position);
                    default:
                        break;
                }
                return true;
            }
        });
        popup.inflate(R.menu.popup);
        popup.show();
        return false;
    }

    private String buildItemDetailsString(String product) throws JSONException {
        JSONObject ji = itemDetails.get(product);
        return "Date added: " + ji.get("date_added") + "\nOrigin: " + ji.get("origin") + "\n";
    }

    private void showNumberPicker(final Integer item) { // source: https://www.zoftino.com/android-numberpicker-dialog-example

        NumberPickerDialog np = new NumberPickerDialog();
        np.setValueChangeListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setQuantity(item, picker.getValue());
            }
        });
        np.show(getSupportFragmentManager(), "Quantity picker");
    }

    private void setQuantity(Integer idx, Integer q) {
        String item = items.get(currentlySelectedList).get(idx);
        JSONObject details = itemDetails.get(item);
        try {
            details.put("size", q);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateListView();
    }

    private void addItemToList(String item, String origin) {
        List<String> ls = items.get(currentlySelectedList);
        try {
            if ((ls != null) && ls.contains(item)) {
                setQuantity(ls.indexOf(item), (Integer) itemDetails.get(item).get("size") + 1); // buggy when same item is on different lists
            } else {
                ls.add(item);
                items.put(currentlySelectedList, ls);
                JSONObject sd = new JSONObject();
                JSONObject td = new JSONObject();
                JSONObject od = new JSONObject();
                sd.put("size", 1);
                td.put("date_added", getTime());
                td.put("origin", origin);
                itemDetails.put(item, sd);
                itemDetails.put(item, td);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateListView();
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
                updateListView();
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
                    updateListView();
                } else {
                    items.remove(currentlySelectedList);
                    createNewList("This was your only list, create a new one?");
                }
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
                updateListView();
            }
        });
        builder.show();
    }


    private String getFirstListName() {
        return (String) items.keySet().toArray()[0];
    }

    private String getTime() {
        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }


    private void fetchFromServer() {
        new JsonTask().execute("https://jsonplaceholder.typicode.com/users"); // TODO
    }

    private void parseJsonResult(String res) {
        try {
            JSONArray arr = new JSONArray(res);
            for (int i = 0; i < arr.length(); i++) {
                String item = arr.getJSONObject(i).get("username").toString();
                addItemToList(item, "Fetched from server");
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }



    private class JsonTask extends AsyncTask<String, String, String> { // source: https://stackoverflow.com/questions/33229869/get-json-data-from-url-using-android

        ProgressDialog pd;
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            parseJsonResult(result);
        }
    }
}
