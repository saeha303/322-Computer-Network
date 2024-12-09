# ./ns3 run "scratch/1905033 --bottleneckDataRate=50 --packetLossRate=0.000001 --file=1 --algo=ns3::TcpWestwoodPlus"

#!/bin/bash

# # Set the path to your simulation file
SIMULATION_FILE="scratch/1905033"
rm -fr "scratch/tp_vs_bndr.dat"
rm -fr "scratch/tp_vs_plr.dat"
rm -fr "scratch/flow1.cwnd"
rm -fr "scratch/flow2.cwnd"

run_ns3_simulation() {
    
    echo "Running simulation with bottleneckDataRate=$1, packetLossRate=$2, Tcp variant=$4"
    ./ns3 run "$SIMULATION_FILE --bottleneckDataRate=$1 --packetLossRate=$2 --file=$3 --algo=$4"
    echo "Simulation completed."
}
run_gnuplot()
{
    gnuplot << EOF
    set terminal png size 640,480
    set output "$5.png"
    set xlabel "$6"
    set ylabel "$7"
    plot "$1" using $8:$9  with linespoints title "$2", "$3" using ${10}:${11} with linespoints title "$4"
    exit
EOF
}
run_gnuplot_scaling()
{
    gnuplot << EOF
    set terminal png size 640,480
    set output "$5.png"
    set xlabel "$6"
    set ylabel "$7"
    set logscale x  # Set x-axis to logarithmic scale
    set xrange [1e-6:1e-2]  # Set the x-axis range from 10^-6 to 10^-2
    plot "$1" using $8:$9  with linespoints title "$2", "$3" using ${10}:${11} with linespoints title "$4"
    exit
EOF
}
run_gnuplot_for_1()
{
    gnuplot << EOF
    set terminal png size 640,480
    set output "$3.png"
    set xlabel "$4"
    set ylabel "$5"
    plot "$1" using $6:$7 title "$2" with linespoints
    exit
EOF
}
read which
bottleneckDataRate=("1" "50" "100" "150" "200" "250" "300")
packetLossRate=("0.01" "0.001" "0.0001" "0.00001" "0.000001")
graph_folder="scratch/graphs"
if [ $which -eq 1 ]; then
algo="ns3::TcpWestwoodPlus"
elif [ $which -eq 2 ]; then
algo="ns3::TcpHighSpeed"
elif [ $which -eq 3 ]; then
algo="ns3::TcpAdaptiveReno"
fi
# # Loop through each combination of parameters and run the simulation
for bndr in "${bottleneckDataRate[@]}"; do
    run_ns3_simulation $bndr 0.000001 1 $algo
    if [ $which -eq 1 ]; then
    run_gnuplot "scratch/flow1.cwnd" "newreno" "scratch/flow2.cwnd" "westwoodplus" "$graph_folder/westwood/Congestion Window VS Time bndr $bndr westwood" "Time" "Congestion Window" 1 2 1 2
    elif [ $which -eq 2 ]; then
    run_gnuplot "scratch/flow1.cwnd" "newreno" "scratch/flow2.cwnd" "highspeed" "$graph_folder/highspeed/Congestion Window VS Time bndr $bndr highspeed" "Time" "Congestion Window" 1 2 1 2
    elif [ $which -eq 3 ]; then
    run_gnuplot "scratch/flow1.cwnd" "newreno" "scratch/flow2.cwnd" "adaptivereno" "$graph_folder/adaptreno/Congestion Window VS Time bndr $bndr adaptreno" "Time" "Congestion Window" 1 2 1 2
    fi
