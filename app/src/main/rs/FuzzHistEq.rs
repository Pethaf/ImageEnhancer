#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.example.pethaf.imageenhancer)
#include "rs_debug.rsh"

uint8_t fuzzyLut[256] ={101,101,101,101,101,101,101,101,101,101,101,101,101,101,101,101,101,101,101,
                      101,101,101,101,101,102,102,102,102,102,102,102,102,102,102,102,102,102,102,
                      102,102,102,102,102,102,102,102,102,102,103,103,103,103,103,103,103,103,103,
                      103,103,103,103,103,103,103,103,103,103,103,103,103,103,104,104,104,104,104,
                      104,104,104,104,104,104,104,104,104,104,104,104,104,104,105,106,107,108,109,
                      110,111,112,113,114,116,117,118,119,120,122,123,124,125,127,128,130,131,133,
                      134,136,138,140,142,144,146,148,150,153,156,159,162,166,168,168,168,168,169,
                      169,169,170,170,171,171,172,173,173,174,175,176,177,178,179,180,181,182,183,
                      185,186,188,190,192,194,196,199,202,205,209,213,218,224,230,230,229,229,229,
                      230,229,230,230,230,230,229,230,229,230,230,229,229,230,229,230,230,230,230,
                      229,229,229,230,229,230,229,229,230,229,230,230,229,230,230,230,230,230,229,
                      230,230,229,229,230,230,229,230,230,229,230,230,230,230,230,230,230,230,230,
                      230,230,229,229,230,230,230,229,229,230,230,230,230,230,229,230,230,229,229,
                      229,229,230,230,230,229,230,230,229};



uchar4 __attribute__((kernel)) root(uchar4 in) {
                        uint8_t rgbMin = min(min(in.r,in.g),in.b);
                        uint8_t rgbMax = max(max(in.r,in.g),in.b);
                        uint8_t hue,saturation, value;
                        uint8_t  region, remainder, p, q, t;
                        uchar4 out;
                         out.a = in.a;
                         value = rgbMax; //Get HSV values
                        if (value == 0)
                             {
                                hue = 0;
                                saturation= 0;
                             }

                       else
                       {
                            saturation = 255 * ((long)(rgbMax - rgbMin)) / value;
                          if (saturation == 0)
                            {
                              hue = 0;
                            }
                          else
                           {
                             if (rgbMax == in.r)
                             {
                                hue = 0 + 43 * (in.g - in.b) / (rgbMax - rgbMin);
                             }
                             else if (rgbMax == in.g)
                             {
                              hue = 85 + 43 * (in.b - in.r) / (rgbMax - rgbMin);
                              }
                              else
                              {
                                hue = 171 + 43 * (in.r - in.g) / (rgbMax - rgbMin);
                              }

                            }
                       }

                       uint8_t newV = fuzzyLut[value];
                       if (saturation == 0)
                           {
                               out.r = newV;
                               out.g = newV;
                               out.b = newV;
                               return out;
                           }

                           region = hue / 43;
                           remainder = (hue - (region * 43)) * 6;

                           p = (newV * (255 - saturation)) >> 8;
                           q = (newV * (255 - ((saturation * remainder) >> 8))) >> 8;
                           t = (newV * (255 - ((saturation * (255 - remainder)) >> 8))) >> 8;

                           switch (region)
                           {
                               case 0:
                                   out.r = newV; out.g = t; out.b = p;
                                   break;
                               case 1:
                                   out.r = q; out.g = newV; out.b = p;
                                   break;
                               case 2:
                                   out.r = p; out.g = newV; out.b = t;
                                   break;
                               case 3:
                                   out.r = p; out.g = q; out.b = newV;
                                   break;
                               case 4:
                                   out.r = t; out.g = p; out.b = newV;
                                   break;
                               default:
                                   out.r = newV; out.g = p; out.b = q;
                                   break;
                           }
                               return out;

}
