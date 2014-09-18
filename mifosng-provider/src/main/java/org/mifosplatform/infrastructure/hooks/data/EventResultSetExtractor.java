/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class EventResultSetExtractor implements
		ResultSetExtractor<List<Grouping>> {

	@Override
	public List<Grouping> extractData(final ResultSet rs) throws SQLException,
			DataAccessException {
		final List<Grouping> groupings = new ArrayList<>();

		final Map<String, Map<String, List<String>>> groupToEntityMapping = new HashMap<>();
		Map<String, List<String>> entityToActionMapping = new HashMap<>();

		while (rs.next()) {
			final String groupingName = rs.getString("grouping");
			final String entityName = rs.getString("entity_name");
			final String actionName = rs.getString("action_name");
			Map<String, List<String>> entities = groupToEntityMapping
					.get(groupingName);
			List<String> actions = entityToActionMapping.get(entityName);

			if (entities == null) {
				entityToActionMapping = new HashMap<>();
			}

			if (actions == null) {
				actions = new ArrayList<>();
			}
			actions.add(actionName);
			entityToActionMapping.put(entityName, actions);

			if (entities == null) {
				entities = new HashMap<>();
			}
			entities.putAll(entityToActionMapping);
			groupToEntityMapping.put(groupingName, entities);
		}

		for (final Entry<String, Map<String, List<String>>> groupingEntry : groupToEntityMapping
				.entrySet()) {
			final List<Entity> entities = new ArrayList<>();
			final Grouping group = new Grouping();
			group.setName(groupingEntry.getKey());
			for (final Entry<String, List<String>> entityEntry : groupingEntry
					.getValue().entrySet()) {
				final List<String> actions = new ArrayList<>();
				final Entity entity = new Entity();
				entity.setName(entityEntry.getKey());
				for (final String action : entityEntry.getValue()) {
					actions.add(action);
				}
				Collections.sort(actions);
				entity.setActions(actions);
				entities.add(entity);
			}

			Collections.sort(entities, new Comparator<Entity>() {
				@Override
				public int compare(final Entity entity1, final Entity entity2) {
					return entity1.getName().compareTo(entity2.getName());
				}
			});
			group.setEntities(entities);
			groupings.add(group);
		}

		Collections.sort(groupings, new Comparator<Grouping>() {
			@Override
			public int compare(final Grouping grouping1,
					final Grouping grouping2) {
				return grouping1.getName().compareTo(grouping2.getName());
			}
		});

		return groupings;
	}

}
