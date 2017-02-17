-- NOTE: these queries have to be on single lines to function properly.

-- Load initial admin user
INSERT INTO users (id, applicationRole, editorLevel, email, name, userName) values (1, 'ADMINISTRATOR', 0, 'test@example.com', 'Admin User', 'admin');
-- Load initial guest user
INSERT INTO users (id, applicationRole, editorLevel, email, name, userName) values (2, 'VIEWER', 0, 'test@example.com', 'Guest User', 'guest');
-- Load initial loader user
INSERT INTO users (id, applicationRole, editorLevel, email, name, userName) values (3, 'VIEWER', 0, 'test@example.com', 'Loader User', 'loader');
-- Load initial author user
INSERT INTO users (id, applicationRole, editorLevel, email, name, userName) values (4, 'AUTHOR', 0, 'test@example.com', 'Author', 'author');

-- concepts indexes for editing
create index x_concepts_1 on concepts(terminology);
create index x_concepts_2 on concepts(workflowStatus);
create index x_atoms_1 on atoms(workflowStatus);
create index x_atom_rels_1 on atom_relationships(workflowStatus);
create index x_concept_rels_1 on concept_relationships(workflowStatus);

-- Create terminology index for tree positions (not needed bcause of lucene)
create index x_atr_t on atom_tree_positions (terminology);
create index x_ctr_t on concept_tree_positions (terminology);
create index x_dtr_t on descriptor_tree_positions (terminology);
create index x_cdtr_t on code_tree_positions (terminology);

-- Views for making it easier to query in a MEME4 kind of way
create view classes_m4
as select a.id atom_d, a.name, a.terminology, a.version, a.publishable, a.stringClassId sui, a.lexicalClassId lui,
 a.codeId code, a.conceptId scui, a.descriptorId sdui, c.id concept_id
from atoms a, concepts_atoms b, concepts c
where c.terminology = 'NCIMTH'
  and atoms_id = a.id and concepts_id = c.id;

create view auis_m4 as
select a.id atom_id, b.alternateTerminologyIds aui
from atoms a, AtomJpa_alternateTerminologyIds b
where a.id = b.AtomJpa_id
  and b.alternateTerminologyIds_KEY = 'NCIMTH';

create view ruis_m4 as
select a.id relationship_id, 'CONCEPT' type, b.alternateTerminologyIds rui
from concept_relationships a, ConceptRelationshipJpa_alternateTerminologyIds b
where a.id = b.ConceptRelationshipJpa_id
  and b.alternateTerminologyIds_KEY = 'NCIMTH'
union all
select a.id relationship_id, 'CODE' type, b.alternateTerminologyIds rui
from code_relationships a, CodeRelationshipJpa_alternateTerminologyIds b
where a.id = b.CodeRelationshipJpa_id
  and b.alternateTerminologyIds_KEY = 'NCIMTH'
union all
select a.id relationship_id, 'DESCRIPTOR' type, b.alternateTerminologyIds rui
from descriptor_relationships a, DescriptorRelationshipJpa_alternateTerminologyIds b
where a.id = b.DescriptorRelationshipJpa_id
  and b.alternateTerminologyIds_KEY = 'NCIMTH'
union all
select a.id relationship_id, 'ATOM' type, b.alternateTerminologyIds rui
from atom_relationships a, AtomRelationshipJpa_alternateTerminologyIds b
where a.id = b.AtomRelationshipJpa_id
  and b.alternateTerminologyIds_KEY = 'NCIMTH';

  

  
