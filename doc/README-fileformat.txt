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
	
