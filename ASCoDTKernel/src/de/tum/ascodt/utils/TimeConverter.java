// Copyright (C) 2009 Technische Universitaet Muenchen
// This file is part of the ASCoDT project. For conditions of distribution and
// use, please see the copyright notice at www5.in.tum.de/ascodt
package de.tum.ascodt.utils;

/**
 * 
 * @author Tobias Weinzierl
 */
public class TimeConverter {
  public static String getHumanReadableTimestamp() {
  try {
    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("hh::mm::ss");
    return dateFormat.format( new java.util.Date(System.currentTimeMillis()) );
    } catch (Exception e) {
      return "<error>"; 
    }
  }
}
