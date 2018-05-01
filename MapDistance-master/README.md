# MapDistance - Android App
If app crashes on startup, then be sure that app permissions for storage and location are set to allowed.

### Two modes:

* GPS- uses current GPS location to drop a marker. Drop a marker with the "DROP" button.To use GPS mode, select "GPS" on the mode toggle.
* Manual - user touches screen at desired location to drop a marker. To use manual mode, select "Manual" on the mode toggle. To set a marker, press "LOCK" for each marker. 

**Mode Notes**:  
* The user can place a mix of markers from either mode to form a quadrilateral.
* In manual mode, when a marker has not been placed, the "LOCK" and "CLEAR" buttons are disabled. Once a marker has been placed, the "LOCK" button is enabled, and the mode toggle is disabled. Not until the marker is set with the "LOCK" button does the "CLEAR" button and mode toggle both become enabled.

**To clear the screen**, press the button "CLEAR". This will remove the markers, the square, and the inputs completely.

**After markers are locked**, they can be removed by clicking on them. This only removes the marker rendering. The point will remain for the square drawing. To remove a locked marker point, use "CLEAR".

### Outputs

* Area in meters squared
* List of coordinates
* Lengths between markers

### Limitations

Currently only works with four markers forming a quadrilateral. If a fifth marker is placed, then the previously outlined quadrilateral will be deleted. 

### Permissions

Allow storage and location in app permissions.
