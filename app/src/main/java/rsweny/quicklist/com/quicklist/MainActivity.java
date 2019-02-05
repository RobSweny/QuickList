package rsweny.quicklist.com.quicklist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import static rsweny.quicklist.com.quicklist.R.id.fill_horizontal;

public class MainActivity extends AppCompatActivity {

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
    private String item_name;

    private Animation animationHolder;



    ArrayList<String> userItems = new ArrayList<>();




    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scrollerlinearlayout = findViewById(R.id.scrollerlinearlayout);
        linearLayout = findViewById(R.id.linearLayout);
        itemDeletionTextView = findViewById(R.id.itemDeletionTextView);
        spinner = findViewById(R.id.spinner);

        userItems.add("Test Item");
        updateItems();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show keyboard

                // Create custom dialog box to input new items
                ItemEntry itemAdded = new ItemEntry();
                itemAdded.showItemEntryDialog(MainActivity.this);

            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @SuppressLint("ClickableViewAccessibility")
    public void updateItems () {

        int itemCount = userItems.size();
        final Button[] btn = new Button[itemCount];
        // Refresh view
        scrollerlinearlayout.removeAllViews();

        for (int i = 0; i < userItems.size(); i++) {
            // Add a new button per items in list
            btn[i] = new Button(this);
            btn[i].setId(i);
            final int id = btn[i].getId();
            btn[i].setText(userItems.get(i));
            btn[i].setHeight(75);
            btn[i].setWidth(fill_horizontal);
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
                                case MotionEvent.ACTION_DOWN:;
                                    x1 = (int) event.getX();
                                    x2 = (int) event.getX();
                                    break;

                                case MotionEvent.ACTION_UP:
                                    x2 = (int) event.getX();
                                    float deltaX = x2 - x1;

                                    if(!dialogTriggered){
                                        btn[finalI].setX(initialStartingButtonPosition);
                                    }

                                    // Check if user swipes left or right
                                    if (deltaX < 0) {
                                        // Right to left swipe
                                    }else if(deltaX > 0){
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

                                        if(currentButtonPositionInt < 350){
                                            // Set the button to half delete
                                            btn[finalI].setBackground(getResources().getDrawable(R.drawable.item_custom_half_deleted));
                                            dialogTriggered = true;
                                        } else {
                                            btn[finalI].setX(btn[finalI].getX() + (event.getX() - x1));
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
    }


    // Adding Items to user list
    public class ItemEntry {
        public void showItemEntryDialog(Activity activity) {
            final Dialog ItemEntrydialog = new Dialog(activity);
            ItemEntrydialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            ItemEntrydialog.setCancelable(false);
            ItemEntrydialog.setContentView(R.layout.custom_item_entry);
            ItemEntrydialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

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
