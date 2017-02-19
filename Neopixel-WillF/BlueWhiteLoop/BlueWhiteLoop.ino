#include <FastLED.h>
#define NUM_LEDS 60 //change back to 159
#define DATA_PIN 6
#define BRIGHTNESS 75
#define BLUES 2
CRGB leds[NUM_LEDS];
void setup() { 
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  FastLED.setBrightness(BRIGHTNESS);
}
int offSet=0;
void loop() {
  for (int i=0; i<NUM_LEDS; i+=BLUES+1) {
    for (int j=0; j<BLUES; j++) {
      leds[(i+j+offSet)%NUM_LEDS] = CRGB::Blue;
    }
    leds[(i+BLUES+offSet)%NUM_LEDS] = CRGB::White;
   }
   offSet=(offSet+1)%(BLUES+1);
   FastLED.show();
   delay(80);
}
