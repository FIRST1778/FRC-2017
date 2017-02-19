#include <FastLED.h>
#define NUM_LEDS 156 //change back to 156
#define DATA_PIN 6
#define BRIGHTNESS 75
CRGB leds[NUM_LEDS];
void setup() { 
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  FastLED.setBrightness(BRIGHTNESS);
}
int offSet=0;
void loop() {
  for (int i=offSet; i<(NUM_LEDS+offSet); i+=6) {
    leds[(i+0)%NUM_LEDS] = CRGB( 10, 10, 10);
    leds[(i+1)%NUM_LEDS] = CRGB( 25, 10, 50);
    leds[(i+2)%NUM_LEDS] = CRGB( 25, 0, 150);
    leds[(i+3)%NUM_LEDS] = CRGB( 25, 100, 200);
    leds[(i+4)%NUM_LEDS] = CRGB( 100, 100, 255);
    leds[(i+5)%NUM_LEDS] = CRGB( 255, 255, 255); 
  }
  delay(80);
  FastLED.show();
  offSet=(offSet+1)%6;
}
