package rsweny.quicklist.com.quicklist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class MainActivity extends AppCompatActivity {

    Database myDb;

    private TextView itemDeletionTextView;
    private float x1, x2;
    private int CurrentBtn;
    private int itemCounter = 0;
    private int initialStartingButtonPosition;
    private int initialStartingButtonPositionY;
    private int xDelta, yDelta;
    private Boolean dialogTriggered = false;
    private int userItemsCount;
    private LinearLayout linearLayout;
    private LinearLayout scrollerlinearlayout;
    private Spinner spinner;
    private String EditTextString;
    private ScrollView scrollViewMainScreen;
    private RelativeLayout relativeLayout;
    private Button btn;
    private int finalI;
    private float halfW;


    // items for database
    private String item_name;
    private int notification_time;
    final String PREFS_NAME = "MyPrefsFile";
    private Animation animationHolder;

    ArrayList<String> userItems = new ArrayList<>();
    ArrayList<Integer> userNotifications = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize database
        myDb = new Database(this);

        scrollerlinearlayout = findViewById(R.id.scrollerlinearlayout);
        linearLayout = findViewById(R.id.linearLayout);
        itemDeletionTextView = findViewById(R.id.itemDeletionTextView);
        spinner = findViewById(R.id.spinner);

        // Adding shared preferences to avoid users getting "Test Item" each time
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // Check if users first time using application
        if (settings.getBoolean("first_time_user", true)) {
            Log.i("Comments", "First time");

            // first time task
            userItems.add("Test Item");
            userNotifications.add(0);

            AddData(userItems.get(0) , userNotifications.get(0));
            updateItems();

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("first_time_user", false).apply();
        } else {
            viewAllItems();
            viewAllNotifications();
            updateItems();
        } // End if

        // On floating action button click
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create custom dialog box to input new items
                ItemEntry itemAdded = new ItemEntry();
                itemAdded.showItemEntryDialog(MainActivity.this);

            }

        });
    }  // End onCreate

    /*
     ____        _        _                      _____                 _   _
    |  _ \  __ _| |_ __ _| |__   __ _ ___  ___  |  ___|   _ _ __   ___| |_(_) ___  _ __  ___
    | | | |/ _` | __/ _` | '_ \ / _` / __|/ _ \ | |_ | | | | '_ \ / __| __| |/ _ \| '_ \/ __|
    | |_| | (_| | || (_| | |_) | (_| \__ \  __/ |  _|| |_| | | | | (__| |_| | (_) | | | \__ \
    |____/ \__,_|\__\__,_|_.__/ \__,_|___/\___| |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|___/

    */

    // Add data from database
    public void AddData(String item_name, Integer notification_time) {
        boolean isInserted = myDb.insertData(item_name, notification_time);

        if(isInserted) {
            Log.i("Data", "Data inserted successfully");
        } else
            Log.i("Data", "Data not Inserted");
    } // End Add Data

    // Delete data from database
    public void DeleteData(String item_name) {
            Integer deletedRows = myDb.deleteData(item_name);

            if(deletedRows > 0) {
                Log.i("Data", "Data Deleted");
            } else
                Log.i("Data", "Data not Deleted");
    } // End Delete Data


    public void viewAllItems() {
        Cursor res = myDb.getAllItems();
        String tempItem;

        if(res.getCount() == 0) {
            // show message
            Log.i("Error","No Items Found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            // Retrieve items from column 1
            buffer.append("Item Name: "+ res.getString(1) +"\n");
            userItems.add(res.getString(1));

            Log.i("Item Name:", res.getString(1) +"\n");
        }

        // Show all data
        Log.i("Data", buffer.toString());
    } // End View All Items


    public void viewAllNotifications() {
        Cursor res = myDb.getAllItems();

        if(res.getCount() == 0) {
            // show message
            Log.i("Error","No Notifications Found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            // Retrieve items from column 2
            buffer.append("Notification Times: "+ res.getString(2) +"\n");


            Log.i("Notification Times: ", res.getString(2) +"\n");
        }

        // Show all data
        Log.i("Data", buffer.toString());
    } // End View All Items





    /*

      _   _           _       _         ___ _
     | | | |_ __   __| | __ _| |_ ___  |_ _| |_ ___ _ __ ___  ___
     | | | | '_ \ / _` |/ _` | __/ _ \  | || __/ _ \ '_ ` _ \/ __|
     | |_| | |_) | (_| | (_| | ||  __/  | || ||  __/ | | | | \__ \
      \___/| .__/ \__,_|\__,_|\__\___| |___|\__\___|_| |_| |_|___/
           |_|

    */

    @SuppressLint("ClickableViewAccessibility")
    public void updateItems () {

        // Get the current number of user items
        int itemCount = userItems.size();

        // Create an array to hold the buttons
        final Button[] btn = new Button[itemCount];

        // Refresh view
        scrollerlinearlayout.removeAllViews();

        // For each item in UserItems.size create a new button
        // This will refresh each time a new item is added or deleted

        for (int i = 0; i < itemCount; i++) {
            // Add a new button per items in list
            btn[i] = new Button(this);
            btn[i].setId(i);
            final int id = btn[i].getId();
            btn[i].setText(userItems.get(i));
            btn[i].setHeight(75);
            btn[i].setWidth(WRAP_CONTENT);
            btn[i].setTextColor(getColor(R.color.colorBlack));
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            buttonLayoutParams.setMargins(30, 30, 30, 30);
            btn[i].setLayoutParams(buttonLayoutParams);
            btn[i].setElevation(15);
            btn[i].setBackground(getResources().getDrawable(R.drawable.item_custom));

            // Bring the button back to position + 30 (30 for margin)
            initialStartingButtonPosition = (int) btn[i].getX() + 30;

            final int finalI = i;
            btn[i].setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            CurrentBtn = finalI;
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    x1 = (int) event.getX();
                                    x2 = (int) event.getX();
                                    break;

                                case MotionEvent.ACTION_UP:
                                    x2 = (int) event.getX();
                                    float deltaX = x2 - x1;

                                    if(!dialogTriggered){
                                        btn[finalI].setX(initialStartingButtonPosition);
                                    }

                                    // Check if user swipes right
                                    if (deltaX > 0){
                                        dialogTriggered = true;

                                        final Dialog dialog = new Dialog(MainActivity.this);
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog.setCancelable(false);
                                        dialog.setContentView(R.layout.custom_dialog_box);
                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                        FrameLayout mDialogNo = dialog.findViewById(R.id.frmNo);
                                        mDialogNo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                btn[finalI].setX(initialStartingButtonPosition);
                                                btn[finalI].setBackground(getResources().getDrawable(R.drawable.item_custom));
                                                dialog.dismiss();
                                                dialogTriggered = false;
                                            }
                                        });

                                        FrameLayout mDialogOk = dialog.findViewById(R.id.frmOk);
                                        mDialogOk.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                btn[finalI].setBackground(getResources().getDrawable(R.drawable.item_custom_deleted));
                                                btn[finalI].startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.left_to_right_deleted));

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        btn[finalI].setVisibility(View.GONE);
                                                    }
                                                // Keep delay_timer the same as "left_to_right_deleted" to avoid issues
                                                }, 500);

                                                // Search through arraylist and remove button
                                                String removeItemHolder = "";
                                                for (String string : userItems) {
                                                    if (string.equals(btn[finalI].getText().toString())) {
                                                        removeItemHolder = string;
                                                    } // End search
                                                }

                                                // Remove from database
                                                DeleteData(removeItemHolder);


                                                // Remove the item from arraylist
                                                userItems.remove(removeItemHolder);




                                                dialog.cancel();
                                                dialogTriggered = false;


                                            } // End onClick
                                        });

                                        dialog.show();
                                    }
                                    break;

                                case MotionEvent.ACTION_MOVE:
                                    x2 = (int) event.getX();
                                    deltaX = x2 - x1;

                                    // Don't allow user to swipe left
                                    if (!(deltaX < 0)){
                                        int currentButtonPositionInt = (int) btn[finalI].getX();
                                        Log.i("Button position", String.valueOf(currentButtonPositionInt));

                                        // TODO 1: Issue where user swipes right slowly, not registering as a motion but still triggering button change.
                                        if(currentButtonPositionInt < 400){
                                            btn[finalI].setX(btn[finalI].getX() + (event.getX() - x1));
                                        } else {
                                            // Set the button to half delete
                                            btn[finalI].setBackground(getResources().getDrawable(R.drawable.item_custom_half_deleted));
                                            dialogTriggered = true;
                                        }

                                    } // Dont allow use to swipe left



                                    break;
                            }
                            return false;
                        } // On Touch
                    });


            scrollerlinearlayout.addView(btn[i]);
            btn[i].setAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.right_to_left));
        } // End for
    } // End updateItems


    /*
         _       _     _ _               ___ _
        / \   __| | __| (_)_ __   __ _  |_ _| |_ ___ _ __ ___  ___
       / _ \ / _` |/ _` | | '_ \ / _` |  | || __/ _ \ '_ ` _ \/ __|
      / ___ \ (_| | (_| | | | | | (_| |  | || ||  __/ | | | | \__ \
     /_/   \_\__,_|\__,_|_|_| |_|\__, | |___|\__\___|_| |_| |_|___/
                                 |___/

    */

    // Adding Items to user list
    public class ItemEntry {
        public void showItemEntryDialog(Activity activity) {
            final Dialog ItemEntrydialog = new Dialog(activity);
            ItemEntrydialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            ItemEntrydialog.setCancelable(false);
            ItemEntrydialog.setContentView(R.layout.custom_item_entry);
            Objects.requireNonNull(ItemEntrydialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            // If the user chooses no
            FrameLayout mDialogNo = ItemEntrydialog.findViewById(R.id.frmNo);
            mDialogNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemEntrydialog.dismiss();
                }
            });

            // if the user chooses yes
            FrameLayout mDialogOk = ItemEntrydialog.findViewById(R.id.frmOk);
            mDialogOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText EditTextEntry = ItemEntrydialog.findViewById(R.id.EditTextEntry);
                    if (EditTextEntry.getText().toString().trim().equals("")) {
                        Toast.makeText(MainActivity.this, "Entered text was empty", Toast.LENGTH_SHORT).show();
                        ItemEntrydialog.dismiss();
                    } else {
                        // Add text to user List
                        // Replace the initial test button


                        userItems.add(EditTextEntry.getText().toString().trim());
                        updateItems();
                        ItemEntrydialog.dismiss();
                    }
                }
            });
            ItemEntrydialog.show();
        } // Show custom_item_entry
    } // End item Entry

}
