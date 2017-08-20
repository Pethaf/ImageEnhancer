#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.example.pethaf.imageenhancer)

#include "rs_debug.rsh"

int32_t histogram[256];
float cummulativeSum[256];
int size;

uchar4 __attribute__((kernel)) root(uchar4 in) {
     uchar4 out;
    //Get HSV values.
     out.a = in.a;
     uchar rgbMin = min(min(in.r,in.g),in.b);
     uchar rgbMax = max(max(in.r,in.g),in.b);
     out.s2 = rgbMax;
     rsAtomicInc(&histogram[out.s2]);
     if (out.s2 == 0)
     {
        out.s0 = 0;
        out.s1= 0;
        return out;
     }

    out.s1 = 255 * ((long)(rgbMax - rgbMin)) / out.s2;
    if (out.s1 == 0)
    {
      out.s0 = 0;
      return out;
     }

     if (rgbMax == in.r)
     {
        out.s0 = 0 + 43 * (in.g - in.b) / (rgbMax - rgbMin);
     }
     else if (rgbMax == in.g)
     {
      out.s0 = 85 + 43 * (in.b - in.r) / (rgbMax - rgbMin);
      }
      else
      {
        out.s0 = 171 + 43 * (in.r - in.g) / (rgbMax - rgbMin);
       }
        return out;

    }

uchar4 __attribute__((kernel)) remaptoRGB(uchar4 in)
{
    uchar4 out;
    uchar region, remainder, p, q, t;
    uchar newV = (uchar)255*cummulativeSum[in.s2];
       if (in.s1 == 0)
        {
            out.r = newV;
            out.g = newV;
            out.b = newV;
            out.a = in.a;
            return out;
        }

        region = in.s0 / 43;
        remainder = (in.s0 - (region * 43)) * 6;

        p = (newV * (255 - in.s1)) >> 8;
        q = (newV * (255 - ((in.s1 * remainder) >> 8))) >> 8;
        t = (newV * (255 - ((in.s1 * (255 - remainder)) >> 8))) >> 8;

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
            out.a = in.a;
            return out;

      }
void init() {
    //initialize the array with zeros
    for (int i = 0; i < 256; i++) {
        histogram[i] = 0;
        cummulativeSum[i] = 0.0f;
    }
}

void createRemapArray() {
    //create map for v
    float sum = 0;
    for (int i = 0; i < 256; i++) {
        sum += histogram[i];
        cummulativeSum[i] = sum / (size);
    }
}