/*
 * Copyright (c) 2024, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.test.jdbc2;

import static org.junit.Assert.fail;

import org.postgresql.test.TestUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Tests both db and java side that correct type are passed.
 */
@RunWith(Parameterized.class)
public class InstantTest extends BaseTest4 {

  public InstantTest(BinaryMode binaryMode) {
    setBinaryMode(binaryMode);
  }

  @Parameterized.Parameters(name = "timestamptzAlways = {1}")
  public static Iterable<Object[]> data() {
    Collection<Object[]> ids = new ArrayList<Object[]>();
    for (BinaryMode binaryMode : BinaryMode.values()) {
      ids.add(new Object[]{binaryMode});
    }
    return ids;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtil.createSchema(con, "testtimestamp");
    TestUtil.createTable(con, "testtimestamp.tbtesttimestamp", "id bigint, ts timestamptz");
  }

  @Override
  public void tearDown() throws SQLException {
    TestUtil.dropTable(con, "testtimestamp.tbtesttimestamp");
    TestUtil.dropSchema(con, "testtimestamp");
    super.tearDown();
  }

  @Test
  public void testJavaTime_ZonedDateTime() throws SQLException {
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"));
    cal.set(2023, Calendar.MARCH, 12, 9, 30);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    //Problem
    // java.lang.AssertionError: Can't infer the SQL type to use for an instance of java.time.ZonedDateTime. Use setObject() with an explicit Types value to specify the type to use.
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, cal.toZonedDateTime());
    } catch (SQLException e) {
      fail(e.getMessage());
    }

    //Problem
    // java.lang.AssertionError: Bad value for type timestamp/date/time: 2023-03-12T09:30+01:00[Europe/Berlin]
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, cal.toZonedDateTime(), Types.TIMESTAMP_WITH_TIMEZONE);
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testJavaTime_Instant() throws SQLException {
    //Problem
    // java.lang.AssertionError: Can't infer the SQL type to use for an instance of java.time.ZonedDateTime. Use setObject() with an explicit Types value to specify the type to use.
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, Instant.now());
    } catch (SQLException e) {
      fail(e.getMessage());
    }

    //Problem
    // java.lang.AssertionError: Bad value for type timestamp/date/time: 2023-03-13T20:34:58.711330339Z
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, Instant.now(), Types.TIMESTAMP);
    } catch (SQLException e) {
      fail(e.getMessage());
    }

    //Problem
    // java.lang.AssertionError: Cannot cast an instance of java.time.Instant to type Types.TIMESTAMP_WITH_TIMEZONE
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, Instant.now(), Types.TIMESTAMP_WITH_TIMEZONE);
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testJavaTime_LocalTime() throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, LocalTime.now());
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testJavaTime_LocalDateTime() throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(" SELECT pg_typeof(?) ")) {
      ps.setObject(1, LocalDateTime.now());
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }

}
