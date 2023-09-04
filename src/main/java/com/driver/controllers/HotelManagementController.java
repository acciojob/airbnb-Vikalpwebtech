package com.driver.controllers;

import com.driver.model.Booking;
import com.driver.model.Facility;
import com.driver.model.Hotel;
import com.driver.model.User;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/hotel")
public class HotelManagementController {

    private HashMap<String,Hotel> hoteldb = new HashMap<>();
    private HashMap<Integer,User> userdb = new HashMap<>();
    private HashMap<String,Booking> bookingdb = new HashMap<>();
    private HashMap<Integer,Integer> countofbooking = new HashMap<>();
    @PostMapping("/add-hotel")
    public String addHotel(@RequestBody Hotel hotel){

        //You need to add an hotel to the database
        //incase the hotelName is null or the hotel Object is null return an empty a FAILURE
        //Incase somebody is trying to add the duplicate hotelName return FAILURE
        //in all other cases return SUCCESS after successfully adding the hotel to the hotelDb.

        if (Objects.nonNull(hotel) && Objects.nonNull(hotel.getHotelName())){
            if (hoteldb.containsKey(hotel.getHotelName())){
                return "FAILURE";
            }
            hoteldb.put(hotel.getHotelName(),hotel);
            return "SUCCESS";
        }
        return "FAILURE";
    }

    @PostMapping("/add-user")
    public Integer addUser(@RequestBody User user){

        //You need to add a User Object to the database
        //Assume that user will always be a valid user and return the aadharCardNo of the user

        userdb.put(user.getaadharCardNo(),user);
       return user.getaadharCardNo();
    }

    @GetMapping("/get-hotel-with-most-facilities")
    public String getHotelWithMostFacilities(){

        //Out of all the hotels we have added so far, we need to find the hotelName with most no of facilities
        //Incase there is a tie return the lexicographically smaller hotelName
        //Incase there is not even a single hotel with atleast 1 facility return "" (empty string)

        String ans = "";
        int max = 0;

        for (Hotel hotelname:hoteldb.values()){
            List<Facility> list = hotelname.getFacilities();
            if(list.size() > max){
                max = list.size();
                ans = hotelname.getHotelName();
            } else if (list.size() == max) {
                if (hotelname.getHotelName().compareTo(ans) < 1){
                    ans = hotelname.getHotelName();
                }
            }
        }

        if (max == 0) return "";
        return ans;
    }

    @PostMapping("/book-a-room")
    public int bookARoom(@RequestBody Booking booking){

        //The booking object coming from postman will have all the attributes except bookingId and amountToBePaid;
        //Have bookingId as a random UUID generated String
        //save the booking Entity and keep the bookingId as a primary key
        //Calculate the total amount paid by the person based on no. of rooms booked and price of the room per night.
        //If there arent enough rooms available in the hotel that we are trying to book return -1 
        //in other case return total amount paid 

        String key = UUID.randomUUID().toString();
        booking.setBookingId(key);

        String hotelname = booking.getHotelName();

        Hotel hotel = hoteldb.get(hotelname);

        if (hotel.getAvailableRooms() < booking.getNoOfRooms()) return -1;

        int amounttopay = hotel.getPricePerNight()*booking.getNoOfRooms();
        booking.setAmountToBePaid(amounttopay);

        //Now need to update no. of available rooms
        hotel.setAvailableRooms(hotel.getAvailableRooms() - booking.getNoOfRooms());

        bookingdb.put(key,booking);
        hoteldb.put(hotelname,hotel);

        int adharcard = booking.getBookingAadharCard();
        Integer countofBooks = countofbooking.get(adharcard);

        //Now increase the count of Booking
        countofbooking.put(adharcard,Objects.isNull(countofBooks)?1:countofBooks+1);
        return amounttopay;
    }
    
    @GetMapping("/get-bookings-by-a-person/{aadharCard}")
    public int getBookings(@PathVariable("aadharCard")Integer aadharCard)
    {
        //In this function return the bookings done by a person
        return countofbooking.get(aadharCard);
    }

    @PutMapping("/update-facilities")
    public Hotel updateFacilities(List<Facility> newFacilities,String hotelName){

        //We are having a new facilites that a hotel is planning to bring.
        //If the hotel is already having that facility ignore that facility otherwise add that facility in the hotelDb
        //return the final updated List of facilities and also update that in your hotelDb
        //Note that newFacilities can also have duplicate facilities possible
        //checked

        List<Facility> facilities1 = hoteldb.get(hotelName).getFacilities();

        for (Facility facility: newFacilities){
            if (!facilities1.contains(facility)){
                facilities1.add(facility);
            }
        }
        hoteldb.get(hotelName).setFacilities(facilities1);
        return hoteldb.get(hotelName);
    }

}
