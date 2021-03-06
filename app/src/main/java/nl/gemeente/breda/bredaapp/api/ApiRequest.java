package nl.gemeente.breda.bredaapp.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;

import nl.gemeente.breda.bredaapp.R;
import nl.gemeente.breda.bredaapp.businesslogic.ServiceManager;
import nl.gemeente.breda.bredaapp.domain.Service;
import nl.gemeente.breda.bredaapp.util.Converter;
import nl.gemeente.breda.bredaapp.util.ReverseGeocoder;

public class ApiRequest {
	
	private Context context;
	
	public ApiRequest(Context context) {
		this.context = context;
	}
	
	public void post(String service, String description, double latitude, double longitude) {
		Log.i("Post", "Activated.");
		String serviceCode = "";
		for (Service s : ServiceManager.getServices()) {
			if (s.getServiceName().equalsIgnoreCase(service)) {
				serviceCode = s.getServiceCode();
			}
		}
		
		ReverseGeocoder reverseGeocoder = new ReverseGeocoder(latitude, longitude, context);
		Log.i("Got address", reverseGeocoder.getAddress());
		String[] addressComponents = reverseGeocoder.getAddress().split(" ");
		String address = "";
		int addressID = 0;
		int position = -1;
		for (String s : addressComponents) {
			if (Converter.isInt(s)) {
				addressID = Integer.parseInt(s);
				position = Arrays.asList(addressComponents).indexOf(s);
				break;
			}
		}
		
		if (position != -1) {
			for (int i = 0; i < position; i++) {
				address = address + " " + addressComponents[i];
			}
			address = address.replaceAll(" ", "%20");
			address = address.trim();
		}
		
		String urlBase = context.getString(R.string.api_address) + "requests.json";
		
		StringBuilder urlStringBuilder = new StringBuilder();
		urlStringBuilder.append("service_code=" + serviceCode + "");
		urlStringBuilder.append("&description=" + description.replaceAll(" ", "%20") + "");
		urlStringBuilder.append("&lat=" + latitude);
		urlStringBuilder.append("&long=" + longitude);
		urlStringBuilder.append("&address_string=" + address + "");
		urlStringBuilder.append("&address_id=" + addressID);
		
		Uri.Builder builder = new Uri.Builder()
				.appendQueryParameter("service_code", serviceCode)
				.appendQueryParameter("description", description.replaceAll(" ", "%20"))
				.appendQueryParameter("lat", Double.toString(latitude))
				.appendQueryParameter("long", Double.toString(longitude))
				.appendQueryParameter("address_string", address)
				.appendQueryParameter("address_id", Integer.toString(addressID));
		
		String query = builder.build().getEncodedQuery();
		Log.i("URL", query);
		
		new ApiPostTask(context, urlBase, query).execute();
	}
}
