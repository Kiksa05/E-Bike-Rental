
package com.example.EcoRide.Rental.dao;

import com.example.EcoRide.Rental.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


//DONE
@Repository
public interface CustomerDao extends JpaRepository<Customer, Integer> {
//    Customer getCustomerById(int id);
////    List<Customer> getAllCustomers();
//    void saveCustomer(Customer customer);
//    void updateCustomer(Customer customer);
//    void deleteCustomer(int id);
    Customer findByEmail(String email);
    Customer findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, int id);
}

