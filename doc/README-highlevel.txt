Magic Ecabu high-level concepts and commands reference
======================================================


mae-create-tree
---------------

Builds and stores a tree for the given component version.

Syntax:
	mae-create-tree <versionspec> <directory>
	
Example:
	mae-create-tree ide-ruby/mac/0.97 ../build/0.97/ruby-mac
	
Files:
	$MAE_DIR/tree/<versionspec>.txt


mae-pack-common
---------------

Create a pack out of the blobs which are both:
(1) common to all of the given trees, and
(2) cannot be taken from one of the existing packs (e.g. because of the reuse thresholds in effect or because those blobs are not contained in any of the existing packs).

Syntax:
	mae-pack-common <versionspec> [<versionspec>]...

Notes:

For each of the <versionspec>'s given, a corresponding tree must have already been created through an invokation of mae-create-tree.


mae-pack-tree
-------------

Creates a pack out of the blobs of the given tree that cannot be taken from one of the existing packs (e.g. because of the reuse thresholds in effect or because those blobs are not contained in any of the existing packs).

Syntax:
	mae-pack-tree <versionspec>
	
Notes:

A tree corresponding to the <versionspec> given must have already been created through an invokation of mae-create-tree.
