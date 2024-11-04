package com.example.EcoRide.Rental;
import com.example.EcoRide.Rental.models.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class CustomerTest {

    //Test 1

    @Test
    public void testMail(){
        Customer p1 = new Customer();
        p1.setEmail("marko@gmail.com");
        Customer p2 = new Customer();
        p2.setEmail("marko@gmail.com");
        Assertions.assertTrue(p1.sameMail(p2));
    }


    //TEST 2

    List<Customer> persons = new ArrayList<>();


    @BeforeEach
        void starting(){
            Customer p1 = new Customer("Marko","1111");
            persons.add(p1);
            Customer p2 = new Customer("Darko","1112");
            persons.add(p2);
        }

        @Test
        //Size of the array
        void test1(){
            Assertions.assertEquals(2, persons.size());
        }



}
