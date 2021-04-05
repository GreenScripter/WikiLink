# WikiLink
A tool for finding paths between Wikipedia articles.  
# Requirements
Setup for this tool requires around 100 GB of free disk space though the resulting database is closer to 650 MB.  
A decent amount of memory is also required, with about 4 GB of heap required for a smooth run.  
# Setup  
In order to run this tool you need to download a wikipedia dump, instructions: https://en.wikipedia.org/wiki/Wikipedia:Database_download  
This tool is designed to use the English Wikipedia XML database dump, which is created approximately once per month.  
The XML dump must be extracted into the folder that WikiLink will be run from and named `output.xml`  
Running the NumDataExtractor class will extract the links from the wikipedia dump, resolve all redirect pages, clean out any dead links and finally generate the file used by WikiLink called `numericalIndex`  
Once a step has completed you can delete the previous step's database file if you need the space immeadiately, though if something were to go wrong this would make potentially correcting the error much harder.  
The only file needed to actually run WikiLink is the 'numericalIndex' file generated at the end.  
# Running
Running the NumWikiLink file brings up a simple command line interface for WikiLink. A prompt will appear after several secodns when the database loads.
Names of articles are case sensitive except for the first letter and must be spelled exactly as they appear in the wikipedia dump, usually the same way they currently are on wikipedia.  
Commands:  
path - Find a single shortest path between two wikipedia articles.  
paths - Find every shortest path between two wikipedia articles.  
depth - Find the deepest optimal route from a starting article. This command will give a pretty similar route for most starting points since there are long groups of article that form a chain.  
pathpast - Find an optimal path between two articles with a stopping point in the middle.  
pathspast - Find every optimal path between two articles with a stopping point in the middle.  
order - Define the print out order of paths when using commands that have multiple results. Choices are simplest, alphabetical and reverse alphabetical. Simplest is the path with the shortest entries on average and is the default.  
