/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/wifi-module.h"
#include "ns3/yans-wifi-helper.h"

// Default Network Topology
//
//   Wifi 10.1.3.0
//                 AP
//  *    *    *    *
//  |    |    |    |    10.1.1.0
// n5   n6   n7   n0 -------------- n1   n2   n3   n4
//                   point-to-point  |    |    |    |
//                                   ================
//                                     LAN 10.1.2.0

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("OfflineScriptExample");

int
main(int argc, char* argv[])
{
    bool verbose = true;
    uint32_t nodes = 20;
    uint32_t flows = 10;
    uint32_t pps = 100;
    uint32_t speed = 5;
    uint32_t coverage = 1;
    // bool tracing = false;

    CommandLine cmd(__FILE__);
    cmd.AddValue("nodes", "Number of nodes", nodes);
    cmd.AddValue("flows", "Number of flows", flows);
    cmd.AddValue("pps", "Packets per second", pps);
    cmd.AddValue("speed", "Speed", speed);
    cmd.AddValue("coverage", "Coverage area multiplier of Tx range", coverage);

    // Parse the command line arguments
    cmd.Parse(argc, argv);

    // // Access the parsed values
    // int nodes = cmd.GetIntegerValue("nodes");
    // int flows = cmd.GetIntegerValue("flows");
    // int pps = cmd.GetIntegerValue("pps");
    // int coverage = cmd.GetIntegerValue("coverage");
    if (verbose)
    {
        LogComponentEnable("UdpEchoClientApplication", LOG_LEVEL_INFO);
        LogComponentEnable("UdpEchoServerApplication", LOG_LEVEL_INFO);
    }

    NodeContainer senders;
    senders.Create((int)(nodes / 2));
    NodeContainer p2pNodes;
    p2pNodes.Create(2);
    NodeContainer receivers;
    receivers.Create((int)(nodes / 2));
    // Set up mobility for wireless nodes
    MobilityHelper mobility;
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    // Create a constant velocity mobility model and set the speed
    std::vector<Ptr<ConstantVelocityMobilityModel>> cvmmVector;
    for (uint32_t i = 0; i < senders.GetN()+receivers.GetN(); ++i) {
        Ptr<ConstantVelocityMobilityModel> cvmm = CreateObject<ConstantVelocityMobilityModel>();
        cvmm->SetVelocity(Vector(speed, 0.0, 0.0)); // Set the speed along the x-axis (horizontal movement)
        cvmmVector.push_back(cvmm);
    }
    mobility.SetPositionAllocator("ns3::RandomRectanglePositionAllocator",
                                  "X", StringValue("ns3::UniformRandomVariable[Min=0|Max=100]"),
                                  "Y", StringValue("ns3::UniformRandomVariable[Min=0|Max=100]"));
    mobility.Install(senders);
    mobility.Install(receivers);
    
    // Attach the constant velocity mobility model to the nodes
    for (uint32_t i = 0; i < senders.GetN(); ++i) {
        senders.Get(i)->AggregateObject(cvmmVector[i]);
        // receivers.Get(i)->AggregateObject(cvmmVector[i]);
    }
    for (uint32_t i = senders.GetN(); i < receivers.GetN(); ++i) {
        // senders.Get(i)->AggregateObject(cvmmVector[i]);
        receivers.Get(i)->AggregateObject(cvmmVector[i]);
    }
    YansWifiChannelHelper channel = YansWifiChannelHelper::Default();
    YansWifiPhyHelper phy;
    phy.SetChannel(channel.Create());
    // // set the Tx range
    // phy.Set("TxPowerStart", DoubleValue(10.0)); // Set the initial Tx power
    // phy.Set("TxPowerEnd", DoubleValue(10.0));   // Set the final Tx power (no power control)
    // phy.Set("TxGain", DoubleValue(0.0));        // Set the Tx gain
    // phy.Set("RxGain", DoubleValue(0.0));        // Set the Rx gain
    // phy.Set("EnergyDetectionThreshold", DoubleValue(-96.0)); // Set the energy detection threshold
    // phy.Set("CcaMode1Threshold", DoubleValue(-99.0));       // Set the CCA mode 1 threshold

    // phy.Set("TxPowerLevels", UintegerValue(1)); // Set the number of Tx power levels
    // phy.Set("TxGainLevels", UintegerValue(1));  // Set the number of Tx gain levels
    // phy.Set("RxGainLevels", UintegerValue(1));  // Set the number of Rx gain levels

    // Calculate the new transmission range based on the coverage area factor
    // double defaultTxRange = 5.0;
    // double newTxRange = coverage * defaultTxRange;
    // phy.Set("TxMaxRange", DoubleValue(newTxRange));
    
    WifiMacHelper mac;
    Ssid ssid = Ssid("senders");

    WifiHelper wifi;
    wifi.SetStandard(WIFI_STANDARD_80211a);
    wifi.SetRemoteStationManager("ns3::ConstantRateWifiManager",
                                 "DataMode",
                                 StringValue("HtMcs7"),
                                 "ControlMode",
                                 StringValue("HtMcs7"));

    mac.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssid), "ActiveProbing", BooleanValue(false));
    NetDeviceContainer senderDevices = wifi.Install(phy, mac, senders);
    ssid = Ssid("receivers");
    mac.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssid), "ActiveProbing", BooleanValue(false));
    NetDeviceContainer receiverDevices = wifi.Install(phy, mac, receivers);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("5Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));

    NetDeviceContainer p2pDevices = pointToPoint.Install(p2pNodes);
    
    InternetStackHelper stack;
    stack.Install(senders);
    stack.Install(receivers);
    stack.Install(p2pNodes);

    Ipv4AddressHelper address;

    address.SetBase("10.2.1.0", "255.255.255.0");
    Ipv4InterfaceContainer p2pInterfaces;
    p2pInterfaces = address.Assign(p2pDevices);

    address.SetBase("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer senderInterfaces;
    senderInterfaces = address.Assign(senderDevices);

    address.SetBase("10.3.1.0", "255.255.255.0");
    Ipv4InterfaceContainer receiverInterfaces;
    receiverInterfaces = address.Assign(receiverDevices);

    // Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    uint16_t onOffPort = 9; // Port number for OnOff application
    // Set the desired packets per second rate
    uint32_t dataRate = pps*8/1e3; // Change this to your desired value

    // Calculate the inter-packet interval based on the packets per second rate
    // double interPacketInterval = 1.0 / packetsPerSecond;

    OnOffHelper onOffHelper("ns3::TcpSocketFactory",
                            InetSocketAddress(receiverInterfaces.GetAddress(0), onOffPort));
    onOffHelper.SetConstantRate(
        DataRate(std::to_string(dataRate))); // Set the data rate of the OnOff application,10Mbps
    onOffHelper.SetAttribute(
        "PacketSize",UintegerValue(1024));
    // onOffHelper.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    // onOffHelper.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
    // Start the OnOff application at 1 second and run for 5 seconds
    ApplicationContainer apps;
    uint32_t temp = 0;
    while (true)
    {
        for (uint32_t i = 0; i < senders.GetN(); ++i)
        {
            if (temp == flows)
            {
                break;
            }
            apps.Add(onOffHelper.Install(senders.Get(i)));
            temp++;
        }
        if (temp == flows)
        {
            break;
        }
    }
    apps.Start(Seconds(0.0));
    apps.Stop(Seconds(10.0));

    uint16_t packetSinkPort = 10;
    PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory",
                                      InetSocketAddress(Ipv4Address::GetAny(), packetSinkPort));
    ApplicationContainer sinkApps;
    // temp = 0;
    // while (true)
    // {
        for (uint32_t i = 0; i < receivers.GetN(); ++i)
        {
            // if (temp == flows)
            // {
            //     break;
            // }
            sinkApps.Add(packetSinkHelper.Install(receivers.Get(i)));
            // temp++;
        }
    //     if (temp == flows)
    //     {
    //         break;
    //     }
    // }
    sinkApps.Start(Seconds(0.0));
    sinkApps.Stop(Seconds(10.0)); // Stop the application after 10 seconds

    // // Install applications for packet delivery ratio measurement
    // UdpEchoServerHelper echoServer(port);
    // ApplicationContainer serverApps = echoServer.Install(senders);
    // serverApps.Start(Seconds(1.0));
    // serverApps.Stop(Seconds(10.0));

    // UdpEchoClientHelper echoClient(receiverInterfaces.GetAddress(0), port);
    // echoClient.SetAttribute("MaxPackets", UintegerValue(100));
    // echoClient.SetAttribute("Interval", TimeValue(Seconds(1.0)));
    // echoClient.SetAttribute("PacketSize", UintegerValue(1024));

    // ApplicationContainer clientApps = echoClient.Install(receivers.Get(0));
    // clientApps.Start(Seconds(1.0));
    // clientApps.Stop(Seconds(10.0));

    Simulator::Stop(Seconds(10.0));

    // if (tracing)
    // {
    //     phy.SetPcapDataLinkType(WifiPhyHelper::DLT_IEEE802_11_RADIO);
    //     pointToPoint.EnablePcapAll("third");
    //     phy.EnablePcap("third", apDevices.Get(0));
    //     csma.EnablePcap("third", csmaDevices.Get(0), true);
    // }

    Simulator::Run();
    Simulator::Destroy();
    return 0;
}
