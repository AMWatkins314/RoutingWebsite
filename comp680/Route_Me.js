/***o
 * Javascript file for Route_Me.java & Route_Me.html
 * Written by AWatkins
 ****/

var geocoder;
var addresses = [];
var startLoc = "";
var endCoordinates = "";
var formattedStartLoc = "";
var formattedArgument = "";
var numberOfThreads = 0;
var numberOfGenerations = 0;
var numberOfSeconds = 0;
var numberGeolocationsFound = 0;

function processAddresses() {
    $("#processing_fader").css("visibility", "visible");
    // Reset addresses between submissions
    addresses = [];
    var startLocTxt = document.getElementById("startLoc").value;
    var firstLocTxt = document.getElementById("loc_0").value;
    numberOfThreads = Number($("#numThreads").val());
    numberOfSeconds = Number($("#numSeconds").val());
    numberOfGenerations = Number($("#numGenerations").val());
    /* Only process locations if a start address and a first address are entered */
    if (startLocTxt != "" && firstLocTxt != "") {
        var addressElements = document.getElementsByClassName("userLoc");
        if (addressElements != null) {
            for (i = 0; i < addressElements.length; i++) {
                /* Only add non-empty addresses for processing */
                var addr = addressElements[i].value;
                if (addr != "") {
                    addresses.push(addr);
                }
            }
            startLoc = addresses[0];
            geocoder = new google.maps.Geocoder();
            console.log("Processing " + addresses.length + " addresses...");

            for (i = 0; i < addresses.length; i++) {
                var address = addresses[i];
                geocodeAddresses(address);
            }
        } else {
            /* Handle address error here */
            console.log("ERROR: There has been an address error.");
            $("#processing_fader").css("visibility", "hidden");
            var posting = $.post("project/Route_Me");
            posting.done(function (data) {
                $("#google_err")[0].style.display = "none";
                $("#addr_add_err")[0].style.display = "none";
                $("#addr_submit_err")[0].style.display = "inline";
            });
        }
    } else {
        console.log("ERROR: Must enter a starting location and at least one first location.");
        $("#processing_fader").css("visibility", "hidden");
        var posting = $.post("project/Route_Me");
        posting.done(function (data) {
            $("#google_err")[0].style.display = "none";
            $("#addr_add_err")[0].style.display = "none";
            $("#addr_submit_err")[0].style.display = "inline";
        });
    }
}

function geocodeAddresses(address) {
    if (geocoder) {
        /* geocode is an asynchronous function */
        geocoder.geocode({
            'address': address
        }, function (results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                var formattedAddress = results[0].formatted_address;
                var latitude = results[0].geometry.location.lat();
                var longitude = results[0].geometry.location.lng();

                if (address == startLoc) {
                    generateURI(formattedAddress, latitude, longitude, 1);
                } else {
                    generateURI(formattedAddress, latitude, longitude, 0);
                }
            } else if (status === "OVER_QUERY_LIMIT") {
                /* Wait 3 seconds (3000 milliseconds) then try again */
                setTimeout(function () {
                    console.log("OVER_QUERY_LIMIT: Waiting 3 seconds before processing more locations.");
                    geocodeAddresses(address);
                }, 3000);
            } else {
                console.log("ERROR: Geocode was not successful for the following reason: " + status);
                $("#processing_fader").css("visibility", "hidden");
                var posting = $.post("project/Route_Me");
                posting.done(function (data) {
                    $("#addr_add_err")[0].style.display = "none";
                    $("#addr_submit_err")[0].style.display = "none";
                    $("#google_err")[0].style.display = "inline";
                });
            }
        });
    } else {
        console.log("ERROR: geocoder is null");
        $("#processing_fader").css("visibility", "hidden");
        var posting = $.post("project/Route_Me");
        posting.done(function (data) {
            $("#addr_add_err")[0].style.display = "none";
            $("#addr_submit_err")[0].style.display = "none";
            $("#google_err")[0].style.display = "inline";
        });
    }
}

