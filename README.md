# code_challenge_android v1.0
======================

## Overview

This is a challenge meant for individuals applying for an Android Developer position at Smashing Boxes.

The purpose of this challenge is to provide a relevant, uniform code example by demonstrating use of the Android platform's core frameworks to implement a simple feature common to many applications -- search.

## Goal

The basic application will consist of the following workflow:

On the initial application startup, if assets/item.csv hasn't been parsed out and stored into a database, run a routine to do so -- all interaction with the database should be through the ContentProvider.  If the user minimizes or closes the application, this routine should still continue until finished.  When finished, if the user is currently viewing the application, a list should be populated with all results (i.e. an empty query should display all items).  Once results are present, a menu item should be displayed to allow the user to provide a search query that will filter the list of displayed results by dispatching an additional query.

Feel free to use as many classes/nested classes as you find necessary to come to a solution.  While the need for a library project is likely unnecessary, if you'd like to use one please contact me at austin@smashingboxes.com so I can approve the library first.  While it is unlikely that I'll disallow the use of a library project, this will allow me to be sure that the purpose of this challenge isn't defeated by its use.


## Ideal Submissions will demonstrate

1.  Usage of each of the core Android framework components (Activity, Service, BroadcastReceiver, and ContentProvider -- or a subclassed implementation) that work together to form a coherent system.
2.  Usage of the ActionBar to recieve the user's search input that updates the ui as the user types.
3.  Knowledge of File parsing, working with Strings, and type conversion.
4.  Knowledge of database creation, insertion, and querying.  Any optimizations performed here are always a plus.
5.  Use of a Shared Preferences object to persist global application state.
6.  Useage of a ListView, Adapter, and user feedback while no results are present.
7.  Usaging of an SQL statement to query search results for both partial and exact textual matches.  Results should be queried through a ContentResolver, and the number of results, as well as the results themselves should be displayed.  


example
-------------------------
    10 Results
    
    Item 1
    Item 2
    Item 3
    
    ...

-------------------------

8.  Use of standard Java naming conventions, Android best practices, and code comments where necessary.

## Submissions

Feel free to completely however much of this challenge as you feel is reasonable since this project is in the early though stages.

Send relevant code as .zip, tar.gz, or link to a public git url and signed .apk file to austin@smashingboxes.com.

