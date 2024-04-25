package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "venues")
public class Venue {
	
	@Id
	@GeneratedValue
	private long id;
	
	@NotBlank(message="Name is a compulsory field")
	@Size(max=255, message="Event Name can't be more than 255 characters")	
	private String name;
	
	@NotNull(message="Name is a compulsory field")
	@Min(value=0, message="Capacity has to be greater than 0")
	private int capacity;
	
	@Size(max=299, message="Address can't be longer than 299 characters")
	private String address;
	
	@Size(max=499, message="Postcode can't be longer than 499 characters")
	private String postcode;
	
	private double longitude = 0.0;
	
	private double latitude = 0.0;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude2) {
		this.latitude = latitude2;
	}
}
