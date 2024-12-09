#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-layout-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"

#include <fstream>

namespace ns3
{

class Application;

/**
 * Tutorial - a simple Application sending packets.
 */
class TutorialApp : public Application
{
  public:
    TutorialApp();
    ~TutorialApp() override;

    /**
     * Register this type.
     * \return The TypeId.
     */
    static TypeId GetTypeId();

    /**
     * Setup the socket.
     * \param socket The socket.
     * \param address The destination address.
     * \param packetSize The packet size to transmit.
     * \param simultime The time span to transmit.
     * \param dataRate the data rate to use.
     */
    void Setup(Ptr<Socket> socket,
               Address address,
               uint32_t packetSize,
               uint32_t simultime,
               DataRate dataRate);

  private:
    void StartApplication() override;
    void StopApplication() override;

    /// Schedule a new transmission.
    void ScheduleTx();
    /// Send a packet.
    void SendPacket();

    Ptr<Socket> m_socket;   //!< The transmission socket.
    Address m_peer;         //!< The destination address.
    uint32_t m_packetSize;  //!< The packet size.
    uint32_t m_simultime;   //!< The time it takes.
    DataRate m_dataRate;    //!< The data rate to use.
    EventId m_sendEvent;    //!< Send event.
    bool m_running;         //!< True if the application is running.
    uint32_t m_packetsSent; //!< The number of packets sent.
};

} // namespace ns3

using namespace ns3;

TutorialApp::TutorialApp()
    : m_socket(nullptr),
      m_peer(),
      m_packetSize(0),
      m_simultime(0),
      m_dataRate(0),
      m_sendEvent(),
      m_running(false),
      m_packetsSent(0)
{
}

TutorialApp::~TutorialApp()
{
    m_socket = nullptr;
}

/* static */
TypeId
TutorialApp::GetTypeId()
{
    static TypeId tid = TypeId("TutorialApp")
                            .SetParent<Application>()
                            .SetGroupName("Tutorial")
                            .AddConstructor<TutorialApp>();
    return tid;
}

void
TutorialApp::Setup(Ptr<Socket> socket,
                   Address address,
                   uint32_t packetSize,
                   uint32_t simultime,
                   DataRate dataRate)
{
    m_socket = socket;
    m_peer = address;
    m_packetSize = packetSize;
    m_simultime = simultime;
    m_dataRate = dataRate;
}

void
TutorialApp::StartApplication()
{
    m_running = true;
    m_packetsSent = 0;
    m_socket->Bind();
    m_socket->Connect(m_peer);
    SendPacket();
}

void
TutorialApp::StopApplication()
{
    m_running = false;

    if (m_sendEvent.IsRunning())
    {
        Simulator::Cancel(m_sendEvent);
    }

    if (m_socket)
    {
        m_socket->Close();
    }
}

void
TutorialApp::SendPacket()
{
    Ptr<Packet> packet = Create<Packet>(m_packetSize);
    m_socket->Send(packet);

    if (Simulator::Now().GetSeconds() < m_simultime)
        ScheduleTx();
}

void
TutorialApp::ScheduleTx()
{
    if (m_running)
    {
        Time tNext(Seconds(m_packetSize * 8 / static_cast<double>(m_dataRate.GetBitRate())));
        m_sendEvent = Simulator::Schedule(tNext, &TutorialApp::SendPacket, this);
    }
}

uint32_t simulationTime = 30;
// double throughputArr[] = {0, 0};
NS_LOG_COMPONENT_DEFINE("OfflineScriptExample");

/**
 * Congestion window change callback
 *
 * \param stream The output stream file.
 * \param oldCwnd Old congestion window.
 * \param newCwnd New congestion window.
 */
static void
CwndChange(Ptr<OutputStreamWrapper> stream, uint32_t oldCwnd, uint32_t newCwnd)
{
    *stream->GetStream() << Simulator::Now().GetSeconds() << " " << newCwnd << std::endl;
}

