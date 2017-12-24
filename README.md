# Distributed file system

Client process takes in the path of a file, then the file is chunked and distributed across chunk servers based on locations given by the Controller Node. Chunk Servers detect alterations to files when a read request for the file is submitted and then request new, unaltered portions of the affected file.

To do: 
* rework behavior when file corruption's detected (request copy from another chunk server instead of failing)
* add number of held chunks as another consideration in addition to free disk space when deciding where files should go

*~~rework file reconstruction so chunks are directly appended to new file~~
