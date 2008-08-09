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


mae-pack-tree
-------------

Creates a pack out of the blobs of the given tree that cannot be reused from one of the existing packs (e.g. because of the reuse thresholds in effect or because those blobs are not contained in any of the existing packs).

Syntax:
	mae-pack-tree <versionspec> [<versionspec>]...
	
Notes:

For each of the <versionspec>'s given, a corresponding tree must have already been created through an invokation of mae-create-tree.

If several <versionspec>'s are given, mae-pack-tree will first attemp to write a pack containing the common
blobs of all of the trees, and will process each tree regularly after that. This can be used to optimize bandwidth and storage space when several similar trees are produced simultaneously (think builds for several platforms with most files being common).
