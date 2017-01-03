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

-- Create ancestorPath index for tree positions (not needed bcause of lucene)
--create index x_ctr_ancestor_path on concept_tree_positions (ancestorPath(255));
--create index x_dtr_ancestor_path on descriptor_tree_positions (ancestorPath(255));
--create index x_cdtr_ancestor_path on code_tree_positions (ancestorPath(255));
