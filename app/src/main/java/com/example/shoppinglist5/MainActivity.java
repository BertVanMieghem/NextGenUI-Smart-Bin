package com.example.shoppinglist5;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.lang.Integer.parseInt;

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
        items.put("Shopping list", new ArrayList<String>());
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

    // called every time the shopping list is updated
    private void updateListView() {
        // Create an ArrayAdapter from List
        List<String> ls = (items.get(currentlySelectedList) == null) ? new ArrayList<String>() : new ArrayList<>(items.get(currentlySelectedList));
        for (int i = 0; i < ls.size(); i++) {
            try {
                String qString = (String) itemDetails.get(ls.get(i)).get("amount");
                Integer q = parseInt(qString);
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
                addItemToList(item);
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

    // Hold an item to show pop up menu with options: details, delete, and set quantity
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

    // create json with all item details
    private String buildItemDetailsString(String product) throws JSONException {
        JSONObject ji = itemDetails.get(product);
        if (ji.get("origin") == "Manual entry")
            return "Date added: " + ji.get("date_added") + "\n" + "Origin: " + ji.get("origin") + "\n";
        if (ji.get("origin") == "Fetched from server")
            return "Date added: " + ji.get("date_added") + "\n" +
                    "Origin: " + ji.get("origin") + "\n" +
                    "Barcode: " + ji.get("barcode") + "\n" +
                    "Description: " + ji.get("description") + "\n" +
                    "Manufacturer: " + ji.get("manufacturer") + "\n" +
                    "Size: " + ji.get("size") + "\n" +
                    "Brand: " + ji.get("brand") + "\n" +
                    "Status: " + ji.get("status");
        return "";
    }

    // display the menu to set a quantity for a specific item
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

    // modify the quantity of a specific item
    private void setQuantity(Integer idx, Integer q) {
        String item = items.get(currentlySelectedList).get(idx);
        JSONObject details = itemDetails.get(item);
        try {
            details.put("amount", q.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateListView();
    }

    // add an item that was received from the server to the shopping list
    private void addItemToList(String item, String barcode, String description, String manufacturer, String image, String size, String brand, String status,String amount) {
        List<String> ls = items.get(currentlySelectedList);
        try {
            if ((ls != null) && ls.contains(item)) {
                setQuantity(ls.indexOf(item), parseInt((String) itemDetails.get(item).get("amount")) + parseInt(amount)); // buggy when same item is on different lists
            } else {
                ls.add(item);
                items.put(currentlySelectedList, ls);
                JSONObject ed = new JSONObject();
                ed.put("barcode", barcode);
                ed.put("date_added", getTime());
                ed.put("origin", "Fetched from server");
                ed.put("description", description);
                ed.put("manufacturer", manufacturer);
                ed.put("image", image);
                ed.put("size", size);
                ed.put("brand", brand);
                ed.put("status", status);
                ed.put("amount", amount);
                itemDetails.put(item, ed);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateListView();
    }

    // add an item that was manually added to the shopping list
    private void addItemToList(String item) {
        List<String> ls = items.get(currentlySelectedList);
        try {
            if ((ls != null) && ls.contains(item)) {
                setQuantity(ls.indexOf(item), parseInt((String) itemDetails.get(item).get("amount")) + 1); // buggy when same item is on different lists
            } else {
                ls.add(item);
                items.put(currentlySelectedList, ls);
                JSONObject ed = new JSONObject();
                ed.put("date_added", getTime());
                ed.put("origin", "Manual entry");
                ed.put("amount", "1");
                itemDetails.put(item, ed);
                JSONObject pd = buildJSONforPost(item);
                postToServer(pd.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateListView();
    }

    // for a manually added product, create a json with information that the server expects
    private JSONObject buildJSONforPost(String name) {
        try {
            JSONObject pd = new JSONObject();
            pd.put("barcode", "1");
            pd.put("name", name);
            pd.put("description", "manual entry");
            pd.put("amount", 1);
            System.out.println(pd);
            return pd;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // create new shopping list and add to hashmap
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

    // remove lsit from hashmap
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

    // rename an entry in the hashmap
    private void renameShoppingList() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a new name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

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

    // from the overview of shopping lists, display the selected shopping list
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


    // when the application is started, this function is used to set current shopping list to the first one in the hashmap
    private String getFirstListName() {
        return (String) items.keySet().toArray()[0];
    }

    private String getTime() {
        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }


    private void fetchFromServer() {
        new JsonTask().execute("http://52.91.230.14:3000/fetch");
    }
    private void postToServer(String params) { new CallAPI().execute("http://52.91.230.14:3000/create", params); }


    // parse json fetched from server
    private void parseJsonResult(String res) {
        if (res != null) {
            try {
                JSONArray arr = new JSONArray(res);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String item = o.get("name").toString();
                    String barcode = o.get("barcode").toString();
                    String description = o.get("description").toString();
                    String manufacturer = o.get("manufacturer").toString();
                    String image = o.get("image").toString();
                    String size = o.get("size").toString();
                    String brand = o.get("brand").toString();
                    String status = o.get("status").toString();
                    String amount = o.get("amount").toString();
                    addItemToList(item, barcode, description, manufacturer, image, size, brand, status, amount);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else Toast.makeText(getApplicationContext(),"Data fetch failed, please try again.", Toast.LENGTH_SHORT).show();
    }

    // post json to the server
    public class CallAPI extends AsyncTask<String, String, String> { // source: https://stackoverflow.com/questions/42767249/android-post-request-with-json

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String data = params[1]; //data to post
            OutputStream out = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                os.writeBytes(data);
                System.out.println("wrote to server: " + data);

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(urlConnection.getResponseCode()));
                Log.i("MSG" , urlConnection.getResponseMessage());

                urlConnection.disconnect();
            } catch (Exception e) {
                System.out.println("WRITE-TO-SERVER ERROR: " + e.getMessage());
            }
            return null;
        }
    }


    // fetch json from server
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
