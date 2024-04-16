package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {
	
	@Autowired
	private VenueRepository venueRepository;

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";
	
	@Value("${server.mapbox.access}")
	private String MAPBOX_ACCESS;

	@Override
	public long count() {
//		long count = 0;
//		Iterator<Venue> i = findAll().iterator();
//
//		for (; i.hasNext(); count++) {
//			i.next();
//		}
//
//		return count;
		
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
//		Iterable<Venue> venues;
//
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			InputStream in = new ClassPathResource(DATA).getInputStream();
//
//			venues = mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, Venue.class));
//		} catch (Exception e) {
//			// If we can't read the file, then the event list is empty...
//			log.error("Exception while reading file '" + DATA + "': " + e);
//			venues = Collections.emptyList();
//		}
//
//		return venues;
		
		return venueRepository.findAllByOrderByNameAsc();
	}
	
	private Venue getLongLat(Venue venue) {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				  .accessToken(MAPBOX_ACCESS)
				  .query(venue.getAddress()+" "+venue.getPostcode())
				  .build();
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			  @Override
			  public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
				  Geometry geometry = response.body().features().get(2).geometry();
				  Point point = (Point) geometry;
				  
				  if (point.coordinates().size() >= 2) {
                      double latitude = point.coordinates().get(1);
                      double longitude = point.coordinates().get(0);
                      venue.setLatitude(latitude);
                      venue.setLongitude(longitude);
                      System.out.println("Venue new location: "+Double.toString(latitude)+", "+Double.toString(longitude));
                  }
//			   venue.setLatitude(response.body());
			  	}
			  	@Override
			  	public void onFailure(Call<GeocodingResponse> call, Throwable t) {
			  		System.out.println(t);
			  	}
			 });
		
		return venue;
		
	}
	
	
	
	@Override
	public Venue save(Venue venue) {
		venue = getLongLat(venue);
		return venueRepository.save(venue);
	}
	
	public Venue findById(long id) {
		Iterable<Venue> venues = venueRepository.findAll();
        for (Venue venue : venues) {
            if (venue.getId() == id) {
                return venue;
            }
        }
        return null; 
	}
	
	@Override
	public void deleteById(long id) {
		 venueRepository.deleteById(id);
	}
		 
	public Iterable<Venue> search(String searchTerm) {
		return venueRepository.findByNameLikeIgnoreCase("%" + searchTerm + "%");

	}



}
