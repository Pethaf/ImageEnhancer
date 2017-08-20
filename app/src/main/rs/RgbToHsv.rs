#pragma version(1)
#pragma rs java_package_name(com.example.pethaf.imageenhancer)
#pragma rs_fp_relaxed
uchar4 __attribute__((kernel)) root(uchar4 in)
{
           uchar4 out;
              //Get HSV values.
               out.a = in.a;
               uchar rgbMin = min(min(in.r,in.g),in.b);
               uchar rgbMax = max(max(in.r,in.g),in.b);
               out.s2 = rgbMax;
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
