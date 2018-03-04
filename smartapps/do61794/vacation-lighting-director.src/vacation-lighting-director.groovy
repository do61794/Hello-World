/**
 * Vacation Lighting Director
 * 
 * Version 2.6 -  rewrote scheduleCheck routine: eliminated the allOK subroutine, created logic that if in the correct mode will always dschedule 		  
 *				  the scheduleCheck routine to run at the next appropriate day and time.
 *
 * 				  Adapted from Version 2.4 of Tim Slagle's Vacation Lighting Directdapted
 *                Source code can be found here: https://github.com/tAdapted from Version 2.4 of Tim Slagle's Vacation Lightislagle13/SmartThings/blob/master/smartapps/tslagle13/vacation-lighting-director.groovy
 *				  Copyright 2016 Tim Slagle
 *
 * Copyright 2018 David Olson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 */


//****************************************************************************************************************************
//this is setup code that creates the SmartApp UI 
//****************************************************************************************************************************

// Automatically generated. Make future change here.
definition(
    name: "Vacation Lighting Director",
    namespace: "do61794",
    author: "David Olson",
    category: "Safety & Security",
    description: "Randomly turn on/off lights to simulate the appearance of a occupied home while you are away.",
    iconUrl: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png",
    iconX2Url: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png"
)

preferences {
    page name:"pageSetup"
    page name:"Setup"
    page name:"Settings"
    page name: "timeIntervalInput"
}

