String Processing Algorithms project
====================================

Author: Heikki Aitakangas


== Problem

We have text and some metadata associated with substring
s of that text and
we need to search that text. And for each text match find the associated
metadata.
Eg. we have some text rendered in a web browser and we've stored as metadata
the bounding boxes of the words in the text. When we search that text, we
can then highlight the appropriate region of the screen.


== Implementation

In this project I implemented exact and inexact binary and backwards search
algorithms using a suffix array for the text search. I also implemented an
interval tree for efficient access to the metadata. The implementation is
Java code structured as a Maven project.

The suffix array implementation comes from the sais project at
https://sites.google.com/site/yuta256/sais. I edited their source code only to
move the classes to move them to a package, `sais`.

My own code is in the fi.helsinki.cs.u.aitakang package
(src/main/java/fi/helsinki/cs/u/aitakang/).


=== Exact binary search

The exact binary search implementation is pretty much trivial.

Classes:
- Searches


=== Exact backwards search

The exact backwards search implementation is as shown in the lecture slides. The
implementations of rank and count are fairly naive:

The counts of lesser characters are maintained for the characters with
codepoints in the range [0, max(alphabet)]. This can be very space-inefficient
if the alphabet is sparse, but contains a very high codepoint. It should be
rebuilt as a succinct sparse array.


Rank is based on storing at N character intervals the number of occurrences of
each character up to that point. What is stored at each checkpoint is again an
array of characters [0, max(alphabet)].

A succinct structure would again be better here. Either a rank/select dictionary
for each character listing or the same checkpoint structure, but with succinct
sparse arrays.

Classes:
- BackwardsSearchData
- Searches

=== Inexact searches

The inexact search algorithms are similar to the exact ones, though implemented
as recursive functions instead of an iterative loop. This is necessary due to
the branching at each pattern position, as we have to perform the next step
with different parameters based on each of the possible edit operations: match,
replace, insert, delete.

The search also needs to branch as each character in the text's alphabet must be
processed separately. Let's say we're binary searching, our pattern is 'ada...'
and the current search range contains the suffixes:

  aaa...
  abb...
  aca...

after matching the first 'a', all of them are mismatch for character position 2.
But only the first and last are match for character position 3 and thus we need
to process each character as it's own match / replacement / insertion.

The same problem occurs with backwards search.

Classes:
- Searches


=== Interval tree

The interval tree for metadata is based on the Wikipedia article on Interval
trees. I implemented the centered interval tree variant.

Classes:
- IntervalTree


=== Searching

When a search is made, each of the elements in the resulting suffix array index
range gives the starting point of a text match. Given that starting position
and the length of the match, we can perform a second search in the metadata
interval tree to find the metadata whose intervals overlap with the search
result's.

Classes:
- Main


== Running

First, compile the jar

     mvn package

You can then run the testing main program:

    java -jar target/string-processing-project-0.0.1-SNAPSHOT.jar <text> <metadata> <queries>

For example:

    java -jar target/string-processing-project-0.0.1-SNAPSHOT.jar samples/short samples/short.meta samples/queries.json

