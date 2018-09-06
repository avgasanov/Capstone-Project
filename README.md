# Capstone-Project
Udacity Android Nanodegree Capstone Project

 
Typical usage is to press photo button take a photo of your friend’s car. Add timestamp, location and bw filter (after cropping image this feature will be in the bar). Then post it using car plate number. If your friend registered car with that plate number, he will be notified.
You also can go to your profile and register your own car. Whenever anyone take picture of your car and enter your plate number, you will be notified. 
Search works only with exact usernames and plate number entered. About search later in this document..
Architecture.
Activities in the application:
1)	Main activity
2)	Photo activity
3)	Post activity
Photo and post activity is for image capturing and posting. 
Everything else happens in main activity. 
I’ve used FragNav library (https://github.com/ncapdevi/FragNav) to implement fragment navigation within the main activity. There are two fields related to this library in the main activity class: mFragController – this one maintain all stuff related to the fragment navigation and mFragNavBuilder – this is to create the first one when is necessary.  
There are two main method, that I’ve used across the application to interact with FragmentNavigationController: pushFragment and popFragment. The first one adds new fragment on currently active stack of fragments. Say, if you’re in a profile fragment, then new fragment will be placed on top of it. In the profile fragment you instantiate new fragment, which you want to “open” and call onPushFragment to push fragment on stack. If you want “close” currently active fragment then you just call onPopFragment.
There are 4 main fragments related to bottom navigation buttons:
1)	UserProfileFragments – can be instantiated by user id. On creation this fragment loads all user related information and shows:
a.	User profile picture
b.	Username
c.	List of user cars
d.	If current user id has been passed to the fragment when it had been instantiated (using newInstance) then fab button will appear. Fab button is for adding new car to user profile
2)	HomeFragment lists all the photos available in database ordered from new to old ones.
3)	NotificationsFragment lists all user notifications. NotificationsFragment extends BasePagingFragment. 
4)	SearchFragment is for naïve search. Implementing smart search is costly for now. In client side I’m planning to make a simple sql database with single table where each username column corresponds to user unique id. New users could be added to the database by using querying abilities of firebase realtime database and there is no need to implement full sync. If users count don’t exceed 10000 users? It would be just a 1mb database file. It would be possible to implement fast smart search with this database. For future I’m planning using elastic search vm available in google cloud platform.
Listed fragments are similar. So I’ve designed BasePagingFragment which I’ve already used for notification fragment, for the rest of the fragments I’m planning refactoring. Each of the listed fragments has adapter that extends RecyclerView.Adapter. Adapters designed in such way so they support paging with firebase. The methods for this are:
1)	getLastItemId()
2)	getFirstItemId()
3)	nextPage()
4)	insertItemTo()
5)	addItem()
6)	addItemToTop()
getLastItemId and getFirstItemId is for firebase query, they are used to know where to start retrieve older or new elements from firebase realtime database. 
InsertItemTo is common code for addItem and addItemToTop, two former methods just call the first one to show where to insert. Firebase RDB returns elements in reverse order: the old ones first. To show the new ones first, I insert items of each page to the index which corresponds to the item count, before the page started loading into adapter. For example:
First, itemsId list is empty and Page with items [5, 4, 3, 2, 1] loaded. Insertion happens to the index 0:
5
4 5
3 4 5
2 3 4 5
1 2 3 4 5
Then in the fragment which loaded the page adapter’s nextPage() method gets called and insertion index renews to 5. The next page with items [8, 7, 6]:
1 2 3 4 5 8
1 2 3 4 5 7 8
1 2 3 4 5 6 7 8
And so on.
While refreshing the new items must be placed in the beginning of the list. So there is addItemToTop item which calls insertItemTo with insertion index of 0 (beginning of list).
When adapter binds the last view in the list it calls nextPage() of fragment callback. Then fragment knows that it have to load new items from database.
I’ve used cloud functions to maintain database triggers and other stuff that should be maintained in server:
1)	Trigger when user adds new photo
2)	HTPP callable function to maintain likes and like counting
3)	Sample function for thumbnail generation from firebase samples to maintain thumbnail generation. Thumbnails aren’t currently used in application
4)	Trigger for notification node to notify user when events happens
When user adds new photo it posts to the server only: pushkey for photo, his profile information (name, id, photo), and caught car plate number. Then cloud function: searches for cars registered with this plate number, adds pushkey information to that cars, gets users that has been related to this platenumber (registered the plate number as their car) adds that users information to photo and creates notification event and posts it to notification node of the database. Also this cloud function creates several search indexes for future use.
