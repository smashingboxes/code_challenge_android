# code_challenge_android v1.0
======================

## Overview

This is a challenge meant for individuals applying for an Android Developer position at Smashing Boxes.

The purpose of this challenge is to provide a relevant, uniform code example by demonstrating use of the Android platform's core frameworks to implement a simple feature common to many applications -- search.

## Goal

The ideal application will consist of the following workflow:

On the initial application startup, if assets/item.csv hasn't been parsed out and stored into a database, run a routine to do so.  If the user minimizes or closes the application, this routine should still continue until finished.  When finished, if the user is currently viewing the application, a list should be populated with all results (i.e. an empty query should display all items).  Once results are present, a menu item should be displayed to allow the user to provide a search query that will filter the list of displayed results by dispatching an additional query. Note:  Query strings should filter on the "Item Description" column of the CSV.

Feel free to use as many Classes/Nested Classes, relevant external gradle libraries, etc that you find necessary to come to a solution.  

## Ideal Submissions will demonstrate

1.  Usage of each of the core Android framework components Activity, Service, BroadcastReceiver, and ContentProvider.
2.  Usage of the ToolBar to receive the user's search input that updates the ui as the user types.
3.  Knowledge of File parsing, and type conversion.
4.  Knowledge of database creation, insertion, and querying.  Any optimizations performed here are always a plus.
5.  Use of a SharedPreferences object to persist global application state.
6.  Useage of a ListView, Adapter, (Bonus: RecyclerView) and user feedback while no results are present.
7.  Use of standard Java naming conventions (Beans Conventions), Android best practices, and code comments where necessary.
8.  *This may differ depending on DB choice* Usage of an SQL statement to query search results for both partial and exact textual matches.  Results should be queried through a ContentResolver, and the number of results, as well as the results themselves should be displayed.


example
-------------------------
    10 Results
    
    Item 1
    Item 2
    Item 3
    
    ...

-------------------------

## Submissions

Feel free to complete as much of this challenge as you feel is reasonable for your skill level.

Send relevant code source as .zip, tar.gz, or link to a git url accompanied with a signed .apk file to anya@smashingboxes.com.