// Show setup page
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

	return dynamicPage(pageProperties) {
    	section(""){
        	paragraph "This app can be used to make your home seem occupied anytime you are away from your home. " +
			"Please use each of the the sections below to setup the different preferences to your liking. " 
        }
        section("Setup Menu") {
            href "Setup", title: "Setup", description: "", state:greyedOut()
            href "Settings", title: "Settings", description: "", state: greyedOutSettings()
            }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "Setup" page
def Setup() {

    def newMode = [
        name:       	"newMode",
        type:       	"mode",
        title:      	"Modes",
        multiple:   	true,
        required:   	true
    ]
    def switches = [
        name:       	"switches",
        type:       	"capability.switch",
        title:      	"Switches",
        multiple:   	true,
        required:   	true
    ]
    
    def frequency_minutes = [
        name:       	"frequency_minutes",
        type:       	"number",
        title:      	"Minutes?",
        required:	true
    ]
    
    def number_of_active_lights = [
        name:       	"number_of_active_lights",
        type:       	"number",
        title:      	"Number of active lights",
        required:	true,
    ]
    
    def pageName = "Setup"
    
    def pageProperties = [
        name:       "Setup",
        title:      "Setup",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

		section(""){            
                    paragraph "In this section you need to setup the deatils of how you want your lighting to be affected while " +
                    "you are away.  All of these settings are required in order for the simulator to run correctly."
        }
        section("Simulator Triggers") {
                    input newMode  
                    href "timeIntervalInput", title: "Times", description: timeIntervalLabel(), refreshAfterSelection:true
        }
        section("Light switches to turn on/off") {
                    input switches           
        }
        section("How often to cycle the lights") {
                    input frequency_minutes            
        }
        section("Number of active lights at any given time") {
                    input number_of_active_lights           
        }    
    }
    
}

// Show "Setup" page
def Settings() {

    def falseAlarmThreshold = [
        name:       "falseAlarmThreshold",
        type:       "decimal",
        title:      "Default is 2 minutes",
        required:	false
    ]
    def days = [
        name:       "days",
        type:       "enum",
        title:      "Only on certain days of the week",
        multiple:   true,
        required:   false,
        options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    ]
    
    def pageName = "Settings"
    
    def pageProperties = [
        name:       "Settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]
    
    def people = [
        name:       "people",
        type:       "capability.presenceSensor",
        title:      "If these people are home do not change light status",
        required:	false,
        multiple:	true
    ]

    return dynamicPage(pageProperties) {

		section(""){              
                    paragraph "In this section you can restrict how your simulator runs.  For instance you can restrict on which days it will run " +
                    "as well as a delay for the simulator to start after it is in the correct mode.  Delaying the simulator helps with false starts based on a incorrect mode change."
        }
        section("Delay to start simulator") {
                    input falseAlarmThreshold
        }
        section("People") {
        			paragraph "Not using this setting may cause some lights to remain on when you arrive home"
                    input people            
        }
        section("More options") {
                    input days
        } 
    }   
}

def timeIntervalInput() {
	dynamicPage(name: "timeIntervalInput") {
		section {
			input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
			if (startTimeType in ["sunrise","sunset"]) {
				input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "starting", "time", title: "Start time", required: false
			}
		}
		section {
			input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
			if (endTimeType in ["sunrise","sunset"]) {
				input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "ending", "time", title: "End time", required: false
			}
		}
	}
}

def installed() {
	log.debug "Enter installed Suboutine"
	initialize()
}

def updated() {
	log.debug "Enter updated Suboutine"
  	unsubscribe();
  	unschedule();
  	initialize()
}

def initialize(){
	log.debug "Enter initialize Suboutine"
    
	if (newMode != null) {
		subscribe(location, modeChangeHandler)
    }
    if (starting != null) {
    	schedule(starting, modeChangeHandler)
    }
    log.debug "*** Installed with settings: ${settings}"
}

//sets complete/not complete for the setup section on the main dynamic page
def greyedOut() {
	log.debug "Enter greyedOut Suboutine"
	def result = ""
    if (switches) {
    	result = "complete"	
    }
    result
}

//sets complete/not complete for the settings section on the main dynamic page
def greyedOutSettings() {
	log.debug "Enter greyedOutSettings Suboutine"
	def result = ""
    if (people || days || falseAlarmThreshold ) {
    	result = "complete"	
    }
    result
}

private timeIntervalLabel() {
	log.debug "Enter timeIntervalLabel Suboutine"
	def start = ""
	switch (startTimeType) {
		case "time":
			if (ending) {
            	start += hhmm(starting)
            }
			break
		case "sunrise":
		case "sunset":
        	start += startTimeType[0].toUpperCase() + startTimeType[1..-1]
			if (startTimeOffset) {
				start += startTimeOffset > 0 ? "+${startTimeOffset} min" : "${startTimeOffset} min"
			}
			break
	}

    def finish = ""
	switch (endTimeType) {
		case "time":
			if (ending) {
            	finish += hhmm(ending)
            }
			break
		case "sunrise":
		case "sunset":
        	finish += endTimeType[0].toUpperCase() + endTimeType[1..-1]
			if (endTimeOffset) {
				finish += endTimeOffset > 0 ? "+${endTimeOffset} min" : "${endTimeOffset} min"
			}
			break
	}
	start && finish ? "${start} to ${finish}" : ""
}


//****************************************************************************************************************************
//main logic starts here, when the mode is set to Vacation (or whatever mode user selected during setup) from some other mode, 
//this routine schedules the scheduleCheck routine to run after false alarm time period has expired
//****************************************************************************************************************************

//this routine is triggered by a change in Moke, it then schedules the initial run of scheduleCheck routine
def modeChangeHandler(evt) {
	log.trace ("Enter modeChangeHandler Suboutine")
	def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 2 * 60  
    runIn(delay, scheduleCheck)
}


//main logic routine to evaluate Mode and then schedule next run or unschedule next run of scheduleCheck routine
def scheduleCheck(evt){
	log.trace ("Enter scheduleCheck routine")
    
    //to eliminate multiple iterations through the restriction routines we set our boolean variables here
    //we check four restrictions: Mode, Day of Week, Time Period, and Presense Sensors
    def modeResult = getModeOk()
    def daysResult = getDaysOk()
    def timeResult = getTimeOk()
    def homeResult = getHomeIsEmpty()
   
    if (modeResult){
    	//we are in the CORRECT mode, we MAY make something happen, but will DEFINETLY schedule scheduleCheck routine to run when the frequency demands it
        if (daysResult && timeResult && homeResult) {
        	//we turn off lights, we turn on the next set of random lights, and schedule the scheduleCheck routine to run again
        	log.trace("*** Day, Time and Presense OK, Running . . . ")
        
        	// turn off all the switches
        	switches.off()
        
        	// grab a random switch
        	def random = new Random()
        	def inactive_switches = switches
        	for (int i = 0 ; i < number_of_active_lights ; i++) {
            	// if there are no inactive switches to turn on then let's break
            	if (inactive_switches.size() == 0){
                	break
            	}
            
            	// grab a random switch and turn it on
            	def random_int = random.nextInt(inactive_switches.size())
            	inactive_switches[random_int].on()
            
            	// then remove that switch from the pool of off switches that can be turned on
            	inactive_switches.remove(random_int)
        	}
        
        	// because we are in the correct day and time period, we schedule scheduleCheck routine to run when the frequency demands it
        	schedule("0 0/${frequency_minutes} * 1/1 * ? *", scheduleCheck)
        }
        
        else if(daysResult && timeResult) {
        	//someone is home, lets not cycle any lights, but we will schedule scheduleCheck routine when the frequency demands it
            log.trace("*** Someone is Home, sheduleCheck routine has been scheduled")
        	schedule("0 0/${frequency_minutes} * 1/1 * ? *", scheduleCheck)
        }
        
         else if(daysResult) {
        	//someone May be home, but for sure we are NOT in the user's time period, so we schedule scheduleCheck routine to run when the start time demands it
            log.trace("*** Out of the defined time period, scheduleCheck routine has been scheduled")
        	def start = timeWindowStart()
            runOnce(start, scheduleCheck)
        }
        
        else {
        	//someone MAY be home, we MAY not be in the user's time period, but for sure we are NOT in one of the user's selected days
            //we schedule scheduleCheck routine to run when the day and start time demands it
            log.trace "*** Out of the defined day of week, scheduleCheck routine has been scheduled"
            
            //get the start time
            def startTimeDate = timeWindowStart()
            log.trace ("*** startTimeDate: $startTimeDate")
            
            //get the number of days to offset from current date
            int offsetDays = getOffsetDays()
            log.trace ("Days to Offset: $offsetDays")
            
            //offset the startTimeDate by tye offset days returned
            def start = startTimeDate + offsetDays
            log.trace ("New schedule date: $start")
            
    		runOnce(start, scheduleCheck)
        }
	}
    
    else {
    	//we are NOT in the correct mode, lets unschedule 
        def result = location.mode
		log.trace "*** Unscheduled, Wrong Mode = $result"
       	unschedule()
    }
} 


private getOffsetDays() {
	//this subroutine is used to determine the number of days to offset from today to schedule the scheduleCheck routine to run
	log.trace ("Entered getOffsetDays routine")
    
    //get the current integer day of the week, Sunday is day 1
    def currentDate = new Date()
	def currentDay = currentDate[Calendar.DAY_OF_WEEK]
    
    //initialize the subroutine result variable
    int result = 0
	
    if (currentDay == 7) {
    	//today is day 7, pretty straight forward, go through the seven days in cronological order and set result variable
        if(days.contains("Sunday")) {result = 1}
        else if(days.contains("Monday")) {result = 2}
        else if(days.contains("Tuesday")) {result = 3}
        else if(days.contains("Wednesday")) {result = 4}
        else if(days.contains("Thursday")) {result = 4}
        else if(days.contains("Friday")) {result = 6}
        else if(days.contains("Saturday")) {result = 7}
    }
    else {
    	//today is not day 7, we iterate through the for loop to determine number of days to offset from today and set the result variable
        //we must iterate thru in cronological order which requires two For Loops to accomplish
    	
    	int counter = 0
        
        for (int i = currentDay + 1  ; i < 8 ; i++) {
        	//if result has been assigned, no need to continue the For Loop
         	if (result != 0) {break} 
            
        	counter = counter + 1
        
            switch (i) {
            	case 1: if(days.contains("Sunday")) {result = counter}; break;
                case 2: if(days.contains("Monday")) {result = counter}; break;
                case 3: if(days.contains("Tuesday")) {result = counter}; break;
                case 4: if(days.contains("Wednesday")) {result = counter}; break;
                case 5: if(days.contains("Thursday")) {result = counter}; break;
                case 6: if(days.contains("Friday")) {result = counter}; break;
                case 7: if(days.contains("Saturday")) {result = counter}; break;
                default: result = 0; break;
            }
        } 
        
        for (int i = 1 ; i < currentDay + 1 ; 1) {
        	//if result was set in the for loop above or in this For Loop, then exit this for loop
            if (result != 0) {break}
			
            //don't reset the counter between For Loops
        	counter = counter + 1
            
        	switch (i) {
            	case 1: if(days.contains("Sunday")) {result = counter}; break;
                case 2: if(days.contains("Monday")) {result = counter}; break;
                case 3: if(days.contains("Tuesday")) {result = counter}; break;
                case 4: if(days.contains("Wednesday")) {result = counter}; break;
                case 5: if(days.contains("Thursday")) {result = counter}; break;
                case 6: if(days.contains("Friday")) {result = counter}; break;
                case 7: if(days.contains("Saturday")) {result = counter}; break;
                default: result = 0; break;
            }
        }
    }

	return result
}


//****************************************************************************************************************************
//the routines below are used to check restrictions on time period, days of week and presense sentors 
//****************************************************************************************************************************

private getModeOk() {
	def result = !newMode || newMode.contains(location.mode)
	log.trace "*** modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
    def result2 = null
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
        result2 = day
        log.trace "*** Current Day of Week = $result2"
	}
	log.trace "*** daysOk = $result"
	result
}

private getHomeIsEmpty() {
  	def result = true

  	if(people?.findAll { it?.currentPresence == "present" }) {
    	result = false
 	}
	
  	log.trace("*** homeIsEmpty: ${result}")

  	return result
}

private getTimeOk() {
	def result = true
	def start = timeWindowStart()
	def stop = timeWindowStop()
	if (start && stop && location.timeZone) {
		result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
	}
	log.trace "*** timeOk = $result"
	result
}

private timeWindowStart() {
	def result = null
	if (startTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (startTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (starting && location.timeZone) {
		result = timeToday(starting, location.timeZone)
	}
	log.trace "*** timeWindowStart = ${result}"
	result
}

private timeWindowStop() {
	def result = null
	if (endTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (endTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (ending && location.timeZone) {
		result = timeToday(ending, location.timeZone)
	}
	log.trace "*** timeWindowStop = ${result}"
	result
}

private hhmm(time, fmt = "h:mm a") {
	log.trace "Enter hhmm Subroutine"
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

