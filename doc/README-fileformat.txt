Magic Ecabu file formats
========================


Common line-oriented format keywords
------------------------------------

Many commands use a line-oriented text file format with tab field separators. The first field of a line is a line type code. A line with each code has a fixed format, which can be looked up below.

Note that the exact meaning of each of the line types might differ depending on the context, but the syntax remains the same.

P <pack-sha1> <pack-size>
	pack information
	
B <blob-sha1> <blob-size>
	blob information
	
F <sha1> <size> <time> <attr> <relative-path>
	file information
	
LF <sha1> <size> <time> <attr> <relative-path> <local-path>
	local file information
	
PV <product> <release-type> <version>
	product version

CVB <versionspec>
	a component of the product denoted by the last PV line

 	PV ide stable 1.2
	CV ide-core/mac/1.0.125
	CV ide-ruby/mac/1.0.435
	CV ide-python/mac/1.0.872
	
CV <versionspec> <release-type>
	
C <name> <platform>
	— component (inside a product def)
	
	
CV	ide-ruby/mac/0.0.3	nighly
CV	somelib/all/0.8		stable

PV	ide	stable	0.99
CVB	-ruby/mac/0.0.3
	
