package com.example.EcoRide.Rental;
import com.example.EcoRide.Rental.controllers.EbikeController;
import com.example.EcoRide.Rental.models.Ebike;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class EbikeTest {
    @Test
    public void testEbikeId() {
        Ebike ebike = new Ebike();
        ebike.setId(101);
        Assertions.assertEquals(101, ebike.getId());
    }

    @Test
    public void testSameBrand() {
        Ebike ebike1 = new Ebike();
        ebike1.setModel("EcoRide");
        Ebike ebike2 = new Ebike();
        ebike2.setModel("EcoRide");
        Assertions.assertEquals(ebike1.getModel(), ebike2.getModel());
    }

    List<Ebike> ebikes = new ArrayList<>();

    @BeforeEach
    public void setup() {
        Ebike ebike1 = new Ebike();
        ebike1.setId(101);
        ebikes.add(ebike1);

        Ebike ebike2 = new Ebike();
        ebike2.setId(102);
        ebikes.add(ebike2);
    }

    @Test
    public void testEbikeListSize() {
        Assertions.assertEquals(2, ebikes.size());
    }
}
