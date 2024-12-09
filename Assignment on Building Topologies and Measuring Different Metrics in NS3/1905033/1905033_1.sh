# ./ns3 run "scratch/1905033_1 --nodes=20 --flows=20 --pps=100 --coverage=1 --turn=1"

#!/bin/bash

# Set the path to your simulation file
SIMULATION_FILE="scratch/1905033_1"
DAT1="scratch/1905033_1_node"
DAT2="scratch/1905033_1_flow"
DAT3="scratch/1905033_1_pps"
DAT4="scratch/1905033_1_coverage"

rm -fr "${DAT1}.dat"
rm -fr "${DAT2}.dat"
rm -fr "${DAT3}.dat"
rm -fr "${DAT4}.dat"

# Array of values for each parameter
NODES=("20" "40" "60" "80" "100")
FLOWS=("10" "20" "30" "40" "50")
PACKETS_PER_SECOND=("100" "200" "300" "400" "500")
COVERAGE_AREA=("1" "2" "4" "5") # This will be multiplied by the Tx range

run_ns3_simulation() {
    
    echo "Running simulation with nodes=$1, flows=$2, pps=$3, coverage=$4"
    ./ns3 run "$SIMULATION_FILE --nodes=$1 --flows=$2 --pps=$3 --coverage=$4 --turn=$6" >> "$5.dat" 2>&1
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
coverage=5
# Loop through each combination of parameters and run the simulation
for nodes in "${NODES[@]}"; do
    # Calculate flow as nodes/2
    flows=$(( $nodes / 2 ))
    run_ns3_simulation $nodes $flows $pps $coverage $DAT1 1
done
run_gnuplot $DAT1 "scratch/1905033_1_node_vs_throughput" "Node vs Throughput" "Node" "Throughput" 2
run_gnuplot $DAT1 "scratch/1905033_1_node_vs_delivery_ratio" "Node vs Delivery ratio" "Node" "Delivery ratio" 3
    
nodes=20
for flows in "${FLOWS[@]}"; do
    run_ns3_simulation $nodes $flows $pps $coverage $DAT2 2
done
run_gnuplot $DAT2 "scratch/1905033_1_flow_vs_throughput" "Flow vs Throughput" "Flow" "Throughput" 2
run_gnuplot $DAT2 "scratch/1905033_1_flow_vs_delivery_ratio" "Flow vs Delivery ratio" "Flow" "Delivery ratio" 3
flows=10
for pps in "${PACKETS_PER_SECOND[@]}"; do
    run_ns3_simulation $nodes $flows $pps $coverage $DAT3 3
done
run_gnuplot $DAT3 "scratch/1905033_1_pps_vs_throughput" "PPS vs Throughput" "PPS" "Throughput" 2
run_gnuplot $DAT3 "scratch/1905033_1_pps_vs_delivery_ratio" "PPS vs Delivery ratio" "PPS" "Delivery ratio" 3
pps=100
for coverage in "${COVERAGE_AREA[@]}"; do
    run_ns3_simulation $nodes $flows $pps $coverage $DAT4 4
done
run_gnuplot $DAT4 "scratch/1905033_1_coverage_vs_throughput" "Coverage vs Throughput" "Coverage" "Throughput" 2
run_gnuplot $DAT4 "scratch/1905033_1_coverage_vs_delivery_ratio" "Coverage vs Delivery ratio" "Coverage" "Delivery ratio" 3
    
