# HW1 Quantization and Subsampling
Your program will be invoked using seven parameters where
YourProgram.exe C:/myDir/myImage.rgb Y U V Sw Sh A
• The first parameter is the name of the image, which will be provided in an 8 bit
per channel RGB format (Total 24 bits per pixel). You may assume that all
images will be of the same size for this assignment (HD size = 1920wx1080h),
more information on the image format will be placed on the class website
• The next three parameters are integers control the subsampling of your Y U and
V spaces respectively. For sake of simplicity, we will follow the convention that
subsampling occurs only along the width dimension and not the height. Each of
these parameters can take on values from 1 to n for some n, 1 suggesting no sub
sampling and n suggesting a sub sampling by n
• The next two parameters are single precision floats Sw and Sh which take positive
values < 1.0 and control the scaled output image width and height independently.
• Finally a integer A ( 0 or 1) to suggest that antialiasing (prefiltering needs to be
performed). 0 indicates no antialiasing and vice versa
<img width="1439" alt="Screen Shot 2022-11-02 at 6 22 32 PM" src="https://user-images.githubusercontent.com/20672326/199631672-f4569099-eb9b-4727-ae8b-7905906da8f2.png">


# HW2 Quantization and Subsampling
To invoke your program we will compile it and run it at the command line as
YourProgram.exe C:/myDir/foreGroundVideo C:/myDir/backGroundVideo mode
Where,
• foreGroundVideo is the base name for a green screened foreground video, which
has a foreground element (actor, object) captured in front of a green screen.
• backGroundVideo is any normal video
• mode is a mode that can take values 1 or 0. 1 indicating that the foreground video
has a green screen, and 0 indicating there is no green screen.

# HW3 DWT and IDWT Encoding
Input to your program will be 2 parameters where:
• The first parameter is the name of the input image rgb file. (file format is similar to previous
assignments).
• The second parameter n is an integral number from 0 to 9 that defines the low pass level to be
used in your decoding. For a given n, this translates to using 2n low pass coefficients in rows
and columns respectively to use in the decoding process . Additionally, n could also take a
value of -1 to show progressive decoding. Please see the implementation section for an
explanation