function generateURI(address, latitude, longitude, isStartLoc) {
    if (isStartLoc) {
        /* Store the encoded address, latitude, and longitude to pre-pend at the end so it is always passed as the first argument to Salesman Java */
        /* Store the formatted starting location
         * which will be passed separately in POST so that it can be appended to the Google URL
         * since Salesman Java does not return the route with the starting location also being the ending location
         */
        formattedStartLoc = encodeURI(address) + ' ' + latitude + ' ' + longitude;
        endCoordinates = '/' + latitude + ',' + longitude;
    } else {
        formattedArgument += encodeURI(address) + ' ' + latitude + ' ' + longitude + ' ';
    }
    numberGeolocationsFound++;

    /* If all addresses have been geocoded */
    if (numberGeolocationsFound == addresses.length) {
        /* Pre-pend the starting location which is the same as the end location */
        var tmp = formattedArgument;
        formattedArgument = formattedStartLoc + ' ' + tmp;

        console.log("All " + numberGeolocationsFound + " addresses have been geocoded.");
        console.log("*** formattedData: " + formattedArgument);
        console.log("*** endCoordinates: " + endCoordinates);

        /* Pass data vi hiddenData div using jQuery post */
        var posting = $.post("project/Route_Me", {
            numberOfSeconds: numberOfSeconds
            , numberOfGenerations: numberOfGenerations
            , numberOfThreads: numberOfThreads
            , formattedData: formattedArgument
            , formattedEndLoc: endCoordinates
        });
        var cookie = {
            numberOfSeconds: numberOfSeconds
            , numberOfGenerations: numberOfGenerations
            , numberOfThreads: numberOfThreads
            , formattedData: formattedArgument
            , formattedEndLoc: endCoordinates
        };
        setCookie(JSON.stringify(cookie), 1);
        posting.done(function (data) {
            document.getElementById("hiddenData").innerHTML = "";
            $("#hiddenData").append(data);
        });
    }
}


var locCtr = 0;

function addLocation(parentDiv) {

    /* Only allow a location to be added if a start address and a first address are already entered */
    var startLoc = document.getElementById("startLoc").value;
    var firstLoc = document.getElementById("loc_0").value;
    if (startLoc != "" && firstLoc != "") {
        locCtr++;
        var divNameID = "div_" + locCtr;
        var inputNameID = "loc_" + locCtr;
        var buttonNameID = "remove_loc_" + locCtr;

        var newLocDiv = document.createElement('div');
        newLocDiv.id = divNameID;
        newLocDiv.name = divNameID;

        newLocDiv.innerHTML = "<input type='text' class='userLoc' name='" + inputNameID + "' id='" + inputNameID + "'> <input type='button' name='" + buttonNameID + "' id='" + buttonNameID + "' value='Remove Address' onClick=\"removeLocation('" + divNameID + "');\">";

        document.getElementById(parentDiv).appendChild(newLocDiv);
        console.log("Added Location: " + divNameID);
    } else {
        console.log("ERROR: A starting location and first location must be entered before adding more locations.");
        $("#processing_fader").css("visibility", "hidden");
        var posting = $.post("project/Route_Me");
        posting.done(function (data) {
            $("#google_err")[0].style.display = "none";
            $("#addr_submit_err")[0].style.display = "none";
            $("#addr_add_err")[0].style.display = "inline";
        });
    }
}

function removeLocation(divName) {
    var locDiv = document.getElementById(divName);
    locDiv.parentNode.removeChild(locDiv);
    console.log("Removed Location: " + locDiv.id);
}

function setCookie(value, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();
    document.cookie = "lastrun" + "=" + value + "; " + expires;
    if (document.cookie)
        return true;
}

function getCookie() {
    return JSON.parse(document.cookie.substring(8));
}

function checkForRerun() {
    if (document.cookie) {
        $("#rerun").css("display", "inherit");
    } else {
        $("#rerun").css("display", "none");
    }
}

function rerun() {
    $("#processing_fader").css("visibility", "visible");
    var cookie = getCookie();
    var posting = $.post("project/Route_Me", {
        numberOfSeconds: cookie.numberOfSeconds
        , numberOfGenerations: cookie.numberOfGenerations
        , numberOfThreads: cookie.numberOfThreads
        , formattedData: cookie.formattedData
        , formattedEndLoc: cookie.formattedEndLoc
    });
    posting.done(function (data) {
        document.getElementById("hiddenData").innerHTML = "";
        $("#hiddenData").append(data);
    });
}