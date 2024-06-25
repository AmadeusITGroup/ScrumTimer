/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amadeus.tpe.src.sca.scrumtimer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author hiran.chaudhuri
 */
public class TimerPanelTest {
    
    public TimerPanelTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetHumanReadableTime() {
        assertEquals(" 0 s", TimerPanel.getHumanReadableTime(0));
        assertEquals(" 1 s", TimerPanel.getHumanReadableTime(1));
        assertEquals(" 2 s", TimerPanel.getHumanReadableTime(2));
        assertEquals(" 9 s", TimerPanel.getHumanReadableTime(9));
        assertEquals("10 s", TimerPanel.getHumanReadableTime(10));
        assertEquals("59 s", TimerPanel.getHumanReadableTime(59));
        assertEquals("60 s", TimerPanel.getHumanReadableTime(60));
        assertEquals(" 1 m 01 s", TimerPanel.getHumanReadableTime(61));
        assertEquals(" 1 m 59 s", TimerPanel.getHumanReadableTime(119));
        assertEquals(" 2 m 00 s", TimerPanel.getHumanReadableTime(120));
        assertEquals(" 2 m 01 s", TimerPanel.getHumanReadableTime(121));
        assertEquals("59 m 59 s", TimerPanel.getHumanReadableTime(3599));
        assertEquals(" 1 h 00 m", TimerPanel.getHumanReadableTime(3600));
        assertEquals(" 1 h 00 m", TimerPanel.getHumanReadableTime(3601));
        assertEquals(" 1 h 00 m", TimerPanel.getHumanReadableTime(3659));
        assertEquals(" 1 h 01 m", TimerPanel.getHumanReadableTime(3660));
        assertEquals(" 1 h 01 m", TimerPanel.getHumanReadableTime(3661));
    }
}
