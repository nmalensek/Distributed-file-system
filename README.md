# Distributed file system

Client process takes in the path of a file, then the file is chunked and distributed across chunk servers based on locations given by the Controller Node. If file corruption occurs, Chunk Servers detect it and request the original portions of the affected file from other Chunk Servers that have copies.

Note: client's currently in "debug" mode; testing file and filepath are hardcoded instead of allowing user input.

To do: 
* ~~rework behavior when file corruption's detected (request copy from another chunk server instead of failing)~~
* add number of held chunks as another consideration in addition to free disk space when deciding where files should go

* ~~rework file reconstruction so chunks are directly appended to new file~~
