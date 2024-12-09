#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"

using namespace ns3;
NS_LOG_COMPONENT_DEFINE("OfflineMobileScriptExample");

// Total received bytes on all receiver network nodes
uint64_t totalReceivedBytes = 0;
// Counters for transmitted and received packets
uint32_t totalTxPackets = 0;
uint32_t totalRxPackets = 0;
double simulationTime = 10.0;
Ptr<OutputStreamWrapper> stream;

void
RxCallback(Ptr<const Packet> packet, const Address& address)
{
    totalReceivedBytes += packet->GetSize();
    ;
    totalRxPackets++;
}

void
TxCallback(Ptr<const Packet> packet)
{
    totalTxPackets++;
}

double
calculateThroughput()
{
    Time now = Simulator::Now();
    double throughput = totalReceivedBytes * 8.0 / (1e6 * simulationTime);
    return throughput;
    NS_LOG_UNCOND(now.GetSeconds() << "s: \tthroughput: " << throughput << " Mbit/s");
    totalReceivedBytes = 0;
}

double
calculateDeliveryRatio()
{
    Time now = Simulator::Now();
    if (totalTxPackets != 0)
    {
        double ratio = (double)totalRxPackets / (double)totalTxPackets;
        return ratio;
    }
    return 0;
}

int
main(int argc, char* argv[])
{
    uint32_t nodes = 20;
    uint32_t flows = 10;
    uint32_t pps = 100;
    uint32_t speed = 5;
    uint32_t turn = 1;
    bool verbose = false;

    CommandLine cmd(__FILE__);
    cmd.AddValue("nodes", "Number of nodes", nodes);
    cmd.AddValue("flows", "Number of flows", flows);
    cmd.AddValue("pps", "Packets per second", pps);
    cmd.AddValue("speed", "Speed", speed);
    cmd.AddValue("turn", "whose turn is it", turn);

    cmd.Parse(argc, argv);

    double dataRate = (pps * 1024.0 * 8.0) / 1e6;
    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(1024));
    Config::SetDefault("ns3::OnOffApplication::PacketSize", UintegerValue(1024));
    Config::SetDefault("ns3::OnOffApplication::DataRate",
                       StringValue(std::to_string(dataRate) + "Mbps"));

    if (verbose)
    {
        Time::SetResolution(Time::NS);
        // LogComponentEnable("MobilityModel", LOG_LEVEL_INFO);
        LogComponentEnable("OnOffApplication", LOG_LEVEL_INFO);
        LogComponentEnable("PacketSink", LOG_LEVEL_INFO);
    }

    NodeContainer p2pNodes;
    p2pNodes.Create(2);
    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("2Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));
    NetDeviceContainer p2pDevices = pointToPoint.Install(p2pNodes);

    NodeContainer senders;
    senders.Create((int)(nodes / 2));
    NodeContainer nodeX = p2pNodes.Get(0);

    NodeContainer receivers;
    receivers.Create((int)(nodes / 2));
    NodeContainer nodeY = p2pNodes.Get(1);

    YansWifiChannelHelper channel = YansWifiChannelHelper::Default();
    channel.AddPropagationLoss(
        "ns3::RangePropagationLossModel"); // wireless range limited to coverage*5 meters!
    YansWifiPhyHelper phySender, phyReceiver;
    phySender.SetChannel(channel.Create());
    phyReceiver.SetChannel(channel.Create());

    WifiHelper wifiSender, wifiReceiver;
    wifiSender.SetStandard(WIFI_STANDARD_80211a);
    wifiSender.SetRemoteStationManager("ns3::IdealWifiManager");
    wifiReceiver.SetStandard(WIFI_STANDARD_80211a);
    wifiReceiver.SetRemoteStationManager("ns3::IdealWifiManager");
    WifiMacHelper macSender, macReceiver;
    Ssid ssidSender = Ssid("ns-3-ssid-sender");
    macSender.SetType("ns3::StaWifiMac",
                      "Ssid",
                      SsidValue(ssidSender),
                      "ActiveProbing",
                      BooleanValue(false));
    NetDeviceContainer senderDevices = wifiSender.Install(phySender, macSender, senders);
    macSender.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssidSender));
    NetDeviceContainer nodeXDevices = wifiSender.Install(phySender, macSender, nodeX);
    Ssid ssidReceiver = Ssid("ns-3-ssid-receiver");
    macReceiver.SetType("ns3::StaWifiMac",
                        "Ssid",
                        SsidValue(ssidReceiver),
                        "ActiveProbing",
                        BooleanValue(false));
    NetDeviceContainer receiverDevices = wifiReceiver.Install(phyReceiver, macReceiver, receivers);
    macReceiver.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssidReceiver));
    NetDeviceContainer nodeYDevices = wifiReceiver.Install(phyReceiver, macReceiver, nodeY);

    MobilityHelper mobility;
    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX",
                                  DoubleValue(0.0),
                                  "MinY",
                                  DoubleValue(0.0),
                                  "DeltaX",
                                  DoubleValue(5.0),
                                  "DeltaY",
                                  DoubleValue(3.0),
                                  "GridWidth",
                                  UintegerValue(1),
                                  "LayoutType",
                                  StringValue("RowFirst"));
    mobility.SetMobilityModel("ns3::ConstantVelocityMobilityModel");
    // mobility.SetMobilityModel("ns3::RandomDirection2dMobilityModel",
    //                         "Bounds", Rectangle(0, 50, 0, 50),
    //                         "Speed", DoubleValue(speed),
    //                         "Pause", DoubleValue(0.2));
    mobility.Install(senders);
    for (uint n = 0; n < nodes/2; n++)
    {
        Ptr<ConstantVelocityMobilityModel> mob =
            senders.Get(n)->GetObject<ConstantVelocityMobilityModel>();
        mob->SetVelocity(Vector(speed, 0, 0));
    }
    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX",
                                  DoubleValue(3.0),
                                  "MinY",
                                  DoubleValue(((nodes/2-1)*3.0)/2.0),
                                  "DeltaX",
                                  DoubleValue(5.0),
                                  "DeltaY",
                                  DoubleValue(3.0),
                                  "GridWidth",
                                  UintegerValue(1),
                                  "LayoutType",
                                  StringValue("RowFirst"));
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");

    mobility.Install(nodeX);
    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX",
                                  DoubleValue(26.0),
                                  "MinY",
                                  DoubleValue(0.0),
                                  "DeltaX",
                                  DoubleValue(5.0),
                                  "DeltaY",
                                  DoubleValue(3.0),
                                  "GridWidth",
                                  UintegerValue(1),
                                  "LayoutType",
                                  StringValue("RowFirst"));
    mobility.SetMobilityModel("ns3::ConstantVelocityMobilityModel");
    // mobility.SetMobilityModel("ns3::RandomDirection2dMobilityModel",
    //                         "Bounds", Rectangle(50, 100, 50, 100),
    //                         "Speed", DoubleValue(speed),
    //                         "Pause", DoubleValue(0.2));
    mobility.Install(receivers);
    for (uint n = 0; n < nodes/2; n++)
    {
        Ptr<ConstantVelocityMobilityModel> mob =
            receivers.Get(n)->GetObject<ConstantVelocityMobilityModel>();
        mob->SetVelocity(Vector(speed, 0, 0));
    }
    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX",
                                  DoubleValue(23.0),
                                  "MinY",
                                  DoubleValue(((nodes/2-1)*3.0)/2.0),
                                  "DeltaX",
                                  DoubleValue(5.0),
                                  "DeltaY",
                                  DoubleValue(3.0),
                                  "GridWidth",
                                  UintegerValue(1),
                                  "LayoutType",
                                  StringValue("RowFirst"));
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(nodeY);

    Ptr<RateErrorModel> em = CreateObject<RateErrorModel>();
    em->SetAttribute("ErrorRate", DoubleValue(0.0001));
    p2pDevices.Get(1)->SetAttribute("ReceiveErrorModel", PointerValue(em));

    InternetStackHelper stack;
    stack.Install(senders);
    stack.Install(receivers);
    stack.Install(nodeX);
    stack.Install(nodeY);

    Ipv4AddressHelper address;
    address.SetBase("10.1.2.0", "255.255.255.0");
    Ipv4InterfaceContainer senderInterfaces, nodeXInterface;
    senderInterfaces = address.Assign(senderDevices);
    nodeXInterface = address.Assign(nodeXDevices);

    address.SetBase("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer p2pInterfaces;
    p2pInterfaces = address.Assign(p2pDevices);

    address.SetBase("10.1.3.0", "255.255.255.0");
    Ipv4InterfaceContainer receiverInterfaces, nodeYInterface;
    receiverInterfaces = address.Assign(receiverDevices);
    nodeYInterface = address.Assign(nodeYDevices);

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();
    uint16_t packetSinkPort = 9;
    for (uint32_t i = 0; i < nodes / 2; ++i)
    {
        for (uint32_t j = 0; j < flows / (nodes / 2); ++j)
        {
            PacketSinkHelper packetSinkHelper(
                "ns3::TcpSocketFactory",
                (InetSocketAddress(receiverInterfaces.GetAddress(i), packetSinkPort + j))); // yes
            ApplicationContainer sinkApps = packetSinkHelper.Install(receivers.Get(i));
            sinkApps.Start(Seconds(0.5));
            sinkApps.Stop(Seconds(simulationTime + 0.5));
            Ptr<PacketSink> packetSink = StaticCast<PacketSink>(sinkApps.Get(0));
            packetSink->TraceConnectWithoutContext("Rx", MakeCallback(&RxCallback));
        }
    }
    uint16_t onOffPort = 9; // Port number for OnOff application
    for (uint32_t i = 0; i < nodes / 2; i++)
    {
        for (uint32_t j = 0; j < flows / (nodes / 2); j++)
        {
            OnOffHelper onOffHelper(
                "ns3::TcpSocketFactory",
                (InetSocketAddress(receiverInterfaces.GetAddress(i), onOffPort + j)));
            onOffHelper.SetAttribute("OnTime",
                                     StringValue("ns3::ConstantRandomVariable[Constant=1]"));
            onOffHelper.SetAttribute("OffTime",
                                     StringValue("ns3::ConstantRandomVariable[Constant=0]"));
            // onOffHelper.SetAttribute("DataRate",
            //                          DataRateValue(DataRate(std::to_string(dataRate) +
            //                                                 "Mbps"))); // Set your desired data
            //                                                 rate
            // onOffHelper.SetAttribute("PacketSize", UintegerValue(1024));
            ApplicationContainer apps = onOffHelper.Install(senders.Get(i));
            apps.Start(Seconds(0.5));
            apps.Stop(Seconds(simulationTime + 0.5));
            Ptr<OnOffApplication> onOffApp = StaticCast<OnOffApplication>(apps.Get(0));
            onOffApp->TraceConnectWithoutContext("Tx", MakeCallback(&TxCallback));
        }
    }
    Simulator::Stop(Seconds(simulationTime + 1));

    Simulator::Run();
    Simulator::Destroy();
    double averageThroughput = calculateThroughput();
    double deliveryRatio = calculateDeliveryRatio();
    switch (turn)
    {
    case 1:
        NS_LOG_UNCOND(nodes << "\t" << averageThroughput << "\t" << deliveryRatio);
        break;
    case 2:
        NS_LOG_UNCOND(flows << "\t" << averageThroughput << "\t" << deliveryRatio);
        break;
    case 3:
        NS_LOG_UNCOND(pps << "\t" << averageThroughput << "\t" << deliveryRatio);
        break;
    case 4:
        NS_LOG_UNCOND(speed << "\t" << averageThroughput << "\t" << deliveryRatio);
        break;
    }
    return 0;
}
