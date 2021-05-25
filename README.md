# Data Deduplication CS Project

### Description

This program acts as a basic database server with only the functionality to add, retrieve, or delete data files from it. It provides options to utilize 3 different deduplication techniques: Whole File, Fixed Chunk, and Variable Chunk deduplication.

### How to build

Compile all the java files in the repository by typing this command in terminal
 ```
 $ javac *.java
 ```

### How to run

This program relies on the ```Server``` class to run, so to run it you must run:
```
$ java Server
```
The program will prompt you with 3 options asking what type of chunking method you would like to use. The server creates a different folder for each database so you can run the server with all three types but you can't run more than three databases total.

Should you chose to clear a database, you can simply delete the created database folder for that type.

#### Options

> Whole File: No chunking occurs, files are only deduped if they are exactly the same

> Fixed: You are given an option of chunk ```size``` and every added file is chunked and stored in ```size``` bytes chunks

> Variable: You are given an option of an ```average``` chunk size and the program will use a rolling hash algorithm to attempt to chunk the file close to that size while also uniquely chunking data to handle file shifts and data changes

* To add files you must pick option ```1``` from the menu and type in the exact file ```PATH``` (including the extension) from the directory you are in.

* To retrieve a file, you must pick option ```2``` from the menu and type in the exact file ```NAME``` (including the extension).

* To delete a file, you must pick option ```3``` from the menu and type in the exact file ```NAME``` (including the extension).

* To show the file list contained in the database, you must choose option ```4```. This will display all the files added to the database.

* To show how much data is being saved, i.e. Dedup ratio, you must choose option ```5```. This will display how many bytes are actually stored, how many are deduplicated (not physically stored). and the Dedup ratio.

* To restart the database, and clear the file list, you can chose option ```6``` which will delete all associated data in the database. Clones made from retrieve will remain in ```/Downloads/```

* To save the database, you must choose option ```7``` before exiting the program. Failure to do so will ***not*** save the database (cached files will still be stored but no pointers or links will exist to them and you won't be able to retrieve them logically);

## Notes

> Storing the index is as equal of a challenge as figuring out which data to chunk. Since my main goal was deduplication, I simply serialized the databases (which are technically hash tables) to store them persistently. There is surely another better way to do so which is why I did not include index size in the Dedup data. Additionally, deduplicating is only good for massive backups that really have a ton of duplicated data. On a small scale, the data needed to store the index and compute the files outweighs the benefits of the saving a few bytes.

I have included samples to use and modify in the ```/Samples/``` folder. Ideally, samples that you would want to use would be much larger and have a lot more in common than text files since if we aim for 4KB chunks, that would be 4096 characters in a row that are the same in two .txt files.

Nevertheless here are some ways to showcase the deduplication:

* Add both ```Samples/Frankenstein.txt``` and ```Samples/FrankensteinCopy.txt``` to the same Whole File database. Since the two files are exact copies, you will only store one copy on disk and will have a Dedup ratio of 0.5.

* Add both ```Samples/SherlockHolmes.txt``` and ```Samples/SherlockHolmesShifted.txt``` into both a fixed and variable chunk database.
The only difference between those two files are an added few characters at the beginning. Under small chunks (since this is text), variable chunking will recognize the shifting of data at the beginning and will still deduplicate the file. On the other hand, fixed chunking won't and instead will recognize the entire file as different because all the chunks are shifted. This showcases why variable chunking can be better than fixed chunking.

* Add ```data.csv``` to a fixed chunk database with 24 byte chunks, and you can find a deduplication ratio of 23.5%! just for this random teacher data. Because however this data is organized, fixed chunking is actually better than variable chunking which produces only a 13% Dedup ratio.

Sources:
* https://www.gluster.org/deduplication-part-1-rabin-karp-for-variable-chunking/
* http://cs.williams.edu/~jannen/teaching/s21/cs333/meetings/dedup-survey.pdf
