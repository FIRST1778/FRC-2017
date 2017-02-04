all:	chillOutObjTracker calibrateTracker

clean:
	rm -f chillOutObjTracker calibrateTracker

chillOutObjTracker: chillOutObjTracker.cpp
	g++ -o chillOutObjTracker chillOutObjTracker.cpp `pkg-config opencv --cflags --libs` -std=gnu++11  -lntcore -lv4l2

calibrateTracker: calibrateTracker.cpp
	g++ -o calibrateTracker calibrateTracker.cpp `pkg-config opencv --cflags --libs` -std=gnu++11  -lntcore -lv4l2
