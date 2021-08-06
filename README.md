# warehouse-4.0

A warehouse simulation environment with unit-load products, used to experiment with optimization algorithms.

## To do

Simulation:
- Different types of mobiles: speed, restricted access (space and time)
- Battery of shuttles and charging zone
- Cross-docking

Algorithms:
- Storage location policies:
  - random
  - dedicated
  - class-based: ABC, clustering, ...),
  - also with (predictive) duration-of-stay
- Mobile-Mission matching: anticipation by considering
  - available mobiles and missions
  - mobiles ending their current mission soon
  - missions becoming available soon
  - priorities between missions
  - distance to start of mission
  - accessibility of pallet
- Truck-Dock matching: anticipation by considering
  - available trucks and docks
  - docks becoming available soon
  - priorities between trucks
  - distance between docks and pallets to load/unload
- Estimation from observations:
  - travel times estimation
  - product metrics: demand rate, capacity needed, seasonality
