# Big Data Management Synopses

This repository contains a collection of simple implementations of synopses:
- Exponential histograms
- Cuckoo filters
- Count-min sketches
- Count-min sketches with range queries
- Bloom filters 
- Counting bloom filters
- Flajolet-Martin sketches

Notes:
- The implementations are not optimized for performance or usability, but for simplicity.
- Due to time constraints i have not been able to test the implementation for the FM sketch thoroughly.
- For FM sketch you could probably better use integers as bitvectors instead of boolean arrays.