# ./ns3 run "scratch/1905033_2 --nodes=20 --flows=10 --pps=100 --speed=1 --turn=1"

#!/bin/bash

# Set the path to your simulation file
SIMULATION_FILE="scratch/1905033_2"
DAT1="scratch/1905033_2_node"
DAT2="scratch/1905033_2_flow"
DAT3="scratch/1905033_2_pps"
DAT4="scratch/1905033_2_speed"

rm -fr "${DAT1}.dat"
rm -fr "${DAT2}.dat"
rm -fr "${DAT3}.dat"
rm -fr "${DAT4}.dat"

# Array of values for each parameter
NODES=("20" "40" "60" "80" "100")
FLOWS=("10" "20" "30" "40" "50")
PACKETS_PER_SECOND=("100" "200" "300" "400" "500")
SPEED=("5" "10" "15" "20" "25")

run_ns3_simulation() {
    
    echo "Running simulation with nodes=$1, flows=$2, pps=$3, speed=$4"
    ./ns3 run "$SIMULATION_FILE --nodes=$1 --flows=$2 --pps=$3 --speed=$4 --turn=$6" >> "$5.dat" 2>&1
    echo "Simulation completed."
}
run_gnuplot()
{
    gnuplot << EOF
    set terminal png size 640,480
    set output "$2.png"
    set xlabel "$4"
    set ylabel "$5"
    plot "$1.dat" using 1:$6 title "$3" with linespoints
    exit
EOF
}

nodes=20
flows=10
pps=100
speed=5
# Loop through each combination of parameters and run the simulation
for nodes in "${NODES[@]}"; do
    # Calculate flow as nodes/2
    flows=$(( $nodes / 2 ))
    run_ns3_simulation $nodes $flows $pps $speed $DAT1 1
done
run_gnuplot $DAT1 "scratch/1905033_2_node_vs_throughput" "Node vs Throughput" "Node" "Throughput" 2
run_gnuplot $DAT1 "scratch/1905033_2_node_vs_delivery_ratio" "Node vs Delivery ratio" "Node" "Delivery ratio" 3
    
nodes=20
for flows in "${FLOWS[@]}"; do
    run_ns3_simulation $nodes $flows $pps $speed $DAT2 2
done
run_gnuplot $DAT2 "scratch/1905033_2_flow_vs_throughput" "Flow vs Throughput" "Flow" "Throughput" 2
run_gnuplot $DAT2 "scratch/1905033_2_flow_vs_delivery_ratio" "Flow vs Delivery ratio" "Flow" "Delivery ratio" 3
flows=10
for pps in "${PACKETS_PER_SECOND[@]}"; do
    run_ns3_simulation $nodes $flows $pps $speed $DAT3 3
done
run_gnuplot $DAT3 "scratch/1905033_2_pps_vs_throughput" "PPS vs Throughput" "PPS" "Throughput" 2
run_gnuplot $DAT3 "scratch/1905033_2_pps_vs_delivery_ratio" "PPS vs Delivery ratio" "PPS" "Delivery ratio" 3
pps=100
for speed in "${SPEED[@]}"; do
    run_ns3_simulation $nodes $flows $pps $speed $DAT4 4
done
run_gnuplot $DAT4 "scratch/1905033_2_speed_vs_throughput" "Speed vs Throughput" "Speed" "Throughput" 2
run_gnuplot $DAT4 "scratch/1905033_2_speed_vs_delivery_ratio" "Speed vs Delivery ratio" "Speed" "Delivery ratio" 3
    
