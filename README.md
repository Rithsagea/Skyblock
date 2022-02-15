# Skyblock Auction Analyzer

This program gets auctions from hypixel skyblock's REST api, and uses them to predict the prices of future auctions.
There are 3 parts of this program, a Downloader, Analyzer, and Uploader. They can be found in their respectively named
source folders under the Skyblock directory.

TODO: Hypixel updated their api some time ago, so all the serialization tools need to be updated.

## API

This module is used by every part of the program, containing essential data structures and miscellaneous helper methods. The SecureConstants class contains sensitive data. It is empty, as passwords and urls should not be uploaded to the repo.

Hypixel Skyblock items have data stored in their NBT tag. NBTUtil converts raw game data into a more structured ItemData. This includes the type of item, the quantity, and any enchantments it may have. ItemType is a list of every single item of interest hardcoded into an Enum. Probably should replace with something more dynamic in the future.

## Downloader

Contains 2 tasks, one for downloading auction data, and the other for storing it in the database.

Downloaded auctions are in json format, and converted to AuctionFormat before being turned into Auction from the API module.

## Analyzer

This is probably the densest part of the project. Grabs historical auction prices from the database, and then attempts to predict future prices. The calculated trend is then displayed on a dataviewer web page.

Analyzer - Passes values from the database connector to the equations in DataUtil. Generates a trace to be passed to dataviewer
AnalyzerWindow - GUI for user to change analysis settings. Dataviewer client is stored here

### DataUtil

Contains methods for data anlysis, utilizing Holt-Winters Triple Exponential Smoothing and a genetics algorithm for parameters.

Auction prices are fairly volatile, so we have to take a moving average of them to fix empty intervals. Holt Winters is used to calculate  seasonal trends. This method takes into account repeating patterns, as well as linear changes in those patterns. 3 variables, alpha, beta, and gamma are passed into Holt Winters to tune the predictions. There's probably a better way to do this, but for now a genetics algorithm does a random walk to find the parameters which give the smallest MSE.

ty random government website for Holt Winters:
https://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm

## Uploader

Simple script to upload average auction prices to a google sheets. Probably going to delete this, too clunky to work with google apis, literally just use this for avg price: https://auctions.craftlink.xyz/.