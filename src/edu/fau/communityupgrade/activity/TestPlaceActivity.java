package edu.fau.communityupgrade.activity;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.DefaultSaveCallback;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.ui.LoadingDialog;

/**
 * This is a test Activity to show how to get all locations within a distance
 * of the user.
 * @author kyle
 *
 */
public class TestPlaceActivity extends BaseActivity {

	ListView placeListView;
	PlaceManager placeManager;
	
	//Contains all places
	ArrayList<Place> arrayOfPlaces;
	
	//View Adapter for List of Places
	//Definition defined in TestPlaceActivity
	PlacesAdapter placesAdapter;
	
	//Default loading Dialog 
	LoadingDialog loadingDialog;
	
	EditText placeName,placeContactName,placeContactPhone;
	Button addPlaceBtn;
	
	private static final double MAX_DISTANCE = 60.0;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.testplace_layout);
		placeManager = new PlaceManager(this);
		loadingDialog = new LoadingDialog(this);
		
		arrayOfPlaces = new ArrayList<Place>();
		
		//Set EditText Objects
		placeName = (EditText)findViewById(R.id.add_place_name);
		placeContactName = (EditText)findViewById(R.id.add_place_contact_name);
		placeContactPhone = (EditText)findViewById(R.id.add_place_contact_number);
		
		//Set Button Object
		addPlaceBtn = (Button)findViewById(R.id.add_place_btn);
		
		placeListView = (ListView) findViewById(R.id.test_place_list_view);
		
		// Create the adapter to convert the array to views
		placesAdapter = new PlacesAdapter(
				TestPlaceActivity.this, arrayOfPlaces);
		
		addPlaceBtn.setOnClickListener(new AddPlaceClickListener());
		
		// Attach the adapter to a ListView
		placeListView.setAdapter(placesAdapter);
		
		placeListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Get current Place
				Place place = arrayOfPlaces.get(position);
				
				//Intent to start next activity
				Intent intent = new Intent(TestPlaceActivity.this,TestSinglePlaceActivity.class);
				
				//bundle to hold current Place
				Bundle mBundle = new Bundle();  
		        mBundle.putParcelable(TestSinglePlaceActivity.PLACE_OBJECT_EXTRA, place);  
				intent.putExtras(mBundle);
				startActivity(intent);	
			}
		});
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		updatePlaces(MAX_DISTANCE);
	}
	
	/**
	 * Adds the places from the database.
	 */
	private void updatePlaces(double maxDistance)
	{
		loadingDialog.show();
		// Construct the data source
		placeManager.getAllPlacesNearUser(maxDistance,
				new DefaultFindCallback<Place>(){

					@Override
					public void onComplete(ArrayList<Place> list) {
						
						
						if(list == null)
						{
							addPlaceBtn.setText("Uh Oh!");
							return;
						}
						
						arrayOfPlaces = list;
						placesAdapter.clear();
						placesAdapter.addAll(list);
						loadingDialog.dismiss();
						
					}

					@Override
					public void onError(String error) {
						loadingDialog.dismiss();
						
					}
			
			
		});
		
	}
	
	/**
	 * This Callback reacts to what happens when a place is saved.
	 * @author kyle
	 */
	private class PlaceSaveCallback implements DefaultSaveCallback<Place>{

		@Override
		public void onSaveComplete(Place place) {
			placesAdapter.add(place);
			loadingDialog.dismiss();
			Toast.makeText(TestPlaceActivity.this, "Place Saved!", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onError(String error) {
			loadingDialog.dismiss();
			Toast.makeText(TestPlaceActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
			
		}
		
		
	}
	
	/**
	 * Returns true is input is valid and false if it isn't
	 * @return
	 */
	private boolean isInputValid()
	{
		return !(placeName.getText().toString().isEmpty() ||
				placeContactName.getText().toString().isEmpty() ||
				placeContactPhone.getText().toString().isEmpty());
		
	}
	
	/**
	 * Adapter for ListView of Places
	 * @author kyle
	 *
	 */
	private class PlacesAdapter extends ArrayAdapter<Place> {

		
	    public PlacesAdapter(Context context, ArrayList<Place> places) {
	       super(context, 0, places);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	       // Get the data item for this position
	    	Place place = getItem(position);    
	       // Check if an existing view is being reused, otherwise inflate the view
	       if (convertView == null) {
	          convertView = LayoutInflater.from(getContext()).inflate(R.layout.testplace_list_item, parent, false);
	       }
	       
	       TextView textView = (TextView)convertView.findViewById(R.id.line);
	       
	       textView.setText(place.getName()+", created By "+place.getCreatedBy().getUsername()
	    		   +": "+place.getComments().toString());
	       
	       return convertView;
	   }
	}

	private class AddPlaceClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			if(!isInputValid())
			{
				Builder alert = new AlertDialog.Builder(TestPlaceActivity.this);
				alert.setTitle(getString(R.string.validate_data_title));
				alert.setMessage(getString(R.string.validate_data_message));
				alert.setPositiveButton(getString(R.string.default_confirmation),null);
				alert.show();    
				return;
			}
			String name = placeName.getText().toString();
			loadingDialog.show();
			placeManager.SavePlaceFromUserLocation(new Place(null,name,null,"","", 0, 0,null), 
					new PlaceSaveCallback());
			
			
		}
	}
	
}
