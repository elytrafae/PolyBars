# Library Limitations

Due to the entire project relying on text and the Action Bar, I had to make some arbitrary limitations to make sure things go smoothly.

- Every bar has a GUI height of 9
- The width of a texture should ideally be divisible by the number of slices requested
- Every slice inside a bar's texture ecosystem should have the exact same width
- One bar cannot span multiple lines
- Only use vanilla font or the font provided by the PolyBars system, to ensure width calculations work
- The positions of bars cannot be moved at runtime
- No alpha channel