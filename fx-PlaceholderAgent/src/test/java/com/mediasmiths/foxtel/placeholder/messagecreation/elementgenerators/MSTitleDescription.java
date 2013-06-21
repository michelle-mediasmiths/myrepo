package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;

import au.com.foxtel.cf.mam.pms.TitleDescriptionType;
import org.apache.commons.lang.RandomStringUtils;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class MSTitleDescription {

	protected final String[] styles = new String[] { "Movie", "Series",
			"Special" };

	protected final Random random = new Random();

	/**
	 * Creates a valid object of type TitleDescriptionType
	 * 
	 * @param titleDescription
	 * @param titleId
	 * @return titleDescription
	 */
	public TitleDescriptionType validTitleDescription(
			TitleDescriptionType titleDescription) {

		String show = getShowAShowTitle();
		
		titleDescription.setEpisodeTitle(getEpisodeTitle());
		titleDescription.setProgrammeTitle(show);
		titleDescription.setProductionNumber(getProductionNumber());
		titleDescription.setEpisodeNumber(getEpisodeNumber());
		titleDescription.setCountryOfProduction(getCountry());
		titleDescription.setYearOfProduction(new BigInteger(""
				+ (random.nextInt(200) + 1900)));
		titleDescription.setStyle(styles[new Random(System.currentTimeMillis())
				.nextInt(styles.length)]);
		titleDescription.setShow(show);
		titleDescription.setSeriesNumber(new BigInteger(RandomStringUtils
				.randomNumeric(1)));

		return titleDescription;
	}

	private String getCountry() {
		 String[] isoCountries = Locale.getISOCountries();
		 String isoCode = isoCountries[random.nextInt(isoCountries.length)];
		 return new Locale("", isoCode).getDisplayCountry();
	}

	private BigInteger getEpisodeNumber() {
		return new BigInteger(RandomStringUtils
				.randomNumeric(2));
	}

	private String getProductionNumber() {
		return UUID.randomUUID().toString();
	}

	public String getShowAShowTitle() {
		return showTitles[random.nextInt(showTitles.length)];
	}

	public String getEpisodeTitle() {
		return episodeTitles[random.nextInt(episodeTitles.length)];
	}

	//from http://en.wikipedia.org/wiki/List_of_Australian_television_series
	private String[] showTitles = new String[] { "At the Movies",
			"First Tuesday Book Club", "Message Stick All for Kids",
			"Bambaloo", "N-View", "Behind the News", "Dance Academy",
			"Kids Choice Awards", "Nickelodeon At The Movies", "Dead Gorgeous",
			"Go Go Stop", "Good Game: Spawn Point", "Hi-5", "It's Academic",
			"Kids' WB", "Kitchen Whiz", "Lab Rats Challenge",
			"Lightning Point", "Match It", "Play School", "Pyramid",
			"Saturday Disney", "Spit It Out", "Studio 3", "Time Trackers",
			"Toasted TV", "Totally Wild", "Toybox",
			"William & Sparkles' Magical Tales", "What Do You Know?",
			"Wurrawhy Australia's Funniest Home Videos", "Gruen Sweat",
			"Gruen Planet", "Lowdown", "Santo, Sam and Ed’s Sports Fever!",
			"Woodley 60 Minutes", "The Bolt Report", "7.30",
			"ABC News Breakfast", "Asia Pacific Focus", "Australian Story",
			"Catalyst", "A Current Affair", "Breakfast", "Dateline",
			"Foreign Correspondent", "Four Corners", "Inside Business",
			"Insiders", "Insight", "Landline", "Lateline",
			"The Melbourne Report", "Question Time", "The Contrarians",
			"Paul Murray Live", "Agenda", "The Nation with David Spears",
			"Living Black", "Media Watch", "Meet the Press", "The Project",
			"Stateline", "Showdown With Peter Van Olsen", "Australian Agenda",
			"Saturday Agenda", "Sunday Night", "Sunrise", "Today",
			"Today Tonight", "Weekend Breakfast", "Weekend Sunrise",
			"Weekend Today", "World Watch",
			"Your Business Success Global Village",
			"Tough Nuts: Australia's Hardest Criminals",
			"Tony Robinsons Time Walks Conspiracy 365",
			"Miss Fisher’s Murder Mysteries", "Offspring",
			"Packed to the Rafters", "Puberty Blues", "Rake",
			"Winners and Losers", "House Husbands Air Ways", "Bondi Rescue",
			"Bondi Vet", "Border Security: Australia's Front Line",
			"The Force: Behind the Line", "Highway Patrol",
			"Recruits: Paramedics", "RBT", "Who Do You Think You Are?",
			"The World's Strictest Parents", "Young Doctors",
			"The Zoo Deal or No Deal",
			"Millionaire Hot Seat Randling Alive and Cooking",
			"Better Homes and Gardens", "The Boat Show", "The Car Show",
			"Compass", "Coxy's Big Break", "Creek to Coast", "Escape with ET",
			"Everyday Gourmet with Justine Schofield", "Fishing Australia",
			"Food Lovers' Guide to Australia", "Food Safari",
			"Gardening Australia", "Getaway", "Good Chef Bad Chef",
			"Good Game", "Ra", "The Great South East", "Hook, Line and Sinker",
			"Hot Property", "Huey's Kitchen", "Level 3", "Melbourne Weekender",
			"Mercurio's Menu", "My Family Feast", "Poh's Kitchen",
			"Queensland Weekender", "Ready Steady Cook", "River To Reef",
			"Selling Houses Australia", "Location Location Location Australia",
			"Grand Designs Australia", "Sydney Weekender",
			"Talk to the Animals", "Vasili's Garden", "Chartbusting 80s",
			"Eclipse Music TV", "Live 'N Local Up Close", "The Loop",
			"MTV Hits Weekly Hot30 Countdown", "Noise TV", "Rage", "RocKwiz",
			"The X Factor", "Turn It Up! ABC News", "A Current Affair",
			"Behind the News", "The Business", "NBN News", "Nine News",
			"Nine's Early Morning News", "Nine's Morning Newshour",
			"Nine's Afternoon News", "Nine News: First at Five", "Q&A",
			"The Project", "SBS World News", "Seven News", "Seven Early News",
			"Seven Morning News", "Seven News At 4:30", "Today Tonight",
			"Ten News", "Ten News at Five", "WIN News", "News Day",
			"First Edition", "Sky National News", "News Night",
			"News Now The Amazing Race Australia", "Australia's Got Talent",
			"Australia's Next Top Model", "Football Superstar",
			"Beauty and the Geek Australia", "Big Brother",
			"The Biggest Loser", "The Block",
			"The Celebrity Apprentice Australia", "Dancing with the Stars",
			"Dinner Date", "Don't Tell the Bride",
			"Dating In The Dark Australia", "The Farmer Wants a Wife",
			"Four Weddings", "I Will Survive", "MasterChef Australia",
			"My Kitchen Rules", "Please Marry My Boy",
			"Project Runway Australia", "The Shire", "The Stafford Brothers",
			"The Voice", "WAG Nation Home and Away",
			"Neighbours Does not include sport broadcasts themselves",
			"ABC Sport", "AFL Game Day", "The Footy Show",
			"The Sunday Footy Show", "The Back Page", "Before the Game",
			"The Cricket Show", "Cycling Central", "The FIFA World Cup Show",
			"Footy Classified", "The Game Plan", "In Pit Lane",
			"The Lawn Bowls Show", "Nine's Wide World of Sports",
			"The Footy Show", "The Sunday Footy Show", "The Golf Show",
			"Inside Cricket", "Inside Rugby", "Offsiders", "NRL on FOX",
			"Sportsline", "Pardon the Interruption Australia", "SBS Sport",
			"Seven Sport", "Speedweek", "Santo, Sam and Ed’s Sports Fever!",
			"Ten Sport", "The Sunday Roast", "Total Football",
			"The World Game", "V8Xtra",
			"Wide World of Sports Adam Hills In Gordon Street Tonight",
			"The Bolt Report", "Can of Worms", "Mornings", "The Morning Show",
			"National Press Club Address", "The Project", "Rove LA",
			"Talking Heads", "20 to 1 The Arts Show", "The Bazura Project",
			"Critical Mass", "Fashionista", "The Movie Show",
			"Sunday Arts Active Kidz", "Adventure Island",
			"The Adventures of Bottle Top Bill and His Best Friend Corky",
			"The Adventures of Blinky Bill",
			"The Adventures of Long John Silver", "The Adventures of Sam",
			"The Adventures of the Bush Patrol", "Adventures of the Seaspray",
			"Adventures on Kythera", "The Afternoon Show",
			"Agro's Cartoon Connection", "Alexander Bunyip's Billabong",
			"A*mazing", "Andra",
			"Arthur! and the Square Knights of the Round Table",
			"Backyard Science", "Bananas In Pyjamas", "The Big Arvo",
			"Bindi the Jungle Girl", "Blue Water High", "Castaway",
			"Challenger", "Cheez TV", "C'mon Kids", "Couch Potato",
			"Crash Zone", "The Curiosity Show", "Cybergirl", "Deadly",
			"Dex Hamilton: Alien Entomologist", "Dogstar",
			"Don't Blame The Koalas", "Double Trouble", "Driven Crazy",
			"The Early Bird Show", "The Eggs", "The Elephant Princess",
			"Elly & Jools", "Escape from Jupiter",
			"Escape of the Artful Dodger", "Escape from Scorpion Island",
			"Fatty and George", "Fat Cat and Friends", "Fergus McPhail",
			"Feral TV", "The Ferals", "The Finder", "Five Times Dizzy",
			"The Flying Dogtor", "Foreign Exchange",
			"Fredd Bear's Breakfast-A-Go-Go", "G2G",
			"The Genie From Down Under", "The Gift", "The Girl from Tomorrow",
			"Girl TV", "Golden Pennies", "Guinevere Jones", "A gURLs wURLd",
			"H2O: Just Add Water", "Halfway Across the Galaxy and Turn Left",
			"Happy Go 'Round", "The Henderson Kids", "Here's Humphrey",
			"Holly's Heroes", "Horace and Tina", "Hills End", "Hunter",
			"Infinity Limited", "In the Box", "Johnson and Friends",
			"Kangaroo Creek Gang", "Kelly", "Kids Only ! - KO!",
			"The Kingdom of Paramithi", "Lift Off",
			"Lil' Elvis and the Truckstoppers", "Lockie Leonard",
			"Magic Circle Club", "Magic Mountain", "Mal.com",
			"Master Raindrop", "Me and My Monsters", "The Miraculous Mellops",
			"Mirror, Mirror", "Mirror, Mirror II", "Misery Guts",
			"Mission Top Secret", "Mortified", "Mr. Squiggle",
			"The Music Shop", "My Place", "The Nargun and the Stars",
			"The New Adventures of Blinky Bill", "New MacDonald's Farm",
			"Noah and Saskia", "Ocean Girl", "Ocean Star", "Old Tom",
			"Outriders", "Out There", "Parallax", "Petals", "Pig's Breakfast",
			"Pirate Islands", "Playhouse Disney",
			"Professor Poopsnagle's Steam Zeppelin", "Pugwall", "Puzzle Play",
			"Romper Room", "Return to Jupiter", "Round the Twist",
			"The Rovers", "The Saddle Club", "Sarvo", "Saturdee",
			"Scooter: Secret Agent", "Secret Valley", "Short Cuts",
			"Ship to Shore", "Shirl's Neighbourhood", "Silversun", "The Shak",
			"The Shapies", "Sharky's Friends", "Simon Townsend's Wonder World",
			"Skippy the Bush Kangaroo", "Skippy: Adventures in Bushtown",
			"Sky Trackers", "The Sleepover Club", "Snake Tales",
			"Smugglers Beware", "Snobs", "Spellbinder",
			"Spellbinder 2: Land of the Dragon Lord", "Stormworld",
			"The Stranger", "Streetsmartz", "Sugar and Spice",
			"The Terrific Adventures of the Terrible Ten and The Ten Again",
			"Thunderstone", "Tracey McBean", "Trapped", "The Wayne Manifesto",
			"The Upside Down Show", "ttn", "What's Up Doc?", "The Wiggles",
			"Wicked Science", "Wombat", "Wormwood", "Worst Best Friends", "Y?",
			"Zoo Family 30 Seconds", "Acropolis Now",
			"The Adventures of Lano and Woodley", "All Aussie Adventures",
			"AFHV: World’s Funniest Videos", "All Together Now", "Angry Boys",
			"Are You Being Served?", "At Home With Julia",
			"The Aunty Jack Show", "Australia You're Standing In It",
			"BackBerner", "Bad Cop, Bad Cop", "Barley Charlie",
			"Ben Elton Live From Planet Earth", "Big Bite", "The Big Gig",
			"The Big Schmooze", "Big Girl's Blouse", "Birds in the Bush",
			"Blah Blah Blah", "The Bob Morrison Show", "Bobby Dazzler",
			"Bogan Pride", "Brass Monkeys", "Chandon Pictures",
			"The Chaser Decides", "The Chaser's War on Everything",
			"Chaser News Alert", "Chop-Socky's The Prison of Art", "CNNNN",
			"The Comedy Company", "Comedy Slapdown", "Comedy Inc.",
			"The Comedy Sale", "Commercial Breakdown", "Corridors of Power",
			"Danger 5", "DAAS Kapital", "The D-Generation", "Daily at Dawn",
			"Dave in the Life", "The Dave & Kerley Show", "Double Take",
			"Double the Fist", "Eagle & Evans", "Effie, Just Quietly",
			"The Election Chaser", "The Eric Bana Show Live", "Fast Forward",
			"Flat Chat", "Flipside", "Friday Night Download", "Frontline",
			"Full Frontal", "Funky Squad", "The Games", "The Gillies Report",
			"The Gillies Republic", "Good News Week", "Good News World",
			"Grass Roots", "The Gruen Transfer", "Hamish & Andy",
			"Hamish and Andy's Gap Year", "Hey Dad..!", "The Hollowmen",
			"House Gang", "Housos", "How the Quest Was Won", "In Harmer's Way",
			"Introducing Gary Petty", "I Rock", "The Jesters", "Jimeoin",
			"John Safran's Race Relations", "Judith Lucy's Spiritual Journey",
			"Just for Laughs", "John Safran vs God",
			"John Safran's Music Jamboree", "The Joy of Sets", "Just Kidding!",
			"Kath and Kim", "Kenny's World", "Kick", "Kidspeak",
			"Kingswood Country", "Kittson Fahey", "Laid",
			"The Last of the Australians", "The Late Show",
			"Lawrence Leung's Choose Your Own Adventure",
			"Lawrence Leung's Unbelievable", "Let Loose Live",
			"Let The Blood Run Free", "The Librarians", "Life Support",
			"Magda's Funny Bits", "The Merrick & Rosso Show", "The Mansion",
			"Mark Loves Sharon", "Marx and Venus", "The Mavis Bramston Show",
			"Merrick and Rosso Unplanned", "The Micallef Program",
			"Micallef Tonight", "The Mick Molloy Show",
			"Mikey, Pubs and Beer Nuts", "Monster House", "Mother and Son",
			"Mulray", "The Naked Vicar Show", "The Nation", "Outland",
			"Newlyweds", "News Free Zone", "Newstopia",
			"The Norman Gunston Show", "One Size Fits All",
			"The Paul Hogan Show", "Pizza", "Pat's Late Night Coffee Revival",
			"The Rasheed Patel Show", "Real Stories",
			"Review with Myles Barlow", "The Ronnie Johns Half Hour", "Rove",
			"Rubbery Figures", "The Rumpus Room with Darren & Brose",
			"Russell Gilbert Live", "The Russell Gilbert Show",
			"Russell Gilbert Was Here!", "The Shambles", "Shock Jock",
			"Sit Down, Shut Up", "SkitHOUSE", "Sleuth 101",
			"Small Time Gangster", "Speaking in Tongues", "Stupid, Stupid Man",
			"Summer Heights High", "Surprise Surprise Gotcha",
			"Swift and Shift Couriers", "Talkin' 'Bout Your Generation",
			"Take That", "Thank God You're Here", "TV Burp", "TwentyfourSeven",
			"Twentysomething", "Unreal Ads", "Unreal TV",
			"The Urban Monkey with Murray Foote", "Very Small Business",
			"We Can Be Heroes: Finding The Australian of the Year",
			"Welcher & Welcher", "Wilfred", "Willing and Abel",
			"Whatever Happened to That Guy?",
			"The Wedge Iron Chef Australia 6.30 with George Negus",
			"The 7.30 Report", "Alan Jones Live", "Business Breakfast",
			"Business Sunday", "Difference of Opinion", "Extra",
			"Face to Face", "FAQ", "George Negus Tonight", "Hadley!",
			"Hemispheres", "Hinch", "Hotline", "Hungry Beast",
			"Lateline Business", "Missing Persons Unit", "Nightline",
			"Quantum", "7 Days", "Real Life", "Sunday", "The Times",
			"This Afternoon", "This Day Tonight",
			"Willesee at 7Bush Tucker Man", "Crime Investigation Australia",
			"Decadence", "Forensic Investigators", "Gangs of Oz",
			"Home Truths", "The Gift", "Inside Australia",
			"Missing Persons Unit", "Outback House", "Parent Rescue",
			"Podlove", "My Space is an Amazing Place", "Storyline Australia",
			"The World Around Us", "World Tales", "Australian Druglords",
			"Australian Families of Crime A Country Practice",
			"A Difficult Woman", "A Thousand Skies", "Above the Law",
			"Academy", "Against the Wind", "The Alice", "All Saints",
			"All the Rivers Run", "All the Rivers Run 2", "All the Way",
			"Always Greener", "Anzacs",
			"Banjo Paterson's The Man From Snowy River", "Bastard Boys",
			"Bed of Roses", "Bikie Wars: Brothers in Arms", "BlackJack",
			"Blue Murder", "Bodyline", "Boney", "Bordertown",
			"Brides of Christ", "Canal Road", "Captain James Cook",
			"Carla Cametti PD", "Cash and Company", "Chances", "Changi",
			"Children's Hospital", "Chopper Squad", "City Homicide",
			"The Circuit", "Cloudstreet", "Dangerous", "Cluedo",
			"Consider Your Verdict", "The Cooks", "Cops L.A.C.", "CrashBurn",
			"Crownies", "The Cut", "The Damnation of Harvey McHugh",
			"The Day of the Roses", "Dirt Game", "The Dirtwater Dynasty",
			"The Dismissal", "East of Everything", "East West 101", "Embassy",
			"Emergency", "Fallen Angels", "Fire", "Fireflies",
			"The Flying Doctors", "For the Term of His Natural Life",
			"The Girl From Steel City", "G. P.", "Going Home",
			"Good Guys Bad Guys", "Halifax f.p.", "The Harp in the South",
			"headLand", "Heartland", "Head Start", "The Hungry Ones",
			"The Incredible Journey of Mary Bryant", "Jessica", "Janus",
			"Killing Time", "Last Man Standing", "Law of the Land",
			"Love Is a Four Letter Word", "Love My Way", "MDA", "Marking Time",
			"McLeod's Daughters", "Medivac", "The Outcasts", "Little Oberon",
			"Poor Man's Orange", "Power Without Glory", "The Potato Factory",
			"Patrol Boat", "The Purple Jacaranda", "Rafferty's Rules",
			"Rain Shadow", "RAN", "Rescue: Special Ops", "Rush", "Rush",
			"Robbery Under Arms", "Satisfaction", "Sea Patrol",
			"Scales of Justice", "Scorched", "SeaChange",
			"The Secret Life of Us", "Secret Men's Business",
			"Shark's Paradise", "The Slap", "Spirited", "State Coroner",
			"Stormy Petrel", "The Story of Peter Grey", "The Straits",
			"The Strip", "Sweet and Sour", "Sword of Honour", "The Surgeon",
			"Tandarra", "Tanamera – Lion of Singapore", "Tangle",
			"The Thorn Birds", "The Timeless Land", "A Town Like Alice",
			"Tricky Business", "Tripping Over", "Twisted Tales", "Two Twisted",
			"Whiplash", "Young Ramsay", "Underbelly",
			"Underbelly: A Tale of Two Cities", "Underbelly: The Golden Mile",
			"Wild Boys", "Cop shows", "Bellamy", "Bluey", "Blue Heelers",
			"Cop Shop", "Division 4", "The Feds", "Homicide", "Jonah",
			"The Link Men", "The Long Arm", "Matlock Police", "Murder Call",
			"Phoenix", "Police Rescue", "Skirts", "Slide", "Small Claims",
			"Solo One", "Special Squad", "Stingers", "Water Rats",
			"White Collar Blue", "Wildside",
			"Young Lions Torvill and Dean's Dancing on Ice AFP",
			"Animal Emergency", "Beyond 2000", "Beyond Tomorrow",
			"Towards 2000", "BIG - Extreme Makeover", "Bush Doctors",
			"The Code", "Crash Investigation Unit", "Family Footsteps",
			"Find My Family", "Fire 000", "Going Places", "Kalgoorlie Cops",
			"Kings Cross ER: St Vincent's Hospital", "Last Chance Surgery",
			"Medical Emergency", "Missing Pieces", "Money",
			"Outback Wildlife Rescue", "Police Files: Unlocked",
			"The Real Seachange", "Recruits", "Royal Flying Doctor Service",
			"RPA", "RSPCA Animal Rescue", "Saving Babies", "Saving Kids",
			"Search and Rescue", "Sudden Impact", "Surf Patrol",
			"Triple Zero Heroes", "This Is Your Life", "Why is it so?",
			"You Saved My Life", "Zumbo 1 vs. 100", "A Question of Sport",
			"ADbc", "All About Faces", "All-Star Squares",
			"Almost Anything Goes", "A*mazing", "Ampol Stamp Quiz",
			"Any Questions?", "Are You Smarter Than a 5th Grader?",
			"Australia's Brainiest", "Battle of the Sexes", "The Better Sex",
			"Bert's Family Feud", "Big Nine", "Big Square Eye",
			"Blankety Blanks", "Blind Date", "Blockbusters",
			"Burgo's Catch Phrase", "Cash Bonanza", "Casino 10",
			"Catch Us If You Can", "The Celebrity Game", "Celebrity Squares",
			"Celebrity Tattletales", "Clever",
			"Coles £3000 Question and Coles $6000 Question", "The Con Test",
			"Concentration", "Crossfire", "The Daryl and Ossie Show",
			"Does Father Know Best?", "Dog Eat Dog",
			"Don't Forget Your Toothbrush", "Double Dare",
			"Double Your Dollars", "Download", "The Dulux Show",
			"EC Plays Lift Off", "The Einstein Factor", "Fairway Fun",
			"Family Bowl Quiz", "Family Double Dare", "Family Feud",
			"The Family Game", "Flashback", "Flashez", "Ford Superquiz",
			"Free for All", "Friday Night Games", "Gambit",
			"The Generation Game", "Generation Gap", "Get the Message",
			"Gladiators", "The Golden Show", "The Gong Show",
			"Great Temptation", "The Great TV Game Show", "Greed",
			"Guess What?", "Have a Go", "Head 2 Head", "High Rollers",
			"Hot Streak", "Hole in the Wall", "I Do I Do", "It Could Be You",
			"It Pays to Be Funny", "It's a Knockout", "I've Got a Secret",
			"Jeopardy!", "Jigsaw", "Joker Poker", "Keynotes",
			"The Krypton Factor", "Let's Make a Deal",
			"Letterbox and $50,000 Letterbox", "Letter Charades",
			"Little Aussie Battlers", "Long Play", "The Love Game",
			"The Main Event", "Man O Man", "The Marriage Game", "The Master",
			"Mastermind", "Match Game", "Match Mates", "Micro Macro",
			"Midnight Zoo", "Million Dollar Chance Of A Lifetime",
			"Million Dollar Wheel of Fortune", "Mind Twist", "The Mint",
			"Minute to Win It", "Money Makers", "My Generation",
			"Name That Tune", "National Bingo Night", "National Star Quest",
			"The Newlywed Game", "Now You See It", "Opportunity Knocks",
			"Out of the Question", "The Oz Game", "Pass the Buck",
			"Perfect Match", "Personality Squares", "Pick a Box",
			"Pick Your Face", "Play Your Cards Right", "Play Your Hunch",
			"Playcards", "Pot Luck", "Pot Of Gold", "Power of 10",
			"Press Your Luck", "The Pressure Pak Show", "Pyramid Challenge",
			"Quest", "The Quiz Kids", "Quiz Master", "Quizmania",
			"Race Around the World", "Ripsnorters Say G'day", "Say When!!",
			"The Singing Bee", "The Squiz", "Strictly Dancing",
			"Search For A Star", "Second Chance", "Shafted",
			"Show Me the Money", "Showcase", "Sleek Geeks", "Spending Spree",
			"Spicks and Specks", "Split Personality", "Spilt Second",
			"Sport in Question", "Star Search", "Stop the Music",
			"Strike It Lucky", "Supermarket Sweep", "Superquiz",
			"Surprise Package", "Talkin' 'Bout Your Generation",
			"Take a Chance", "Take A Letter", "Take The Hint", "Taken Out",
			"Talking Telephone Numbers", "The Up-Late Game Show",
			"Tell the Truth", "Temptation", "Theatre Sports", "The Rich List",
			"Three on a Match", "Tic-Tac-Dough", "Time Masters",
			"The Tommy Hanlon Show", "Total Recall", "Treasure Hunt",
			"The Trivial Video Show", "The Trophy Room", "TV Talent Scout",
			"University Challenge", "Video Village", "Vidiot", "Visquiz",
			"The Weakest Link", "What's It Worth?", "Win Roy and HG's Money",
			"Wipe Out", "Wheel Of Fortune", "Who Dares Wins",
			"Who Wants to Be a Millionaire?", "Who, What And Where?",
			"Would You Believe?", "You May Be Right",
			"You're A Star Auction Squad", "Australia's Best Backyards",
			"Burke's Backyard", "Backyard Blitz", "Can We Help?", "Collectors",
			"The Cook and the Chef", "Costa's Garden Odyssey", "Dirty Jobs",
			"Discover Tasmania", "DIY Rescue", "Do It", "Domestic Blitz",
			"Food 4 Life", "Food Lovers' Guide to Australia", "Fresh",
			"The Great Outdoors", "Gourmet Farmer", "Ground Force",
			"Harry's Practice", "Home Cooked! With Julie Goodwin",
			"House Calls to the Rescue", "Huey's Cooking Adventures",
			"Is Your House Killing You?", "Lonely Planet Six Degrees",
			"Luke Nguyen's Vietnam", "Lyndey and Blair's Taste of Greece",
			"My Home", "The New Inventors", "Our House", "Our Place",
			"Postcards", "Ralph TV", "Renovation Rescue",
			"Room for Improvement", "Second Opinion", "Surprise Chef",
			"Things To Try Before You Die", "You've Got the Job",
			"Your Life on the Lawn", "The 10:30 Slot", "Accent on Youth",
			"The ARIA Music Show", "All Music Video", "Bandstand", "Club 17",
			"Countdown", "GTK", "Hit Parade", "It's Happening",
			"Matt Flinders and Friends", "MTV", "The Music Jungle",
			"Pepsi Live", "Popstars", "Popstars Live", "Recovery",
			"Rock Arena", "Six O'Clock Rock", "So Frenchy, So Chic",
			"So Fresh", "Studio 22", "The 10:30 Slot", "jtv and", "Video Hits",
			"Video Smash Hits", "Whatever",
			"Young Talent Time 10 Years Younger in 10 Days",
			"The $20 Challenge", "The Apprentice Australia",
			"Aussie Queer Eye for the Straight Guy", "Australian Idol",
			"Australia's Perfect Couple", "Australian Survivor", "The Band",
			"Battle of the Choirs", "Being Lara Bingle",
			"Celebrity MasterChef Australia", "The Colony",
			"Celebrity Big Brother Australia", "Celebrity Dog School",
			"Celebrity Overhaul", "Celebrity Survivor", "The Chopping Block",
			"Dreamhome", "Eco House Challenge", "Everybody Dance Now",
			"Excess Baggage", "The Fugitive", "Girlband", "Freshwater Blue",
			"homeMADE", "The Hot House", "House From Hell", "It Takes Two",
			"Junior MasterChef Australia", "Last Chance Learners",
			"The Lost Tribes", "Make Me a Supermodel",
			"MasterChef Australia All-Stars", "The Mole", "My Kid's a Star",
			"My Restaurant Rules", "Neighbours at War", "Nerds FC",
			"Outback House", "Outback Jack", "Planet Cake",
			"Playing It Straight", "Popstars", "Popstars Live",
			"Queer Eye for the Straight Guy", "The Renovators", "The Resort",
			"The Real Seachange", "So You Think You Can Dance Australia",
			"Starstruck", "Starstruck", "Sylvania Waters", "Teen Fit Camp",
			"Top Design Australia", "Top Gear Australia", "Treasure Island",
			"Undercover Boss Australia",
			"Yasmin's Getting Married Farscape Arcade", "Autumn Affair",
			"Bellbird", "The Box", "Breakers", "Carson's Law", "Certain Women",
			"Chances", "Class of 74", "Cop Shop", "A Country Practice",
			"E Street", "Echo Point", "Family and Friends", "Glenview High",
			"Heartbreak High", "Holiday Island", "Hotel Story", "Kings",
			"Motel", "Number 96", "Out of the Blue", "Pacific Drive",
			"Paradise Beach", "Possession", "The Power, The Passion",
			"Prime Time", "Prisoner", "Punishment", "The Restless Years",
			"Return to Eden", "Richmond Hill", "Skyways",
			"Something in the Air", "Sons and Daughters", "Starting Out",
			"The Sullivans", "Taurus Rising", "The Unisexers",
			"Until Tomorrow", "Waterloo Station",
			"The Young Doctors Beyond the Boundary", "The Bounce",
			"Boots N' All", "The Game Plan", "Live and Sweaty",
			"Live and Kicking", "Sam and The Fatman", "Talking Footy",
			"The Fat", "The Fifth Quarter", "One Week at a Time",
			"One Week at a Time", "RPM", "Sports Tonight", "Sportsworld",
			"Thursday Night Live", "Toyota World Sport", "World of Sport",
			"Trackside 9am with David & Kim", "Access 1974",
			"Australia Versus", "Darren & Brose", "David Tench Tonight",
			"The Catch-Up", "The Circle", "Coast to Coast", "Denise",
			"The Don Lane Show", "Enough Rope with Andrew Denton",
			"The Eric Bana Show Live", "Good Morning Australia",
			"Good Morning Australia with Bert Newton", "Greeks on the Roof",
			"Hey Hey It's Saturday", "In Brisbane Today",
			"In Melbourne Tonight", "Kerri-Anne", "Micallef Tonight",
			"New Faces", "Mouthing Off", "Rove", "The Midday Show",
			"The Mike Walsh Show", "The NightCap",
			"O'Loghlin on Saturday Night", "The Panel",
			"Saturday Night Darren & Brose", "The Sideshow",
			"The Simon Gallaher Show", "The Spearman Experiment" };

	// thanks to tvtropes for the list of stock episode titles
	private String[] episodeTitles = new String[] { "Finale ", "Reunion ",
			"Wedding ", "Homecoming ", "Lost and Found", "Runaway ",
			"Double Trouble", "_____ to the Rescue", "Halloween", "Legacy ",
			"Second Chance ", "Party ", "Witness ", "The Gift", "Hero ",
			"Secrets", "Trapped", "Great Expectations", "Family Affair ",
			"Trial ", "Aftermath ", "Til Death Do Us Part ", "Home ",
			"Judgment Day ", "Ties That Bind ", "Escape", "Ghosts",
			"No Place Like Home ", "Return ", "Revelation ",
			"Secrets and Lies", "Blind Date", "Betrayal ", "Home Sweet Home",
			"End Game ", "Hide and Seek", "Anniversary ",
			"Sins of the Father ", "Survivor ", "Father's Day", "Rescue ",
			"Skin Deep", "Christmas", "Showdown ", "Mother's Day", "Dream ",
			"Happy Birthday", "Nightmare ", "Brothers ", "Eye for an Eye ",
			"Survival", "Hit and Run", "My Brother's Keeper", "Power Play ",
			"Teacher's Pet", "A Friend in Need", "Blast from the Past ",
			"All That Glitters", "Arrival ", "Confession ", "The Letter",
			"Manhunt", "Missing", "Strange Bedfellows", "Sanctuary",
			"Double Jeopardy", "Masquerade", "The Secret", "Revenge",
			"Surprise", "Second Time Around ", "Awakening ", "Duel ",
			"Unfinished Business", "The Choice", "Deadline ", "The Test",
			"Blood Money", "Countdown", "Crush ", "Love Hurts", "The Game",
			"Payback", "Resurrection", "Fire ", "My Dinner with _____",
			"Sisters ", "The Search", "The Stranger", "Birthday ", "Crash ",
			"Eye of the Beholder", "Finders Keepers", "The Fugitive",
			"Obsession", "Redemption", "A Star Is Born", "Checkmate",
			"Crossroads", "Invasion ", "Promised Land ", "Trial by Fire",
			"Double Date ", "The Great Escape", "Hunted ", "34	Inheritance ",
			"The Last Temptation of _____", "Love", "The Race", "Sacrifice ",
			"Best Friends", "Crime and Punishment", "Election ", "Fool's Gold",
			"The Other Woman", "Requiem", "Vengeance", "Cry Wolf",
			"Devil You Know ", "Guess Who's Coming to Dinner", "Journey ",
			"The One That Got Away", "Paradise Lost", "The Show Must Go On",
			"End of the World ", "Fear", "Full Circle", "Chase ",
			"Old Friends", "The Ring", "Valentine's Day",
			"Beauty and the Beast", "A Day in the Life", "Faith",
			"Fire and Ice", "The Hunt", "30	Reckoning ", "Time Bomb ",
			"Heroes", "Killer ", "Shadows", "Breakdown", "Enemy Within ",
			"Family", "The Morning After", "Game Over", "Quarantine",
			"Someone to Watch Over Me", "Playing with Fire",
			"The Play's the Thing", "See No Evil", "Trading Places",
			"Truth or Dare", "The Whole Truth", "Do the Right Thing",
			"Fall Out ", "Fame", "Justice", "Mirror, Mirror", "The Storm",
			"Tough Love", "Truth and Consequences", "The Wall", "Angel",
			"Blackmail", "Buried Treasure", "Choices", "Come Fly with Me",
			"Flashback", "Help", "Identity Crisis", "Last Dance ",
			"Love Story", "A Night to Remember", "Snow Job", "Ashes to Ashes",
			"Black Widow ", "Home Alone", "In Sickness and in Health",
			"Leap of Faith", "Money", "Phoenix", "Race Against Time ", "Sex",
			"Bloodlines", "Echoes", "Flight", "Free Fall ", "Heat Wave ",
			"The Key", "No Way Out", "Power", "Under Pressure", "Big Bang ",
			"Changes", "Earthquake", "Forbidden Fruit", "Friends Like These ",
			"High Noon", "Hollywood", "Home Again", "The Hunter",
			"Once Upon a Time", "Rendezvous", "Something Old, Something New",
			"Survival of the Fittest", "Talent Show ", "Wedding Bells",
			"The Accused", "Bad Company", "The Cure", "Day of Reckoning ",
			"Dear Diary", "Father Knows Best ", "Fever", "First Date",
			"Funeral ", "Games People Play", "Ghost Town", "Graduation ",
			"Hello(,) Goodbye", "House of Cards", "A Matter of Life and Death",
			"Nemesis", "The Party's Over", "Sink or Swim",
			"Sleeping with the Enemy", "To Catch A Thief", "Trust Me",
			"The Bridge", "Conspiracy", "The Date", "Divorce ",
			"Eye of the Storm", "Ghost in the Machine", "Hearts and Minds",
			"The Interview", "Killer Instinct ", "The Kiss",
			"Let the Games Begin", "Lost", "To Have and to Hold",
			"When the Bough Breaks", "The Birthday Party", "Blind Faith",
			"Blood Ties", "Boys Will Be Boys", "The Dance", "False Witness",
			"Genesis", "In The Dark", "Inside Out", "No Good Deed ",
			"Prodigal Son ", "_____ Saves the Day", "War", "The Beginning",
			"The Bet", "Cabin Fever", "Coming of Age", "Dead on Arrival ",
			"Dead Reckoning", "Exodus", "The Fight", "First Day ",
			"Follow The Leader", "A Friend Indeed ", "Gimme Shelter",
			"Haunted", "The Job", "Learning Curve ", "Meltdown",
			"Metamorphosis", "Mistaken Identity", "Nobody's Perfect",
			"Pressure", "Rescue Me", "Stranded", "Suspect ", "Underground",
			"Viva Las Vegas", "Back To Basics", "Blindside ", "The Contest",
			"Do No Harm ", "Out of the Blue", "The End", "Graduation Day",
			"Heart of the Matter ", "Help Wanted", "Holiday",
			"Message in a Bottle", "Moving Day", "Rest in Peace ",
			"Turning Point ", "Aftershock ", "All's Fair", "Asylum",
			"Bon Voyage", "Cat and Mouse", "Dead or Alive",
			"Fear And Loathing ", "Fear of Flying", "Future Shock",
			"Happily Ever After", "Homeward Bound", "The Lie", "Opening Night",
			"Out of Time", "Partners in Crime", "Search and Rescue",
			"Sweet Dreams", "The Sound of Silence ", "Twilight",
			"The Big Sleep", "Carnival", "Chameleon ", "Consequences",
			"Crossfire", "Destiny", "Dream Lover", "The Ex Files ",
			"Hair Today, Gone Tomorrow", "The Judge", "Magic",
			"Nowhere to Run", "Once Bitten", "The Quest", "Rivals", "Showtime",
			"Vacation", "About Face", "And Baby Makes Three", "Animals",
			"Brave New World", "Collision", "Dead End", "Dead Ringer",
			"Double Cross", "Fatal Attraction ", "He Said, She Said",
			"Illusion ", "Into the Fire", "The Long Way Home", "Panic",
			"Rules of the Game", "Shattered", "Silent Night", "Squeeze Play",
			"Still Waters", "Storm Warning", "Temptation", "Time",
			"Affairs of the Heart", "Baptism by Fire ", "The Big Game",
			"Blood", "Charlie", "Chaos", "Dad", "Dangerous Liaisons", "Demons",
			"Doomsday", "Eclipse", "Fight or Flight", "Forever Young",
			"Friend or Foe", "The Girl Next Door", "Ice", "Intervention",
			"Into the Woods", "I've Got You Under My Skin ", "Journey's End",
			"Leader of the Pack", "A Little Learning", "The Lottery",
			"Mind Over Matter", "An Officer and a Gentleman ", "One",
			"One on One", "Out of Sight", "The Quality of Mercy",
			"The Road Not Taken", "Shades of Gray ", "Shadow Play ",
			"Star Crossed", "A Tale of Two Cities", "War Games ", "Abandoned",
			"All Fall Down", "As Time Goes By", "Boiling Point",
			"The Boy Next Door", "The Cage", "The Car", "Chain Reaction",
			"Crash and Burn", "The Day After", "Fall From Grace",
			"Fallen Angel", "Final Countdown ", "Fish Out of Water", "The Fix",
			"Fly Away Home", "Framed", "Freedom", "Gone But Not Forgotten",
			"Happy Anniversary", "Heartbeat", "The Long Night",
			"Love on the Rocks", "One of Us", "Pregnancy", "Prodigy ",
			"Rebellion", "Shadow ", "S.O.S.", "Talk Show ", "Alone", "Avatar",
			"The Big Squeeze", "Black and White", "Born To Run",
			"Borrowed Time", "Broken", "The Bully", "Burnin' Down the House ",
			"Descent ", "The Edge", "The Enemy", "For Whom the Bell Tolls",
			"A Friend in Deed", "Head Over Heels", "Home Invasion", "I Do",
			"Justice For All ", "Knock Knock ", "Last Call",
			"The Long Road Home", "New Year's Eve", "Night Shift", "Surf's Up",
			"The Sky is Falling", "Time Machine ", "To Be or Not to Be",
			"24 Hours ", "War and Peace", "What If", "Whisper ", "Windows",
			"After Hours", "After the Fall", "Alter Ego", "And Baby Makes Two",
			"The Big One", "Chain of Command", "Chosen One ",
			"Dream a Little Dream ", "Evolution", "Fear Itself", "First Blood",
			"Goodbye", "Hook, Line and Sinker", "The House",
			"Letter of the Law", "Little White Lies", "Lockdown", "Lucky",
			"Lust", "The Man", "The New Guy", "No Exit", "Prey", "Saturday",
			"Shadow of the Past ", "Sleeper", "Special",
			"Through the Looking Glass", "Time and Tide", "Two For The Road",
			"Victory", "Witch Hunt", "And Baby Makes Four", "Armageddon",
			"Battle ", "Bad Boys", "Chance of a Lifetime", "Chaos Theory",
			"The Curse", "Death", "Do Or Die", "Dream Girl", "Drive",
			"Forever", "Gold", "Grace", "Gravity", "The Greater Good",
			"Initiation", "Inside Job", "Life After Death", "The Locket",
			"Paradise", "Paranoia", "The Prom", "Prom Night",
			"Public Relations", "Rain", "The Road", "Rose",
			"Save the Last Dance for Me", "Silence", "Something in the Air",
			"Suffer the Little Children", "Sunday", "Texas", "The Truth",
			"All Good Things ", "All's Well That Ends Well", "The Bank Job",
			"Blue", "Boiling Point", "Born To Be Wild",
			"Breaking and Entering", "Burn, Baby, Burn", "Carpe Diem",
			"Dear John", "Encounter", "Gold", "Guys and Dolls",
			"If Looks Could Kill", "Kindness of Strangers ",
			"Long Live the King", "Monday", "The Oath", "Out of the Ashes",
			"Peer Pressure", "Phantom ", "Rock Bottom", "The Invisible Man",
			"The Tower", "War Stories", "Memento Mori", "Apocalypse ",
			"The Big Race", "Born To Be Mild", "Chosen ", "Class Reunion",
			"Danger", "Darkness Falls ", "Day One", "Deus Ex Machina",
			"The Dogs of War", "Don't Look Back", "Enigma", "Extreme Measures",
			"Falling", "Family Man", "First Blood", "First Kiss", "Hat Trick",
			"A Knight to Remember", "Lessons", "Lightning",
			"Lost In Translation", "Not Fade Away", "Robot", "Skin",
			"A Stitch in Time", "Tabula Rasa", "Three", "Thursday",
			"Time Out of Mind", "Tomorrow", "Transformation", "Triple Threat",
			"Tuesday", "Unusual Suspects ", "Werewolf", "8	White Rabbit",
			"Yesterday", "Zero", "All's Fair in Love and War", "The Big Day",
			"The Big Sleep Over", "Black Hole", "7	Butterfly Effect ",
			"The Cave", "Company Man ", "A Dark and Stormy Night",
			"Dark Side of the Moon", "Deliver Us From Evil", "The Door",
			"Dreamland", "The Eleventh Hour", "Farewell",
			"Game, Set and Match", "Gone with the Wind",
			"The Great Train Robbery", "The Greater Good", "In Deep",
			"The Incident", "It's A Wonderful Life", "Learning to Fly",
			"Legacies", "The Match", "Merry Christmas", "Odyssey ", "Outlaws",
			"Over the Rainbow", "Pier Pressure", "Reign of Terror",
			"Reversal of Fortune", "Salvation", "Selling Out", "Sleep",
			"Sleepwalker", "Two", "The Usual Suspects",
			"Valentine's Day Massacre", "Wednesday", "What About Bob?",
			"Who's on First?", "The Window", "All Hell Breaks Loose ",
			"Adrift", "All About Eve", "April Fool's Day", "April Fools",
			"Beer", "Believe", "Blood Feud", "Booby Trap", "Book of Love ",
			"Breathless", "Bus Stop", "College", "Damned If You Do",
			"Day of The Dead", "D.N.A. ", "Duet", "Employee of the Month",
			"Enemies", "The End of the Beginning", "Friday", "Gambit",
			"The Game Show", "Green", "Hammer Time ", "Home Away from Home",
			"I'm with Stupid", "Kill or Be Killed", "The King is Dead",
			"The Last Day", "Live and Learn", "Muddy Waters", "Nevermore",
			"Nightmares", "Numbers", "Past Lives", "Prime Suspect",
			"Prison Break", "Rain of Terror", "Real Life", "Remembrance",
			"A Star is Torn", "Queen's Gambit", "Unholy Alliance",
			"The Beginning of the End", "The Chaperone", "Culture Shock",
			"Dawn", "Doing Time", "Double Double Toil and Trouble",
			"Fifteen Minutes", "Grace Under Pressure", "Graveyard Shift",
			"In Love and War", "Invisible Woman ", "Life", "Midnight", "Mom",
			"More Things in Heaven and Earth ", "No Rest for the Wicked",
			"Peer Pressure", "Potato", "Red", "Snapshot", "Terminal",
			"The Thin Red Line", "Three Strikes", "Tower of Power",
			"The Vampire", "Vortex", "Wonderland" };

}
