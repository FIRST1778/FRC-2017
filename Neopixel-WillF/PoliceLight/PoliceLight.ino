#include <FastLED.h>
#define NUM_LEDS 156
#define DATA_PIN 6
#define BRIGHTNESS 75
CRGB leds[NUM_LEDS];
void setup() {
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  FastLED.setBrightness(BRIGHTNESS);
}
int Rpos = 0;
int Bpos = 5;
int Save = 5;
int LedRep = NUM_LEDS / 5;
void loop() {
  colB(Bpos);
  colR(Rpos);
  FastLED.show();
  delay(30);
  Save = Rpos;
  Rpos = Bpos;
  Bpos = Save;
  delay(200);

}
//_______________________________
void colR(int Rlo) {
  for (int rep = 0; rep < LedRep; rep++) {
    leds[Rlo] = CRGB::Red;
    leds[Rlo + 1] = CRGB::Red;
    leds[Rlo + 2] = CRGB::Red;
    leds[Rlo + 3] = CRGB::Red;
    leds[Rlo + 4] = CRGB::Red;
    Rlo += 10;
    rep += 1;
  }
}
//___________________________________
void colB(int Blo) {
  for (int rep = 0; rep < LedRep; rep++) {
    leds[Blo] = CRGB::Blue;
    leds[Blo + 1] = CRGB::Blue;
    leds[Blo + 2] = CRGB::Blue;
    leds[Blo + 3] = CRGB::Blue;
    leds[Blo + 4] = CRGB::Blue;
    Blo += 10;
    rep += 1;
  }
}
