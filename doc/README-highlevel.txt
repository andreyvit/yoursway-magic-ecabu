Magic Ecabu high-level concepts and commands reference
======================================================

The whole usage looks like this:

	% mae-create-tree eclipse/mac/3.4 /Applications/Eclipse\ 3.4.app 
	% mae-pack-tree eclipse/mac/3.4
	% mae-create-version eclipse/mac/3.4
	% mae-promote-version eclipse mac eclipse/mac/3.4 stable


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


mae-create-version
------------------

Creates a component version by postprocessing the results of mae-create-tree and mae-pack-tree. The produced version is to be uploaded to the public update site.

Syntax:
	mae-create-version <versionspec>


mae-promote-version
-------------------

Assigns the given component version to the given product.

Syntax:
	mae-promote-version <product> <platform> <versionspec> <role>
	
Example:
	mae-promote-version ide ide-core/mac/1.1 stable
	mae-promote-version ide mac ide-core/mac/1.1.135 nightly
	
Notes:

Platform names used by various components and platform names used by the product don't have to match, so the platform name is specified twice (as <platform> and inside <versionspec>).


mae-create-pack
---------------

Creates a (single) pack out of (all) the files in the specified folder(s). Useful for tweaking the packs manually, but please use this only if you understand all consequences. (See notes below.)

Syntax:

	mae-create-pack <folder> [<folder>]...
	
Notes:

If you want to make a pack out of a specific component, you should do a regular mae-create-tree, mae-pack-tree, mae-create-version sequence. For example, if several applications use Google Collections library, you should run the following commands (*before* you run mae-pack-tree on any component that includes files from Google Collections):

	mae-create-tree google-collections/mac/1.0 ...path...
	mae-pack-tree google-collections/mac/1.0
	mae-create-version google-collections/mac/1.0
	mae-promote-version libraries mac google-collections/mac/1.0 stable
	
However, if you are not satisfied with the packs chosen for your component(s), you can manually build more suitable packs using mae-create-pack (and they will be automatically chosen then, instead of other ones).

Because of unreachable pack pruning, you should not normally use mae-create-pack to pack files that are not referenced by other components.