done
if [ $which -eq 1 ]; then
run_gnuplot "scratch/tp_vs_bndr.dat" "newreno" "scratch/tp_vs_bndr.dat" "westwoodplus" "$graph_folder/westwood/Throughput VS Bottleneck Data Rate westwood" "Bottleneck Data Rate" "Throughput" 1 2 1 3
run_gnuplot_for_1 "scratch/tp_vs_bndr.dat" "newreno+westwoodplus" "$graph_folder/Fairness Index VS Bottleneck Data Rate newreno+westwoodplus" "Bottleneck Data Rate" "Fairness Index" 1 4
elif [ $which -eq 2 ]; then
run_gnuplot "scratch/tp_vs_bndr.dat" "newreno" "scratch/tp_vs_bndr.dat" "highspeed" "$graph_folder/highspeed/Throughput VS Bottleneck Data Rate highspeed" "Bottleneck Data Rate" "Throughput" 1 2 1 3
run_gnuplot_for_1 "scratch/tp_vs_bndr.dat" "newreno+highspeed" "$graph_folder/Fairness Index VS Bottleneck Data Rate newreno+highspeed" "Bottleneck Data Rate" "Fairness Index" 1 4
elif [ $which -eq 3 ]; then
run_gnuplot "scratch/tp_vs_bndr.dat" "newreno" "scratch/tp_vs_bndr.dat" "adaptivereno" "$graph_folder/adaptreno/Throughput VS Bottleneck Data Rate adaptreno" "Bottleneck Data Rate" "Throughput" 1 2 1 3
run_gnuplot_for_1 "scratch/tp_vs_bndr.dat" "newreno+adaptreno" "$graph_folder/Fairness Index VS Bottleneck Data Rate newreno+adaptreno" "Bottleneck Data Rate" "Fairness Index" 1 4
fi

for plr in "${packetLossRate[@]}"; do
    run_ns3_simulation 50 $plr 2 $algo
    if [ $which -eq 1 ]; then
    run_gnuplot "scratch/flow1.cwnd" "newreno" "scratch/flow2.cwnd" "westwoodplus" "$graph_folder/westwood/Congestion Window VS Time plr $plr westwood" "Time" "Congestion Window" 1 2 1 2
    elif [ $which -eq 2 ]; then
    run_gnuplot "scratch/flow1.cwnd" "newreno" "scratch/flow2.cwnd" "highspeed" "$graph_folder/highspeed/Congestion Window VS Time plr $plr highspeed" "Time" "Congestion Window" 1 2 1 2
    elif [ $which -eq 3 ]; then
    run_gnuplot "scratch/flow1.cwnd" "newreno" "scratch/flow2.cwnd" "adaptivereno" "$graph_folder/adaptreno/Congestion Window VS Time plr $plr adaptreno" "Time" "Congestion Window" 1 2 1 2
    fi
done
if [ $which -eq 1 ]; then
run_gnuplot_scaling "scratch/tp_vs_plr.dat" "newreno" "scratch/tp_vs_plr.dat" "westwoodplus" "$graph_folder/westwood/Throughput VS Packet Loss Rate westwood" "Packet Loss Rate" "Throughput" 1 2 1 3
run_gnuplot_for_1 "scratch/tp_vs_plr.dat" "newreno+westwoodplus" "$graph_folder/Fairness Index VS Packet Loss Rate newreno+westwoodplus" "Packet Loss Rate" "Fairness Index" 1 4
elif [ $which -eq 2 ]; then
run_gnuplot_scaling "scratch/tp_vs_plr.dat" "newreno" "scratch/tp_vs_plr.dat" "highspeed" "$graph_folder/highspeed/Throughput VS Packet Loss Rate highspeed" "Packet Loss Rate" "Throughput" 1 2 1 3
run_gnuplot_for_1 "scratch/tp_vs_plr.dat" "newreno+highspeed" "$graph_folder/Fairness Index VS Packet Loss Rate newreno+highspeed" "Packet Loss Rate" "Fairness Index" 1 4
elif [ $which -eq 3 ]; then
run_gnuplot_scaling "scratch/tp_vs_plr.dat" "newreno" "scratch/tp_vs_plr.dat" "adaptivereno" "$graph_folder/adaptreno/Throughput VS Packet Loss Rate adaptreno" "Packet Loss Rate" "Throughput" 1 2 1 3
run_gnuplot_for_1 "scratch/tp_vs_plr.dat" "newreno+adaptivereno" "$graph_folder/Fairness Index VS Packet Loss Rate newreno+adaptivereno" "Packet Loss Rate" "Fairness Index" 1 4
fi
