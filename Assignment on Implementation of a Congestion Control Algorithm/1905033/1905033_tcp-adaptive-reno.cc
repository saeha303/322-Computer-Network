#include "1905033_tcp-adaptive-reno.h"

#include "rtt-estimator.h"
#include "tcp-socket-base.h"

#include "ns3/log.h"
#include "ns3/simulator.h"

NS_LOG_COMPONENT_DEFINE("TcpAdaptiveReno");

namespace ns3
{

NS_OBJECT_ENSURE_REGISTERED(TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId(void)
{
    static TypeId tid =
        TypeId("ns3::TcpAdaptiveReno")
            .SetParent<TcpNewReno>()
            .SetGroupName("Internet")
            .AddConstructor<TcpAdaptiveReno>()
            .AddAttribute(
                "FilterType",
                "Use this to choose no filter or Tustin's approximation filter",
                EnumValue(TcpAdaptiveReno::TUSTIN),
                MakeEnumAccessor(&TcpAdaptiveReno::m_fType),
                MakeEnumChecker(TcpAdaptiveReno::NONE, "None", TcpAdaptiveReno::TUSTIN, "Tustin"))
            .AddTraceSource("EstimatedBW",
                            "The estimated bandwidth",
                            MakeTraceSourceAccessor(&TcpAdaptiveReno::m_currentBW),
                            "ns3::TracedValueCallback::Double");
    return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno(void)
    : TcpWestwoodPlus(),
      m_RTT_min(Time(0)),
      m_RTT_curr(Time(0)),
      m_RTT_jth_pl(Time(0)),
      m_RTT_conj(Time(0)),
      m_RTT_conj_prev(Time(0)),
      m_incWnd(0),
      m_baseWnd(0),
      m_probeWnd(0)
{
    NS_LOG_FUNCTION(this);
}

TcpAdaptiveReno::TcpAdaptiveReno(const TcpAdaptiveReno& sock)
    : TcpWestwoodPlus(sock),
      m_RTT_min(Time(0)),
      m_RTT_curr(Time(0)),
      m_RTT_jth_pl(Time(0)),
      m_RTT_conj(Time(0)),
      m_RTT_conj_prev(Time(0)),
      m_incWnd(0),
      m_baseWnd(0),
      m_probeWnd(0)
{
    NS_LOG_FUNCTION(this);
    NS_LOG_LOGIC("Invoked the copy constructor");
}

TcpAdaptiveReno::~TcpAdaptiveReno(void)
{
}

/*
The function is called every time an ACK is received (only one time
also for cumulative ACKs) and contains timing information
*/
void
TcpAdaptiveReno::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{
    NS_LOG_FUNCTION(this << tcb << packetsAcked << rtt);

    if (rtt.IsZero())
    {
        NS_LOG_WARN("RTT measured is zero!");
        return;
    }

    m_ackedSegments += packetsAcked;
    // calculate min rtt here
    if (m_RTT_min.IsZero()) // at first
    {
        m_RTT_min = rtt;
    }
    if (rtt <= m_RTT_min)
    {
        m_RTT_min = rtt;
    }

    m_RTT_curr = rtt;
    // if (!(rtt.IsZero() || m_IsCount))
    // {
    //     m_IsCount = true;
    //     m_bwEstimateEvent.Cancel();
        TcpWestwoodPlus::EstimateBW(rtt, tcb);
    // }
    NS_LOG_LOGIC("Min RTT is "<<m_RTT_min.GetMilliSeconds() << "ms");
    NS_LOG_LOGIC ("Current Rtt is " << m_RTT_curr.GetMilliSeconds () << "ms");
}

double
TcpAdaptiveReno::EstimateCongestionLevel()
{
    NS_LOG_FUNCTION(this);
    float a = 0.85; // exponential smoothing factor
    if (m_RTT_conj_prev < m_RTT_min)
    {
        a = 0;
    }
    double RTT_conj_j =
        a * m_RTT_conj_prev.GetSeconds() +
        (1 - a) * m_RTT_jth_pl.GetSeconds();
    m_RTT_conj = Seconds(RTT_conj_j);
    NS_LOG_LOGIC("Conjestion rtt is " << m_RTT_conj << " ; m_RTT_conj_prev : " << m_RTT_conj_prev << " ; jth packet loss rtt : " << m_RTT_jth_pl);
    return std::min((m_RTT_curr.GetSeconds() - m_RTT_min.GetSeconds()) /
                        (m_RTT_conj.GetSeconds() - m_RTT_min.GetSeconds()),
                    1.0);
}

void
TcpAdaptiveReno::EstimateIncWnd(Ptr<TcpSocketState> tcb)
{
    double congestion = EstimateCongestionLevel();
    int M = 1000;

    double w_max_inc = m_currentBW.Get().GetBitRate() / M *
                         static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize);
    double alpha = 10;
    double beta = 2 * w_max_inc * ((1 / alpha) - ((1 / alpha + 1) / (std::exp(alpha))));
    double gamma = 1 - (2 * w_max_inc * ((1 / alpha) - ((1 / alpha + 0.5) / (std::exp(alpha)))));

    m_incWnd = (int)((w_max_inc / std::exp(alpha * congestion)) + (beta * congestion) + gamma);

    NS_LOG_LOGIC("maxInc: " << w_max_inc << "; congestion: " << congestion << " ; beta: " << beta
                            << " ; gamma: " << gamma
                            << " ; exp(alpha * congestion): " << std::exp(alpha * congestion));
    NS_LOG_LOGIC("m_incWnd: " << m_incWnd << " ; prev_wind: " << tcb->m_cWnd
                              << " ; new: " << (m_incWnd / (int)tcb->m_cWnd));
}

void
TcpAdaptiveReno::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    NS_LOG_FUNCTION(this << tcb << segmentsAcked);
    if (segmentsAcked > 0)
    {
        EstimateIncWnd(tcb);
        double adder =
            static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize) / tcb->m_cWnd.Get();
        adder = std::max(1.0, adder);
        m_baseWnd += static_cast<uint32_t>(adder);

        // change probe window
        m_probeWnd = std::max((double)(m_probeWnd + m_incWnd / (int)tcb->m_cWnd.Get()), (double)0);
        tcb->m_cWnd = m_baseWnd + m_probeWnd;
        NS_LOG_LOGIC("Congestion window before " << tcb->m_cWnd << " ; base window " << m_baseWnd << " ; probe window "
                               << m_probeWnd<<"; New congestion window: "<<tcb->m_cWnd);
    }
}

uint32_t
TcpAdaptiveReno::GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight)
{
    NS_LOG_FUNCTION(this);
    m_RTT_conj_prev = m_RTT_conj;// update the RT Tj−1cong & RT Tj
    m_RTT_jth_pl =
        m_RTT_curr; // also jth packet loss RTT=current RTT
    double congestion = EstimateCongestionLevel();
    uint32_t minssthresh=2 * tcb->m_segmentSize;
    uint32_t w_base=(uint32_t)(tcb->m_cWnd / (1.0 + congestion));
    uint32_t ssthresh =
        std::max(minssthresh, w_base);
    m_baseWnd = ssthresh;
    m_probeWnd = 0;
    NS_LOG_LOGIC("Congestion : " << congestion << " ;"
                                   << "; New ssthresh : " << ssthresh);
    return ssthresh;
}

Ptr<TcpCongestionOps>
TcpAdaptiveReno::Fork()
{
    return CreateObject<TcpAdaptiveReno>(*this);
}

} // namespace ns3