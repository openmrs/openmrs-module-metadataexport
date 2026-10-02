/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Environment;

/**
 * Adjusts the in-memory test schema that Hibernate generates from the mappings where it is stricter
 * than the schema the modules' liquibase changesets create on a real database.
 */
public final class TestSchema {
	
	private TestSchema() {
	}
	
	/**
	 * Hibernate 7 generates a unique constraint for the join column of a {@code @OneToOne}, which the
	 * modules' liquibase changesets do not create, so rows sharing the referenced row cannot be seeded
	 * otherwise. H2 keeps the unique index alive while the column's foreign key uses it, so the foreign
	 * key is dropped first and recreated afterwards. Runs on its own connection because DDL commits the
	 * transaction it runs in.
	 */
	public static void dropUniqueConstraints(Properties runtimeProperties, String table, String column,
	        String referencedTable, String referencedColumn) {
		try (Connection connection = DriverManager.getConnection(runtimeProperties.getProperty(Environment.URL),
		    runtimeProperties.getProperty(Environment.USER), runtimeProperties.getProperty(Environment.PASS))) {
			List<String> foreignKeys = constraints(connection, "FOREIGN KEY", table, column);
			List<String> uniqueConstraints = constraints(connection, "UNIQUE", table, column);
			try (Statement statement = connection.createStatement()) {
				for (String constraint : foreignKeys) {
					statement.execute("alter table " + table + " drop constraint \"" + constraint + "\"");
				}
				for (String constraint : uniqueConstraints) {
					statement.execute("alter table " + table + " drop constraint \"" + constraint + "\"");
				}
				for (String constraint : foreignKeys) {
					statement.execute("alter table " + table + " add constraint \"" + constraint + "\" foreign key ("
					        + column + ") references " + referencedTable + " (" + referencedColumn + ")");
				}
			}
		}
		catch (SQLException e) {
			throw new IllegalStateException("Could not drop the unique constraints on " + table + "." + column, e);
		}
	}
	
	private static List<String> constraints(Connection connection, String type, String table, String column)
	        throws SQLException {
		List<String> constraints = new ArrayList<>();
		try (PreparedStatement query = connection.prepareStatement("select tc.constraint_name"
		        + " from information_schema.table_constraints tc join information_schema.key_column_usage k"
		        + " on k.constraint_name = tc.constraint_name and k.table_name = tc.table_name"
		        + " where tc.constraint_type = ? and upper(tc.table_name) = ? and upper(k.column_name) = ?")) {
			query.setString(1, type);
			query.setString(2, table.toUpperCase());
			query.setString(3, column.toUpperCase());
			try (ResultSet rs = query.executeQuery()) {
				while (rs.next()) {
					constraints.add(rs.getString(1));
				}
			}
		}
		return constraints;
	}
}
