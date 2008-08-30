Java Automatic Updater behavior
===============================


Updater shall employ the application preferences window as a site of any significant user interactions. Additional interactions may take place in the form of non-modal bars appearing at the top or bottom edge of the main application window(s).

Screen mock:

=====================================================================                                                  
| o o o                 YourSway IDE preferences                    |
=====================================================================
|          |          |          |  Software |                      |
|          |          |          |  Updates  |                      |
|---------------------------------           --------------------   |
|  Automatically check for updates:                                 |
|  (x) daily                                                        |
|  ( ) weekly                                                       |
|  ( ) never (manual checking only)                                 |
|                                                                   |
|  Last check 2 days ago.                < Check for Updates Now >  |
|                                                                   |
|  Updates:                                                         |
|  ---------------------------------------------------------------  |
|    - YourSway IDE 1.0.4 (5 Mb)             < Update to 1.0.4 >    |
|      -------------------------------------------------            |
|      | New features:                                ^|            |
|      |  - completion is working better              O|            |
|      |  - initial debugging support                 ||            |
|      |  - overwrite editing mode                    J|            |
|      -------------------------------------------------            |
|    - YourSway IDE 1.0.3          [===========            ] <x>    |
|      ¤ downloading: 40 Mb of 70 Mb...                             |
|      -------------------------------------------------            |
|      | New features:                                ^|            |
|      |  - ruby added!                               ||            |
|      | Bug fixes:                                   O|            |
|      |  - no longer crashes on startup              J|            |
|      -------------------------------------------------            |
|    + YourSway IDE 1.0.2 crashed on startup   < Retry install >    |
|    + YourSway IDE 1.0.1                    CURRENTLY INSTALLED    |
|    + YourSway IDE 1.0.0                 < Downgrade to 1.0.0 >    |
|  ---------------------------------------------------------------  |
|                                                                   |
|  [ ] Consider unstable nightly versions (NOT RECOMMENDED)         |
=====================================================================

