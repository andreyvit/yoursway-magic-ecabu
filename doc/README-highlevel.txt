Magic Ecabu high-level concepts and commands reference
======================================================

To understand the layout of an update repository, you need to know several concepts:

Components
----------

Components are the smallest units of installation: a given application installation contains a specific set of installed components. Each file that has been installed with the application belongs to exactly one of the components.

Some components might be shared by several applications, which means that each application is using the components in question. The sharing is on the update site side only: there is a single copy of each component's files on the update site, but a new copy of the component is installed with every application that needs it.

An update site contains multiple versions of a component, but neither of them is designated as "stable", "integration" etc. There is no notion of stability at the component level.

Component versions are specified using a strict format "component-name/platform/version", e.g. eclipse/mac/3.4. This format is enforced and interpreted by all commands dealing with versions.


Suite
-----

A suite is the largest unit of installation, and is a singleton object from the updates point of view: it's a single application (or even a suite of applications, like “Apple iWork”) that is invoking the whole update machinary. A suite consists of one or more products.


Products
--------

A product is a user-visible unit of update. A product gives a common (product) version number to some specific versions of multiple components.

The product versioning might differ depending on a “release type” chosen by the user, with examples being "stable", "integration" and "nightly". Product maintainers define a set of such release types, and decide which version of each component corresponds to each release type.


Component vs. product vs. suite
-------------------------------

The suite corresponds to the whole operating system application or a group of applications, like, say, “yoursway-ide” or “iwork” or “ms-office”.

In a simple case, there may be one-to-one correspondance between suites, products and components. For example, a simple “corchy” suite might have a single product “corchy”, which might have a single component “corchy”. (Of course, for packing purposes there would be other components defined by the developer, like “google-collections” or “corchy-sync-trac”, but those components would not be propagated to the public update site.)

However in a more complex case a need might arise to separate a suite from products and from components. Consider the following case:
— YourSway IDE contains optional components (Ruby support, PHP support etc), and
— There are third-party contributed components that do not participate in the release cycle of YourSway IDE, are marketed separately but still should be made available to the users on the main update site (say, a “Mega Language Pack”)

The whole YourSway IDE is a suite (probably named “ide”).

However, YourSway IDE consists of several components, like ide-core, ide-ruby and ide-php, some of which might not be installed. Still the users want to have a single notion of “YourSway IDE version”, so that an update dialog can say “YourSway IDE 1.0.3 released”. Thus an “ide” product should be defined.

However the “Mega Language Pack” has a separate release cycle and thus has own version numbers too, so it's another product: “ide-megapack”. This product might have optional components too: “ide-megapack-algol”, “ide-megapack-ada”, “ide-megapack-cobol”. Still an update notification would say “Mega Language Pack 2.3 released”.


Pack
----

A unit of uploading and downloading. A pack is a zip file containing several other files, with each file named after SHA1 hash of its content.

A pack thus does *not* associate any specific file name or path with the files it carries. A pack alone cannot be used to reconstruct a file system structure of the suite. The only thing a pack does is carrying the actual file content.

Each component version has a version definition file that specifies a way to reconstruct the file system tree from the data inside one or more packs:
	b143427e1d89442420f28cc339dc63dd864fa4cc myapp.exe
	de775019e7319f150b50550c957a7ab92a494900 lib/somelib.dll
	1b8c7c2b2111d54fb8235109b8cd2417e3c644fe help/myapp.html
Given a SHA1 hash of the data (which is the leftmost field above), the actual data can be obtained by downloading one or more packs and extracting the necessary files from them. (The set of packs to be downloaded is also specified in the component definition file.)

Packs serve 4 purposes:

1) Download size minimization. It is likely that in a new version of a component most files are unchanged, and thus the version definition file of the new component will refer to a large pack(s) that the user already has, and a (small) pack with only the changed files.

2) Connection count minimization. A pack consolidates multiple source files into a large unit, so only a single connection to the server is needed to download them all.

3) Upload size minimization. If the suite is large, and a new build is needed urgently, it is not wise to wait for the whole suite to be uploaded.

3) Update site storage optimization. Storing gigabytes of duplicate bits of various suite versions is a waste and costs money.


Tree
----

An intermediate object created when defining a new version of a component. A tree lists all the files of the component, together with a corresponding path in the local file system. (A tree is thus local to a single computer and should never be transferred.)


Update site layout
------------------

packs/<sha1>.zip
	– here are the packs (which are themselves named after SHA1 of their content
	to prevent duplicate packs).

components/<name>_<platform>_<version>.txt
	— is a component definition file that lists a set of packs needed by this component,
	and the layout of files on the file system.

suites/<name>/versions_<platform>.txt
	— lists component and product versions for the given suite.
	
products/<name>/info.txt
products/<name>/<platform>_<version>/news.html

The following directories are not required by the update process, but are required to be present locally for the operation of Magic Ecabu. They don't need to be transferred to a public update site.

catalog/<sha1>.txt
	— lists SHA1 hashes of the files inside a corresponding pack; this speeds up
	processing by avoiding operations on large pack files, and also allows to delete
	the actual pack files locally after they have been transferred to the public
	update site.
	
	Magic Ecabu does not need any pack files to operate, it only needs the corresponding
	catalog files. And in case the catalog files are lost, they can always be regenerated
	from the pack files.
	
trees/<name>_<plaftorm>_<version>.txt
	— lists the files of the given tree.
	
trees/<name>_<plaftorm>_<version>_packlist.txt
	— lists the set of packs that together entirely contain all the files of the tree;
	the packlist is saved by mae-pack-tree command and is then used by mae-create-version.
	
The whole trees/ directory should never be transferred to other computers and can be safely deleted.


Commands reference
==================

A process of publishing a new version of a component looks like this:

	% mae-create-tree eclipse/mac/3.4 /Applications/Eclipse\ 3.4.app 
	% mae-pack-tree eclipse/mac/3.4
	% mae-create-version eclipse/mac/3.4
	% mae-promote-version eclipse mac eclipse/mac/3.4 stable
	
Then use e.g. s3sync to upload the new data to a public update site stored on S3.


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

Assigns the given component version to the given suite.

Syntax:
	mae-promote-version <product> <platform> <versionspec> <release-type>
	
Example:
	mae-promote-version ide ide-core/mac/1.1 stable
	mae-promote-version ide mac ide-core/mac/1.1.135 nightly
	
Notes:

Platform names used by various components and platform names used by the suite don't have to match, so the platform name is specified twice (as <platform> and inside <versionspec>).


mae-create-pack
---------------

Creates a (single) pack out of (all) the files in the specified folder(s). Useful for tweaking the packs manually, but please use this only if you understand all consequences. (See notes below.)

Syntax:

	mae-create-pack <folder> [<folder>]...
	
Notes:

If you want to make a pack out of a specific component, you should do a regular mae-create-tree, mae-pack-tree, mae-create-version sequence. For example, if several suites use Google Collections library, you should run the following commands (*before* you run mae-pack-tree on any component that includes files from Google Collections):

	mae-create-tree google-collections/mac/1.0 ...path...
	mae-pack-tree google-collections/mac/1.0
	mae-create-version google-collections/mac/1.0
	mae-promote-version libraries mac google-collections/mac/1.0 stable
	
However, if you are not satisfied with the packs chosen for your component(s), you can manually build more suitable packs using mae-create-pack (and they will be automatically chosen then, instead of other ones).

Because of unreachable pack pruning, you should not normally use mae-create-pack to pack files that are not referenced by other components.
