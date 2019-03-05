package com.wci.umls.server.mojo.processes;

import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctRelationship;

public class SctRelationshipParser {

	/** The output file path. */
	public SctRelationship parse(String conName, final Relationship<?, ?> relObject) {

		SctRelationship rel = new SctRelationship();

		if (!relObject.isObsolete() && relObject.isInferred() && !relObject.isStated()) {
			// Concept Name
			rel.setDescription(conName);

			// Relationship Type
			if (relObject.getRelationshipType().equals("Is a")) {
				rel.setRelationshipType(relObject.getRelationshipType());
			} else {
				rel.setRelationshipType(relObject.getAdditionalRelationshipType());
			}

			// Relationship Destination
			rel.setRelationshipDestination(relObject.getTo().getName());

			// Role Group
			rel.setRoleGroup(Integer.parseInt(relObject.getGroup()));
			return rel;
		}

		return null;
	}

}
