#!/bin/bash

# RXTXRobot Mac OS X Installation Script
# Written by Christopher King


VERSION=`sw_vers -productVersion | awk -F. '{print $2}'`

SAVESPOT='/Library/Java/Extensions/librxtxSerial.jnilib'

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "========================================"
echo "|   RXTXRobot Installer for Mac OS X   |"
echo "========================================"
echo ""
echo "To continue, you must enter your password."
echo "NOTE: It will look like you aren't typing anything.  Just type your password and hit enter."
echo ""
sudo touch aRandomFile
sudo rm aRandomFile
echo ""
if [ "$?" -ne 0 ]; then
	echo "Password Authentication failed.  Please try rerunning this installer."
	exit
fi
echo -e "Access was granted successfully!  Continuing...\n"
echo -n "Installing Java libraries........."

if [ $VERSION -lt 5 ]; then
	JFILE="$SCRIPTDIR/libs/less_leopard.jnilib"
elif [ $VERSION -eq 5 ] || [ $VERSION -eq 6 ]; then
	JFILE="$SCRIPTDIR/libs/leop_snow.jnilib"
elif [ $VERSION -gt 7 ]; then
	JFILE="$SCRIPTDIR/libs/mnt_lion.jnilib"
else
	echo "error\n\nFATAL ERROR: Your Mac OS is not supported.  You have $(sw_vers -productVersion) and this only supports 10.0.x - 10.9.x"
	exit
fi

if [ ! -f "$JFILE" ]; then
	echo "error\n\nFATAL ERROR: The library file \"$JFILE\" could not be found.  Please make sure you kept the \"libs\" folder in this directory"
	exit
fi

if [ -f "$SAVESPOT" ]; then
	sudo rm "$SAVESPOT"
fi


sudo cp "$JFILE" "$SAVESPOT"

echo "done"
echo -n "Fixing permissions................"

curruser=`sudo id -p | grep 'login' | sed 's/login.//'`

if [ ! -d /var/lock ]; then
	sudo mkdir /var/lock
fi

sudo chgrp uucp /var/lock &> /dev/null
sudo chmod 775  /var/lock &> /dev/null

if [ $VERSION -lt 5 ]; then
	if [ ! `sudo niutil -readprop / /groups/uucp users 2> /dev/null | grep "$curruser" &> /dev/null` ]; then
		sudo niutil -mergeprop / /groups/uucp users "$curruser" &> /dev/null
	fi
else
	if [ ! `sudo dscl . -read / /groups/_uucp users 2> /dev/null | grep "$curruser" &> /dev/null` ]; then
		sudo dscl . -append /groups/_uucp GroupMembership "$curruser" &> /dev/null
	fi
fi

echo 'done'

echo -n "Determining 32-bit or 64-bit....."
if [ $(uname -m) == 'x86_64' ]; then
	echo "done"
	echo "You are running a 64-bit machine"
	# XFILE="$SCRIPTDIR/libs/ftdi_x64.dmg"
	XFILE="$SCRIPTDIR/libs/ch34xInstall.pkg"
else
	echo "done"
	echo "You are running a 32-bit machine"
	# XFILE="$SCRIPTDIR/libs/ftdi_x86.dmg"
	XFILE="$SCRIPTDIR/libs/ch34xInstall.pkg"
fi
if [ ! -f "$XFILE" ]; then
	echo "FATAL ERROR: Could not find \"$XFILE\".  Make sure the folder \"libs\" is in this directory."
	exit
fi

echo ""
echo "Follow the instructions to install the FTDI drivers for the Nano."
sudo installer -pkg "$XFILE" -target /
sudo nvram boot-args="kext-dev-mode=1"
# hdiutil detach -quiet "/Volumes/FTDIUSBSerialDriver_v2_2_18"


# XBee...
# echo "Follow the instructions to install the FTDI drivers for the XBee."
# hdiutil attach -quiet -noautoopen -nobrowse "$XFILE" 
# sleep 2
# open -W "/Volumes/FTDIUSBSerialDriver_v2_2_18/FTDIUSBSerialDriver_10_4_10_5_10_6_10_7.mpkg"
# hdiutil detach -quiet "/Volumes/FTDIUSBSerialDriver_v2_2_18"

echo ""
echo ""
# echo "Follow the instructions to install the drivers for the Phidget motors."
# hdiutil attach -quiet -noautoopen -nobrowse "$SCRIPTDIR/libs/Phidget.dmg" &> /dev/null
# sleep 2
# open -W "/Volumes/Phidgets21/Phidgets.mpkg"
# hdiutil detach -quiet "/Volumes/Phidgets21" &> /dev/null
# echo ""

echo "The driver installation has completed!  You may need to restart your computer for it to finish successfully"





