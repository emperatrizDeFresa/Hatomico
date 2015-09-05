package emperatriz.hatomico2;


import java.util.ArrayList;



import android.app.LauncherActivity.ListItem;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ContactList extends ListActivity {
	boolean marking = true;
	ListAdapter adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        adapter = new android.widget.SimpleCursorAdapter(
                this, // Context.
                android.R.layout.simple_list_item_multiple_choice,  // Specify the row template to use (here, two columns bound to the two retrieved cursor rows).
                Sys.init().getAllContacts(),                                              // Pass in the cursor to bind to.
                new String[] {ContactsContract.Contacts.DISPLAY_NAME},           // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2}); 
        setListAdapter(adapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        long[] checked = Sys.init().getWhiteList();
        
        for (int i=0;i<checked.length;i++){
        	long id = checked[i];
        	for (int j=0;j<adapter.getCount();j++){
        		if (id==adapter.getItemId(j)){
        			getListView().setItemChecked(j, true);
        		}
        	}
        }

        if (Build.VERSION.SDK_INT >= 20){
            super.setTheme(android.R.style.Theme_Material_Light_DarkActionBar);
            getActionBar().setIcon(null);
        }

	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        long[] checked = Sys.init().getWhiteList();
        

        	for (int j=0;j<adapter.getCount();j++){

        			getListView().setItemChecked(j, marking);
        		
        	}
        
        	marking = !marking;

			return true;

		
	}
	
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		Sys.init().setWhiteList(getListView().getCheckedItemIds());
		
	}
	

	
}