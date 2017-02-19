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
  for (int i=offSet; i<(NUM_LEDS+offSet); i+=3) {
    leds[(i+0)%NUM_LEDS] = CRGB::Blue;
    leds[(i+1)%NUM_LEDS] = CRGB::White;
    leds[(i+2)%NUM_LEDS] = CRGB::Teal;
  }
  FastLED.show();
  delay(80);
  offSet=(offSet+1)%3;
} 