// static void
// calculateThroughput(Ptr<FlowMonitor> monitor)
// {
//     int j = 0;
//     throughputArr[0] = 0;
//     throughputArr[1] = 0;
//     FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
//     for (auto iter = stats.begin(); iter != stats.end(); ++iter)
//     {
//         if (j % 2 == 0)
//         {
//             throughputArr[0] += iter->second.rxBytes;
//         }
//         if (j % 2 == 1)
//         {
//             throughputArr[1] += iter->second.rxBytes;
//         }
//         j = j + 1;
//     }
//     Simulator::Schedule(Seconds(0.2), &calculateThroughput, monitor);
// }

int
main(int argc, char* argv[])
{
    bool verbose = false;
    uint32_t nLeaf = 2;
    uint32_t bottleneckDataRate = 50;
    double packetLossRate = 0.000001;
    int file = 1;
    std::string filename;
    std::string algo = "ns3::TcpWestwoodPlus";

    CommandLine cmd(__FILE__);
    cmd.AddValue("bottleneckDataRate", "Bottleneck Data rate", bottleneckDataRate);
    cmd.AddValue("packetLossRate", "packet Loss Rate", packetLossRate);
    cmd.AddValue("file", "output file", file);
    cmd.AddValue("algo", "Second TCP algorithm", algo);

    cmd.Parse(argc, argv);
    if (file == 1)
        filename = "scratch/tp_vs_bndr.dat";
    else if (file == 2)
        filename = "scratch/tp_vs_plr.dat";
    std::ofstream myfile(filename, std::ios_base::app);
    std::string pointToPointLeafLinkBw = "1Gbps";
    std::string pointToPointLeafLinkDelay = "1ms";
    std::string bottleNeckLinkBw = std::to_string(bottleneckDataRate) + "Mbps";
    std::string bottleNeckLinkDelay = "100ms";
    double bandwidth_delay_product = (bottleneckDataRate * 0.1) / 1024;
    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(1024));
    Config::SetDefault("ns3::TcpSocket::InitialCwnd", UintegerValue(1));
    Config::SetDefault("ns3::TcpL4Protocol::RecoveryType",
                       TypeIdValue(TypeId::LookupByName("ns3::TcpClassicRecovery")));

    if (verbose)
    {
        LogComponentEnable("PacketSink", LOG_LEVEL_INFO);
    }

    // Create the point-to-point link helpers
    PointToPointHelper bottleNeckLink;
    bottleNeckLink.SetDeviceAttribute("DataRate", StringValue(bottleNeckLinkBw));
    bottleNeckLink.SetChannelAttribute("Delay", StringValue(bottleNeckLinkDelay));

    PointToPointHelper pointToPointLeaf;
    pointToPointLeaf.SetDeviceAttribute("DataRate", StringValue(pointToPointLeafLinkBw));
    pointToPointLeaf.SetChannelAttribute("Delay", StringValue(pointToPointLeafLinkDelay));
    pointToPointLeaf.SetQueue("ns3::DropTailQueue",
                              "MaxSize",
                              StringValue(std::to_string(bandwidth_delay_product) + "p"));

    PointToPointDumbbellHelper d(nLeaf, pointToPointLeaf, nLeaf, pointToPointLeaf, bottleNeckLink);

    Ptr<RateErrorModel> em = CreateObject<RateErrorModel>();
    em->SetAttribute("ErrorRate", DoubleValue(packetLossRate));
    d.m_routerDevices.Get(0)->SetAttribute("ReceiveErrorModel", PointerValue(em));

    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue("ns3::TcpNewReno"));
    // Install Stack
    InternetStackHelper stackNewReno;
    for (uint32_t i = 0; i < d.LeftCount(); i += 2)
    {
        stackNewReno.Install(d.GetLeft(i));
    }
    for (uint32_t i = 0; i < d.RightCount(); i += 2)
    {
        stackNewReno.Install(d.GetRight(i));
    }

    stackNewReno.Install(d.GetLeft());
    stackNewReno.Install(d.GetRight());
    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue(algo));
    InternetStackHelper stack;
    for (uint32_t i = 1; i < d.LeftCount(); i += 2)
    {
        stack.Install(d.GetLeft(i));
    }
    for (uint32_t i = 1; i < d.RightCount(); i += 2)
    {
        stack.Install(d.GetRight(i));
    }

    // Assign IP Addresses
    d.AssignIpv4Addresses(Ipv4AddressHelper("10.1.1.0", "255.255.255.0"),
                          Ipv4AddressHelper("10.2.1.0", "255.255.255.0"),
                          Ipv4AddressHelper("10.3.1.0", "255.255.255.0"));

    // Install packetsink app on all left side nodes
    uint16_t port = 9;
    Address sinkLocalAddress(InetSocketAddress(Ipv4Address::GetAny(), port));
    PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory", sinkLocalAddress);
    ApplicationContainer sinkApps;
    for (uint32_t i = 0; i < d.LeftCount(); ++i)
    {
        sinkApps.Add(packetSinkHelper.Install(d.GetLeft(i)));
    }
    sinkApps.Start(Seconds(0.0));
    sinkApps.Stop(Seconds(simulationTime));

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    Ptr<FlowMonitor> flowMonitor;
    FlowMonitorHelper flowHelper;
    flowMonitor = flowHelper.InstallAll();
    for (uint i = 0; i < 2; i++)
    {
        Ptr<Socket> ns3TcpSocket =
            Socket::CreateSocket(d.GetRight(i), TcpSocketFactory::GetTypeId());

        Ptr<TutorialApp> app = CreateObject<TutorialApp>();
        app->Setup(ns3TcpSocket,
                   Address(InetSocketAddress(d.GetLeftIpv4Address(i), port)),
                   1024,
                   simulationTime,
                   DataRate(pointToPointLeafLinkBw));
        d.GetRight(i)->AddApplication(app);
        app->SetStartTime(Seconds(1.));
        app->SetStopTime(Seconds(simulationTime - 1));
        std::string str = "scratch/flow" + std::to_string(i + 1) + ".cwnd";
        AsciiTraceHelper asciiTraceHelper;
        Ptr<OutputStreamWrapper> stream = asciiTraceHelper.CreateFileStream(str);
        ns3TcpSocket->TraceConnectWithoutContext("CongestionWindow",
                                                 MakeBoundCallback(&CwndChange, stream));
    }

    // Simulator::Schedule(Seconds(1.1), &calculateThroughput, flowMonitor);
    Simulator::Stop(Seconds(simulationTime + 1));
    Simulator::Run();

    double jain_num=0, jain_denom=0;
    int count = 0;
    double throughputArr[] = {0, 0};
    FlowMonitor::FlowStatsContainer stats = flowMonitor->GetFlowStats();
    for (auto iter = stats.begin(); iter != stats.end(); ++iter)
    {
        if (count % 2 == 0)
        {
            throughputArr[0] += iter->second.rxBytes;
        }
        if (count % 2 == 1)
        {
            throughputArr[1] += iter->second.rxBytes;
        }
        count +=1;

        double throughput_i = iter->second.rxBytes * 8.0 / ((simulationTime)*1000);//Kbps
        jain_num += throughput_i;
        jain_denom += (throughput_i * throughput_i);
    }
    double jain_index =
        (jain_num * jain_num) / (count * jain_denom);
    throughputArr[0] = (throughputArr[0] * 8) / ((simulationTime)*1000);
    throughputArr[1] = (throughputArr[1] * 8) / ((simulationTime)*1000);
    if (file == 1)
    {
        myfile << bottleneckDataRate << "\t" << throughputArr[0] << "\t" << throughputArr[1]
               << "\t" << jain_index << std::endl;
    }
    else if (file == 2)
    {
        myfile << packetLossRate << "\t" << throughputArr[0] << "\t" << throughputArr[1]
               << "\t" << jain_index << std::endl;
    }
    Simulator::Destroy();

    return 0;
}
